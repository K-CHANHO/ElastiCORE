package io.elasticore.base.model.pub.rules;

/**
 * The domain's env.yml config section, as a rule reads it: a dotted key
 * under elasticore.config - "outbox", "table.prefix", or a project's own
 * such as "acme.topicPrefix". Answers null for a key that is not there.
 */
public interface ConfigSource {

    String get(String key);
}
