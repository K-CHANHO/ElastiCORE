//ecd:-553541935H20261028193835_V3.0
package io.elasticore.blueprint.domain.shop.service;

import io.elasticore.blueprint.domain.shop.entity.Ledger;
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
 * Comprehensive service layer for managing Ledger entities.
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
public class LedgerCoreService {

    protected final ShopRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Ledger> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all Ledger entities, converts them to LedgerDTO objects, and returns them as a list.
     *
     * @return a list of LedgerDTO objects
     */
    public List<LedgerDTO> findAll() {
        return helper.getLedger().findAll().stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tLedger entities, converts them to LedgerDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of LedgerDTO objects
     */
    public List<LedgerDTO> findAll(Sort sort) {
        return helper.getLedger().findAll(sort).stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Ledger entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(LedgerQuery searchDTO) {
        Specification<Ledger> specification = ShopMapper.toSpec(searchDTO);
        return helper.getLedger().delete(specification);
    }




    /**
     * Finds a list of Ledger entities that match the given search criteria
     * and returns them as a list of LedgerDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching LedgerDTO objects
     */
    public List<LedgerDTO> listBySearch(LedgerQuery searchDTO) {
        Specification<Ledger> specification = ShopMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getLedger().findAll(specification).stream()
                        
                        .map(ShopMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getLedger().findAll(specification, sort).stream()
                
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Ledger entities that match the given search criteria and returns them as a paginated list of LedgerDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of LedgerDTO objects
     */
    public Page<LedgerDTO> findBySearch(LedgerQuery searchDTO) {
        Specification<Ledger> specification = ShopMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Ledger> result = helper.getLedger().findAll(specification, pageable);
        return result.map(ShopMapper::toDTO);
    }

    /**
     * Finds the first Ledger entity that matches the given search criteria
     * and returns it as a LedgerDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching LedgerDTO object, or {@code null} if no match is found
     */
    public LedgerDTO findFirstBySearch(LedgerQuery searchDTO) {
        searchDTO.setPageSize(1);
        Page<LedgerDTO> pages= findBySearch(searchDTO);
        List<LedgerDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Ledger entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(LedgerQuery searchDTO) {
        Specification<Ledger> specification = ShopMapper.toSpec(searchDTO);
        return helper.getLedger().count(specification);
    }
    /**
     * Finds a Ledger entity by its ID and converts it to a LedgerDTO.
     *
     * @param ledgerId the ID of the FaxResult entity
     * @return an Optional containing the LedgerDTO if found, or an empty Optional if not found
     */
    public Optional<LedgerDTO> findById(Long ledgerId) {
        return helper.getLedger().findById(ledgerId).map(e->ShopMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Ledger entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the LedgerDTO to save
     * @return the saved LedgerDTO
     */
    @Transactional
    public LedgerDTO save(LedgerDTO dto) {
        Ledger entity = ShopMapper.toEntity(dto, helper.getEntityManager());
        Ledger result = helper.getLedger().save(entity);
        return ShopMapper.toDTO(result);
    }


    /**
     * Updates an existing Ledger entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the LedgerDTO with updated information
     * @return the updated LedgerDTO
     */
    @Transactional
    public LedgerDTO update(LedgerDTO dto) {
        Ledger entity = helper.getLedger().findById(dto.getLedgerId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid LedgerDTO ID");
        ShopMapper.mapping(dto, entity, true, helper.getEntityManager());

        Ledger result = helper.getLedger().save(entity);
        return ShopMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Ledger entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of LedgerDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<LedgerDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (LedgerDTO dto : dtoList) {
            Ledger entity = helper.getLedger().findById(dto.getLedgerId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid LedgerDTO ID: " + dto.getLedgerId());
            }
            ShopMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getLedger().save(entity);
        }
    }

    /**
     * Deletes a list of Ledger entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Ledger representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<LedgerKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (LedgerKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getLedgerId()));
        }
        return result;
    }


    /**
     * Deletes a Ledger entity by its ID.
     *
     * @param ledgerId the ID of the Ledger entity to delete
     * @return true if the Ledger entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long ledgerId) {
        helper.getLedger().deleteById(ledgerId);
        return !helper.getLedger().existsById(ledgerId);
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
    public int updateBySpecification(LedgerDTO dto, LedgerQuery srchDTO) {
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
    public int updateBySpecification(LedgerDTO dto, Specification<Ledger> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Ledger> update = cb.createCriteriaUpdate(Ledger.class);
        Root<Ledger> root = update.from(Ledger.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Ledger.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
