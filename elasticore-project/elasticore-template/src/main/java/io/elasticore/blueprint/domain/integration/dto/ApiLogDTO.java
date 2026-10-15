//ecd:919436964H20260929130404_V3.0
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
import io.elasticore.blueprint.domain.shop.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * ApiLogDTO
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
public  class ApiLogDTO  implements java.io.Serializable  {

	@Schema(description = "relatedOrderStatus"  , example="PEN: Pending | CFM: Confirmed | PRC: Processing | SHP: Shipped | DLV: Delivered | CAN: Cancelled | RFD: Refunded")
	private OrderStatus relatedOrderStatus;
	
	@Schema(description = "logId"  )
	private Long logId;
	
	@Schema(description = "endpoint" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=500)
	private String endpoint;
	
	@Schema(description = "method" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=10)
	private String method;
	
	@Schema(description = "requestBody"  )
	private String requestBody;
	
	@Schema(description = "responseBody"  )
	private String responseBody;
	
	@Schema(description = "statusCode"  )
	private Integer statusCode;
	
	@Schema(description = "duration"  )
	private Long duration;
	
	@Schema(description = "createdAt"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime createdAt;
	
	@Schema(description = "inheritedPaymentMethod"  , example="CC: Credit Card | DC: Debit Card | BT: Bank Transfer | PP: PayPal | CA: Cash")
	private PaymentMethod inheritedPaymentMethod;
	
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
