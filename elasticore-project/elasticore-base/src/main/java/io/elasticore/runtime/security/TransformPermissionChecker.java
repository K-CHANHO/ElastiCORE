package io.elasticore.runtime.security;

/**
 * The {@code TransformPermissionChecker} interface provides a mechanism
 * to check permissions between two objects. Implementations of this
 * interface should provide the logic to determine if the transformation
 * or access between the given objects is allowed.
 * <p>
 * This interface is typically used in scenarios where access control
 * or permissions need to be validated dynamically between two different
 * objects in the system.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     TransformPermissionChecker checker = new CustomTransformPermissionChecker();
 *     boolean hasAccess = checker.hasPermission(sourceObject, targetObject);
 * </pre>
 *
 *
 */
public interface TransformPermissionChecker {

    /**
     * Checks if the transformation or access between the given {@code fromObject}
     * and {@code targetObject} is permitted.
     *
     * @param fromObject the source object from which the transformation or access is initiated
     * @param targetObject the target object to which the transformation or access is intended
     * @return {@code true} if the transformation or access is permitted; {@code false} otherwise
     */
    boolean hasPermission(Object fromObject, Object targetObject);
}
