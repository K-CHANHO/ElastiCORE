### Bug Fixes (프로젝트 DDL - `-PddlBaseline` 으로 넘겨받은 이름)

#### 넘겨받은 이력의 외래키·인덱스를 그 이름으로 지우는 문장은 검토로 - 그 DB 는 다른 이름일 수 있다
- 왜: `-PddlBaseline` 은 직접 쓴 Flyway 이력이 만든 DB 위에서 상태를 지금 모델로 시작한다. 상태의 외래키·인덱스 이름은 모델이
  붙일 이름(`fk_{table}_{field}`, 30 자 넘으면 해시)인데, 그 DB 를 만든 것은 대개 Hibernate 이고 Hibernate 는 외래키를 해시로
  이름 붙이며(`FKr9vr3r6m7v6cwwn40ywlq22p1`) `@Index(unique)` 를 유일 제약으로 둔다. 마이그레이션은 외래키·인덱스 삭제를 실행
  문장으로 쓰므로, 넘겨받은 뒤 관계나 `@Index` 를 빼면 `DROP CONSTRAINT fk_...`(없는 이름)·`DROP INDEX ..._u`(제약이 쓰는
  인덱스)가 실패한다 - Flyway 가 실패하면 앱이 뜨지 않는다. 도메인 9개짜리 프로젝트에서 Hibernate 스키마와 프로젝트 DDL 을
  대조하면 테이블·컬럼·타입·외래키 대상은 모두 같고, 외래키 이름 69/69 와 유일 인덱스의 형태 32/32 가 달랐다
- 무엇: `-PddlBaseline` 이 쓴 상태는 그때의 외래키·인덱스 이름을 `takenOver` 로 적는다. 그 이름을 지우는 문장, 지운 뒤 같은
  이름으로 다시 만드는 문장, 그리고 상태가 SQL Server 이력을 가질 때 컬럼을 바꾸며 그 이름을 지웠다 다시 만드는 문장은 검토
  주석으로 쓰고 이유를 단다(그 DB 의 이름으로 바꿔 실행). 넘겨받은 뒤 마이그레이션이 만든 외래키·인덱스는 그 이름이 맞으므로
  전처럼 실행한다. 모델에 그 이름이 남아 있는 동안 `takenOver` 도 남는다. 이름은 바꾸지 않는다 - 3.0 의 `init` 으로 만든 DB 는
  지금 이름을 쓴다. `takenOver` 가 없는 상태 파일은 전과 같다(쓰는 내용도 바이트 단위로 같다)
- 확인: `DdlMigrationTest.aKeyNamedByAHistoryTakenOverIsNotDroppedAsWritten`(자기 이력 위에 baseline → 관계를 빼면
  `-- ALTER TABLE visit DROP CONSTRAINT fk_visit_customer;` 와 이유, 마이그레이션이 만든 `fk_visit_owner` 는 빼면 실행 문장),
  `SchemaDiffTest.anIndexByANameTakenOverIsNotDroppedAndMadeAgainAsWritten`(SQL Server 의 컬럼 넓힘이 넘겨받은 인덱스를
  건드리면 검토, 아니면 실행; 같은 이름으로 지우고 다시 만드는 둘 다 검토; 넘겨받지 않았으면 전처럼 실행) - 고치기 전 코드에서
  앞의 것은 실패하고 뒤의 것은 컴파일되지 않는다. 위 프로젝트(`db/migration` 의 자기 이력, 기본 위치 이력을 다루는 변경과 함께
  돌림)에서 baseline 이 이름 124 개(외래키 69, 인덱스 55)를 넘겨받고, 생성된 엔티티에서 관계 하나를 빼자 마이그레이션이
  `-- ALTER TABLE bid_accident DROP CONSTRAINT fk_bid_accident_assessor;` 와 이유를 검토로 썼다 - 그 DB 에서 이 외래키의 이름은
  Hibernate 의 `fkr9vr3r6m7v6cwwn40ywlq22p1` 이다
- 관련 파일: `Schema.java`(`takenOver`), `SchemaDiff.java`, `DdlRun.java`(baseline, 상태 쓰기), `DdlMigrationTest.java`,
  `SchemaDiffTest.java`, `elasticore-env-guide.md`(9-1 절)
