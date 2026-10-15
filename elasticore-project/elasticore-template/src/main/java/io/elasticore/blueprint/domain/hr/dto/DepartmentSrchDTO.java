//ecd:856220230H20260416172730_V2.2
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


/**
 * DepartmentSrchDTO
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
public  class DepartmentSrchDTO  implements java.io.Serializable, SortableObject, PageableObject  {

	@Schema(description = "Field matches any value in the given list. field:status"  , example="A: Active | I: Inactive | M: Merged")
	private DeptStatus status;
	
	@Schema(description = "Field equals value. field:deptId"  )
	private Long deptId;
	
	@Schema(description = "Field equals value. field:code"  )
	private String code;
	
	@Schema(description = "Field matches pattern. field:name"  )
	private String name;
	
	@Schema(description = "Field matches value (case-insensitive) with identical content between start and end. field:nameInsensitive"  )
	private String nameInsensitive;
	
	@Schema(description = "Use 'like' if value has %, else 'equal' field:description"  )
	private String description;
	
	@Schema(description = "Field is between two values (inclusive). field:budget"  )
	private java.math.BigDecimal budgetFrom;
	
	private java.math.BigDecimal budgetTo;
	
	@Schema(description = "Field is greater than or equal to value. field:headCount"  )
	private Integer headCount;
	
	@Schema(description = "Field is less than or equal to value. field:maxCapacity"  )
	private Integer maxCapacity;
	
	@Schema(description = "Field is greater than value. field:floorNumber"  )
	private Integer floorNumber;
	
	@Schema(description = "Field is less than value. field:minSalary"  )
	private java.math.BigDecimal minSalary;
	
	@Schema(description = "Field matches value with the leading part being identical. field:namePrefix"  )
	private String namePrefix;
	
	@Schema(description = "Field matches value with the trailing part being identical. field:nameSuffix"  )
	private String nameSuffix;
	
	@Schema(description = "Field matches value (case-insensitive) with the leading part being identical. field:namePrefixCI"  )
	private String namePrefixCI;
	
	@Schema(description = "Field matches value (case-insensitive) with the trailing part being identical. field:nameSuffixCI"  )
	private String nameSuffixCI;
	
	@Schema(description = "Field equals value(case-insensitive). field:nameExactCI"  )
	private String nameExactCI;
	
	@Schema(description = "Field equals value. field:active"  )
	private Boolean active;
	
	@Schema(description = "Use 'like' if value has %, else 'equal' field:internalCode"  )
	private String internalCode;
	
	@Schema(description = "Use 'like' if value has %, else 'equal' field:externalCode"  )
	private String externalCode;
	
	@Schema(description = "Use 'like' if value has %, else 'equal' field:legacyCode"  )
	private String legacyCode;
	
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
	
	@Schema(description = "Field equals value. field:version"  )
	private Long version;
	
	
	private String sortCode;
	
	@Builder.Default
	private int pageNumber=0;
	
	@Builder.Default
	private int pageSize=20;

    private String sortColumn; // Column to sort by
    private Boolean sortAscending; // Sort order (true: ascending, false: descending)
}
