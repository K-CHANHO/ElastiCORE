# Releasing ElastiCORE

A release is a tag. Pushing `v{version}` runs the Release workflow (`.github/workflows/release.yml`): it builds and
checks the tagged commit, signs every published artifact, uploads them to Maven Central and makes the GitHub release.

The version is `{major}.{minor}.{YYMMDD}`, e.g. `3.0.261020`. A pre-release adds `-alpha{n}`, `-beta{n}` or `-rc{n}`
(`3.0.261015-alpha1`); Maven and Gradle order it before any release of the same line, and its GitHub release is marked
as a pre-release. What is published:

| Artifact | Coordinates |
|---|---|
| Generator | `io.elasticore:elasticore-base` |
| Runtime library, Spring Boot 3 | `io.elasticore.springboot3:elasticore-springboot3` |
| Runtime library, Spring Boot 4 | `io.elasticore.springboot4:elasticore-springboot4` |
| Gradle plugin | `io.elasticore:elasticore-gradle-plugin`, and the marker `io.elasticore:io.elasticore.gradle.plugin` that `id 'io.elasticore'` resolves |

## Cutting a release

1. On `main` (or a `release/*` branch), everything passes:
   ```bash
   ./gradlew build
   ./gradlew :elasticore-project:elasticore-template:elcoreRulesCheck -Pstrict   # 0 DIFF
   ./gradlew :elasticore-project:elasticore-template:elcoreTreeCheck             # 0 differ / missing / extra
   ```
2. One commit names the version everywhere: `elasticoreVersion` in `gradle.properties` without `-SNAPSHOT`, the
   install snippets that carry it (`git grep -n <previous version>` finds them - the READMEs and the guides, whose
   copies in the template `blueprint/` folders the build refreshes), and the `## {version}` heading in
   `elasticore-history.md`.
3. Tag that commit and push the tag:
   ```bash
   git tag -a v3.0.261020 -m "ElastiCORE 3.0.261020"
   git push origin v3.0.261020
   ```
4. The workflow waits for approval when the `release` environment asks for one. It stops unless the tag,
   `gradle.properties` and the history name the same version and the commit is on `main` or a `release/*` branch.
5. Central validates the upload. By default the deployment then waits at
   [central.sonatype.com/publishing/deployments](https://central.sonatype.com/publishing/deployments): check it, then
   **Publish** (or **Drop**). The GitHub release is made as a draft - publish it once the artifacts are on Maven
   Central, usually within half an hour. With the environment variable `CENTRAL_PUBLISHING_TYPE` set to `AUTOMATIC`,
   Central publishes as soon as the upload is valid, and the workflow waits for that and publishes the GitHub release.
6. Set the next version: a `-SNAPSHOT` that sorts after the release, the next day's date (`3.0.261021-SNAPSHOT`).

A version on Maven Central never changes and is never removed. A deployment that fails validation or is dropped
publishes nothing: fix the cause, move the tag to the fixed commit and push it again. If a run fails after the
upload, drop that deployment on central.sonatype.com before running again.

## What the workflow needs

In the repository's **Settings → Environments → release**, with the deployment rule `v*` so that no other branch or
tag reaches these secrets:

| Secret | What |
|---|---|
| `CENTRAL_USERNAME`, `CENTRAL_PASSWORD` | A user token of a Central Portal account that owns the `io.elasticore` namespace (central.sonatype.com → View User Tokens → Generate User Token) |
| `SIGNING_KEY` | The ASCII-armored private key that signs the artifacts (`gpg --armor --export-secret-keys <fingerprint>`); Central verifies against the primary key, so sign with it, not with a subkey |
| `SIGNING_PASSWORD` | Its passphrase |

The public half of the signing key must be on a key server Central reads - `keyserver.ubuntu.com` or
`keys.openpgp.org` - before the first upload:
```bash
gpg --keyserver hkps://keyserver.ubuntu.com --send-keys <fingerprint>
```

## The bundle, locally

`./gradlew centralBundle` does what the workflow does short of the upload: it signs every published artifact, stages
it in `build/central-bundle/` and writes `build/central-bundle.zip`, after checking that every file has its signature
and checksums and every jar its sources and javadoc. It refuses a `-SNAPSHOT` version and a build without a key:
```bash
ORG_GRADLE_PROJECT_signingKey="$(cat signing-key.asc)" ORG_GRADLE_PROJECT_signingPassword=... ./gradlew centralBundle
```
`./gradlew publishToMavenLocal` needs neither. `./gradlew publish` also sends to a Maven repository of your own when
`elasticorePublishUrl`, `-Username` and `-Password` are set in `~/.gradle/gradle.properties`.

See `BRANCHING.md` for which branch a fix goes to.
