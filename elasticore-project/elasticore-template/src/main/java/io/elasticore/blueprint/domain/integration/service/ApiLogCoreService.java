//ecd:-634536512H20260301235721_V2.0
package io.elasticore.blueprint.domain.integration.service;

import io.elasticore.blueprint.domain.integration.entity.ApiLog;
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
 * Comprehensive service layer for managing ApiLog entities.
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
public class ApiLogCoreService {

    protected final IntegrationRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<ApiLog> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all ApiLog entities, converts them to ApiLogDTO objects, and returns them as a list.
     *
     * @return a list of ApiLogDTO objects
     */
    public List<ApiLogDTO> findAll() {
        return helper.getApiLog().findAll().stream()
                 
                .map(IntegrationMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tApiLog entities, converts them to ApiLogDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of ApiLogDTO objects
     */
    public List<ApiLogDTO> findAll(Sort sort) {
        return helper.getApiLog().findAll(sort).stream()
                 
                .map(IntegrationMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes ApiLog entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(ApiLogSrchDTO searchDTO) {
        Specification<ApiLog> specification = IntegrationMapper.toSpec(searchDTO);
        return helper.getApiLog().delete(specification);
    }




    /**
     * Finds a list of ApiLog entities that match the given search criteria
     * and returns them as a list of ApiLogDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching ApiLogDTO objects
     */
    public List<ApiLogDTO> listBySearch(ApiLogSrchDTO searchDTO) {
        Specification<ApiLog> specification = IntegrationMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getApiLog().findAll(specification).stream()
                        
                        .map(IntegrationMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getApiLog().findAll(specification, sort).stream()
                
                .map(IntegrationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds ApiLog entities that match the given search criteria and returns them as a paginated list of ApiLogDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of ApiLogDTO objects
     */
    public Page<ApiLogDTO> findBySearch(ApiLogSrchDTO searchDTO) {
        Specification<ApiLog> specification = IntegrationMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<ApiLog> result = helper.getApiLog().findAll(specification, pageable);
        return result.map(IntegrationMapper::toDTO);
    }

    /**
     * Finds the first ApiLog entity that matches the given search criteria
     * and returns it as a ApiLogDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching ApiLogDTO object, or {@code null} if no match is found
     */
    public ApiLogDTO findFirstBySearch(ApiLogSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<ApiLogDTO> pages= findBySearch(searchDTO);
        List<ApiLogDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of ApiLog entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(ApiLogSrchDTO searchDTO) {
        Specification<ApiLog> specification = IntegrationMapper.toSpec(searchDTO);
        return helper.getApiLog().count(specification);
    }
    /**
     * Finds a ApiLog entity by its ID and converts it to a ApiLogDTO.
     *
     * @param logId the ID of the FaxResult entity
     * @return an Optional containing the ApiLogDTO if found, or an empty Optional if not found
     */
    public Optional<ApiLogDTO> findById(Long logId) {
        return helper.getApiLog().findById(logId).map(e->IntegrationMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new ApiLog entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the ApiLogDTO to save
     * @return the saved ApiLogDTO
     */
    @Transactional
    public ApiLogDTO save(ApiLogDTO dto) {
        ApiLog entity = IntegrationMapper.toEntity(dto, helper.getEntityManager());
        ApiLog result = helper.getApiLog().save(entity);
        return IntegrationMapper.toDTO(result);
    }


    /**
     * Updates an existing ApiLog entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the ApiLogDTO with updated information
     * @return the updated ApiLogDTO
     */
    @Transactional
    public ApiLogDTO update(ApiLogDTO dto) {
        ApiLog entity = helper.getApiLog().findById(dto.getLogId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid ApiLogDTO ID");
        IntegrationMapper.mapping(dto, entity, true, helper.getEntityManager());

        ApiLog result = helper.getApiLog().save(entity);
        return IntegrationMapper.toDTO(result);
    }

    /**
     * Batch updates multiple ApiLog entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of ApiLogDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<ApiLogDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (ApiLogDTO dto : dtoList) {
            ApiLog entity = helper.getApiLog().findById(dto.getLogId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid ApiLogDTO ID: " + dto.getLogId());
            }
            IntegrationMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getApiLog().save(entity);
        }
    }

    /**
     * Deletes a list of ApiLog entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of ApiLog representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<ApiLogKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (ApiLogKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getLogId()));
        }
        return result;
    }


    /**
     * Deletes a ApiLog entity by its ID.
     *
     * @param logId the ID of the ApiLog entity to delete
     * @return true if the ApiLog entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long logId) {
        helper.getApiLog().deleteById(logId);
        return !helper.getApiLog().existsById(logId);
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
    public int updateBySpecification(ApiLogDTO dto, ApiLogSrchDTO srchDTO) {
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
    public int updateBySpecification(ApiLogDTO dto, Specification<ApiLog> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<ApiLog> update = cb.createCriteriaUpdate(ApiLog.class);
        Root<ApiLog> root = update.from(ApiLog.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(ApiLog.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
