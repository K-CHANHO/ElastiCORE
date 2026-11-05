package ddd

import io.elasticore.blueprint.domain.refdata.entity.Code
import io.elasticore.blueprint.domain.refdata.entity.RegionCode
import io.elasticore.blueprint.domain.refdata.entity.StatusCode
import io.elasticore.blueprint.domain.refdata.repository.CodeRepository
import io.elasticore.springboot3.bean.ApplicationContextProvider
import io.elasticore.springboot3.bean.DbTransactionGateway
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional

/** The application of the refdata domain alone: KotlinShopApp scans the shop's. */
@SpringBootApplication(proxyBeanMethods = false)
@Import(DbTransactionGateway::class, ApplicationContextProvider::class)
@EnableJpaAuditing
@ComponentScan("io.elasticore.blueprint.domain.refdata")
@EntityScan("io.elasticore.blueprint.domain.refdata.entity")
@EnableJpaRepositories("io.elasticore.blueprint.domain.refdata.repository")
open class KotlinRefdataApp

/**
 * The Kotlin abstract root, on the generated schema: one repository reads the rows of its
 * subclasses' tables, each as its own class.
 */
@SpringBootTest(
    classes = [KotlinRefdataApp::class],
    properties = [
        "spring.datasource.url=jdbc:h2:mem:kt-abstract;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.defer-datasource-initialization=false",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:db/schema-h2.sql",
    ],
)
class AbstractEntityTest {

    @Autowired lateinit var jdbc: JdbcTemplate
    @Autowired lateinit var repository: CodeRepository

    @Test
    @Transactional
    fun oneRepositoryReadsEverySubclassTable() {
        jdbc.update("INSERT INTO ref_status_code (code, name) VALUES (?, ?)", "OPEN", "Open")
        jdbc.update("INSERT INTO ref_region_code (code, name, country) VALUES (?, ?, ?)", "SEL", "Seoul", "KR")

        val codes = repository.findAll().map { it.code }
        assertTrue(codes.containsAll(listOf("OPEN", "SEL")), codes.toString())
        assertTrue(repository.findById("OPEN").orElseThrow() is StatusCode)
        val region: Code = repository.findById("SEL").orElseThrow()
        assertEquals("KR", (region as RegionCode).country)
    }
}
