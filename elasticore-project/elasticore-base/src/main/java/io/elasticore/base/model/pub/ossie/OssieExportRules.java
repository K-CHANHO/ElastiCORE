package io.elasticore.base.model.pub.ossie;

import io.elasticore.base.model.pub.rules.CodeRules;

/**
 * The opt-in, as a rule set.
 *
 * Deliberately empty: the export produces one project-level side-car, not
 * per-entity sources, so there is nothing for the engine to emit. What
 * registering it buys is the grammar - a project turns the export on the same
 * way it turns anything on, by naming a set in publisher.rules, instead of
 * learning a separate switch that exists only for this feature.
 *
 * {@link OssieSemanticModelExporter#requested} is the reader.
 */
public final class OssieExportRules implements CodeRules {
}
