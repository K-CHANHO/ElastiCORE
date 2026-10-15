//ecd:-1934090139H20260328122051_V2.0
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
 * BlogPostDTO
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
public  class BlogPostDTO  implements java.io.Serializable  {

	@Schema(description = "id"  )
	@JsonView({Views.Update.class, Views.Read.class})
	private Long id;
	
	@Schema(description = "title" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=200)
	private String title;
	
	@Schema(description = "content"  )
	private String content;
	
	@Schema(description = "summary"  )
	@Size(max=500)
	private String summary;
	
	@Schema(description = "status"  )
	@Size(max=20)
	@Builder.Default
	private String status = "DRAFT";
	
	@Schema(description = "viewCount"  )
	@JsonView({Views.Create.class, Views.Read.class})
	@Builder.Default
	private Integer viewCount = 0;
	
	@Schema(description = "author" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=100)
	@JsonView({Views.Create.class, Views.Read.class})
	private String author;
	
	@Schema(description = "publishedAt"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime publishedAt;
	
	@Schema(description = "modifiedAt"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime modifiedAt;
	

}
