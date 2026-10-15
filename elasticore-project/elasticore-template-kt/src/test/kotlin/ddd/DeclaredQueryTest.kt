package ddd

import io.elasticore.blueprint.domain.shop.entity.Category
import io.elasticore.blueprint.domain.shop.repository.CategoryRepositoryPort
import io.elasticore.blueprint.domain.shop.service.CategoryQueryPort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional

/**
 * The queries the repository DSL declares reach the ports: a question about
 * the aggregate on the domain's, a page on the application's - and they run.
 */
@SpringBootTest(classes = [KotlinShopApp::class])
@Transactional
class DeclaredQueryTest {

    @Autowired lateinit var categories: CategoryRepositoryPort
    @Autowired lateinit var categoryQuery: CategoryQueryPort

    private fun category(name: String, sortOrder: Int, active: Boolean): Category {
        val c = Category()
        c.name = name
        c.sortOrder = sortOrder
        c.active = active
        return categories.save(c)
    }

    @Test
    @DisplayName("a derived and a JPQL query on the domain port, a paged one on the query port")
    fun declaredQueriesRun() {
        category("KQ-1", 10, true)
        category("KQ-2", 25, true)
        category("KQ-3", 40, false)

        assertThat(categories.findByName("KQ-2").map { it.sortOrder }.orElse(null)).isEqualTo(25)
        assertThat(categories.findByName("none")).isEmpty

        assertThat(categories.findBySortRange(20, 45).map { it.name })
                .contains("KQ-2", "KQ-3").doesNotContain("KQ-1")

        val page = categoryQuery.findByActive(false, PageRequest.of(0, 10))
        assertThat(page.content.map { it.name }).contains("KQ-3").doesNotContain("KQ-1", "KQ-2")
    }
}
