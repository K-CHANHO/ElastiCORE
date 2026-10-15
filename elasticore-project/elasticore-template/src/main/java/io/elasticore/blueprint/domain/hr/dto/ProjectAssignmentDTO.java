//ecd:-1418362357H20260207233344_V1.0
package io.elasticore.blueprint.domain.hr.dto;

import io.elasticore.blueprint.domain.hr.enums.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.time.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * ProjectAssignmentDTO
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public  class ProjectAssignmentDTO  implements java.io.Serializable  {

	@Schema(description = "empId"  )
	@Size(max=36)
	private String empId;
	
	@Schema(description = "projectCode"  )
	@Size(max=20)
	private String projectCode;
	
	@Schema(description = "role"  )
	@Size(max=50)
	private String role;
	
	@Schema(description = "assignDate"  , example="yyyy-MM-dd")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
	private java.time.LocalDate assignDate;
	
	@Schema(description = "hoursAllocated"  )
	@jakarta.validation.constraints.Min(0)
	@jakarta.validation.constraints.Max(2000)
	private Integer hoursAllocated;
	

}
