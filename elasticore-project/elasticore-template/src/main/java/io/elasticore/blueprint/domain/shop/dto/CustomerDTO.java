//ecd:-1449551393H20260207233345_V1.0
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * CustomerDTO
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
public  class CustomerDTO  implements java.io.Serializable  {

	@Schema(description = "customerId"  )
	private Long customerId;
	
	@Schema(description = "firstName" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=50)
	private String firstName;
	
	@Schema(description = "lastName" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=50)
	private String lastName;
	
	@Schema(description = "email" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=100)
	private String email;
	
	@Schema(description = "phone"  )
	@Size(max=20)
	private String phone;
	
	@Schema(description = "dateOfBirth"  , example="yyyy-MM-dd")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
	private java.time.LocalDate dateOfBirth;
	
	@Schema(description = "loyaltyPoints"  )
	@jakarta.validation.constraints.Min(0)
	@Builder.Default
	private Long loyaltyPoints = 0L;
	
	@Schema(description = "customerType"  , example="I: Individual | B: Business | W: Wholesale")
	private CustomerType customerType;
	
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
