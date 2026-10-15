package io.elasticore.base.model.pub.dsl;

import io.elasticore.base.util.CodeTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * `mode: dsl` loads its DTO template when the publisher is created, so a template
 * that is missing from the jar fails every DSL-mode generation.
 */
class DslcodeFilePublisherTemplateTest {

    @Test
    void defaultDtoTemplateIsOnTheClasspath() {
        assertNotNull(CodeTemplate.newInstance(DslcodeFilePublisher.DEFAULT_DTO_TEMPLATE));
    }
}
