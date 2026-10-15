//ecd:1940643242H20261012161929_V3.0
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
import io.elasticore.blueprint.domain.content.entity.ContentCategory;
import jakarta.persistence.Entity;


/**
 * ContentCategory
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="cms_content_category")
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class ContentCategory extends ContentBase implements java.io.Serializable  {

	public ContentCategory(Long categoryId) {
	    this.categoryId = categoryId;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private Long categoryId;
	
	
	@Column(name = "name", nullable = false, length = 100)
	private String name;
	
	
	@Column(name = "slug", nullable = false, length = 100)
	private String slug;
	
	
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;
	
	
	@Column(name = "sort_order")
	private Integer sortOrder = 0;
	
	
	@ManyToOne
	private ContentCategory parent;
	
	
	@OneToMany(fetch = FetchType.LAZY ,mappedBy="parent")
	private List<ContentCategory> children;
	
	
	/*
	  하위 카테고리 수
	*/
	@Formula("(select count(*) from cms_content_category c where c.parent_category_id = category_id)")
	private Long childCount;
	
	
	/*
	  상위 카테고리명
	*/
	@Formula("(select p.name from cms_content_category p where p.category_id = parent_category_id)")
	private String parentName;
	
	
}
