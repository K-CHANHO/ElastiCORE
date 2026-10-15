//ecd:-860822574H20260301235721_V2.0
package io.elasticore.blueprint.domain.hr.service;

import io.elasticore.blueprint.domain.hr.entity.Employee;
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
 * Comprehensive service layer for managing Employee entities.
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
public class EmployeeCoreService {

    protected final HrRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Employee> withJoinFetches() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                // JOIN FETCH optimization enabled
                root.fetch("department", JoinType.LEFT);

                query.distinct(true);
            }
            return null;
        };
    }

    /**
     * Retrieves all Employee entities, converts them to EmployeeDTO objects, and returns them as a list.
     *
     * @return a list of EmployeeDTO objects
     */
    public List<EmployeeDTO> findAll() {
        return helper.getEmployee().findAll().stream()
                 
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tEmployee entities, converts them to EmployeeDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of EmployeeDTO objects
     */
    public List<EmployeeDTO> findAll(Sort sort) {
        return helper.getEmployee().findAll(sort).stream()
                 
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Employee entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(EmployeeSrchDTO searchDTO) {
        Specification<Employee> specification = HrMapper.toSpec(searchDTO);
        return helper.getEmployee().delete(specification);
    }




    /**
     * Finds a list of Employee entities that match the given search criteria
     * and returns them as a list of EmployeeDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching EmployeeDTO objects
     */
    public List<EmployeeDTO> listBySearch(EmployeeSrchDTO searchDTO) {
        Specification<Employee> specification = HrMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getEmployee().findAll(specification).stream()
                        
                        .map(HrMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getEmployee().findAll(specification, sort).stream()
                
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Employee entities that match the given search criteria and returns them as a paginated list of EmployeeDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of EmployeeDTO objects
     */
    public Page<EmployeeDTO> findBySearch(EmployeeSrchDTO searchDTO) {
        Specification<Employee> specification = HrMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Employee> result = helper.getEmployee().findAll(specification, pageable);
        return result.map(HrMapper::toDTO);
    }

    /**
     * Finds the first Employee entity that matches the given search criteria
     * and returns it as a EmployeeDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching EmployeeDTO object, or {@code null} if no match is found
     */
    public EmployeeDTO findFirstBySearch(EmployeeSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<EmployeeDTO> pages= findBySearch(searchDTO);
        List<EmployeeDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Employee entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(EmployeeSrchDTO searchDTO) {
        Specification<Employee> specification = HrMapper.toSpec(searchDTO);
        return helper.getEmployee().count(specification);
    }
    /**
     * Finds a Employee entity by its ID and converts it to a EmployeeDTO.
     *
     * @param empId the ID of the FaxResult entity
     * @return an Optional containing the EmployeeDTO if found, or an empty Optional if not found
     */
    public Optional<EmployeeDTO> findById(String empId) {
        return helper.getEmployee().findById(empId).map(e->HrMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Employee entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the EmployeeDTO to save
     * @return the saved EmployeeDTO
     */
    @Transactional
    public EmployeeDTO save(EmployeeDTO dto) {
        Employee entity = HrMapper.toEntity(dto, helper.getEntityManager());
        Employee result = helper.getEmployee().save(entity);
        return HrMapper.toDTO(result);
    }


    /**
     * Updates an existing Employee entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the EmployeeDTO with updated information
     * @return the updated EmployeeDTO
     */
    @Transactional
    public EmployeeDTO update(EmployeeDTO dto) {
        Employee entity = helper.getEmployee().findById(dto.getEmpId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid EmployeeDTO ID");
        HrMapper.mapping(dto, entity, true, helper.getEntityManager());

        Employee result = helper.getEmployee().save(entity);
        return HrMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Employee entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of EmployeeDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<EmployeeDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (EmployeeDTO dto : dtoList) {
            Employee entity = helper.getEmployee().findById(dto.getEmpId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid EmployeeDTO ID: " + dto.getEmpId());
            }
            HrMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getEmployee().save(entity);
        }
    }

    /**
     * Deletes a list of Employee entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Employee representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<EmployeeKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (EmployeeKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getEmpId()));
        }
        return result;
    }


    /**
     * Deletes a Employee entity by its ID.
     *
     * @param empId the ID of the Employee entity to delete
     * @return true if the Employee entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(String empId) {
        helper.getEmployee().deleteById(empId);
        return !helper.getEmployee().existsById(empId);
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
    public int updateBySpecification(EmployeeDTO dto, EmployeeSrchDTO srchDTO) {
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
    public int updateBySpecification(EmployeeDTO dto, Specification<Employee> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Employee> update = cb.createCriteriaUpdate(Employee.class);
        Root<Employee> root = update.from(Employee.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Employee.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
