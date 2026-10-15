//ecd:-1418837151H20260414094513_V2.1
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
import io.elasticore.blueprint.domain.shop.enums.PaymentMethod;


/**
 * AuditableWithEnum
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableWithEnum extends AuditEntity implements java.io.Serializable  {

	@Column(length = 2)
	@Convert(converter = PaymentMethod.EntityConverter.class)
	private PaymentMethod inheritedPaymentMethod;
	
	
}
