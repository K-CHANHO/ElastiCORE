//ecd:-21331051H20260301235721_V2.0
package io.elasticore.blueprint.domain.content.service;

import io.elasticore.blueprint.domain.content.entity.Comment;
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
 * Comprehensive service layer for managing Comment entities.
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
public class CommentCoreService {

    protected final ContentRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Comment> withJoinFetches() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                // JOIN FETCH optimization enabled
                root.fetch("article", JoinType.LEFT);

                query.distinct(true);
            }
            return null;
        };
    }

    /**
     * Retrieves all Comment entities, converts them to CommentDTO objects, and returns them as a list.
     *
     * @return a list of CommentDTO objects
     */
    public List<CommentDTO> findAll() {
        return helper.getComment().findAll().stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tComment entities, converts them to CommentDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of CommentDTO objects
     */
    public List<CommentDTO> findAll(Sort sort) {
        return helper.getComment().findAll(sort).stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Comment entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(CommentSrchDTO searchDTO) {
        Specification<Comment> specification = ContentMapper.toSpec(searchDTO);
        return helper.getComment().delete(specification);
    }




    /**
     * Finds a list of Comment entities that match the given search criteria
     * and returns them as a list of CommentDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching CommentDTO objects
     */
    public List<CommentDTO> listBySearch(CommentSrchDTO searchDTO) {
        Specification<Comment> specification = ContentMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getComment().findAll(specification).stream()
                        
                        .map(ContentMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getComment().findAll(specification, sort).stream()
                
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Comment entities that match the given search criteria and returns them as a paginated list of CommentDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of CommentDTO objects
     */
    public Page<CommentDTO> findBySearch(CommentSrchDTO searchDTO) {
        Specification<Comment> specification = ContentMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Comment> result = helper.getComment().findAll(specification, pageable);
        return result.map(ContentMapper::toDTO);
    }

    /**
     * Finds the first Comment entity that matches the given search criteria
     * and returns it as a CommentDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching CommentDTO object, or {@code null} if no match is found
     */
    public CommentDTO findFirstBySearch(CommentSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<CommentDTO> pages= findBySearch(searchDTO);
        List<CommentDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Comment entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(CommentSrchDTO searchDTO) {
        Specification<Comment> specification = ContentMapper.toSpec(searchDTO);
        return helper.getComment().count(specification);
    }
    /**
     * Finds a Comment entity by its ID and converts it to a CommentDTO.
     *
     * @param commentId the ID of the FaxResult entity
     * @return an Optional containing the CommentDTO if found, or an empty Optional if not found
     */
    public Optional<CommentDTO> findById(Long commentId) {
        return helper.getComment().findById(commentId).map(e->ContentMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Comment entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the CommentDTO to save
     * @return the saved CommentDTO
     */
    @Transactional
    public CommentDTO save(CommentDTO dto) {
        Comment entity = ContentMapper.toEntity(dto, helper.getEntityManager());
        Comment result = helper.getComment().save(entity);
        return ContentMapper.toDTO(result);
    }


    /**
     * Updates an existing Comment entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the CommentDTO with updated information
     * @return the updated CommentDTO
     */
    @Transactional
    public CommentDTO update(CommentDTO dto) {
        Comment entity = helper.getComment().findById(dto.getCommentId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid CommentDTO ID");
        ContentMapper.mapping(dto, entity, true, helper.getEntityManager());

        Comment result = helper.getComment().save(entity);
        return ContentMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Comment entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of CommentDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<CommentDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (CommentDTO dto : dtoList) {
            Comment entity = helper.getComment().findById(dto.getCommentId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid CommentDTO ID: " + dto.getCommentId());
            }
            ContentMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getComment().save(entity);
        }
    }

    /**
     * Deletes a list of Comment entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Comment representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<CommentKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (CommentKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getCommentId()));
        }
        return result;
    }


    /**
     * Deletes a Comment entity by its ID.
     *
     * @param commentId the ID of the Comment entity to delete
     * @return true if the Comment entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long commentId) {
        helper.getComment().deleteById(commentId);
        return !helper.getComment().existsById(commentId);
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
    public int updateBySpecification(CommentDTO dto, CommentSrchDTO srchDTO) {
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
    public int updateBySpecification(CommentDTO dto, Specification<Comment> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Comment> update = cb.createCriteriaUpdate(Comment.class);
        Root<Comment> root = update.from(Comment.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Comment.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
