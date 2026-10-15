package io.elasticore.base.model.enums;

import io.elasticore.base.model.*;
import io.elasticore.base.model.core.*;
import io.elasticore.base.model.entity.Field;
import lombok.Getter;

import java.util.Objects;


@Getter
public class EnumModel extends AbstractReplaceableModel implements DataModelComponent {

    private final MetaInfo metaInfo;
    private final Items<Field> fieldItems;
    private final Items<EnumConstant> enumConstantItems;

    private EnumModel(ComponentIdentity id, MetaInfo metaInfo, Items<Field> fieldItems, Items<EnumConstant> enumConstantItems) {
        super(id);

        if(metaInfo==null)
            this.metaInfo = MetaInfo.createEmpty();
        else
            this.metaInfo = metaInfo;


        this.fieldItems = fieldItems;
        this.enumConstantItems = enumConstantItems;
    }

    public static EnumModel create(String domainId, String name, MetaInfo meta, Items<Field> items, Items<EnumConstant> enumConstantItems) {
        ComponentIdentity identity = BaseComponentIdentity.create(ComponentType.ENUM, domainId, name);
        return new EnumModel(identity, meta, items, enumConstantItems);
    }

    /**
     *
     * @return
     */
    private ECoreModel getEcoreModel() {
        ECoreModel eCoreModel = null;
        try {
            eCoreModel = BaseModelDomain.getModelDomain(getIdentity().getDomainId())
                    .getModel();
        }catch (RuntimeException re) {

        }
        if(eCoreModel ==null)
            eCoreModel = BaseModelDomain.getCurrentModel();

        return eCoreModel;
    }

    @Override
    public ModelComponentItems<Field> getItems() {
        return new BaseModelComponentItem(fieldItems);
    }

    @Override
    public ListMap<String, Field> getAllFieldListMap() {
        ListMap<String, Field> fieldListMap = new ListMap<>();
        ModelComponentItems<Field> list = getItems();
        while(list.hasNext()) {
            Field next = list.next();
            fieldListMap.put(next.getName(), next);
        }

        return fieldListMap;
    }

    @Override
    public String getFullName() {
        ComponentType componentType = this.getIdentity().getComponentType();

        String ns = getEcoreModel().getNamespace(componentType.getName());

        return ns+"."+this.getIdentity().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EnumModel enumModel = (EnumModel) o;
        return Objects.equals(getIdentity().getId(), enumModel.getIdentity().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getIdentity().getId());
    }
}
