//ecd:-116857949H20261012161930_V3.0
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
 * ContentCategoryDTO
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
public  class ContentCategoryDTO  implements java.io.Serializable  {

	@Schema(description = "children"  )
	private List<ContentCategoryDTO> children;
	
	@Schema(description = "categoryId"  )
	private Long categoryId;
	
	@Schema(description = "name" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=100)
	private String name;
	
	@Schema(description = "slug" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=100)
	private String slug;
	
	@Schema(description = "description"  )
	private String description;
	
	@Schema(description = "sortOrder"  )
	@Builder.Default
	private Integer sortOrder = 0;
	
	/*
	  하위 카테고리 수
	*/
	@Schema(description = "하위 카테고리 수"  )
	private Long childCount;
	
	/*
	  상위 카테고리명
	*/
	@Schema(description = "상위 카테고리명"  )
	@Size(max=100)
	private String parentName;
	
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
