//ecd:-2083384709H20260207233344_V1.0
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



/**
 * Address
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@jakarta.persistence.Table(name="shop_address")
@org.hibernate.annotations.DynamicUpdate
@Embeddable
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Address  implements java.io.Serializable  {

	@Column(name = "street", length = 200)
	private String street;
	
	
	@Column(name = "city", length = 100)
	private String city;
	
	
	@Column(name = "state", length = 50)
	private String state;
	
	
	@Column(name = "zip_code", length = 10)
	private String zipCode;
	
	
	@Column(name = "country", length = 50)
	private String country = "US";
	
	
}
