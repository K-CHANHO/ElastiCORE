package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.PortSpec;
import io.elasticore.base.model.pub.rules.SourceCode;
import io.elasticore.base.model.pub.rules.Syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP ports written as Spring HTTP service clients (env.yml httpPort: spring,
 * Spring Boot 4): an @HttpExchange interface Spring implements, one
 * configuration class per domain that registers the ports as service groups
 * and applies their authentication, and a properties file with each group's
 * base URL and timeouts.
 *
 * Every request goes out as the ElastiCORE port runtime sends it:
 *   a simple value named in the URL      -> path variable
 *   any other simple value               -> query parameter
 *   an object on a GET (a search DTO)    -> its properties as query parameters
 *   an object otherwise                  -> the JSON body (the last one)
 *   on multipart/form-data, an object    -> a part
 * An object sent as a query keeps its place in the method a caller sees: that
 * method is a default one that flattens it and calls the exchange.
 */
public final class SpringHttpPorts {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    /** The ElastiCORE port runtime's defaults, kept so a port answers as before. */
    static final int DEFAULT_CONNECT_MS = 10000;
    static final int DEFAULT_READ_MS = 90000;

    private SpringHttpPorts() {
    }

    /** The exchange's query map parameter: a name no DSL parameter is expected to take. */
    static final String QUERY_PARAM = "ecQuery";

    /**
     * The Spring group a port is registered as: lowercase and dashed, as
     * property keys read best, with a double dash between domain and port so
     * that domain erp-core's CreditPort and erp's CoreCreditPort stay apart.
     */
    public static String group(PortSpec p) {
        String id = p.id();
        int dot = id.indexOf('.');
        String domain = dot < 0 ? "" : id.substring(0, dot);
        return kebab(domain).toLowerCase(Locale.ROOT) + "--" + kebab(p.getName());
    }

