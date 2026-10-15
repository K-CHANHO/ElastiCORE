//ecd:-554294734H20260207233344_V1.0
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
import io.elasticore.blueprint.domain.shop.entity.Customer;
import jakarta.persistence.Entity;


/**
 * VipProfile
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="shop_vip_profile")
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class VipProfile  implements java.io.Serializable  {

	public VipProfile(Long profileId) {
	    this.profileId = profileId;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "profile_id")
	private Long profileId;
	
	
	@Column(name = "tier", length = 20)
	private String tier = "SILVER";
	
	
	@Column(name = "discount_rate")
	private Double discountRate = 0.0;
	
	
	@Column(name = "enroll_date")
	private java.time.LocalDate enrollDate;
	
	
	@OneToOne
	private Customer customer;
	
	
}
