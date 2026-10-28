### Build (변경 기록 - PR 마다 한 파일)

#### 변경은 `changes/<주제>.md` 에 적고, 릴리스할 때 `./gradlew assembleHistory` 가 이 이력으로 옮긴다
- 왜: 모든 풀 리퀘스트가 이 이력의 맨 위(현재 버전 섹션)를 같은 줄에서 고쳐, 하나가 머지되면 다른 것이 모두 충돌했다.
  템플릿 셋의 `blueprint/` 에 사본(이력 사본 하나와 빌드가 복사하는 가이드·가이드 색인)을 커밋해 두어 한 변경이 네 번
  충돌했다. 이틀 사이 외부 기여자의 풀 리퀘스트 여덟(#23~#26, #31, #33~#35)이 모두 이 파일들 때문에 충돌했다
- 이제 변경마다 `changes/` 에 파일 하나를 더한다(형식은 `changes/README.md` - 이 이력과 같은 `###`·`####` 항목, 버전 제목
  없이). 릴리스 커밋에서 `./gradlew assembleHistory` 가 조각을 이름 순으로 그 버전의 섹션(`3.0.261029-SNAPSHOT` 이면
  `## 3.0.261029`, 없으면 맨 위에 새로)에 넣고 지운다. Release 워크플로는 `changes/` 에 조각이 남아 있으면 업로드 전에 멈춘다
- 템플릿 셋의 `blueprint/elasticore-*.md`(가이드 9개, 가이드 색인, 이력 사본)는 더 이상 커밋하지 않는다(`.gitignore`).
  가이드와 색인은 빌드가 `elasticore-base` 의 원본에서 다시 쓰고, 이력 사본은 아무도 읽지 않았다. CI 의 `blueprint/` 비교는
  커밋하는 Ossie 모델과 스키마에 그대로 남는다
- `CONTRIBUTING.md`·PR 템플릿: 이력 대신 조각 파일, 최신 `main` 에서 브랜치를 따고 필요할 때만 다른 PR 위에 쌓는다,
  `blueprint/` 의 가이드 사본은 커밋하지 않는다. `RELEASING.md` 2단계에 `assembleHistory`
- 확인: 조각 둘을 이름 순으로 기존 `## 3.0.261029` 섹션 끝에 넣고 지우는 것, 섹션이 없을 때(`-PelasticoreVersion=
  3.0.261030-SNAPSHOT`) 맨 위에 새 섹션을 만드는 것을 실행해 보았다(시험한 이력은 되돌림)
- 관련 파일: `build.gradle`(`assembleHistory`), `changes/README.md`, `.github/workflows/release.yml`, `.gitignore`,
  `CONTRIBUTING.md`, `RELEASING.md`, `.github/PULL_REQUEST_TEMPLATE.md`, `.github/workflows/ci.yml`(주석)
