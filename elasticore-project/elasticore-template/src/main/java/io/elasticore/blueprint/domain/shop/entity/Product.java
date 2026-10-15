//ecd:-1353337720H20260509113822_V2.2
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
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import io.elasticore.blueprint.domain.shop.entity.Category;
import io.elasticore.blueprint.domain.shop.entity.OrderItem;
import jakarta.persistence.Entity;


/**
 * Product
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Audited
@Entity
@jakarta.persistence.Table(name="shop_shop_products",
  indexes = {
    @jakarta.persistence.Index(name="idx_shop_prod_sku", columnList="sku", unique=true)
  }
  )
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Product extends AuditEntity implements java.io.Serializable  {

	public Product(String productId) {
	    this.productId = productId;
	}
	
	@Id
	@Column(name = "product_id", length = 36)
	private String productId;
	
	
	@PrePersist
	public void prePersist() {
	  if (productId == null)
	        productId = io.elasticore.springboot3.util.IdUtils.newId(36);
	}
	
	
	@Column(name = "name", nullable = false, length = 200)
	private String name;
	
	
	@Column(name = "sku", nullable = false, length = 50)
	private String sku;
	
	
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;
	
	
	@Column(name = "price", nullable = false, precision = 12, scale = 2)
	private java.math.BigDecimal price;
	
	
	@Column(name = "cost_price", precision = 12, scale = 2)
	private java.math.BigDecimal costPrice;
	
	
	@Column(name = "stock_quantity")
	private Integer stockQuantity = 0;
	
	
	@Column(name = "weight")
	private Float weight;
	
	
	@Column(name = "rating")
	private Double rating = 0.0;
	
	
	@Column(name = "active")
	private Boolean active = true;
	
	
	@Column(name = "launch_date")
	private java.time.LocalDate launchDate;
	
	
	@Column(name = "last_restocked")
	private java.time.LocalDateTime lastRestocked;
	
	
	@Column(name = "uuid", length = 36)
	private String uuid;
	
	
	@Column(name = "metadata", columnDefinition = "TEXT")
	private String metadata;
	
	
	@Column(name = "thumbnail", length = 500)
	private String thumbnail;
	
	
	@Column(name = "manual_content", columnDefinition = "TEXT")
	private String manualContent;
	
	
	@Column(name = "metadata_raw", columnDefinition = "jsonb")
	@JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
	private String metadataRaw;
	
	
	@Column(name = "old_payload", columnDefinition = "json")
	@JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
	private String oldPayload;
	
	
	@Column(name = "custom_xml", columnDefinition = "xml")
	@JdbcTypeCode(org.hibernate.type.SqlTypes.SQLXML)
	private String customXml;
	
	
	@Column(name = "force_long", columnDefinition = "jsonb")
	@JdbcTypeCode(org.hibernate.type.SqlTypes.LONG32VARCHAR)
	private String forceLong;
	
	
	@Column(name = "tags")
	@Convert(converter = io.elasticore.springboot3.util.StringArrayConverter.class)
	private String[] tags;
	
	
	@ManyToOne
	@NotAudited
	private Category category;
	
	
	@OneToMany(fetch = FetchType.LAZY ,mappedBy="product")
	@NotAudited
	private List<OrderItem> orderItems;
	
	
}
