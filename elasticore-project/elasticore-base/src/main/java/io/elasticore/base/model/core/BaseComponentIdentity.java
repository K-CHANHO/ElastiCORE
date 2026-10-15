package io.elasticore.base.model.core;


import io.elasticore.base.model.ComponentDesc;
import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ComponentType;

import java.util.Objects;

public final class BaseComponentIdentity implements ComponentIdentity {

    private String id;

    private String hashId;

    private ComponentDesc info;

    private ComponentType type;
    private String domainId;
    private String name;


    private BaseComponentIdentity(ComponentType type, String domainId, String name) {
        this.type = type;
        this.domainId = domainId;
        this.name = name;

        this.id = type.getName() + "://" + domainId + "/" + name;

        this.hashId = makeHashId();
    }

    /*
    public static BaseComponentIdentity create(ComponentType type, String name) {
        return new BaseComponentIdentity(type, null, name);
    }
     */

    public static BaseComponentIdentity create(ComponentType type, String domainId, String name) {
        return new BaseComponentIdentity(type, domainId, name);
    }


    private String makeHashId() {
        int hashCode = Objects.hash(this.id);
        if(type==null ) {
            System.err.println("outpuot");
        }

        if(name ==null)
            name = "undefined";

        return type.getName().substring(0, 1) + name.substring(0, 1) + hashCode;
    }

    BaseComponentIdentity(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseComponentIdentity that = (BaseComponentIdentity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public String getHashId() {
        return this.hashId;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setInfo(ComponentDesc info) {
        this.info = info;
    }

    @Override
    public ComponentDesc getInfo() {
        return this.info;
    }


    @Override
    public String getDomainId() {
        return this.domainId;
    }


    public ComponentType getComponentType() {
        return this.type;
    }
}
