package io.elasticore.blueprint.domain.content;

import io.elasticore.blueprint.domain.content.dto.ContentCategoryDTO;
import io.elasticore.blueprint.domain.content.dto.ContentMapper;
import io.elasticore.blueprint.domain.content.entity.ContentCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * childCount and parentName are @formula fields: the database computes them
 * on read with an SQL expression whose condition has an '='. They are not
 * columns of the table, the expression reaches Hibernate whole and runs, and
 * a value sent in a DTO is not written into the entity.
 */
@DataJpaTest
class ContentCategoryFormulaTest {

    @Autowired
    TestEntityManager em;

    @Test
    void theDatabaseComputesThem() {
        ContentCategory parent = new ContentCategory();
        parent.setName("parent");
        parent.setSlug("parent");
        em.persist(parent);
        Long childId = null;
        for (int i = 0; i < 2; i++) {
            ContentCategory child = new ContentCategory();
            child.setName("child" + i);
            child.setSlug("child" + i);
            child.setParent(parent);
            em.persist(child);
            childId = child.getCategoryId();
        }
        em.flush();
        em.clear();

        ContentCategory read = em.find(ContentCategory.class, parent.getCategoryId());
        assertThat(read.getChildCount()).isEqualTo(2L);
        assertThat(read.getParentName()).isNull();
        assertThat(em.find(ContentCategory.class, childId).getParentName()).isEqualTo("parent");
    }

    @Test
    void theyAreNotColumns() {
        @SuppressWarnings("unchecked")
        List<String> columns = em.getEntityManager().createNativeQuery(
                "select column_name from information_schema.columns where table_name = 'CMS_CONTENT_CATEGORY'")
                .getResultList();
        assertThat(columns).contains("NAME", "PARENT_CATEGORY_ID");
        assertThat(columns).doesNotContain("CHILD_COUNT", "PARENT_NAME");
    }

    @Test
    void aDtoValueIsNotWrittenIn() {
        ContentCategoryDTO dto = new ContentCategoryDTO();
        dto.setName("x");
        dto.setChildCount(99L);
        dto.setParentName("y");
        ContentCategory entity = ContentMapper.toEntity(dto);
        assertThat(entity.getName()).isEqualTo("x");
        assertThat(entity.getChildCount()).isNull();
        assertThat(entity.getParentName()).isNull();
    }
}
