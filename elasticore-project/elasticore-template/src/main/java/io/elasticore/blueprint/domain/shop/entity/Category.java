//ecd:201525161H20260207233344_V1.0
package io.elasticore.blueprint.domain.shop.entity;

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
import io.elasticore.blueprint.domain.shop.entity.Category;
import jakarta.persistence.Entity;


/**
 * Category
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="shop_category",
  indexes = {
    @jakarta.persistence.Index(name="idx_shop_cat_name", columnList="name")
  }
  )
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Category extends AuditEntity implements java.io.Serializable  {

	public Category(Long catId) {
	    this.catId = catId;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cat_id")
	private Long catId;
	
	
	@Column(name = "name", nullable = false, length = 100)
	private String name;
	
	
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;
	
	
	@Column(name = "sort_order")
	private Integer sortOrder = 0;
	
	
	@Column(name = "active")
	private Boolean active = true;
	
	
	@ManyToOne
	private Category parentCategory;
	
	
	@OneToMany(fetch = FetchType.LAZY ,mappedBy="parentCategory")
	private List<Category> subCategories;
	
	
}
