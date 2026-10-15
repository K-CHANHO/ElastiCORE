package io.elasticore.base.model.pub.dsl;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.*;
import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.core.ListMap;
import io.elasticore.base.model.dto.DataTransfer;
import io.elasticore.base.model.dto.DataTransferAnnotation;
import io.elasticore.base.model.dto.DataTransferModels;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.model.entity.EntityAnnotation;
import io.elasticore.base.model.entity.EntityModels;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.pub.jpa.SrcFilePublisher;
import io.elasticore.base.util.CodeStringBuilder;
import io.elasticore.base.util.CodeTemplate;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Handles the publishing of source files for entities.
 * This class is responsible for generating Java source code
 * based on entity definitions and their annotations.
 */
public class DslcodeFilePublisher extends SrcFilePublisher {

    // DTO template used when env.yml sets no template.dto
    static final String DEFAULT_DTO_TEMPLATE = "elasticore-template/dsl/dto.tmpl";

    private CodeTemplate javaClassTmpl;

    private CodePublisher publisher;

    private String packageName;

    private String enumPackageName;

    // the generation mode from env.yml (config.mode)
    private String publishMode;

    private CodeTemplate.Paragraph paragraphForEntity;

    private ECoreModel eCoreModel;


    /**
     * Initializes a new instance of the EntitySrcFilePublisher class.
     * Sets up the template path and packages based on the provided publisher.
     *
     * @param publisher The JPACodePublisher instance used for publishing.
     */
    public DslcodeFilePublisher(CodePublisher publisher) {
        super(publisher);

        this.publisher = publisher;
        this.eCoreModel = this.publisher.getECoreModelContext().getDomain().getModel();

        this.publishMode = this.eCoreModel.getConfig("mode");

        String templatePath = this.publisher.getECoreModelContext().getDomain().getModel().getConfig("template.dto");
        if (templatePath == null)
            templatePath = DEFAULT_DTO_TEMPLATE;
        else
            templatePath = "resource://" + templatePath;

        this.javaClassTmpl = CodeTemplate.newInstance(templatePath);

        ECoreModel model = publisher.getECoreModelContext().getDomain().getModel();
        processEntityModel(model);

    }

    protected void processEntityModel(ECoreModel model) {

        EntityModels entityModels = model.getEntityModels();
        ModelComponentItems<Entity> items = entityModels.getItems();

        CodeStringBuilder cb = new CodeStringBuilder("","",2);
        cb.line("entity:").block("");


        while (items.hasNext()) {

            Entity entity = items.next();
            cb.line("%s:",entityModels.getIdentity().getName()).block("");
            cb.line("meta: entity %s","");
            cb.line("fields:").block("");

            ModelComponentItems<Field> fieldItem = entity.getItems();
            while (fieldItem.hasNext()) {
                Field f = fieldItem.next();

                String label = f.getAnnotationValue("label");
                if(label!=null)
                    label = " -- "+label.trim();

                cb.line("%s: %s %s",f.getName(),f.getTypeInfo().getInitTypeInfo(),label);

            }

            cb.end("");
            cb.end("");



        }

        System.out.println(cb.toString());
    }

    /**
     * Retrieves the extension information for an entity.
     * This method checks if the entity has an "extend" meta-annotation and
     * returns the extension clause (e.g., "extends BaseClass") for the entity.
     * If the "extend" meta-annotation is not present, an empty string is returned.
     *
     * @param entity The entity for which to retrieve the extension information.
     * @return A string containing the extension clause, if applicable; otherwise, an empty string.
     */
    private String getExtendInfo(MetaInfoModel entity) {


        Annotation annotation = entity.getMetaInfo().getMetaAnnotation("extend");
        if (annotation != null)
            return "extends " + annotation.getValue();

        return "";
    }


    private String getAbstractInfo(MetaInfoModel entity) {
        if (entity.getMetaInfo().hasMetaAnnotation("abstract")) {
            return "abstract";
        }
        return "";
    }

