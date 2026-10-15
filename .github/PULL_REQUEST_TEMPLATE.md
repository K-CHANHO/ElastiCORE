## What and why

<!-- What changes for a user of ElastiCORE, and the problem it solves. -->

## Checklist

- [ ] `./gradlew build` passes
- [ ] `./gradlew :elasticore-project:elasticore-template:elcoreRulesCheck -Pstrict` - 0 DIFF
- [ ] `./gradlew :elasticore-project:elasticore-template:elcoreTreeCheck` - 0 differ / missing / extra
- [ ] A project that does not use this change generates exactly what it did before
- [ ] Tests that fail without the change
- [ ] Entry in `elasticore-history.md` (current version section)
- [ ] Guides updated (`elasticore-base/src/main/resources/elasticore-guide/`, English docs in `docs/`)
- [ ] Main Java sources in `elasticore-base` are ASCII only (`SourceAsciiTest`)
