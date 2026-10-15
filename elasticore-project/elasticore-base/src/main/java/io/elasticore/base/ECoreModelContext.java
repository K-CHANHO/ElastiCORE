package io.elasticore.base;

import io.elasticore.base.model.DataModelComponent;
import io.elasticore.base.model.loader.ModelLoaderContext;

/**
 * Represents the context of an ECore model, providing access to model domains
 * and facilitating the publication of model components.
 * This interface defines methods to retrieve domain names, access specific model domains,
 * and publish model components using a given code publisher.
 */
public interface ECoreModelContext {

    /**
     * Retrieves the names of all domains within the ECore model context.
     *
     * @return An array of domain names.
     */
    String[] getInternalDomainNames();


    String[] getAllDomainNames();


    /**
     * Retrieves the default model domain associated with this context.
     * If multiple domains exist, this method may return the primary or default domain.
     *
     * @return The default ModelDomain instance.
     */
    ModelDomain getDomain();

    /**
     * Retrieves a specific model domain by its name.
     *
     * @param name The name of the domain to retrieve.
     * @return The ModelDomain instance with the specified name, or null if no such domain exists.
     */
    ModelDomain getDomain(String name);


    DataModelComponent findModelComponent(String domainId, String modelName);

    DataModelComponent findModelComponent(String domainId, String modelName, boolean isSearchOtherDdomain);


    DataModelComponent findModelComponent(String modelName);


    <T> T findModelComponent(String modelName, Class<T> type);


    ModelLoaderContext getModelLoaderContext();

}
