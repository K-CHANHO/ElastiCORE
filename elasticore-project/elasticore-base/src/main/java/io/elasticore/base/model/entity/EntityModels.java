package io.elasticore.base.model.entity;

import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ComponentType;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.BaseComponentIdentity;
import io.elasticore.base.model.core.BaseModelComponentItem;
import io.elasticore.base.model.core.Items;
import lombok.Getter;

import java.util.List;


@Getter
public class EntityModels {

    private final ComponentIdentity identity;
    private final MetaInfo meta;
    private final Items<Entity> items;

    private EntityModels(ComponentIdentity identity, MetaInfo meta, Items<Entity> items) {
        this.identity = identity;

        this.meta = meta;
        this.items = items;
    }

    public ModelComponentItems<Entity> getItems() {
        return new BaseModelComponentItem(items);
    }


    public static EntityModels create(String domainId, String name, MetaInfo meta, Items<Entity> items) {
        ComponentIdentity identity = BaseComponentIdentity.create(ComponentType.ENTITY_GROUP, domainId, name);
        return new EntityModels(identity, meta, items);

    }


    public Entity findByName(String name) {
        return this.items.find(BaseComponentIdentity.create(ComponentType.ENTITY, identity.getDomainId(), name));
    }


    public Entity findByTableName(String tblName) {
        List<Entity> list = this.items.getItemList();
        for(Entity entity: list) {
            if(entity.getTableName().equalsIgnoreCase(tblName)) {
                return entity;
            }
        }

        return null;
    }


}
