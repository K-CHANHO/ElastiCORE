package io.elasticore.base.model.listener;

import io.elasticore.base.model.dto.DataTransfer;
import io.elasticore.base.model.dto.DataTransferModels;

public class ModelObjectListener {


    private final static ModelObjectListener instance = new ModelObjectListener();


    private ModelObjectListener() {}

    public static ModelObjectListener getInstance() {
        return instance;
    }

    private DataTransferModels dataTransferModels;

    public void setDataTransferModels(DataTransferModels dataTransferModels) {
        this.dataTransferModels = dataTransferModels;
    }


    public void onCreateDataTransfer(DataTransfer dataTransfer) {
        if(dataTransferModels!=null)
            dataTransferModels.append(dataTransfer);
    }


    public void clear() {
        this.dataTransferModels = null;
    }


}
