//ecd:-2043153015H20260207233343_V1.0
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
import jakarta.persistence.Entity;


/**
 * ProjectAssignment
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="hr_project_assignment")
@org.hibernate.annotations.DynamicUpdate
@IdClass(ProjectAssignmentIdentity.class)
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class ProjectAssignment  implements java.io.Serializable  {

	public ProjectAssignment(String empId, String projectCode) {
	    this.empId = empId;
	    this.projectCode = projectCode;
	}
	
	@Id
	@Column(name = "emp_id", length = 36)
	private String empId;
	
	
	@Id
	@Column(name = "project_code", length = 20)
	private String projectCode;
	
	
	@Column(name = "role", length = 50)
	private String role;
	
	
	@Column(name = "assign_date")
	private java.time.LocalDate assignDate;
	
	
	@Column(name = "hours_allocated")
	private Integer hoursAllocated;
	
	
}
