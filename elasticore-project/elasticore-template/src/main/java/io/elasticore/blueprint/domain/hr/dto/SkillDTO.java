//ecd:-1626135505H20260207233343_V1.0
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
 * SkillDTO
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
public  class SkillDTO  implements java.io.Serializable  {

	@Schema(description = "skillId"  )
	private Long skillId;
	
	@Schema(description = "category"  )
	@Size(max=50)
	private String category;
	
	@Schema(description = "skillName" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=100)
	private String skillName;
	
	@Schema(description = "proficiency"  )
	@jakarta.validation.constraints.Min(1)
	@jakarta.validation.constraints.Max(10)
	private Integer proficiency;
	

}
