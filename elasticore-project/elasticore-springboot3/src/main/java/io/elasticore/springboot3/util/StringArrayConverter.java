package io.elasticore.springboot3.util;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter for mapping {@code String[]} arrays to a single {@code String} column and vice versa.
 * <p>
 * Strings are joined or split using the {@code |} (pipe) character as the separator.
 * </p>
 */
@Converter
public class StringArrayConverter implements AttributeConverter<String[], String> {

    /**
     * Converts a {@code String[]} to a single {@code String} for database storage.
     *
     * @param attribute the array to convert
     * @return a {@code String} with array elements joined by {@code |}, or {@code null} if the attribute is {@code null}
     */
    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        return attribute == null ? null : String.join("|", attribute);
    }

    /**
     * Converts a {@code String} from the database back to a {@code String[]} array.
     *
     * @param dbData the database value
     * @return a {@code String[]} split by {@code |}, or {@code null} if the database value is {@code null}
     */
    @Override
    public String[] convertToEntityAttribute(String dbData) {
        return dbData == null ? null : dbData.split("\\|");
    }
}