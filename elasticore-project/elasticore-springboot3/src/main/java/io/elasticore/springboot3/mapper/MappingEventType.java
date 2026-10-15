package io.elasticore.springboot3.mapper;

/**
 * Enumeration representing different types of mapping events that can occur
 * during the object mapping process. These events allow for fine-grained control
 * and customization of mapping behavior at different stages.
 * 
 * <p>This enum is used by {@link MappingContext} to track the current stage
 * of the mapping operation and by {@link MappingGuard} implementations to
 * apply conditional logic based on the mapping event type.</p>
 * 
 * @author ElastiCore Framework
 * @since 1.0
 */
public enum MappingEventType {

    /** 
     * Indicates the mapping event occurs before a field value is mapped from source to target.
     * This event is triggered at the beginning of the field mapping process, allowing
     * guards to perform validation or preprocessing before the actual value transfer.
     */
    BEFORE("BEFORE",""),
    
    /** 
     * Indicates the mapping event occurs after an item has been added to a list during mapping.
     * This event is triggered when mapping operations involve collections, allowing for
     * post-processing logic after individual elements have been added to target collections.
     */
    AFTER_ADD_LIST("AFTER_ADD_LIST","");



    private final String name;

    private final String desc;

    MappingEventType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }
}
