### Build (변경 기록 검사 - CI)

#### 풀 리퀘스트가 변경을 기록하는 방식을 CI 가 검사하고, 어기면 고칠 방법을 메시지로 알린다
- 왜: 변경을 `changes/<주제>.md` 에 적는 방식(같은 버전의 "변경 기록 - PR 마다 한 파일")은 `CONTRIBUTING.md` 와 PR 템플릿에만
  적혀 있어, 읽지 않은 기여자는 충돌을 겪고서야 알았다
- `ci.yml` 에 풀 리퀘스트에서만 도는 작업 "Change record" 를 더했다(`.github/scripts/change-record.sh`). 실패:
  `elasticore-history.md` 를 고친 PR(릴리스 브랜치 `version/*` 는 예외 - 조각을 이력에 모으는 곳), 배포되는 것
  (`elasticore-project/*/src/main/**`, 이력 제외)을 바꾸고 `changes/*.md` 를 더하지 않은 PR, 샘플 `blueprint/` 의 가이드 사본을
  커밋한 PR. 경고: `main` 보다 커밋 10개 넘게 뒤처진 브랜치. 각 메시지는 무엇을 어떻게 하라고 적는다
- 브랜치 이름은 기여자가 정하므로 명령에 끼워 넣지 않고 환경 변수로 스크립트에 넘긴다
- 확인: 로컬에서 경우마다 실행 - 이 브랜치(통과), 이력을 고친 커밋(실패, 오류 하나), 같은 커밋을 `version/` 브랜치로(통과),
  `src/main` 을 바꾸고 조각이 없는 커밋(실패), 조각을 더한 커밋(통과)
- 관련 파일: `.github/workflows/ci.yml`, `.github/scripts/change-record.sh`, `CONTRIBUTING.md`
