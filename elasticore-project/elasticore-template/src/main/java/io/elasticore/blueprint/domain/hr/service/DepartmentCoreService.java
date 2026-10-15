//ecd:1969149451H20260301235721_V2.0
package io.elasticore.blueprint.domain.hr.service;

import io.elasticore.blueprint.domain.hr.entity.Department;
import io.elasticore.blueprint.domain.hr.dto.*;
import io.elasticore.blueprint.domain.hr.repository.*;
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
 * Comprehensive service layer for managing Department entities.
 *
 * Provides full CRUD operations, dynamic search with specification support,
 * pagination, sorting, and advanced query functions such as min/max/sum.
 * Delegates repository operations via HrRepositoryHelper.
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class DepartmentCoreService {

    protected final HrRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Department> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all Department entities, converts them to DepartmentDTO objects, and returns them as a list.
     *
     * @return a list of DepartmentDTO objects
     */
    public List<DepartmentDTO> findAll() {
        return helper.getDepartment().findAll().stream()
                 
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tDepartment entities, converts them to DepartmentDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of DepartmentDTO objects
     */
    public List<DepartmentDTO> findAll(Sort sort) {
        return helper.getDepartment().findAll(sort).stream()
                 
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Department entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(DepartmentSrchDTO searchDTO) {
        Specification<Department> specification = HrMapper.toSpec(searchDTO);
        return helper.getDepartment().delete(specification);
    }




    /**
     * Finds a list of Department entities that match the given search criteria
     * and returns them as a list of DepartmentDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching DepartmentDTO objects
     */
    public List<DepartmentDTO> listBySearch(DepartmentSrchDTO searchDTO) {
        Specification<Department> specification = HrMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getDepartment().findAll(specification).stream()
                        
                        .map(HrMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getDepartment().findAll(specification, sort).stream()
                
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Department entities that match the given search criteria and returns them as a paginated list of DepartmentDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of DepartmentDTO objects
     */
    public Page<DepartmentDTO> findBySearch(DepartmentSrchDTO searchDTO) {
        Specification<Department> specification = HrMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Department> result = helper.getDepartment().findAll(specification, pageable);
        return result.map(HrMapper::toDTO);
    }

    /**
     * Finds the first Department entity that matches the given search criteria
     * and returns it as a DepartmentDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching DepartmentDTO object, or {@code null} if no match is found
     */
    public DepartmentDTO findFirstBySearch(DepartmentSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<DepartmentDTO> pages= findBySearch(searchDTO);
        List<DepartmentDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Department entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(DepartmentSrchDTO searchDTO) {
        Specification<Department> specification = HrMapper.toSpec(searchDTO);
        return helper.getDepartment().count(specification);
    }
    /**
     * Finds a Department entity by its ID and converts it to a DepartmentDTO.
     *
     * @param deptId the ID of the FaxResult entity
     * @return an Optional containing the DepartmentDTO if found, or an empty Optional if not found
     */
    public Optional<DepartmentDTO> findById(Long deptId) {
        return helper.getDepartment().findById(deptId).map(e->HrMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Department entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the DepartmentDTO to save
     * @return the saved DepartmentDTO
     */
    @Transactional
    public DepartmentDTO save(DepartmentDTO dto) {
        Department entity = HrMapper.toEntity(dto, helper.getEntityManager());
        Department result = helper.getDepartment().save(entity);
        return HrMapper.toDTO(result);
    }


    /**
     * Updates an existing Department entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the DepartmentDTO with updated information
     * @return the updated DepartmentDTO
     */
    @Transactional
    public DepartmentDTO update(DepartmentDTO dto) {
        Department entity = helper.getDepartment().findById(dto.getDeptId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid DepartmentDTO ID");
        HrMapper.mapping(dto, entity, true, helper.getEntityManager());

        Department result = helper.getDepartment().save(entity);
        return HrMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Department entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of DepartmentDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<DepartmentDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (DepartmentDTO dto : dtoList) {
            Department entity = helper.getDepartment().findById(dto.getDeptId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid DepartmentDTO ID: " + dto.getDeptId());
            }
            HrMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getDepartment().save(entity);
        }
    }

    /**
     * Deletes a list of Department entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Department representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<DepartmentKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (DepartmentKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getDeptId()));
        }
        return result;
    }


    /**
     * Deletes a Department entity by its ID.
     *
     * @param deptId the ID of the Department entity to delete
     * @return true if the Department entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long deptId) {
        helper.getDepartment().deleteById(deptId);
        return !helper.getDepartment().existsById(deptId);
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
    public int updateBySpecification(DepartmentDTO dto, DepartmentSrchDTO srchDTO) {
        return updateBySpecification(dto, HrMapper.toSpec(srchDTO));
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
    public int updateBySpecification(DepartmentDTO dto, Specification<Department> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Department> update = cb.createCriteriaUpdate(Department.class);
        Root<Department> root = update.from(Department.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Department.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
