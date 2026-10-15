# 업무 로직 DSL — 페이퍼 프로토타입

[설계안](../business-logic-dsl.md)의 어휘를 ddd 샘플의 `shop` 도메인에 실제로
써 본 것. **입력 하나, 기대 산출물 여섯.** 아직 생성기는 없다 — 이 디렉터리는
"이렇게 쓰면 이것이 나온다"를 코드 수준으로 고정해, L0-L2 구현의 골든 파일
후보가 된다.

```
shop/logic.yml                                  입력 (state / decision / usecase / policy / ai)
shop/generated/
  domain/model/Order.java                       state:    전이 메서드·can 질의·세터 봉인
  domain/model/ShippingFeeDecision.java         decision: 순수 함수
  application/OrderCoreService.placeOrder.java  usecase:  requires -> steps -> ensures
  application/OrderService.java                 custom 스텝의 generate-once 시그니처
  test/OrderTransitionsTest.java                전이표 전수 (허용 9 / 금지 33)
  test/ShippingFeeDecisionTest.java             결정표 행별 + 경계값
  test/PlaceOrderContractTest.java              requires 별 / examples 별 / ensures 계약
  agent/tools.json                              agent-export: MCP tool + effects
```

## 기존 blueprint 와의 접점

`logic.yml` 의 모든 이름은 이미 있는 선언을 가리킨다.

| logic.yml | 가리키는 곳 |
|---|---|
| `state.Order`, `field: status` | `entity.yml` 의 `Order.status: OrderStatus` |
| 상태 이름 `PENDING` … | `enum.yml` 의 `OrderStatus` 값 |
| `emits: Placed` | `Order` 의 `@event(Placed, Cancelled)` |
| `customerType == WHOLESALE` | `enum.yml` 의 `CustomerType` |
| `output: OrderDTO` | 생성되는 DTO |
| `custom: priceItems` | `OrderService` (generate-once) 에 시그니처 |

샘플에 실제로 적용하려면 두 줄이 더 필요하다:
`Order` 에 `shippingFee: bigdecimal(10.2) @default(0)`, `enum.yml` 에 `CancelReason`.

## 읽는 순서

1. `logic.yml` 의 `usecase.PlaceOrder` — requires/steps/ensures/examples 네 부분.
2. `OrderCoreService.placeOrder.java` — 네 부분이 코드에서 어디에 놓이는지.
3. `PlaceOrderContractTest.java` — 같은 네 부분이 테스트에서 어디에 놓이는지.
   특히 마지막 테스트: **틀리게 구현한 custom 스텝을 거절**하는 것이 LLM 이 채운
   본문에 대한 방어선이다.
4. `OrderService.java` — 사람/LLM 이 채우는 곳. 두 메서드뿐이다.

## 이 프로토타입이 보여주지 않는 것

- `process:` (사가). 설계안 4.5 의 모양 그대로이며 여기서는 생략.
- TypeScript 산출물. `ShippingFeeDecision` 과 `canConfirm(dto)` 만 브라우저로 간다.
- `ai:` 의 어댑터 코드. tool-use 로 출력 스키마를 강제하는 포트 프록시로, 기존
  `port:` HTTP 프록시와 같은 자리.
