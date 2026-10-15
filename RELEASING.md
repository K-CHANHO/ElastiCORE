# Releasing ElastiCORE

How a version is cut.

## Cutting a release

1. On the release branch, everything passes - `build` also makes the sources and javadoc jars that publishing needs, and runs the plugin's tests:
   ```bash
   ./gradlew build
   ./gradlew :elasticore-project:elasticore-template:elcoreRulesCheck -Pstrict   # 0 DIFF
   ./gradlew :elasticore-project:elasticore-template:elcoreTreeCheck             # 0 differ / missing / extra
   ```
2. Set `elasticoreVersion` in `gradle.properties` to the release version - no `-SNAPSHOT`
   (`{major}.{minor}.{YYMMDD}`, e.g. `3.0.261003`).
3. Make sure `elasticore-history.md` has the section for that version.
4. Publish. The destination, the account and the signing passphrase come from `~/.gradle/gradle.properties`
   (or `-P`), the signing key from the environment - never from the repository. Without the signing key
   nothing is signed, and Maven Central refuses unsigned artifacts:
   ```properties
   elasticorePublishUrl=https://...
   elasticorePublishUsername=...
   elasticorePublishPassword=...
   signingPassword=...
   ```
   The armored key spans many lines, which a properties value cannot; pass it from the
   environment instead:
   ```bash
   ORG_GRADLE_PROJECT_signingKey="$(cat signing-key.asc)" ./gradlew publish
   ```
   The root `publish` also publishes the Gradle plugin (an included build); publishing it again on its
   own would upload the same version twice, which a release repository refuses.
5. Tag `v{version}` and write the GitHub release notes from the history section.
6. Set the next `-SNAPSHOT` version.

See `BRANCHING.md` for which branch a fix goes to.
