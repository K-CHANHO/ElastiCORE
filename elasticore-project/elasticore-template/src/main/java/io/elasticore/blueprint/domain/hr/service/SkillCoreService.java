//ecd:1858738489H20260301235721_V2.0
package io.elasticore.blueprint.domain.hr.service;

import io.elasticore.blueprint.domain.hr.entity.Skill;
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
 * Comprehensive service layer for managing Skill entities.
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
public class SkillCoreService {

    protected final HrRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Skill> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all Skill entities, converts them to SkillDTO objects, and returns them as a list.
     *
     * @return a list of SkillDTO objects
     */
    public List<SkillDTO> findAll() {
        return helper.getSkill().findAll().stream()
                 
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tSkill entities, converts them to SkillDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of SkillDTO objects
     */
    public List<SkillDTO> findAll(Sort sort) {
        return helper.getSkill().findAll(sort).stream()
                 
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Skill entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(SkillSrchDTO searchDTO) {
        Specification<Skill> specification = HrMapper.toSpec(searchDTO);
        return helper.getSkill().delete(specification);
    }




    /**
     * Finds a list of Skill entities that match the given search criteria
     * and returns them as a list of SkillDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching SkillDTO objects
     */
    public List<SkillDTO> listBySearch(SkillSrchDTO searchDTO) {
        Specification<Skill> specification = HrMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getSkill().findAll(specification).stream()
                        
                        .map(HrMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getSkill().findAll(specification, sort).stream()
                
                .map(HrMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Skill entities that match the given search criteria and returns them as a paginated list of SkillDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of SkillDTO objects
     */
    public Page<SkillDTO> findBySearch(SkillSrchDTO searchDTO) {
        Specification<Skill> specification = HrMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Skill> result = helper.getSkill().findAll(specification, pageable);
        return result.map(HrMapper::toDTO);
    }

    /**
     * Finds the first Skill entity that matches the given search criteria
     * and returns it as a SkillDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching SkillDTO object, or {@code null} if no match is found
     */
    public SkillDTO findFirstBySearch(SkillSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<SkillDTO> pages= findBySearch(searchDTO);
        List<SkillDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Skill entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(SkillSrchDTO searchDTO) {
        Specification<Skill> specification = HrMapper.toSpec(searchDTO);
        return helper.getSkill().count(specification);
    }
    /**
     * Finds a Skill entity by its ID and converts it to a SkillDTO.
     *
     * @param skillId the ID of the FaxResult entity
     * @return an Optional containing the SkillDTO if found, or an empty Optional if not found
     */
    public Optional<SkillDTO> findById(Long skillId) {
        return helper.getSkill().findById(skillId).map(e->HrMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Skill entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the SkillDTO to save
     * @return the saved SkillDTO
     */
    @Transactional
    public SkillDTO save(SkillDTO dto) {
        Skill entity = HrMapper.toEntity(dto, helper.getEntityManager());
        Skill result = helper.getSkill().save(entity);
        return HrMapper.toDTO(result);
    }


    /**
     * Updates an existing Skill entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the SkillDTO with updated information
     * @return the updated SkillDTO
     */
    @Transactional
    public SkillDTO update(SkillDTO dto) {
        Skill entity = helper.getSkill().findById(dto.getSkillId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid SkillDTO ID");
        HrMapper.mapping(dto, entity, true, helper.getEntityManager());

        Skill result = helper.getSkill().save(entity);
        return HrMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Skill entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of SkillDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<SkillDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (SkillDTO dto : dtoList) {
            Skill entity = helper.getSkill().findById(dto.getSkillId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid SkillDTO ID: " + dto.getSkillId());
            }
            HrMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getSkill().save(entity);
        }
    }

    /**
     * Deletes a list of Skill entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Skill representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<SkillKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (SkillKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getSkillId()));
        }
        return result;
    }


    /**
     * Deletes a Skill entity by its ID.
     *
     * @param skillId the ID of the Skill entity to delete
     * @return true if the Skill entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long skillId) {
        helper.getSkill().deleteById(skillId);
        return !helper.getSkill().existsById(skillId);
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
    public int updateBySpecification(SkillDTO dto, SkillSrchDTO srchDTO) {
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
    public int updateBySpecification(SkillDTO dto, Specification<Skill> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Skill> update = cb.createCriteriaUpdate(Skill.class);
        Root<Skill> root = update.from(Skill.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Skill.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
