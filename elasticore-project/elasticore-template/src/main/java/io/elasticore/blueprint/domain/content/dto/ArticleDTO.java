//ecd:-1375193215H20260207233342_V1.0
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
import io.elasticore.blueprint.domain.content.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * ArticleDTO
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
public  class ArticleDTO  implements java.io.Serializable  {

	@Schema(description = "categoryName"  )
	private String categoryName;
	
	@Schema(description = "categorySlug"  )
	private String categorySlug;
	
	@Schema(description = "attachments"  )
	private List<AttachmentDTO> attachments;
	
	@Schema(description = "articleId"  )
	@Size(max=36)
	private String articleId;
	
	@Schema(description = "title" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=300)
	private String title;
	
	@Schema(description = "slug" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=300)
	private String slug;
	
	@Schema(description = "summary"  )
	@Size(max=500)
	private String summary;
	
	@Schema(description = "publishDate"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime publishDate;
	
	@Schema(description = "viewCount"  )
	@jakarta.validation.constraints.Min(0)
	@Builder.Default
	private Long viewCount = 0L;
	
	@Schema(description = "featured"  )
	@Builder.Default
	private Boolean featured = false;
	
	@Schema(description = "articleStatus"  , example="DRF: Draft | REV: Review | PUB: Published | ARC: Archived")
	private ArticleStatus articleStatus;
	
	@Schema(description = "ratings"  )
	private Integer[] ratings;
	
	@Schema(description = "labels"  )
	private String[] labels;
	
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
