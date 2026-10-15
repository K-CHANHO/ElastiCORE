//ecd:-1266612261H20260803220215_V2.2
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
 * BookmarkDTO
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
public  class BookmarkDTO  implements java.io.Serializable  {

	@Schema(description = "article" , accessMode = Schema.AccessMode.READ_ONLY )
	private ArticleDTO article;
	
	@Schema(description = "articleArticleId"  )
	private String articleArticleId;
	
	@Schema(description = "bookmarkId"  )
	private Long bookmarkId;
	
	@Schema(description = "memo"  )
	@Size(max=200)
	private String memo;
	

}
