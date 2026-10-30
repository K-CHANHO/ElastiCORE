### Bug Fixes (Ossie 내보내기 - `unique_keys` 의 컬럼 이름)

#### `@Index` 의 `columnList` 에 속성 이름을 적어도 `unique_keys` 는 컬럼 이름으로
- 왜: `@Index(columnList="company_id,seqCd", unique="true")` 처럼 속성 이름을 적은 모델에서 `unique_keys` 가
  `[company_id, seqCd]` 로 나왔다. 데이터셋의 필드는 컬럼 이름(`seq_cd`)이라 키가 없는 필드를 가리켰다. Hibernate 는
  `columnList` 의 이름을 논리 이름으로 읽어 같은 이름 규칙으로 컬럼을 찾고(`seq_cd`), 프로젝트 DDL(`JpaSchemaReader`)도
  그렇게 읽는다 - 내보내기만 글자 그대로 옮겼다
- 무엇: `uniqueKeys` 가 `columnList` 의 이름마다 데이터셋의 필드를 찾는다(`keyColumn`) - 속성 이름이면 그 필드의 컬럼(`columnOf`,
  `@column` 이 있으면 그 이름), 컬럼 이름이면 대소문자와 상관없이 그 컬럼을 가진 필드의 것(`@column(HALL_ID)` 의 `HALL_ID` 를
  `hall_id` 로 낮추지 않음 - 리뷰에서 찾은 회귀), 어느 필드도 아니면 Hibernate 의 이름 규칙(`camelToSnake`). `@unique` 필드도
  데이터셋 필드와 같은 `columnOf` 로 - 그래서 `@unique` 와 같은 컬럼의 유일 `@Index` 는 키 하나
- 확인: `OssieUniqueKeysTest`(`columnList="hall_id,seatNo"` → `[hall_id, seat_no]`, 두 이름이 모두 데이터셋의 필드) - 고치기
  전 코드에서 둘 다 실패했다. `@column(BOX_ROW)`·`@column(BOX_NO) @unique` 와 `columnList="BOX_ROW,boxNo"`·`"box_no"` →
  `[BOX_ROW, BOX_NO]` 와 `[BOX_NO]` 한 번(`aColumnWrittenByColumnKeepsItsCaseAndAKeyIsWrittenOnce`) - 소문자로 바꾸던 코드에서 실패. 커밋된 샘플 둘(petstore·template-ddd)의 `semantic_model.yml` 은 그대로다 - 샘플의 `columnList` 는
  모두 컬럼 이름이다. 도메인 9개짜리 프로젝트에서 `idx_sequence_info_u(company_id,seqCd)` 의 Hibernate DDL 과 프로젝트 DDL 이
  모두 `seq_cd` 인 것을 확인했다
- 관련 파일: `OssieSemanticModelExporter.java`(`uniqueKeys`), `OssieUniqueKeysTest.java`, `elasticore-rule-guide.md`(Ossie 대응 표)
