//ecd:1139278511H20260207233343_V1.0
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
 * SkillBase
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public abstract class SkillBase  implements java.io.Serializable  {

	@Column(name = "skill_name", nullable = false, length = 100)
	private String skillName;
	
	
	@Column(name = "proficiency")
	private Integer proficiency;
	
	
}
