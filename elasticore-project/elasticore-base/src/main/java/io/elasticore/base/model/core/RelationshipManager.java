package io.elasticore.base.model.core;

import io.elasticore.base.model.relation.ModelRelationship;
import io.elasticore.base.model.relation.RelationType;

import java.util.*;
import java.util.stream.Collectors;

public class RelationshipManager implements ReferenceResolver{

    private List<ModelRelationship> relationshipList;
    private Set<ModelRelationship> relationshipSet;

    private Map<String,Object> refTargetMap = new HashMap<>();

    private RelationshipManager() {
        this.relationshipList = new ArrayList<>();
        this.relationshipSet = new HashSet<>();
    }

    public static RelationshipManager getInstance() {
        return null;
    }

    synchronized void reset() {
        this.relationshipList.clear();
        this.relationshipSet.clear();
    }

    private static Map<String, RelationshipManager> relationshipManagerMap = new HashMap<>();

    @Override
    public Object getReferenceTarget(String refName) {
        return refTargetMap.get(refName);
    }

    public void setRefTargetInfo(String refName, Object targetObject){
        refTargetMap.put(refName, targetObject);
    }

    public static RelationshipManager getInstance(String domainId) {
        RelationshipManager instance = relationshipManagerMap.get(domainId);
        if(instance==null) {
            instance = new RelationshipManager();
            relationshipManagerMap.put(domainId, instance);
        }
        return instance;
    }

    static boolean clear(String domainId) {
        return relationshipManagerMap.remove(domainId)!=null;
    }


    public synchronized void addRelationship(ModelRelationship relationship) {
        relationship.setReferenceResolver(this);
        if (!relationshipSet.contains(relationship)) {
            this.relationshipList.add(relationship);
            this.relationshipSet.add(relationship);
        }
    }

    public List<ModelRelationship> findByToName(String name) {
        return relationshipList.stream()
                .filter(r -> r.getToName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    public List<ModelRelationship> findByToNameAndType(String name, RelationType relationType) {
        return relationshipList.stream()
                .filter(r -> r.getToName().equalsIgnoreCase(name) && r.getRelationType() == relationType)
                .collect(Collectors.toList());
    }

    public List<ModelRelationship> findByFromName(String name) {
        return relationshipList.stream()
                .filter(r -> r.getFromName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    public List<ModelRelationship> findByFromNameAndType(String name, RelationType relationType) {
        return relationshipList.stream()
                .filter(r -> r.getFromName().equalsIgnoreCase(name) && r.getRelationType() == relationType)
                .collect(Collectors.toList());
    }


    public List<ModelRelationship> findByType(RelationType relationType) {
        return relationshipList.stream()
                .filter(r -> r.getRelationType()==relationType)
                .collect(Collectors.toList());
    }

}
