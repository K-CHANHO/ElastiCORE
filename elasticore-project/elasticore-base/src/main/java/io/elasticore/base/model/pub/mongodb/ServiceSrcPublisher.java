package io.elasticore.base.model.pub.mongodb;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.ConstantParam;
import io.elasticore.base.model.DataModelComponent;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.core.ListMap;
import io.elasticore.base.model.core.RelationshipManager;
import io.elasticore.base.model.dto.DataTransfer;
import io.elasticore.base.model.dto.DataTransferAnnotation;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.model.entity.EntityAnnotation;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.pub.jpa.SrcFilePublisher;
import io.elasticore.base.model.relation.ModelRelationship;
import io.elasticore.base.model.relation.RelationType;
import io.elasticore.base.util.CodeStringBuilder;
import io.elasticore.base.util.CodeTemplate;
import io.elasticore.base.util.StringUtils;

import java.util.List;
import java.util.Properties;

public class ServiceSrcPublisher extends SrcFilePublisher {

    private CodeTemplate baseCodeTmpl;


    private CodePublisher publisher;
    private ECoreModel model;
    private RelationshipManager relationshipManager;


    private String packageName;
    private String entityPackageName;
    private String dtoPackageName;
    private String repoPackageName;

    ;


    public ServiceSrcPublisher(CodePublisher publisher) {
        super(publisher);

        this.model = publisher.getECoreModelContext().getDomain().getModel();

        if (model.getEntityModels().getItems().size() == 0 || model.getDataTransferModels().getItems().size() == 0)
            return;

        this.relationshipManager = RelationshipManager.getInstance(publisher.getECoreModelContext().getDomain().getName());

        this.packageName = model.getNamespace(ConstantParam.KEYNAME_SERVICE);

        if (this.packageName == null) return;

        this.entityPackageName = model.getNamespace(ConstantParam.KEYNAME_ENTITY);
        this.dtoPackageName = model.getNamespace(ConstantParam.KEYNAME_DTO);
        this.repoPackageName = model.getNamespace(ConstantParam.KEYNAME_REPOSITORY);

        this.publisher = publisher;

        this.baseCodeTmpl = createCodeTemplate(publisher, "template.service", "service_mongo.tmpl");

        //System.err.println(this.baseCodeTmpl);

    }


