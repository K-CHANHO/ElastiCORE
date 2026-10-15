//ecd:-1156003860H20260509113822_V2.2
package io.elasticore.blueprint.domain.shop.dto;

import io.elasticore.blueprint.domain.shop.enums.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.time.*;
import io.elasticore.blueprint.domain.shop.dto.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * ProductDTO
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
public  class ProductDTO  implements java.io.Serializable  {

	@Schema(description = "categoryName"  )
	private String categoryName;
	
	@Schema(description = "categoryCatId"  )
	private Long categoryCatId;
	
	@Schema(description = "productId"  )
	@Size(max=36)
	private String productId;
	
	@Schema(description = "name" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=200)
	private String name;
	
	@Schema(description = "sku" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@Size(max=50)
	private String sku;
	
	@Schema(description = "description"  )
	private String description;
	
	@Schema(description = "price" , requiredMode=Schema.RequiredMode.REQUIRED )
	@NotNull
	@jakarta.validation.constraints.DecimalMin("0")
	private java.math.BigDecimal price;
	
	@Schema(description = "costPrice"  )
	@jakarta.validation.constraints.DecimalMin("0")
	private java.math.BigDecimal costPrice;
	
	@Schema(description = "stockQuantity"  )
	@jakarta.validation.constraints.Min(0)
	@Builder.Default
	private Integer stockQuantity = 0;
	
	@Schema(description = "weight"  )
	private Float weight;
	
	@Schema(description = "rating"  )
	@jakarta.validation.constraints.Min(0)
	@jakarta.validation.constraints.Max(5)
	@Builder.Default
	private Double rating = 0.0;
	
	@Schema(description = "active"  )
	@Builder.Default
	private Boolean active = true;
	
	@Schema(description = "launchDate"  , example="yyyy-MM-dd")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
	private java.time.LocalDate launchDate;
	
	@Schema(description = "lastRestocked"  , example="yyyy-MM-dd HH:mm:ss")
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.time.LocalDateTime lastRestocked;
	
	@Schema(description = "uuid"  )
	@Size(max=36)
	private String uuid;
	
	@Schema(description = "metadata"  )
	private String metadata;
	
	@Schema(description = "metadataRaw"  )
	private String metadataRaw;
	
	@Schema(description = "oldPayload"  )
	private String oldPayload;
	
	@Schema(description = "customXml"  )
	private String customXml;
	
	@Schema(description = "forceLong"  )
	private String forceLong;
	
	@Schema(description = "tags"  )
	private String[] tags;
	
	@Schema(description = "category"  )
	private CategoryDTO category;
	
	@Schema(description = "createdBy"  )
	@Size(max=50)
	private String createdBy;
	
	@Schema(description = "lastModifiedBy"  )
	@Size(max=50)
	private String lastModifiedBy;
	

}
