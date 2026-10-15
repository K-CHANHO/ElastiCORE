//ecd:-2144189712H20260328122051_V2.0
package io.elasticore.blueprint.domain.jsonview_demo.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.time.*;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * MemberDTO
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public  class MemberDTO  implements java.io.Serializable  {

	@Schema(description = "id"  )
	@JsonView(Views.Read.class)
	private Long id;
	
	@Schema(description = "username" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=50)
	@JsonView({Views.Create.class, Views.Read.class})
	private String username;
	
	@Schema(description = "email" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=100)
	@JsonView({Views.Create.class, Views.Read.class, Views.Update.class})
	private String email;
	
	@Schema(description = "nickname"  )
	@Size(max=50)
	@JsonView({Views.Create.class, Views.Read.class, Views.Update.class})
	private String nickname;
	
	@Schema(description = "role"  )
	@Size(max=20)
	@JsonView(Views.Read.class)
	@Builder.Default
	private String role = "USER";
	
	@Schema(description = "loginCount"  )
	@JsonView(Views.Read.class)
	@Builder.Default
	private Integer loginCount = 0;
	
	@Schema(description = "lastLoginAt"  , example="yyyy-MM-dd HH:mm:ss")
	@JsonView(Views.Read.class)
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime lastLoginAt;
	
	@Schema(description = "bio"  )
	@Size(max=500)
	@JsonView({Views.Create.class, Views.Update.class})
	private String bio;
	
	@Schema(description = "createdAt"  , example="yyyy-MM-dd HH:mm:ss")
	@JsonView(Views.Read.class)
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime createdAt;
	

}
