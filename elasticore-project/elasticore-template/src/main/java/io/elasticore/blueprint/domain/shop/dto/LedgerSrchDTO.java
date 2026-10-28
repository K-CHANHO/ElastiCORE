//ecd:-1516947450H20261028193834_V3.0
package io.elasticore.blueprint.domain.shop.dto;

import io.elasticore.blueprint.domain.shop.enums.*;
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
 * LedgerSrchDTO
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
public  class LedgerSrchDTO  implements java.io.Serializable, SortableObject, PageableObject  {

	@Schema(description = "Field equals value. field:ownerCustomerId"  )
	private Long ownerCustomerId;
	
	@Schema(description = "Field equals value. field:partnerCustomerId"  )
	private Long partnerCustomerId;
	
	@Schema(description = "Field equals value. field:ledgerId"  )
	private Long ledgerId;
	
	@Schema(description = "Field equals value. field:code"  , example="^[0-9]{13}$', 'code must be 13 digits")
	private String code;
	
	@Schema(description = "Field matches pattern. field:memo"  )
	private String memo;
	
	@Schema(description = "Field equals value. field:amount"  )
	private Long amount;
	
	@Schema(description = "Field is between two values (inclusive). field:postedAt"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime postedAtFrom;
	
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime postedAtTo;
	
	@Schema(description = "Field equals value. field:deleted"  )
	private Boolean deleted;
	
	
	private String sortCode;
	
	@Builder.Default
	private int pageNumber=0;
	
	@Builder.Default
	private int pageSize=20;

    private String sortColumn; // Column to sort by
    private Boolean sortAscending; // Sort order (true: ascending, false: descending)
}
