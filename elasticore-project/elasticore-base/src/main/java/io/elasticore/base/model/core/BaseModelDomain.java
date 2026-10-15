package io.elasticore.base.model.core;

import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.dto.DataTransferModels;
import io.elasticore.base.model.entity.EntityModels;
import io.elasticore.base.model.enums.EnumModels;

import java.util.HashMap;
import java.util.Map;

public final class BaseModelDomain implements ModelDomain {

    private final String name;
    private final ECoreModel model;

    private static Map<String, ModelDomain> modelDomainMap = new HashMap<>();

    private static ECoreModel currentModel;


    private BaseModelDomain(String domainId, ECoreModel model) {
        this.name = domainId;
        this.model = model;

        modelDomainMap.put(name, this);
    }


    /**
     *
     * @param domainId
     * @return
     */
    public static ModelDomain getModelDomain(String domainId) {
        return modelDomainMap.get(domainId);
    }

    public static BaseModelDomain newInstance(String name, ECoreModel model) {
        String configDomainName = model.getConfigDomainName();
        if(configDomainName==null)
            configDomainName = name;
        return new BaseModelDomain(configDomainName, model);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ECoreModel getModel() {
        return model;
    }


    public static ECoreModel getCurrentModel() {
        return currentModel;
    }

    public static void setCurrentModel(ECoreModel currentModel) {
        BaseModelDomain.currentModel = currentModel;
    }
}
