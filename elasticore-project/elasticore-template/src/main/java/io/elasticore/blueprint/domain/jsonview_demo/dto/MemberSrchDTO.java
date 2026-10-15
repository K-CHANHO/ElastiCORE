//ecd:514886414H20260328122051_V2.0
package io.elasticore.blueprint.domain.jsonview_demo.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.time.*;



/**
 * MemberSrchDTO
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public  class MemberSrchDTO  implements java.io.Serializable, SortableObject, PageableObject  {

	@Schema(description = "Field matches pattern. field:username"  )
	private String username;
	
	@Schema(description = "Field matches pattern. field:email"  )
	private String email;
	
	@Schema(description = "Field equals value. field:role"  )
	private String role;
	
	
	private String sortCode;
	
	@Builder.Default
	private int pageNumber=0;
	
	@Builder.Default
	private int pageSize=10;

    private String sortColumn; // Column to sort by
    private Boolean sortAscending; // Sort order (true: ascending, false: descending)
}
