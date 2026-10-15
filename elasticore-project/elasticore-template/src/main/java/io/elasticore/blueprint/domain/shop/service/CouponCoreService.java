//ecd:-593075156H20260926235721_V3.0
package io.elasticore.blueprint.domain.shop.service;

import io.elasticore.blueprint.domain.shop.entity.Coupon;
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
 * Comprehensive service layer for managing Coupon entities.
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
public class CouponCoreService {

    protected final ShopRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Coupon> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all Coupon entities, converts them to CouponView objects, and returns them as a list.
     *
     * @return a list of CouponView objects
     */
    public List<CouponView> findAll() {
        return helper.getCoupon().findAll().stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tCoupon entities, converts them to CouponView objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of CouponView objects
     */
    public List<CouponView> findAll(Sort sort) {
        return helper.getCoupon().findAll(sort).stream()
                 
                .map(ShopMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Coupon entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(CouponQuery searchDTO) {
        Specification<Coupon> specification = ShopMapper.toSpec(searchDTO);
        return helper.getCoupon().delete(specification);
    }




    /**
     * Finds Coupon entities that match the given search criteria and returns them as a paginated list of CouponView.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of CouponView objects
     */
    public Page<CouponView> findBySearch(CouponQuery searchDTO) {
        Specification<Coupon> specification = ShopMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Coupon> result = helper.getCoupon().findAll(specification, pageable);
        return result.map(ShopMapper::toDTO);
    }

    /**
     * Finds the first Coupon entity that matches the given search criteria
     * and returns it as a CouponView object.
     *
     * @param searchDTO the search criteria
     * @return the first matching CouponView object, or {@code null} if no match is found
     */
    public CouponView findFirstBySearch(CouponQuery searchDTO) {
        Page<CouponView> pages= findBySearch(searchDTO);
        List<CouponView> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Coupon entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(CouponQuery searchDTO) {
        Specification<Coupon> specification = ShopMapper.toSpec(searchDTO);
        return helper.getCoupon().count(specification);
    }
    /**
     * Finds a Coupon entity by its ID and converts it to a CouponView.
     *
     * @param couponCode the ID of the FaxResult entity
     * @return an Optional containing the CouponView if found, or an empty Optional if not found
     */
    public Optional<CouponView> findById(String couponCode) {
        return helper.getCoupon().findById(couponCode).map(e->ShopMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Coupon entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the CouponView to save
     * @return the saved CouponView
     */
    @Transactional
    public CouponView save(CouponView dto) {
        Coupon entity = ShopMapper.toEntity(dto, helper.getEntityManager());
        Coupon result = helper.getCoupon().save(entity);
        return ShopMapper.toDTO(result);
    }


    /**
     * Updates an existing Coupon entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the CouponView with updated information
     * @return the updated CouponView
     */
    @Transactional
    public CouponView update(CouponView dto) {
        Coupon entity = helper.getCoupon().findById(dto.getCouponCode()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid CouponView ID");
        ShopMapper.mapping(dto, entity, true, helper.getEntityManager());

        Coupon result = helper.getCoupon().save(entity);
        return ShopMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Coupon entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of CouponView objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<CouponView> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (CouponView dto : dtoList) {
            Coupon entity = helper.getCoupon().findById(dto.getCouponCode()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid CouponView ID: " + dto.getCouponCode());
            }
            ShopMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getCoupon().save(entity);
        }
    }

    /**
     * Deletes a list of Coupon entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Coupon representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<CouponKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (CouponKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getCouponCode()));
        }
        return result;
    }


    /**
     * Deletes a Coupon entity by its ID.
     *
     * @param couponCode the ID of the Coupon entity to delete
     * @return true if the Coupon entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(String couponCode) {
        helper.getCoupon().deleteById(couponCode);
        return !helper.getCoupon().existsById(couponCode);
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
    public int updateBySpecification(CouponView dto, CouponQuery srchDTO) {
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
    public int updateBySpecification(CouponView dto, Specification<Coupon> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Coupon> update = cb.createCriteriaUpdate(Coupon.class);
        Root<Coupon> root = update.from(Coupon.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Coupon.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
