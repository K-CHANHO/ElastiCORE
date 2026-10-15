//ecd:949871689H20260207233343_V1.0
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
 * HrAuditEntity
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
public abstract class HrAuditEntity  implements java.io.Serializable  {

	@Column(name = "created_by", updatable = false, length = 50)
	private String createdBy;
	
	
	@Column(name = "create_date", updatable = false)
	private java.time.LocalDateTime createDate;
	
	
	@Column(name = "last_modified_by", length = 50)
	private String lastModifiedBy;
	
	
	@Column(name = "last_modified_date")
	private java.time.LocalDateTime lastModifiedDate;
	
	
	@Column(name = "version")
	@Version
	private Long version;
	
	
}
