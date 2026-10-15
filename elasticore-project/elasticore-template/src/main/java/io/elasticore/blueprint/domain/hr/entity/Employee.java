//ecd:451627532H20260301010751_V${version}
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
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import io.elasticore.blueprint.domain.hr.entity.Skill;
import io.elasticore.blueprint.domain.hr.entity.Employee;
import io.elasticore.blueprint.domain.hr.entity.Department;
import jakarta.persistence.Entity;


/**
 * Employee
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Audited
@Entity
@jakarta.persistence.Table(name="hr_employee",
  indexes = {
    @jakarta.persistence.Index(name="idx_hr_emp_email", columnList="email", unique=true)
   ,@jakarta.persistence.Index(name="idx_hr_emp_dept", columnList="department_dept_id")
  }
  )
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Employee extends HrAuditEntity implements java.io.Serializable  {

	public Employee(String empId) {
	    this.empId = empId;
	}
	
	@Id
	@Column(name = "emp_id", length = 36)
	private String empId;
	
	
	@PrePersist
	public void prePersist() {
	  if (empId == null)
	        empId = io.elasticore.springboot3.util.IdUtils.newId(36);
	}
	
	
	@Column(name = "first_name", nullable = false, length = 50)
	private String firstName;
	
	
	@Column(name = "last_name", nullable = false, length = 50)
	private String lastName;
	
	
	@Column(name = "email", nullable = false, length = 100)
	private String email;
	
	
	@Column(name = "phone", length = 20)
	private String phone;
	
	
	@Column(name = "hire_date", nullable = false)
	private java.time.LocalDate hireDate;
	
	
	@Column(name = "salary", nullable = false, precision = 12, scale = 2)
	private java.math.BigDecimal salary;
	
	
	@Column(name = "bonus_rate")
	private Double bonusRate = 0.0;
	
	
	@Column(name = "active")
	private Boolean active = true;
	
	
	@ManyToOne
	@NotAudited
	private Department department;
	
	
	@ManyToMany(fetch = FetchType.LAZY ,cascade = CascadeType.ALL)
	@NotAudited
	private List<Skill> skills;
	
	
	@ManyToOne
	private Employee manager;
	
	
	@OneToMany(fetch = FetchType.LAZY ,mappedBy="manager")
	@NotAudited
	private List<Employee> directReports;
	
	
}
