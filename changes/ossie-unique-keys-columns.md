### Bug Fixes (Ossie 내보내기 - `unique_keys` 의 컬럼 이름)

#### `@Index` 의 `columnList` 에 속성 이름을 적어도 `unique_keys` 는 컬럼 이름으로
- 왜: `@Index(columnList="company_id,seqCd", unique="true")` 처럼 속성 이름을 적은 모델에서 `unique_keys` 가
  `[company_id, seqCd]` 로 나왔다. 데이터셋의 필드는 컬럼 이름(`seq_cd`)이라 키가 없는 필드를 가리켰다. Hibernate 는
  `columnList` 의 이름을 논리 이름으로 읽어 같은 이름 규칙으로 컬럼을 찾고(`seq_cd`), 프로젝트 DDL(`JpaSchemaReader`)도
  그렇게 읽는다 - 내보내기만 글자 그대로 옮겼다
- 무엇: `uniqueKeys` 가 `columnList` 의 이름마다 필드의 컬럼 이름과 같은 규칙(`StringUtils.camelToSnake`, `getDbColumnName`)을
  적용한다. 이미 컬럼 이름(`company_id`)이면 그대로다. `@unique` 필드는 전처럼 그 필드의 컬럼 이름
- 확인: `OssieUniqueKeysTest`(`columnList="hall_id,seatNo"` → `[hall_id, seat_no]`, 두 이름이 모두 데이터셋의 필드) - 고치기
  전 코드에서 둘 다 실패했다. 커밋된 샘플 둘(petstore·template-ddd)의 `semantic_model.yml` 은 그대로다 - 샘플의 `columnList` 는
  모두 컬럼 이름이다. 도메인 9개짜리 프로젝트에서 `idx_sequence_info_u(company_id,seqCd)` 의 Hibernate DDL 과 프로젝트 DDL 이
  모두 `seq_cd` 인 것을 확인했다
- 관련 파일: `OssieSemanticModelExporter.java`(`uniqueKeys`), `OssieUniqueKeysTest.java`, `elasticore-rule-guide.md`(Ossie 대응 표)
