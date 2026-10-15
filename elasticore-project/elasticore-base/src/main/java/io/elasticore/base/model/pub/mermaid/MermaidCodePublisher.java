package io.elasticore.base.model.pub.mermaid;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ECoreModelContext;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.SourceFileAccessFactory;
import io.elasticore.base.model.*;
import io.elasticore.base.model.core.RelationshipManager;
import io.elasticore.base.model.entity.EntityAnnotation;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.model.entity.EntityModels;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.enums.EnumModel;
import io.elasticore.base.model.enums.EnumModels;
import io.elasticore.base.model.relation.ModelRelationship;
import io.elasticore.base.model.relation.RelationType;
import io.elasticore.base.util.CodeStringBuilder;
import io.elasticore.base.util.CodeTemplate;
import io.elasticore.base.util.ConsoleLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MermaidCodePublisher implements CodePublisher {

    private ECoreModelContext ctx;

    private SourceFileAccessFactory sourceFileAccessFactory;


    private MermaidCodePublisher() {

    }

    @Override
    public void setSrcCodeWriterFactory(SourceFileAccessFactory sourceFileAccessFactory) {
        this.sourceFileAccessFactory = sourceFileAccessFactory;
    }

    @Override
    public SourceFileAccessFactory getSrcFileAccessFactory() {
        return this.sourceFileAccessFactory;
    }


    @Override
    public void errorOnPublish(ModelComponent modelComponent, Throwable e) {

        // TODO
        e.printStackTrace();
    }

    public static MermaidCodePublisher newInstance() {
        return new MermaidCodePublisher();
    }


    @Override
    public ECoreModelContext getECoreModelContext() {
        return this.ctx;
    }


    @Override
    public boolean deleteSource(ModelDomain domain, String rootDir) {
        return false;
    }

    public void publish(ECoreModelContext ctx, ModelDomain domain) {

        // TODO
        // deprecated this class
        if(domain!=null) return;

        this.ctx = ctx;
        ECoreModel model = domain.getModel();
        EntityModels entityModels = model.getEntityModels();
        ModelComponentItems<Entity> items = entityModels.getItems();

        CodeStringBuilder cb = new CodeStringBuilder("{", "}");
        cb.line("classDiagram").block("");

        Set<String> relationModelNames = new HashSet<>();
        Set<String> entityModelNames = new HashSet<>();

        for (int i = 0; i < items.size(); i++) {
            Entity entity = items.get(i);
            precheckRelationship(entity, relationModelNames ,entityModelNames);
        }

        for(String entityName: relationModelNames) {

            if(entityModelNames.contains(entityName)) {
                continue;
            }
            DataModelComponent modelComponent = this.ctx.findModelComponent(entityName, Entity.class);
            if(modelComponent!=null) {
                makeClassInfoScript((Entity) modelComponent, cb );

            }
        }

        // entity
        for (int i = 0; i < items.size(); i++) {
            Entity entity = items.get(i);
            String entityNm = entity.getIdentity().getName();

            makeClassInfoScript(entity, cb );
        }
        cb.end("");


        String templatePath = domain.getModel().getConfig("template.uml");
        if (templatePath == null)
            templatePath = "elasticore-template/mermaid/uml_html.tmpl";
        else
            templatePath = "resource://"+templatePath;

        //String templatePath = "elasticore-template/mermaid/uml_html.tmpl";
        CodeTemplate umlHtmlTmpl = CodeTemplate.newInstance(templatePath);

        CodeTemplate.Parameters params = CodeTemplate.newParameters();
        params.set("umlTxt", cb.toString());

        String content = umlHtmlTmpl.toString(params);


        String fileName = "uml_" + ctx.getDomain().getName() + ".html";
        File umlFile = new File(fileName);
        ConsoleLog.printInfo("UML file name : " + umlFile.getAbsolutePath());

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(umlFile), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(content);

            writer.flush();
        } catch (Throwable e) {
            e.printStackTrace();

        }


    }


    private void makeUMLFile(String umlTxt) {

    }

    private void precheckRelationship(Entity entity , Set<String> relationModelSet, Set<String> entityModelNames) {
        String classNm = entity.getIdentity().getName();
        entityModelNames.add(classNm);


        List<ModelRelationship> list = RelationshipManager.getInstance(entity.getIdentity().getDomainId()).findByFromName(classNm);

        ECoreModel model = ctx.getDomain().getModel();

        EntityModels entityModels = model.getEntityModels();
        EnumModels enumModels = model.getEnumModels();


        for (ModelRelationship r : list) {

            if (enumModels != null && enumModels.findByName(r.getToName()) != null)
                continue;

            if (r.getToName().indexOf("PK:") >= 0)
                continue;

            if (r.getFromName().indexOf("PK:") >= 0)
                continue;

            if(this.ctx.findModelComponent(r.getToName(), Entity.class) ==null)
                continue;

            if(this.ctx.findModelComponent(r.getFromName(), Entity.class) ==null)
                continue;

            relationModelSet.add(r.getFromName());
            relationModelSet.add(r.getToName());
        }
    }

    private void makeClassInfoScript(Entity entity, CodeStringBuilder cb) {

        StringBuilder sb = new StringBuilder();

        String classNm = entity.getIdentity().getName();

        Entity refEntity = this.getECoreModelContext().findModelComponent(classNm, Entity.class);
        if(refEntity==null) {
            //ConsoleLog.printWarn(" UML : NOT FOUND CLASS: ["+classNm+"]");
            return;
        }

        /*
        ShadowModel shadowModel = this.getECoreModelContext().getDomain(refEntity.getIdentity().getDomainId()).getModel()
                .getShadowModel(classNm);

        if(shadowModel==null) {
            //ConsoleLog.printWarn(" UML : NOT FOUND CLASS: "+classNm+" from "+refEntity.getIdentity().getDomainId());
            return;
        }

         */


        if( entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_EMBEDDABLE) ) {
            cb.line("class %s:::embeddable", classNm).block();
            cb.line("&lt;&lt;embeddable &gt;&gt;");
        }
        else if( entity.getMetaInfo().hasMetaAnnotation(EntityAnnotation.META_ABSTRACT) ) {
            cb.line("class %s:::abstract", classNm).block();
            cb.line("&lt;&lt;Abstract &gt;&gt;");
        }else{
            cb.line("class %s", classNm).block();
        }



        //List<Field> fieldList = shadowModel.listFields();
        ModelComponentItems<Field> items = refEntity.getItems();
        List<Field> fieldList = new ArrayList<>();
        while (items.hasNext()) {
            Field next = items.next();
            fieldList.add(next);
        }



        ModelComponentItems<Field> fields = entity.getItems();
        //while (fields.hasNext()) {
        //Field f = fields.next();

        for(Field f:fieldList) {

            String type = f.getTypeInfo().getDefaultTypeName();
            if(f.getTypeInfo().isList()) {

                type = f.getTypeInfo().getCoreItemType()+"[]";
            }
            String fieldName = f.getName();

            String label = f.getAnnotationValue(EntityAnnotation.COMMENT);
            if(label !=null && !label.isEmpty() ) {
                label = "//"+label;
            }else {
                label ="";
            }

            if(f.isPrimaryKey())
                type = type+"[PK]";

            cb.line("%s: %s %s", fieldName, type,label);
        }

        cb.end();


        List<ModelRelationship> list = RelationshipManager.getInstance(entity.getIdentity().getDomainId()).findByFromName(classNm);

        ECoreModel model = ctx.getDomain().getModel();

        EntityModels entityModels = model.getEntityModels();
        EnumModels enumModels = model.getEnumModels();


        for (ModelRelationship r : list) {

            if( enumModels !=null && enumModels.findByName(r.getToName()) !=null)
                continue;

            String toName = r.getToName();
            String fromName = r.getFromName();

            if(toName.indexOf("PK:")>=0)
                continue;

            if(fromName.indexOf("PK:")>=0)
                continue;

            String[] toItems = toName.split(":");
            String[] fromItems = fromName.split(":");

            if(toItems.length==2)
                toName = toItems[1];
            if(fromItems.length==2)
                fromName = fromItems[1];

            if(this.ctx.findModelComponent(toName, Entity.class) ==null)
                continue;

            if(this.ctx.findModelComponent(fromName, Entity.class) ==null)
                continue;

            if (r.getRelationType() == RelationType.MANY_TO_ONE)
                cb.line("%s --o \"0..*\" %s : %s", fromName, toName, r.getRelationName());

            else if (r.getRelationType() == RelationType.ONE_TO_ONE)
                cb.line("%s --o \"0..1\" %s : %s", fromName, toName, r.getRelationName());

            else if (r.getRelationType() == RelationType.SUPER)
                cb.line("%s <|-- %s : %s",  toName, fromName, "extend");

            else if (r.getRelationType() == RelationType.ROLLUP)
                cb.line("%s <|-- %s : %s", toName, fromName,  r.getRelationType().getName());

            else if (r.getRelationType() == RelationType.EMBEDDED)
                cb.line("%s --* %s : %s",  fromName, toName,  r.getRelationName()+"(embedded)");

            else if (r.getRelationType() == RelationType.AOSSOCIATION)
                cb.line("%s --> \"0..*\" %s : %s", toName, fromName, r.getRelationName());
        }


    }
}