    public void publish(ModelDomain domain) {
        DataTransferModels dataTransferModels = this.eCoreModel.getDataTransferModels();
        ModelComponentItems<DataTransfer> dtoItems = dataTransferModels.getItems();
        for (int i = 0; i < dtoItems.size(); i++) {
            DataTransfer dto = dtoItems.get(i);
            publish(domain, dto, REQUEST);
            publish(domain, dto, RESPONSE);
        }
    }

    private final static int REQUEST = 1;

    private final static int RESPONSE = 2;


    /**
     * Generates and publishes Java source code for the specified dto.
     * This method orchestrates the creation of dto classes based on
     * dto definition and annotations.
     *
     * @param domain The model domain to which the dto belongs.
     * @param dto    The dto for which source code is to be published.
     */
    public void publish(ModelDomain domain, DataTransfer dto, int mode) {
        MetaInfo metaInfo = dto.getMetaInfo();
        Annotation typeAnnotation = metaInfo.getMetaAnnotation("type");
        if (typeAnnotation != null) {
            if ("template".equals(typeAnnotation.getValue()))
                return;
        }

        if (metaInfo.hasMetaAnnotation(DataTransferAnnotation.META_SEARCHABLE)) return;


        CodeTemplate.Parameters p = CodeTemplate.newParameters();
        String classNm = dto.getIdentity().getName();


        this.paragraphForEntity = CodeTemplate.newParagraph();

        setNativeAnnotation(dto, this.paragraphForEntity);

        // must call 'getFieldInfo' first
        CodeTemplate.Paragraph pr = getFieldInfo(dto, mode);

        String classPkgName = packageName;
        if (mode == REQUEST)
            classPkgName = classPkgName + ".request";
        else
            classPkgName = classPkgName + ".response";

        String desc = metaInfo.getInfoAnnotationValue("description");
        if (desc == null)
            desc = "";

        p
                .set("packageName", classPkgName)
                .set("j2eePkgName", getPersistentPackageName(domain))
                .set("enumPackageName", enumPackageName)
                .set("abstract", getAbstractInfo(dto))
                .set("classAnnotationList", this.paragraphForEntity)
                .set("extendInfo", getExtendInfo(dto))
                .set("implementInfo", "implements java.io.Serializable")
                .set("description", desc)
                .set("className", classNm);


        p.set("fieldList", pr);

        String qualifiedClassName = classPkgName + "." + classNm;
        String code = javaClassTmpl.toString(p);

        this.writeSrcCode(this.publisher, dto, qualifiedClassName, code);
    }


    private CodeTemplate.Paragraph getMultiplePkField(Entity entity) {
        ModelComponentItems<Field> fields = entity.getItems();
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
        while (fields.hasNext()) {
            Field f = fields.next();

            if (!f.isPrimaryKey())
                continue;
            ;

            setFieldDesc(f, p);
            //setFieldPkInfo(f, p);


            /*
            BaseFieldType ft = f.getTypeInfo().getBaseFieldType();
            if (ft == BaseFieldType.DATETIME)
                p.add("@Temporal(TemporalType.TIMESTAMP)");
            else if (ft == BaseFieldType.DATE)
                p.add("@Temporal(TemporalType.DATE)");
            else if (ft == BaseFieldType.TIME)
                p.add("@Temporal(TemporalType.TIME)");

             */

            String code = String.format("%s %s %s;", "private", f.getTypeInfo().getDefaultTypeName(), f.getName());
            p.add(code);
            p.add("");
        }
        return p;
    }

    private CodeTemplate.Paragraph getFieldInfo(DataModelComponent dto, int mode) {
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

        loadFieldInfo(dto, p, mode);
        return p;
    }


