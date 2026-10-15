//ecd:-1623036198H20260301235721_V2.0
package io.elasticore.blueprint.domain.content.service;

import io.elasticore.blueprint.domain.content.entity.ContentCategory;
import io.elasticore.blueprint.domain.content.dto.*;
import io.elasticore.blueprint.domain.content.repository.*;
import io.elasticore.springboot3.mapper.MappingContext;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.EntityManager;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.lang.reflect.Field;



/**
 * Comprehensive service layer for managing ContentCategory entities.
 *
 * Provides full CRUD operations, dynamic search with specification support,
 * pagination, sorting, and advanced query functions such as min/max/sum.
 * Delegates repository operations via ContentRepositoryHelper.
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ContentCategoryCoreService {

    protected final ContentRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<ContentCategory> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all ContentCategory entities, converts them to ContentCategoryDTO objects, and returns them as a list.
     *
     * @return a list of ContentCategoryDTO objects
     */
    public List<ContentCategoryDTO> findAll() {
        return helper.getContentCategory().findAll().stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tContentCategory entities, converts them to ContentCategoryDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of ContentCategoryDTO objects
     */
    public List<ContentCategoryDTO> findAll(Sort sort) {
        return helper.getContentCategory().findAll(sort).stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes ContentCategory entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(ContentCategorySrchDTO searchDTO) {
        Specification<ContentCategory> specification = ContentMapper.toSpec(searchDTO);
        return helper.getContentCategory().delete(specification);
    }




    /**
     * Finds a list of ContentCategory entities that match the given search criteria
     * and returns them as a list of ContentCategoryDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching ContentCategoryDTO objects
     */
    public List<ContentCategoryDTO> listBySearch(ContentCategorySrchDTO searchDTO) {
        Specification<ContentCategory> specification = ContentMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getContentCategory().findAll(specification).stream()
                        
                        .map(ContentMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getContentCategory().findAll(specification, sort).stream()
                
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds ContentCategory entities that match the given search criteria and returns them as a paginated list of ContentCategoryDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of ContentCategoryDTO objects
     */
    public Page<ContentCategoryDTO> findBySearch(ContentCategorySrchDTO searchDTO) {
        Specification<ContentCategory> specification = ContentMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<ContentCategory> result = helper.getContentCategory().findAll(specification, pageable);
        return result.map(ContentMapper::toDTO);
    }

    /**
     * Finds the first ContentCategory entity that matches the given search criteria
     * and returns it as a ContentCategoryDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching ContentCategoryDTO object, or {@code null} if no match is found
     */
    public ContentCategoryDTO findFirstBySearch(ContentCategorySrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<ContentCategoryDTO> pages= findBySearch(searchDTO);
        List<ContentCategoryDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of ContentCategory entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(ContentCategorySrchDTO searchDTO) {
        Specification<ContentCategory> specification = ContentMapper.toSpec(searchDTO);
        return helper.getContentCategory().count(specification);
    }
    /**
     * Finds a ContentCategory entity by its ID and converts it to a ContentCategoryDTO.
     *
     * @param categoryId the ID of the FaxResult entity
     * @return an Optional containing the ContentCategoryDTO if found, or an empty Optional if not found
     */
    public Optional<ContentCategoryDTO> findById(Long categoryId) {
        return helper.getContentCategory().findById(categoryId).map(e->ContentMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new ContentCategory entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the ContentCategoryDTO to save
     * @return the saved ContentCategoryDTO
     */
    @Transactional
    public ContentCategoryDTO save(ContentCategoryDTO dto) {
        ContentCategory entity = ContentMapper.toEntity(dto, helper.getEntityManager());
        ContentCategory result = helper.getContentCategory().save(entity);
        return ContentMapper.toDTO(result);
    }


    /**
     * Updates an existing ContentCategory entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the ContentCategoryDTO with updated information
     * @return the updated ContentCategoryDTO
     */
    @Transactional
    public ContentCategoryDTO update(ContentCategoryDTO dto) {
        ContentCategory entity = helper.getContentCategory().findById(dto.getCategoryId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid ContentCategoryDTO ID");
        ContentMapper.mapping(dto, entity, true, helper.getEntityManager());

        ContentCategory result = helper.getContentCategory().save(entity);
        return ContentMapper.toDTO(result);
    }

    /**
     * Batch updates multiple ContentCategory entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of ContentCategoryDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<ContentCategoryDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (ContentCategoryDTO dto : dtoList) {
            ContentCategory entity = helper.getContentCategory().findById(dto.getCategoryId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid ContentCategoryDTO ID: " + dto.getCategoryId());
            }
            ContentMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getContentCategory().save(entity);
        }
    }

    /**
     * Deletes a list of ContentCategory entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of ContentCategory representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<ContentCategoryKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (ContentCategoryKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getCategoryId()));
        }
        return result;
    }


    /**
     * Deletes a ContentCategory entity by its ID.
     *
     * @param categoryId the ID of the ContentCategory entity to delete
     * @return true if the ContentCategory entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long categoryId) {
        helper.getContentCategory().deleteById(categoryId);
        return !helper.getContentCategory().existsById(categoryId);
    }





    /**
     * Updates Message entities that match the given search criteria,
     * using non-null fields from the update DTO.
     *
     * @param dto     DTO containing the fields to update (only non-null fields will be applied)
     * @param srchDTO DTO containing search conditions for filtering target entities
     * @return number of records updated
     */
    @Transactional
    public int updateBySpecification(ContentCategoryDTO dto, ContentCategorySrchDTO srchDTO) {
        return updateBySpecification(dto, ContentMapper.toSpec(srchDTO));
    }

    /**
     * Performs a conditional update using the provided Specification and non-null fields from the DTO.
     * Prevents full table updates by requiring at least one condition.
     *
     * @param dto  DTO containing the fields to update (only non-null fields will be applied)
     * @param spec JPA Specification defining update conditions
     * @return number of records updated
     * @throws IllegalArgumentException if no condition is provided
     */
    @Transactional
    public int updateBySpecification(ContentCategoryDTO dto, Specification<ContentCategory> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<ContentCategory> update = cb.createCriteriaUpdate(ContentCategory.class);
        Root<ContentCategory> root = update.from(ContentCategory.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(ContentCategory.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
