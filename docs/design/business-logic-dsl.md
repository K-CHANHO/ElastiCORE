# 업무 로직 DSL 설계 — 구조를 넘어 행위로

작성: 2026-09-02. 상태: **연구 및 설계안** (구현 전).
근거 자료: [학술](business-logic-dsl-research-academic.md) ·
[산업 표준](business-logic-dsl-research-industry.md) ·
[LLM/AGI 시대](business-logic-dsl-research-llm-era.md) — 세 갈래로 조사한 원문 보고서.

---

## 0. 한 문단 요약

ElastiCORE 는 지금 **구조**(엔티티·DTO·관계·포트)를 선언하고 그로부터 계층을
생성한다. 행위는 "자리"까지만 생성한다 — `@factory` 는 던지는 스텁, `domainService`
는 빈 클래스, `OrderService` 는 generate-once 확장점. 이 문서는 그 자리에 들어갈
**업무 로직 자체를 선언**하는 DSL 을 설계한다. 방향은 하나다: 범용 액션 언어를
만들지 않는다. 검증 가능한 좁은 형식 다섯 가지 — **상태기계 · 결정표 · 계약(사전/
사후/불변) · 효과 · 사가** — 만 받고, 그 밖의 것은 타입 있는 탈출구(`custom`, `ai`)로
LLM 이나 사람이 채우되 **선언이 만든 계약 테스트가 채운 것을 검증**한다. 선언 하나가
코드·테스트·에이전트 설명서 세 산출물을 동시에 만든다. 이것이 AGI 시대에 사람이
남기는 것 — 의도·불변식·정책·허용 효과 — 을 담는 그릇이다.

---

## 1. 지금 DSL 이 말할 수 있는 것과 없는 것

| 로직의 종류 | 지금 | 비고 |
|---|---|---|
| 필드 검증 (길이·범위·필수) | O | Bean Validation 생성 |
| 검색 조건 | O | `@search(...)` → Specification |
| 애그리거트 경계·참조 | O | `@root` / `@aggregate` |
| 불변식 | **부분** | `@invariant(items.size > 0, ...)` — 필드·비교·리터럴만 |
| 이름 있는 규칙 | **부분** | `@specification(highValue, totalAmount > 100)` — 같은 제한 |
| 도메인 이벤트 | **부분** | 클래스·raise 메서드는 생성, **언제 발생하는지는 없음** |
| 팩토리·도메인 서비스 | 자리만 | generate-once 스텁 |
| 외부 호출 (SQL/HTTP/gRPC) | O | `port:` — 선언 → 런타임 프록시 |
| 상태 전이 (주문: 접수→결제→배송) | X | `status` 는 그냥 enum 필드 |
| 조건 분기 규칙 (배송비·할인·자격) | X | 서비스 코드에 `if` 로 |
| 유스케이스 (사전·사후조건, 순서) | X | `OrderUseCase` 는 CRUD 시그니처뿐 |
| 이벤트 → 반응 (정책) | X | 리스너를 손으로 |
| 여러 애그리거트에 걸친 프로세스·보상 | X | |
| 유스케이스가 건드리는 범위 (읽기/쓰기/호출) | X | 코드 리뷰로만 |
| LLM 이 수행하는 단계 | X | |

핵심 관찰 둘.

1. **조건 문법이 한계에 닿아 있다.** `@invariant` 의 조건이 `>=` 를 못 쓰는 이유는
   메타 애노테이션 파서가 인자 속 `=` 를 키/값으로 읽기 때문이고, `:` 를 못 쓰는
   이유는 meta 줄이 YAML 스칼라이기 때문이다 (rule-guide 226행). 로직을 더 넣으려면
   **애노테이션 인자가 아니라 YAML 블록**이어야 한다. 이 결정이 문법 전체를 정한다.
2. **생성기는 이미 "행위를 쓸 자리"를 정확히 안다.** 팩토리·도메인 서비스·확장
   서비스·유스케이스 인터페이스가 모두 있다. 새 DSL 은 새 계층을 만드는 것이 아니라
   **그 자리들을 채우는 선언**이다.

---

## 2. 조사에서 가져온 결정 근거

세 보고서에서 설계를 실제로 바꾼 것만 적는다.

