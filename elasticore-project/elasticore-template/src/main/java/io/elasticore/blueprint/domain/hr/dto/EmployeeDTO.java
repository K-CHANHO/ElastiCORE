//ecd:-805744472H20260207233343_V1.0
package io.elasticore.blueprint.domain.hr.dto;

import io.elasticore.blueprint.domain.hr.enums.*;
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
 * EmployeeDTO
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
public  class EmployeeDTO  implements java.io.Serializable  {

	@Schema(description = "deptName"  )
	private String deptName;
	
	@Schema(description = "deptCode"  )
	private String deptCode;
	
	@Schema(description = "managerName"  )
	private String managerName;
	
	@Schema(description = "empId"  )
	@Size(max=36)
	private String empId;
	
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
	
	@Schema(description = "hireDate" , requiredMode=Schema.RequiredMode.REQUIRED , example="yyyy-MM-dd")
	@NotNull
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
	private java.time.LocalDate hireDate;
	
	@Schema(description = "salary" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	private java.math.BigDecimal salary;
	
	@Schema(description = "bonusRate"  )
	@jakarta.validation.constraints.Min(0)
	@jakarta.validation.constraints.Max(100)
	@Builder.Default
	private Double bonusRate = 0.0;
	
	@Schema(description = "active"  )
	@Builder.Default
	private Boolean active = true;
	
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
	
	@Schema(description = "version"  )
	private Long version;
	

}
