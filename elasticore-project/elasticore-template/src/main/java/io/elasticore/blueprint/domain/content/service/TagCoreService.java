//ecd:-1817142642H20260301235721_V2.0
package io.elasticore.blueprint.domain.content.service;

import io.elasticore.blueprint.domain.content.entity.Tag;
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
 * Comprehensive service layer for managing Tag entities.
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
public class TagCoreService {

    protected final ContentRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Tag> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all Tag entities, converts them to TagDTO objects, and returns them as a list.
     *
     * @return a list of TagDTO objects
     */
    public List<TagDTO> findAll() {
        return helper.getTag().findAll().stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tTag entities, converts them to TagDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of TagDTO objects
     */
    public List<TagDTO> findAll(Sort sort) {
        return helper.getTag().findAll(sort).stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Tag entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(TagSrchDTO searchDTO) {
        Specification<Tag> specification = ContentMapper.toSpec(searchDTO);
        return helper.getTag().delete(specification);
    }




    /**
     * Finds a list of Tag entities that match the given search criteria
     * and returns them as a list of TagDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching TagDTO objects
     */
    public List<TagDTO> listBySearch(TagSrchDTO searchDTO) {
        Specification<Tag> specification = ContentMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getTag().findAll(specification).stream()
                        
                        .map(ContentMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getTag().findAll(specification, sort).stream()
                
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Tag entities that match the given search criteria and returns them as a paginated list of TagDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of TagDTO objects
     */
    public Page<TagDTO> findBySearch(TagSrchDTO searchDTO) {
        Specification<Tag> specification = ContentMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Tag> result = helper.getTag().findAll(specification, pageable);
        return result.map(ContentMapper::toDTO);
    }

    /**
     * Finds the first Tag entity that matches the given search criteria
     * and returns it as a TagDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching TagDTO object, or {@code null} if no match is found
     */
    public TagDTO findFirstBySearch(TagSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<TagDTO> pages= findBySearch(searchDTO);
        List<TagDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Tag entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(TagSrchDTO searchDTO) {
        Specification<Tag> specification = ContentMapper.toSpec(searchDTO);
        return helper.getTag().count(specification);
    }
    /**
     * Finds a Tag entity by its ID and converts it to a TagDTO.
     *
     * @param tagId the ID of the FaxResult entity
     * @return an Optional containing the TagDTO if found, or an empty Optional if not found
     */
    public Optional<TagDTO> findById(Long tagId) {
        return helper.getTag().findById(tagId).map(e->ContentMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Tag entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the TagDTO to save
     * @return the saved TagDTO
     */
    @Transactional
    public TagDTO save(TagDTO dto) {
        Tag entity = ContentMapper.toEntity(dto, helper.getEntityManager());
        Tag result = helper.getTag().save(entity);
        return ContentMapper.toDTO(result);
    }


    /**
     * Updates an existing Tag entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the TagDTO with updated information
     * @return the updated TagDTO
     */
    @Transactional
    public TagDTO update(TagDTO dto) {
        Tag entity = helper.getTag().findById(dto.getTagId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid TagDTO ID");
        ContentMapper.mapping(dto, entity, true, helper.getEntityManager());

        Tag result = helper.getTag().save(entity);
        return ContentMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Tag entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of TagDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<TagDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (TagDTO dto : dtoList) {
            Tag entity = helper.getTag().findById(dto.getTagId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid TagDTO ID: " + dto.getTagId());
            }
            ContentMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getTag().save(entity);
        }
    }

    /**
     * Deletes a list of Tag entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Tag representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<TagKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (TagKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getTagId()));
        }
        return result;
    }


    /**
     * Deletes a Tag entity by its ID.
     *
     * @param tagId the ID of the Tag entity to delete
     * @return true if the Tag entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long tagId) {
        helper.getTag().deleteById(tagId);
        return !helper.getTag().existsById(tagId);
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
    public int updateBySpecification(TagDTO dto, TagSrchDTO srchDTO) {
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
    public int updateBySpecification(TagDTO dto, Specification<Tag> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Tag> update = cb.createCriteriaUpdate(Tag.class);
        Root<Tag> root = update.from(Tag.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Tag.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
