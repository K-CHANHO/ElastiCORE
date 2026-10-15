//ecd:1409198478H20260207233344_V1.0
package io.elasticore.blueprint.domain.integration.entity;

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
import io.elasticore.blueprint.domain.integration.enums.SyncStatus;
import jakarta.persistence.Entity;


/**
 * SyncRecord
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="intg_sync_record")
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class SyncRecord  implements java.io.Serializable  {

	public SyncRecord(String recordId) {
	    this.recordId = recordId;
	}
	
	@Id
	@Column(name = "record_id", length = 36)
	private String recordId;
	
	
	@PrePersist
	public void prePersist() {
	  if (recordId == null)
	        recordId = io.elasticore.springboot3.util.IdUtils.newId(36);
	}
	
	
	@Column(name = "source_system", nullable = false, length = 50)
	private String sourceSystem;
	
	
	@Column(name = "target_system", nullable = false, length = 50)
	private String targetSystem;
	
	
	@Column(name = "entity_type", length = 50)
	private String entityType;
	
	
	@Column(name = "entity_id", length = 100)
	private String entityId;
	
	
	@Column(length = 3)
	@Convert(converter = SyncStatus.EntityConverter.class)
	private SyncStatus syncStatus;
	
	
	@Column(name = "sync_date")
	private java.time.LocalDateTime syncDate;
	
	
	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage;
	
	
	@Column(name = "retry_count")
	private Integer retryCount = 0;
	
	
}
