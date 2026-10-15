//ecd:1562988672H20260301235722_V2.0
package io.elasticore.blueprint.domain.shop.service;

import io.elasticore.blueprint.domain.shop.entity.Category;
import io.elasticore.blueprint.domain.shop.dto.*;
import io.elasticore.blueprint.domain.shop.repository.*;
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
 * Comprehensive service layer for managing Category entities.
 *
 * Provides full CRUD operations, dynamic search with specification support,
 * pagination, sorting, and advanced query functions such as min/max/sum.
 * Delegates repository operations via ShopRepositoryHelper.
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CategoryCoreService {

    protected final ShopRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Category> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all Category entities, converts them to CategoryDTO objects, and returns them as a list.
     *
     * @return a list of CategoryDTO objects
     */
    public List<CategoryDTO> findAll() {
        return helper.getCategory().findAll().stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tCategory entities, converts them to CategoryDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of CategoryDTO objects
     */
    public List<CategoryDTO> findAll(Sort sort) {
        return helper.getCategory().findAll(sort).stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Category entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(CategorySrchDTO searchDTO) {
        Specification<Category> specification = ShopMapper.toSpec(searchDTO);
        return helper.getCategory().delete(specification);
    }




    /**
     * Finds a list of Category entities that match the given search criteria
     * and returns them as a list of CategoryDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching CategoryDTO objects
     */
    public List<CategoryDTO> listBySearch(CategorySrchDTO searchDTO) {
        Specification<Category> specification = ShopMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getCategory().findAll(specification).stream()
                        
                        .map(ShopMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getCategory().findAll(specification, sort).stream()
                
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Category entities that match the given search criteria and returns them as a paginated list of CategoryDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of CategoryDTO objects
     */
    public Page<CategoryDTO> findBySearch(CategorySrchDTO searchDTO) {
        Specification<Category> specification = ShopMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Category> result = helper.getCategory().findAll(specification, pageable);
        return result.map(ShopMapper::toDTO);
    }

    /**
     * Finds the first Category entity that matches the given search criteria
     * and returns it as a CategoryDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching CategoryDTO object, or {@code null} if no match is found
     */
    public CategoryDTO findFirstBySearch(CategorySrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<CategoryDTO> pages= findBySearch(searchDTO);
        List<CategoryDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Category entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(CategorySrchDTO searchDTO) {
        Specification<Category> specification = ShopMapper.toSpec(searchDTO);
        return helper.getCategory().count(specification);
    }
    /**
     * Finds a Category entity by its ID and converts it to a CategoryDTO.
     *
     * @param catId the ID of the FaxResult entity
     * @return an Optional containing the CategoryDTO if found, or an empty Optional if not found
     */
    public Optional<CategoryDTO> findById(Long catId) {
        return helper.getCategory().findById(catId).map(e->ShopMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Category entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the CategoryDTO to save
     * @return the saved CategoryDTO
     */
    @Transactional
    public CategoryDTO save(CategoryDTO dto) {
        Category entity = ShopMapper.toEntity(dto, helper.getEntityManager());
        Category result = helper.getCategory().save(entity);
        return ShopMapper.toDTO(result);
    }


    /**
     * Updates an existing Category entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the CategoryDTO with updated information
     * @return the updated CategoryDTO
     */
    @Transactional
    public CategoryDTO update(CategoryDTO dto) {
        Category entity = helper.getCategory().findById(dto.getCatId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid CategoryDTO ID");
        ShopMapper.mapping(dto, entity, true, helper.getEntityManager());

        Category result = helper.getCategory().save(entity);
        return ShopMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Category entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of CategoryDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<CategoryDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (CategoryDTO dto : dtoList) {
            Category entity = helper.getCategory().findById(dto.getCatId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid CategoryDTO ID: " + dto.getCatId());
            }
            ShopMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getCategory().save(entity);
        }
    }

    /**
     * Deletes a list of Category entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Category representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<CategoryKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (CategoryKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getCatId()));
        }
        return result;
    }


    /**
     * Deletes a Category entity by its ID.
     *
     * @param catId the ID of the Category entity to delete
     * @return true if the Category entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long catId) {
        helper.getCategory().deleteById(catId);
        return !helper.getCategory().existsById(catId);
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
    public int updateBySpecification(CategoryDTO dto, CategorySrchDTO srchDTO) {
        return updateBySpecification(dto, ShopMapper.toSpec(srchDTO));
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
    public int updateBySpecification(CategoryDTO dto, Specification<Category> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Category> update = cb.createCriteriaUpdate(Category.class);
        Root<Category> root = update.from(Category.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Category.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