| # | 근거 | 설계에 미친 영향 |
|---|---|---|
| A1 | MDE 실무 조사 (Whittle et al. 2014): 행위 DSL 은 합의가 없어 파편화됐고, 살아남은 것은 **좁고 실행 의미가 명확한 형식**(fUML, OCL, 상태차트) 뿐 | 범용 액션 언어를 만들지 않는다. 다섯 형식으로 제한 |
| A2 | DMN 결정표의 형식 의미론과 **완전성·중복 검사 알고리즘** (Calvanese et al., BPM 2016); 결정표가 트리·자유 규칙보다 이해도 높음 (DSS 2011) | `decision:` 블록을 DMN 결정표 모양으로. 빌드 시 완전성/중복 검사 |
| A3 | Sagas (Garcia-Molina & Salem 1987): 단계 + 보상의 단순한 실패 의미론 | `process:` 는 단계마다 `compensate` 를 선언. 새 실패 의미론 발명 금지 |
| A4 | Statecharts (Harel 1987), SCXML/XState: 사람과 기계가 같이 읽는 형식 | 엔티티 생명주기는 `state:` 블록. 프로세스 그래프와 **분리** |
| A5 | 효과 ≡ 세션 타입 (POPL 2016): 단순한 허용 목록이 행위 타입으로 충분 | `effects:` (reads/writes/emits/calls) 를 정적 강제 |
| I1 | CEL: 비튜링완전·선형시간·종료 보장, cel-java, k8s/Firebase 채택 | 표현식 언어는 **CEL 부분집합**. SpEL 은 기본 안전하지 않아 배제 |
| I2 | 조사한 모든 선언 플로우가 **타입 있는 코드 탈출구**를 갖는다 (BPMN service task, ASL Resource, Hasura Actions, Salesforce Apex) | `custom:` 단계 = generate-once 메서드 + 계약 테스트 |
| I3 | Temporal(코드 우선)과 JHipster JDL(로직 미포함)은 반대편 증거 | 모든 로직을 선언하려 들지 않는다. "잘 타입된 이음매"가 승부처 |
| I4 | Dark lang 종료: 언어 + 전용 런타임 동시 발명은 위험 | **런타임 인터프리터 없음**. 항상 관용적 Java/Kotlin/TS 로 트랜스파일 |
| I5 | Context Mapper CML: 전술 DDD 문법의 가장 가까운 선례 | `usecase` 어휘 참조 |
| L1 | Kinaxis 사례 (ICSE-SEIP 2026): 잘 설계된 DSL 은 LLM 생성 실행 가능률 **94.7% vs Python 79.8%** | DSL 이 LLM 의 인터페이스라는 전제가 수치로 지지됨 |
| L2 | Fowler/Joshi 2026: DSL 이 LLM 에 유효한 이유는 **결정적 검증기** 와 좁은 어휘 | 모든 블록에 로드 시점 검사 + 즉시 오류 메시지 (자기교정 루프) |
| L3 | Vericoding (POPL 2026): Dafny 82% / Verus 44% / Lean 27% — 형식이 단순할수록 LLM 성공 | 완전 형식 증명이 아니라 **계약 수준**(사전/사후/불변)까지만 |
| L4 | 맞춤 어휘 환각 (CallNavi): 사전학습에 없는 애노테이션은 환각률 상승 | DSL 의 **JSON Schema 발행** + 스킬 문서에 문법 발췌 |
| L5 | MCP/A2A/OpenAI 어느 표준도 **부수효과를 1급 필드로 갖지 않음** | `effects:` 가 ElastiCORE 가 채울 빈자리 |
| L6 | EU AI Act 12/18조, DORA 2026 "검증 세금" | 버전 있는 선언이 감사 산출물. 리뷰 대상은 코드가 아니라 선언 |

---

## 3. 설계 원칙

**P1. 선언은 계약이다.** 모든 선언은 세 산출물을 만든다: (a) 코드, (b) 그 코드를
검증하는 테스트, (c) 사람과 에이전트가 읽는 설명(주석·OpenAPI·Ossie·MCP). 하나만
만드는 선언은 넣지 않는다.

**P2. 좁고 검증 가능한 형식만.** 상태기계·결정표·계약·효과·사가. `if/for/while` 이
있는 스텝 언어는 만들지 않는다. 그 순간 "거의 맞는 파서"가 되고 검증이 불가능해진다.

