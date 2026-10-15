//ecd:282989628H20260408121433_V${version}
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
import io.elasticore.blueprint.domain.content.entity.Comment;
import io.elasticore.blueprint.domain.content.entity.Article;
import jakarta.persistence.Entity;


/**
 * Comment
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="cms_comment")
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Comment extends ContentBase implements java.io.Serializable  {

	public Comment(Long commentId) {
	    this.commentId = commentId;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id")
	private Long commentId;
	
	
	@Column(name = "content", columnDefinition = "TEXT", nullable = false)
	private String content;
	
	
	@Column(name = "author_name", nullable = false, length = 100)
	private String authorName;
	
	
	@Column(name = "author_email", length = 100)
	private String authorEmail;
	
	
	@Column(name = "approved")
	private Boolean approved = false;
	
	
	@ManyToOne
	private Article article;
	
	
	@ManyToOne
	@JoinColumn(name = "test_id" )
	private Comment parentComment;
	
	
	@OneToMany(fetch = FetchType.LAZY ,mappedBy="parentComment")
	private List<Comment> replies;
	
	
}
