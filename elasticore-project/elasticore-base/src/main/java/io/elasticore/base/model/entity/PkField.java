package io.elasticore.base.model.entity;

import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.BaseModelComponentItem;
import io.elasticore.base.model.core.Items;
import lombok.Getter;

@Getter
public class PkField {

    private final MetaInfo metaInfo;
    private final ModelComponentItems<Field> items;
    private final Entity parentEntity;

    private String type;


    private PkField(Items<Field> items, MetaInfo metaInfo, Entity parentEntity) {
        this.items = new BaseModelComponentItem(items);
        this.metaInfo = metaInfo;
        this.parentEntity = parentEntity;

        if (isMultiple()) {

            this.type = parentEntity.getIdentity().getName() + "Identity";
        } else {
            try {
                this.type = this.items.get(0).getTypeInfo().getDefaultTypeName();
            }catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }
    }

    public boolean isMultiple() {
        if (items.size() > 1)
            return true;

        return false;
    }

    public String getName() {
        if(isMultiple()){
            return "id";
        }
        else {
            return items.get(0).getName();
        }
    }

    public static PkField create(Items<Field> items, MetaInfo metaInfo, Entity parentEntity) {
        return new PkField(items, metaInfo, parentEntity);
    }


    public int getKeyCount() {
        return this.items.size();
    }


}
