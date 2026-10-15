package io.elasticore.base.model.relation;

import io.elasticore.base.model.ModelComponent;
import io.elasticore.base.model.core.ReferenceResolver;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ModelRelationship {

    private String fromName;
    private String toName;
    private RelationType relationType;

    private String relationName;

    private ReferenceResolver referenceResolver;


    private ModelRelationship(String fromName, String toName, RelationType relationType ,String relationName) {

        if(fromName==null || fromName.trim().isEmpty()) {
            throw new IllegalArgumentException("fromName is null");
        }
        if(toName==null || toName.trim().isEmpty()) {
            throw new IllegalArgumentException("toName is null");
        }

        this.fromName = fromName.trim();
        this.toName = toName.trim();
        this.relationType = relationType;
        this.relationName = relationName;
    }

    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    private String getRefTarget(String keyName) {
        if(referenceResolver!=null) {
            Object refTarget = referenceResolver.getReferenceTarget(keyName);
            if(refTarget!=null)
                return refTarget.toString();
        }
        return null;
    }

    public String getFromName() {
        String refTarget = getRefTarget(this.fromName);
        if(refTarget!=null) return refTarget;

        return fromName;
    }

    public String getToName() {
        String refTarget = getRefTarget(this.toName);
        if(refTarget!=null) return refTarget;

        return toName;
    }

    public static ModelRelationship create(String fromName, String toName, RelationType relationType) {
        return new ModelRelationship(fromName, toName, relationType, null);
    }

    public static ModelRelationship create(String fromName, String toName, RelationType relationType,String relationName) {
        return new ModelRelationship(fromName, toName, relationType, relationName);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelRelationship that = (ModelRelationship) o;
        return Objects.equals(fromName, that.fromName) && Objects.equals(toName, that.toName) && relationType == that.relationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromName, toName, relationType);
    }
}