    private void loadFieldInfo(DataModelComponent dto, CodeTemplate.Paragraph p, int mode) {

        ListMap<String, Field> fields = dto.getAllFieldListMap();

        List<Field> fieldList = fields.getList();

        for (Field f : fieldList) {

            if (f.hasAnnotation(EntityAnnotation.META_DISABLE))
                continue;


            String val = f.getAnnotationValue(EntityAnnotation.KIND);

            if (mode == REQUEST && "calculated".equals(val))
                continue;


            if (isEntityReturnType(f, eCoreModel)) continue;

            setFieldDesc(f, p);
            //setFieldDocumentation(f,p);
            setFieldValidation(f, p);
            setJsonInfo(f, p);


            setNativeAnnotation(f, p);

            String defaultValDefined = getDefaultValueSetup(f);

            /*
            BaseFieldType ft = f.getTypeInfo().getBaseFieldType();
            if (ft == BaseFieldType.DATETIME)
                p.add("@Temporal(TemporalType.TIMESTAMP)");
            else if (ft == BaseFieldType.DATE)
                p.add("@Temporal(TemporalType.DATE)");
            else if (ft == BaseFieldType.TIME)
                p.add("@Temporal(TemporalType.TIME)");

             */

            String type = f.getTypeInfo().getDefaultTypeName();

            if (mode == RESPONSE && f.getTypeInfo().isList()) {
                String coreType = f.getTypeInfo().getCoreItemType();
                p.add("%s %s %s = new ArrayList<%s>(Arrays.asList(new %s()));", "private", type, f.getName(), coreType, coreType);
            } else {
                p.add("%s %s %s%s;", "private", type, f.getName(), defaultValDefined);
            }


            p.add("");

            setFunctionInfo(f, p);

        }
    }


    private String getDefaultValueSetup(Field f) {
        if (!f.hasAnnotation("default")) return "";
        String val = f.getAnnotation("default").getValue();
        if (f.getTypeInfo().isStringType()) {
            // Smart quote handling to prevent double quotes
            if (val.startsWith("\"") && val.endsWith("\"")) {
                // Already properly quoted: @default("COMPLETED") -> "COMPLETED"
                return " = " + val;
            }
            if (val.startsWith("'") && val.endsWith("'")) {
                // Convert single quotes to double quotes: @default('COMPLETED') -> "COMPLETED"
                return " = \"" + val.substring(1, val.length()-1) + "\"";
            }
            // Add quotes for unquoted values: @default(COMPLETED) -> "COMPLETED"
            return " = \"" + val + "\"";
        } else {
            return " = " + val;
        }
    }


    private void setFieldPkInfo(Field field, CodeTemplate.Paragraph paragraph) {

        if (field.hasAnnotation("id") || field.hasAnnotation("pk") || field.isPrimaryKey()) {
            paragraph.add("@Id");

            if (field.hasAnnotation("sequence")) {
                paragraph.add("@GeneratedValue(strategy = GenerationType.IDENTITY)");
            }
        }


        if (field.getGenerationType() != null) {
            paragraph.add("@GeneratedValue(strategy = GenerationType." + field.getGenerationType());
        }
    }


    private void setNativeAnnotation(MetaInfoModel entity, CodeTemplate.Paragraph paragraph) {
        setNativeAnnotation(entity.getMetaInfo().getMetaAnnotationMap(), paragraph);
    }

    private void setNativeAnnotation(Field field, CodeTemplate.Paragraph paragraph) {
        setNativeAnnotation(field.getAnnotationMap(), paragraph);
    }

    private void setNativeAnnotation(Map<String, Annotation> annotationMap, CodeTemplate.Paragraph paragraph) {

        if (annotationMap == null)
            return;

        if (this.publishMode == null) return;

        String prefix = "dto:";
        annotationMap.entrySet().stream()
                .filter(entry -> entry.getKey().contains(prefix))
                .map(Map.Entry::getValue)
                .forEach(paragraph::add);


    }


    private void setSqlDeleteAnnotation(Field field, CodeTemplate.Paragraph paragraph) {
        if (field.hasAnnotation("chkdelete")) {

        }
    }
}
