package io.elasticore.base.model.pub.mongodb;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.*;
import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.core.BaseModelDomain;
import io.elasticore.base.model.core.RelationshipManager;
import io.elasticore.base.model.entity.*;
import io.elasticore.base.model.enums.EnumModel;
import io.elasticore.base.model.pub.jpa.SrcFilePublisher;
import io.elasticore.base.model.relation.ModelRelationship;
import io.elasticore.base.model.relation.RelationType;
import io.elasticore.base.util.CodeTemplate;
import io.elasticore.base.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Handles the publishing of source files for entities.
 * This class is responsible for generating Java source code
 * based on entity definitions and their annotations.
 */
public class DocEntitySrcFilePublisher extends SrcFilePublisher {

    private CodeTemplate javaClassTmpl;

    private CodePublisher publisher;

    private ECoreModel model;

    private String packageName;

    private String enumPackageName;

    // the generation mode from env.yml (config.mode)
    private String publishMode;

    private CodeTemplate.Paragraph paragraphForEntity;


    /**
     * Initializes a new instance of the EntitySrcFilePublisher class.
     * Sets up the template path and packages based on the provided publisher.
     *
     * @param publisher The JPACodePublisher instance used for publishing.
     */
    public DocEntitySrcFilePublisher(CodePublisher publisher) {
        super(publisher);


        this.publisher = publisher;
        this.model =  this.publisher.getECoreModelContext().getDomain().getModel();

        this.publishMode = model.getConfig("mode");

        String templatePath = model.getConfig("template.entity");
        if(templatePath==null)
            templatePath = "elasticore-template/entity_mongo.tmpl";
        else
            templatePath = "resource://"+templatePath;

        this.javaClassTmpl = CodeTemplate.newInstance(templatePath);

        ECoreModel model = publisher.getECoreModelContext().getDomain().getModel();
        this.packageName = model.getNamespace(ConstantParam.KEYNAME_ENTITY);

        if(model.getEnumModels().getItems().size()>0)
            this.enumPackageName = model.getNamespace(ConstantParam.KEYNAME_ENUMERATION);
        else
            this.enumPackageName = null;

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
    private String getExtendInfo(Entity entity) {

        Annotation annotation = entity.getMetaInfo().getMetaAnnotation(EntityAnnotation.META_EXTEND);
        if (annotation != null)
            return "extends " + annotation.getValue();

        return "";
    }


    private String getAbstractInfo(Entity entity) {
        if (entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_ABSTRACT)) {
            return "abstract";
        }
        return "";
    }

    private CodeTemplate.Paragraph getEntityAnnotation(Entity entity, String j2eePkgNm) {
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();


        MetaInfo metaInfo = entity.getMetaInfo();
        String mappingNm = metaInfo.getMetaAnnotationValue(EntityAnnotation.META_MAPPING);

        if (mappingNm == null) {
            mappingNm = entity.getIdentity().getName();
        }

        p.add("@org.springframework.data.mongodb.core.mapping.Document(collection =\"" + mappingNm + "\")");


        /*
        if(metaInfo.hasMetaAnnotation("DynamicUpdate"))
            p.add("@org.hibernate.annotations.DynamicUpdate");

         */



        //Entity = BaseModelDomain.getModelDomain(this.getIdentity().getDomainId()).getModel().getEntityModels().findByName(toName);
        if (metaInfo.hasMetaAnnotation(EntityAnnotation.META_EMBEDDABLE)) {
            p.add("@Embeddable");
        }else {
            ComponentIdentity identity = entity.getIdentity();

            List<ModelRelationship> relationshipList = RelationshipManager.getInstance(identity.getDomainId()).findByToName(identity.getName());
            for(ModelRelationship r : relationshipList){
                if(r.getRelationType() == RelationType.EMBEDDED) {
                    p.add("@Embeddable");
                    break;
                }
            }
        }

        entity.getItems().forEachRemaining(field -> {
            if (field.hasAnnotation(EntityAnnotation.DISCRIMINATOR)) {
                p.add("@Inheritance(strategy = InheritanceType.SINGLE_TABLE)");
                String dbColumnNm = field.getDbColumnName();
                String type = field.getTypeInfo().getDefaultTypeName().toUpperCase(Locale.ROOT);
                p.add("@DiscriminatorColumn(name = \"" + dbColumnNm + "\", discriminatorType = DiscriminatorType." + type + ")");

                String defaultDesciminatorValue = field.getAnnotationValue(EntityAnnotation.DISCRIMINATOR);
                if(defaultDesciminatorValue!=null) {
                    p.add("@DiscriminatorValue(\"" + defaultDesciminatorValue + "\")");
                }
            }
        });

        if (metaInfo.hasMetaAnnotation(EntityAnnotation.META_ROLL_UP)) {
            String disVal = metaInfo.getMetaAnnotation(EntityAnnotation.META_ROLL_UP).getValue();
            p.add("@DiscriminatorValue(\"" + disVal + "\")");
        }


        return p;
    }


