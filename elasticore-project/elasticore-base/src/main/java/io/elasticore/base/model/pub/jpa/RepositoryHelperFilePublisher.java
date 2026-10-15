package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.ConstantParam;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.BaseModelDomain;
import io.elasticore.base.model.core.Items;
import io.elasticore.base.model.core.ListMap;
import io.elasticore.base.model.dto.DataTransfer;
import io.elasticore.base.model.entity.*;
import io.elasticore.base.model.repo.Method;
import io.elasticore.base.model.repo.Repository;
import io.elasticore.base.model.repo.RepositoryModels;
import io.elasticore.base.model.repo.SqlQueryInfo;
import io.elasticore.base.util.CodeTemplate;
import io.elasticore.base.util.ConsoleLog;
import io.elasticore.base.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepositoryHelperFilePublisher extends SrcFilePublisher {

    private CodeTemplate baseCodeTmpl;

    private CodePublisher publisher;


    private String packageName;
    private String entityPackageName;
    private String dtoPackageName;


    public RepositoryHelperFilePublisher(CodePublisher publisher) {
        super(publisher);

        this.publisher = publisher;

        ECoreModel eCoreModel = this.publisher.getECoreModelContext().getDomain().getModel();

        String templatePath = eCoreModel.getConfig("template.templateHelper");
        if (templatePath == null)
            templatePath = "elasticore-template/repositoryHelper.tmpl";
        else
            templatePath = "resource://"+templatePath;

        this.baseCodeTmpl = CodeTemplate.newInstance(templatePath);

        ECoreModel model = publisher.getECoreModelContext().getDomain().getModel();
        this.packageName = model.getNamespace(ConstantParam.KEYNAME_REPOSITORY);

        if(eCoreModel.getEntityModels().getItems().size()>0)
            this.entityPackageName = model.getNamespace(ConstantParam.KEYNAME_ENTITY);

        if(eCoreModel.getDataTransferModels().getItems().size()>0)
            this.dtoPackageName = model.getNamespace(ConstantParam.KEYNAME_DTO);

    }

    public void publish(ModelDomain domain, RepositoryModels repositoryModels) {

        if(this.packageName==null) return;

        CodeTemplate.Parameters params = CodeTemplate.newParameters();

        String name = StringUtils.snakeToCamel(domain.getName(),"-");

        String classNm = StringUtils.capitalize(name)+"RepositoryHelper";

        params
                .set("packageName", packageName)
                .set("j2eePkgName",getPersistentPackageName(domain))
                .set("entityPackageName", entityPackageName)
                .set("dtoPackageName", dtoPackageName)
                .set("className", classNm);


        CodeTemplate.Paragraph fieldP = CodeTemplate.newParagraph();
        CodeTemplate.Paragraph methodP = CodeTemplate.newParagraph();
        Set<String> namespaceSet = new HashSet<>();
        loadRepositoryInfo(repositoryModels, fieldP, methodP ,namespaceSet);

        CodeTemplate.Paragraph importList = CodeTemplate.newParagraph();
        for(String ns: namespaceSet) {
            importList.add(ns);
        }
        params.set("importList",importList);


        if(fieldP.size()==0 && methodP.size()==0)
            return;

        params.set("repositoryList", fieldP);
        params.set("methodList", methodP);

        String code = baseCodeTmpl.toString(params);
        String qualifiedClassName = packageName + "." + classNm;

        this.writeSrcCode(this.publisher, null, qualifiedClassName, code);
    }

    private boolean isEnable(Repository repo) {
        String targetModelName = repo.getIdentity().getName();
        ModelDomain modelDomain =BaseModelDomain.getModelDomain(repo.getIdentity().getDomainId());

        //Entity entity = modelDomain.getModel().getEntityModels().findByName(targetModelName);
        Entity entity = this.publisher.getECoreModelContext().findModelComponent(targetModelName, Entity.class);
        if(entity==null)
            return true;


        PkField pkField = entity.findPkField(modelDomain);
        if (entity == null
                //|| entity.getMetaInfo().hasMetaAnnotation("abstract")
                || pkField == null) {
            return false;
        }

        return true;
    }

    private void loadRepositoryInfo(RepositoryModels repositoryModels, CodeTemplate.Paragraph fieldP, CodeTemplate.Paragraph methodP , Set<String> namespaceSet) {
        //private final ContactInfoRepository contactInfo;
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
        ModelComponentItems<Repository> repoItems = repositoryModels.getItems();

        ECoreModel model = publisher.getECoreModelContext().getDomain().getModel();

        while (repoItems.hasNext()) {
            Repository repo = repoItems.next();
            if(!isEnable(repo))
                continue;

            String repoName = repo.getIdentity().getName();

            //Entity entity = model.getEntityModels().findByName(repoName);
            Entity entity = this.publisher.getECoreModelContext().findModelComponent(repoName, Entity.class);
            PkField pkField = entity.getPkField();

            String fieldNm = null;
            if(entity!=null) {
                MetaInfo metaInfo = entity.getMetaInfo();
                // except metaInfo.hasMetaAnnotation(EntityAnnotation.META_ROLL_UP)
                if( metaInfo.hasMetaAnnotation(EntityAnnotation.META_EMBEDDABLE)
                        || (metaInfo.hasMetaAnnotation(EntityAnnotation.META_ABSTRACT) && pkField==null)
                )
                    continue;

            /*if(entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_ABSTRACT))
                continue;*/

                fieldNm = StringUtils.uncapitalize(repoName);


                fieldP.add("private final %s %s;", repoName + "Repository", fieldNm);
                fieldP.add("");
            }


            /*
            public List<ContractGroupDTO2> selectCnctCustList(Integer grpSeq) {


        String[] fieldNames = { "carSeq","contrNm","custNm"};

        List<Object[]> result = contactInfo.selectCnctCustList(grpSeq);
        return new ModelTransList<ContractGroupDTO2>(result, ContractGroupDTO2.class, fieldNames);
    }
             */


            ModelComponentItems<Method> methods = repo.getItems();
            while (methods.hasNext()) {
                Method method = methods.next();

                if (method.getQueryInfo() != null) {
                    if (method.getQueryInfo().isDynamicQuery() || fieldNm ==null) {
                        makeDynamicQueryMethod(method, methodP);
                    } else if (method.getQueryInfo().isOwnOutputDTO()) {

                        String paramType = StringUtils.findParameterType(method.getReturnType());
                        if(paramType == null)
                            continue;

                        //if("Object[]".equals(paramType))
                        {
                            makeTransformMethod(method, methodP, fieldNm);
                        }

                    }
                }
            }

        }

        String[] extEntities = this.publisher.getECoreModelContext().getDomain().getModel().getExternalEntitiesAsArray();
        for(String extEntityName : extEntities) {
            Entity extEntity = this.publisher.getECoreModelContext().findModelComponent(extEntityName, Entity.class);
            if(extEntity!=null) {
                MetaInfo metaInfo = extEntity.getMetaInfo();
                if(metaInfo.hasMetaAnnotation(EntityAnnotation.META_ROLL_UP)
                        ||metaInfo.hasMetaAnnotation(EntityAnnotation.META_EMBEDDABLE)
                        ||metaInfo.hasMetaAnnotation(EntityAnnotation.META_ABSTRACT)
                )
                    continue;
            }

            String repoName = extEntityName + "Repository";
            fieldP.add("private final %s %s;", repoName, StringUtils.uncapitalize(extEntityName));
            fieldP.add("");

            String ns = getNamespace(extEntityName, "repository");
            namespaceSet.add(ns+"."+repoName);
        }
    }


    protected String getMappingFieldNames(Items<Field> selectFieldItems, DataTransfer dto) {

        ListMap<String,Field> allFieldListMap = dto.getAllFieldListMap();


        StringBuilder sb = new StringBuilder();
        List<Field> itemList = selectFieldItems.getItemList();

        String notFoundField = null;
        for(Field f: itemList) {
            String fieldName = f.getName();
            if( allFieldListMap.getIgnoreCase(fieldName) ==null) {
                notFoundField = fieldName;
                continue;
            }

            if(notFoundField!=null)
                throw new IllegalArgumentException("Field: "+notFoundField+ " is not exist in DTO");


            if(sb.length()>0)
                sb.append(" ,");

            sb.append(StringUtils.quoteString(fieldName));
        }

        if(notFoundField!=null) {
            ConsoleLog.storeLog("getMappingFieldNames", "Field: "+notFoundField+ " is not exist in DTO");
        }

        return sb.toString();
    }

    private boolean hasReplaceFieldName(String template, String fieldName) {
        if(template==null) return false;

        if( template.indexOf("/*${"+fieldName+"}*/")<0)
            return false;

        return true;
    }

    private String getParamInfo(String varName, String fieldName) {
        if(varName==null || varName.isEmpty())
            return fieldName;

        return varName+".get"+StringUtils.capitalize(fieldName)+"()";
    }

    /**
     * public List<ContractGroup> findAllConnectionGroups(String groupName, Integer grpSeq) {
     * StringBuilder sb = new StringBuilder("SELECT * FROM T_CNCT_GRP");
     * sb.append( " WHERE 1=1 ");
     * <p>
     * if(groupName!=null && !groupName.isEmpty())
     * sb.append(" AND group_name like CONCAT('%', :groupName, '%') ");
     * <p>
     * if(grpSeq!=null)
     * sb.append(" AND grp_seq = :grpSeq");
     * <p>
     * Query query = entityManager.createNativeQuery(sb.toString(), ContractGroup.class);
     * <p>
     * if(groupName!=null && !groupName.isEmpty())
     * query.setParameter("groupName" , groupName);
     * <p>
     * if(grpSeq!=null)
     * query.setParameter("grpSeq" , grpSeq);
     * <p>
     * <p>
     * return query.getResultList();
     * }
     */
    private void makeDynamicQueryMethod(Method method, CodeTemplate.Paragraph p) {

        SqlQueryInfo queryInfo = method.getQueryInfo();
        if (queryInfo == null) return;
        String sqlQuery = queryInfo.getSqlTxt();

        //if (sqlQuery == null || sqlQuery.indexOf("/* if:") < 0) return;


        List<String> namedParamList = queryInfo.getNamedParamerList();

        DataTransfer outputDTO = queryInfo.getDataTransfer();

        Items<Field> methodParams = method.getParams();

        Set<String> paramFieldNmSet = new HashSet<>();
        for(Field f:methodParams.getItemList()) {
            paramFieldNmSet.add(f.getName());
        }
        Pattern patternVars = Pattern.compile(":(\\w+)");
        //Pattern patternVars = Pattern.compile(":(\\w+)(?=(?:(?!/\\*).)*?(?<!\\*/))");


        String params = getParametersForMethod(methodParams, true);
        String inputType = method.getInputType();
        String inputTyprVarName = null;
        if(inputType!=null) {
            inputTyprVarName = "input";
            params = inputType +" "+inputTyprVarName;

            if( methodParams.size() ==0) {
                String cleanedInput = sqlQuery.replaceAll("/\\*.*?\\*/", "");
                Matcher matcherVars = patternVars.matcher(cleanedInput);

                while (matcherVars.find()) {
                    String varName = matcherVars.group(1);
                    paramFieldNmSet.add(varName);
                }
            }
        }
        String fieldNames = null;

        boolean isSingleResult = false;
        TypeInfo typeInfo = null;

        String outputClassNm = extractContentBetweenBrackets(method.getReturnType());
        if (queryInfo.isNativeQuery()) {
            if (queryInfo.isOwnOutputDTO()) {

                DataTransfer outputDTO2 = queryInfo.getDataTransfer();
                if(outputDTO2==null) {
                    System.err.println("queryInfo return type error:" +queryInfo.getJpaMethodName());
                }
                outputClassNm = outputDTO2.getIdentity().getName();

                Items<Field> selectFieldItems = queryInfo.getSelectFieldItems();
                fieldNames = getMappingFieldNames(selectFieldItems, outputDTO2);

                //fieldNames = getMappingFieldNames(outputDTO2, queryInfo.getSelectColumnCount());



            } else {
                typeInfo = new TypeInfo(method.getReturnType());
                if(typeInfo.isBaseType()) {
                    isSingleResult = true;
                    outputClassNm = typeInfo.getDefaultTypeName();
                }
                else {
                    outputClassNm = "Object[]";
                }


            }

        }
        String methodName = method.getName();

        //boolean isPageable = method.isPageable() && !queryInfo.isNativeQuery();
        boolean isPageable = method.isPageable();
        if(isSingleResult)
            p.add("public %s %s(%s) {", outputClassNm, methodName, params);
        else if(isPageable)
            p.add("public Page<%s> %s(%s ,Pageable pageable) {", outputClassNm, methodName, params);
        else
            p.add("public List<%s> %s(%s) {", outputClassNm, methodName, params);

        if (fieldNames != null)
            p.add("    String[] fieldNames = {%s};", fieldNames);

        p.add("    StringBuilder sb = new StringBuilder();");


        String[] lines = sqlQuery.split("\n");
        //Pattern pattern = Pattern.compile("(/\\* if:(\\w+) \\*/)$");
        Pattern pattern = Pattern.compile("/\\* if:(.*?) \\*/");




        List<String> varConditionList = new ArrayList<>();
        Map<String, List<String>> varChildListMap = new HashMap<>();

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                // Extracting variable name
                String varConditionChk = matcher.group(1);
                // Removing the pattern from the line
                String modifiedLine = line.replaceAll("/\\* if:(.*?) \\*/", "").trim();


                if(varConditionChk.indexOf(".") >0 || varConditionChk.indexOf(" ")>0 || varConditionChk.indexOf("!")>0 || varConditionChk.indexOf("=")>0) {

                }
                else {
                    varConditionChk = getParamInfo(inputTyprVarName, varConditionChk)+" !=null";
                }

                varConditionList.add(varConditionChk );

                Matcher matcherVars = patternVars.matcher(modifiedLine);
                List<String> varNmList = new ArrayList<>();
                while (matcherVars.find()) {
                    String varName = matcherVars.group(1);
                    varNmList.add(varName);
                }
                varChildListMap.put(varConditionChk, varNmList);


                p.add("    if(" + varConditionChk + ")");
                p.add("      sb.append(\"" + modifiedLine + " \");");
            } else {
                p.add("    sb.append(\"" + line + " \");");
            }
        }

        p.add("    java.util.Map<String, Object> placeholders = new java.util.HashMap<>();");
        Items<Field> params1 = method.getParams();
        List<Field> itemList = params1.getItemList();
        for(Field fld:itemList) {
            if(hasReplaceFieldName(sqlQuery,fld.getName()))
                p.add("    placeholders.put(%s ,%s);", StringUtils.quoteString(fld.getName()), fld.getName());
        }

        p.add("    String sql = ModelTransList.replace(sb.toString(),  placeholders);");

        String classNm = "null";
        if (queryInfo.isNativeQuery()) classNm = outputClassNm+".class";

        String pageableStr= "null";
        if(isPageable)
            pageableStr= "pageable";



        p.add("    Query query = createQuery(sql, %s, %s, %s);", queryInfo.isNativeQuery(),classNm,pageableStr);


        Set<String> paramKeyNameSet = new HashSet<>();

        for (String varCondition : varConditionList) {
            if(hasReplaceFieldName(sqlQuery,varCondition))
                continue;

            p.add("    if(" + varCondition + ") {");

            for(String varNm: varChildListMap.get(varCondition)) {
                p.add("      query.setParameter(\"" + varNm + "\" , " + getParamInfo(inputTyprVarName,varNm) + ");");
                paramKeyNameSet.add(varNm);
            }

            p.add("    }");
        }

        boolean hasCustomDefinedParam = false;
        for(String inputParamName: paramFieldNmSet) {
            hasCustomDefinedParam = true;

            if(hasReplaceFieldName(sqlQuery,inputParamName))
                continue;

            boolean isDynamicField = false;
            for (String varCondition : varConditionList) {

                for(String varNm: varChildListMap.get(varCondition)) {
                    if(varNm.equals(inputParamName)) {
                        isDynamicField = true;
                        break;
                    }
                }
            }

            if(!isDynamicField) {
                boolean add = paramKeyNameSet.contains(inputParamName);
                System.err.println(add);
                if(!add) {
                    p.add("    query.setParameter(\"" + inputParamName + "\" , " + getParamInfo(inputTyprVarName,inputParamName) + ");");
                    paramKeyNameSet.add(inputParamName);
                }

            }
        }


        if(!hasCustomDefinedParam && inputTyprVarName!=null) {
            for(String fieldNm : namedParamList) {
                boolean isDynamicField = false;
                for (String varCondition : varConditionList) {

                    for(String varNm: varChildListMap.get(varCondition)) {
                        if(varNm.equals(fieldNm)) {
                            isDynamicField = true;
                            break;
                        }
                    }

                }
                if(!isDynamicField) {
                    if(!paramKeyNameSet.contains(fieldNm)) {
                        p.add("    query.setParameter(\"" + fieldNm + "\" , " + getParamInfo(inputTyprVarName,fieldNm) + ");");
                        paramKeyNameSet.add(fieldNm);
                    }

                }
            }
        }

        if(isPageable) {
            //p.add("    query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());");
            //p.add("    query.setMaxResults(pageable.getPageSize());");
            p.add("");
            p.add("    Query countQuery = entityManager.createNativeQuery(\"select count(*) from ( \"+sb+\" ) as X\");");

            for (String varCondition : varConditionList) {
                p.add("    if(" + varCondition + ") {");

                for(String varNm: varChildListMap.get(varCondition)) {

                    p.add("      countQuery.setParameter(\"" + varNm + "\" , " + getParamInfo(inputTyprVarName,varNm) + ");");
                }
                p.add("    }");

            }

            for(Field f: method.getParams().getItemList()) {
                String inputParamName = f.getIdentity().getName();

                boolean isDynamicField = false;
                for (String varCondition : varConditionList) {
                    for(String varNm: varChildListMap.get(varCondition)) {
                        if(varNm.equals(inputParamName)) {
                            isDynamicField = true;
                            break;
                        }
                    }

                }
                if (!isDynamicField) {
                    p.add("    countQuery.setParameter(\"" + inputParamName + "\" , " + inputParamName + ");");
                }
            }

            p.add("    long totalRows = ((Number) countQuery.getSingleResult()).longValue();");

            if(isSingleResult) {
                p.add("    return query.getSingleResult();");
            }
            else if (fieldNames != null) {
                p.add("    ModelTransList<%s> list = ", outputClassNm);
                p.add("       new ModelTransList<%s>(query.getResultList(), %s.class, fieldNames);", outputClassNm, outputClassNm);
                p.add("    return new PageImpl<%s>(list, pageable, totalRows);",outputClassNm);
            }else{
                p.add("    return new PageImpl<%s>(query.getResultList(), pageable, totalRows);",outputClassNm);
            }


        }
        else
        {
            if (isSingleResult) {
                p.add("    Object result = query.getSingleResult();");

                if(typeInfo.getBaseFieldType() == BaseFieldType.STRING) {
                    p.add("    return result != null ? result.toString() : null;");
                }
                else if(typeInfo.getBaseFieldType() == BaseFieldType.LONG) {
                    p.add("    return result != null ? ((Number) result).longValue() : null;");
                }
                else if(typeInfo.getBaseFieldType() == BaseFieldType.DOUBLE) {
                    p.add("    return result != null ? ((Number) result).doubleValue() : null;");
                }
                else if(typeInfo.getBaseFieldType() == BaseFieldType.INTEGER) {
                    p.add("    return result != null ? ((Number) result).intValue() : null;");
                }
                else if(typeInfo.getBaseFieldType() == BaseFieldType.FLOAT) {
                    p.add("    return result != null ? ((Number) result).floatValue() : null;");
                }
                else
                    p.add("    return result");
            }
            else if (fieldNames != null) {
                p.add("    return new ModelTransList<%s>(query.getResultList(), %s.class, fieldNames);", outputClassNm, outputClassNm);
            } else {
                p.add("    return query.getResultList();");
            }
        }



        p.add("}");
        p.add("");

        if(ConsoleLog.hasStoredLogkey("getMappingFieldNames")) {

            ConsoleLog.printWarn("");
            ConsoleLog.printWarn("Need to check [Query method] "+method.getName());
            ConsoleLog.printStoredWarnLog("getMappingFieldNames","  ");
        }
    }

    public static String extractContentBetweenBrackets(String input) {
        Pattern pattern = Pattern.compile("<([^>]+)>");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }


    private void makeTransformMethod(Method method, CodeTemplate.Paragraph methodP, String repoName) {

        if(repoName==null)  {
            repoName = "test";
        }


        SqlQueryInfo queryInfo = method.getQueryInfo();
        DataTransfer outputDTO = queryInfo.getDataTransfer();

        String params = getParametersForMethod(method.getParams(), true);

        String outputClassNm = outputDTO.getIdentity().getName();
        String methodName = method.getName();

        methodP.add("public List<%s> %s(%s) {", outputClassNm, methodName, params);

        String fieldNames = getMappingFieldNames(outputDTO ,queryInfo.getSelectColumnCount());
        methodP.add("    String[] fieldNames = {%s};", fieldNames);

        params = getParametersForMethod(method.getParams(), false);
        methodP.add("    List<Object[]> result = %s.%s(%s);", repoName, methodName, params);
        methodP.add("    return new ModelTransList<%s>(result, %s.class, fieldNames);", outputClassNm, outputClassNm);
        methodP.add("}");
        methodP.add("");
    }


    private String getMappingFieldNames(DataTransfer dto, int maxCount) {
        if(maxCount<200) {
            return getMappingFieldNames(dto.getItems(), maxCount);
        }

        ListMap<String,Field> allFieldListMap = dto.getAllFieldListMap();

        List<Field> list = allFieldListMap.getList();

        int count=0;
        StringBuilder sb = new StringBuilder();
        for(Field f :list) {
            if(!f.getTypeInfo().isBaseType())
                continue;

            if (sb.length() > 0)
                sb.append(" ,");
            sb.append("\"").append(f.getName()).append("\"");
            count++;
            if(count >= maxCount)
                break;
        }

        return sb.toString();
    }

    private String getMappingFieldNames(ModelComponentItems<Field> params, int maxCount) {
        StringBuilder sb = new StringBuilder();
        int count=0;
        if (params != null) {
            while (params.hasNext()) {
                Field f = params.next();
                if (sb.length() > 0)
                    sb.append(" ,");
                sb.append("\"").append(f.getName()).append("\"");
                count++;
                if(count >= maxCount)
                    break;
            }
        }
        return sb.toString();
    }


    private String getParametersForMethod(Items<Field> params, boolean hasType) {
        StringBuilder sb = new StringBuilder();
        if (params != null) {
            for (Field f : params.getItemList()) {
                if (sb.length() > 0)
                    sb.append(" ,");
                // @Param("username")'
                if (hasType) {
                    sb.append(f.getTypeInfo().getDefaultTypeName()).append(" ");
                }
                sb.append(f.getName());
            }
        }

        return sb.toString();
    }

}
