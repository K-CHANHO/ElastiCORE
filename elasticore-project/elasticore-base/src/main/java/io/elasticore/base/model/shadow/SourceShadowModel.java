package io.elasticore.base.model.shadow;

import io.elasticore.base.model.DataModelComponent;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.ShadowModel;
import io.elasticore.base.model.core.BaseECoreModelContext;
import io.elasticore.base.model.entity.Field;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SourceShadowModel implements ShadowModel {

    private ECoreModel eCoreModel;
    private String parentName;
    private String name;
    private List<Field> fieldList = new ArrayList<>();

    private Set<String> namespaceSet = new HashSet<>();
    private Set<String> individualClassSet = new HashSet<>();


    public SourceShadowModel(String name) {
        this.name = name;
    }

    public SourceShadowModel(String name, String parentName) {
        this.name = name;
        this.parentName = parentName;

    }

    @Override
    public String getParentName() {
        return this.parentName;
    }

    @Override
    public void setECoreModel(ECoreModel eCoreModel) {
        this.eCoreModel = eCoreModel;
    }

    @Override
    public boolean hasField(Field f) {

        return hasField(f.getName());
    }

    private ShadowModel findShadowModelFromAllDoamin(String modelName) {
        String[] items = modelName.split(":");
        if(items.length==2) {
            String domainId = items[0];
            String coreName = items[1];
            return BaseECoreModelContext.getContext().getDomain(domainId).getModel().getShadowModel(coreName);
        }else {
            ShadowModel shadowModel = BaseECoreModelContext.findShadowModelByName(modelName);
            if(shadowModel!=null) {
                return shadowModel;
            }
            return eCoreModel.getShadowModel(modelName);
        }
    }

    @Override
    public boolean hasField(String fieldName) {
        if( getField(fieldName) ==null) {
            if(this.parentName!=null) {
                ShadowModel shadowModel = findShadowModelFromAllDoamin(this.parentName);
                if(shadowModel!=null) {
                    return shadowModel.hasField(fieldName);
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Field> listFields() {
        return fieldList;
    }

    @Override
    public Field getField(String name) {
        if(name==null || name.isEmpty())
            return null;

        for(Field f: fieldList) {
            if(name.equals(f.getName()))
                return f;
        }

        return null;
    }

    public void addField(Field f) {
        this.fieldList.add(f);
        this.setNamespaceInfo(f);
    }

    public Set<String> getNamespaceSet() {
        return this.namespaceSet;
    }

    public Set<String> getIndividualClassSet() {
        return this.individualClassSet;
    }

    public void addIndividualClass(String fullyQualifiedClassName) {
        this.individualClassSet.add(fullyQualifiedClassName);
    }


    private void setNamespaceInfo(Field f) {

        if(!f.getTypeInfo().isBaseType()) {

            Set<String> typeSet = f.getTypeInfo().getTypes();
            for(String coreType: typeSet) {
                setNamespaceInfo(coreType);
            }
        }
    }
    public void setImportPackageName(String packageName) {
        namespaceSet.add(packageName);
    }

    public void setNamespaceInfo(String typeName) {
        DataModelComponent modelComponent = BaseECoreModelContext.getContext().findModelComponent(typeName);

        if(modelComponent!=null) {


            String domainId = modelComponent.getIdentity().getDomainId();

            String ns = BaseECoreModelContext.getContext().getDomain(domainId).getModel()
                    .getNamespace(modelComponent.getIdentity().getComponentType().getName());

            // Keep existing wildcard approach for compatibility
            namespaceSet.add(ns+".*");

            // Add individual class name
            String fullyQualifiedClassName = ns + "." + typeName;
            individualClassSet.add(fullyQualifiedClassName);
        }
    }
}
