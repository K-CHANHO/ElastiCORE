//ecd:1244336066H20260328122051_V2.0
package io.elasticore.blueprint.domain.jsonview_demo.service;

import io.elasticore.blueprint.domain.jsonview_demo.entity.BlogPost;
import io.elasticore.blueprint.domain.jsonview_demo.dto.*;
import io.elasticore.blueprint.domain.jsonview_demo.repository.*;
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
 * Comprehensive service layer for managing BlogPost entities.
 *
 * Provides full CRUD operations, dynamic search with specification support,
 * pagination, sorting, and advanced query functions such as min/max/sum.
 * Delegates repository operations via Jsonview_demoRepositoryHelper.
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class BlogPostCoreService {

    protected final Jsonview_demoRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<BlogPost> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all BlogPost entities, converts them to BlogPostDTO objects, and returns them as a list.
     *
     * @return a list of BlogPostDTO objects
     */
    public List<BlogPostDTO> findAll() {
        return helper.getBlogPost().findAll().stream()
                 
                .map(Jsonview_demoMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tBlogPost entities, converts them to BlogPostDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of BlogPostDTO objects
     */
    public List<BlogPostDTO> findAll(Sort sort) {
        return helper.getBlogPost().findAll(sort).stream()
                 
                .map(Jsonview_demoMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes BlogPost entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(BlogPostSrchDTO searchDTO) {
        Specification<BlogPost> specification = Jsonview_demoMapper.toSpec(searchDTO);
        return helper.getBlogPost().delete(specification);
    }




    /**
     * Finds a list of BlogPost entities that match the given search criteria
     * and returns them as a list of BlogPostDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching BlogPostDTO objects
     */
    public List<BlogPostDTO> listBySearch(BlogPostSrchDTO searchDTO) {
        Specification<BlogPost> specification = Jsonview_demoMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getBlogPost().findAll(specification).stream()
                        
                        .map(Jsonview_demoMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getBlogPost().findAll(specification, sort).stream()
                
                .map(Jsonview_demoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds BlogPost entities that match the given search criteria and returns them as a paginated list of BlogPostDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of BlogPostDTO objects
     */
    public Page<BlogPostDTO> findBySearch(BlogPostSrchDTO searchDTO) {
        Specification<BlogPost> specification = Jsonview_demoMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<BlogPost> result = helper.getBlogPost().findAll(specification, pageable);
        return result.map(Jsonview_demoMapper::toDTO);
    }

    /**
     * Finds the first BlogPost entity that matches the given search criteria
     * and returns it as a BlogPostDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching BlogPostDTO object, or {@code null} if no match is found
     */
    public BlogPostDTO findFirstBySearch(BlogPostSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<BlogPostDTO> pages= findBySearch(searchDTO);
        List<BlogPostDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of BlogPost entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(BlogPostSrchDTO searchDTO) {
        Specification<BlogPost> specification = Jsonview_demoMapper.toSpec(searchDTO);
        return helper.getBlogPost().count(specification);
    }
    /**
     * Finds a BlogPost entity by its ID and converts it to a BlogPostDTO.
     *
     * @param id the ID of the FaxResult entity
     * @return an Optional containing the BlogPostDTO if found, or an empty Optional if not found
     */
    public Optional<BlogPostDTO> findById(Long id) {
        return helper.getBlogPost().findById(id).map(e->Jsonview_demoMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new BlogPost entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the BlogPostDTO to save
     * @return the saved BlogPostDTO
     */
    @Transactional
    public BlogPostDTO save(BlogPostDTO dto) {
        BlogPost entity = Jsonview_demoMapper.toEntity(dto, helper.getEntityManager());
        BlogPost result = helper.getBlogPost().save(entity);
        return Jsonview_demoMapper.toDTO(result);
    }


    /**
     * Updates an existing BlogPost entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the BlogPostDTO with updated information
     * @return the updated BlogPostDTO
     */
    @Transactional
    public BlogPostDTO update(BlogPostDTO dto) {
        BlogPost entity = helper.getBlogPost().findById(dto.getId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid BlogPostDTO ID");
        Jsonview_demoMapper.mapping(dto, entity, true, helper.getEntityManager());

        BlogPost result = helper.getBlogPost().save(entity);
        return Jsonview_demoMapper.toDTO(result);
    }

    /**
     * Batch updates multiple BlogPost entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of BlogPostDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<BlogPostDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (BlogPostDTO dto : dtoList) {
            BlogPost entity = helper.getBlogPost().findById(dto.getId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid BlogPostDTO ID: " + dto.getId());
            }
            Jsonview_demoMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getBlogPost().save(entity);
        }
    }

    /**
     * Deletes a list of BlogPost entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of BlogPost representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<BlogPostKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (BlogPostKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getId()));
        }
        return result;
    }


    /**
     * Deletes a BlogPost entity by its ID.
     *
     * @param id the ID of the BlogPost entity to delete
     * @return true if the BlogPost entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long id) {
        helper.getBlogPost().deleteById(id);
        return !helper.getBlogPost().existsById(id);
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
    public int updateBySpecification(BlogPostDTO dto, BlogPostSrchDTO srchDTO) {
        return updateBySpecification(dto, Jsonview_demoMapper.toSpec(srchDTO));
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
    public int updateBySpecification(BlogPostDTO dto, Specification<BlogPost> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<BlogPost> update = cb.createCriteriaUpdate(BlogPost.class);
        Root<BlogPost> root = update.from(BlogPost.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(BlogPost.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
