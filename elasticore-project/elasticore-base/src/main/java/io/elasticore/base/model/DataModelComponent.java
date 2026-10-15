package io.elasticore.base.model;

import io.elasticore.base.model.core.ListMap;
import io.elasticore.base.model.entity.Field;

import java.util.List;

public interface DataModelComponent extends ModelComponent {

    MetaInfo getMetaInfo();

    ModelComponentItems<Field> getItems();

    /**
     *
     * @return ListMap<String,Field>
     */
    ListMap<String,Field> getAllFieldListMap();


    /**
     *
     * @return
     */
    String getFullName();

}
