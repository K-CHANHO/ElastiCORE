package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ECoreModelContext;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.*;
import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.core.BaseECoreModelContext;
import io.elasticore.base.model.core.ListMap;
import io.elasticore.base.model.core.RelationshipManager;
import io.elasticore.base.model.entity.*;
import io.elasticore.base.model.enums.EnumModel;
import io.elasticore.base.model.relation.ModelRelationship;
import io.elasticore.base.model.relation.RelationType;
import io.elasticore.base.model.shadow.SourceShadowModel;
import io.elasticore.base.util.CodeTemplate;
import io.elasticore.base.util.ConsoleLog;
import io.elasticore.base.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles the publishing of source files for entities.
 * This class is responsible for generating Java source code
 * based on entity definitions and their annotations.
 */
public class EntitySrcFilePublisher extends SrcFilePublisher {

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
    public EntitySrcFilePublisher(CodePublisher publisher) {
        super(publisher);


        this.publisher = publisher;
        this.model = this.publisher.getECoreModelContext().getDomain().getModel();

        this.publishMode = model.getConfig("mode");

        String templatePath = model.getConfig("template.entity");
        if (templatePath == null)
            templatePath = "elasticore-template/entity.tmpl";
        else
            templatePath = "resource://" + templatePath;

        this.javaClassTmpl = CodeTemplate.newInstance(templatePath);

        ECoreModel model = publisher.getECoreModelContext().getDomain().getModel();
        this.packageName = model.getNamespace(ConstantParam.KEYNAME_ENTITY);

        if (model.getEnumModels().getItems().size() > 0)
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
    private String getExtendInfo(Entity entity, CodeTemplate.Paragraph importList) {

        Annotation annotation = entity.getMetaInfo().getMetaAnnotation(EntityAnnotation.META_EXTEND);
        if (annotation != null)
            return "extends " + resolveExtendClassName(entity, annotation.getValue(), importList);
        else {
            String rollupTarget = entity.getMetaInfo().getMetaAnnotationValue(EntityAnnotation.META_ROLL_UP_TARGET);
            if (rollupTarget != null) {
                return "extends " + resolveExtendClassName(entity, rollupTarget, importList);
            }
        }
        return "";
    }

    /**
     * Resolves the parent class name used in an "extends" clause.
     * Directly searches each loaded domain's EntityModels / DataTransferModels / EnumModels
     * (bypassing ECoreModel.findModelByName which re-fetches via the static context and
     * can misroute in multi-module setups). If the parent is found in a different domain
     * than the current entity, registers its fully-qualified name into importList and
     * returns the simple name. Otherwise returns the simple name without touching imports.
     */
    private String resolveExtendClassName(Entity entity, String className, CodeTemplate.Paragraph importList) {
        if (className == null || className.isEmpty()) return className;

        ECoreModelContext ctx = BaseECoreModelContext.getContext();
        if (ctx == null) return className;

        String currentDomainId = entity.getIdentity().getDomainId();

        // Explicit "domainId:ClassName" cross-domain syntax
        String searchDomain = null;
        String searchName = className;
        int colonIdx = className.indexOf(':');
        if (colonIdx > 0) {
            searchDomain = className.substring(0, colonIdx);
            searchName = className.substring(colonIdx + 1);
        }

        DataModelComponent found = null;
        String foundDomain = null;

        if (searchDomain != null) {
            found = lookupInDomain(ctx, searchDomain, searchName);
            if (found != null) foundDomain = searchDomain;
        } else {
            // Check current domain first (same-domain path, no import needed)
            DataModelComponent cur = lookupInDomain(ctx, currentDomainId, searchName);
            if (cur != null) {
                return cur.getIdentity().getName();
            }
            // Search every other loaded domain across all modules
            String[] allDomains = ctx.getAllDomainNames();
            if (allDomains != null) {
                for (String domainNm : allDomains) {
                    if (domainNm == null || domainNm.equals(currentDomainId)) continue;
                    DataModelComponent dmc = lookupInDomain(ctx, domainNm, searchName);
                    if (dmc != null) {
                        found = dmc;
                        foundDomain = domainNm;
                        break;
                    }
                }
            }
        }

        if (found == null) {
            ConsoleLog.storeLog("DSL_WARN",
                    String.format("[%s] %s: @extend target '%s' not found in any loaded domain",
                            currentDomainId, entity.getIdentity().getName(), searchName));
            return className;
        }

        if (foundDomain != null && foundDomain.equals(currentDomainId)) {
            return found.getIdentity().getName();
        }

        // Cross-domain parent: resolve FQCN and register import
        String simpleName = found.getIdentity().getName();
        String fqcn = resolveFqcn(ctx, foundDomain, found, simpleName);

        if (fqcn != null && fqcn.indexOf('.') > 0) {
            if (importList != null) {
                importList.add(fqcn);
            }
            int lastDot = fqcn.lastIndexOf('.');
            return fqcn.substring(lastDot + 1);
        }

        ConsoleLog.storeLog("DSL_WARN",
                String.format("[%s] %s: failed to resolve namespace for cross-domain @extend target '%s' (found in domain '%s')",
                        currentDomainId, entity.getIdentity().getName(), searchName, foundDomain));
        return simpleName;
    }

    /**
     * Directly searches Entity / DataTransfer / Enum models of the given domain without
     * going through ECoreModel.findModelByName (which redirects via the static context).
     */
    private DataModelComponent lookupInDomain(ECoreModelContext ctx, String domainId, String name) {
        if (ctx == null || domainId == null || name == null) return null;
        ModelDomain d = ctx.getDomain(domainId);
        if (d == null) return null;
        ECoreModel m = d.getModel();
        if (m == null) return null;
        try {
            if (m.getEntityModels() != null) {
                Entity e = m.getEntityModels().findByName(name);
                if (e != null) return e;
            }
            if (m.getDataTransferModels() != null) {
                DataModelComponent dt = m.getDataTransferModels().findByName(name);
                if (dt != null) return dt;
            }
            if (m.getEnumModels() != null) {
                DataModelComponent em = m.getEnumModels().findByName(name);
                if (em != null) return em;
            }
        } catch (RuntimeException re) {
            // fall through
        }
        return null;
    }

    /**
     * Resolves the fully-qualified class name for a cross-domain parent.
     * Tries component.getFullName() first; on failure, constructs it from the
     * domain's entity/dto/enum namespace configuration directly.
     */
    private String resolveFqcn(ECoreModelContext ctx, String foundDomain, DataModelComponent found, String simpleName) {
        // Try the standard API path
        try {
            String fqcn = found.getFullName();
            if (fqcn != null && fqcn.indexOf('.') > 0) return fqcn;
        } catch (RuntimeException re) {
            // fall through
        }
        // Fallback: look up the namespace directly from the found domain's model
        if (foundDomain == null) return null;
        ModelDomain d = ctx.getDomain(foundDomain);
        if (d == null || d.getModel() == null) return null;
        String nsKey;
        if (found instanceof Entity) nsKey = "entity";
        else if (found instanceof EnumModel) nsKey = "enum";
        else nsKey = "dto";
        String ns = d.getModel().getNamespace(nsKey);
        if (ns == null || ns.isEmpty()) return null;
        return ns + "." + simpleName;
    }


    private String getAbstractInfo(Entity entity, CodeTemplate.Paragraph p) {
        if (entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_ABSTRACT)) {
            return "abstract";
        }
        /*
        if(p.contains("@DiscriminatorColumn(")) {
            return "abstract";
        }
         */
        return "";
    }