    private boolean isPageable(DataTransfer searchDTO) {
        Annotation annotation = searchDTO.getMetaInfo().getMetaAnnotation(DataTransferAnnotation.META_SEARCHABLE);
        if (annotation == null) return false;

        Properties props = annotation.getProperties();
        if (props == null) return false;
        if (props.getProperty("pageSize") == null) return false;

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

        if (this.baseCodeTmpl == null) return;

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

    private String makeGetFieldInfo(String searchResultDtoNm ) {
        StringBuilder sb = new StringBuilder();

        DataTransfer dto = this.model.getDataTransferModels().findByName(searchResultDtoNm);
        if(dto ==null) return sb.toString();

        ListMap<String, Field>  listMap = dto.getAllFieldListMap();

        //root.get("comSeq")
        for(Field f:listMap.getList()) {
            if(f.hasAnnotation(EntityAnnotation.META_DISABLE))
                continue;
            String coreItemType = f.getTypeInfo().getCoreItemType();
            if(this.findEntityByName(coreItemType)!=null) continue;

            if(sb.length()>0) sb.append("\n ,");
            sb.append("root.get(");
            sb.append(StringUtils.quoteString(f.getName()));
            sb.append(")");

        }

        return sb.toString();
    }



    public void publish(ModelDomain domain, Entity entity, DataTransfer dto, DataTransfer searchDto) {

        String domainName = domain.getName();
        String entityClassName = entity.getIdentity().getName();
        String className = entityClassName + ConstantParam.POSTFIX_SERVICE;

        String dtoClassName = dto.getIdentity().getName();
        String mapperName = findMapperClassName(dto);
        String searchDTOClassName = searchDto.getIdentity().getName();
        String pkType = entity.getPkField().getType();
        String pkName = entity.getPkField().getName();

        CodeStringBuilder cb = new CodeStringBuilder("{", "}");
        makeChildRefCode(dto, entity, cb);

        boolean isListOutput = false;
        boolean isPageOutput = false;

        if (isPageable(searchDto)) {
            isPageOutput = true;
        }else {
            isListOutput = true;
        }

        boolean isCustomPageOutput = false;
        boolean isCustomListOutput = false;
        String selectColumnNmList = "";

        String toSearchListMappingName = "toDTO";

        String searchResultDTOClassName = findSearchResultDTOName(this.relationshipManager, entity);
        if(searchResultDTOClassName!=null) {
            toSearchListMappingName = "to"+searchResultDTOClassName;
            /*
            if(isPageOutput) {
                isCustomPageOutput = true;
                isPageOutput = false;
            }else {
                isCustomListOutput = true;
                isListOutput = false;
            }

            selectColumnNmList = makeGetFieldInfo(searchResultDTOClassName);
             */
        }else {
            searchResultDTOClassName=dtoClassName;
        }

        boolean isSkipNull = true;
        if( "false".equals(entity.getMetaInfo().getMetaAnnotationValue("dynamicupdate"))) {
            isSkipNull = false;
        }


        CodeTemplate.Parameters params = CodeTemplate.newParameters();
        params
                .set("className", className)
                .set("domainName", StringUtils.capitalize(domainName))
                .set("entityClassName", entityClassName)
                .set("dtoClassName", dtoClassName)
                .set("mapperName", mapperName)
                .set("searchDTOClassName", searchDTOClassName)
                .set("pkType", pkType)
                .set("pkName", StringUtils.capitalize(pkName))
                .set("childRefInfo", cb.toString())
                .set("isListOutput",isListOutput)
                .set("isPageOutput",isPageOutput)
                .set("isCustomPageOutput",isCustomPageOutput)
                .set("isCustomListOutput",isCustomListOutput)
                .set("isSkipNull",isSkipNull)


                .set("searchResultDTOClassName",searchResultDTOClassName)
                .set("selectColumnNmList" ,selectColumnNmList)
                .set("toSearchListMappingName", toSearchListMappingName)



                .set("j2eePkgName",getPersistentPackageName(domain))
                .set("entityPackageName", entityPackageName)
                .set("dtoPackageName", dtoPackageName)
                .set("repositoryPackageName", repoPackageName)
                .set("packageName", packageName);

        String code = baseCodeTmpl.toString(params);

        String qualifiedClassName = packageName + "." + className;

        this.writeSrcCode(this.publisher, null, qualifiedClassName, code);
    }

    private void makeChildRefCode(DataTransfer dto, Entity entity, CodeStringBuilder cb) {


        cb.block("");
        cb.block("");


        ModelComponentItems<Field> items = dto.getItems();


        while (items.hasNext()) {
            Field f = items.next();
            // ex) Company.comSeq  || Company
            String refFieldNm = f.getAnnotationValue(EntityAnnotation.REFERENCE);
            if (refFieldNm == null) continue;

            String[] refItems = refFieldNm.split("\\.");
            String refClassPkNm = null;
            if (refItems.length == 2) {
                refClassPkNm = refItems[1];
                refFieldNm = refItems[0];
            }

            Field targetField = entity.getItems().findByName(refFieldNm);
            if (targetField == null) continue;
            ;

            String getTypeNm = targetField.getTypeInfo().getDefaultTypeName();

            Entity targetEntity = this.findEntityByName(getTypeNm);
            if (targetEntity == null) continue;


            String refClassPkNm2 = targetEntity.getPkField().getName();


            if (refClassPkNm == null || !refClassPkNm.equals(refClassPkNm2)) {
                continue;
            }

            String extraDtoFieldName = "";
            if (!f.getTypeInfo().isBaseType()) {
                extraDtoFieldName = ".get" + StringUtils.capitalize(refClassPkNm2) + "()";
            }

            String dtoGetName = StringUtils.capitalize(f.getName());

            String getDtoFieldName = StringUtils.capitalize(f.getName());

            String fieldInfo = "";
            String entitySetName = findFieldNameByTypeName(entity, refFieldNm);


            cb.line("if(dto.get%s()!=null)", getDtoFieldName);
            cb.block();
            cb.line("%s item = helper.get%s().findById(dto.get%s()%s).orElse(null);"
                    , getTypeNm, getTypeNm, dtoGetName, extraDtoFieldName);
            cb.line("if(item!=null) entity.set%s(item);", StringUtils.capitalize(refFieldNm));

            cb.end();

            //System.err.println(cb.toString());

        }

        cb.end("");
        cb.end("");
    }


}
