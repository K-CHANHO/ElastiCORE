package io.elasticore.base.model.pub.mongodb;

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
import io.elasticore.base.model.pub.jpa.SrcFilePublisher;
import io.elasticore.base.model.relation.ModelRelationship;
import io.elasticore.base.model.relation.RelationType;
import io.elasticore.base.util.CodeTemplate;
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

;


    public ControlSrcPublisher(CodePublisher publisher) {
        super(publisher);

        this.model = publisher.getECoreModelContext().getDomain().getModel();

        if (model.getEntityModels().getItems().size() == 0 || model.getDataTransferModels().getItems().size() == 0)
            return;

        this.relationshipManager = RelationshipManager.getInstance(publisher.getECoreModelContext().getDomain().getName());

        this.packageName = model.getNamespace(ConstantParam.KEYNAME_CONTROL);

        if(this.packageName==null) return;


        this.dtoPackageName = model.getNamespace(ConstantParam.KEYNAME_DTO);
        this.servicePackageName = model.getNamespace(ConstantParam.KEYNAME_SERVICE);

        this.publisher = publisher;

        this.baseCodeTmpl = createCodeTemplate(publisher, "template.control", "control_mongo.tmpl");

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
            DataTransfer dto = findDTO(entity);
            if (dto == null) continue;

            DataTransfer searchDTO = findSearchDTO(entity);
            if (searchDTO == null) continue;


            publish(domain, entity, dto, searchDTO);
        }
    }


    public void publish(ModelDomain domain, Entity entity, DataTransfer dto , DataTransfer searchDto) {

        String entityClassName = entity.getIdentity().getName();
        String className = entityClassName+ ConstantParam.POSTFIX_CONTROL;
        String entityName = StringUtils.uncapitalize(entityClassName);
        String serviceClassName = entityClassName+ ConstantParam.POSTFIX_SERVICE;

        String domainName = domain.getName();
        String dtoClassName = dto.getIdentity().getName();
        String mapperName = findMapperClassName(dto);
        String searchDTOClassName = searchDto.getIdentity().getName();
        String pkType = entity.getPkField().getType();
        String searchReturnType = "List";
        if(isPageable(searchDto)) {
            searchReturnType = "Page";
        }

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

        CodeTemplate.Parameters params = CodeTemplate.newParameters();
        params
                .set("className",className)
                .set("domainName", domainName)

                .set("entityLabel", entityLabel)
                .set("entityDesc", entityDesc)

                .set("entityName", entityName)
                .set("serviceClassName",serviceClassName)
                .set("entityClassName",entityClassName)
                .set("dtoClassName",dtoClassName)
                .set("mapperName",mapperName)
                .set("searchDTOClassName", searchDTOClassName)
                .set("pkType", pkType)
                .set("searchReturnType", searchReturnType)

                .set("searchResultDTOClassName" ,searchResultDTOClassName)



                .set("dtoPackageName", dtoPackageName)
                .set("servicePackageName", servicePackageName)
                .set("packageName", packageName);

        String code = baseCodeTmpl.toString(params);


        String qualifiedClassName = packageName + "." + className;

        this.writeSrcCode(this.publisher, null, qualifiedClassName, code);
    }

}
