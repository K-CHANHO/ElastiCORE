package io.elasticore.base.model.loader.module;

import io.elasticore.base.model.ConstantParam;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.core.Items;
import io.elasticore.base.model.domainservice.DomainService;
import io.elasticore.base.model.loader.FileSource;
import io.elasticore.base.model.loader.ModelLoader;
import io.elasticore.base.model.loader.ModelLoaderContext;
import io.elasticore.base.util.ConsoleLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads the domainService section.
 *
 * A section of its own rather than a meta annotation, because a domain
 * service is defined by not belonging to an entity and every meta annotation
 * in this DSL is written on one. See {@link DomainService}.
 *
 *   domainService:
 *     Pricing:
 *       desc: What an order costs, which no single aggregate can answer
 *       uses: [Order, Product]
 */
public class DomainServiceModelLoader extends AbstractModelLoader
        implements ConstantParam, ModelLoader<DomainService> {

    public boolean loadModel(ModelLoaderContext ctx, FileSource source) {
        return loadModel(ctx, source.getInfoMap());
    }

    public boolean loadModel(ModelLoaderContext ctx, Map<String, Map> map) {
        boolean services = map.containsKey(ConstantParam.KEYNAME_DOMAIN_SERVICE);
        boolean processes = map.containsKey(KEYNAME_PROCESS_MANAGER);
        boolean metrics = map.containsKey(ConstantParam.KEYNAME_METRICS);
        if (metrics) loadMetrics(ctx, map.get(ConstantParam.KEYNAME_METRICS));
        if (!services && !processes) return metrics;

        if (services) loadModel(ctx, ctx.getDomainServiceItems(), map.get(ConstantParam.KEYNAME_DOMAIN_SERVICE));
        if (processes) loadProcessManagers(ctx, ctx.getDomainServiceItems(), map.get(KEYNAME_PROCESS_MANAGER));
        return true;
    }

    /**
     *   metrics:
     *     revenue:    sum(PurchaseOrder.total) where status != CANCELLED -- Revenue
     *     orderCount: count(PurchaseOrder) where status != CANCELLED     -- Orders
     *
     * Kept as written: what a line means is read where it is used (the
     * semantic model export), against the entities and enums of the model.
     */
    @SuppressWarnings("unchecked")
    private void loadMetrics(ModelLoaderContext ctx, Object section) {
        if (!(section instanceof Map)) {
            ConsoleLog.storeLog("DSL_WARN", String.format("[%s] metrics: is a list of name: line entries; it is left out.",
                    ctx.getDomainId()));
            return;
        }
        for (Map.Entry<Object, Object> e : ((Map<Object, Object>) section).entrySet()) {
            if (e.getKey() == null || e.getValue() == null) continue;
            String name = String.valueOf(e.getKey()).trim();
            if (ctx.getMetrics().containsKey(name)) {
                ConsoleLog.storeLog("DSL_WARN", String.format("[%s] metric %s is declared twice; the first is kept.",
                        ctx.getDomainId(), name));
                continue;
            }
            ctx.getMetrics().put(name, String.valueOf(e.getValue()).trim());
        }
    }

    /** The domain-level section of process managers: what reacts to which events. */
    static final String KEYNAME_PROCESS_MANAGER = "processManager";

    /**
     *   processManager:
     *     Billing:
     *       desc: Bills an order once it has shipped
     *       on: [SalesOrderShipped]
     *       uses: [Invoice]
     *
     * {@code on} names the events (the root and the event, as the class is
     * named without its Event suffix); {@code uses} the aggregates whose ports
     * it is handed. Held with the domain services - it is the same kind of
     * declaration, a domain-level place for behaviour - and told apart by
     * having events to react to.
     */
    private void loadProcessManagers(ModelLoaderContext ctx, Items<DomainService> items,
                                     Map<String, Map> processMap) {
        if (processMap == null) return;
        for (Map.Entry<String, Map> entry : processMap.entrySet()) {
            String name = entry.getKey();
            Map body = entry.getValue();
            List<String> on = listOf(body, "on");
            if (on.isEmpty()) {
                ConsoleLog.storeLog("DSL_WARN", String.format(
                        "[%s] processManager %s: 'on' names no event. A process manager exists to react to one.",
                        ctx.getDomainId(), name));
                continue;
            }
            String desc = body == null ? null : asText(body.get(ConstantParam.PROPERTY_DESC));
            MetaInfo metaInfo = parseMetaInfoObject(body, KEYNAME_PROCESS_MANAGER, name);
            items.addItem(DomainService.processManager(ctx.getDomainId(), name, on, listOf(body, "uses"), desc, metaInfo));
        }
    }

    private void loadModel(ModelLoaderContext ctx, Items<DomainService> items,
                           Map<String, Map> serviceMap) {
        if (serviceMap == null) return;

        for (Map.Entry<String, Map> entry : serviceMap.entrySet()) {
            String name = entry.getKey();
            Map body = entry.getValue();

            List<String> uses = usesOf(body);
            String desc = body == null ? null : asText(body.get(ConstantParam.PROPERTY_DESC));

            // Said at load time, where the line that caused it is still in
            // view. A service that spans nothing is a method on the aggregate
            // it names; one that spans one is the same thing with an extra
            // file. Neither is refused - the model may be mid-edit - but
            // neither passes silently either.
            if (uses.isEmpty()) {
                ConsoleLog.storeLog("DSL_WARN", String.format(
                        "[%s] domainService %s: 'uses' names no aggregate. A domain service"
                                + " exists to span more than one; without them there is nothing"
                                + " to wire and nothing that makes this not a method on an entity.",
                        ctx.getDomainId(), name));
            } else if (uses.size() == 1) {
                ConsoleLog.storeLog("DSL_WARN", String.format(
                        "[%s] domainService %s: spans only %s. An operation on one aggregate"
                                + " usually belongs on that aggregate.",
                        ctx.getDomainId(), name, uses.get(0)));
            }

            MetaInfo metaInfo = parseMetaInfoObject(body, ConstantParam.KEYNAME_DOMAIN_SERVICE, name);
            items.addItem(DomainService.create(ctx.getDomainId(), name, uses, desc, metaInfo));
        }
    }

    /** The aggregates named by 'uses', written as a list or as one name. */
    private List<String> usesOf(Map body) {
        return listOf(body, "uses");
    }

    /** A list-valued key, written as a YAML list or as comma-separated names. */
    private List<String> listOf(Map body, String key) {
        List<String> out = new ArrayList<String>();
        if (body == null) return out;

        Object declared = body.get(key);
        if (declared instanceof List) {
            for (Object each : (List) declared) {
                String name = asText(each);
                if (name != null && !name.isEmpty()) out.add(name);
            }
        } else {
            String single = asText(declared);
            if (single != null) {
                for (String part : single.split(",")) {
                    String name = part.trim();
                    if (!name.isEmpty()) out.add(name);
                }
            }
        }
        return out;
    }

    private String asText(Object value) {
        if (value == null) return null;
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    public void completeLoad() {
    }
}
