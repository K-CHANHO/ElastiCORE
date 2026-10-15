//ecd:1482004950H20260328122051_V2.0
package io.elasticore.blueprint.domain.jsonview_demo.entity;

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
 * BlogPost
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class BlogPost  implements java.io.Serializable  {

	public BlogPost(Long id) {
	    this.id = id;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	
	@Column(name = "title", nullable = false, length = 200)
	private String title;
	
	
	@Column(name = "content", columnDefinition = "TEXT")
	private String content;
	
	
	@Column(name = "summary", length = 500)
	private String summary;
	
	
	@Column(name = "status", length = 20)
	private String status = "DRAFT";
	
	
	@Column(name = "view_count", updatable = false)
	private Integer viewCount = 0;
	
	
	@Column(name = "author", nullable = false, updatable = false, length = 100)
	private String author;
	
	
	@Column(name = "published_at")
	private java.time.LocalDateTime publishedAt;
	
	
	@Column(name = "modified_at")
	private java.time.LocalDateTime modifiedAt;
	
	
}
