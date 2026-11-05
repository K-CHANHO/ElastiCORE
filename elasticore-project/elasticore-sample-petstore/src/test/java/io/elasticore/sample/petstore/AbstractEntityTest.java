package io.elasticore.sample.petstore;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