**P3. 표현식은 종료가 보장되고 모델에 타입 검사된다.** CEL 부분집합. 필드 접근·
비교·논리·산술·`size()`·`exists()/all()`·문자열 함수·`in`. 호출·할당·루프 없음.
로드 시점에 모든 식별자를 엔티티/DTO 필드에 대해 해석하고, 못 하면 `DSL_WARN`.

**P4. 효과는 선언하고 강제한다.** 유스케이스는 `reads/writes/emits/calls` 를 적는다.
생성기는 선언되지 않은 리포지터리·포트를 주입하지 않으므로 컴파일이 강제한다.

**P5. LLM 은 선언할 수 없는 것만 채운다.** `custom` 스텝은 시그니처와 계약 테스트가
생성된 generate-once 메서드다. `ai` 스텝은 입출력 스키마로 계약된 런타임 LLM 호출이다.
둘 다 선언이 만든 사전/사후조건 안에서만 실행된다.

**P6. 침묵 약속.** 새 블록은 전부 새 최상위 YAML 키다. 로더는 자기 키만 읽으므로
(`YamlFileMainModelLoader` 119-133행) 안 쓰면 2.x 와 바이트 동일하다.

**P7. 빠른 검증 루프.** 모든 검사는 로드 시점(Phase 1)에 `DSL_WARN` 으로, 위치와
고칠 방법을 담아. `-Pmode=valid` 로 빌드 실패. 이것이 LLM 자기교정의 신호다.

**P8. 런타임 없음.** 상태기계도 결정표도 관용적 Java/Kotlin/TS 로 트랜스파일한다.
`DddCondition` 이 이미 그렇게 한다. 인터프리터를 넣는 순간 생성 코드가 읽히지 않고,
Dark lang 의 길이다.

**P9. 한 선언, 여러 읽기.** 번들이 정한다: `jpa-springboot` 는 서비스 메서드로,
`hexagonal` 은 유스케이스 구현으로, `ts-model` 은 브라우저가 정직하게 가질 수 있는
것(결정표·상태 전이 가능 여부)만으로, `ossie-export` 는 `ai_context`·metrics 로,
`agent-export` 는 MCP tool 설명서로.

---

## 4. 어휘

파일: `blueprint/{domain}/model/logic.yml` (분할 가능: `state.yml`, `decision.yml`,
`usecase.yml`, `policy.yml`). 여섯 최상위 키. 예시는 ddd 샘플의 `shop` 도메인.

### 4.1 `state:` — 엔티티 생명주기 (Statecharts)

```yaml
state:
  Order:
    field: status                 # OrderStatus enum 필드. 값 집합 = 상태 집합
    initial: DRAFT
    transitions:
      place:
        from: DRAFT
        to: PLACED
        guard: items.size() > 0 && customer.active
        emits: Placed             # @event 에 선언된 이름이어야 함
      pay:
        from: PLACED
        to: PAID
        guard: paymentMethod != null
      ship:
        from: PAID
        to: SHIPPED
        requires: trackingNumber != null   # guard 는 "가능한가", requires 는 "어겼으면 오류"
      cancel:
        from: [DRAFT, PLACED, PAID]
        to: CANCELLED
        emits: Cancelled
    final: [SHIPPED, CANCELLED]
```

생성물: 루트에 `place()/pay()/ship()/cancel()` 메서드 (전이 검사 + 상태 변경 +
`raisePlaced()` 호출), `canPlace()` 질의 메서드, `status` 세터 봉인. TS 에는
`canPlace(dto)` 만 간다. 테스트: 전이표에서 **허용 전이 × 금지 전이** 전수 생성.
검사: enum 값 ⊇ 상태 집합, 도달 불가 상태, 탈출 없는 비종료 상태, `emits` 가
`@event` 에 있는지.

이것이 `@event` 의 "언제 발생하는지는 생성하지 않는다"는 빈칸을 채운다.

### 4.2 `decision:` — 결정표 (DMN)

