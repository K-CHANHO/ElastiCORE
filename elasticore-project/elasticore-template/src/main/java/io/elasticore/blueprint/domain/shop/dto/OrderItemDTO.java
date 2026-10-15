//ecd:1876421087H20260301221734_V2.0
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * OrderItemDTO
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
public  class OrderItemDTO  implements java.io.Serializable  {

	@Schema(description = "productName"  )
	private String productName;
	
	@Schema(description = "productSku"  )
	private String productSku;
	
	@Schema(description = "orderItemId"  )
	@Size(max=36)
	private String orderItemId;
	
	@Schema(description = "itemSeq"  )
	private Integer itemSeq;
	
	@Schema(description = "quantity" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@jakarta.validation.constraints.Min(1)
	@jakarta.validation.constraints.Max(9999)
	private Integer quantity;
	
	@Schema(description = "unitPrice" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	private java.math.BigDecimal unitPrice;
	
	@Schema(description = "discount"  )
	@Builder.Default
	private java.math.BigDecimal discount = java.math.BigDecimal.ZERO;
	

}
