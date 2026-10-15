package io.elasticore.springboot3.mapper;

/**
 * Functional interface for implementing custom validation and conditional logic
 * during object mapping operations. MappingGuards allow developers to control
 * whether specific field mappings should proceed based on runtime conditions.
 * 
 * <p>Guards are associated with specific fields in the mapping configuration
 * and are invoked during the mapping process to determine if the mapping
 * operation should continue. This enables sophisticated mapping logic such as:</p>
 * <ul>
 *   <li>Conditional field mapping based on source or target object state</li>
 *   <li>Runtime validation of field values before mapping</li>
 *   <li>Custom business rules enforcement during mapping</li>
 *   <li>Event-specific behavior based on mapping context</li>
 * </ul>
 * 
 * @author ElastiCore Framework
 * @since 1.0
 * @see MappingContext
 * @see Mapper#on(String, MappingGuard)
 */
@FunctionalInterface
public interface MappingGuard {
    /**
     * Evaluates whether the mapping operation should proceed based on the provided context.
     * This method is called during the mapping process to determine if a specific field
     * mapping should be executed.
     * 
     * @param ctx the mapping context containing information about the current mapping operation,
     *           including source object, target object, field name, and event type
     * @return {@code true} if the mapping should proceed, {@code false} to skip the mapping
     */
    boolean check(MappingContext ctx);
}
