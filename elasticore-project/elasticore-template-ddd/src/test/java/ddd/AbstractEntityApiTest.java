package ddd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.elasticore.sample.ddd.catalog.entity.Code;
import io.elasticore.sample.ddd.catalog.entity.RegionCode;
import io.elasticore.sample.ddd.catalog.entity.StatusCode;
import io.elasticore.sample.ddd.catalog.repository.CodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * The plain v3 bundle's abstract root {@code Code}: one repository and one service read the rows of
 * its subclasses' tables, and there is no way to create one.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AbstractEntityApiTest {

    private static final String CODES = "/api/v1/catalog/code";

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private CodeRepository repository;

    @Test
    @Transactional
    void oneRepositoryReadsEverySubclassTable() {
        jdbc.update("INSERT INTO cat_status_code (code, name) VALUES (?, ?)", "OPEN", "Open");
        jdbc.update("INSERT INTO cat_region_code (code, name, country) VALUES (?, ?, ?)", "SEL", "Seoul", "KR");

        assertThat(repository.findAll()).extracting(Code::getCode).contains("OPEN", "SEL");
        assertThat(repository.findById("OPEN").orElseThrow()).isInstanceOf(StatusCode.class);
        assertThat(((RegionCode) repository.findById("SEL").orElseThrow()).getCountry()).isEqualTo("KR");
    }

    @Test
    void theApiReadsSearchesAndDeletesButDoesNotCreate() throws Exception {
        jdbc.update("INSERT INTO cat_status_code (code, name) VALUES (?, ?)", "DONE", "Done");
        jdbc.update("INSERT INTO cat_region_code (code, name, country) VALUES (?, ?, ?)", "BUS", "Busan", "KR");

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
