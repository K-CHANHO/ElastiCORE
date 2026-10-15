//ecd:1040319246H20260301235722_V2.0
package io.elasticore.blueprint.domain.shop.service;

import io.elasticore.blueprint.domain.shop.entity.Customer;
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
 * Comprehensive service layer for managing Customer entities.
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
public class CustomerCoreService {

    protected final ShopRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Customer> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all Customer entities, converts them to CustomerDTO objects, and returns them as a list.
     *
     * @return a list of CustomerDTO objects
     */
    public List<CustomerDTO> findAll() {
        return helper.getCustomer().findAll().stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tCustomer entities, converts them to CustomerDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of CustomerDTO objects
     */
    public List<CustomerDTO> findAll(Sort sort) {
        return helper.getCustomer().findAll(sort).stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Customer entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(CustomerSrchDTO searchDTO) {
        Specification<Customer> specification = ShopMapper.toSpec(searchDTO);
        return helper.getCustomer().delete(specification);
    }




    /**
     * Finds a list of Customer entities that match the given search criteria
     * and returns them as a list of CustomerDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching CustomerDTO objects
     */
    public List<CustomerDTO> listBySearch(CustomerSrchDTO searchDTO) {
        Specification<Customer> specification = ShopMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getCustomer().findAll(specification).stream()
                        
                        .map(ShopMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getCustomer().findAll(specification, sort).stream()
                
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Customer entities that match the given search criteria and returns them as a paginated list of CustomerDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of CustomerDTO objects
     */
    public Page<CustomerDTO> findBySearch(CustomerSrchDTO searchDTO) {
        Specification<Customer> specification = ShopMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Customer> result = helper.getCustomer().findAll(specification, pageable);
        return result.map(ShopMapper::toDTO);
    }

    /**
     * Finds the first Customer entity that matches the given search criteria
     * and returns it as a CustomerDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching CustomerDTO object, or {@code null} if no match is found
     */
    public CustomerDTO findFirstBySearch(CustomerSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<CustomerDTO> pages= findBySearch(searchDTO);
        List<CustomerDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Customer entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(CustomerSrchDTO searchDTO) {
        Specification<Customer> specification = ShopMapper.toSpec(searchDTO);
        return helper.getCustomer().count(specification);
    }
    /**
     * Finds a Customer entity by its ID and converts it to a CustomerDTO.
     *
     * @param customerId the ID of the FaxResult entity
     * @return an Optional containing the CustomerDTO if found, or an empty Optional if not found
     */
    public Optional<CustomerDTO> findById(Long customerId) {
        return helper.getCustomer().findById(customerId).map(e->ShopMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Customer entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the CustomerDTO to save
     * @return the saved CustomerDTO
     */
    @Transactional
    public CustomerDTO save(CustomerDTO dto) {
        Customer entity = ShopMapper.toEntity(dto, helper.getEntityManager());
        Customer result = helper.getCustomer().save(entity);
        return ShopMapper.toDTO(result);
    }


    /**
     * Updates an existing Customer entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the CustomerDTO with updated information
     * @return the updated CustomerDTO
     */
    @Transactional
    public CustomerDTO update(CustomerDTO dto) {
        Customer entity = helper.getCustomer().findById(dto.getCustomerId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid CustomerDTO ID");
        ShopMapper.mapping(dto, entity, true, helper.getEntityManager());

        Customer result = helper.getCustomer().save(entity);
        return ShopMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Customer entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of CustomerDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<CustomerDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (CustomerDTO dto : dtoList) {
            Customer entity = helper.getCustomer().findById(dto.getCustomerId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid CustomerDTO ID: " + dto.getCustomerId());
            }
            ShopMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getCustomer().save(entity);
        }
    }

    /**
     * Deletes a list of Customer entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Customer representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<CustomerKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (CustomerKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getCustomerId()));
        }
        return result;
    }


    /**
     * Deletes a Customer entity by its ID.
     *
     * @param customerId the ID of the Customer entity to delete
     * @return true if the Customer entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long customerId) {
        helper.getCustomer().deleteById(customerId);
        return !helper.getCustomer().existsById(customerId);
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
    public int updateBySpecification(CustomerDTO dto, CustomerSrchDTO srchDTO) {
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
    public int updateBySpecification(CustomerDTO dto, Specification<Customer> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Customer> update = cb.createCriteriaUpdate(Customer.class);
        Root<Customer> root = update.from(Customer.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Customer.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
