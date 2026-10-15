package io.elasticore.springboot3.mapper;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Page;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rows of a projecting query, as the DTO the query declares.
 *
 * A repository query whose result is a DTO - a report, a summary, a join -
 * returns tuples, and each column is set on the DTO property of the same
 * name: the alias in the query ({@code SELECT p.name AS partnerName}).
 * Matching ignores case and underscores, because a native query's aliases
 * come back as the database spells them (PARTNER_NAME on H2 and Oracle,
 * partner_name on PostgreSQL). Values are converted to the property's type -
 * a Long count into an Integer, a Timestamp into a LocalDateTime.
 */
public final class TupleMapper {

    private TupleMapper() {
    }

    private static final Map<Class<?>, Map<String, String>> PROPERTIES = new ConcurrentHashMap<>();

    public static <T> List<T> list(List<Tuple> rows, Class<T> type) {
        List<T> out = new ArrayList<>(rows == null ? 0 : rows.size());
        if (rows != null) for (Tuple row : rows) out.add(one(row, type));
        return out;
    }

    public static <T> Page<T> page(Page<Tuple> rows, Class<T> type) {
        return rows.map(row -> one(row, type));
    }

    public static <T> Optional<T> optional(Optional<Tuple> row, Class<T> type) {
        return row.map(r -> one(r, type));
    }

    public static <T> T one(Tuple row, Class<T> type) {
        if (row == null) return null;
        T target;
        try {
            target = type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(type.getName() + " needs a no-argument constructor to receive query rows", e);
        }
        BeanWrapper bean = new BeanWrapperImpl(target);
        bean.setConversionService(DefaultConversionService.getSharedInstance());
        Map<String, String> properties = propertiesOf(type, bean);
        for (TupleElement<?> element : row.getElements()) {
            String alias = element.getAlias();
            if (alias == null) {
                throw new IllegalStateException("A column of the query for " + type.getSimpleName()
                        + " has no alias. Name each column after the property it fills: SELECT x AS propertyName");
            }
            String property = properties.get(key(alias));
            if (property == null) continue;
            bean.setPropertyValue(property, row.get(element));
        }
        return target;
    }

    private static Map<String, String> propertiesOf(Class<?> type, BeanWrapper bean) {
        return PROPERTIES.computeIfAbsent(type, t -> {
            Map<String, String> byKey = new HashMap<>();
            for (PropertyDescriptor pd : bean.getPropertyDescriptors()) {
                if (pd.getWriteMethod() != null) byKey.put(key(pd.getName()), pd.getName());
            }
            return byKey;
        });
    }

    private static String key(String name) {
        return name.replace("_", "").toLowerCase(Locale.ROOT);
    }
}
