//ecd:-1083753898H20260926235721_V3.0
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
import jakarta.persistence.Entity;


/**
 * Coupon
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="shop_coupon")
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Coupon  implements java.io.Serializable  {

	public Coupon(String couponCode) {
	    this.couponCode = couponCode;
	}
	
	@Id
	@Column(name = "coupon_code", length = 20)
	private String couponCode;
	
	
	@Column(name = "title", nullable = false, length = 100)
	private String title;
	
	
	@Column(name = "discount_rate")
	private Integer discountRate;
	
	
}
