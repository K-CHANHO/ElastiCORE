//ecd:-1995494155H20260301235721_V2.0
package io.elasticore.blueprint.domain.hr.service;

import io.elasticore.blueprint.domain.hr.entity.Project;
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
 * Comprehensive service layer for managing Project entities.
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
public class ProjectCoreService {

    protected final HrRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Project> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all Project entities, converts them to ProjectDTO objects, and returns them as a list.
     *
     * @return a list of ProjectDTO objects
     */
    public List<ProjectDTO> findAll() {
        return helper.getProject().findAll().stream()
                 
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tProject entities, converts them to ProjectDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of ProjectDTO objects
     */
    public List<ProjectDTO> findAll(Sort sort) {
        return helper.getProject().findAll(sort).stream()
                 
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Project entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(ProjectSrchDTO searchDTO) {
        Specification<Project> specification = HrMapper.toSpec(searchDTO);
        return helper.getProject().delete(specification);
    }




    /**
     * Finds a list of Project entities that match the given search criteria
     * and returns them as a list of ProjectDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching ProjectDTO objects
     */
    public List<ProjectDTO> listBySearch(ProjectSrchDTO searchDTO) {
        Specification<Project> specification = HrMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getProject().findAll(specification).stream()
                        
                        .map(HrMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getProject().findAll(specification, sort).stream()
                
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Project entities that match the given search criteria and returns them as a paginated list of ProjectDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of ProjectDTO objects
     */
    public Page<ProjectDTO> findBySearch(ProjectSrchDTO searchDTO) {
        Specification<Project> specification = HrMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Project> result = helper.getProject().findAll(specification, pageable);
        return result.map(HrMapper::toDTO);
    }

    /**
     * Finds the first Project entity that matches the given search criteria
     * and returns it as a ProjectDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching ProjectDTO object, or {@code null} if no match is found
     */
    public ProjectDTO findFirstBySearch(ProjectSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<ProjectDTO> pages= findBySearch(searchDTO);
        List<ProjectDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Project entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(ProjectSrchDTO searchDTO) {
        Specification<Project> specification = HrMapper.toSpec(searchDTO);
        return helper.getProject().count(specification);
    }
    /**
     * Finds a Project entity by its ID and converts it to a ProjectDTO.
     *
     * @param projectCode the ID of the FaxResult entity
     * @return an Optional containing the ProjectDTO if found, or an empty Optional if not found
     */
    public Optional<ProjectDTO> findById(String projectCode) {
        return helper.getProject().findById(projectCode).map(e->HrMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Project entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the ProjectDTO to save
     * @return the saved ProjectDTO
     */
    @Transactional
    public ProjectDTO save(ProjectDTO dto) {
        Project entity = HrMapper.toEntity(dto, helper.getEntityManager());
        Project result = helper.getProject().save(entity);
        return HrMapper.toDTO(result);
    }


    /**
     * Updates an existing Project entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the ProjectDTO with updated information
     * @return the updated ProjectDTO
     */
    @Transactional
    public ProjectDTO update(ProjectDTO dto) {
        Project entity = helper.getProject().findById(dto.getProjectCode()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid ProjectDTO ID");
        HrMapper.mapping(dto, entity, true, helper.getEntityManager());

        Project result = helper.getProject().save(entity);
        return HrMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Project entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of ProjectDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<ProjectDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (ProjectDTO dto : dtoList) {
            Project entity = helper.getProject().findById(dto.getProjectCode()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid ProjectDTO ID: " + dto.getProjectCode());
            }
            HrMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getProject().save(entity);
        }
    }

    /**
     * Deletes a list of Project entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Project representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<ProjectKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (ProjectKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getProjectCode()));
        }
        return result;
    }


    /**
     * Deletes a Project entity by its ID.
     *
     * @param projectCode the ID of the Project entity to delete
     * @return true if the Project entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(String projectCode) {
        helper.getProject().deleteById(projectCode);
        return !helper.getProject().existsById(projectCode);
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
    public int updateBySpecification(ProjectDTO dto, ProjectSrchDTO srchDTO) {
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
    public int updateBySpecification(ProjectDTO dto, Specification<Project> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Project> update = cb.createCriteriaUpdate(Project.class);
        Root<Project> root = update.from(Project.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Project.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
