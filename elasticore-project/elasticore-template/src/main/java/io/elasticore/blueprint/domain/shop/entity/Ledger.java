//ecd:393354262H20261028193834_V3.0
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
 * Ledger
 * ledger, kept by soft delete
 * 
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="shop_ledger", schema="acct")
@org.hibernate.annotations.DynamicUpdate
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Ledger  implements java.io.Serializable  {

	public Ledger(Long ledgerId) {
	    this.ledgerId = ledgerId;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ledger_id")
	private Long ledgerId;
	
	
	@Column(name = "code", nullable = false, length = 13)
	private String code;
	
	
	@Column(name = "memo", length = 200)
	private String memo;
	
	
	@Column(name = "amount", nullable = false)
	private Long amount;
	
	
	@Column(name = "posted_at")
	private java.time.LocalDateTime postedAt;
	
	
	@ManyToOne
	@JoinColumn(nullable = false)
	private Customer owner;
	
	
	@ManyToOne
	private Customer partner;
	
	
	@Column(name = "deleted")
	private Boolean deleted = false;
	
	
}
