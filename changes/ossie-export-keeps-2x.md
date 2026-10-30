### Bug Fixes (2.x 코드 - `ossie-export` 와 함께)

#### 2.x 코드를 지키는 빌드도 `rules = ['ossie-export']` 를 쓸 수 있다
- 왜: 3.0.261028-rc3 부터 `output = 'v2'`(와 `-Pv2`)는 프로젝트 규칙과 함께 쓰면 거절한다 - 그 규칙은 2.x 템플릿에
  그려지지 않기 때문이다. 그런데 `ossie-export` 도 프로젝트 규칙으로 세었다. 이 규칙은 코드를 쓰지 않는 시맨틱 모델 내보내기의
  켜기 신호이고(`RuleCatalog` 의 등록 주석, `OssieExportRules` 는 비어 있음), 2.x 생성기도 같은 신호로 내보낸다
  (`ModelExtractor` 가 `OssieSemanticModelExporter.requested` 를 읽음). 그래서 `./gradlew elcore` 로 2.x 코드와
  `semantic_model.yml` 을 만들던 프로젝트가 `output = 'v2'` 를 적으면 `./gradlew elcore` 가 태스크를 구성하다 실패했고
  (`ddl` 을 켰으면 어느 빌드든), `elcore3 -Pv2` 는 "which only the 3.0 code carries" 라며 멈췄다. 선택을 적지 못하니 `generateBeforeCompile` 을 켜면 빌드가 `elcore3` 에서
  멈추고, `ddl` 의 `elcore` → `elcoreDdl` 연결도 받을 수 없었다
- 무엇: 플러그인의 `output = 'v2'` 검사(`keepsTwoX`)와 규칙 엔진의 `-Pv2` 검사가 `ossie-export` 를 빼고 센다. 다른
  프로젝트 규칙은 전처럼 거절한다(`ossie-export` 와 함께 적어도, 여러 번 적어도). 생성물은 그대로다 - 2.x 호환 번들에
  더해지는 `OssieExportRules` 는 아무것도 쓰지 않는다
- 멀티모듈(`modules`, 또는 하위 프로젝트에 `blueprint/` 가 있는 빌드)의 2.x 생성기(`ModelExtractor.extractMultiModule`)는
  시맨틱 모델을 내보내지 않았다 - 이 변경으로 거절이 풀리면 `semantic_model.yml` 없이, 아무 말 없이 지나갔다(리뷰에서 찾음).
  이제 단일 모듈과 같은 조건(`exportOssieIfRequested`)으로 첫 모듈의 `blueprint/`, 그 `schema.yml` 옆에 쓴다
- 확인: `DefaultGenerationTest.v2KeepsTheOssieExport`(`-Pv2` + `ossie-export` → 2.x 호환 코드와 `semantic_model.yml`),
  `ElastiCorePluginTest.outputV2KeepsTheOssieExport`(`output = 'v2'` + `ossie-export` → `:elcore`, 규칙 전달; 프로젝트
  규칙이 더해지면 거절) - 둘 다 고치기 전 코드에서 실패했다. `v2ExportsTheOssieModelOfAMultiModuleBuild`(모듈 둘, 두 도메인이
  든 `semantic_model.yml` 이 첫 모듈에) - 고치기 전 `extractMultiModule` 에서 실패. `ossieExportIsNoRuleSetOfTheCodeHoweverOftenItIsNamed`. `v2IsNotTheProjectsOwnRules` 는 프로젝트 규칙의 예로
  `ossie-export` 를 쓰고 있어, 규칙 개발 가이드의 예(`com.acme.rules.AcmeRules`)로 바꿨다. 도메인 9개짜리 2.x 프로젝트
  (`rules = ['ossie-export']`)에서 `output = 'v2'` 를 적고 `elcore`·`elcore3 -Pv2` 를 돌려 확인했다
- 관련 파일: `ElastiCorePlugin.java`(`keepsTwoX`), `RulesGenRunner.java`(`addedCodeRules`, `-Pv2` 검사), `ModelExtractor.java`(`exportOssieIfRequested`),
  `DefaultGenerationTest.java`, `ElastiCorePluginTest.java`, `elasticore-rule-guide.md`(어느 세대의 코드인가 표, Ossie 켜는 법)
