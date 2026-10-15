package io.elasticore.base;

import io.elasticore.base.model.ECoreModel;

/**
 * Represents a domain within an ECore model. A model domain is a logical grouping
 * of model components that are related or that share a common context.
 * This interface provides methods to access the name of the domain and
 * to retrieve the associated ECore model.
 */
public interface ModelDomain {

    /**
     * Gets the name of the model domain.
     * The name is used to uniquely identify the domain within the ECore model context.
     *
     * @return The name of the model domain.
     */
    String getName();

    /**
     * Retrieves the ECoreModel instance associated with this domain.
     * The ECoreModel contains the definitions and configurations of model components
     * belonging to this domain.
     *
     * @return The associated ECoreModel instance.
     */
    ECoreModel getModel();
}
