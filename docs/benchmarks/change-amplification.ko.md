<p align="right"><a href="change-amplification.md">English</a> · <b>한국어</b></p>

# 변경 증폭: DSL 한 줄이 움직이는 것

업무 규칙이 바뀌면 코드는 얼마나 바뀌고, 사람은 그중 얼마를 읽어야 하나? 이 벤치마크는 Pet Store 예제의 DSL 에 같은
업무 변경을 가하고, 생성된 애플리케이션이 얼마나 바뀌는지 셉니다. 같은 DSL 로 두 번 생성하면 같은 코드가 나오는지도
확인합니다.

## 결과

`elasticore-sample-petstore` 를 그 빌드가 생성하는 대로 측정: 헥사고날, Java, Spring Boot 4, Apache Ossie 내보내기,
TypeScript 클라이언트.

**모델**: 빈 줄이 아닌 DSL 151줄 -> 생성 파일 130개, 빈 줄이 아닌 9,341줄.

**결정성**: 같은 DSL 로 두 번 생성하면 **130개 파일 중 0개가 다름** (생성 파일 첫 줄 - 내용 해시와 생성 시각 - 은 제외).

| 업무 변경 | DSL 줄 | 생성 파일 | 생성 줄 | Java | TypeScript | 기타 | DSL 한 줄당 |
|---|---:|---:|---:|---:|---:|---:|---:|
| 검색 가능한 필드 추가 (Customer.nickname) | 1 | 9 | 39 | 26 | 6 | 7 | 39 |
| 생명주기 단계 추가 (deliver: SHIPPED -> DELIVERED) | 2 | 12 | 69 | 47 | 14 | 8 | 35 |
| 누가 할 수 있는지 변경 (cancel: owner -> admin) | 2 | 2 | 4 | 2 | 0 | 2 | 2 |
| 업무 규칙 추가 (가격 상한) | 2 | 5 | 12 | 5 | 6 | 1 | 6 |
| 검색 기본 정렬 (Category @sort) | 2 | 1 | 3 | 3 | 0 | 0 | 2 |

"줄" 은 diff 가 보여 주는 대로 지운 줄 + 더한 줄입니다 - 한 줄을 고치면 2, 한 줄을 더하면 1. "기타" 는 시맨틱 모델,
메시지 번들, 스키마입니다.

## 말해 주는 것

- **필드 하나는 코드 한 줄이 아닙니다.** 검색 가능한 필드 하나가 엔티티, DTO, 검색 DTO, 매퍼, 쿼리 메타데이터(`Q`),
  TypeScript DTO 와 검색 DTO, 스키마, 시맨틱 모델 - 9개 파일을 건드립니다. 손으로 쓰면 그 하나하나가 빠뜨릴 수 있는 곳이고, 여기서는 검토할
  것이 DSL 한 줄입니다.
- **생명주기 단계 하나는 파일 열두 개입니다.** enum (Java·TypeScript), 애그리거트의 명령과 검사, 유스케이스 포트,
  코어 서비스, REST 엔드포인트, 클라이언트 호출과 화면용 `canDeliver()`, 거절 문구의 메시지 코드, 스키마, 시맨틱 모델.
- **결정을 바꾸는 규칙은 작고 국소적입니다.** 누가 취소하는지, 가격 상한, 기본 정렬: 생성 diff 는 몇 줄이고, 바로 그
  규칙을 지키는 파일에만 있습니다.
- **재생성은 안전합니다.** 같은 DSL -> 같은 코드: 재생성은 아무것도 바꾸지 않거나 모델이 바꾼 것만 바꾸므로, 생성 diff 는
  한 줄씩 읽지 않아도 믿을 수 있습니다.

## 변경마다 건드린 파일

숫자는 그 파일에서 바뀐 줄 수입니다.

| 업무 변경 | 파일 |
|---|---|
| 검색 가능한 필드 추가 | `Customer.java` (4), `CustomerDTO.java` (4), `CustomerSrchDTO.java` (3), `PetstoreMapper.java` (10), `Q.java` (5), `CustomerDTO.ts` (4), `CustomerSrchDTO.ts` (2), `schema.yml` (1), `semantic_model.yml` (6) |
| 생명주기 단계 추가 | `PurchaseOrderBase.java` (16), `PurchaseOrderCoreService.java` (16), `PurchaseOrderController.java` (6), `PurchaseOrderUseCase.java` (2), `PurchaseOrderDTO.java` (2), `PurchaseOrderSrchDTO.java` (2), `OrderStatus.java` (3), `PurchaseOrderApi.ts` (10), `OrderStatus.ts` (4), `petstore-messages.properties` (1), `schema.yml` (3), `semantic_model.yml` (4) |
| 누가 할 수 있는지 변경 | `PurchaseOrderCoreService.java` (2), `semantic_model.yml` (2) |
| 업무 규칙 추가 | `ItemInvariants.java` (3), `ItemDTO.java` (2), `ItemInvariants.ts` (3), `ItemDTO.ts` (3), `petstore-messages.properties` (1) |
| 검색 기본 정렬 | `CategorySrchDTO.java` (3) |

## 말해 주지 않는 것

- AI 가 쓴 코드와 ElastiCORE 를 비교한 것이 아닙니다. 재는 것은 **검토 면적**입니다: ElastiCORE 에서 사람이 검토하는 변경은
  DSL diff 이고, 없으면 누군가 생성 열만큼을 쓰고 누군가 검토합니다.
- 생성된 줄이 모두 사람이 썼을 줄은 아닙니다. 손으로 쓴 애플리케이션은 모양이 다를 것입니다. 이 열은 규칙이 계층 전체에
  요구하는 일관된 변경의 양이지, 생산성 배수가 아닙니다.
- 예제 하나, 변경 다섯 개입니다. 시나리오와 세는 방법은 아래 테스트에 있습니다 - 직접 더해 보세요.
- 결정성은 한 JVM 안에서 두 번 생성해 확인합니다. 다른 기계에서의 같음은 커밋된 출력과 비교하는 호환 검사
  (`elcoreRulesCheck`)의 일입니다.

## 재현

```bash
ELASTICORE_BENCH=1 ./gradlew :elasticore-project:elasticore-base:test --tests "*ChangeAmplificationBenchmark"
cat elasticore-project/elasticore-base/build/bench/change-amplification.md
```

벤치마크는
[`ChangeAmplificationBenchmark`](../../elasticore-project/elasticore-base/src/test/java/io/elasticore/base/model/pub/rules/verify/ChangeAmplificationBenchmark.java)
입니다: 시나리오마다 DSL 의 새 복사본에 적용하고, 예제 빌드와 같은 설정으로 새 디렉터리에 생성해, 바꾸지 않은 모델을 같은
방식으로 생성한 것과 비교합니다. 일반 빌드에서는 돌지 않습니다.
