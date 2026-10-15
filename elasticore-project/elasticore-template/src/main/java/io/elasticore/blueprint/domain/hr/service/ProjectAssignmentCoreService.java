//ecd:1212547537H20260301235721_V2.0
package io.elasticore.blueprint.domain.hr.service;

import io.elasticore.blueprint.domain.hr.entity.ProjectAssignment;
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

import io.elasticore.blueprint.domain.hr.entity.ProjectAssignmentIdentity;

/**
 * Comprehensive service layer for managing ProjectAssignment entities.
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
public class ProjectAssignmentCoreService {

    protected final HrRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<ProjectAssignment> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all ProjectAssignment entities, converts them to ProjectAssignmentDTO objects, and returns them as a list.
     *
     * @return a list of ProjectAssignmentDTO objects
     */
    public List<ProjectAssignmentDTO> findAll() {
        return helper.getProjectAssignment().findAll().stream()
                 
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tProjectAssignment entities, converts them to ProjectAssignmentDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of ProjectAssignmentDTO objects
     */
    public List<ProjectAssignmentDTO> findAll(Sort sort) {
        return helper.getProjectAssignment().findAll(sort).stream()
                 
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes ProjectAssignment entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(ProjectAssignmentSrchDTO searchDTO) {
        Specification<ProjectAssignment> specification = HrMapper.toSpec(searchDTO);
        return helper.getProjectAssignment().delete(specification);
    }




    /**
     * Finds a list of ProjectAssignment entities that match the given search criteria
     * and returns them as a list of ProjectAssignmentDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching ProjectAssignmentDTO objects
     */
    public List<ProjectAssignmentDTO> listBySearch(ProjectAssignmentSrchDTO searchDTO) {
        Specification<ProjectAssignment> specification = HrMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getProjectAssignment().findAll(specification).stream()
                        
                        .map(HrMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getProjectAssignment().findAll(specification, sort).stream()
                
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds ProjectAssignment entities that match the given search criteria and returns them as a paginated list of ProjectAssignmentDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of ProjectAssignmentDTO objects
     */
    public Page<ProjectAssignmentDTO> findBySearch(ProjectAssignmentSrchDTO searchDTO) {
        Specification<ProjectAssignment> specification = HrMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<ProjectAssignment> result = helper.getProjectAssignment().findAll(specification, pageable);
        return result.map(HrMapper::toDTO);
    }

    /**
     * Finds the first ProjectAssignment entity that matches the given search criteria
     * and returns it as a ProjectAssignmentDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching ProjectAssignmentDTO object, or {@code null} if no match is found
     */
    public ProjectAssignmentDTO findFirstBySearch(ProjectAssignmentSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<ProjectAssignmentDTO> pages= findBySearch(searchDTO);
        List<ProjectAssignmentDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of ProjectAssignment entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(ProjectAssignmentSrchDTO searchDTO) {
        Specification<ProjectAssignment> specification = HrMapper.toSpec(searchDTO);
        return helper.getProjectAssignment().count(specification);
    }
    /**
     * Finds a ProjectAssignment entity by its ID and converts it to a ProjectAssignmentDTO.
     *
     * @param new ProjectAssignmentIdentity(empId ,projectCode) the ID of the FaxResult entity
     * @return an Optional containing the ProjectAssignmentDTO if found, or an empty Optional if not found
     */
    public Optional<ProjectAssignmentDTO> findById(String empId ,String projectCode) {
        return helper.getProjectAssignment().findById(new ProjectAssignmentIdentity(empId ,projectCode)).map(e->HrMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new ProjectAssignment entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the ProjectAssignmentDTO to save
     * @return the saved ProjectAssignmentDTO
     */
    @Transactional
    public ProjectAssignmentDTO save(ProjectAssignmentDTO dto) {
        ProjectAssignment entity = HrMapper.toEntity(dto, helper.getEntityManager());
        ProjectAssignment result = helper.getProjectAssignment().save(entity);
        return HrMapper.toDTO(result);
    }


    /**
     * Updates an existing ProjectAssignment entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the ProjectAssignmentDTO with updated information
     * @return the updated ProjectAssignmentDTO
     */
    @Transactional
    public ProjectAssignmentDTO update(ProjectAssignmentDTO dto) {
        ProjectAssignment entity = helper.getProjectAssignment().findById(new ProjectAssignmentIdentity(dto.getEmpId() ,dto.getProjectCode())).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid ProjectAssignmentDTO ID");
        HrMapper.mapping(dto, entity, true, helper.getEntityManager());

        ProjectAssignment result = helper.getProjectAssignment().save(entity);
        return HrMapper.toDTO(result);
    }

    /**
     * Batch updates multiple ProjectAssignment entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of ProjectAssignmentDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<ProjectAssignmentDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (ProjectAssignmentDTO dto : dtoList) {
            ProjectAssignment entity = helper.getProjectAssignment().findById(new ProjectAssignmentIdentity(dto.getEmpId() ,dto.getProjectCode())).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid ProjectAssignmentDTO ID: " + new ProjectAssignmentIdentity(dto.getEmpId() ,dto.getProjectCode()));
            }
            HrMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getProjectAssignment().save(entity);
        }
    }

    /**
     * Deletes a list of ProjectAssignment entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of ProjectAssignment representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<ProjectAssignmentKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (ProjectAssignmentKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getEmpId() ,dto.getProjectCode()));
        }
        return result;
    }


    /**
     * Deletes a ProjectAssignment entity by its ID.
     *
     * @param new ProjectAssignmentIdentity(empId ,projectCode) the ID of the ProjectAssignment entity to delete
     * @return true if the ProjectAssignment entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(String empId ,String projectCode) {
        helper.getProjectAssignment().deleteById(new ProjectAssignmentIdentity(empId ,projectCode));
        return !helper.getProjectAssignment().existsById(new ProjectAssignmentIdentity(empId ,projectCode));
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
    public int updateBySpecification(ProjectAssignmentDTO dto, ProjectAssignmentSrchDTO srchDTO) {
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
    public int updateBySpecification(ProjectAssignmentDTO dto, Specification<ProjectAssignment> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<ProjectAssignment> update = cb.createCriteriaUpdate(ProjectAssignment.class);
        Root<ProjectAssignment> root = update.from(ProjectAssignment.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(ProjectAssignment.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
