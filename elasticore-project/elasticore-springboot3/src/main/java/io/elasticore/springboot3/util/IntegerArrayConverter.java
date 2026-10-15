package io.elasticore.springboot3.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;

/**
 * JPA AttributeConverter for mapping {@code Integer[]} arrays to a single {@code String} column and vice versa.
 * <p>
 * Integers are joined or split using the {@code |} (pipe) character as the separator.
 * </p>
 */
@Converter
public class IntegerArrayConverter implements AttributeConverter<Integer[], String> {

    /**
     * Converts an {@code Integer[]} to a single {@code String} for database storage.
     *
     * @param attribute the array to convert
     * @return a {@code String} with array elements joined by {@code |}, or {@code null} if the attribute is {@code null}
     */
    @Override
    public String convertToDatabaseColumn(Integer[] attribute) {
        if (attribute == null) {
            return null;
        }
        return Arrays.stream(attribute)
                .map(String::valueOf)
                .reduce((a, b) -> a + "|" + b)
                .orElse(null);
    }

    /**
     * Converts a {@code String} from the database back to an {@code Integer[]} array.
     *
     * @param dbData the database value
     * @return an {@code Integer[]} split by {@code |}, or {@code null} if the database value is {@code null}
     */
    @Override
    public Integer[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return Arrays.stream(dbData.split("\\|"))
                .map(Integer::valueOf)
                .toArray(Integer[]::new);
    }
}
