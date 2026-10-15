package io.elasticore.base.util;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TextTemplate {

    private StringBuilder sb = new StringBuilder();

    private Map<String, Object> variableMap = new HashMap<>();

    public TextTemplate() {

    }

    /**
     * @param initMsg
     */
    public TextTemplate(String initMsg) {
        sb.append(initMsg);
    }

    /**
     * append new line
     *
     * @return
     */
    public TextTemplate line() {
        sb.append(System.lineSeparator());

        return this;
    }

    /**
     * append text
     *
     * @param msg
     * @return
     */
    public TextTemplate add(String msg) {
        sb.append(msg);

        return this;
    }


    /**
     * append text after new line
     *
     * @param msg
     * @return
     */
    public TextTemplate line(String msg) {
        line();
        sb.append(msg);

        return this;
    }

    /**
     * set replace information
     *
     * @param key
     * @param value
     * @return
     */
    public TextTemplate set(String key, Object value) {
        String keyName = key;
        if (key.indexOf("$") < 0) {
            keyName = "${" + key + "}";
        }
        variableMap.put(keyName, value);

        return this;

    }

    /**
     * get full conversion text
     *
     * @return
     */
    public String toString() {
        String tmpMsg = sb.toString();

        Set<String> set = variableMap.keySet();

        for (String key : set) {
            Object val = variableMap.get(key);

            String valTxt = "";
            if (val != null) valTxt = val.toString();

            StringBuffer sb = replaceText(tmpMsg, key, valTxt);

            tmpMsg = sb.toString();
        }

        return tmpMsg;
    }


    /**
     * replace text
     *
     * @param strTarget
     * @param strSearch
     * @param strReplace
     * @return
     */
    static StringBuffer replaceText(String strTarget, String strSearch, String strReplace) {
        String strCheck = new String(strTarget);
        StringBuffer strBuf = new StringBuffer();
        while (strCheck.length() != 0) {
            int begin = strCheck.indexOf(strSearch);
            if (begin == -1) {
                strBuf.append(strCheck);
                break;
            } else {
                int end = begin + strSearch.length();
                strBuf.append(strCheck.substring(0, begin));
                strBuf.append(strReplace);
                strCheck = strCheck.substring(end);
            }
        }
        return strBuf;
    }

}