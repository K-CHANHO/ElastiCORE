//ecd:621676604H20261028193834_V3.0
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
import com.fasterxml.jackson.annotation.*;
import io.elasticore.blueprint.domain.shop.dto.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * LedgerDTO
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
public  class LedgerDTO  implements java.io.Serializable  {

	@Schema(description = "owner" , requiredMode=Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY )
	@NotNull
	private CustomerDTO owner;
	
	@Schema(description = "partner" , accessMode = Schema.AccessMode.READ_ONLY )
	private CustomerDTO partner;
	
	@Schema(description = "ownerCustomerId"  )
	private Long ownerCustomerId;
	
	@Schema(description = "partnerCustomerId"  )
	private Long partnerCustomerId;
	
	@Schema(description = "ledgerId"  )
	private Long ledgerId;
	
	@Schema(description = "code" , requiredMode=Schema.RequiredMode.REQUIRED , example="^[0-9]{13}$', 'code must be 13 digits")
	@NotNull
	@Size(max=13)
	private String code;
	
	@Schema(description = "memo"  )
	@Size(min=2 , max=200)
	private String memo;
	
	@Schema(description = "amount" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	private Long amount;
	
	@Schema(description = "postedAt"  , example="yyyy-MM-dd HH:mm:ss")
	@JsonProperty(access=JsonProperty.Access.READ_ONLY)
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime postedAt;
	
	@Schema(description = "deleted"  )
	@Builder.Default
	private Boolean deleted = false;
	

}