```yaml
decision:
  ShippingFee:
    desc: What delivery costs, by destination and customer standing
    inputs:
      country: string
      weight: float
      customerType: CustomerType
    output: bigdecimal
    hitPolicy: first              # first | unique | collect(sum|min|max)
    rules:                        # 행 하나 = 플로우 매핑 {when, then}. 한 줄에 두 키를 평문으로 쓰면 YAML 이 아니다
      - {when: 'customerType == VIP',              then: 0}
      - {when: 'country == "US" && weight < 5.0',  then: 5.00}
      - {when: 'country == "US"',                  then: 12.00}
      - {else: 20.00}
```

생성물: `ShippingFeeDecision.decide(country, weight, customerType)` 순수 함수
(Java/Kotlin/TS 모두), 표를 그대로 옮긴 파라미터화 테스트, DMN 1.5 XML 내보내기
(선택). 검사: `unique` 정책에서 **중복 규칙**, `else` 없는 표의 **완전성** —
Calvanese 의 초직사각형 알고리즘으로 입력 공간 커버리지 계산. 타입 검사: `then`
값이 `output` 타입인지.

로더 규칙 하나: `then` 은 **원문 스칼라**로 읽는다. YAML 파서에 맡기면 `5.00` 이
`5.0` 이 되어 `bigdecimal(12.2)` 의 스케일이 사라진다 (프로토타입 검증에서 확인).
`requires` 의 `- 식 : 코드` 는 단일 키 매핑으로 파싱되며, 식은 키에서 읽는다.

### 4.3 `usecase:` — 명령과 그 계약 (Design by Contract + Effects)

```yaml
usecase:
  PlaceOrder:
    desc: A customer turns a draft into an order the shop has accepted
    on: Order                     # 대상 루트. 생략하면 도메인 서비스급 연산
    input: PlaceOrderInput        # dto: 에 선언된 DTO, 또는 인라인 fields:
    output: OrderDTO
    effects:
      reads:  [Customer, Product]
      writes: [Order]
      calls:  [InventoryPort.reserve]
      emits:  [Placed]
    requires:                     # 사전조건. 각각 오류 코드를 가진다
      - customer.active                     : CUSTOMER_INACTIVE
      - input.items.size() > 0              : EMPTY_ORDER
      - input.items.all(i, i.quantity > 0)  : BAD_QUANTITY
    steps:                        # 닫힌 어휘. 순서대로. 분기·루프 없음
      - decide: order.shippingFee = ShippingFee(input.country, input.weight, customer.customerType)
      - custom: priceItems        # 시그니처 생성, 본문은 사람/LLM
      - call:   InventoryPort.reserve(input.items)
      - transition: order.place   # state: 의 전이. guard 실패 = 오류
    ensures:                      # 사후조건. 테스트와 런타임 assert 둘 다
      - result.status == PLACED
      - result.totalAmount >= 0
    examples:                     # 실행 가능한 예시 → 테스트
      - name: a vip pays no shipping
        given:
          Customer: {customerId: 1, active: true, customerType: VIP}
        when: {customerId: 1, country: "US", weight: 3.0, items: [{productId: p1, quantity: 2}]}
        then: {status: PLACED, shippingFee: 0}
```

생성물: `OrderUseCase.placeOrder(PlaceOrderInput)` 시그니처 (인바운드 포트에 추가),
`OrderCoreService.placeOrder` 본문 — requires 검사 → steps 순서 → ensures 검사 →
저장, 각 오류 코드는 `PlaceOrderException(code)`; `custom` 스텝은
`OrderService.priceItems(Order, PlaceOrderInput)` **추상 메서드** 로 generate-once
파일에 시그니처만; 컨트롤러 엔드포인트 `POST /orders/place`; OpenAPI 에 desc 와
오류 코드; 테스트 — requires 하나씩 어긴 입력이 해당 코드를 내는지, examples 각각,
ensures 를 어기는 `custom` 구현을 잡는 계약 테스트.

효과 강제: `OrderCoreService` 생성자에는 `effects` 에 적힌 리포지터리·포트만 주입된다.
`Product` 를 쓰려면 선언해야 하고, 선언하지 않은 것은 컴파일이 막는다.

### 4.4 `policy:` — 이벤트에 대한 반응

```yaml
policy:
  ReserveStockWhenPlaced:
    on: Order.Placed
    do: ReserveStock              # usecase 이름
    with: {orderId: event.orderId}
    async: true                   # false 면 같은 트랜잭션
```

