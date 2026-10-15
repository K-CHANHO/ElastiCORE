package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.ConstantParam;
import io.elasticore.base.model.DataModelComponent;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.core.RelationshipManager;
import io.elasticore.base.model.dto.DataTransfer;
import io.elasticore.base.model.dto.DataTransferAnnotation;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.model.entity.EntityAnnotation;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.entity.PkField;
import io.elasticore.base.model.relation.ModelRelationship;
import io.elasticore.base.model.relation.RelationType;
import io.elasticore.base.util.CodeTemplate;
import io.elasticore.base.util.ConsoleLog;
import io.elasticore.base.util.StringUtils;

import java.util.List;
import java.util.Properties;

public class ControlSrcPublisher extends SrcFilePublisher {

    private CodeTemplate baseCodeTmpl;



    private CodePublisher publisher;
    private ECoreModel model;
    private RelationshipManager relationshipManager;



    private String packageName;

    private String dtoPackageName;
    private String servicePackageName;

    private boolean isDefaultGen = true;


    public ControlSrcPublisher(CodePublisher publisher) {
        super(publisher);

        String config = publisher.getECoreModelContext().getDomain().getModel().getConfig("defaultGeneration.api", "true");
        if("false".equals(config))
            isDefaultGen = false;

        this.model = publisher.getECoreModelContext().getDomain().getModel();

        if (model.getEntityModels().getItems().size() == 0 || model.getDataTransferModels().getItems().size() == 0)
            return;

        this.relationshipManager = RelationshipManager.getInstance(publisher.getECoreModelContext().getDomain().getName());

        this.packageName = model.getNamespace(ConstantParam.KEYNAME_CONTROL);

        if(this.packageName==null) return;


        this.dtoPackageName = model.getNamespace(ConstantParam.KEYNAME_DTO);
        this.servicePackageName = model.getNamespace(ConstantParam.KEYNAME_SERVICE);

        this.publisher = publisher;

        this.baseCodeTmpl = createCodeTemplate(publisher, "template.control", "control.tmpl");

    }


    private boolean isPageable(DataTransfer searchDTO) {
        Annotation annotation = searchDTO.getMetaInfo().getMetaAnnotation(DataTransferAnnotation.META_SEARCHABLE);
        if(annotation==null) return false;

        Properties props = annotation.getProperties();
        if(props==null) return false;
        if( props.getProperty("pageSize") ==null) return false;

        return true;

    }



    private DataTransfer findSearchDTO(Entity entity) {
        String entityNm = entity.getIdentity().getName();
        List<ModelRelationship> relationshipList = relationshipManager
                .findByToNameAndType(entityNm, RelationType.SEARCHABLE);

        for (ModelRelationship r : relationshipList) {
            String dtoName = r.getFromName();

            return this.model.getDataTransferModels().findByName(dtoName);
        }

        return null;
    }

    private String findMapperClassName(DataModelComponent model) {
        List<ModelRelationship> relationshipList = relationshipManager
                .findByFromNameAndType(model.getIdentity().getName(), RelationType.MAPPER);
        for (ModelRelationship r : relationshipList) {
            return r.getToName();
        }
        return null;
    }

    /**
     * @param domain
     */
    public void publish(ModelDomain domain) {

        if(this.baseCodeTmpl==null) return;

        ModelComponentItems<Entity> entities = this.model.getEntityModels().getItems();



        while (entities.hasNext()) {
            Entity entity = entities.next();

            // Runtime-only DSL entities (@runtime) are never code-generated.
            if (entity.getMetaInfo() != null && entity.getMetaInfo().hasMetaAnnotation("runtime")) {
                continue;
            }

            DataTransfer dto = findDTO(entity);
            if (dto == null) continue;

            DataTransfer searchDTO = findSearchDTO(entity);
            if (searchDTO == null) continue;


            publish(domain, entity, dto, searchDTO);
        }
    }


    public void publish(ModelDomain domain, Entity entity, DataTransfer dto , DataTransfer searchDto) {

        // Skip controller generation for @service entities
        if(entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_SERVICE)) {
            return;
        }

