//ecd:387906054H20260207233344_V1.0
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


/**
 * SyncRecordSrchDTO
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
public  class SyncRecordSrchDTO  implements java.io.Serializable, SortableObject, PageableObject  {

	@Schema(description = "Field matches any value in the given list. field:syncStatus"  , example="PND: Pending | IPR: In Progress | SUC: Success | FAL: Failed | SKP: Skipped")
	private SyncStatus syncStatus;
	
	@Schema(description = "Use 'like' if value has %, else 'equal' field:recordId"  )
	private String recordId;
	
	@Schema(description = "Field equals value. field:sourceSystem"  )
	private String sourceSystem;
	
	@Schema(description = "Field equals value. field:targetSystem"  )
	private String targetSystem;
	
	@Schema(description = "Field matches pattern. field:entityType"  )
	private String entityType;
	
	@Schema(description = "Use 'like' if value has %, else 'equal' field:entityId"  )
	private String entityId;
	
	@Schema(description = "Field is between two values (inclusive). field:syncDate"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime syncDateFrom;
	
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime syncDateTo;
	
	@Schema(description = "Use 'like' if value has %, else 'equal' field:errorMessage"  )
	private String errorMessage;
	
	@Schema(description = "Field equals value. field:retryCount"  )
	private Integer retryCount;
	
	
	private String sortCode;
	
	@Builder.Default
	private int pageNumber=0;
	
	@Builder.Default
	private int pageSize=30;

    private String sortColumn; // Column to sort by
    private Boolean sortAscending; // Sort order (true: ascending, false: descending)
}