    private CodeTemplate.Paragraph getEntityAnnotation(Entity entity, String j2eePkgNm,CodeTemplate.Paragraph importList) {
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();


        AtomicBoolean isInheritance = new AtomicBoolean(false);
        AtomicBoolean hasPkField = new AtomicBoolean(false);

        entity.getItems().forEachRemaining(field -> {
            if (field.hasAnnotation(EntityAnnotation.DISCRIMINATOR)) {
                isInheritance.set(true);
                p.add("@Inheritance(strategy = InheritanceType.SINGLE_TABLE)");
                String dbColumnNm = field.getDbColumnName();
                String type = field.getTypeInfo().getDefaultTypeName().toUpperCase(Locale.ROOT);
                p.add("@DiscriminatorColumn(name = \"" + dbColumnNm + "\", discriminatorType = DiscriminatorType." + type + ")");

                String defaultDesciminatorValue = field.getAnnotationValue(EntityAnnotation.DISCRIMINATOR);
                if (defaultDesciminatorValue != null) {
                    p.add("@DiscriminatorValue(\"" + defaultDesciminatorValue + "\")");
                }
            }
            else if(field.isPrimaryKey()) {
                hasPkField.set(true);
            }
        });

        if(entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.AUDITED)) {
            p.add("@Audited");
            importList.add("org.hibernate.envers.Audited");
        }

        boolean isEntityClass = true;

