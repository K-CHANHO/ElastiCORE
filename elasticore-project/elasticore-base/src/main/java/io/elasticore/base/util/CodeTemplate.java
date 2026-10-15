package io.elasticore.base.util;



import io.elasticore.base.model.ConstantParam;
import lombok.SneakyThrows;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 */

public class CodeTemplate {



    private static final String NEWLINE = "\n";

    private List<LineInfo> lineInfos = new ArrayList<>();

    private CodeTemplate() {
    }

    private static String getRootDir() {
        String basePath = System.getProperty("elasticore.base.path");
        if (basePath == null) {
            basePath = System.getProperty("user.dir");
        }

        return basePath;
    }


    private static InputStream getTemplateInputStream(String resourcePath) throws FileNotFoundException {
        InputStream inputStream = null;

        int p = resourcePath.indexOf("file:");

        if(p>=0) {
            String url = resourcePath.substring(p);
            ConsoleLog.storeLog("TEMPLATE",url);
            try {

                URL fileUrl = new URL(url);
                inputStream = fileUrl.openStream();
            }catch (Exception e) {
                throw new FileNotFoundException(url);
            }
        }
        else if (resourcePath.indexOf("resource://") == 0) {

            String path = getRootDir() + "/"+ ConstantParam.PROPERTY_ELCORE_HOME+"/" + resourcePath.substring(11);


            ConsoleLog.storeLog("TEMPLATE",path);
            inputStream = new FileInputStream(path);
        } else {
            // Built-in classpath templates are logged separately from user-defined ones
            ConsoleLog.storeLog("TEMPLATE_BUILTIN", resourcePath);
            inputStream = CodeTemplate.class.getClassLoader().getResourceAsStream(resourcePath);
        }

        return inputStream;
    }

    public static CodeTemplate newInstance() {
        return new CodeTemplate();
    }


