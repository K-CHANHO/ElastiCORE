package io.elasticore.base.model.enums;


import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ComponentType;
import io.elasticore.base.model.ModelComponent;
import io.elasticore.base.model.core.BaseComponentIdentity;
import lombok.Builder;
import lombok.Setter;

@Builder
@Setter
public class Parameter implements ModelComponent {


    private ComponentIdentity identity;

    private String value;

    public String getValue() {
        return this.value;
    }


    @Override
    public ComponentIdentity getIdentity() {
        return BaseComponentIdentity.create(ComponentType.FIELD, "param", this.value);
    }

}
