//ecd:-1810007809H20260328122051_V2.0
package io.elasticore.blueprint.domain.jsonview_demo.dto;

/**
 * JsonView definitions for field visibility control per operation.
 *
 * - Create: Fields visible during entity creation (POST)
 * - Update: Fields visible during entity update (PUT/PATCH)
 * - Read: Fields visible in read/response operations (GET, all responses)
 *
 * Fields without @JsonView are visible in ALL operations.
 *
 * Generated and managed by ElastiCORE.
 */
public class Views {

    /** Fields visible during entity creation (POST) */
    public interface Create {}

    /** Fields visible during entity update (PUT/PATCH) */
    public interface Update {}

    /** Fields visible in read/response operations (GET, all responses) */
    public interface Read {}
}
