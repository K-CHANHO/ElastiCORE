//ecd:-441213192H20260414181942_V2.1
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
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import io.elasticore.blueprint.domain.content.entity.Attachment;
import io.elasticore.blueprint.domain.content.entity.Tag;
import io.elasticore.blueprint.domain.content.entity.ContentCategory;
import io.elasticore.blueprint.domain.content.enums.ArticleStatus;
import jakarta.persistence.Entity;


/**
 * Article
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Audited
@Entity
@jakarta.persistence.Table(name="cms_article",
  indexes = {
    @jakarta.persistence.Index(name="idx_cms_art_slug", columnList="slug", unique=true)
   ,@jakarta.persistence.Index(name="idx_cms_art_pub_date", columnList="publish_date")
  }
  )
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Article extends ContentBase implements java.io.Serializable  {

	public Article(String articleId) {
	    this.articleId = articleId;
	}
	
	@Id
	@Column(name = "article_id", length = 36)
	private String articleId;
	
	
	@PrePersist
	public void prePersist() {
	  if (articleId == null)
	        articleId = io.elasticore.springboot3.util.IdUtils.newId(36);
	}
	
	
	@Column(name = "title", nullable = false, length = 300)
	private String title;
	
	
	@Column(name = "slug", nullable = false, length = 300)
	private String slug;
	
	
	@Column(name = "summary", length = 500)
	private String summary;
	
	
	@Column(name = "body", columnDefinition = "TEXT")
	private String body;
	
	
	@Column(name = "body_text", columnDefinition = "TEXT")
	private String bodyText;
	
	
	@Column(name = "publish_date")
	private java.time.LocalDateTime publishDate;
	
	
	@Column(name = "view_count")
	private Long viewCount = 0L;
	
	
	@Column(name = "featured")
	private Boolean featured = false;
	
	
	@Column(length = 3)
	@Convert(converter = ArticleStatus.EntityConverter.class)
	private ArticleStatus articleStatus;
	
	
	@ManyToOne
	@NotAudited
	private ContentCategory category;
	
	
	@ManyToMany(fetch = FetchType.LAZY ,cascade = CascadeType.ALL)
	@NotAudited
	private List<Tag> tags;
	
	
	@OneToMany(fetch = FetchType.LAZY ,cascade = CascadeType.ALL,mappedBy="article")
	@NotAudited
	private List<Attachment> attachments;
	
	
	@Column(name = "ratings")
	@Convert(converter = io.elasticore.springboot3.util.IntegerArrayConverter.class)
	private Integer[] ratings;
	
	
	@Column(name = "labels")
	@Convert(converter = io.elasticore.springboot3.util.StringArrayConverter.class)
	private String[] labels;
	
	
}