    /**
     * Why a port cannot be a Spring HTTP service client and send what the
     * ElastiCORE port runtime sends - or null when it can. Such a port stays
     * on the runtime, and the build says why; the others of the domain move.
     */
    public static String runtimeOnlyReason(PortSpec p) {
        for (PortSpec.Operation op : p.operations()) {
            String verb = op.httpMethod() == null ? "GET" : op.httpMethod().toUpperCase(Locale.ROOT);
            String url = op.url() == null ? "" : op.url();
            String ct = op.contentType() == null ? "" : op.contentType().toLowerCase(Locale.ROOT);
            String where = op.name() + "()";

            if (ct.contains("x-www-form-urlencoded")) {
                return where + " sends a form: the runtime writes an object's fields as form data, a Spring client has no converter for it";
            }
            int objects = 0;
            boolean typedParts = false;
            for (String[] param : op.params()) {
                if (!PortSpec.Operation.isSimple(param)) objects++;
                if (param[0].startsWith("@MultipartContentType")) typedParts = true;
                if (PortSpec.Operation.isSimple(param) && PortSpec.Operation.typeOf(param).trim().endsWith("[]")) {
                    return where + " takes an array: the runtime sends it as one comma-joined value";
                }
            }
            if (ct.contains("multipart/form-data") && !typedParts && objects > 0) {
                return where + " is multipart without @contentType: the runtime sends the object's fields as the parts";
            }
            if (!"GET".equals(verb) && !ct.contains("multipart/form-data") && objects > 1) {
                return where + " takes " + objects + " objects: the runtime sends the last one and drops the others";
            }
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\{([^}/]+)}").matcher(url);
            while (m.find()) {
                boolean bound = false;
                for (String[] param : op.params()) {
                    if (param[1].equals(m.group(1)) && PortSpec.Operation.isSimple(param)) bound = true;
                }
                if (!bound) {
                    return where + " has {" + m.group(1) + "} in its URL and no simple parameter of that name: the runtime leaves it out";
                }
            }
        }
        return null;
    }

    /** The configuration class of a domain's Spring HTTP ports. */
    public static String configClassName(String domainClassName) {
        return domainClassName + "HttpServices";
    }

    /** Where a domain's port defaults are written, under src/main/resources. */
    public static String propertiesName(String domainName) {
        return domainName + "-http-services";
    }

    /** The interface's operations, as the methodList of portSpring.tmpl. */
    static void operations(SourceCode c, PortSpec p) {
        Syntax s = c.syntax();
        for (PortSpec.Operation op : p.operations()) {
            String verb = op.httpMethod() == null ? "GET" : op.httpMethod().toUpperCase(Locale.ROOT);
            String url = op.url() == null ? "" : op.url();
            String contentType = op.contentType();
            boolean multipart = contentType != null && contentType.toLowerCase(Locale.ROOT).contains("multipart/form-data");
            boolean get = "GET".equals(verb);

            // Which object travels as the body: the last one, as the runtime keeps it.
            int bodyIndex = -1;
            if (!get && !multipart) {
                for (int i = 0; i < op.params().size(); i++) {
                    if (!PortSpec.Operation.isSimple(op.params().get(i))) bodyIndex = i;
                }
            }

            List<String> exchangeParams = new ArrayList<String>();
            List<String> callerParams = new ArrayList<String>();
            List<String> callArgs = new ArrayList<String>();
            List<String> queryObjects = new ArrayList<String>();
            for (int i = 0; i < op.params().size(); i++) {
                String[] param = op.params().get(i);
                String type = PortSpec.Operation.typeOf(param);
                String name = param[1];
                callerParams.add(s.parameter(type, name));
                if (PortSpec.Operation.isSimple(param)) {
                    String binding = url.contains("{" + name + "}")
                            ? String.format("@PathVariable(%s) ", RuleText.quote(name))
                            : String.format("@RequestParam(name = %s, required = false) ", RuleText.quote(name));
                    exchangeParams.add(binding + s.parameter(type, name));
                    callArgs.add(name);
                } else if (multipart) {
                    exchangeParams.add(String.format("@RequestPart(name = %s, required = false) ", RuleText.quote(name))
                            + s.parameter(type, name));
                    callArgs.add(name);
                } else if (i == bodyIndex) {
                    // A null object goes out with no body, as it did.
                    exchangeParams.add("@RequestBody(required = false) " + s.parameter(type, name));
                    callArgs.add(name);
                } else {
                    queryObjects.add(name);
                }
            }

            String returnType = s.nullableType(op.returnType());
            String exchangeName = queryObjects.isEmpty() ? op.name() : op.name() + "Exchange";
            if (!queryObjects.isEmpty()) {
                // A name no DSL parameter takes: search ports often have one called query.
                exchangeParams.add("@RequestParam " + s.parameter("MultiValueMap<String, String>", QUERY_PARAM));
                callArgs.add(String.format("HttpQuery.of(%s)", String.join(", ", queryObjects)));
            }

            c.varLine("methodList", "/*")
             .varLine("methodList", " * %s", op.description())
             .varLine("methodList", "*/");

            if (!queryObjects.isEmpty()) {
                // The method a caller sees keeps its objects; the exchange takes them as a query.
                boolean returns = !"void".equals(op.returnType()) && op.returnType() != null;
                String call = String.format("%s(%s)", exchangeName, String.join(", ", callArgs));
                if (s == Syntax.KOTLIN) {
                    // methodSignature leaves a void return out, as Kotlin wants it.
                    c.varLine("methodList", "%s = %s",
                            s.methodSignature("", returnType, op.name(), String.join(", ", callerParams)), call);
                } else {
                    c.varLine("methodList", "default %s %s(%s) {", returnType, op.name(), String.join(", ", callerParams))
                     .varLine("methodList", "    %s%s;", returns ? "return " : "", call)
                     .varLine("methodList", "}");
                }
                c.varLine("methodList", "");
            }

            c.varLine("methodList", "%s", exchangeAnnotation(verb, url, contentType, multipart));
            c.varLine("methodList", "%s", s.statement(s.methodSignature("", returnType, exchangeName,
                    String.join(", ", exchangeParams))))
             .varLine("methodList", "");
        }
    }

    private static String exchangeAnnotation(String verb, String url, String contentType, boolean multipart) {
        String kind;
        switch (verb) {
            case "GET": kind = "GetExchange"; break;
            case "POST": kind = "PostExchange"; break;
            case "PUT": kind = "PutExchange"; break;
            case "PATCH": kind = "PatchExchange"; break;
            case "DELETE": kind = "DeleteExchange"; break;
            default: kind = null;
        }
        StringBuilder sb = new StringBuilder();
        if (kind == null) {
            sb.append(String.format("@HttpExchange(method = %s, url = %s", RuleText.quote(verb), RuleText.quote(url)));
        } else {
            sb.append(String.format("@%s(url = %s", kind, RuleText.quote(url)));
        }
        // A body's type, where it is not JSON; a GET has none to declare.
        if (!"GET".equals(verb) && contentType != null && !contentType.isEmpty()
                && !DEFAULT_CONTENT_TYPE.equalsIgnoreCase(contentType)) {
            sb.append(String.format(", contentType = %s", RuleText.quote(multipart ? "multipart/form-data" : contentType)));
        }
        return sb.append(")").toString();
    }

    /** The properties file: each group's base URL and timeouts, overridable by the keys the runtime read. */
    public static String properties(String domainName, List<PortSpec> ports) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated by ElastiCORE from the ").append(domainName).append(" domain's HTTP ports. Do not edit.\n");
        sb.append("# Each value can be overridden in application.yml, under spring.http.serviceclient.<group>,\n");
        sb.append("# or by the port keys the ElastiCORE port runtime has always read ({domain}.{Port}.url, ...).\n");
        for (PortSpec p : ports) {
            String g = "spring.http.serviceclient." + group(p);
            Map<String, Integer> t = p.timeouts();
            sb.append('\n').append("# ").append(p.getName()).append('\n');
            sb.append(g).append(".base-url=${").append(p.id()).append(".url:")
              .append(p.url() == null ? "" : p.url()).append("}\n");
            sb.append(g).append(".connect-timeout=${").append(p.id()).append(".connectTimeout:")
              .append(timeoutDefault(t, "connectTimeout", "elasticore.http.connectTimeout", DEFAULT_CONNECT_MS)).append("}ms\n");
            sb.append(g).append(".read-timeout=${").append(p.id()).append(".readTimeout:")
              .append(timeoutDefault(t, "readTimeout", "elasticore.http.readTimeout", DEFAULT_READ_MS)).append("}ms\n");
        }
        return sb.toString();
    }

    /** What a Spring HTTP client has no setting for, said once per port. */
    public static List<String> unsupported(PortSpec p) {
        List<String> out = new ArrayList<String>();
        if (p.timeouts().containsKey("writeTimeout")) out.add("@timeout(write=...)");
        if (p.timeouts().containsKey("responseTimeout")) out.add("@timeout(response=...)");
        return out;
    }

    /**
     * What a timeout falls back to when the port key is not set, in the
     * runtime's order: the DSL's @timeout, else the global key, else the
     * runtime's default (HttpServiceProxyFactory.resolveTimeoutConfig).
     */
    private static String timeoutDefault(Map<String, Integer> timeouts, String key, String globalKey, int fallback) {
        Integer v = timeouts.get(key);
        return v != null ? String.valueOf(v) : "${" + globalKey + ":" + fallback + "}";
    }

    /** The configuration class: the groups, and the authentication of each. */
    public static String configuration(Syntax s, String packageName, String className, String propertiesName,
                                       List<PortSpec> ports) {
        StringBuilder sb = new StringBuilder();
        boolean kt = s == Syntax.KOTLIN;
        String end = kt ? "" : ";";
        sb.append("package ").append(packageName).append(end).append("\n\n");
        sb.append("import io.elasticore.springboot3.http.PortAuth").append(end).append('\n');
        sb.append("import org.springframework.context.ApplicationContext").append(end).append('\n');
        sb.append("import org.springframework.context.annotation.Bean").append(end).append('\n');
        sb.append("import org.springframework.context.annotation.Configuration").append(end).append('\n');
        sb.append("import org.springframework.context.annotation.PropertySource").append(end).append('\n');
        sb.append("import org.springframework.core.env.Environment").append(end).append('\n');
        sb.append("import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer").append(end).append('\n');
        sb.append("import org.springframework.web.service.registry.ImportHttpServices").append(end).append("\n\n");
        sb.append("/**\n");
        sb.append(" * The domain's HTTP ports as Spring HTTP service clients: one group per port,\n");
        sb.append(" * its base URL and timeouts from ").append(propertiesName).append(".properties, its\n");
        sb.append(" * authentication as the ElastiCORE port runtime applied it ({id}.apiKey, {id}.authKey,\n");
        sb.append(" * bean {id}.httpAuthProvider).\n");
        sb.append(" *\n");
        sb.append(" * Generated and managed by ElastiCORE.\n");
        sb.append(" */\n");
        sb.append(kt ? "@Configuration(proxyBeanMethods = false)\n" : "@Configuration(proxyBeanMethods = false)\n");
        // Beside this class, so a port namespace sent to another module takes its defaults along.
        sb.append(String.format("@PropertySource(\"classpath:%s/%s.properties\")\n",
                packageName.replace('.', '/'), propertiesName));
        for (PortSpec p : ports) {
            sb.append(String.format("@ImportHttpServices(group = \"%s\", types = %s)\n", group(p),
                    kt ? "[" + p.getName() + "::class]" : p.getName() + ".class"));
        }
        if (kt) {
            sb.append("class ").append(className).append(" {\n\n");
            sb.append("    @Bean\n");
            sb.append("    fun ").append(lowerFirst(className)).append("Auth(env: Environment, context: ApplicationContext): RestClientHttpServiceGroupConfigurer =\n");
            sb.append("        RestClientHttpServiceGroupConfigurer { groups ->\n");
            for (PortSpec p : ports) {
                sb.append(String.format("            groups.filterByName(\"%s\").forEachClient { _, builder ->\n", group(p)));
                sb.append(String.format("                builder.requestInterceptor(PortAuth.interceptor(env, context, \"%s\", %s))\n",
                        p.id(), p.authKey() == null ? "null" : RuleText.quote(p.authKey())));
                sb.append("                PortAuth.passErrorBodies(builder)\n");
                sb.append("            }\n");
            }
            sb.append("        }\n");
            sb.append("}\n");
        } else {
            sb.append("public class ").append(className).append(" {\n\n");
            sb.append("    @Bean\n");
            sb.append("    RestClientHttpServiceGroupConfigurer ").append(lowerFirst(className)).append("Auth(Environment env, ApplicationContext context) {\n");
            sb.append("        return groups -> {\n");
            for (PortSpec p : ports) {
                sb.append(String.format("            groups.filterByName(\"%s\").forEachClient((group, builder) -> {\n", group(p)));
                sb.append(String.format("                builder.requestInterceptor(PortAuth.interceptor(env, context, \"%s\", %s));\n",
                        p.id(), p.authKey() == null ? "null" : RuleText.quote(p.authKey())));
                sb.append("                PortAuth.passErrorBodies(builder);\n");
                sb.append("            });\n");
            }
            sb.append("        };\n");
            sb.append("    }\n");
            sb.append("}\n");
        }
        return sb.toString();
    }

    private static String lowerFirst(String s) {
        return s.isEmpty() ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static String kebab(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '-') sb.append('-');
                sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
