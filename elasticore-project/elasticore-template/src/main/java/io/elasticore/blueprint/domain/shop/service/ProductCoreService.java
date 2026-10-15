//ecd:-1373918317H20260301235722_V2.0
package io.elasticore.blueprint.domain.shop.service;

import io.elasticore.blueprint.domain.shop.entity.Product;
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
 * Comprehensive service layer for managing Product entities.
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
public class ProductCoreService {

    protected final ShopRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Product> withJoinFetches() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                // JOIN FETCH optimization enabled
                root.fetch("category", JoinType.LEFT);

                query.distinct(true);
            }
            return null;
        };
    }

    /**
     * Retrieves all Product entities, converts them to ProductDTO objects, and returns them as a list.
     *
     * @return a list of ProductDTO objects
     */
    public List<ProductDTO> findAll() {
        return helper.getProduct().findAll().stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tProduct entities, converts them to ProductDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of ProductDTO objects
     */
    public List<ProductDTO> findAll(Sort sort) {
        return helper.getProduct().findAll(sort).stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Product entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(ProductSrchDTO searchDTO) {
        Specification<Product> specification = ShopMapper.toSpec(searchDTO);
        return helper.getProduct().delete(specification);
    }




    /**
     * Finds a list of Product entities that match the given search criteria
     * and returns them as a list of ProductDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching ProductDTO objects
     */
    public List<ProductDTO> listBySearch(ProductSrchDTO searchDTO) {
        Specification<Product> specification = ShopMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getProduct().findAll(specification).stream()
                        
                        .map(ShopMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getProduct().findAll(specification, sort).stream()
                
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Product entities that match the given search criteria and returns them as a paginated list of ProductDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of ProductDTO objects
     */
    public Page<ProductDTO> findBySearch(ProductSrchDTO searchDTO) {
        Specification<Product> specification = ShopMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Product> result = helper.getProduct().findAll(specification, pageable);
        return result.map(ShopMapper::toDTO);
    }

    /**
     * Finds the first Product entity that matches the given search criteria
     * and returns it as a ProductDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching ProductDTO object, or {@code null} if no match is found
     */
    public ProductDTO findFirstBySearch(ProductSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<ProductDTO> pages= findBySearch(searchDTO);
        List<ProductDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Product entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(ProductSrchDTO searchDTO) {
        Specification<Product> specification = ShopMapper.toSpec(searchDTO);
        return helper.getProduct().count(specification);
    }
    /**
     * Finds a Product entity by its ID and converts it to a ProductDTO.
     *
     * @param productId the ID of the FaxResult entity
     * @return an Optional containing the ProductDTO if found, or an empty Optional if not found
     */
    public Optional<ProductDTO> findById(String productId) {
        return helper.getProduct().findById(productId).map(e->ShopMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Product entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the ProductDTO to save
     * @return the saved ProductDTO
     */
    @Transactional
    public ProductDTO save(ProductDTO dto) {
        Product entity = ShopMapper.toEntity(dto, helper.getEntityManager());
        Product result = helper.getProduct().save(entity);
        return ShopMapper.toDTO(result);
    }


    /**
     * Updates an existing Product entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the ProductDTO with updated information
     * @return the updated ProductDTO
     */
    @Transactional
    public ProductDTO update(ProductDTO dto) {
        Product entity = helper.getProduct().findById(dto.getProductId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid ProductDTO ID");
        ShopMapper.mapping(dto, entity, true, helper.getEntityManager());

        Product result = helper.getProduct().save(entity);
        return ShopMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Product entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of ProductDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<ProductDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (ProductDTO dto : dtoList) {
            Product entity = helper.getProduct().findById(dto.getProductId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid ProductDTO ID: " + dto.getProductId());
            }
            ShopMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getProduct().save(entity);
        }
    }

    /**
     * Deletes a list of Product entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Product representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<ProductKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (ProductKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getProductId()));
        }
        return result;
    }


    /**
     * Deletes a Product entity by its ID.
     *
     * @param productId the ID of the Product entity to delete
     * @return true if the Product entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(String productId) {
        helper.getProduct().deleteById(productId);
        return !helper.getProduct().existsById(productId);
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
    public int updateBySpecification(ProductDTO dto, ProductSrchDTO srchDTO) {
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
    public int updateBySpecification(ProductDTO dto, Specification<Product> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Product> update = cb.createCriteriaUpdate(Product.class);
        Root<Product> root = update.from(Product.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Product.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
