//ecd:790211153H20260301235722_V2.0
package io.elasticore.blueprint.domain.shop.service;

import io.elasticore.blueprint.domain.shop.entity.OrderItem;
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
 * Comprehensive service layer for managing OrderItem entities.
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
public class OrderItemCoreService {

    protected final ShopRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<OrderItem> withJoinFetches() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                // JOIN FETCH optimization enabled
                root.fetch("order", JoinType.LEFT);
                root.fetch("product", JoinType.LEFT);

                query.distinct(true);
            }
            return null;
        };
    }

    /**
     * Retrieves all OrderItem entities, converts them to OrderItemDTO objects, and returns them as a list.
     *
     * @return a list of OrderItemDTO objects
     */
    public List<OrderItemDTO> findAll() {
        return helper.getOrderItem().findAll().stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tOrderItem entities, converts them to OrderItemDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of OrderItemDTO objects
     */
    public List<OrderItemDTO> findAll(Sort sort) {
        return helper.getOrderItem().findAll(sort).stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes OrderItem entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(OrderItemSrchDTO searchDTO) {
        Specification<OrderItem> specification = ShopMapper.toSpec(searchDTO);
        return helper.getOrderItem().delete(specification);
    }




    /**
     * Finds a list of OrderItem entities that match the given search criteria
     * and returns them as a list of OrderItemDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching OrderItemDTO objects
     */
    public List<OrderItemDTO> listBySearch(OrderItemSrchDTO searchDTO) {
        Specification<OrderItem> specification = ShopMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getOrderItem().findAll(specification).stream()
                        
                        .map(ShopMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getOrderItem().findAll(specification, sort).stream()
                
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds OrderItem entities that match the given search criteria and returns them as a paginated list of OrderItemDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of OrderItemDTO objects
     */
    public Page<OrderItemDTO> findBySearch(OrderItemSrchDTO searchDTO) {
        Specification<OrderItem> specification = ShopMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<OrderItem> result = helper.getOrderItem().findAll(specification, pageable);
        return result.map(ShopMapper::toDTO);
    }

    /**
     * Finds the first OrderItem entity that matches the given search criteria
     * and returns it as a OrderItemDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching OrderItemDTO object, or {@code null} if no match is found
     */
    public OrderItemDTO findFirstBySearch(OrderItemSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<OrderItemDTO> pages= findBySearch(searchDTO);
        List<OrderItemDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of OrderItem entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(OrderItemSrchDTO searchDTO) {
        Specification<OrderItem> specification = ShopMapper.toSpec(searchDTO);
        return helper.getOrderItem().count(specification);
    }
    /**
     * Finds a OrderItem entity by its ID and converts it to a OrderItemDTO.
     *
     * @param orderItemId the ID of the FaxResult entity
     * @return an Optional containing the OrderItemDTO if found, or an empty Optional if not found
     */
    public Optional<OrderItemDTO> findById(String orderItemId) {
        return helper.getOrderItem().findById(orderItemId).map(e->ShopMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new OrderItem entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the OrderItemDTO to save
     * @return the saved OrderItemDTO
     */
    @Transactional
    public OrderItemDTO save(OrderItemDTO dto) {
        OrderItem entity = ShopMapper.toEntity(dto, helper.getEntityManager());
        OrderItem result = helper.getOrderItem().save(entity);
        return ShopMapper.toDTO(result);
    }


    /**
     * Updates an existing OrderItem entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the OrderItemDTO with updated information
     * @return the updated OrderItemDTO
     */
    @Transactional
    public OrderItemDTO update(OrderItemDTO dto) {
        OrderItem entity = helper.getOrderItem().findById(dto.getOrderItemId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid OrderItemDTO ID");
        ShopMapper.mapping(dto, entity, true, helper.getEntityManager());

        OrderItem result = helper.getOrderItem().save(entity);
        return ShopMapper.toDTO(result);
    }

    /**
     * Batch updates multiple OrderItem entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of OrderItemDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<OrderItemDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (OrderItemDTO dto : dtoList) {
            OrderItem entity = helper.getOrderItem().findById(dto.getOrderItemId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid OrderItemDTO ID: " + dto.getOrderItemId());
            }
            ShopMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getOrderItem().save(entity);
        }
    }

    /**
     * Deletes a list of OrderItem entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of OrderItem representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<OrderItemKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (OrderItemKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getOrderItemId()));
        }
        return result;
    }


    /**
     * Deletes a OrderItem entity by its ID.
     *
     * @param orderItemId the ID of the OrderItem entity to delete
     * @return true if the OrderItem entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(String orderItemId) {
        helper.getOrderItem().deleteById(orderItemId);
        return !helper.getOrderItem().existsById(orderItemId);
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
    public int updateBySpecification(OrderItemDTO dto, OrderItemSrchDTO srchDTO) {
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
    public int updateBySpecification(OrderItemDTO dto, Specification<OrderItem> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<OrderItem> update = cb.createCriteriaUpdate(OrderItem.class);
        Root<OrderItem> root = update.from(OrderItem.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(OrderItem.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
