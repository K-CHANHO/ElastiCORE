# Change fragments

A pull request records what it changes as **one new file here**, not as an edit to
`elasticore-history.md`. Every pull request used to edit the top of the history at the same
lines, so every second one conflicted; a file of its own does not.

- **Name:** a short topic in kebab-case, e.g. `v3-join-fetch.md`. Fragments are assembled in the
  order of their names.
- **Content:** what the history holds, in the same form and language (Korean) - one `###` section
  with its `####` entries: why, before and after, how it was checked, the files it touched.
  No `## version` heading; the release adds it.
- **Several changes:** one fragment each, or several `###` sections in one.

```markdown
### Bug Fixes (v3 서비스 - `@fetchjoin`)

#### v3 CoreService 가 `@fetchjoin` 연관을 페이지 검색에서 루트와 함께 가져온다
- 왜: ...
- 확인: ...
- 관련 파일: ...
```

When a release is cut, `./gradlew assembleHistory` moves every fragment into the history section
of that version and deletes the fragments (RELEASING.md). The Release workflow refuses a tag
while a fragment is left here.
