//ecd:-1236906141H20260207233342_V1.0
package io.elasticore.blueprint.domain.content.entity;

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
import io.elasticore.blueprint.domain.content.entity.Article;
import jakarta.persistence.Entity;


/**
 * Attachment
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="cms_attachment")
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Attachment  implements java.io.Serializable  {

	public Attachment(Long attachId) {
	    this.attachId = attachId;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "attach_id")
	private Long attachId;
	
	
	@Column(name = "file_name", nullable = false, length = 255)
	private String fileName;
	
	
	@Column(name = "file_size")
	private Long fileSize;
	
	
	@Column(name = "content_type", length = 100)
	private String contentType;
	
	
	@Column(name = "storage_key", length = 500)
	private String storageKey;
	
	
	@ManyToOne
	private Article article;
	
	
}
