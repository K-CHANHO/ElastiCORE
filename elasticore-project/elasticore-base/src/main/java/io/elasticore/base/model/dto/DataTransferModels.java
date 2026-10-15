package io.elasticore.base.model.dto;

import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ComponentType;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.BaseComponentIdentity;
import io.elasticore.base.model.core.BaseModelComponentItem;
import io.elasticore.base.model.core.Items;
import io.elasticore.base.model.listener.ModelObjectListener;
import lombok.Getter;


@Getter
public class DataTransferModels {

    private final ComponentIdentity identity;
    private final MetaInfo meta;
    private ModelComponentItems<DataTransfer> items;

    private final Items<DataTransfer> orgItems;

    private DataTransferModels(ComponentIdentity identity, MetaInfo meta, Items<DataTransfer> items) {
        this.identity = identity;

        this.meta = meta;
        this.orgItems = items;
        this.items = new BaseModelComponentItem(items);
    }


    public static DataTransferModels create(String domainId, String name, MetaInfo meta, Items<DataTransfer> items) {
        ComponentIdentity identity = BaseComponentIdentity.create(ComponentType.DTO_GROUP, domainId, name);
        DataTransferModels dataTransferModels = new DataTransferModels(identity, meta, items);

        ModelObjectListener.getInstance().setDataTransferModels(dataTransferModels);

        return dataTransferModels;
    }

    public void append(DataTransfer item) {

        this.orgItems.addItem(item);
        this.items = new BaseModelComponentItem(this.orgItems);
    }

    public DataTransfer findByName(String name) {
        return this.items.find(BaseComponentIdentity.create(ComponentType.DTO, this.identity.getDomainId(), name));
    }

}
