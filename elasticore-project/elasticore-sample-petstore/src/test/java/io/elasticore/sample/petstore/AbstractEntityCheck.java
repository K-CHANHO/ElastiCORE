package io.elasticore.sample.petstore;

import static org.assertj.core.api.Assertions.assertThat;

import io.elasticore.sample.petstore.refdata.domain.model.Code;
import io.elasticore.sample.petstore.refdata.domain.model.RegionCode;
import io.elasticore.sample.petstore.refdata.domain.model.StatusCode;
import io.elasticore.sample.petstore.refdata.infrastructure.persistence.CodeRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * A polymorphic findAll over TABLE_PER_CLASS, on whichever database the test runs: a row in each
 * subclass's table, both read through the abstract root's repository, each as its own class.
 */
class AbstractEntityCheck {

    @Autowired JdbcTemplate jdbc;
    @Autowired CodeRepository repository;

    @Transactional
    public void readsEveryTable() {
        jdbc.update("INSERT INTO ref_status_code (code, name) VALUES (?, ?)", "OPEN", "Open");
        jdbc.update("INSERT INTO ref_region_code (code, name, country) VALUES (?, ?, ?)", "SEL", "Seoul", "KR");

        List<Code> all = repository.findAll();

        assertThat(all.stream().map(Code::getCode).collect(Collectors.toList())).contains("OPEN", "SEL");
        assertThat(repository.findById("OPEN").orElseThrow()).isInstanceOf(StatusCode.class);
        RegionCode region = (RegionCode) repository.findById("SEL").orElseThrow();
        assertThat(region.getCountry()).isEqualTo("KR");
        assertThat(region.getName()).isEqualTo("Seoul");
    }
}
