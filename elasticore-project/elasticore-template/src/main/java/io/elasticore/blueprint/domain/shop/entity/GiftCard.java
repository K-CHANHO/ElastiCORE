//ecd:195755969H20260926235721_V3.0
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
 * GiftCard
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="shop_gift_card")
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class GiftCard  implements java.io.Serializable  {

	public GiftCard(String cardNo) {
	    this.cardNo = cardNo;
	}
	
	@Id
	@Column(name = "card_no", length = 20)
	private String cardNo;
	
	
	@Column(name = "balance", precision = 12, scale = 2)
	private java.math.BigDecimal balance;
	
	
}
