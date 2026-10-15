package io.elasticore.base.model.pub.dsl;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ECoreModelContext;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.SourceFileAccessFactory;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.ModelComponent;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.model.entity.EntityModels;
import io.elasticore.base.util.ConsoleLog;

public class DslCodePublihser implements CodePublisher {

    private ECoreModelContext ctx;

    private SourceFileAccessFactory sourceFileAccessFactory;

    @Override
    public void setSrcCodeWriterFactory(SourceFileAccessFactory sourceFileAccessFactory) {

        this.sourceFileAccessFactory = sourceFileAccessFactory;
    }

    @Override
    public SourceFileAccessFactory getSrcFileAccessFactory() {
        return this.sourceFileAccessFactory;
    }

    @Override
    public ECoreModelContext getECoreModelContext() {
        return this.ctx;
    }


    @Override
    public boolean deleteSource(ModelDomain domain, String rootDir) {
        return false;
    }

    @Override
    public void publish(ECoreModelContext ctx, ModelDomain domain) {
        this.ctx = ctx;

        ECoreModel model = domain.getModel();

        EntityModels entityModels = model.getEntityModels();

        ModelComponentItems<Entity> items = entityModels.getItems();


        DslcodeFilePublisher publisher = new DslcodeFilePublisher(this);
        publisher.publish(domain);


        ConsoleLog.clear();

    }

    @Override
    public void errorOnPublish(ModelComponent modelComponent, Throwable e) {

    }
}
