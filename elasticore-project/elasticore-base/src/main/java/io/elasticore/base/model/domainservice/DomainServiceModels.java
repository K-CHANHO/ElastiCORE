package io.elasticore.base.model.domainservice;

import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ComponentType;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.BaseComponentIdentity;
import io.elasticore.base.model.core.BaseModelComponentItem;
import io.elasticore.base.model.core.Items;
import lombok.Getter;

/** The domain services one domain declares. */
@Getter
public class DomainServiceModels {

    private final ComponentIdentity identity;
    private final MetaInfo meta;

    private final Items<DomainService> orgItems;

    private ModelComponentItems<DomainService> items;

    private DomainServiceModels(ComponentIdentity identity, MetaInfo meta,
                                Items<DomainService> items) {
        this.identity = identity;
        this.meta = meta;
        this.orgItems = items;
    }

    public ModelComponentItems<DomainService> getItems() {
        if (items == null || !items.hasNext()) {
            this.items = new BaseModelComponentItem(orgItems);
        }
        return this.items;
    }

    public static DomainServiceModels create(String domainId, String name, MetaInfo meta,
                                             Items<DomainService> items) {
        ComponentIdentity identity =
                BaseComponentIdentity.create(ComponentType.ENTITY_GROUP, domainId, name);
        return new DomainServiceModels(identity, meta, items);
    }
}
