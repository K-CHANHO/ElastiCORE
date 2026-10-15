//ecd:-460902743H20260416172730_V2.2
package io.elasticore.blueprint.domain.hr.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import java.util.*;
import java.time.*;
import io.elasticore.blueprint.domain.hr.enums.DeptStatus;
import jakarta.persistence.Entity;


/**
 * Department
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="hr_department",
  indexes = {
    @jakarta.persistence.Index(name="idx_hr_dept_code", columnList="code", unique=true)
  }
  )
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Department extends HrAuditEntity implements java.io.Serializable  {

	public Department(Long deptId) {
	    this.deptId = deptId;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "dept_id")
	private Long deptId;
	
	
	@Column(name = "code", nullable = false, length = 10)
	private String code;
	
	
	@Column(name = "name", nullable = false, length = 100)
	private String name;
	
	
	@Column(name = "name_insensitive", length = 100)
	private String nameInsensitive;
	
	
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;
	
	
	@Column(name = "budget", precision = 14, scale = 2)
	private java.math.BigDecimal budget;
	
	
	@Column(name = "head_count")
	private Integer headCount = 0;
	
	
	@Column(name = "max_capacity")
	private Integer maxCapacity = 100;
	
	
	@Column(name = "floor_number")
	private Integer floorNumber;
	
	
	@Column(name = "min_salary", precision = 10, scale = 2)
	private java.math.BigDecimal minSalary;
	
	
	@Column(length = 1)
	@Convert(converter = DeptStatus.EntityConverter.class)
	private DeptStatus status;
	
	
	@Column(name = "name_prefix", length = 50)
	private String namePrefix;
	
	
	@Column(name = "name_suffix", length = 50)
	private String nameSuffix;
	
	
	@Column(name = "name_prefix_ci", length = 50)
	private String namePrefixCI;
	
	
	@Column(name = "name_suffix_ci", length = 50)
	private String nameSuffixCI;
	
	
	@Column(name = "name_exact_ci", length = 50)
	private String nameExactCI;
	
	
	@Column(name = "active")
	private Boolean active = true;
	
	
	@Column(name = "internal_code", length = 30)
	private String internalCode;
	
	
	@Column(name = "external_code", length = 30)
	private String externalCode;
	
	
	@Column(name = "legacy_code", length = 30)
	private String legacyCode;
	
	
}
