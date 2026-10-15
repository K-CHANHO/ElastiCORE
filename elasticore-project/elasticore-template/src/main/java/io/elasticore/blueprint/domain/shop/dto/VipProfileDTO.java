//ecd:1136627328H20260803220216_V2.2
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
import io.elasticore.blueprint.domain.shop.dto.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * VipProfileDTO
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
public  class VipProfileDTO  implements java.io.Serializable  {

	@Schema(description = "customer" , accessMode = Schema.AccessMode.READ_ONLY )
	private CustomerDTO customer;
	
	@Schema(description = "customerCustomerId"  )
	private Long customerCustomerId;
	
	@Schema(description = "profileId"  )
	private Long profileId;
	
	@Schema(description = "tier"  )
	@Size(max=20)
	@Builder.Default
	private String tier = "SILVER";
	
	@Schema(description = "discountRate"  )
	@jakarta.validation.constraints.Min(0)
	@jakarta.validation.constraints.Max(50)
	@Builder.Default
	private Double discountRate = 0.0;
	
	@Schema(description = "enrollDate"  , example="yyyy-MM-dd")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
	private java.time.LocalDate enrollDate;
	

}
