package io.elasticore.base.model.domainservice;

import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ComponentType;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.core.AbstractReplaceableModel;
import io.elasticore.base.model.core.BaseComponentIdentity;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An operation the domain performs that belongs to no single aggregate.
 *
 * Declared at the domain level rather than on an entity, which is not a
 * filing preference but the definition: a domain service exists precisely
 * because the operation is nobody's. Hanging it off a root - the cheaper
 * option, since every other meta annotation is entity-level - would have
 * contradicted the one thing that makes it this pattern rather than a method
 * somebody forgot to put on an aggregate.
 *
 *   domainService:
 *     Pricing:
 *       desc: What an order costs, which no single aggregate can answer
 *       uses: [Order, Product]
 *
 * {@code uses} names the aggregates the operation spans. It is not
 * decoration: it is what the generator wires in, and it is the sentence that
 * justifies the service existing at all. A service naming one aggregate is
 * usually a method that belongs on that aggregate instead, and the generator
 * says so rather than generating it.
 */
@Getter
public class DomainService extends AbstractReplaceableModel {

    private final MetaInfo metaInfo;
    private final List<String> uses;
    private final String description;

    /**
     * The events it reacts to, when this is a process manager (declared
     * under processManager: rather than domainService:). Empty otherwise.
     */
    private final List<String> on;

    private DomainService(ComponentIdentity id, List<String> uses,
                          String description, MetaInfo metaInfo, List<String> on) {
        super(id);
        this.uses = uses == null ? new ArrayList<String>() : uses;
        this.description = description;
        this.metaInfo = metaInfo == null ? MetaInfo.createEmpty() : metaInfo;
        this.on = on == null ? new ArrayList<String>() : on;
    }

    public static DomainService create(String domainId, String name, List<String> uses,
                                       String description, MetaInfo metaInfo) {
        BaseComponentIdentity identity =
                BaseComponentIdentity.create(ComponentType.SERVICE, domainId, name);
        return new DomainService(identity, uses, description, metaInfo, null);
    }

    /** A process manager: reacts to the named events, and may change the aggregates it uses. */
    public static DomainService processManager(String domainId, String name, List<String> on,
                                               List<String> uses, String description, MetaInfo metaInfo) {
        BaseComponentIdentity identity =
                BaseComponentIdentity.create(ComponentType.SERVICE, domainId, name);
        return new DomainService(identity, uses, description, metaInfo, on);
    }

    /** The events a process manager reacts to; empty for a domain service. */
    public List<String> getOn() {
        return Collections.unmodifiableList(on);
    }

    public boolean isProcessManager() {
        return !on.isEmpty();
    }

    /** The aggregates this operation spans, in declaration order. */
    public List<String> getUses() {
        return Collections.unmodifiableList(uses);
    }

    public MetaInfo getMeta() {
        return metaInfo;
    }
}
