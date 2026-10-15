package io.elasticore.base.model.pub.rules.lang.java;

import io.elasticore.base.model.pub.rules.base.RuleText;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;
import io.elasticore.base.util.CodeTemplate;

import java.util.ArrayList;
import java.util.List;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The search DTO class around its properties, ported from the file-level part
 * of SearchDtoSrcFilePublisher.
 *
 * It shares the DTO template, which carries a sort column and direction of
 * its own behind an isSearchDTO switch. Below the search properties come the
 * sort code and, when the search pages, the page number and size - and each
 * of those capabilities is declared by an interface on the class.
 */
public class SearchDtoFileRules implements CodeRules {

    private static final int DEFAULT_PAGE_SIZE = 100;

    @Rule(value = "srchdto-file", target = "srchdto", order = 90, phase = Rule.Phase.TAIL)
    void file(EntitySpec e) {
        SourceCode c = ctx(e);
        String className = e.searchDtoClassName();

        c.template("dto")
         .var("packageName", e.pkg("dto"))
         .var("j2eePkgName", "jakarta")
         .var("className", className)
         .var("abstract", e.hasMeta("abstract") ? "abstract" : "")
         .var("extendInfo", "")
         .var("implementInfo", implementClause(e))
         .var("isSearchDTO", "true")
         .varLines("classAnnotationList", e.nativeClassAnnotationLines())
         ;

        body(e, c);

        // An interface named by its full package has to be imported; one named
        // by a bare class comes from the template's own imports.
        String sortable = e.sortableInterface();
        if (sortable != null && sortable.indexOf('.') >= 0) e.dtoImportOnce(sortable);
        String pageable = e.pageableInterface();
        if (e.searchPageSize() != null && pageable != null && pageable.indexOf('.') >= 0) {
            e.dtoImportOnce(pageable);
        }

        if (e.hasEnums()) c.var("enumPackageName", e.pkg("enumeration"));
        if (!e.dtoImports().isEmpty()) c.varLines("importList", e.dtoImports());
    }

    /**
     * Every search DTO sorts; one with a page size also pages. The interfaces
     * say so, and a project that named its own in env.yml gets that name.
     */
    private String implementClause(EntitySpec e) {
        StringBuilder sb = new StringBuilder("implements java.io.Serializable");
        sb.append(", ").append(interfaceName(e.sortableInterface(), "SortableObject"));
        if (e.searchPageSize() != null) {
            sb.append(", ").append(interfaceName(e.pageableInterface(), "PageableObject"));
        }
        return sb.toString();
    }

    /** The interface a project named for itself, or the generated one. */
    private String interfaceName(String declared, String fallback) {
        return RuleText.simpleName((declared == null || declared.isEmpty()) ? fallback : declared);
    }


    /** The search properties, then the sort and paging ones. */
    private void body(EntitySpec e, SourceCode c) {
        CodeTemplate.Paragraph body = c.paragraph("fieldList");

        // Split here rather than by the paragraph: the block's trailing
        // blank line is content, and the paragraph's splitter drops it.
        // A search DTO with no condition of its own has no block at all,
        // and an empty one would still contribute its separator line.
        String fields = c.renderFieldBlock(1);
        if (!fields.isEmpty()) {
            for (String line : RuleText.linesOf(fields)) {
                body.add((Object) line);
            }
        }

        body.add((Object) "");
        if (e.defaultSort() != null) {
            // @sort: the order a search answers in when the request names none.
            body.add((Object) "@Builder.Default");
            body.add("private String sortCode=\"%s\";", e.defaultSort());
        } else {
            body.add((Object) "private String sortCode;");
        }

        if (e.searchPageSize() != null) {
            body.add((Object) "");
            body.add((Object) "@Builder.Default");
            body.add((Object) "private int pageNumber=0;");
            body.add((Object) "");
            body.add((Object) "@Builder.Default");
            body.add("private int pageSize=%d;", e.searchPageSize());
        }
    }
}
