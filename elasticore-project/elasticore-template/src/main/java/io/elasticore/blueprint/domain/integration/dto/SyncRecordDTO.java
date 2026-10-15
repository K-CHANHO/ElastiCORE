//ecd:1119541131H20260207233344_V1.0
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
import io.elasticore.blueprint.domain.integration.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * SyncRecordDTO
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
public  class SyncRecordDTO  implements java.io.Serializable  {

	@Schema(description = "syncStatus"  , example="PND: Pending | IPR: In Progress | SUC: Success | FAL: Failed | SKP: Skipped")
	private SyncStatus syncStatus;
	
	@Schema(description = "recordId"  )
	@Size(max=36)
	private String recordId;
	
	@Schema(description = "sourceSystem" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=50)
	private String sourceSystem;
	
	@Schema(description = "targetSystem" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=50)
	private String targetSystem;
	
	@Schema(description = "entityType"  )
	@Size(max=50)
	private String entityType;
	
	@Schema(description = "entityId"  )
	@Size(max=100)
	private String entityId;
	
	@Schema(description = "syncDate"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime syncDate;
	
	@Schema(description = "errorMessage"  )
	private String errorMessage;
	
	@Schema(description = "retryCount"  )
	@Builder.Default
	private Integer retryCount = 0;
	

}
