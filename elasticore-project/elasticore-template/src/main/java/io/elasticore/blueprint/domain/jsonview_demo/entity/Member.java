//ecd:1369106776H20260328122051_V2.0
package io.elasticore.blueprint.domain.jsonview_demo.entity;

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
 * Member
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Entity
@org.hibernate.annotations.DynamicUpdate
@Getter
@Setter
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
public  class Member  implements java.io.Serializable  {

	public Member(Long id) {
	    this.id = id;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	
	@Column(name = "username", nullable = false, length = 50)
	private String username;
	
	
	@Column(name = "password", length = 200)
	private String password;
	
	
	@Column(name = "email", nullable = false, length = 100)
	private String email;
	
	
	@Column(name = "nickname", length = 50)
	private String nickname;
	
	
	@Column(name = "role", length = 20)
	private String role = "USER";
	
	
	@Column(name = "login_count")
	private Integer loginCount = 0;
	
	
	@Column(name = "last_login_at")
	private java.time.LocalDateTime lastLoginAt;
	
	
	@Column(name = "bio", length = 500)
	private String bio;
	
	
	@Column(name = "created_at")
	private java.time.LocalDateTime createdAt;
	
	
}
