package io.elasticore.sample.petstore;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.elasticore.sample.petstore.refdata.application.dto.RefdataMapper;
import io.elasticore.sample.petstore.refdata.application.dto.CaseNoteDTO;
import io.elasticore.sample.petstore.refdata.domain.model.CaseNote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The abstract root {@code Code} over HTTP, on the generated schema: it reads, searches and deletes
 * the rows of its subclasses' tables, and there is no way to create one.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:petstore-abstract;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.defer-datasource-initialization=false",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:db/schema-h2.sql",
        "spring.sql.init.data-locations=classpath:data.sql"})
@AutoConfigureMockMvc
@Import(AbstractEntityCheck.class)
class AbstractEntityTest {

    private static final String CODES = "/api/refdata/code";

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired AbstractEntityCheck codes;

    @Test
    void oneRepositoryReadsEverySubclassTable() {
        codes.readsEveryTable();
    }

    @Test
    void theApiReadsSearchesAndDeletesButDoesNotCreate() throws Exception {
        jdbc.update("INSERT INTO ref_status_code (code, name) VALUES (?, ?)", "DONE", "Done");
        jdbc.update("INSERT INTO ref_region_code (code, name, country) VALUES (?, ?, ?)", "BUS", "Busan", "KR");

        mvc.perform(get(CODES + "/DONE")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Done"));
        mvc.perform(post(CODES + "/search").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].code", hasItem("DONE")))
                .andExpect(jsonPath("$.content[*].code", hasItem("BUS")));

        mvc.perform(post(CODES).contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"NEW\",\"name\":\"New\"}"))
                .andExpect(status().isMethodNotAllowed());

        mvc.perform(delete(CODES + "/BUS")).andExpect(status().isOk());
        mvc.perform(get(CODES + "/BUS")).andExpect(status().isNotFound());
    }

    /** A relation to the root holds a proxy, and its column reads back through whichever table has the row. */
    @Test
    void aRelationToTheRootIsWrittenByItsKey() throws Exception {
        jdbc.update("INSERT INTO ref_region_code (code, name, country) VALUES (?, ?, ?)", "PUS", "Pusan", "KR");

        mvc.perform(post("/api/refdata/caseNote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Lost\",\"statusCode\":\"PUS\"}"))
                .andExpect(status().isOk());

        Integer rows = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ref_case_note WHERE title = ? AND status_code = ?", Integer.class, "Lost", "PUS");
        org.assertj.core.api.Assertions.assertThat(rows).isEqualTo(1);
    }

    /** Without an EntityManager there is no proxy and no stub to make, and it is not left unset without a word. */
    @Test
    void writingTheRelationWithoutAnEntityManagerSaysSo() {
        CaseNoteDTO dto = new CaseNoteDTO();
        dto.setStatusCode("PUS");

        assertThatThrownBy(() -> RefdataMapper.mapping(dto, new CaseNote()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("needs an EntityManager");
        assertThatThrownBy(() -> RefdataMapper.toEntity(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("needs an EntityManager");
    }
}
