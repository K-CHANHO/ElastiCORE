//ecd:231126177H20260301235721_V2.0
package io.elasticore.blueprint.domain.content.service;

import io.elasticore.blueprint.domain.content.entity.Attachment;
import io.elasticore.blueprint.domain.content.dto.*;
import io.elasticore.blueprint.domain.content.repository.*;
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
 * Comprehensive service layer for managing Attachment entities.
 *
 * Provides full CRUD operations, dynamic search with specification support,
 * pagination, sorting, and advanced query functions such as min/max/sum.
 * Delegates repository operations via ContentRepositoryHelper.
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AttachmentCoreService {

    protected final ContentRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Attachment> withJoinFetches() {
        return (root, query, cb) -> {
            return null;
        };
    }

    /**
     * Retrieves all Attachment entities, converts them to AttachmentDTO objects, and returns them as a list.
     *
     * @return a list of AttachmentDTO objects
     */
    public List<AttachmentDTO> findAll() {
        return helper.getAttachment().findAll().stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tAttachment entities, converts them to AttachmentDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of AttachmentDTO objects
     */
    public List<AttachmentDTO> findAll(Sort sort) {
        return helper.getAttachment().findAll(sort).stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Attachment entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(AttachmentSrchDTO searchDTO) {
        Specification<Attachment> specification = ContentMapper.toSpec(searchDTO);
        return helper.getAttachment().delete(specification);
    }




    /**
     * Finds a list of Attachment entities that match the given search criteria
     * and returns them as a list of AttachmentDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching AttachmentDTO objects
     */
    public List<AttachmentDTO> listBySearch(AttachmentSrchDTO searchDTO) {
        Specification<Attachment> specification = ContentMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getAttachment().findAll(specification).stream()
                        
                        .map(ContentMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getAttachment().findAll(specification, sort).stream()
                
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Attachment entities that match the given search criteria and returns them as a paginated list of AttachmentDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of AttachmentDTO objects
     */
    public Page<AttachmentDTO> findBySearch(AttachmentSrchDTO searchDTO) {
        Specification<Attachment> specification = ContentMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Attachment> result = helper.getAttachment().findAll(specification, pageable);
        return result.map(ContentMapper::toDTO);
    }

    /**
     * Finds the first Attachment entity that matches the given search criteria
     * and returns it as a AttachmentDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching AttachmentDTO object, or {@code null} if no match is found
     */
    public AttachmentDTO findFirstBySearch(AttachmentSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<AttachmentDTO> pages= findBySearch(searchDTO);
        List<AttachmentDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Attachment entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(AttachmentSrchDTO searchDTO) {
        Specification<Attachment> specification = ContentMapper.toSpec(searchDTO);
        return helper.getAttachment().count(specification);
    }
    /**
     * Finds a Attachment entity by its ID and converts it to a AttachmentDTO.
     *
     * @param attachId the ID of the FaxResult entity
     * @return an Optional containing the AttachmentDTO if found, or an empty Optional if not found
     */
    public Optional<AttachmentDTO> findById(Long attachId) {
        return helper.getAttachment().findById(attachId).map(e->ContentMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Attachment entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the AttachmentDTO to save
     * @return the saved AttachmentDTO
     */
    @Transactional
    public AttachmentDTO save(AttachmentDTO dto) {
        Attachment entity = ContentMapper.toEntity(dto, helper.getEntityManager());
        Attachment result = helper.getAttachment().save(entity);
        return ContentMapper.toDTO(result);
    }


    /**
     * Updates an existing Attachment entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the AttachmentDTO with updated information
     * @return the updated AttachmentDTO
     */
    @Transactional
    public AttachmentDTO update(AttachmentDTO dto) {
        Attachment entity = helper.getAttachment().findById(dto.getAttachId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid AttachmentDTO ID");
        ContentMapper.mapping(dto, entity, true, helper.getEntityManager());

        Attachment result = helper.getAttachment().save(entity);
        return ContentMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Attachment entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of AttachmentDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<AttachmentDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (AttachmentDTO dto : dtoList) {
            Attachment entity = helper.getAttachment().findById(dto.getAttachId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid AttachmentDTO ID: " + dto.getAttachId());
            }
            ContentMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getAttachment().save(entity);
        }
    }

    /**
     * Deletes a list of Attachment entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Attachment representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<AttachmentKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (AttachmentKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getAttachId()));
        }
        return result;
    }


    /**
     * Deletes a Attachment entity by its ID.
     *
     * @param attachId the ID of the Attachment entity to delete
     * @return true if the Attachment entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(Long attachId) {
        helper.getAttachment().deleteById(attachId);
        return !helper.getAttachment().existsById(attachId);
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
    public int updateBySpecification(AttachmentDTO dto, AttachmentSrchDTO srchDTO) {
        return updateBySpecification(dto, ContentMapper.toSpec(srchDTO));
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
    public int updateBySpecification(AttachmentDTO dto, Specification<Attachment> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Attachment> update = cb.createCriteriaUpdate(Attachment.class);
        Root<Attachment> root = update.from(Attachment.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Attachment.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
