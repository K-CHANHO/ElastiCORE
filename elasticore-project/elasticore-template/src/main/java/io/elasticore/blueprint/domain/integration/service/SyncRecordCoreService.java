//ecd:156538369H20260301235721_V2.0
package io.elasticore.blueprint.domain.integration.service;

import io.elasticore.blueprint.domain.integration.entity.SyncRecord;
import io.elasticore.blueprint.domain.integration.dto.*;
import io.elasticore.blueprint.domain.integration.repository.*;
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
 * Comprehensive service layer for managing SyncRecord entities.
 *
 * Provides full CRUD operations, dynamic search with specification support,
 * pagination, sorting, and advanced query functions such as min/max/sum.
 * Delegates repository operations via IntegrationRepositoryHelper.
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class SyncRecordCoreService {

    protected final IntegrationRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<SyncRecord> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all SyncRecord entities, converts them to SyncRecordDTO objects, and returns them as a list.
     *
     * @return a list of SyncRecordDTO objects
     */
    public List<SyncRecordDTO> findAll() {
        return helper.getSyncRecord().findAll().stream()
                 
                .map(IntegrationMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tSyncRecord entities, converts them to SyncRecordDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of SyncRecordDTO objects
     */
    public List<SyncRecordDTO> findAll(Sort sort) {
        return helper.getSyncRecord().findAll(sort).stream()
                 
                .map(IntegrationMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes SyncRecord entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(SyncRecordSrchDTO searchDTO) {
        Specification<SyncRecord> specification = IntegrationMapper.toSpec(searchDTO);
        return helper.getSyncRecord().delete(specification);
    }




    /**
     * Finds a list of SyncRecord entities that match the given search criteria
     * and returns them as a list of SyncRecordDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching SyncRecordDTO objects
     */
    public List<SyncRecordDTO> listBySearch(SyncRecordSrchDTO searchDTO) {
        Specification<SyncRecord> specification = IntegrationMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getSyncRecord().findAll(specification).stream()
                        
                        .map(IntegrationMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getSyncRecord().findAll(specification, sort).stream()
                
                .map(IntegrationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds SyncRecord entities that match the given search criteria and returns them as a paginated list of SyncRecordDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of SyncRecordDTO objects
     */
    public Page<SyncRecordDTO> findBySearch(SyncRecordSrchDTO searchDTO) {
        Specification<SyncRecord> specification = IntegrationMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<SyncRecord> result = helper.getSyncRecord().findAll(specification, pageable);
        return result.map(IntegrationMapper::toDTO);
    }

    /**
     * Finds the first SyncRecord entity that matches the given search criteria
     * and returns it as a SyncRecordDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching SyncRecordDTO object, or {@code null} if no match is found
     */
    public SyncRecordDTO findFirstBySearch(SyncRecordSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<SyncRecordDTO> pages= findBySearch(searchDTO);
        List<SyncRecordDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of SyncRecord entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(SyncRecordSrchDTO searchDTO) {
        Specification<SyncRecord> specification = IntegrationMapper.toSpec(searchDTO);
        return helper.getSyncRecord().count(specification);
    }
    /**
     * Finds a SyncRecord entity by its ID and converts it to a SyncRecordDTO.
     *
     * @param recordId the ID of the FaxResult entity
     * @return an Optional containing the SyncRecordDTO if found, or an empty Optional if not found
     */
    public Optional<SyncRecordDTO> findById(String recordId) {
        return helper.getSyncRecord().findById(recordId).map(e->IntegrationMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new SyncRecord entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the SyncRecordDTO to save
     * @return the saved SyncRecordDTO
     */
    @Transactional
    public SyncRecordDTO save(SyncRecordDTO dto) {
        SyncRecord entity = IntegrationMapper.toEntity(dto, helper.getEntityManager());
        SyncRecord result = helper.getSyncRecord().save(entity);
        return IntegrationMapper.toDTO(result);
    }


    /**
     * Updates an existing SyncRecord entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the SyncRecordDTO with updated information
     * @return the updated SyncRecordDTO
     */
    @Transactional
    public SyncRecordDTO update(SyncRecordDTO dto) {
        SyncRecord entity = helper.getSyncRecord().findById(dto.getRecordId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid SyncRecordDTO ID");
        IntegrationMapper.mapping(dto, entity, true, helper.getEntityManager());

        SyncRecord result = helper.getSyncRecord().save(entity);
        return IntegrationMapper.toDTO(result);
    }

    /**
     * Batch updates multiple SyncRecord entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of SyncRecordDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<SyncRecordDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (SyncRecordDTO dto : dtoList) {
            SyncRecord entity = helper.getSyncRecord().findById(dto.getRecordId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid SyncRecordDTO ID: " + dto.getRecordId());
            }
            IntegrationMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getSyncRecord().save(entity);
        }
    }

    /**
     * Deletes a list of SyncRecord entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of SyncRecord representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<SyncRecordKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (SyncRecordKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getRecordId()));
        }
        return result;
    }


    /**
     * Deletes a SyncRecord entity by its ID.
     *
     * @param recordId the ID of the SyncRecord entity to delete
     * @return true if the SyncRecord entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(String recordId) {
        helper.getSyncRecord().deleteById(recordId);
        return !helper.getSyncRecord().existsById(recordId);
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
    public int updateBySpecification(SyncRecordDTO dto, SyncRecordSrchDTO srchDTO) {
        return updateBySpecification(dto, IntegrationMapper.toSpec(srchDTO));
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
    public int updateBySpecification(SyncRecordDTO dto, Specification<SyncRecord> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<SyncRecord> update = cb.createCriteriaUpdate(SyncRecord.class);
        Root<SyncRecord> root = update.from(SyncRecord.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(SyncRecord.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
