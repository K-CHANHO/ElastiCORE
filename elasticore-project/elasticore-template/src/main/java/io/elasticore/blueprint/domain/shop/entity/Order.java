//ecd:-681169026H20260211211138_V1.0
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
import java.math.BigDecimal;
import org.hibernate.envers.NotAudited;
import io.elasticore.blueprint.domain.shop.entity.OrderItem;
import io.elasticore.blueprint.domain.shop.entity.Customer;
import io.elasticore.blueprint.domain.shop.entity.Address;
import io.elasticore.blueprint.domain.shop.enums.PaymentMethod;
import io.elasticore.blueprint.domain.shop.enums.OrderStatus;
import jakarta.persistence.Entity;


/**
 * Order
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Audited
@Entity
@jakarta.persistence.Table(name="shop_order",
  indexes = {
    @jakarta.persistence.Index(name="idx_shop_ord_date", columnList="order_date")
  }
  )
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Order extends AuditEntity implements java.io.Serializable  {

	public Order(String orderId) {
	    this.orderId = orderId;
	}
	
	@Id
	@Column(name = "order_id", length = 36)
	private String orderId;
	
	
	@PrePersist
	public void prePersist() {
	  if (orderId == null)
	        orderId = io.elasticore.springboot3.util.IdUtils.newId(36);
	}
	
	
	@Column(name = "order_date", nullable = false)
	private java.time.LocalDateTime orderDate;
	
	
	@Column(name = "total_amount", precision = 14, scale = 2)
	private java.math.BigDecimal totalAmount = BigDecimal.ZERO;
	
	
	@Column(length = 3)
	@Convert(converter = OrderStatus.EntityConverter.class)
	private OrderStatus status;
	
	
	@Column(length = 2)
	@Convert(converter = PaymentMethod.EntityConverter.class)
	private PaymentMethod paymentMethod;
	
	
	@ManyToOne
	@NotAudited
	private Customer customer;
	
	
	@OneToMany(fetch = FetchType.LAZY ,cascade = CascadeType.ALL,mappedBy="order")
	@NotAudited
	private List<OrderItem> items;
	
	
	@Embedded
	private Address shippingAddress;
	
	
	@Column(name = "notes", columnDefinition = "TEXT")
	private String notes;
	
	
	@Column(name = "tracking_number", length = 50)
	private String trackingNumber;
	
	
	@Column(name = "version")
	@Version
	private Long version;
	
	
}
