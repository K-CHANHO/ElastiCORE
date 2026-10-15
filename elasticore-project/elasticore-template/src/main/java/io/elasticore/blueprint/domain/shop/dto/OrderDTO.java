//ecd:-2007436515H20260207233345_V1.0
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
import io.elasticore.blueprint.domain.shop.enums.*;
import io.elasticore.blueprint.domain.shop.dto.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * OrderDTO
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
public  class OrderDTO  implements java.io.Serializable  {

	@Schema(description = "customerFirstName"  )
	private String customerFirstName;
	
	@Schema(description = "customerEmail"  )
	private String customerEmail;
	
	@Schema(description = "items"  )
	private List<OrderItemDTO> items;
	
	@Schema(description = "orderId"  )
	@Size(max=36)
	private String orderId;
	
	@Schema(description = "orderDate" , requiredMode=Schema.RequiredMode.REQUIRED , example="yyyy-MM-dd HH:mm:ss")
	@NotNull
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime orderDate;
	
	@Schema(description = "totalAmount"  )
	@Builder.Default
	private java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
	
	@Schema(description = "status"  , example="PEN: Pending | CFM: Confirmed | PRC: Processing | SHP: Shipped | DLV: Delivered | CAN: Cancelled | RFD: Refunded")
	private OrderStatus status;
	
	@Schema(description = "paymentMethod"  , example="CC: Credit Card | DC: Debit Card | BT: Bank Transfer | PP: PayPal | CA: Cash")
	private PaymentMethod paymentMethod;
	
	@Schema(description = "notes"  )
	private String notes;
	
	@Schema(description = "trackingNumber"  )
	@Size(max=50)
	private String trackingNumber;
	
	@Schema(description = "version"  )
	private Long version;
	
	@Schema(description = "createdBy"  )
	@Size(max=50)
	private String createdBy;
	
	@Schema(description = "createDate"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime createDate;
	
	@Schema(description = "lastModifiedBy"  )
	@Size(max=50)
	private String lastModifiedBy;
	
	@Schema(description = "lastModifiedDate"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime lastModifiedDate;
	

}
