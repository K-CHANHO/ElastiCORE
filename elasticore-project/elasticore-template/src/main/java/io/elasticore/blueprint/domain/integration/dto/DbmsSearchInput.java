//ecd:-257591443H20260207233344_V1.0
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
 * DbmsSearchInput
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
public  class DbmsSearchInput  implements java.io.Serializable  {

	@Schema(description = "keyword"  )
	private String keyword;
	
	@Schema(description = "status"  )
	private Integer status;
	
	@Schema(description = "minId"  )
	private Long minId;
	
	@Schema(description = "maxId"  )
	private Long maxId;
	
	@Schema(description = "sortCode"  )
	private String sortCode;
	
	@Schema(description = "pageNumber"  )
	private Integer pageNumber;
	
	@Schema(description = "pageSize"  )
	private Integer pageSize;
	

}
