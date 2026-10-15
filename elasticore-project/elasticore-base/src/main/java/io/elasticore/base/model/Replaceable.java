package io.elasticore.base.model;

public interface Replaceable<T extends Replaceable<T>> extends ModelComponent {

    String TYPE_ENTITY = "entity";
    String TYPE_IO = "io";
    String TYPE_OPERTATION = "op";
    String TYPE_REPOSITORY = "repo";
    String TYPE_SERVICE = "service";

    T getObject();


}
