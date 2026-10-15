package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.ConstantParam;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.core.Items;
import io.elasticore.base.model.entity.BaseFieldType;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.entity.TypeInfo;
import io.elasticore.base.model.port.Port;
import io.elasticore.base.model.port.PortAnnotation;
import io.elasticore.base.model.repo.Method;
import io.elasticore.base.util.CodeTemplate;
import io.elasticore.base.util.ConsoleLog;
import io.elasticore.base.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PortSrcPublisher extends SrcFilePublisher {

    private CodeTemplate baseCodeTmpl;

    private CodePublisher publisher;

    private String packageName;

    private String entityPackageName;

    private String dtoPackageName;

    private String enumPackageName;

    protected ECoreModel eCoreModel;

    private boolean isEnable = false;


    public PortSrcPublisher(CodePublisher publisher) {
        super(publisher);

        this.publisher = publisher;

        this.eCoreModel = this.publisher.getECoreModelContext().getDomain().getModel();

        String templatePath = eCoreModel.getConfig("template.port");
        if (templatePath == null)
            templatePath = "elasticore-template/port.tmpl";
        else
            templatePath = "resource://" + templatePath;

        this.baseCodeTmpl = CodeTemplate.newInstance(templatePath);

        ECoreModel model = publisher.getECoreModelContext().getDomain().getModel();
        this.packageName = model.getNamespace(ConstantParam.KEYNAME_PORT);

        if (eCoreModel.getEntityModels().getItems().size() > 0)
            this.entityPackageName = model.getNamespace(ConstantParam.KEYNAME_ENTITY);

        if (eCoreModel.getDataTransferModels().getItems().size() > 0)
            this.dtoPackageName = model.getNamespace(ConstantParam.KEYNAME_DTO);

        if (eCoreModel.getEnumModels().getItems().size() > 0)
            this.enumPackageName = model.getNamespace(ConstantParam.KEYNAME_ENUMERATION);

    }

    public void publish(ModelDomain domain) {
        ModelComponentItems<Port> portServices = this.eCoreModel.getPortModels().getItems();
        while (portServices.hasNext()) {
            Port port = portServices.next();
            publish(domain, port);
        }
    }

    private boolean processHttpMeta(String id, Port portService, CodeTemplate.Paragraph p) {
        MetaInfo metaInfo = portService.getMetaInfo();
        String url = metaInfo.getMetaAnnotationValue(PortAnnotation.META_URL);

        if (url == null) {
            return false;
        }

        // Build the @ExternalService annotation dynamically. String values are passed
        // as format args (not embedded in the format literal) so that '%' characters
        // inside a URL never break String.format inside Paragraph.add().
        StringBuilder fmt = new StringBuilder("@ExternalService(protocol=\"http\", id=%s ,url=%s");
        List<Object> args = new ArrayList<>();
        args.add(StringUtils.quoteString(id));
        args.add(StringUtils.quoteString(url));

        String authKey = metaInfo.getMetaAnnotationValue(PortAnnotation.META_AUTHORIZATION);
        if (authKey != null && !authKey.isEmpty()) {
            fmt.append(", authKey=%s");
            args.add(StringUtils.quoteString(authKey));
        }

        // Optional per-port timeout defaults declared in DSL via @timeout(connect=.., read=.., write=.., response=..)
        appendTimeoutParam(fmt, args, metaInfo, "connectTimeout", "timeout.connect");
        appendTimeoutParam(fmt, args, metaInfo, "readTimeout", "timeout.read");
        appendTimeoutParam(fmt, args, metaInfo, "writeTimeout", "timeout.write");
        appendTimeoutParam(fmt, args, metaInfo, "responseTimeout", "timeout.response");

        fmt.append(")");
        p.add(fmt.toString(), args.toArray());
        return true;
    }

    /**
     * Appends a single timeout attribute to the @ExternalService annotation when the
     * matching @timeout sub-value is present and is a valid non-negative integer (ms).
     *
     * @param fmt           the format buffer being assembled
     * @param args          the format argument list being assembled
     * @param metaInfo      the port meta info to read the @timeout value from
     * @param attrName      the @ExternalService attribute name to emit (e.g. "connectTimeout")
     * @param annotationKey the @timeout sub-key to read (e.g. "timeout.connect")
     */
    private void appendTimeoutParam(StringBuilder fmt, List<Object> args, MetaInfo metaInfo,
                                    String attrName, String annotationKey) {
        String val = metaInfo.getMetaAnnotationValue(annotationKey);
        if (val == null) {
            return;
        }
        val = val.trim();
        if (val.isEmpty()) {
            return;
        }
        try {
            int ms = Integer.parseInt(val);
            if (ms < 0) {
                return;
            }
            // Integer rendered via %s to avoid any locale-specific %d grouping.
            fmt.append(", ").append(attrName).append("=%s");
            args.add(ms);
        } catch (NumberFormatException e) {
            ConsoleLog.printWarn("[WARN] Invalid @timeout value for " + attrName + " (must be integer ms): " + val);
        }
    }

    private boolean processDbmsMeta(String id, Port portService, CodeTemplate.Paragraph p) {
        MetaInfo meta = portService.getMetaInfo();
        String datasource = meta.getMetaAnnotationValue("datasource");
        String sqlSource = meta.getMetaAnnotationValue("sqlsource");
        if(datasource!=null) {
            p.add("@DbmsService(id=%s ,datasource=%s ,sqlSource=%s)"
                    ,StringUtils.quoteString(id)
                    ,StringUtils.quoteString(datasource), StringUtils.quoteString(sqlSource));
        }
        else {
            p.add("@DbmsService(id=%s ,sqlSource=%s)"
                    ,StringUtils.quoteString(id),StringUtils.quoteString(sqlSource));
        }
        return true;
    }


    private CodeTemplate.Paragraph getSvcAnnotation(Port portService) {
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

        String type = portService.getMeta().getMetaAnnotationValue("type");
        if(type!=null) {
            type = type.toLowerCase();
            String id = portService.getIdentity().getDomainId() +"."+ portService.getIdentity().getName();

            if("http".equals(type)) {
                processHttpMeta(id, portService, p);
             }
            else if("dbms".equals(type)) {
                processDbmsMeta(id, portService, p);

            }
        }

        return p;
    }

    public void publish(ModelDomain domain, Port portService) {

        if (this.packageName == null) return;


        String type = portService.getMeta().getMetaAnnotationValue("type");
        if(ConstantParam.KEYNAME_PROTO.equalsIgnoreCase(type))
            return;

        CodeTemplate.Parameters params = CodeTemplate.newParameters();

        CodeTemplate.Paragraph classAnnotationList = getSvcAnnotation(portService);

        String classNm = portService.getIdentity().getName();

        String desc = portService.getMeta().getLabel();

        params
                .set("packageName", packageName)
                .set("j2eePkgName", getPersistentPackageName(domain))
                .set("entityPackageName", entityPackageName)
                .set("dtoPackageName", dtoPackageName)
                .set("enumPackageName", enumPackageName)
                .set("classAnnotationList", classAnnotationList)
                .set("description", desc)
                .set("className", classNm);


        CodeTemplate.Paragraph fieldP = CodeTemplate.newParagraph();
        CodeTemplate.Paragraph methodP = CodeTemplate.newParagraph();
        loadRepositoryInfo(portService, fieldP, methodP);

        if (fieldP.size() == 0 && methodP.size() == 0)
            return;

        params.set("repositoryList", fieldP);
        params.set("methodList", methodP);

        String code = baseCodeTmpl.toString(params);
        String qualifiedClassName = packageName + "." + classNm;

        this.writeSrcCode(this.publisher, null, qualifiedClassName, code);
    }

    protected String geParamInfos(Items<Field> fieldItems) {
        if(fieldItems==null) return "";
        StringBuilder sb = new StringBuilder();
        for (Field f : fieldItems.getItemList()) {
            if (sb.length() > 0)
                sb.append(", ");

            // Check for @contentType annotation and output @MultipartContentType
            String contentType = f.getAnnotationValue("contenttype", "contentType");
            if (contentType != null && !contentType.isEmpty()) {
                sb.append("@MultipartContentType(")
                  .append(StringUtils.quoteString(contentType))
                  .append(") ");
            }

            sb.append(getParamTypeName(f.getTypeInfo()))
                    .append(" ")
                    .append(f.getName());
        }

        return sb.toString();

    }

    /**
     * Resolves the Java type name for a port method parameter.
     * DSL list types (e.g. list&lt;SomeDTO&gt;) are normalized to the Java
     * List form (e.g. List&lt;SomeDTO&gt;), with base item types converted
     * to their wrapper class names (e.g. list&lt;long&gt; to List&lt;Long&gt;).
     *
     * @param typeInfo the parameter type information
     * @return the Java type name to render in the generated source
     */
    protected String getParamTypeName(TypeInfo typeInfo) {
        if (typeInfo.isList()) {
            String itemType = typeInfo.getTypeParameterName();
            if (itemType == null || itemType.isEmpty()) {
                return "List<?>";
            }
            BaseFieldType itemBaseType = BaseFieldType.findByName(itemType);
            if (itemBaseType != null && itemBaseType != BaseFieldType.UNKNOWN && itemBaseType != BaseFieldType.LIST) {
                itemType = itemBaseType.getWrapperClassName();
            }
            return "List<" + itemType + ">";
        }
        return typeInfo.getDefaultTypeName();
    }


    private boolean processHttpMapping(Port portService,Method method, CodeTemplate.Paragraph methodP) {
        MetaInfo metaInfo = method.getMetaInfo();
        if(metaInfo==null)
            return false;

        String qParamNames = "\"\"";
        Items<Field> methodParams = method.getParams();
        if (methodParams != null) {
            List<Field> itemList = methodParams.getItemList();
            StringBuilder sb = new StringBuilder();
            for(Field f : itemList) {
                if(sb.length()>0) sb.append(",");
                sb.append(f.getName());
            }
            qParamNames = StringUtils.quoteString(sb.toString());
        }



        String url = null;
        String httpMethod = null;
        String contentType = null;

        Annotation metaAnnotation = metaInfo.getMetaAnnotation(PortAnnotation.META_HTTP_ENDPOINT);

        if (metaAnnotation != null) {
            Properties properties = null;
            try {
                properties = metaAnnotation.getProperties();
                url = (String) properties.get("url");
                httpMethod = properties.get("method").toString().toUpperCase();
                contentType = (String) properties.get("contentType");
            } catch (RuntimeException re) {
            }
        }
        if(contentType==null) {
            contentType = metaInfo.getMetaAnnotationValue(PortAnnotation.META_CONTENT_TYPE);
        }

        String chkHttpMethod = null;
        if(url==null) {
            url = metaInfo.getMetaAnnotationValue(PortAnnotation.META_GET_MAPPING);
            chkHttpMethod = "GET";
        }
        if(url==null) {
            url = metaInfo.getMetaAnnotationValue(PortAnnotation.META_POST_MAPPING);
            chkHttpMethod = "POST";
        }
        if(url==null) {
            url = metaInfo.getMetaAnnotationValue(PortAnnotation.META_PUT_MAPPING);
            chkHttpMethod = "PUT";
        }
        if(url==null) {
            url = metaInfo.getMetaAnnotationValue(PortAnnotation.META_DELETE_MAPPING);
            chkHttpMethod = "DELETE";
        }
        if(url==null)
            return false;

        if(chkHttpMethod!=null && httpMethod==null) {
            httpMethod = chkHttpMethod;
        }

        if(contentType==null)
            contentType = "application/json";

        String qUrl = StringUtils.quoteString(url);
        String qHttpMethod = StringUtils.quoteString(httpMethod);
        String qContentType = StringUtils.quoteString(contentType);

        methodP.add("@HttpEndpoint(url=%s, method=%s, contentType=%s, paramNames=%s)", qUrl, qHttpMethod, qContentType, qParamNames);
        return true;
    }

    private void loadRepositoryInfo(Port portService, CodeTemplate.Paragraph fieldP, CodeTemplate.Paragraph methodP) {
        //private final ContactInfoRepository contactInfo;
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
        ModelComponentItems<Method> methods = portService.getItems();

        ECoreModel model = publisher.getECoreModelContext().getDomain().getModel();

        while (methods.hasNext()) {
            Method method = methods.next();

            String returnType = method.getReturnType();
            Items<Field> fieldItems = method.getParams();

            String paramInfos = geParamInfos(fieldItems);

            String methodName = method.getIdentity().getName();

            MetaInfo metaInfo = method.getMetaInfo();

            String desc = method.getDescription();
            if(!StringUtils.hasValue(desc)) {
                desc = methodName;
            }
            methodP.add("/*");
            methodP.add(StringUtils.addPrefixToEachLine(desc, " * "));
            methodP.add("*/");
            processHttpMapping(portService, method ,methodP);

            methodP.add("%s %s(%s);", returnType, methodName, paramInfos);
            methodP.add("");
        }
    }


}
