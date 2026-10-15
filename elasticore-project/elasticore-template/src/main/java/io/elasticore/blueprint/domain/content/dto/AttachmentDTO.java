//ecd:-1100109518H20260207233342_V1.0
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * AttachmentDTO
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
public  class AttachmentDTO  implements java.io.Serializable  {

	@Schema(description = "articleId"  )
	private String articleId;
	
	@Schema(description = "attachId"  )
	private Long attachId;
	
	@Schema(description = "fileName" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=255)
	private String fileName;
	
	@Schema(description = "fileSize"  )
	@jakarta.validation.constraints.Min(0)
	private Long fileSize;
	
	@Schema(description = "contentType"  )
	@Size(max=100)
	private String contentType;
	
	@Schema(description = "storageKey"  )
	@Size(max=500)
	private String storageKey;
	

}