        if (!isInheritance.get() && !hasPkField.get() && entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_ABSTRACT)) {
            isEntityClass = false;
            p.add("@MappedSuperclass");
        } else if (!entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_EMBEDDABLE)) {
            p.add("@Entity");
        }


        MetaInfo metaInfo = entity.getMetaInfo();


        /*
        if(metaInfo.hasMetaAnnotation("DynamicUpdate"))
            p.add("@org.hibernate.annotations.DynamicUpdate");

         */

        boolean isRollUp = metaInfo.hasMetaAnnotation(EntityAnnotation.META_ROLL_UP);

        if (isEntityClass) {

            CodeTemplate.Paragraph idxPara = CodeTemplate.newParagraph();
            Annotation idxAnnot = metaInfo.getMetaAnnotation(EntityAnnotation.META_INDEX);
            if (idxAnnot != null) {
                idxPara.add("  indexes = {");

                boolean isFirst = true;
                for (Annotation ant : idxAnnot.getSiblings()) {
                    Properties properties = ant.getProperties();
                    String name = properties.getProperty("name");
                    String comlumnList = properties.getProperty("columnList");
                    String unique = properties.getProperty("unique");
                    StringBuilder sb = new StringBuilder();
                    if (name != null && !name.isEmpty()) {
                        sb.append("name=" + StringUtils.quoteString(name));
                    }
                    if (comlumnList != null && !comlumnList.isEmpty()) {
                        if (sb.length() > 0)
                            sb.append(", ");
                        // Check if columnList already has quotes to avoid double-quoting
                        String columnListValue = comlumnList;
                        if (!(columnListValue.startsWith("\"") && columnListValue.endsWith("\""))) {
                            columnListValue = StringUtils.quoteString(columnListValue);
                        }
                        sb.append("columnList=" + columnListValue);
                    }
                    if (unique != null && !unique.isEmpty()) {
                        if (sb.length() > 0)
                            sb.append(", ");
                        sb.append("unique=" + unique);
                    }
                    String commaTxt = " ";
                    if (!isFirst)
                        commaTxt = ",";
                    idxPara.add("   %s@%s.persistence.Index(%s)", commaTxt, j2eePkgNm, sb.toString());
                    isFirst = false;

                }
                idxPara.add("  }");
            }

            String tbl_prefix = this.publisher.getECoreModelContext().getDomain().getModel().getConfig("table.prefix");

            String tblNameInfo = null;
            Annotation tblAnnotation = metaInfo.getMetaAnnotation(EntityAnnotation.META_TABLE);
            if (tblAnnotation != null) {
                String dbTblNm = tblAnnotation.getValue();
                tblNameInfo = dbTblNm;
            }

            if(tbl_prefix!=null && !tbl_prefix.isEmpty()) {
                if(tblNameInfo==null)
                    tblNameInfo = entity.getTableName();
                tblNameInfo = tbl_prefix + tblNameInfo;
            }
            if(tblNameInfo!=null) {
                tblNameInfo = "name=\"" + tblNameInfo + "\"";
            }

            // Resolve schema: entity-level @schema annotation takes priority over env.yml tableSchema config
            String schemaName = metaInfo.getMetaAnnotationValue(EntityAnnotation.META_SCHEMA);
            if (schemaName == null || schemaName.isEmpty()) {
                schemaName = this.publisher.getECoreModelContext().getDomain().getModel().getConfig("tableSchema");
            }
            String schemaInfo = null;
            if (schemaName != null && !schemaName.isEmpty()) {
                schemaInfo = "schema=\"" + schemaName + "\"";
            }

            if(!isRollUp) {
                if (idxPara.size() > 0 || tblNameInfo != null || schemaInfo != null) {
                    // Build @Table attributes
                    StringBuilder tableAttrs = new StringBuilder();
                    if (tblNameInfo != null) {
                        tableAttrs.append(tblNameInfo);
                    }
                    if (schemaInfo != null) {
                        if (tableAttrs.length() > 0) tableAttrs.append(", ");
                        tableAttrs.append(schemaInfo);
                    }

                    if (idxPara.size() > 0) {
                        if (tableAttrs.length() > 0) tableAttrs.append(",");
                        p.add("@" + j2eePkgNm + ".persistence.Table(%s", tableAttrs.toString());
                        p.add(idxPara);
                        p.add("  )");
                    } else {
                        p.add("@" + j2eePkgNm + ".persistence.Table(%s)", tableAttrs.toString());
                    }

                }

                if (metaInfo.hasMetaAnnotation(EntityAnnotation.META_IMMUTABLE)) {
                    p.add("@org.hibernate.annotations.Immutable");

                    if (tblAnnotation == null) {
                        String viewName = metaInfo.getMetaAnnotationValue(EntityAnnotation.META_IMMUTABLE);
                        if (viewName != null) {
                            p.add("@" + j2eePkgNm + ".persistence.Table(name=\"" + viewName + "\")");
                        }
                    }
                }
            }



            if (!"false".equals(metaInfo.getMetaAnnotationValue(EntityAnnotation.META_DYNAMIC_UPDATE))) {
                if (!metaInfo.hasMetaAnnotation(EntityAnnotation.META_IMMUTABLE))
                    p.add("@org.hibernate.annotations.DynamicUpdate");
            }
            if (metaInfo.hasMetaAnnotation(EntityAnnotation.META_DYNAMIC_INSERT))
                p.add("@org.hibernate.annotations.DynamicInsert");


        }


        //Entity = BaseModelDomain.getModelDomain(this.getIdentity().getDomainId()).getModel().getEntityModels().findByName(toName);
        if (metaInfo.hasMetaAnnotation(EntityAnnotation.META_EMBEDDABLE)) {
            p.add("@Embeddable");
        } else {
            ComponentIdentity identity = entity.getIdentity();

            List<ModelRelationship> relationshipList = RelationshipManager.getInstance(identity.getDomainId()).findByToName(identity.getName());
            for (ModelRelationship r : relationshipList) {
                if (r.getRelationType() == RelationType.EMBEDDED) {
                    p.add("@Embeddable");
                    break;
                }
            }
        }


        if (metaInfo.hasMetaAnnotation(EntityAnnotation.META_ROLL_UP)) {

            String discriminatorVal = metaInfo.getMetaAnnotationValue(EntityAnnotation.META_ROLL_UP_DISCRIMINATOR);
            if (discriminatorVal == null) {
                discriminatorVal = metaInfo.getMetaAnnotationValue(EntityAnnotation.META_ROLL_UP_TARGET);
            }
            if (discriminatorVal == null) {
                discriminatorVal = entity.getIdentity().getName();
            }

            p.add("@DiscriminatorValue(\"" + discriminatorVal + "\")");
        }


        if (entity.getPkField() != null && entity.getPkField().isMultiple()) {
            //@IdClass(PersonFamilyIdentity.class)
            p.add("@IdClass(%s.class)", entity.getPkField().getType());
        }


        return p;
    }

    private SourceShadowModel shadowModel;


    public void publish(ModelDomain domain, EntityModels entityModels) {
        if (this.packageName == null) return;

        String id = domain.getName() + "_EntityMeta";

        SourceShadowModel shadowModel = new SourceShadowModel(id);

        CodeTemplate.Parameters p = CodeTemplate.newParameters();
        p
                .set("packageName", packageName)
                .set("enumPackageName", enumPackageName);


        CodeTemplate.Paragraph pr = CodeTemplate.newParagraph();
        ModelComponentItems<Entity> items = entityModels.getItems();
        boolean existEntity = false;
        while (items.hasNext()) {
            Entity entity = items.next();
            // Runtime-only DSL entities (@runtime) are excluded from EntityMeta generation.
            if (entity.getMetaInfo() != null && entity.getMetaInfo().hasMetaAnnotation("runtime")) {
                continue;
            }
            CodeTemplate.Paragraph entityMeta = getEntityMeta(entity, shadowModel);
            if (entityMeta != null) {
                pr.add(entityMeta);
                existEntity = true;
            }
        }
        if(!existEntity) return;
        p.set("fieldList", pr);

        CodeTemplate.Paragraph importList = CodeTemplate.newParagraph();

        // Use Set to prevent duplicate imports for Q class
        Set<String> importSet = new HashSet<>();

        // Add individual entity and enum imports for Q class directly
        addIndividualImportsForQ(domain, entityModels, importSet);

        // Add all imports to paragraph
        for (String className : importSet) {
            importList.add(className);
        }
        p.set("importList", importList);

        CodeTemplate codeTemplate = CodeTemplate.newInstance("elasticore-template/Q.tmpl");
        String qualifiedClassName = packageName + ".Q";
        String code = codeTemplate.toString(p);

        this.writeSrcCode(this.publisher, null, qualifiedClassName, code);
    }

    private CodeTemplate.Paragraph getEntityMeta(Entity entity, SourceShadowModel shadowModel) {

        if (entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_ABSTRACT))
            return null;

        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

        String entityClassNm = entity.getIdentity().getName();
        p.add("public static $%s<%s> %s=new $%s<>();", entityClassNm, entityClassNm, entityClassNm, entityClassNm);
        p.add("public static class $%s<T> {", entityClassNm);
        p.add("");
        p.add("    private FieldInfo p=null;");
        p.add("    public $%s() {}", entityClassNm);
        p.add("    public $%s(FieldInfo p) {this.p=p;}", entityClassNm);

        p.add("    public static JoinSpec<%s> joinSpec() {", entityClassNm);
        p.add("    \treturn new JoinSpec<>(%s.class);", entityClassNm);
        p.add("    }");

        p.add("    public static UpdateSpec<%s> updateSpec() {", entityClassNm);
        p.add("    \treturn new UpdateSpec<>(%s.class);", entityClassNm);
        p.add("    }");

        p.add("    public static DeleteSpec<%s> deleteSpec() {", entityClassNm);
        p.add("    \treturn new DeleteSpec<>(%s.class);", entityClassNm);
        p.add("    }");


        ListMap allFieldListMap = entity.getAllFieldListMap();
        List<Field> fieldList = allFieldListMap.getList();

        for (Field field : fieldList) {
            String fieldName = field.getIdentity().getName();

            String baseType = "null";
            if (field.getTypeInfo().isGenericType()) {
                baseType = field.getTypeInfo().getBaseFieldType().getName();

                if (baseType != null && !baseType.isEmpty())
                    baseType = baseType + ".class";
            }
            if (!field.getTypeInfo().isBaseType()) {
                shadowModel.setNamespaceInfo(field.getTypeInfo().getCoreItemType());
            }

            p.add("");
            p.add("    public final FieldInfo<T> get%s() {return new FieldInfo(%s.class,%s,%s.class, %s,p);}"
                    , StringUtils.capitalize(fieldName)
                    //, entityClassNm
                    , entityClassNm
                    , StringUtils.quoteString(fieldName)
                    , field.getTypeInfo().getCoreItemType()
                    , baseType

            );
            p.add("    public final String %s=%s;", fieldName, StringUtils.quoteString(fieldName));
            //p.add("    public final FieldInfo get%s() {return F_%s;}" ,StringUtils.capitalize(fieldName),fieldName);

            String typeNm = field.getTypeInfo().getInitTypeInfo();
            Entity refEntity = findEntityByName(typeNm);
            EnumModel enumModel = findEnumModel(typeNm);

            if (field.getTypeInfo().isBaseType() || enumModel != null) {
                p.add("    public Specification<T> %s(Op op,Object value) {return get%s().where(op,value);}"
                        //, entityClassNm
                        , fieldName
                        , StringUtils.capitalize(fieldName)
                );
                p.add("    public Specification<T> %s(Op op,Object value,boolean chkEmpty) {return get%s().where(op,value,chkEmpty);}"
                        //, entityClassNm
                        , fieldName
                        , StringUtils.capitalize(fieldName)
                );
            } else if (refEntity != null) {
                // if entity

                {
                    String refEntityNm = refEntity.getIdentity().getName();

                    // Check if referenced entity is in a different domain
                    String refDomainId = refEntity.getIdentity().getDomainId();
                    String currentDomainId = entity.getIdentity().getDomainId();
                    String qClassPrefix = "";

                    if (!refDomainId.equals(currentDomainId)) {
                        // Cross-domain reference - use full package path to Q class
                        // Cannot use "Q." because that refers to current class, not the other domain's Q
                        String fullNameWithNS = findFullNameWithNS(refEntityNm);
                        if (fullNameWithNS != null) {
                            int lastDotIndex = fullNameWithNS.lastIndexOf('.');
                            if (lastDotIndex > 0) {
                                String refEntityPackage = fullNameWithNS.substring(0, lastDotIndex);
                                qClassPrefix = refEntityPackage + ".Q.";
                            }
                        }
                    }

                    p.add("    public final %s$%s<T> %s() {return new %s$%s(get%s());}"
                            , qClassPrefix  // Full path for cross-domain, empty for same domain
                            , refEntityNm
                            , fieldName
                            , qClassPrefix  // Full path for cross-domain, empty for same domain
                            , refEntityNm
                            , StringUtils.capitalize(fieldName));

                }
            }

        }
        p.add("}");
        p.add("");

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
        if (this.packageName == null) return;

        Annotation typeAnnotation = entity.getMetaInfo().getMetaAnnotation(EntityAnnotation.META_TYPE);
        if (typeAnnotation != null) {
            if ("template".equals(typeAnnotation.getValue()))
                return;
        }
        String j2eePkgNm = getPersistentPackageName(domain);
        publishEntityIdentityClass(domain, entity);

        CodeTemplate.Parameters p = CodeTemplate.newParameters();
        String classNm = entity.getIdentity().getName();

        CodeTemplate.Paragraph importList = CodeTemplate.newParagraph(true);

        this.paragraphForEntity = getEntityAnnotation(entity, j2eePkgNm ,importList);

        setNativeAnnotation(entity, this.paragraphForEntity);

        shadowModel = new SourceShadowModel(entity.getIdentity().getName());
        this.model.setShadowModel(shadowModel);

        // must call 'getFieldInfo' first
        CodeTemplate.Paragraph pr = getFieldInfo(entity ,importList);



        // Use Set to prevent duplicate imports
        Set<String> importSet = new HashSet<>();

        // Add individual entity class imports
        Set<String> individualClassSet = shadowModel.getIndividualClassSet();
        importSet.addAll(individualClassSet);

        // Add individual enum imports
        if (enumPackageName != null) {
            addEnumImportsToSet(entity, importSet);
        }

        // Add all imports to paragraph
        for (String className : importSet) {
            importList.add(className);
        }


        if (this.paragraphForEntity.contains("@Entity")) {
            importList.add(j2eePkgNm + ".persistence.Entity");
        }

        if (pr.contains("@Convert(")) {
            // TODO
            //importList.add(j2eePkgNm+".persistence.Convert");
        }

        String label = entity.getMetaInfo().getLabel();
        String desc = entity.getMetaInfo().getDescription();

        String sbdc = null;
        if(StringUtils.hasValue(label) || StringUtils.hasValue(desc)  ) {
            sbdc = StringUtils.fixNull(label) +"\n"+StringUtils.fixNull(desc);
            sbdc = StringUtils.addPrefixToEachLine(sbdc, " * ");

        }

        p
                .set("packageName", packageName)
                .set("enumPackageName", enumPackageName)
                .set("abstract", getAbstractInfo(entity, this.paragraphForEntity))
                .set("classAnnotationList", this.paragraphForEntity)
                .set("extendInfo", getExtendInfo(entity, importList))
                .set("j2eePkgName", j2eePkgNm)
                .set("importList", importList)
                .set("implementInfo", "implements java.io.Serializable")
                .set("description", StringUtils.fixNull( sbdc, " *"))
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

            CodeTemplate.Paragraph p2 = CodeTemplate.newParagraph();
            p2.add("@Embeddable");
            p2.add("@lombok.EqualsAndHashCode");
            p2.add("@AllArgsConstructor");

            CodeTemplate.Parameters param = CodeTemplate.newParameters();
            param
                    .set("packageName", packageName)
                    .set("abstract", "")
                    .set("extendInfo", "")
                    .set("j2eePkgName", j2eePkgNm)
                    .set("implementInfo", "implements java.io.Serializable")
                    .set("classAnnotationList", p2)
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
            setFieldColumnAnnotation(entity, f, p);

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

    private CodeTemplate.Paragraph getFieldInfo(Entity entity ,CodeTemplate.Paragraph importList) {
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

        loadFieldInfo(entity, p,importList,0);
        return p;
    }

    /**
     * Collects inherited primary key fields from parent entities recursively.
     * @param entity The entity to collect inherited PK fields from
     * @return List of inherited PK fields with their type information
     */
    private List<Field> collectInheritedPkFields(Entity entity) {
        List<Field> inheritedPkFields = new ArrayList<>();

        String parentName = entity.getMetaInfo().getMetaAnnotationValue(EntityAnnotation.META_EXTEND);
        if (parentName == null || parentName.isEmpty()) {
            return inheritedPkFields;
        }

        // Find parent entity using the model's findModelByName method
        //DataModelComponent component = model.findModelByName(parentName);
        DataModelComponent component = this.findEntityByName(parentName);
        if (component == null || !(component instanceof Entity)) {
            return inheritedPkFields;
        }

        Entity parentEntity = (Entity) component;

        // Recursively collect PK fields from parent hierarchy
        inheritedPkFields.addAll(collectInheritedPkFields(parentEntity));

        // Collect PK fields from current parent
        ModelComponentItems<Field> parentFields = parentEntity.getItems();
        while (parentFields.hasNext()) {
            Field f = parentFields.next();
            if (f.isPrimaryKey()) {
                inheritedPkFields.add(f);
            }
        }

        return inheritedPkFields;
    }

    private void loadFieldInfo(Entity entity, CodeTemplate.Paragraph p,CodeTemplate.Paragraph importList, int depth) {


        boolean isExtendIdentity = false;
        /*
        if (entity.getPkField() != null && entity.getPkField().isMultiple()) {
            isExtendIdentity = true;

            p.add("@EmbeddedId");
            p.add("private %s %s;", entity.getPkField().getType() , entity.getPkField().getName());
            p.add("");
        }
        */


        String parentName = entity.getMetaInfo().getMetaAnnotationValue(EntityAnnotation.META_EXTEND);

        String entityName = entity.getIdentity().getName();

        // for prePersist
        boolean isMultiplePK = entity.getPkField() != null && entity.getPkField().isMultiple();
        CodeTemplate.Paragraph prePersist = CodeTemplate.newParagraph();

        // Collect inherited PK fields from parent entities
        List<Field> inheritedPkFields = collectInheritedPkFields(entity);

        // Collect direct PK fields from this entity
        List<Field> directPkFields = new ArrayList<>();
        ModelComponentItems<Field> fieldsPre = entity.getItems();
        while (fieldsPre.hasNext()) {
            Field f = fieldsPre.next();
            if(f.isPrimaryKey()) {
                directPkFields.add(f);
            }
        }

        // Generate constructor only if there are PK fields (inherited or direct)
        if(depth == 0 && (!inheritedPkFields.isEmpty() || !directPkFields.isEmpty())) {
            StringBuilder sb1 = new StringBuilder(); // Constructor parameters
            StringBuilder sb2 = new StringBuilder(); // super() call
            StringBuilder sb3 = new StringBuilder(); // this.field assignments

            // Build parameter list: inherited PKs first, then direct PKs
            for (Field f : inheritedPkFields) {
                if(sb1.length() > 0) sb1.append(", ");
                sb1.append(f.getTypeInfo().getCoreItemType()).append(" ").append(f.getName());
            }
            for (Field f : directPkFields) {
                if(sb1.length() > 0) sb1.append(", ");
                sb1.append(f.getTypeInfo().getCoreItemType()).append(" ").append(f.getName());
            }

            // If there are inherited PK fields, generate super() call
            if (!inheritedPkFields.isEmpty()) {
                sb2.append("    super(");
                for (int i = 0; i < inheritedPkFields.size(); i++) {
                    if(i > 0) sb2.append(", ");
                    sb2.append(inheritedPkFields.get(i).getName());
                }
                sb2.append(");");
            }

            // Generate field assignments for direct PK fields
            for (Field f : directPkFields) {
                if(sb3.length() > 0) sb3.append("\n");
                sb3.append("    this.").append(f.getName()).append(" = ").append(f.getName()).append(";");
            }

            // Output constructor
            p.add("public %s(%s) {", entityName, sb1.toString());
            if(sb2.length() > 0) p.add(sb2.toString());
            if(sb3.length() > 0) p.add(sb3.toString());
            p.add("}");
            p.add("");
        }

        ModelComponentItems<Field> fields = entity.getItems();
        while (fields.hasNext()) {
            Field f = fields.next();

            if (shadowModel.getField(f.getName()) != null)
                continue;

            if (isExtendIdentity) {
                if (f.isPrimaryKey())
                    continue;
            }

            setFieldDesc(f, p);

            boolean isIdField =
                    setFieldPkInfo(entity, f, p);

            setFieldLabel(entity, f, p);
            boolean hasColumnInfo = setRelationInfo(f, p, entity);
            if (!hasColumnInfo)
                setFieldColumnAnnotation(entity, f, p);

            // Emit @JdbcTypeCode after @Column when @columnDef/@jdbcType are present.
            // Phase 1 scope: validated for BaseFieldType.STRING fields. The helper
            // is a no-op for fields without these annotations, so it is safe for
            // relation fields too.
            setJdbcTypeAnnotation(f, p);

            setEnumListAnnotation(entity, f, p);

            if(f.hasAnnotation(EntityAnnotation.NOT_AUDITED)) {
                p.add("@NotAudited");
                importList.add("org.hibernate.envers.NotAudited");
            }


            setJoinColumnAnnotation(f, p);
            setNativeAnnotation(f, p);
            setConvertAnnotation(entity.getIdentity().getDomainId(), f, p);


            String defaultValDefined = getDefaultValueSetup(f, importList);

            /*
            BaseFieldType ft =f.getTypeInfo().getBaseFieldType();
            if(ft == BaseFieldType.DATETIME)
                p.add("@Temporal(TemporalType.TIMESTAMP)");
            else if(ft == BaseFieldType.DATE)
                p.add("@Temporal(TemporalType.DATE)");
            else if(ft == BaseFieldType.TIME)
                p.add("@Temporal(TemporalType.TIME)");

             */

            shadowModel.addField(f);

            storeRefEntityInfo(entity.getIdentity().getName(), f.getTypeInfo().getCoreItemType());

            p.add("%s %s %s%s;", "private", f.getTypeInfo().getDefaultTypeName(), f.getName(), defaultValDefined);
            p.add("");
            p.add("");

            if (isIdField) {
                String idGenerator = f.getAnnotationValue("genid");
                if(f.hasAnnotation("genid")) {
                    if (idGenerator == null) {
                        if (f.getTypeInfo().getBaseFieldType() == BaseFieldType.STRING) {

                            int fieldSize = f.getLength();
                            if (fieldSize > 1) {
                                idGenerator = "io.elasticore.springboot3.util.IdUtils.newId(" + fieldSize + ")";
                            } else {
                                idGenerator = "java.util.UUID.randomUUID().toString()";
                            }
                        }
                    }else {
                        if(idGenerator.lastIndexOf("()")<0) {
                            idGenerator = idGenerator+"()";
                        }
                    }
                }

                if (idGenerator != null) {

                    if (!isMultiplePK) {
                        p.add("@PrePersist");
                        p.add("public void prePersist() {");
                        p.add("  if (%s == null)", f.getName());
                        p.add("        %s = %s;", f.getName(), idGenerator);
                        p.add("}");
                        p.add("");
                        p.add("");
                    } else {

                        prePersist.add("  if (%s == null)", f.getName());
                        prePersist.add("        %s = %s;", f.getName(), idGenerator);
                    }

                }
            }
        }

        if (isMultiplePK && prePersist.size() > 0) {
            p.add("@PrePersist");
            p.add("public void prePersist() {");
            p.add(prePersist);
            p.add("}");
            p.add("");
            p.add("");
        }

        Annotation templateAnt = entity.getMetaInfo().getMetaAnnotation("template");
        if (templateAnt != null) {
            String templates = templateAnt.getValue();

            if (templates != null && templates.length() > 0) {
                String[] templateNmArray = templates.split(",");

                int nextDepth = depth +1;
                for (String templateNm : templateNmArray) {
                    Entity templateEntity = this.publisher.getECoreModelContext().findModelComponent(templateNm, Entity.class);
                    if (templateEntity != null)
                        loadFieldInfo(templateEntity, p, importList ,nextDepth);
                }
            }
        }
    }

    private String getDefaultValueSetup(Field f, CodeTemplate.Paragraph importList) {
        if (!f.hasAnnotation("default")) return "";
        String val = f.getAnnotation("default").getValue();

        BaseFieldType fieldType = f.getTypeInfo().getBaseFieldType();

        // Check if field type is Enumeration
        String typeName = f.getTypeInfo().getDefaultTypeName();
        io.elasticore.base.model.enums.EnumModel enumModel = this.publisher.getECoreModelContext()
                .findModelComponent(typeName, io.elasticore.base.model.enums.EnumModel.class);
        if (enumModel != null) {
            // Enumeration type
            if (!val.contains(".")) {
                // Add type prefix: Y -> Indicator.Y
                return " = " + typeName + "." + val;
            }
            // Already has dot notation: Type.CUSTOM -> Type.CUSTOM
            return " = " + val;
        }

        // Handle date/time types with "now" default value
        if (fieldType == BaseFieldType.DATE || fieldType == BaseFieldType.DATETIME || fieldType == BaseFieldType.TIME) {
            if ("now".equalsIgnoreCase(val)) {
                if (fieldType == BaseFieldType.DATE) {
                    importList.add("java.time.LocalDate");
                    return " = LocalDate.now()";
                } else if (fieldType == BaseFieldType.DATETIME) {
                    importList.add("java.time.LocalDateTime");
                    return " = LocalDateTime.now()";
                } else if (fieldType == BaseFieldType.TIME) {
                    importList.add("java.time.LocalTime");
                    return " = LocalTime.now()";
                }
            }
            // For other date values, return as is (assuming proper format)
            return " = " + val;
        }

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
            // Add BigDecimal import when needed
            importList.add("java.math.BigDecimal");

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

        boolean isSequence = field.hasAnnotation("sequence");

        if (field.hasAnnotation("id") || field.hasAnnotation("pk") || field.isPrimaryKey()) {
            paragraph.add("@Id");
            isIdField = true;

            if (isSequence) {
                String type = field.getAnnotationValue("sequence");
                if(type==null || type.isEmpty()) {
                    type = "IDENTITY";
                }
                paragraph.add("@GeneratedValue(strategy = GenerationType.%s)",type.toUpperCase());
            }

        } else {
            if (isSequence) {
                String seqName = entity.getIdentity().getName() + "_" + field.getName() + "_seq";

                paragraph.add("@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"seq_gen\")");
                paragraph.add("@GenericGenerator(");
                paragraph.add("  name = \"seq_gen\",");
                paragraph.add("  strategy = \"org.hibernate.id.enhanced.SequenceStyleGenerator\",");
                paragraph.add("  parameters = {");
                paragraph.add("    @Parameter(name = \"sequence_name\", value = \"%s\"),", seqName);
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

    private String getMappedByName(Field field, Entity entity) {

        String mappedBy = field.getAnnotationValue(EntityAnnotation.MAPPED_BY);
        if(StringUtils.hasValue(mappedBy))
            return mappedBy;

        if (field.getTypeInfo().isList()) {
            String fieldCoreType = field.getTypeInfo().getCoreItemType();

            Entity byName = this.publisher.getECoreModelContext().findModelComponent(fieldCoreType, Entity.class);
            ModelComponentItems<Field> items = byName.getItems();
            String entityClassName = entity.getIdentity().getName();
            while (items.hasNext()) {
                Field f = items.next();
                if (entityClassName.equals(f.getTypeInfo().getInitTypeInfo())) {
                    return f.getName();
                }
            }
        }
        return null;
    }

    /**
     * Sets annotation information for configuring relational information in JPA.
     *
     * @param field     Field
     * @param paragraph paragraph
     */
    private boolean setRelationInfo(Field field, CodeTemplate.Paragraph paragraph, Entity ety) {
        TypeInfo typeInfo = field.getTypeInfo();

        boolean hasColumnInfo = false;

        boolean isEntityType = false;

        if (typeInfo.isBaseType())
            return false;

        Entity typeEntity = this.publisher.getECoreModelContext().findModelComponent(typeInfo.getCoreItemType(), Entity.class);


        if (typeEntity != null)
            isEntityType = true;

        String fetchType = field.getAnnotationValue(EntityAnnotation.FETCH);

        if (isEntityType &&
                (field.hasAnnotation(EntityAnnotation.EMBEDDED)
                        || typeEntity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_EMBEDDABLE))) {
            paragraph.add("@Embedded");

            String embPrefix = field.getAnnotationValue(EntityAnnotation.EMBEDDED_PREFIX);
            if (embPrefix != null) {
                paragraph.add("@AttributeOverrides({");

                boolean isFirst = true;
                ModelComponentItems<Field> items = typeEntity.getItems();

                while (items.hasNext()) {

                    Field ef = items.next();

                    String commaTxt = "";
                    if (!isFirst)
                        commaTxt = ",";

                    String newName = embPrefix + "_" + StringUtils.camelToSnake(ef.getName());
                    paragraph.add(" %s @AttributeOverride(name = \"%s\", column = @Column(name = \"%s\"))", commaTxt, ef.getName(), newName);

                    isFirst = false;
                }
                paragraph.add("})");
            }

        } else if (typeInfo.isList()) {
            if (isEntityType) {


                if (fetchType == null)
                    fetchType = "LAZY";
                else
                    fetchType = fetchType.toUpperCase();

                String extPropsTxt = "";
                if (field.hasAnnotation(EntityAnnotation.CASCADE)) {
                    String cascadeType = field.getAnnotationValue(EntityAnnotation.CASCADE);
                    if (cascadeType == null)
                        cascadeType = "ALL, orphanRemoval = true";
                    else
                        cascadeType = cascadeType.toUpperCase(Locale.ROOT);

                    extPropsTxt = ",cascade = CascadeType." + cascadeType;
                }

                String mappedByName = getMappedByName(field, ety);
                if (mappedByName != null) {
                    extPropsTxt = extPropsTxt + ",mappedBy=\"" + mappedByName + "\"";
                }

                String relationType = "OneToMany";
                if (field.hasAnnotation(EntityAnnotation.MANY_TO_MANY))
                    relationType = "ManyToMany";

                if (!field.hasAnnotation("jpa:onetomany")) {
                    paragraph.add("@%s(fetch = FetchType.%s %s)",relationType, fetchType, extPropsTxt);
                }
            }

        } else if (isEntityType) {


            String targetEntityName = typeInfo.getDefaultTypeName();
            Entity entity = this.publisher.getECoreModelContext().findModelComponent(targetEntityName, Entity.class);

            if (entity != null) {

                String cascade = "";

                String relationType = "ManyToOne";
                if (field.hasAnnotation(EntityAnnotation.ONE_TO_ONE)) {
                    cascade = getCascadeInfo(field); //only one to one
                    relationType = "OneToOne";
                }
                else if (field.hasAnnotation(EntityAnnotation.MANY_TO_MANY))
                    relationType = "ManyToMany";

                if(cascade.isEmpty()) {
                    if (fetchType != null) {
                        fetchType = fetchType.toUpperCase();
                        paragraph.add("@%s(fetch = FetchType.%s)", relationType, fetchType);


                    } else {
                        paragraph.add("@%s", relationType);
                    }
                }else {
                    if (fetchType != null) {
                        fetchType = fetchType.toUpperCase();
                        paragraph.add("@%s(fetch = FetchType.%s,%s)", relationType, fetchType,cascade);


                    } else {
                        paragraph.add("@%s(%s)", relationType,cascade);
                    }
                }




                //String joinFieldName = field.getName() + "_id";
                String joinFieldName = field.getAnnotationValue(EntityAnnotation.COLUMN_NAME);

                // for this case '@JoinColumn(name = "up_id", insertable = false, updatable = false)'
                String joinByFieldName = field.getAnnotationValue(EntityAnnotation.JOIN_BY);
                String extAttrForJoinBy = "";
                if(StringUtils.hasValue(joinByFieldName)) {
                    Field joinField = (Field) ety.getAllFieldListMap().get(joinByFieldName);
                    if(joinField!=null) {
                        joinFieldName = joinField.getDbColumnName();

                        extAttrForJoinBy = ", insertable = false, updatable = false";
                    }
                }

                if (StringUtils.hasValue(joinFieldName) && !field.hasAnnotation("jpa:joincolumn")) {
                    String nullable = "true";
                    if (field.hasAnnotation(EntityAnnotation.NOT_NULL)) {
                        nullable = "false";
                        paragraph.add(("@JoinColumn(name = \"" + joinFieldName + "\", nullable = " + nullable +extAttrForJoinBy+ ")"));
                    } else {
                        paragraph.add(("@JoinColumn(name = \"" + joinFieldName + "\" "+extAttrForJoinBy+")"));
                    }
                    hasColumnInfo = true;
                }

                // When no explicit @column(name) is specified but @notnull is present,
                // generate @JoinColumn(nullable = false) to enforce NOT NULL constraint
                if (!hasColumnInfo && field.hasAnnotation(EntityAnnotation.NOT_NULL)) {
                    paragraph.add("@JoinColumn(nullable = false)");
                    hasColumnInfo = true;
                }
            }

        }

        return hasColumnInfo;
    }

    private String makeLengthInfo(Field field) {
        if (!field.getTypeInfo().isStringType())
            return null;

        String fieldNm = field.getName();

        if (fieldNm.contains("Addr")) {
            return "256";
        } else if (fieldNm.contains("Type")) {
            return "10";
        } else if (fieldNm.contains("Name") || fieldNm.contains("Nm")) {
            return "64";
        } else if (fieldNm.contains("Code")) {
            return "32";
        } else if (fieldNm.contains("Status")) {
            return "10";
        } else if (fieldNm.contains("Date")) {
            // yyyy-MM-dd
            return "10";
        } else if (fieldNm.contains("Time")) {
            // HH:mm:ss
            return "8";
        } else if (fieldNm.contains("Zip")) {
            return "7";
        } else if (fieldNm.contains("Tel")) {
            return "15";
        }

        return null;
    }

    protected void setFieldLabel(Entity entity, Field field, CodeTemplate.Paragraph paragraph) {
        // A formula has no column to comment on; Hibernate fails on @Comment there.
        if (field.hasAnnotation(EntityAnnotation.FORMULA))
            return;
        String labelTxt = field.getAnnotationValue(EntityAnnotation.COMMENT);
        if (labelTxt != null && !labelTxt.isEmpty())
            paragraph.add(commentAnnotation(model) + "(\"%s\")", labelTxt);
    }

    /**
     * Hibernate's @Comment, by its simple name through the template's
     * org.hibernate.annotations.* import - unless the domain has an entity or
     * an enum named Comment. Its own package, or the enum package's wildcard,
     * then takes the simple name, and @Comment would not compile. A Comment
     * of another domain that an entity refers to is not looked for here.
     */
    private static String commentAnnotation(ECoreModel model) {
        boolean taken = model != null
                && ((model.getEntityModels() != null && model.getEntityModels().findByName("Comment") != null)
                || (model.getEnumModels() != null && model.getEnumModels().findByName("Comment") != null));
        return taken ? "@org.hibernate.annotations.Comment" : "@Comment";
    }

    /**
     * Sets JPA annotation information according to the configuration information of each field.
     *
     * @param field
     * @param paragraph
     */
    private void setFieldColumnAnnotation(Entity entity, Field field, CodeTemplate.Paragraph paragraph) {


        List<String> list = new ArrayList<>();
        List<String> list4Enum = new ArrayList<>(); // for Enumeration


        String dbColumnName = field.getDbColumnName();

        String formulaVal = field.getAnnotationValue(EntityAnnotation.FORMULA);

        // A formula is not a column. Hibernate reads @Column first and then
        // ignores @Formula, so a @Column here (from a length, for example)
        // would map a real column instead of the expression.
        if (formulaVal != null && formulaVal.length() > 0) {
            paragraph.add("@Formula(%s)", StringUtils.quoteString(formulaVal));
            return;
        }

        if (dbColumnName != null) {
            list.add("name = \"" + dbColumnName + "\"");
            list4Enum.add("name = \"" + dbColumnName + "\"");
        }


        String columnDefinition = field.getAnnotationValue("columndefinition", "def");
        // @columnDef('xxx') - generic DB column definition (e.g. jsonb, json, xml, vector(1536))
        // Annotation keys are stored lowercase, so the lookup uses "columndef".
        String columnDefAnnotValue = field.getAnnotationValue("columndef");

        if (field.getColumnDefinition() != null) {
            list.add("columnDefinition=\"" + field.getColumnDefinition() + "\"");
        } else if (columnDefinition != null) {
            list.add("columnDefinition = \"" + columnDefinition + "\"");
        } else if (columnDefAnnotValue != null && !columnDefAnnotValue.isEmpty()) {
            // Merge into the single @Column output; do not emit a separate @Column elsewhere.
            list.add("columnDefinition = \"" + columnDefAnnotValue + "\"");
        } else if (field.hasAnnotation("text")) {
            list.add("columnDefinition = \"TEXT\"");
        } else if (field.hasAnnotation("longtext")) {
            list.add("columnDefinition = \"LONGTEXT\"");
        }

        Entity entityModel = this.findEntityByField(field);

        if (entityModel==null && field.hasAnnotation("notnull")) {
            list.add("nullable = false");
            list4Enum.add("nullable = false");
        }

        if ("false".equals(field.getAnnotationValue(EntityAnnotation.UPDATABLE))) {
            list.add("updatable = false");
            list4Enum.add("updatable = false");
        }

        EnumModel enumModel = this.getEnumModelFromType(field);


        boolean isLengthDefine = false;
        String lengthVal = null;

        // Check for @length annotation (both simple and complex parameter formats)
        if (field.hasAnnotation(EntityAnnotation.LENGTH) && !field.getTypeInfo().isBigDecimal()) {
            // Only apply length annotation if it's not a BigDecimal type
            // BigDecimal types use precision/scale instead of length

            // Handle complex parameter format: @length(min=1, max=200) or @length(200)
            int maxLength = field.getMaxLength();
            if (maxLength > 0) {
                list.add("length = " + maxLength);
                list4Enum.add("length = " + maxLength);
                isLengthDefine = true;
            }
            else {
                // Fallback to original parsing for backward compatibility
                lengthVal = field.getAnnotationValue(EntityAnnotation.LENGTH);
                if (lengthVal != null) {
                    list.add("length = " + lengthVal);
                    list4Enum.add("length = " + lengthVal);
                    isLengthDefine = true;
                }
            }
        } else {
            if (!field.getTypeInfo().isList() && enumModel != null) {
                Annotation dbAnt = enumModel.getMetaInfo().getMetaAnnotation("db");
                if (dbAnt != null) {
                    String dbFieldNm = dbAnt.getValue();
                    if (dbFieldNm != null) {
                        Field enumField = enumModel.getFieldItems().findByName(dbFieldNm);
                        if (enumField != null) {
                            lengthVal = enumField.getAnnotationValue(EntityAnnotation.LENGTH);
                            if (lengthVal != null) {
                                list4Enum.add("length = " + lengthVal);
                                isLengthDefine = true;
                            }
                        }
                    }
                }

            }
        }

        // BigDecimal precision and scale support
        if (field.getTypeInfo().isBigDecimal() && field.getTypeInfo().hasPrecisionScale()) {
            list.add("precision = " + field.getTypeInfo().getPrecision());
            list.add("scale = " + field.getTypeInfo().getScale());
        } else {
            // Only apply length logic if not BigDecimal with precision/scale
            if (!isLengthDefine) {
                lengthVal = makeLengthInfo(field);
                if (lengthVal != null) {
                    list.add("length = " + lengthVal);
                }
            }
        }

        Annotation uniqAnnot = field.getAnnotation("unique");
        if (uniqAnnot != null && !"false".equals(uniqAnnot.getValue()))
            list.add("unique = true");

        //insertable=false, updatable=false
        if (field.hasAnnotation("discriminator")) {
            list.add("insertable = false");
            list.add("updatable = false");
        }


        //if (list.size() > 0 && !isEnumerationType(field)) {
        if (list.size() > 0) {
            String txt = "@Column(" + String.join(", ", list) + ")";
            paragraph.add(txt);
        } else if (list4Enum.size() > 0) {
            String txt = "@Column(" + String.join(", ", list4Enum) + ")";
            paragraph.add(txt);
        }
    }

    /**
     * Emits a Hibernate {@code @JdbcTypeCode(org.hibernate.type.SqlTypes.XXX)} annotation when
     * the field declares either {@code @jdbcType('XXX')} or a {@code @columnDef} value that
     * maps to a well-known JDBC type code.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>Explicit {@code @jdbcType} value wins.</li>
     *   <li>Otherwise, {@code @columnDef} is consulted via
     *       {@link #inferJdbcTypeFromColumnDef(String)} (json/jsonb -> JSON, xml -> SQLXML).</li>
     * </ol>
     *
     * <p>The fully-qualified {@code org.hibernate.type.SqlTypes.XXX} reference is emitted inline,
     * so no additional import is required (the entity template already wildcard-imports
     * {@code org.hibernate.annotations.*}).
     *
     * @param field     field whose annotations are inspected
     * @param paragraph paragraph to which the annotation line is appended
     */
    private void setJdbcTypeAnnotation(Field field, CodeTemplate.Paragraph paragraph) {
        if (field == null || paragraph == null) return;

        // Annotation map keys are lowercase, see Annotation constructor.
        String explicit = field.getAnnotationValue("jdbctype");
        String resolved = (explicit != null && !explicit.isEmpty()) ? explicit : null;

        if (resolved == null) {
            String columnDef = field.getAnnotationValue("columndef");
            if (columnDef != null && !columnDef.isEmpty()) {
                resolved = inferJdbcTypeFromColumnDef(columnDef);
            }
        }

        if (resolved != null) {
            paragraph.add("@JdbcTypeCode(org.hibernate.type.SqlTypes." + resolved + ")");
        }
    }

    /**
     * Infers a Hibernate {@code SqlTypes} constant name from a {@code @columnDef} value.
     * Returns {@code null} when no mapping exists; in that case the user must declare
     * {@code @jdbcType} explicitly to emit a {@code @JdbcTypeCode} annotation.
     *
     * @param columnDef raw column definition string (case insensitive)
     * @return SqlTypes constant name (e.g. "JSON", "SQLXML") or null when no auto mapping
     */
    private static String inferJdbcTypeFromColumnDef(String columnDef) {
        if (columnDef == null) return null;
        String key = columnDef.trim().toLowerCase(Locale.ROOT);
        if (key.isEmpty()) return null;

        // Well-known mappings. Add new entries here as the framework adopts more SqlTypes.
        if ("json".equals(key) || "jsonb".equals(key)) {
            return "JSON";
        }
        if ("xml".equals(key)) {
            return "SQLXML";
        }
        return null;
    }

    private void setEnumListAnnotation(Entity entity, Field field, CodeTemplate.Paragraph p) {
        TypeInfo typeInfo = field.getTypeInfo();
        if (!typeInfo.isList()) return;

        String enumName = typeInfo.getCoreItemType();
        EnumModel enumModel = this.model.getEnumModels().findByName(enumName);
        if (enumModel == null) return;

        String tblName = StringUtils.camelToSnake(entity.getIdentity().getName())
                + "_" + StringUtils.camelToSnake(field.getName());


        p.add("@ElementCollection( fetch = FetchType.EAGER)");
        p.add("@CollectionTable(name = \"%s\", joinColumns = @JoinColumn(name = \"map_seq\"))", tblName);
        p.add("@Column(name = \"code\")");
    }


    private void setJoinColumnAnnotation(Field field, CodeTemplate.Paragraph paragraph) {
        List<String> list = new ArrayList<>();

        if (field.hasAnnotation("join")) {
            String fieldNm = field.getAnnotation("join").getValue();
            list.add("columnDefinition=\"" + fieldNm + "\"");
        }

        if (list.size() > 0 && !field.hasAnnotation("jpa:joincolumn")) {
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


    private EnumModel getEnumModelFromType(Field field) {
        if (field.getTypeInfo().isBaseType()) return null;

        String typeName = field.getTypeInfo().getCoreItemType();

        return this.findEnumModel(typeName);
    }

    private void setConvertAnnotation(String domainId, Field field, CodeTemplate.Paragraph paragraph) {
        String type = field.getTypeInfo().getCoreItemType();

        EnumModel enumModel = getEnumModelFromType(field);
        if (enumModel != null) {
            if (enumModel.getMetaInfo().hasMetaAnnotation("db")) {
                paragraph.add("@Convert(converter = %s.EntityConverter.class)", type);
            }
        }
    }

    private void addEnumImports(Entity entity, CodeTemplate.Paragraph importList) {
        // Check all fields for enum references and add individual enum imports
        ModelComponentItems<Field> fields = entity.getItems();
        Set<String> enumClassNames = new HashSet<>();

        while (fields.hasNext()) {
            Field field = fields.next();
            String typeName = field.getTypeInfo().getCoreItemType();
            EnumModel enumModel = findEnumModel(typeName);
            if (enumModel != null) {
                String enumClassName = enumPackageName + "." + typeName;
                enumClassNames.add(enumClassName);
            }
        }

        // Add enum imports
        for (String enumClassName : enumClassNames) {
            importList.add(enumClassName);
        }
    }

    private void addEnumImportsToSet(Entity entity, Set<String> importSet) {
        // Check all fields for enum references and add individual enum imports to Set
        ModelComponentItems<Field> fields = entity.getItems();

        while (fields.hasNext()) {
            Field field = fields.next();
            String typeName = field.getTypeInfo().getCoreItemType();
            String fullNameWithNS = findFullNameWithNS(typeName);
            if(fullNameWithNS !=null) {
                importSet.add(fullNameWithNS);
            }
        }
    }

    private void addIndividualImportsForQ(ModelDomain domain, EntityModels entityModels, Set<String> importSet) {
        // Add individual entity and enum imports for Q class generation
        // NOTE: Do NOT import cross-domain Q classes - they will cause name conflict
        // Instead, use full qualified name in the code (e.g., com.example.domain.Q.$Entity)
        //
        // IMPORTANT: iterate allFieldListMap (which includes inherited fields from @extend parents),
        // NOT entity.getItems() (which returns only fields declared on the entity itself).
        // The Q.java code generator uses getAllFieldListMap() so inherited field types appear in
        // the generated code - their imports must be collected from the same source.
        ECoreModelContext ctx = BaseECoreModelContext.getContext();
        if (ctx == null) return;

        ModelComponentItems<Entity> entities = entityModels.getItems();

        while (entities.hasNext()) {
            Entity entity = entities.next();
            if (entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_ABSTRACT)) {
                continue; // Skip abstract entities
            }

            ListMap allFieldListMap = entity.getAllFieldListMap();
            if (allFieldListMap == null) continue;
            List<Field> fieldList = allFieldListMap.getList();
            if (fieldList == null) continue;

            for (Field field : fieldList) {
                if (field == null) continue;
                if (field.getTypeInfo().isBaseType()) continue;
                String typeName = field.getTypeInfo().getCoreItemType();
                if (typeName == null || typeName.isEmpty()) continue;

                // Direct cross-domain lookup (bypasses ECoreModel.findModelByName's static redirect)
                String fullNameWithNS = resolveTypeFqcnAcrossDomains(ctx, typeName);
                if (fullNameWithNS == null) continue;

                int lastDotIndex = fullNameWithNS.lastIndexOf('.');
                if (lastDotIndex <= 0) continue;

                String classPackage = fullNameWithNS.substring(0, lastDotIndex);
                // Skip import if same package as the Q class
                if (classPackage.equals(packageName)) continue;

                importSet.add(fullNameWithNS);
            }
        }
    }

    /**
     * Searches every loaded domain directly for the given simple type name and returns its
     * fully-qualified class name. Bypasses ECoreModel.findModelByName(), which re-routes via
     * the static context and can miss cross-module domains in multi-module setups.
     */
    private String resolveTypeFqcnAcrossDomains(ECoreModelContext ctx, String typeName) {
        if (ctx == null || typeName == null) return null;

        String[] allDomains = ctx.getAllDomainNames();
        if (allDomains == null) return null;

        for (String domainNm : allDomains) {
            DataModelComponent found = lookupInDomain(ctx, domainNm, typeName);
            if (found == null) continue;
            String simpleName = found.getIdentity().getName();
            String fqcn = resolveFqcn(ctx, domainNm, found, simpleName);
            if (fqcn != null && fqcn.indexOf('.') > 0) {
                return fqcn;
            }
        }
        return null;
    }

    private void addEnumImportsForQ(Set<String> importSet) {
        // Add enum imports for Q class generation - check all entities in the domain
        if (enumPackageName == null) return;

        EntityModels entityModels = this.model.getEntityModels();
        ModelComponentItems<Entity> entities = entityModels.getItems();

        while (entities.hasNext()) {
            Entity entity = entities.next();
            if (entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_ABSTRACT)) {
                continue; // Skip abstract entities
            }

            ModelComponentItems<Field> fields = entity.getItems();
            while (fields.hasNext()) {
                Field field = fields.next();
                String typeName = field.getTypeInfo().getCoreItemType();
                String fullNameWithNS = findFullNameWithNS(typeName);
                if(fullNameWithNS !=null) {
                    importSet.add(fullNameWithNS);
                }
            }
        }
    }

    private void setNativeAnnotation(Map<String, Annotation> annotationMap, CodeTemplate.Paragraph paragraph) {

        if (this.publishMode == null) return;
        if (annotationMap == null) return;

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