    /**
     * Generates and publishes Java source code for the specified entity.
     * This method orchestrates the creation of entity classes based on
     * entity definition and annotations.
     *
     * @param domain The model domain to which the entity belongs.
     * @param entity The entity for which source code is to be published.
     */
    public void publish(ModelDomain domain, Entity entity) {

        Annotation typeAnnotation = entity.getMetaInfo().getMetaAnnotation(EntityAnnotation.META_TYPE);
        if(typeAnnotation!=null) {
            if("template".equals(typeAnnotation.getValue()))
                return;
        }
        String j2eePkgNm = getPersistentPackageName(domain);
        publishEntityIdentityClass(domain, entity);

        CodeTemplate.Parameters p = CodeTemplate.newParameters();
        String classNm = entity.getIdentity().getName();

        this.paragraphForEntity = getEntityAnnotation(entity , j2eePkgNm);

        setNativeAnnotation(entity, this.paragraphForEntity);

        // must call 'getFieldInfo' first
        CodeTemplate.Paragraph pr = getFieldInfo(entity);

        CodeTemplate.Paragraph importList = CodeTemplate.newParagraph();
        if(pr.contains("@Convert(")) {
             // TODO
        }

        p
                .set("packageName", packageName)
                .set("enumPackageName", enumPackageName)
                .set("abstract", getAbstractInfo(entity))
                .set("classAnnotationList", this.paragraphForEntity)
                .set("extendInfo", getExtendInfo(entity))
                .set("j2eePkgName",j2eePkgNm)
                .set("importList", importList)
                .set("implementInfo", "implements java.io.Serializable")
                .set("className", classNm);


        p.set("fieldList", pr);

        String qualifiedClassName = packageName + "." + classNm;
        String code = javaClassTmpl.toString(p);

        this.writeSrcCode(this.publisher, entity, qualifiedClassName, code);
    }


