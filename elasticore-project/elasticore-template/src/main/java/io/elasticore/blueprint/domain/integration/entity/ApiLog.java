//ecd:-1792772792H20260414094512_V2.1
package io.elasticore.blueprint.domain.integration.entity;

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
import io.elasticore.blueprint.domain.shop.enums.OrderStatus;
import jakarta.persistence.Entity;
import io.elasticore.blueprint.domain.shop.entity.AuditableWithEnum;


/**
 * ApiLog
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@jakarta.persistence.Table(name="intg_api_log")
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class ApiLog extends AuditableWithEnum implements java.io.Serializable  {

	public ApiLog(Long logId) {
	    this.logId = logId;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "log_id")
	private Long logId;
	
	
	@Column(name = "endpoint", nullable = false, length = 500)
	private String endpoint;
	
	
	@Column(name = "method", nullable = false, length = 10)
	private String method;
	
	
	@Column(name = "request_body", columnDefinition = "TEXT")
	private String requestBody;
	
	
	@Column(name = "response_body", columnDefinition = "TEXT")
	private String responseBody;
	
	
	@Column(name = "status_code")
	private Integer statusCode;
	
	
	@Column(name = "duration")
	private Long duration;
	
	
	@Column(name = "created_at")
	private java.time.LocalDateTime createdAt;
	
	
	@Column(length = 3)
	@Convert(converter = OrderStatus.EntityConverter.class)
	private OrderStatus relatedOrderStatus;
	
	
}
