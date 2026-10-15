//ecd:1712971420H20260803220215_V2.2
package io.elasticore.blueprint.domain.content.service;

import io.elasticore.blueprint.domain.content.entity.Bookmark;
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
 * Comprehensive service layer for managing Bookmark entities.
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
public class BookmarkCoreService {

    protected final ContentRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Bookmark> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all Bookmark entities, converts them to BookmarkDTO objects, and returns them as a list.
     *
     * @return a list of BookmarkDTO objects
     */
    public List<BookmarkDTO> findAll() {
        return helper.getBookmark().findAll().stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tBookmark entities, converts them to BookmarkDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of BookmarkDTO objects
     */
    public List<BookmarkDTO> findAll(Sort sort) {
        return helper.getBookmark().findAll(sort).stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Bookmark entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(BookmarkSrchDTO searchDTO) {
        Specification<Bookmark> specification = ContentMapper.toSpec(searchDTO);
        return helper.getBookmark().delete(specification);
    }




    /**
     * Finds a list of Bookmark entities that match the given search criteria
     * and returns them as a list of BookmarkDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching BookmarkDTO objects
     */
    public List<BookmarkDTO> listBySearch(BookmarkSrchDTO searchDTO) {
        Specification<Bookmark> specification = ContentMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getBookmark().findAll(specification).stream()
                        
                        .map(ContentMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getBookmark().findAll(specification, sort).stream()
                
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Bookmark entities that match the given search criteria and returns them as a paginated list of BookmarkDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of BookmarkDTO objects
     */
    public Page<BookmarkDTO> findBySearch(BookmarkSrchDTO searchDTO) {
        Specification<Bookmark> specification = ContentMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Bookmark> result = helper.getBookmark().findAll(specification, pageable);
        return result.map(ContentMapper::toDTO);
    }

    /**
     * Finds the first Bookmark entity that matches the given search criteria
     * and returns it as a BookmarkDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching BookmarkDTO object, or {@code null} if no match is found
     */
    public BookmarkDTO findFirstBySearch(BookmarkSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<BookmarkDTO> pages= findBySearch(searchDTO);
        List<BookmarkDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Bookmark entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(BookmarkSrchDTO searchDTO) {
        Specification<Bookmark> specification = ContentMapper.toSpec(searchDTO);
        return helper.getBookmark().count(specification);
    }
    /**
     * Finds a Bookmark entity by its ID and converts it to a BookmarkDTO.
     *
     * @param bookmarkId the ID of the FaxResult entity
     * @return an Optional containing the BookmarkDTO if found, or an empty Optional if not found
     */
    public Optional<BookmarkDTO> findById(Long bookmarkId) {
        return helper.getBookmark().findById(bookmarkId).map(e->ContentMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Bookmark entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the BookmarkDTO to save
     * @return the saved BookmarkDTO
     */
    @Transactional
    public BookmarkDTO save(BookmarkDTO dto) {
        Bookmark entity = ContentMapper.toEntity(dto, helper.getEntityManager());
        Bookmark result = helper.getBookmark().save(entity);
        return ContentMapper.toDTO(result);
    }


    /**
     * Updates an existing Bookmark entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the BookmarkDTO with updated information
     * @return the updated BookmarkDTO
     */
    @Transactional
    public BookmarkDTO update(BookmarkDTO dto) {
        Bookmark entity = helper.getBookmark().findById(dto.getBookmarkId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid BookmarkDTO ID");
        ContentMapper.mapping(dto, entity, true, helper.getEntityManager());

        Bookmark result = helper.getBookmark().save(entity);
        return ContentMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Bookmark entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of BookmarkDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<BookmarkDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (BookmarkDTO dto : dtoList) {
            Bookmark entity = helper.getBookmark().findById(dto.getBookmarkId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid BookmarkDTO ID: " + dto.getBookmarkId());
            }
            ContentMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getBookmark().save(entity);
        }
    }

    /**
     * Deletes a list of Bookmark entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Bookmark representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<BookmarkKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (BookmarkKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getBookmarkId()));
        }
        return result;
    }


    /**
     * Deletes a Bookmark entity by its ID.
     *
     * @param bookmarkId the ID of the Bookmark entity to delete
     * @return true if the Bookmark entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long bookmarkId) {
        helper.getBookmark().deleteById(bookmarkId);
        return !helper.getBookmark().existsById(bookmarkId);
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
    public int updateBySpecification(BookmarkDTO dto, BookmarkSrchDTO srchDTO) {
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
    public int updateBySpecification(BookmarkDTO dto, Specification<Bookmark> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Bookmark> update = cb.createCriteriaUpdate(Bookmark.class);
        Root<Bookmark> root = update.from(Bookmark.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Bookmark.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
