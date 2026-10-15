//ecd:-1197486248H20260207233344_V1.0
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
import io.elasticore.blueprint.domain.shop.enums.CustomerType;
import io.elasticore.blueprint.domain.shop.entity.Address;
import io.elasticore.blueprint.domain.shop.entity.Order;
import io.elasticore.blueprint.domain.shop.entity.VipProfile;
import jakarta.persistence.Entity;


/**
 * Customer
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="shop_customer",
  indexes = {
    @jakarta.persistence.Index(name="idx_shop_cust_email", columnList="email", unique=true)
  }
  )
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Customer extends AuditEntity implements java.io.Serializable  {

	public Customer(Long customerId) {
	    this.customerId = customerId;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "customer_id")
	private Long customerId;
	
	
	@Column(name = "first_name", nullable = false, length = 50)
	private String firstName;
	
	
	@Column(name = "last_name", nullable = false, length = 50)
	private String lastName;
	
	
	@Column(name = "email", nullable = false, length = 100)
	private String email;
	
	
	@Column(name = "phone", length = 20)
	private String phone;
	
	
	@Column(name = "date_of_birth")
	private java.time.LocalDate dateOfBirth;
	
	
	@Column(name = "loyalty_points")
	private Long loyaltyPoints = 0L;
	
	
	@Column(length = 1)
	@Convert(converter = CustomerType.EntityConverter.class)
	private CustomerType customerType;
	
	
	@Embedded
	private Address billingAddress;
	
	
	@OneToOne(cascade = CascadeType.ALL)
	private VipProfile vipProfile;
	
	
	@OneToMany(fetch = FetchType.LAZY ,mappedBy="customer")
	private List<Order> orders;
	
	
}
