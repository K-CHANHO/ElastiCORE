package io.elasticore.springboot3.entity;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

/**
 * An aggregate's members, handed out to be read.
 *
 * The generated Java root returns Collections.unmodifiableList from a getter
 * it writes by hand. A Kotlin property cannot do the same: its getter is the
 * only code that can reach the backing field, so the root's own add and
 * remove methods would go through the read-only view as well. This view
 * remembers the list behind it, and {@link #backing} is how the root - and
 * only code that means to - gets the real one back.
 *
 * Hibernate is unaffected: it reads and writes the field, not the property.
 */
public final class MemberList {

    private MemberList() {
    }

    /** A read-only view of the members, or null when there are none yet. */
    public static <T> List<T> view(List<T> members) {
        if (members == null || members instanceof View) return members;
        return new View<T>(members);
    }

    /** The list a view stands for; a list that is not a view is returned as it is. */
    @SuppressWarnings("unchecked")
    public static <T> List<T> backing(List<T> members) {
        return members instanceof View ? ((View<T>) members).members : members;
    }

    private static final class View<T> extends AbstractList<T> implements RandomAccess {
        private final List<T> members;

        View(List<T> members) {
            this.members = members;
        }

        @Override
        public T get(int index) {
            return members.get(index);
        }

        @Override
        public int size() {
            return members.size();
        }
    }
}
