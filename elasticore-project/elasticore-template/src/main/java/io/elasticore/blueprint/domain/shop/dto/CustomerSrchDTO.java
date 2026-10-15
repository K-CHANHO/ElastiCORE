//ecd:-885924100H20260207233345_V1.0
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


/**
 * CustomerSrchDTO
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
public  class CustomerSrchDTO  implements java.io.Serializable, SortableObject, PageableObject  {

	@Schema(description = "Field equals value. field:customerType"  , example="I: Individual | B: Business | W: Wholesale")
	private CustomerType customerType;
	
	@Schema(description = "Field equals value. field:vipProfileProfileId"  )
	private Long vipProfileProfileId;
	
	@Schema(description = "Field equals value. field:customerId"  )
	private Long customerId;
	
	@Schema(description = "Field matches pattern. field:firstName"  )
	private String firstName;
	
	@Schema(description = "Field matches pattern. field:lastName"  )
	private String lastName;
	
	@Schema(description = "Field equals value. field:email"  )
	private String email;
	
	@Schema(description = "Use 'like' if value has %, else 'equal' field:phone"  )
	private String phone;
	
	@Schema(description = "Field is between two values (inclusive). field:dateOfBirth"  , example="yyyy-MM-dd")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
	private java.time.LocalDate dateOfBirthFrom;
	
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
	private java.time.LocalDate dateOfBirthTo;
	
	@Schema(description = "Field equals value. field:loyaltyPoints"  )
	private Long loyaltyPoints;
	
	@Schema(description = "Use 'like' if value has %, else 'equal' field:createdBy"  )
	private String createdBy;
	
	@Schema(description = "Field is between two values (inclusive). field:createDate"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime createDateFrom;
	
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime createDateTo;
	
	@Schema(description = "Use 'like' if value has %, else 'equal' field:lastModifiedBy"  )
	private String lastModifiedBy;
	
	@Schema(description = "Field is between two values (inclusive). field:lastModifiedDate"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime lastModifiedDateFrom;
	
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime lastModifiedDateTo;
	
	
	private String sortCode;
	
	@Builder.Default
	private int pageNumber=0;
	
	@Builder.Default
	private int pageSize=25;

    private String sortColumn; // Column to sort by
    private Boolean sortAscending; // Sort order (true: ascending, false: descending)
}