생성물: `@TransactionalEventListener` (또는 `@EventListener`) 리스너 클래스.
검사: `on` 이 선언된 이벤트인지, `do` 의 input 이 `with` 로 채워지는지.

### 4.5 `process:` — 사가 (Garcia-Molina)

```yaml
process:
  Fulfilment:
    starts: Order.Placed
    steps:
      - do: ReserveStock     compensate: ReleaseStock
      - do: ChargePayment    compensate: RefundPayment
      - do: ArrangeShipping  compensate: CancelShipping
    ends: [Order.Shipped, Order.Cancelled]
    timeout: 24h -> CancelOrder
```

생성물: 단계 실행 + 실패 시 **역순 보상** 을 하는 프로세스 매니저 클래스, 진행 상태
엔티티(`FulfilmentProcess`) 자동 선언. 검사: 각 단계와 보상이 usecase 인지, 보상이
없는 단계는 마지막이어야 한다는 경고(보상 불가 단계 뒤의 실패는 되돌릴 수 없으므로).

### 4.6 `ai:` — LLM 이 수행하는 단계 (typed behavioural API)

```yaml
ai:
  ClassifyReturnReason:
    desc: Read a customer's free-text return request and name the reason
    input:  {text: string}
    output: ReturnReason          # enum → 제약 디코딩 / tool-use 스키마
    instructions: |
      Pick the single reason that best fits. Prefer OTHER over guessing.
    fallback: MANUAL_REVIEW       # 스키마 불일치·타임아웃·낮은 신뢰도
    effects: {}                   # 순수: 아무것도 읽거나 쓰지 않음
    model: default
```

생성물: `ClassifyReturnReasonAi` 포트 인터페이스 + 어댑터 (Anthropic tool-use 방식으로
출력 스키마 강제), usecase 의 `- ai: ClassifyReturnReason(input.text)` 스텝에서 호출.
**모든 ai 스텝은 fallback 이 필수**이고, 결과는 usecase 의 `ensures` 를 통과해야 한다.
검사: output 이 enum/DTO 인지, fallback 이 output 타입인지.

### 4.7 `context` — 의도 (Ossie ai_context, 로드맵 #2)

```yaml
entity:
  Order:
    meta: entity @root ...
    context:
      synonyms: [purchase, 주문]
      description: An accepted purchase; money has not necessarily moved yet
    fields:
      totalAmount: bigdecimal(14.2) @default(0)
      # 필드에도: context: {description: "..."}
```

생성물: 클래스/필드 Javadoc, OpenAPI description, Ossie `ai_context`, MCP tool
description. 새 블록이 아니라 기존 블록의 선택 키.

### 4.8 표현식 언어 (공통)

CEL 부분집합. 허용: `a.b`, `== != < <= > >=`, `&& || !`, `+ - * /`, 리터럴,
`in`, `size()`, `x.all(i, p)`, `x.exists(i, p)`, `startsWith/contains/matches`,
`null`. 금지: 함수 정의, 할당, 루프, 호출 (decision 참조는 `decide:` 스텝으로만).
식별자 해석 순서: `input` → `result`/`event` → 대상 루트의 필드 → `on` 이 없으면
오류. 기존 `@invariant` 문법은 그대로 두고, `invariant:` 블록 형태를 추가해 같은
언어를 쓴다 (`>=` 문제의 해법).

트랜스파일: Java(`order.getItems().size() > 0`), Kotlin(`order.items!!.size > 0`),
TS(`dto.items.length > 0`). `DddCondition` 을 일반화한 `ExprCompiler` 하나가 담당.
런타임 CEL 엔진은 쓰지 않는다 (P8).

---

## 5. 정적 검사 목록 (로드 시점, 전부 `DSL_WARN`)

