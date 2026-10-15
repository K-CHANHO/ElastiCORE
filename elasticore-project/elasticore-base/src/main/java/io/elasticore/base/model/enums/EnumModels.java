package io.elasticore.base.model.enums;

import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ComponentType;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.BaseComponentIdentity;
import io.elasticore.base.model.core.BaseModelComponentItem;
import io.elasticore.base.model.core.Items;
import io.elasticore.base.model.entity.Entity;
import lombok.Getter;


@Getter
public class EnumModels {

    private final ComponentIdentity identity;
    private final MetaInfo meta;
    private final ModelComponentItems<EnumModel> items;

    private EnumModels(ComponentIdentity identity, MetaInfo meta, Items<EnumModel> items) {
        this.identity = identity;

        this.meta = meta;
        this.items = new BaseModelComponentItem(items);
    }

    public static EnumModels create(String domainId, String name, MetaInfo meta, Items<EnumModel> items) {
        ComponentIdentity identity = BaseComponentIdentity.create(ComponentType.ENUM_GROUP, domainId, name);
        return new EnumModels(identity, meta, items);
    }


    public EnumModel findByName(String name) {
        return this.items.find(BaseComponentIdentity.create(ComponentType.ENUM, identity.getDomainId(), name));
    }

}
