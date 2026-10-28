### Bug Fixes (규칙 엔진 - JPA 가 아닌 모드)

#### `mode: mongo` 도메인을 JPA 코드로 쓰던 것 - 이제 `elcore3` 가 거절한다
- 왜: env.yml 의 `config.mode`(`jpa`, `mongo`, `uml`, `dsl`, 쉼표로 여럿)는 2.x 생성기가 읽어 그에 맞는 코드나 문서를 쓴다.
  규칙 엔진은 모드를 읽지 않아, `mode: mongo` 도메인에 JPA 엔티티·리포지토리·서비스를 아무 말 없이 썼다(실행해서 확인 -
  `Note.java` 등 15개 파일, 종료 코드 0). 3.0.261028-rc3 부터 플러그인의 기본값이 `elcore3` 이므로, 선언 없이 시작한 MongoDB
  프로젝트가 그대로 받게 된다
- 이제 모드에 `jpa` 가 없는 도메인이 있으면 `elcore3` 가 아무 소스도 쓰지 않고 1 로 끝나며, MongoDB 코드는 2.x 생성기의
  것이니 `elasticore { output = 'v2' }`(빌드가 `elcore` 를 부름)나 `./gradlew elcore` 를 쓰라고 알린다. `jpa` 와 함께 적은
  `uml`·`dsl` 은 쓰지 않는다고 DSL 경고로 알리고 JPA 코드는 쓴다
- 확인: `DefaultGenerationTest` - `mode: mongo` 는 1 과 그 메시지, JPA 파일 없음(수정 전에는 0 과 JPA 파일 15개로 실패),
  `mode: jpa,uml` 은 경고와 함께 JPA 코드. 샘플의 도메인은 모두 `mode: jpa`
- 관련 파일: `RulesGenRunner.java`, `DefaultGenerationTest.java`, `elasticore-env-guide.md`(모드 절)