        //this.isDefaultGen
        boolean hasApiAnt = entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_API);
        String isGen = entity.getMetaInfo().getInfoAnnotationValue(EntityAnnotation.META_API);

        if("false".equals(isGen) || (!hasApiAnt && !this.isDefaultGen))
            return;

        if(entity.getPkField()==null)
            return;

        String entityClassName = entity.getIdentity().getName();
        String className = entityClassName+ ConstantParam.POSTFIX_CONTROL;
        String entityName = StringUtils.uncapitalize(entityClassName);
        String serviceClassName = entityClassName+ ConstantParam.POSTFIX_SERVICE;

        if(entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_EXPOSE)) {
            serviceClassName = entityClassName+ ConstantParam.POSTFIX_EXT_SERVICE;
        }

        String keyDtoClassName = entityClassName+"Key"+"DTO";

        String domainName = domain.getName();
        String dtoClassName = dto.getIdentity().getName();
        String mapperName = findMapperClassName(dto);
        String searchDTOClassName = searchDto.getIdentity().getName();
        PkField pkField = entity.getPkField();

        boolean isSinglePrimitivePk = false;
        String singlePrimitivePkType = "";
        if(pkField!=null) {
            if(!pkField.isMultiple()) {
                isSinglePrimitivePk = true;
                singlePrimitivePkType = pkField.getType();
            }
        }
        String pkType = ( entity.getPkField()!=null)?entity.getPkField().getType():"";
        String searchReturnType = "List";
        if(isPageable(searchDto)) {
            searchReturnType = "Page";
        }

        // Read apiPath configuration with default value "/api"
        String apiPath = this.model.getConfig("apiPath", "/api");

        String entityLabel = entity.getMetaInfo().getMetaAnnotationValue("label");
        if(entityLabel ==null)
            entityLabel = entityName;

        String entityDesc = entity.getMetaInfo().getMetaAnnotationValue("description", "desc");
        if(entityDesc ==null)
            entityDesc = entityLabel;


        String searchResultDTOClassName = findSearchResultDTOName(this.relationshipManager, entity);
        if(searchResultDTOClassName==null) {
            searchResultDTOClassName=dtoClassName;
        }

        /**
         * @DeleteMapping("/{id}")
         *     public ResponseEntity<Void> delete(@PathVariable("id") ${pkType} id) {
         *         if (!${entityName}Service.findById(id).isPresent()) {
         */
        ////////////////////////
        String pkPathInfo = null;
        String pkParamInfo = null;
        String pkFieldInfo = null;

        StringBuilder sb = new StringBuilder(); // for path
        StringBuilder sb2 = new StringBuilder(); // for field
        StringBuilder sb3 = new StringBuilder(); // for declare
        ModelComponentItems<Field> items = entity.getItems();

        List<Field> fieldList = entity.getAllFieldListMap().getList();

        for(Field field: fieldList) {
            if(!field.isPrimaryKey()) continue;
            if(sb.length()>0) {

                sb2.append(" ,");
                sb3.append(" ,");
            }

            // dto.get${pkName}()
            sb.append("/{").append(field.getName()).append("}");
            sb2.append("@PathVariable(\"").append(field.getName()).append("\") ").append(field.getTypeInfo().getDefaultTypeName()).append(" ").append(field.getName());
            sb3.append(field.getName());
        }

        pkPathInfo = sb.toString();
        pkParamInfo = sb2.toString();
        pkFieldInfo = sb3.toString();

        String j2eePkgNm = getPersistentPackageName(domain);


        CodeTemplate.Parameters params = CodeTemplate.newParameters();
        params
                .set("className",className)
                .set("domainName", domainName)
                .set("apiPath", apiPath)

                .set("entityLabel", entityLabel)
                .set("entityDesc", entityDesc)
                .set("j2eePkgName",j2eePkgNm)

                .set("entityName", entityName)
                .set("serviceClassName",serviceClassName)
                .set("entityClassName",entityClassName)
                .set("dtoClassName",dtoClassName)
                .set("keyDtoClassName",keyDtoClassName)
                .set("mapperName",mapperName)
                .set("searchDTOClassName", searchDTOClassName)
                .set("pkType", pkType)
                .set("isSinglePrimitivePk", isSinglePrimitivePk)
                .set("singlePrimitivePkType",singlePrimitivePkType)

                .set("pkPathInfo", pkPathInfo)
                .set("pkParamInfo", pkParamInfo)
                .set("pkFieldInfo", pkFieldInfo)

                .set("searchReturnType", searchReturnType)

                .set("searchResultDTOClassName" ,searchResultDTOClassName)



                .set("dtoPackageName", dtoPackageName)
                .set("servicePackageName", servicePackageName)
                .set("packageName", packageName);

        // JsonView support: resolve mode and set template parameters
        String jsonViewMode = resolveJsonViewMode(dto);
        boolean hasJsonView = !"false".equals(jsonViewMode);

        String jsonViewImport = "";
        String createMethodJsonView = "";
        String createParamJsonView = "";
        String updateMethodJsonView = "";
        String updateParamJsonView = "";
        String readMethodJsonView = "";
        String searchMethodJsonView = "";
        String batchParamJsonView = "";

        if (hasJsonView) {
            jsonViewImport = String.format(
                "\nimport com.fasterxml.jackson.annotation.JsonView;\nimport %s.Views;",
                dtoPackageName
            );
            createMethodJsonView = "    @JsonView(Views.Read.class)\n";
            createParamJsonView = "@JsonView(Views.Create.class) ";
            updateMethodJsonView = "    @JsonView(Views.Read.class)\n";
            updateParamJsonView = "@JsonView(Views.Update.class) ";
            readMethodJsonView = "    @JsonView(Views.Read.class)\n";
            searchMethodJsonView = "    @JsonView(Views.Read.class)\n";
            batchParamJsonView = "@JsonView(Views.Update.class) ";

        }

        // ALWAYS set all JsonView parameters (empty string when disabled)
        params
                .set("jsonViewImport", jsonViewImport)
                .set("createMethodJsonView", createMethodJsonView)
                .set("createParamJsonView", createParamJsonView)
                .set("updateMethodJsonView", updateMethodJsonView)
                .set("updateParamJsonView", updateParamJsonView)
                .set("readMethodJsonView", readMethodJsonView)
                .set("searchMethodJsonView", searchMethodJsonView)
                .set("batchParamJsonView", batchParamJsonView);

        String code = baseCodeTmpl.toString(params);


        String qualifiedClassName = packageName + "." + className;

        this.writeSrcCode(this.publisher, null, qualifiedClassName, code);
    }

}