| 검사 | 블록 | 왜 |
|---|---|---|
| 식별자가 모델 필드로 해석되는가, 타입이 맞는가 | 전부 | 타이핑 시점 오류 = LLM 자기교정 신호 |
| `emits` 가 `@event` 선언에 있는가 | state, usecase | 이벤트 이름의 단일 출처 |
| 상태 집합 ⊆ enum 값, 도달 불가·탈출 불가 상태 | state | 죽은 상태는 죽은 코드 |
| 결정표 완전성·중복 (Calvanese 2016) | decision | DSS 연구가 이해도, BPM 연구가 검증 가능성을 보장 |
| `effects` 폐쇄성: steps 가 쓰는 루트·포트·결정이 전부 선언됐는가 | usecase | 효과 = 행위 타입 |
| `requires` 마다 오류 코드, 코드 중복 없음 | usecase | API 계약 |
| `custom` 스텝 이름이 Java 식별자, 같은 루트 안에서 유일 | usecase | 시그니처 생성 |
| `process` 의 보상 없는 단계 위치 | process | 되돌릴 수 없는 실패 |
| `ai` 의 fallback 존재·타입 | ai | LLM 은 실패한다 |
| 침묵: 어느 블록도 없으면 경고 0, 생성물 변화 0 | 전부 | 2.x 호환 골든 비교 |

---

## 6. AGI 시대 대응 — 계층 그림

```
사람 ─── 의도 ─────────────────────────────────────────────┐
  │   "VIP 는 배송비를 내지 않는다"                             │ 자연어 · 대화
  ▼                                                          │
선언 (DSL, 사람 + LLM 공동 저작, 버전 관리, 리뷰 대상) ◄───────┘
  │   decision.ShippingFee / usecase.PlaceOrder.requires / effects
  │   ── JSON Schema 로 제약 디코딩 · 로드 시점 검사로 자기교정
  ▼
생성 (결정적)                    검증 (선언에서 파생)
  │   OrderCoreService.placeOrder    PlaceOrderContractTest
  │   ShippingFeeDecision            ShippingFeeTableTest
  │   OrderUseCase, 컨트롤러         전이표 테스트
  ▼
채움 (LLM 또는 사람, 선언할 수 없는 것만)
  │   OrderService.priceItems(...)  ← custom 스텝, generate-once
  │   ClassifyReturnReasonAi        ← ai 스텝, 런타임
  │   ── 채운 것은 계약 테스트를 통과해야 빌드가 끝난다
  ▼
실행 (런타임 가드)
      requires/ensures assert · 효과 제한(주입 목록) · 정책 엔진 훅(Cedar/OPA, 선택)
      감사: "어느 선언 v몇" 이 결정을 내렸는지 (EU AI Act 12/18조)
```

사람이 남기는 것은 넷이다: **무엇이 참이어야 하는가**(불변식·사전/사후조건),
**무엇이 일어나는가**(전이·정책·프로세스), **무엇을 건드려도 되는가**(효과),
**옳은 예**(examples). 그 넷이 있으면 채우는 쪽이 사람이든 모델이든 검증 방법이
같다. 그것이 이 설계가 "AGI 대응"이라고 말하는 정확한 의미다 — 모델이 더 좋아질수록
채움 계층은 얇아지고 선언 계층의 가치는 커진다.

### 에이전트 내보내기 (`agent-export` 룰셋)

usecase 하나 = MCP tool 하나. `name/desc/input schema/output schema` 는 이미
선언에 있고, `effects` 는 어느 표준에도 없는 것을 우리가 채운다:

```json
{ "name": "shop.placeOrder", "description": "...", "inputSchema": {...},
  "x-elasticore": { "effects": {"writes": ["Order"], "emits": ["Placed"]},
                    "requires": ["CUSTOMER_INACTIVE", "EMPTY_ORDER"] } }
```

`ossie-export` 와 같은 사이드카 방식, 같은 opt-in 문법.

---

## 7. 단계별 로드맵

각 단계는 **샘플이 첫 사용자**이고, 골든 비교 0 DIFF 와 변이 검증을 통과해야 닫힌다.

