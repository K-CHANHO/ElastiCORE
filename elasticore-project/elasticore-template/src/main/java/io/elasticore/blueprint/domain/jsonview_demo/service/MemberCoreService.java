//ecd:87996322H20260328122051_V2.0
package io.elasticore.blueprint.domain.jsonview_demo.service;

import io.elasticore.blueprint.domain.jsonview_demo.entity.Member;
import io.elasticore.blueprint.domain.jsonview_demo.dto.*;
import io.elasticore.blueprint.domain.jsonview_demo.repository.*;
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
 * Comprehensive service layer for managing Member entities.
 *
 * Provides full CRUD operations, dynamic search with specification support,
 * pagination, sorting, and advanced query functions such as min/max/sum.
 * Delegates repository operations via Jsonview_demoRepositoryHelper.
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class MemberCoreService {

    protected final Jsonview_demoRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Member> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all Member entities, converts them to MemberDTO objects, and returns them as a list.
     *
     * @return a list of MemberDTO objects
     */
    public List<MemberDTO> findAll() {
        return helper.getMember().findAll().stream()
                 
                .map(Jsonview_demoMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tMember entities, converts them to MemberDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of MemberDTO objects
     */
    public List<MemberDTO> findAll(Sort sort) {
        return helper.getMember().findAll(sort).stream()
                 
                .map(Jsonview_demoMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Member entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(MemberSrchDTO searchDTO) {
        Specification<Member> specification = Jsonview_demoMapper.toSpec(searchDTO);
        return helper.getMember().delete(specification);
    }




    /**
     * Finds a list of Member entities that match the given search criteria
     * and returns them as a list of MemberDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching MemberDTO objects
     */
    public List<MemberDTO> listBySearch(MemberSrchDTO searchDTO) {
        Specification<Member> specification = Jsonview_demoMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getMember().findAll(specification).stream()
                        
                        .map(Jsonview_demoMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getMember().findAll(specification, sort).stream()
                
                .map(Jsonview_demoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Member entities that match the given search criteria and returns them as a paginated list of MemberDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of MemberDTO objects
     */
    public Page<MemberDTO> findBySearch(MemberSrchDTO searchDTO) {
        Specification<Member> specification = Jsonview_demoMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Member> result = helper.getMember().findAll(specification, pageable);
        return result.map(Jsonview_demoMapper::toDTO);
    }

    /**
     * Finds the first Member entity that matches the given search criteria
     * and returns it as a MemberDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching MemberDTO object, or {@code null} if no match is found
     */
    public MemberDTO findFirstBySearch(MemberSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<MemberDTO> pages= findBySearch(searchDTO);
        List<MemberDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Member entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(MemberSrchDTO searchDTO) {
        Specification<Member> specification = Jsonview_demoMapper.toSpec(searchDTO);
        return helper.getMember().count(specification);
    }
    /**
     * Finds a Member entity by its ID and converts it to a MemberDTO.
     *
     * @param id the ID of the FaxResult entity
     * @return an Optional containing the MemberDTO if found, or an empty Optional if not found
     */
    public Optional<MemberDTO> findById(Long id) {
        return helper.getMember().findById(id).map(e->Jsonview_demoMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Member entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the MemberDTO to save
     * @return the saved MemberDTO
     */
    @Transactional
    public MemberDTO save(MemberDTO dto) {
        Member entity = Jsonview_demoMapper.toEntity(dto, helper.getEntityManager());
        Member result = helper.getMember().save(entity);
        return Jsonview_demoMapper.toDTO(result);
    }


    /**
     * Updates an existing Member entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the MemberDTO with updated information
     * @return the updated MemberDTO
     */
    @Transactional
    public MemberDTO update(MemberDTO dto) {
        Member entity = helper.getMember().findById(dto.getId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid MemberDTO ID");
        Jsonview_demoMapper.mapping(dto, entity, true, helper.getEntityManager());

        Member result = helper.getMember().save(entity);
        return Jsonview_demoMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Member entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of MemberDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<MemberDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (MemberDTO dto : dtoList) {
            Member entity = helper.getMember().findById(dto.getId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid MemberDTO ID: " + dto.getId());
            }
            Jsonview_demoMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getMember().save(entity);
        }
    }

    /**
     * Deletes a list of Member entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Member representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<MemberKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (MemberKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getId()));
        }
        return result;
    }


    /**
     * Deletes a Member entity by its ID.
     *
     * @param id the ID of the Member entity to delete
     * @return true if the Member entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long id) {
        helper.getMember().deleteById(id);
        return !helper.getMember().existsById(id);
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
    public int updateBySpecification(MemberDTO dto, MemberSrchDTO srchDTO) {
        return updateBySpecification(dto, Jsonview_demoMapper.toSpec(srchDTO));
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
    public int updateBySpecification(MemberDTO dto, Specification<Member> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Member> update = cb.createCriteriaUpdate(Member.class);
        Root<Member> root = update.from(Member.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Member.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
