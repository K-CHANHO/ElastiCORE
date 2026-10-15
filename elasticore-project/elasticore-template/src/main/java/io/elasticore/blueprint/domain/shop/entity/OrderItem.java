//ecd:-140944765H20260301221734_V2.0
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
import java.math.BigDecimal;
import io.elasticore.blueprint.domain.shop.entity.Order;
import io.elasticore.blueprint.domain.shop.entity.Product;
import jakarta.persistence.Entity;


/**
 * OrderItem
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="shop_order_item")
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class OrderItem  implements java.io.Serializable  {

	public OrderItem(String orderItemId) {
	    this.orderItemId = orderItemId;
	}
	
	@Id
	@Column(name = "order_item_id", length = 36)
	private String orderItemId;
	
	
	@PrePersist
	public void prePersist() {
	  if (orderItemId == null)
	        orderItemId = io.elasticore.springboot3.util.IdUtils.newId(36);
	}
	
	
	@ManyToOne
	private Order order;
	
	
	@Column(name = "item_seq")
	private Integer itemSeq;
	
	
	@ManyToOne
	private Product product;
	
	
	@Column(name = "quantity", nullable = false)
	private Integer quantity;
	
	
	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
	private java.math.BigDecimal unitPrice;
	
	
	@Column(name = "discount", precision = 8, scale = 2)
	private java.math.BigDecimal discount = BigDecimal.ZERO;
	
	
}