| 단계 | 내용 | 선행 | 검증 기준 |
|---|---|---|---|
| **L0** | `ExprCompiler`: CEL 부분집합 파서 + 타입 검사 + 3언어 트랜스파일. `invariant:` 블록 형태 추가. DSL JSON Schema 발행 | 없음 | 기존 `@invariant` 골든 불변. `>=` 가 블록 형태에서 동작 |
| **L1** | `state:` + `decision:` | L0 | shop 샘플 Order 상태기계, ShippingFee 결정표. 전이표·결정표 테스트 자동 생성. 완전성/중복 검사 변이 검증 |
| **L2** | `usecase:` (requires/ensures/effects/steps/custom/examples) | L1 | `PlaceOrder` 가 HTTP 로 동작. 효과 미선언 → 컴파일 실패 시연. 계약 테스트가 잘못된 custom 구현을 잡는 변이 검증 |
| **L3** | `policy:` + `process:` | L2 | Fulfilment 사가, 보상 역순 실행 테스트 |
| **L4** | `context` (ai_context) + `ai:` 스텝 | L2 | Ossie export 에 ai_context, ai 스텝 fallback 경로 테스트 |
| **L5** | `agent-export` (MCP tool 사이드카) | L2, L4 | 생성 매니페스트가 MCP 스키마 검증 통과 |

L0-L1 이 가장 값싸고 검증이 확실하다 (순수 함수, 표에서 테스트가 나옴). L2 가
중심이고 가장 어렵다 — `steps` 어휘를 **닫힌 채로 유지**하는 규율이 관건이다.

---

## 8. 결정 표

### 표현식 언어

| 후보 | Java 구현 | 안전·종료 | 타입 | LLM 친숙도 | 판정 |
|---|---|---|---|---|---|
| **CEL 부분집합** | 트랜스파일 (자체) | 비튜링완전, 종료 보장 | 모델에 정적 | 중 (k8s) | **채택** |
| FEEL | Drools/Camunda | 엔진 샌드박스 | DMN 타입 | 하 | 결정표 내보내기 대상으로만 |
| JSON Logic | 커뮤니티 | 구조적으로 안전 | 없음 | 하 | YAML 에서 읽기 나쁨 |
| Rego | 없음 (사이드카) | 부분 | 없음 | 하, 학습곡선 | 정책 엔진 훅으로만 |
| SpEL | 네이티브 | **기본 비안전** (리플렉션) | 동적 | 중 | 배제 |

### 플로우 표현

| 후보 | YAML | 보상 | 가독성 | 코드 생성 | 판정 |
|---|---|---|---|---|---|
| BPMN | 나쁨 | 강 | 도구 필요 | 무거움 | 내보내기 대상으로만 |
| ASL | 좋음 | 수동 | 소규모 좋음 | 높음 | 참고 |
| **Serverless Workflow 1.0** | 매우 좋음 | **내장** | 좋음 | 높음 | `process:` 의 try/compensate 모양 차용 |
| **Statechart** | 좋음 | 없음 | 상태 중심에 최적 | 높음 | `state:` 채택 |
| Context Mapper | 좋음 | 없음 | 아키텍처 | 낮음 | 어휘 참고 |

---

## 9. 위험과 미결

- **steps 어휘 확장 압력.** "분기 하나만" 요청이 반드시 온다. 답은 `decision:` 으로
  빼거나 `custom` 으로 내리는 것이다. 분기를 받는 순간 P2 가 무너진다.
- **표현식 파서 범위.** 부분집합을 늘리면 트랜스파일 대상 3개가 같이 늘어난다.
  L0 에서 부분집합을 문서로 고정하고 이후 확장은 히스토리에 이유와 함께.
- **examples 의 given 생성.** 픽스처를 어디까지 자동으로 만드는가 — 루트와 to-one
  참조까지만, 그 밖은 `custom` 픽스처 훅.
- **ai 스텝의 비결정성 테스트.** 단위 테스트는 어댑터를 스텁하고 fallback 경로를
  검증한다. 실제 모델 호출은 테스트에 넣지 않는다.
- **2.x 번들에서의 usecase.** `jpa-springboot` 는 인바운드 포트가 없으므로
  `OrderService` 에 메서드를 더하는 방식으로 읽는다. 같은 선언, 다른 읽기 (P9).
- **JSON Schema 와 YAML 의 이중 표면.** 사람은 YAML, 에이전트는 스키마 — 스키마는
  로더의 어휘에서 **생성**해야지 손으로 두 벌 유지하면 어긋난다.

---

## 10. 다음 행동

1. L0: `ExprCompiler` 를 `DddCondition` 을 일반화하여 구현. 기존 골든 불변 확인.
2. `docs/design/` 의 이 문서를 근거로 `elasticore-history.md` 에 로드맵 항목 기록.
3. L1 을 shop 샘플에 첫 사용자로 적용 — `Order` 상태기계부터.