    @SneakyThrows
    public static CodeTemplate newInstance(String resourcePath) {
        //ConsoleLog.printInfo("Template: "+resourcePath);
        CodeTemplate codeTemplate = new CodeTemplate();
        InputStream inputStream = getTemplateInputStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        // Always read templates as UTF-8. On Windows the default is MS949/CP949 which mangles
        // Korean (and other non-ASCII) characters embedded in user-authored .tmpl files such as
        // custom control.tmpl. The generated files are written via FileBasedSourceFileAccessFactory
        // which already uses UTF-8, so both ends must agree.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            Stream<String> lines = reader.lines();
            lines.forEach(line -> {
                if (line.contains("List}")) {
                    codeTemplate.line(line, true);
                } else {
                    codeTemplate.line(line);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Resource parse error.", e);
        }
        return codeTemplate;
    }


    /**
     * append new line
     *
     * @return CodeTemplate
     */
    public CodeTemplate line() {

        this.lineInfos.add(new NewLineInfo());
        return this;
    }

    /**
     * get full conversion text
     *
     * @return String
     */
    public String toString(Parameters parameters) {
        StringBuilder sb = new StringBuilder();

        boolean isSkip = false;
        boolean conditionMatched = false; // Track if condition has been satisfied

        for (LineInfo lineInfo : lineInfos) {
            String lineTxt = lineInfo.toString(parameters);

            boolean isCommandLine = false;
            
            // Detect print-if, print-elseif, print-if:end commands
            if(lineTxt.indexOf("/*-- print-if:")>=0 || lineTxt.indexOf("/*-- print-elseif:")>=0) {
                boolean isEndLine = lineTxt.indexOf("end")>0;
                boolean isElseIfLine = lineTxt.indexOf("/*-- print-elseif:")>=0;
                boolean isIfLine = lineTxt.indexOf("/*-- print-if:")>=0 && !isEndLine;
                boolean conditionTrue = lineTxt.indexOf("true")>0;
                
                isCommandLine = true;

                if(isEndLine) {
                    // End block
                    isSkip = false;
                    conditionMatched = false;
                } else if(isIfLine) {
                    // Start if block
                    conditionMatched = conditionTrue;
                    isSkip = !conditionTrue;
                } else if(isElseIfLine) {
                    // Handle elseif block (execute when previous condition false and current condition false) - acts as else
                    if(!conditionMatched && !conditionTrue) {
                        conditionMatched = true;
                        isSkip = false;
                    } else {
                        isSkip = true;
                    }
                }
            }

            if(!isSkip && !isCommandLine)
                sb.append(lineTxt).append(NEWLINE);
        }

        return sb.toString();
    }

    /**
     * @param line
     * @param isMultiLine
     * @return
     */
    public CodeTemplate line(String line, boolean isMultiLine) {
        return line(line, isMultiLine, null);
    }


    public CodeTemplate line(String line, boolean isMultiLine, String lineSuffix) {
        LineInfo lineInfo = new RegxLineInfo(line, isMultiLine, lineSuffix);
        this.lineInfos.add(lineInfo);

        return this;
    }

    public CodeTemplate line(String line) {
        return line(line, false);
    }

    public static Paragraph newParagraph() {
        return new Paragraph(false);
    }

    public static Paragraph newParagraph(boolean isUnique) {
        return new Paragraph(isUnique);
    }

    public static Parameters newParameters() {
        return new Parameters();
    }

    /**
     *
     */
    private interface LineInfo {
        String toString(Parameters parameters);
    }


    /**
     *
     */
    private static class NewLineInfo implements LineInfo {

        @Override
        public String toString(Parameters parameters) {
            return "";
        }
    }

    /**
     *
     */
    private static class RegxLineInfo implements LineInfo {
        private final static String regex = "\\$\\{(.+?)\\}";

        private Pattern pattern;
        private Matcher matcher;

        private String lineText;
        private String lineSuffix;

        private String[] keyNames;
        private boolean isMultiLine = false;

        private List<String> keyNameList = new ArrayList<>();

        RegxLineInfo(String lineText, boolean isMultiLine, String lineSuffix) {
            this.lineText = lineText;
            this.isMultiLine = isMultiLine;
            this.lineSuffix = lineSuffix;

            this.pattern = Pattern.compile(regex);
            this.matcher = pattern.matcher(lineText);

            while (matcher.find()) {
                String keyNm = matcher.group(1);
                keyNameList.add(keyNm);
            }
        }

        private int getMaxCount(Parameters parameters) {
            int maxCount = 0;
            for (String keyNm : keyNameList) {
                Paragraph paragraph = parameters.getParamMap().get(keyNm);
                if (paragraph == null) continue;
                int valueCount = paragraph.size();
                if (valueCount > maxCount) {
                    maxCount = valueCount;
                }
            }

            return maxCount;
        }

        public String toString(Parameters parameters) {
            if (keyNameList.size() == 0) {
                return this.lineText;
            }

            int maxCount = getMaxCount(parameters);

            List<String> lineList = new ArrayList<>();

            String replaceTxt = lineText;
            for (int i = 0; i < maxCount; i++) {
                String regTmpTxt = replaceTxt;
                for (String keyNm : keyNameList) {
                    Paragraph paragraph = parameters.getParamMap().get(keyNm);
                    if (paragraph == null) continue;
                    String valueTxt = paragraph.toString(i);
                    regTmpTxt = regTmpTxt.replace("${" + keyNm + "}", valueTxt);
                }
                lineList.add(regTmpTxt);
            }

            boolean isFirst= true;
            StringBuilder sb = new StringBuilder();
            for (String line : lineList) {
                if(!isFirst)
                    sb.append("\n");
                isFirst = false;
                sb.append(line);
                if (lineSuffix != null)
                    sb.append(lineSuffix);
            }
            return sb.toString();
        }
    }

    public static class Parameters {

        private Map<String, Paragraph> paramMap = new HashMap<>();

        private Parameters() {
        }

        private Map<String, Paragraph> getParamMap() {
            return this.paramMap;
        }

        public Parameters set(String keyName, boolean value) {
            return set(keyName, Boolean.toString(value));
        }

        public Parameters set(String keyName, String value) {
            if(value!=null)
                paramMap.put(keyName, new Paragraph(true, value));
            return this;
        }

        public Parameters set(String keyName, Paragraph paragraph) {
            paramMap.put(keyName, paragraph);
            return this;
        }
    }

    public static class Paragraph {

        private List<Object> valueList = new ArrayList<>();

        private Map<Object, Object> checkMap;

        private Paragraph(boolean isUnique) {
            if (isUnique)
                checkMap = new HashMap<>();
        }

        private Paragraph(boolean isUnique, Object val) {
            this(isUnique);
            valueList.add(val);
        }

        public Paragraph add(String template, Object... parameter) {
            String code = String.format(template, parameter);
            return add(code);
        }

        public boolean contains(String content) {
            for(Object t:valueList) {
                if(t!=null && t.toString().contains(content))
                    return true;
            }
            return false;
        }

        public Paragraph add(Paragraph p) {
            if(p!=null) {
                int size = p.size();
                for(int i=0; i<size; i++) {
                    add(p.toString(i));
                }
            }

            return this;
        }

        public Paragraph add(Object val) {

            if (val != null) {

                if (checkMap != null) {
                    if (checkMap.containsKey(val))
                        return this;

                    checkMap.put(val, val);
                }
                String txt = val.toString();
                String[] linesArray = txt.split("\n");
                for (String line : linesArray) {
                    valueList.add(line);
                }


            }
            return this;
        }

        public int size() {
            return this.valueList.size();
        }

        public String toString() {
            return toString(0);
        }

        public String toString(int idx) {
            try {
                return this.valueList.get(idx).toString();
            } catch (NullPointerException npe) {
                if (valueList.size() == 1) {
                    try {
                        return valueList.get(0).toString();
                    } catch (NullPointerException noe2) {

                    }
                }
                return "";
            }
        }

    }
}