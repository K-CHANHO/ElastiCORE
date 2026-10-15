package io.elasticore.base.model.port;


import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ComponentType;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.BaseComponentIdentity;
import io.elasticore.base.model.core.BaseModelComponentItem;
import io.elasticore.base.model.core.Items;
import io.elasticore.base.model.repo.Repository;
import lombok.Getter;


@Getter
public class PortModels {

    private final ComponentIdentity identity;

    private final MetaInfo meta;

    private final Items<Port> orgItems;

    private ModelComponentItems<Port> items;

    private PortModels(ComponentIdentity identity, MetaInfo meta, Items<Port> items) {
        this.identity = identity;
        this.meta = meta;
        this.orgItems = items;
    }


    public ModelComponentItems<Port> getItems() {
        if (items == null || !items.hasNext())
            this.items = new BaseModelComponentItem(orgItems);
        return this.items;
    }


    public static PortModels create(String domainId, String name, MetaInfo meta, Items<Port> items) {
        ComponentIdentity identity = BaseComponentIdentity.create(ComponentType.PORT_GROUP, domainId, name);
        return new PortModels(identity, meta, items);
    }
}
