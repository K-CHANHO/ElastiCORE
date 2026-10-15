package io.elasticore.base.extract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Where generated code goes.
 *
 * This is the gap that let a real defect live for months: every check this
 * project runs asks what a generated file contains, and none of them asked
 * where it was written. So generation leaked into the repository root of a
 * multi-module build on every compile, the golden-tree comparison stayed at
 * 142 MATCH / 0 DIFF throughout, and the 978 tests passing said nothing about
 * it.
 *
 * The output follows the model: the module is derived from the blueprint
 * path, which both entry paths know, and the root directory is the answer
 * only when that path cannot be read. These are the cases that decision has
 * to get right.
 */
public class GenerationTargetTest {

    private static final String FALLBACK = "/repo-root";

    @Test
    @DisplayName("a blueprint under a module names that module, not the working directory")
    public void blueprintNamesItsModule() {
        assertEquals("/repo/app-shop",
                ModelExtractor.moduleRootOf(
                        "/repo/app-shop/src/main/resources/blueprint", FALLBACK),
                "the module owning the DSL is where its code belongs");
    }

    @Test
    @DisplayName("the annotation processor's mixed separators name the same module")
    public void windowsPathsAreRead() {
        // What the processor actually hands over: a Windows project path with
        // a forward-slashed suffix appended. Read as one or the other alone,
        // this falls through to the fallback - which is precisely the bug.
        assertEquals("C:\\work\\repo\\app-shop",
                ModelExtractor.moduleRootOf(
                        "C:\\work\\repo\\app-shop/src/main/resources/blueprint", FALLBACK),
                "a module is a module however the platform spells its separators");

        assertEquals("C:\\work\\repo\\app-shop",
                ModelExtractor.moduleRootOf(
                        "C:\\work\\repo\\app-shop\\src\\main\\resources\\blueprint", FALLBACK));
    }

    @Test
    @DisplayName("a single-module project answers the same either way")
    public void singleModuleIsUnchanged() {
        // The CLI path sets elasticore.base.path to the project directory, so
        // the module and the root are already the same folder. This is why
        // every existing check kept passing while the processor path was
        // writing somewhere else entirely.
        assertEquals("/repo",
                ModelExtractor.moduleRootOf("/repo/src/main/resources/blueprint", "/repo"));
    }

    @Test
    @DisplayName("a blueprint somewhere unrecognised falls back rather than guessing")
    public void unrecognisedPathFallsBack() {
        assertEquals(FALLBACK,
                ModelExtractor.moduleRootOf("/repo/app-shop/elsewhere/blueprint", FALLBACK),
                "no suffix to strip means nothing was learned about the module");
        assertEquals(FALLBACK, ModelExtractor.moduleRootOf(null, FALLBACK));
    }

    @Test
    @DisplayName("the root is never the answer when the blueprint says otherwise")
    public void theRootDoesNotWin() {
        // The regression, stated as the thing that must not happen again: the
        // fallback is the Gradle root project during annotation processing,
        // and returning it here is what put 227 generated files beside the
        // modules instead of in them.
        String moduleRoot = ModelExtractor.moduleRootOf(
                "/repo/app-shop/src/main/resources/blueprint", "/repo");

        assertEquals("/repo/app-shop", moduleRoot);
    }
}
