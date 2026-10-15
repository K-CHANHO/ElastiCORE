//ecd:-992671819H20260207233343_V1.0
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



/**
 * ProjectAssignmentIdentity

 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Embeddable
@lombok.EqualsAndHashCode
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class ProjectAssignmentIdentity  implements java.io.Serializable  {

	@Column(name = "emp_id", length = 36)
	private String empId;
	
	@Column(name = "project_code", length = 20)
	private String projectCode;
	
}
