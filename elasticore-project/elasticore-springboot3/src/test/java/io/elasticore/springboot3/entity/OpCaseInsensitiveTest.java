package io.elasticore.springboot3.entity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for the case-insensitive {@link Op} family
 * (ILIKE / NOT_ILIKE / ISTARTS_WITH / IENDS_WITH / IEQ).
 *
 * <p>Each operator must lower both sides: it applies {@code cb.lower(path)} on the column
 * and lower-cases the value, so matching is database-portable (not PostgreSQL-only ILIKE)
 * and mirrors the DSL {@code %__%} case-insensitive pattern. The mixed-case input "AuTo"
 * must therefore reach the CriteriaBuilder as the lower-cased pattern.</p>
 */
@DisplayName("Op case-insensitive family")
class OpCaseInsensitiveTest {

    @SuppressWarnings("unchecked")
    private Path<String> stringPath() {
        return (Path<String>) mock(Path.class);
    }

    @Nested
    @DisplayName("Case-insensitive operators lower both column and value")
    class LowersBothSides {

        @Test
        @DisplayName("ILIKE -> like(lower(path), %value-lowercased%)")
        void ilike() {
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            Path<String> path = stringPath();
            Expression<String> lowered = mock(Expression.class);
            Predicate expected = mock(Predicate.class);

            when(cb.lower(path)).thenReturn(lowered);
            when(cb.like(lowered, "%auto%")).thenReturn(expected);

            Predicate result = Op.ILIKE.toPredicate(cb, path, "AuTo");

            assertThat(result).isSameAs(expected);
            verify(cb).lower(path);
            verify(cb).like(lowered, "%auto%");
        }

        @Test
        @DisplayName("NOT_ILIKE -> notLike(lower(path), %value-lowercased%)")
        void notIlike() {
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            Path<String> path = stringPath();
            Expression<String> lowered = mock(Expression.class);
            Predicate expected = mock(Predicate.class);

            when(cb.lower(path)).thenReturn(lowered);
            when(cb.notLike(lowered, "%auto%")).thenReturn(expected);

            Predicate result = Op.NOT_ILIKE.toPredicate(cb, path, "AuTo");

            assertThat(result).isSameAs(expected);
            verify(cb).notLike(lowered, "%auto%");
        }

        @Test
        @DisplayName("ISTARTS_WITH -> like(lower(path), value-lowercased%)")
        void istartsWith() {
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            Path<String> path = stringPath();
            Expression<String> lowered = mock(Expression.class);
            Predicate expected = mock(Predicate.class);

            when(cb.lower(path)).thenReturn(lowered);
            when(cb.like(lowered, "auto%")).thenReturn(expected);

            Predicate result = Op.ISTARTS_WITH.toPredicate(cb, path, "AuTo");

            assertThat(result).isSameAs(expected);
            verify(cb).like(lowered, "auto%");
        }

        @Test
        @DisplayName("IENDS_WITH -> like(lower(path), %value-lowercased)")
        void iendsWith() {
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            Path<String> path = stringPath();
            Expression<String> lowered = mock(Expression.class);
            Predicate expected = mock(Predicate.class);

            when(cb.lower(path)).thenReturn(lowered);
            when(cb.like(lowered, "%auto")).thenReturn(expected);

            Predicate result = Op.IENDS_WITH.toPredicate(cb, path, "AuTo");

            assertThat(result).isSameAs(expected);
            verify(cb).like(lowered, "%auto");
        }

        @Test
        @DisplayName("IEQ -> equal(lower(path), value-lowercased)")
        void ieq() {
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            Path<String> path = stringPath();
            Expression<String> lowered = mock(Expression.class);
            Predicate expected = mock(Predicate.class);

            when(cb.lower(path)).thenReturn(lowered);
            when(cb.equal(lowered, "auto")).thenReturn(expected);

            Predicate result = Op.IEQ.toPredicate(cb, path, "AuTo");

            assertThat(result).isSameAs(expected);
            verify(cb).equal(lowered, "auto");
        }
    }

    @Nested
    @DisplayName("Backward compatibility: case-sensitive LIKE is unchanged")
    class CaseSensitiveUnchanged {

        @Test
        @DisplayName("LIKE keeps the original casing and does NOT lower the column")
        void likeRemainsCaseSensitive() {
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            Path<String> path = stringPath();
            Predicate expected = mock(Predicate.class);

            when(cb.like(path, "%AuTo%")).thenReturn(expected);

            Predicate result = Op.LIKE.toPredicate(cb, path, "AuTo");

            assertThat(result).isSameAs(expected);
            verify(cb).like(path, "%AuTo%");
            verify(cb, never()).lower(any());
        }
    }
}