    /**
     * @param domain
     * @param entity
     */
    private void publishEntityIdentityClass(ModelDomain domain, Entity entity) {
        if (entity.getPkField() != null && entity.getPkField().isMultiple()) {

            String classNm = entity.getPkField().getType();

            String j2eePkgNm = getPersistentPackageName(domain);

            CodeTemplate.Parameters param = CodeTemplate.newParameters();
            param
                    .set("packageName", packageName)
                    .set("abstract", "")
                    .set("extendInfo", "")
                    .set("j2eePkgName",j2eePkgNm)
                    .set("implementInfo", "implements java.io.Serializable")
                    .set("classAnnotationList", "@Embeddable")
                    .set("className", classNm);

            CodeTemplate.Paragraph p = getMultiplePkField(entity);
            param.set("fieldList", p);

            String qualifiedClassName = packageName + "." + classNm;
            String code = javaClassTmpl.toString(param);

            this.writeSrcCode(this.publisher, entity, qualifiedClassName, code);
        }
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
            //setFieldColumnAnnotation( entity, f, p);

            /*
            BaseFieldType ft =f.getTypeInfo().getBaseFieldType();
            if(ft == BaseFieldType.DATETIME)
                p.add("@Temporal(TemporalType.TIMESTAMP)");
            else if(ft == BaseFieldType.DATE)
                p.add("@Temporal(TemporalType.DATE)");
            else if(ft == BaseFieldType.TIME)
                p.add("@Temporal(TemporalType.TIME)");

             */

            p.add("%s %s %s;", "private", f.getTypeInfo().getDefaultTypeName(), f.getName());
            p.add("");

        }
        return p;
    }

    private CodeTemplate.Paragraph getFieldInfo(Entity entity) {
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

        loadFieldInfo(entity, p);
        return p;
    }


    private void loadFieldInfo(Entity entity, CodeTemplate.Paragraph p) {
        ModelComponentItems<Field> fields = entity.getItems();

        boolean isExtendIdentity = false;
        if (entity.getPkField() != null && entity.getPkField().isMultiple()) {
            isExtendIdentity = true;

            p.add("@EmbeddedId");
            p.add("private %s %s;", entity.getPkField().getType() , entity.getPkField().getName());
            p.add("");
        }
        while (fields.hasNext()) {
            Field f = fields.next();

            if (isExtendIdentity) {
                if (f.isPrimaryKey())
                    continue;
            }

            setFieldDesc(f, p);

            boolean isIdField =
                    setFieldPkInfo(entity, f, p);

            //setFieldColumnAnnotation(entity, f, p);

            setEnumListAnnotation(entity, f, p);

            setRelationInfo(f, p);
            setJoinColumnAnnotation(f, p);
            setNativeAnnotation(f, p);
            setConvertAnnotation(entity.getIdentity().getDomainId(), f,p);



            String defaultValDefined = getDefaultValueSetup(f);

            /*
            BaseFieldType ft =f.getTypeInfo().getBaseFieldType();
            if(ft == BaseFieldType.DATETIME)
                p.add("@Temporal(TemporalType.TIMESTAMP)");
            else if(ft == BaseFieldType.DATE)
                p.add("@Temporal(TemporalType.DATE)");
            else if(ft == BaseFieldType.TIME)
                p.add("@Temporal(TemporalType.TIME)");

             */


            p.add("%s %s %s%s;", "private", f.getTypeInfo().getDefaultTypeName(), f.getName(), defaultValDefined);
            p.add("");
            p.add("");

            if(isIdField) {
                String idGenerator = f.getAnnotationValue("genid");
                if(idGenerator!=null) {
                    p.add("@PrePersist");
                    p.add("public void prePersist() {");
                    p.add("  if (%s == null)", f.getName());
                    p.add("        %s = %s();",f.getName(), idGenerator);
                    p.add("}");
                    p.add("");
                    p.add("");

                }
            }

        }

        Annotation templateAnt = entity.getMetaInfo().getMetaAnnotation("template");
        if(templateAnt!=null) {
            String templates = templateAnt.getValue();

            if(templates!=null && templates.length()>0) {
                String[] templateNmArray = templates.split(",");
                EntityModels models = this.publisher.getECoreModelContext().getDomain().getModel().getEntityModels();
                for(String templateNm : templateNmArray) {
                    Entity templateEntity = this.publisher.getECoreModelContext().findModelComponent(templateNm, Entity.class);
                    if(templateEntity!=null)
                        loadFieldInfo(templateEntity, p);
                }
            }

        }
    }

    private String getDefaultValueSetup(Field f) {
        if (!f.hasAnnotation("default")) return "";
        String val = f.getAnnotation("default").getValue();

        BaseFieldType fieldType = f.getTypeInfo().getBaseFieldType();

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
        } else if (f.getTypeInfo().isBigDecimal()) {
            // BigDecimal default value handling
            if ("0".equals(val) || "0.0".equals(val) || "0.00".equals(val)) {
                return " = BigDecimal.ZERO";
            } else if ("1".equals(val) || "1.0".equals(val) || "1.00".equals(val)) {
                return " = BigDecimal.ONE";
            } else if ("10".equals(val) || "10.0".equals(val) || "10.00".equals(val)) {
                return " = BigDecimal.TEN";
            } else {
                return " = new BigDecimal(\"" + val + "\")";
            }
        } else if (fieldType == BaseFieldType.P_LONG || fieldType == BaseFieldType.LONG) {
            // Check if user already added L postfix
            if (!val.toUpperCase().endsWith("L")) {
                val = val + "L";
            }
            return " = " + val;
        } else if (fieldType == BaseFieldType.P_FLOAT || fieldType == BaseFieldType.FLOAT) {
            // Check if user already added F postfix
            if (!val.toUpperCase().endsWith("F")) {
                val = val + "F";
            }
            return " = " + val;
        } else if (fieldType == BaseFieldType.P_DOUBLE || fieldType == BaseFieldType.DOUBLE) {
            // Check if user already added D postfix (D is optional for double)
            if (!val.toUpperCase().endsWith("D") && !val.contains(".")) {
                val = val + ".0"; // Add decimal point for integer values
            }
            return " = " + val;
        } else {
            return " = " + val;
        }
    }



    private boolean setFieldPkInfo(Entity entity, Field field, CodeTemplate.Paragraph paragraph) {

        boolean isIdField = false;

        boolean isSequence =field.hasAnnotation("sequence");

        if (field.hasAnnotation("id") || field.hasAnnotation("pk") || field.isPrimaryKey()) {
            paragraph.add("@Id");
            isIdField = true;


        }else{
            if(isSequence) {
                String seqName = entity.getIdentity().getName()+"_"+field.getName()+"_seq";

                paragraph.add("@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"seq_gen\")");
                paragraph.add("@GenericGenerator(");
                paragraph.add("  name = \"seq_gen\",");
                paragraph.add("  strategy = \"org.hibernate.id.enhanced.SequenceStyleGenerator\",");
                paragraph.add("  parameters = {");
                paragraph.add("    @Parameter(name = \"sequence_name\", value = \"%s\"),",seqName);
                paragraph.add("    @Parameter(name = \"initial_value\", value = \"1\"),");
                paragraph.add("    @Parameter(name = \"increment_size\", value = \"1\")");
                paragraph.add("}");
                paragraph.add(")");
            }
        }


        if (field.getGenerationType() != null) {
            paragraph.add("@GeneratedValue(strategy = GenerationType." + field.getGenerationType());
        }

        return isIdField;
    }


    /**
     * Sets annotation information for configuring relational information in JPA.
     *
     * @param field     Field
     * @param paragraph paragraph
     */
    private void setRelationInfo(Field field, CodeTemplate.Paragraph paragraph) {
        TypeInfo typeInfo = field.getTypeInfo();

        boolean isEntityType = false;
        if(this.model.getEntityModels().findByName(typeInfo.getCoreItemType())!=null)
            isEntityType = true;

        if (field.hasAnnotation("Embedded")) {
            paragraph.add("@Embedded");
        }

        else if (typeInfo.isList()) {
            if(isEntityType) {

                String fetchType = field.getAnnotationValue(EntityAnnotation.FETCH);

                if(fetchType==null)
                    fetchType = "LAZY";
                else
                    fetchType = fetchType.toUpperCase();

                String cascade = getCascadeInfo(field);
                if(!cascade.isEmpty())
                    cascade = ","+cascade;
                if(field.hasAnnotation(EntityAnnotation.CASCADE)) {
                    String cascadeType = field.getAnnotationValue(EntityAnnotation.CASCADE);
                    if(cascadeType==null)
                        cascadeType = "ALL, orphanRemoval = true";
                    else
                        cascadeType = cascadeType.toUpperCase(Locale.ROOT);

                    cascade = ",cascade = CascadeType."+cascadeType;
                }


                paragraph.add("@OneToMany(fetch = FetchType.%s %s)",fetchType,cascade);
            }

        } else if (isEntityType) {


            String targetEntityName = typeInfo.getDefaultTypeName();
            Entity entity = this.publisher.getECoreModelContext().getDomain().getModel().getEntityModels()
                    .findByName(targetEntityName);

            if (entity != null) {
                if (field.hasAnnotation("onetoone")) {
                    String cascade = getCascadeInfo(field);
                    if(cascade.isEmpty()) {
                        paragraph.add("@OneToOne");
                    }else{
                        paragraph.add("@OneToOne(%s)", cascade);
                    }

                } else {
                    paragraph.add(("@ManyToOne"));
                }


                String joinFieldName = field.getName() + "_id";
                if (!field.hasAnnotation("join")) {
                    paragraph.add(("@JoinColumn(columnDefinition = \"" + joinFieldName + "\")"));
                }
            }

        }
    }

    private String makeLengthInfo(Field field) {
        if(!field.getTypeInfo().isStringType())
            return null;

        String fieldNm = field.getName();

        if(fieldNm.contains("Addr")) {
            return "256";
        }
        else if(fieldNm.contains("Type")) {
            return "10";
        }
        else if(fieldNm.contains("Name") || fieldNm.contains("Nm")) {
            return "64";
        }else if(fieldNm.contains("Code")) {
            return "32";
        }else if(fieldNm.contains("Status")) {
            return "10";
        }else if(fieldNm.contains("Date")) {
            // yyyy-MM-dd
            return "10";
        }else if(fieldNm.contains("Time")) {
            // HH:mm:ss
            return "8";
        }else if(fieldNm.contains("Zip")) {
            return "7";
        }else if(fieldNm.contains("Tel")) {
            return "15";
        }

        return null;
    }

    /**
     * Sets JPA annotation information according to the configuration information of each field.
     *
     * @param field
     * @param paragraph
     */
    private void setFieldColumnAnnotation(Entity entity, Field field, CodeTemplate.Paragraph paragraph) {

        String labelTxt = field.getAnnotationValue(EntityAnnotation.COMMENT);
        if(labelTxt!=null && !labelTxt.isEmpty())
            paragraph.add("@Comment(\"%s\")", labelTxt);


        List<String> list = new ArrayList<>();


        String dbColumnName = field.getDbColumnName();

        if (dbColumnName !=null)
            list.add("name = \"" + dbColumnName + "\"");



        if (field.hasAnnotation("text")) {
            list.add("columnDefinition = \"TEXT\"");
        }
        else if (field.hasAnnotation("longtext")) {
            list.add("columnDefinition = \"LONGTEXT\"");
        }
        if (field.hasAnnotation("notnull")) {
            list.add("nullable = false");
        }

        if("false".equals(field.getAnnotationValue(EntityAnnotation.UPDATABLE))) {
            list.add("updatable = false");
        }

        EnumModel enumModel = this.getEnumModelFromType(entity.getIdentity().getDomainId(),field);

        boolean isLengthDefine = false;
        String lengthVal = field.getAnnotationValue(EntityAnnotation.LENGTH);
        if (lengthVal != null) {
            list.add("length = " + lengthVal);
            isLengthDefine = true;
        }else {
            if(enumModel!=null) {
                Annotation dbAnt = enumModel.getMetaInfo().getMetaAnnotation("db");
                if(dbAnt!=null) {
                    String dbFieldNm = dbAnt.getValue();
                    if(dbFieldNm!=null) {
                        Field enumField = enumModel.getFieldItems().findByName(dbFieldNm);
                        if(enumField!=null) {
                            lengthVal = enumField.getAnnotationValue(EntityAnnotation.LENGTH);
                            if (lengthVal != null) {
                                list.add("length = " + lengthVal);
                                isLengthDefine = true;
                            }
                        }
                    }
                }

            }
        }

        if(!isLengthDefine) {
            lengthVal = makeLengthInfo(field);
            if (lengthVal != null) {
                list.add("length = " + lengthVal);
            }
        }

        if (field.getColumnDefinition() != null) {
            list.add("columnDefinition=\"" + field.getColumnDefinition() + "\"");
        }

        Annotation uniqAnnot = field.getAnnotation("unique");
        if (uniqAnnot != null && !"false".equals(uniqAnnot.getValue()))
            list.add("unique = true");

        //insertable=false, updatable=false
        if (field.hasAnnotation("discriminator")) {
            list.add("insertable = false");
            list.add("updatable = false");
        }


        if (list.size() > 0 && !isEnumerationType(field)) {
            String txt = "@Column(" + String.join(", ", list) + ")";
            paragraph.add(txt);
        }
    }

    private void setEnumListAnnotation(Entity entity, Field field , CodeTemplate.Paragraph p) {
        TypeInfo typeInfo = field.getTypeInfo();
        if(!typeInfo.isList()) return;

        String enumName = typeInfo.getCoreItemType();
        EnumModel enumModel = this.model.getEnumModels().findByName(enumName);
        if(enumModel==null) return;

        String tblName = StringUtils.camelToSnake(entity.getIdentity().getName())
                +"_"+StringUtils.camelToSnake(field.getName());


        p.add("@ElementCollection( fetch = FetchType.EAGER)");
        p.add("@CollectionTable(name = \"%s\", joinColumns = @JoinColumn(name = \"map_seq\"))",tblName );
        p.add("@Column(name = \"code\")");
    }


    private void setJoinColumnAnnotation(Field field, CodeTemplate.Paragraph paragraph) {
        List<String> list = new ArrayList<>();

        if (field.hasAnnotation("join")) {
            String fieldNm = field.getAnnotation("join").getValue();
            list.add("columnDefinition=\"" + fieldNm + "\"");
        }

        if (list.size() > 0) {
            String txt = "@JoinColumn(" + String.join(", ", list) + ")";
            paragraph.add(txt);
        }
    }

    private void setNativeAnnotation(Entity entity, CodeTemplate.Paragraph paragraph) {
        setNativeAnnotation(entity.getMetaInfo().getMetaAnnotationMap(), paragraph);
    }

    private void setNativeAnnotation(Field field, CodeTemplate.Paragraph paragraph) {
        setNativeAnnotation(field.getAnnotationMap(), paragraph);
    }


    private EnumModel getEnumModelFromType(String domainId, Field field) {
        if(field.getTypeInfo().isBaseType()) return null;

        String typeName = field.getTypeInfo().getCoreItemType();
        try {
            ModelDomain domain = BaseModelDomain.getModelDomain(domainId);
            EnumModel enumModel = domain.getModel()
                    .getEnumModels().findByName(typeName);

            return enumModel;
        }catch (NullPointerException npe) {}
        return null;
    }

    private void setConvertAnnotation(String domainId, Field field, CodeTemplate.Paragraph paragraph) {
        String type=  field.getTypeInfo().getCoreItemType();

        EnumModel enumModel = getEnumModelFromType(domainId, field);
        if (enumModel != null) {
            if (enumModel.getMetaInfo().hasMetaAnnotation("db")) {
                paragraph.add("@Convert(converter = %s.EntityConverter.class)", type);
            }
        }
    }

    private void setNativeAnnotation(Map<String, Annotation> annotationMap, CodeTemplate.Paragraph paragraph) {

        if(this.publishMode==null) return;
        if(annotationMap ==null) return;

        //String prefix = this.publishMode+":";
        String prefix = "jpa:";
        annotationMap.entrySet().stream()
                .filter(entry -> entry.getKey().contains(prefix))
                .map(Map.Entry::getValue)
                .forEach(paragraph::add);


        String prefix2 = "entity:";
        annotationMap.entrySet().stream()
                .filter(entry -> entry.getKey().contains(prefix2))
                .map(Map.Entry::getValue)
                .forEach(paragraph::add);
    }



    private void setSqlDeleteAnnotation(Field field, CodeTemplate.Paragraph paragraph) {
        if (field.hasAnnotation("chkdelete")) {

        }
    }
}
