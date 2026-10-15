//ecd:-1505339693H20260301235722_V2.0
package io.elasticore.blueprint.domain.shop.service;

import io.elasticore.blueprint.domain.shop.entity.VipProfile;
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
 * Comprehensive service layer for managing VipProfile entities.
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
public class VipProfileCoreService {

    protected final ShopRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<VipProfile> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all VipProfile entities, converts them to VipProfileDTO objects, and returns them as a list.
     *
     * @return a list of VipProfileDTO objects
     */
    public List<VipProfileDTO> findAll() {
        return helper.getVipProfile().findAll().stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tVipProfile entities, converts them to VipProfileDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of VipProfileDTO objects
     */
    public List<VipProfileDTO> findAll(Sort sort) {
        return helper.getVipProfile().findAll(sort).stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes VipProfile entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(VipProfileSrchDTO searchDTO) {
        Specification<VipProfile> specification = ShopMapper.toSpec(searchDTO);
        return helper.getVipProfile().delete(specification);
    }




    /**
     * Finds a list of VipProfile entities that match the given search criteria
     * and returns them as a list of VipProfileDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching VipProfileDTO objects
     */
    public List<VipProfileDTO> listBySearch(VipProfileSrchDTO searchDTO) {
        Specification<VipProfile> specification = ShopMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getVipProfile().findAll(specification).stream()
                        
                        .map(ShopMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getVipProfile().findAll(specification, sort).stream()
                
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds VipProfile entities that match the given search criteria and returns them as a paginated list of VipProfileDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of VipProfileDTO objects
     */
    public Page<VipProfileDTO> findBySearch(VipProfileSrchDTO searchDTO) {
        Specification<VipProfile> specification = ShopMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<VipProfile> result = helper.getVipProfile().findAll(specification, pageable);
        return result.map(ShopMapper::toDTO);
    }

    /**
     * Finds the first VipProfile entity that matches the given search criteria
     * and returns it as a VipProfileDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching VipProfileDTO object, or {@code null} if no match is found
     */
    public VipProfileDTO findFirstBySearch(VipProfileSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<VipProfileDTO> pages= findBySearch(searchDTO);
        List<VipProfileDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of VipProfile entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(VipProfileSrchDTO searchDTO) {
        Specification<VipProfile> specification = ShopMapper.toSpec(searchDTO);
        return helper.getVipProfile().count(specification);
    }
    /**
     * Finds a VipProfile entity by its ID and converts it to a VipProfileDTO.
     *
     * @param profileId the ID of the FaxResult entity
     * @return an Optional containing the VipProfileDTO if found, or an empty Optional if not found
     */
    public Optional<VipProfileDTO> findById(Long profileId) {
        return helper.getVipProfile().findById(profileId).map(e->ShopMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new VipProfile entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the VipProfileDTO to save
     * @return the saved VipProfileDTO
     */
    @Transactional
    public VipProfileDTO save(VipProfileDTO dto) {
        VipProfile entity = ShopMapper.toEntity(dto, helper.getEntityManager());
        VipProfile result = helper.getVipProfile().save(entity);
        return ShopMapper.toDTO(result);
    }


    /**
     * Updates an existing VipProfile entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the VipProfileDTO with updated information
     * @return the updated VipProfileDTO
     */
    @Transactional
    public VipProfileDTO update(VipProfileDTO dto) {
        VipProfile entity = helper.getVipProfile().findById(dto.getProfileId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid VipProfileDTO ID");
        ShopMapper.mapping(dto, entity, true, helper.getEntityManager());

        VipProfile result = helper.getVipProfile().save(entity);
        return ShopMapper.toDTO(result);
    }

    /**
     * Batch updates multiple VipProfile entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of VipProfileDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<VipProfileDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (VipProfileDTO dto : dtoList) {
            VipProfile entity = helper.getVipProfile().findById(dto.getProfileId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid VipProfileDTO ID: " + dto.getProfileId());
            }
            ShopMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getVipProfile().save(entity);
        }
    }

    /**
     * Deletes a list of VipProfile entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of VipProfile representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<VipProfileKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (VipProfileKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getProfileId()));
        }
        return result;
    }


    /**
     * Deletes a VipProfile entity by its ID.
     *
     * @param profileId the ID of the VipProfile entity to delete
     * @return true if the VipProfile entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long profileId) {
        helper.getVipProfile().deleteById(profileId);
        return !helper.getVipProfile().existsById(profileId);
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
    public int updateBySpecification(VipProfileDTO dto, VipProfileSrchDTO srchDTO) {
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
    public int updateBySpecification(VipProfileDTO dto, Specification<VipProfile> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<VipProfile> update = cb.createCriteriaUpdate(VipProfile.class);
        Root<VipProfile> root = update.from(VipProfile.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(VipProfile.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
