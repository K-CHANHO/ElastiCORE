package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.ProtoServiceSpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;

import java.util.List;
import java.util.Map;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * Proto definition emission for the 3.0 clean mode (v3/proto.tmpl).
 *
 * A non-Java artifact driven by the same rule engine: no imports, no class
 * annotations, proto line comments, and an output identity of
 * src/main/proto/<Service>.proto rather than a Java package path.
 *
 * Differences from the 2.x ProtoSrcPublisher, both intentional:
 * - message order follows DECLARATION order; 2.x collects types into a
 *   HashSet, so its order is hash-dependent and varies between runs
 * - no trailing-whitespace lines; 2.x emits a blank paragraph line that
 *   picks up the template slot indentation
 *
 * Users suppress or layer rules exactly as with the Java artifacts, e.g.
 * suppress("v3-proto-enums") to keep enums out of the generated file.
 */
public class ProtoBaseRules implements CodeRules {

    private static final String FUNCS = "proto-funcs";
    private static final String MSGS = "proto-msgs";
    private static final String ENUMS = "proto-enums";

    /** The rpc entries of the service block, in declaration order. */
    @Rule(value = "v3-proto-rpcs", target = "proto", order = 10)
    void rpcs(ProtoServiceSpec s) {
        SourceCode c = ctx(s);
        List<ProtoServiceSpec.Rpc> list = s.rpcs();
        for (int i = 0; i < list.size(); i++) {
            ProtoServiceSpec.Rpc r = list.get(i);
            if (i > 0) c.line(FUNCS, "");
            String desc = r.description();
            if (desc != null && !desc.isEmpty()) {
                for (String dl : desc.split("\n")) {
                    c.line(FUNCS, "// %s", dl);
                }
            }
            c.line(FUNCS, "rpc %s (%s) returns (%s);", r.name(), r.inputType(), r.outputType());
        }
    }

    /** Top-level messages with their nested messages, in declaration order. */
    @Rule(value = "v3-proto-messages", target = "proto", order = 20)
    void messages(ProtoServiceSpec s) {
        SourceCode c = ctx(s);
        List<EntitySpec> list = s.messages();
        for (int i = 0; i < list.size(); i++) {
            EntitySpec msg = list.get(i);
            if (isNestedSomewhere(s, msg.getName())) continue;
            if (i > 0) c.line(MSGS, "");
            emitMessage(c, s, msg, "");
        }
    }

    /** Enum blocks; constants are numbered from zero as proto3 requires. */
    @Rule(value = "v3-proto-enums", target = "proto", order = 30)
    void enums(ProtoServiceSpec s) {
        SourceCode c = ctx(s);
        boolean first = true;
        for (Map.Entry<String, List<String>> en : s.enums().entrySet()) {
            if (!first) c.line(ENUMS, "");
            first = false;
            c.line(ENUMS, "enum %s {", en.getKey());
            List<String> constants = en.getValue();
            for (int i = 0; i < constants.size(); i++) {
                c.line(ENUMS, "  %s = %d;", constants.get(i), i);
            }
            c.line(ENUMS, "}");
        }
    }

    /** Assembles the template variables once every block is complete. */
    @Rule(value = "v3-proto-vars", target = "proto", order = 90)
    void vars(ProtoServiceSpec s) {
        SourceCode c = ctx(s);
        c.var("protoPackageName", s.javaPackage())
         .var("protoServiceName", s.getName())
         // The rpc slot sits inside the service block, so each entry takes
         // the slot's indentation; the message and enum slots start at the
         // left margin and carry their own.
         .varLines("protoFuncList", withoutTrailingBlanks(c.blockLines(FUNCS)))
         .var("protoMsgList", RuleText.stripTrailing(c.renderBlock(MSGS)))
         .var("protoEnumList", RuleText.stripTrailing(c.renderBlock(ENUMS)));
    }

    // ------------------------------------------------------------------

    private void emitMessage(SourceCode c, ProtoServiceSpec s, EntitySpec msg, String indent) {
        c.line(MSGS, "%smessage %s {", indent, msg.getName());
        int seq = 1;
        for (FieldSpec f : msg.fields()) {
            c.line(MSGS, "%s  %s %s = %d;", indent, f.protoTypeName(), f.name(), seq);
            seq++;
        }
        List<String> nestedNames = s.nestedOf(msg.getName());
        for (String nestedName : nestedNames) {
            EntitySpec nested = s.message(nestedName);
            if (nested == null) continue;
            c.line(MSGS, "");
            emitMessage(c, s, nested, String.format("%s  ", indent));
        }
        c.line(MSGS, "%s}", indent);
    }

    private boolean isNestedSomewhere(ProtoServiceSpec s, String messageName) {
        for (EntitySpec m : s.messages()) {
            if (s.nestedOf(m.getName()).contains(messageName)) return true;
        }
        return false;
    }

    /** The same trimming as stripTrailing, for a value carried as entries. */
    private static java.util.List<String> withoutTrailingBlanks(java.util.List<String> lines) {
        java.util.List<String> out = new java.util.ArrayList<String>(lines);
        while (!out.isEmpty() && out.get(out.size() - 1).trim().isEmpty()) {
            out.remove(out.size() - 1);
        }
        return out;
    }
}
