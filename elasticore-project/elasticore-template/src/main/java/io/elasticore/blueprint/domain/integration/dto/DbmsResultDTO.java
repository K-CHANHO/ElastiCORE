//ecd:1726348727H20260207233344_V1.0
package io.elasticore.blueprint.domain.integration.dto;

import io.elasticore.blueprint.domain.integration.enums.*;
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
 * DbmsResultDTO
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
public  class DbmsResultDTO  implements java.io.Serializable  {

	@Schema(description = "logId"  )
	private Long logId;
	
	@Schema(description = "endpoint"  )
	private String endpoint;
	
	@Schema(description = "method"  )
	private String method;
	
	@Schema(description = "statusCode"  )
	private Integer statusCode;
	
	@Schema(description = "duration"  )
	private Long duration;
	
	@Schema(description = "createdAt"  )
	private String createdAt;
	

}
