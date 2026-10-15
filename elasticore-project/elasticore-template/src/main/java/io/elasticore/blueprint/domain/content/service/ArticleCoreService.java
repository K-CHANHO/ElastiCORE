//ecd:520878082H20260301235721_V2.0
package io.elasticore.blueprint.domain.content.service;

import io.elasticore.blueprint.domain.content.entity.Article;
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
 * Comprehensive service layer for managing Article entities.
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
public class ArticleCoreService {

    protected final ContentRepositoryHelper helper;

    /**
     * Creates a Specification that applies JOIN FETCH to related entities to optimize query performance.
     * Prevents N+1 queries by eagerly loading relationships in a single query.
     *
     * @return a Specification that applies JOIN FETCH based on autoJoin configuration
     */
    protected Specification<Article> withJoinFetches() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                // JOIN FETCH optimization enabled
                root.fetch("category", JoinType.LEFT);

                query.distinct(true);
            }
            return null;
        };
    }

    /**
     * Retrieves all Article entities, converts them to ArticleDTO objects, and returns them as a list.
     *
     * @return a list of ArticleDTO objects
     */
    public List<ArticleDTO> findAll() {
        return helper.getArticle().findAll().stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all tArticle entities, converts them to ArticleDTO objects, and returns them as a list.
     *
     * @param sort the sort information
     * @return a list of ArticleDTO objects
     */
    public List<ArticleDTO> findAll(Sort sort) {
        return helper.getArticle().findAll(sort).stream()
                 
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Deletes Article entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities deleted
     */
    @Transactional
    public long delete(ArticleSrchDTO searchDTO) {
        Specification<Article> specification = ContentMapper.toSpec(searchDTO);
        return helper.getArticle().delete(specification);
    }




    /**
     * Finds a list of Article entities that match the given search criteria
     * and returns them as a list of ArticleDTO objects.
     * If sorting information is provided in the search criteria, the results are sorted accordingly.
     *
     * @param searchDTO the search criteria
     * @return a list of matching ArticleDTO objects
     */
    public List<ArticleDTO> listBySearch(ArticleSrchDTO searchDTO) {
        Specification<Article> specification = ContentMapper.toSpec(searchDTO);
        Sort sort = searchDTO.getSort();
        if(sort ==null) {
            return helper.getArticle().findAll(specification).stream()
                        
                        .map(ContentMapper::toDTO)
                        .collect(Collectors.toList());
        }
        return helper.getArticle().findAll(specification, sort).stream()
                
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds Article entities that match the given search criteria and returns them as a paginated list of ArticleDTO.
     *
     * @param searchDTO the search criteria
     * @return a paginated list of ArticleDTO objects
     */
    public Page<ArticleDTO> findBySearch(ArticleSrchDTO searchDTO) {
        Specification<Article> specification = ContentMapper.toSpec(searchDTO).and(withJoinFetches());
        Pageable pageable = searchDTO.getPageable();
        Page<Article> result = helper.getArticle().findAll(specification, pageable);
        return result.map(ContentMapper::toDTO);
    }

    /**
     * Finds the first Article entity that matches the given search criteria
     * and returns it as a ArticleDTO object.
     *
     * @param searchDTO the search criteria
     * @return the first matching ArticleDTO object, or {@code null} if no match is found
     */
    public ArticleDTO findFirstBySearch(ArticleSrchDTO searchDTO) {
        searchDTO.setPageSize(1);
        Page<ArticleDTO> pages= findBySearch(searchDTO);
        List<ArticleDTO> list=pages.getContent();
        if(list==null || list.size()==0)
            return null;
        return list.get(0);
    }


    /**
     * Counts the number of Article entities that match the given search criteria.
     *
     * @param searchDTO the search criteria
     * @return the number of entities that match the search criteria
     */
    public long countBySearch(ArticleSrchDTO searchDTO) {
        Specification<Article> specification = ContentMapper.toSpec(searchDTO);
        return helper.getArticle().count(specification);
    }
    /**
     * Finds a Article entity by its ID and converts it to a ArticleDTO.
     *
     * @param articleId the ID of the FaxResult entity
     * @return an Optional containing the ArticleDTO if found, or an empty Optional if not found
     */
    public Optional<ArticleDTO> findById(String articleId) {
        return helper.getArticle().findById(articleId).map(e->ContentMapper.toDTO(e, MappingContext.withGuard(2)));
    }


    /**
     * Saves a new Article entity based on the given DTO and returns the saved entity as a DTO.
     *
     * @param dto the ArticleDTO to save
     * @return the saved ArticleDTO
     */
    @Transactional
    public ArticleDTO save(ArticleDTO dto) {
        Article entity = ContentMapper.toEntity(dto, helper.getEntityManager());
        Article result = helper.getArticle().save(entity);
        return ContentMapper.toDTO(result);
    }


    /**
     * Updates an existing Article entity based on the given DTO and returns the updated entity as a DTO.
     *
     * @param dto the ArticleDTO with updated information
     * @return the updated ArticleDTO
     */
    @Transactional
    public ArticleDTO update(ArticleDTO dto) {
        Article entity = helper.getArticle().findById(dto.getArticleId()).orElse(null);
        if(entity==null)
            throw new EntityNotFoundException("Invalid ArticleDTO ID");
        ContentMapper.mapping(dto, entity, true, helper.getEntityManager());

        Article result = helper.getArticle().save(entity);
        return ContentMapper.toDTO(result);
    }

    /**
     * Batch updates multiple Article entities in a single transaction.
     * If any update fails, all changes are rolled back.
     *
     * @param dtoList the list of ArticleDTO objects to update
     * @throws EntityNotFoundException if any entity is not found
     * @throws RuntimeException if any update operation fails
     */
    @Transactional
    public void batchUpdate(List<ArticleDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        for (ArticleDTO dto : dtoList) {
            Article entity = helper.getArticle().findById(dto.getArticleId()).orElse(null);
            if (entity == null) {
                throw new EntityNotFoundException("Invalid ArticleDTO ID: " + dto.getArticleId());
            }
            ContentMapper.mapping(dto, entity, true, helper.getEntityManager());
            helper.getArticle().save(entity);
        }
    }

    /**
     * Deletes a list of Article entities based on the given list of DTOs and returns a list of boolean results
     * indicating the success or failure of each deletion.
     *
     * @param dtoList the list of Article representing the entities to delete
     * @return a list of boolean values where each value represents the deletion success (true) or failure (false)
     *         for the corresponding ContractRelated entity
     */
    @Transactional
    public List<Boolean> delete(List<ArticleKeyDTO> dtoList) {
        List<Boolean> result = new ArrayList<>();
        for (ArticleKeyDTO dto : dtoList) {
            result.add(this.deleteById(dto.getArticleId()));
        }
        return result;
    }


    /**
     * Deletes a Article entity by its ID.
     *
     * @param articleId the ID of the Article entity to delete
     * @return true if the Article entity was successfully deleted, false if it still exists
     */
    @Transactional
    public boolean deleteById(String articleId) {
        helper.getArticle().deleteById(articleId);
        return !helper.getArticle().existsById(articleId);
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
    public int updateBySpecification(ArticleDTO dto, ArticleSrchDTO srchDTO) {
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
    public int updateBySpecification(ArticleDTO dto, Specification<Article> spec) {
        CriteriaBuilder cb = helper.getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Article> update = cb.createCriteriaUpdate(Article.class);
        Root<Article> root = update.from(Article.class);

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null)
                    update.set(root.get(field.getName()), value);
            } catch (IllegalAccessException e) { }
        }
        Predicate predicate = spec.toPredicate(root, cb.createQuery(Article.class), cb);
        if (predicate == null)
            throw new IllegalArgumentException("Update condition is missing. Full table update is not allowed.");
        update.where(predicate);
        return helper.getEntityManager().createQuery(update).executeUpdate();
    }
}
