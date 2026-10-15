package io.elasticore.springboot3.security;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Who may do what to one kind of entity: the DSL's
 * {@code @access(read=public, create=user, cancel=owner)} at run time.
 *
 * <pre>
 *   public  anyone
 *   user    anyone signed in
 *   owner   the signed-in owner of the row (@owner) - and the administrator
 *   admin   the administrator only
 *   none    nobody; the application itself still may
 * </pre>
 *
 * An operation the declaration does not name takes "*" when it is given,
 * and admin otherwise: what was not opened stays closed. With no declaration
 * at all everything is public - the generated code then never asks.
 *
 * The application itself - inside {@link CurrentUser#runAsSystem} - passes every check.
 */
public final class AccessPolicy {

    public enum Level { PUBLIC, USER, OWNER, ADMIN, NONE }

    private final String entity;
    private final Map<String, Level> levels;
    private final Level fallback;
    private final String adminRole;

    private AccessPolicy(String entity, Map<String, Level> levels, Level fallback, String adminRole) {
        this.entity = entity;
        this.levels = levels;
        this.fallback = fallback;
        this.adminRole = adminRole == null || adminRole.trim().isEmpty() ? "ADMIN" : adminRole.trim();
    }

    /**
     * "read=public, create=user, *=admin". Null or empty is no declaration:
     * everything public.
     */
    public static AccessPolicy parse(String entity, String declaration, String adminRole) {
        Map<String, Level> levels = new LinkedHashMap<String, Level>();
        if (declaration == null || declaration.trim().isEmpty()) {
            return new AccessPolicy(entity, levels, Level.PUBLIC, adminRole);
        }
        Level fallback = Level.ADMIN;
        for (String part : declaration.split(",")) {
            String p = part.replace("'", "").replace("\"", "").trim();
            if (p.isEmpty()) continue;
            int eq = p.indexOf('=');
            if (eq <= 0) throw new IllegalArgumentException(entity + " @access: '" + p + "' is not operation=level");
            String operation = p.substring(0, eq).trim();
            Level level = level(entity, p.substring(eq + 1).trim());
            if ("*".equals(operation)) fallback = level;
            else levels.put(operation, level);
        }
        return new AccessPolicy(entity, levels, fallback, adminRole);
    }

    private static Level level(String entity, String text) {
        try {
            return Level.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(entity + " @access: '" + text
                    + "' is not one of public, user, owner, admin, none");
        }
    }

    public Level level(String operation) {
        Level level = levels.get(operation);
        return level == null ? fallback : level;
    }

    public boolean isAdmin(CurrentUser user) {
        return user.system() || (user.signedIn() && user.hasRole(adminRole));
    }

    /** Refuses the caller who may not do this at all: 401 for a visitor, 403 for the rest. */
    public void require(CurrentUser user, String operation) {
        if (user.system()) return;
        Level level = level(operation);
        switch (level) {
            case PUBLIC:
                return;
            case USER:
            case OWNER:
                if (!user.signedIn()) throw new NotSignedInException("Sign in to " + operation + " " + entity);
                return;
            case ADMIN:
                if (!user.signedIn()) throw new NotSignedInException("Sign in to " + operation + " " + entity);
                if (!user.hasRole(adminRole)) throw new ForbiddenException(operation + " " + entity + " is for the administrator");
                return;
            default:
                throw new ForbiddenException(operation + " " + entity + " is not allowed");
        }
    }

    /**
     * The same, for an operation over many rows at once - findAll, delete by
     * condition. An owner-level operation is not offered that way to anyone
     * but the administrator, since the rows are not checked one by one.
     */
    public void requireAll(CurrentUser user, String operation) {
        require(user, operation);
        if (level(operation) == Level.OWNER && !isAdmin(user)) {
            throw new ForbiddenException(operation + " " + entity + " is done one at a time, on your own");
        }
    }

    /** Whether the caller may do this to the row owned by ownerValue. */
    public boolean owns(CurrentUser user, String operation, Object ownerValue) {
        if (level(operation) != Level.OWNER || isAdmin(user)) return true;
        Object id = user.id();
        return id != null && ownerValue != null && String.valueOf(id).equals(String.valueOf(ownerValue));
    }

    /** The owner a search must be narrowed to, or null when the caller sees every row. */
    public Object ownerFilter(CurrentUser user, String operation) {
        if (level(operation) != Level.OWNER || isAdmin(user)) return null;
        return user.id();
    }

    /** The owner a new row is created for: the signed-in caller, unless that is the administrator. */
    public Object ownerToClaim(CurrentUser user) {
        if (isAdmin(user) || !user.signedIn()) return null;
        return user.id();
    }

    /** An identity, as the owner field's type holds it. */
    @SuppressWarnings("unchecked")
    public static <T> T as(Object id, Class<T> type) {
        if (id == null || type.isInstance(id)) return (T) id;
        String text = String.valueOf(id);
        if (type == Long.class) return (T) Long.valueOf(text);
        if (type == Integer.class) return (T) Integer.valueOf(text);
        if (type == String.class) return (T) text;
        if (type == java.util.UUID.class) return (T) java.util.UUID.fromString(text);
        throw new IllegalArgumentException("An identity of " + id.getClass().getName() + " is not a " + type.getName());
    }
}
