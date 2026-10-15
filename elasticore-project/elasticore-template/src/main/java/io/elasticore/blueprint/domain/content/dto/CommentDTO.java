//ecd:153064649H20260207233342_V1.0
package io.elasticore.blueprint.domain.content.dto;

import io.elasticore.blueprint.domain.content.enums.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.time.*;
import io.elasticore.blueprint.domain.content.dto.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * CommentDTO
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
public  class CommentDTO  implements java.io.Serializable  {

	@Schema(description = "articleTitle"  )
	private String articleTitle;
	
	@Schema(description = "replies"  )
	private List<CommentDTO> replies;
	
	@Schema(description = "commentId"  )
	private Long commentId;
	
	@Schema(description = "content" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	private String content;
	
	@Schema(description = "authorName" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=100)
	private String authorName;
	
	@Schema(description = "authorEmail"  )
	@Size(max=100)
	private String authorEmail;
	
	@Schema(description = "approved"  )
	@Builder.Default
	private Boolean approved = false;
	
	@Schema(description = "createdBy"  )
	@Size(max=50)
	private String createdBy;
	
	@Schema(description = "createDate"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime createDate;
	
	@Schema(description = "lastModifiedBy"  )
	@Size(max=50)
	private String lastModifiedBy;
	
	@Schema(description = "lastModifiedDate"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime lastModifiedDate;
	

}
