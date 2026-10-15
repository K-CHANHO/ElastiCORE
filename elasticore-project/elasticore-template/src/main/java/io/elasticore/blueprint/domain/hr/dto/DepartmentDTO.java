//ecd:569641071H20260416172730_V2.2
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
import io.elasticore.blueprint.domain.hr.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * DepartmentDTO
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
public  class DepartmentDTO  implements java.io.Serializable  {

	@Schema(description = "deptId"  )
	private Long deptId;
	
	@Schema(description = "code" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=10)
	private String code;
	
	@Schema(description = "name" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=100)
	private String name;
	
	@Schema(description = "nameInsensitive"  )
	@Size(max=100)
	private String nameInsensitive;
	
	@Schema(description = "description"  )
	private String description;
	
	@Schema(description = "budget"  )
	private java.math.BigDecimal budget;
	
	@Schema(description = "headCount"  )
	@Builder.Default
	private Integer headCount = 0;
	
	@Schema(description = "maxCapacity"  )
	@Builder.Default
	private Integer maxCapacity = 100;
	
	@Schema(description = "floorNumber"  )
	private Integer floorNumber;
	
	@Schema(description = "minSalary"  )
	private java.math.BigDecimal minSalary;
	
	@Schema(description = "status"  , example="A: Active | I: Inactive | M: Merged")
	private DeptStatus status;
	
	@Schema(description = "namePrefix"  )
	@Size(max=50)
	private String namePrefix;
	
	@Schema(description = "nameSuffix"  )
	@Size(max=50)
	private String nameSuffix;
	
	@Schema(description = "namePrefixCI"  )
	@Size(max=50)
	private String namePrefixCI;
	
	@Schema(description = "nameSuffixCI"  )
	@Size(max=50)
	private String nameSuffixCI;
	
	@Schema(description = "nameExactCI"  )
	@Size(max=50)
	private String nameExactCI;
	
	@Schema(description = "active"  )
	@Builder.Default
	private Boolean active = true;
	
	@Schema(description = "internalCode"  )
	@Size(max=30)
	private String internalCode;
	
	@Schema(description = "externalCode"  )
	@Size(max=30)
	private String externalCode;
	
	@Schema(description = "legacyCode"  )
	@Size(max=30)
	private String legacyCode;
	
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
