//ecd:829477668H20260301235722_V2.0
package io.elasticore.blueprint.domain.shop.service;

import io.elasticore.blueprint.domain.shop.entity.Order;
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
 * Comprehensive service layer for managing Order entities.
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
public class OrderCoreService {

    protected final ShopRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Order> withJoinFetches() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                // JOIN FETCH optimization enabled
                root.fetch("customer", JoinType.LEFT);

                query.distinct(true);
            }
            return null;
        };
    }

    /**
     * Retrieves all Order entities, converts them to OrderDTO objects, and returns them as a list.
     *
     * @return a list of OrderDTO objects
     */
    public List<OrderDTO> findAll() {
        return helper.getOrder().findAll().stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tOrder entities, converts them to OrderDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of OrderDTO objects
     */
    public List<OrderDTO> findAll(Sort sort) {
        return helper.getOrder().findAll(sort).stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Order entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(OrderSrchDTO searchDTO) {
        Specification<Order> specification = ShopMapper.toSpec(searchDTO);
        return helper.getOrder().delete(specification);
    }




    /**
     * Finds a list of Order entities that match the given search criteria
     * and returns them as a list of OrderDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching OrderDTO objects
     */
    public List<OrderDTO> listBySearch(OrderSrchDTO searchDTO) {
        Specification<Order> specification = ShopMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getOrder().findAll(specification).stream()
                        
                        .map(ShopMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getOrder().findAll(specification, sort).stream()
                
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Order entities that match the given search criteria and returns them as a paginated list of OrderDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of OrderDTO objects
     */
    public Page<OrderDTO> findBySearch(OrderSrchDTO searchDTO) {
        Specification<Order> specification = ShopMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Order> result = helper.getOrder().findAll(specification, pageable);
        return result.map(ShopMapper::toDTO);
    }

    /**
     * Finds the first Order entity that matches the given search criteria
     * and returns it as a OrderDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching OrderDTO object, or {@code null} if no match is found
     */
    public OrderDTO findFirstBySearch(OrderSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<OrderDTO> pages= findBySearch(searchDTO);
        List<OrderDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Order entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(OrderSrchDTO searchDTO) {
        Specification<Order> specification = ShopMapper.toSpec(searchDTO);
        return helper.getOrder().count(specification);
    }
    /**
     * Finds a Order entity by its ID and converts it to a OrderDTO.
     *
     * @param orderId the ID of the FaxResult entity
     * @return an Optional containing the OrderDTO if found, or an empty Optional if not found
     */
    public Optional<OrderDTO> findById(String orderId) {
        return helper.getOrder().findById(orderId).map(e->ShopMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Order entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the OrderDTO to save
     * @return the saved OrderDTO
     */
    @Transactional
    public OrderDTO save(OrderDTO dto) {
        Order entity = ShopMapper.toEntity(dto, helper.getEntityManager());
        Order result = helper.getOrder().save(entity);
        return ShopMapper.toDTO(result);
    }


    /**
     * Updates an existing Order entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the OrderDTO with updated information
     * @return the updated OrderDTO
     */
    @Transactional
    public OrderDTO update(OrderDTO dto) {
        Order entity = helper.getOrder().findById(dto.getOrderId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid OrderDTO ID");
        ShopMapper.mapping(dto, entity, true, helper.getEntityManager());

        Order result = helper.getOrder().save(entity);
        return ShopMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Order entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of OrderDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<OrderDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (OrderDTO dto : dtoList) {
            Order entity = helper.getOrder().findById(dto.getOrderId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid OrderDTO ID: " + dto.getOrderId());
            }
            ShopMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getOrder().save(entity);
        }
    }

    /**
     * Deletes a list of Order entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Order representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<OrderKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (OrderKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getOrderId()));
        }
        return result;
    }


    /**
     * Deletes a Order entity by its ID.
     *
     * @param orderId the ID of the Order entity to delete
     * @return true if the Order entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(String orderId) {
        helper.getOrder().deleteById(orderId);
        return !helper.getOrder().existsById(orderId);
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
    public int updateBySpecification(OrderDTO dto, OrderSrchDTO srchDTO) {
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
    public int updateBySpecification(OrderDTO dto, Specification<Order> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Order> update = cb.createCriteriaUpdate(Order.class);
        Root<Order> root = update.from(Order.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Order.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
