package io.elasticore.base.model.repo;


import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ComponentType;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.AbstractReplaceableModel;
import io.elasticore.base.model.core.BaseComponentIdentity;
import io.elasticore.base.model.core.BaseModelComponentItem;
import io.elasticore.base.model.core.Items;
import lombok.Getter;

@Getter
public class Repository extends AbstractReplaceableModel {

    private final MetaInfo metaInfo;
    private ModelComponentItems<Method> items;

    private Items<Method> orgItems;

    private Repository(ComponentIdentity id, Items<Method> items, MetaInfo metaInfo) {
        super(id);

        if(items==null)
            items = Items.create(Method.class);

        this.orgItems =items;
        this.items = new BaseModelComponentItem(items);
        if(metaInfo ==null)
            this.metaInfo = MetaInfo.createEmpty();
        else
            this.metaInfo = metaInfo;

    }

    public static Repository create(String domainId, String name, Items<Method> items, MetaInfo metaInfo) {
        BaseComponentIdentity identity = BaseComponentIdentity.create(ComponentType.REPOSITORY, domainId, name);
        return new Repository(identity, items, metaInfo);
    }

    public ModelComponentItems<Method> getItems() {
        if(!this.items.hasNext())
            this.items = new BaseModelComponentItem(orgItems);

        return this.items;
    }

    public MetaInfo getMeta() {
        return metaInfo;
    }

}
