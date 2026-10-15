# DDD 룰셋 설계

## 2. DDD 패턴 인벤토리

Evans/Vernon 의 tactical pattern 을 하나씩 대조한다.

| 패턴 | DSL이 지금 말하나 | 룰로 생성 가능 | 판정 |
|------|------------------|---------------|------|
| Entity | `@id` | O (구현됨) | **완료** |
| Value Object | `@embeddable` | 부분 | equals/hashCode 는 됨, 불변성은 미흡 |
| Aggregate Root | `@root` / `@aggregate(X)` / `@cascade` 추론 | O (구현됨) | **인과관계 역전** (4장) |
| Aggregate 멤버 | `@cascade` 로 추론 | O (declines) | **완료** |
| Repository (root당 1개) | 경계에서 도출 | O (구현됨) | **완료** |
| Repository 인터페이스/구현 분리 | X | 새 아티팩트 필요 | runner 변경 |
| **Reference by identity** | X (추론 가능) | SpecFactory 데이터 필요 | **Tier A+** |
| **Collection 캡슐화** | `@cascade` + List 로 도출 | O (`addMethod`) | **Tier A** |
| Application Service | 현 `service` 가 이것 | O (패키지만 이동) | **Tier A (설정)** |
| Domain Service | X | 새 아티팩트 필요 | Tier C |
| Layering (Module) | env.yml namespace | 룰 불필요 | **Tier A (설정, 무료)** |
| Invariant | X | 새 어휘 + `addMethod` | **Tier B** |
| Factory | X | 새 어휘 + `addMethod` | **Tier B** |
| Domain Event | X | 새 어휘 + **새 아티팩트** | Tier C |
| Specification (도메인 규칙) | X | 새 어휘 | Tier C |

### Tier 정의

- **Tier A** — 새 어휘 없이, 기존 룰 메커니즘만으로 가능. 지금 만든다.
- **Tier B** — 새 애노테이션 어휘 필요. 어휘 설계 후 가능. 기계적 생성.
- **Tier C** — `RulesGenRunner` 확장 필요. 구조 변경이 선행되어야 함.

## 3. Vernon 의 aggregate 설계 4원칙 대조

| 원칙 | 현재 상태 |
|------|-----------|
| 1. 진짜 invariant 를 경계로 삼아라 | `@cascade` 로 대신하고 있음 - 잘못된 근거 (4장) |
| 2. 작은 aggregate 를 설계하라 | 강제 수단 없음. 경고는 가능 |
| 3. **다른 aggregate 는 identity 로 참조하라** | **위반 중.** 지금은 객체 참조가 그대로 남음 |
| 4. 경계 밖은 eventual consistency | domain event 없이는 불가 (Tier C) |

3번이 가장 시급하다. 현재 생성물에서 `Order.customer: Customer` 는 다른
aggregate root 를 객체로 붙들고 있고, 이는 한 트랜잭션에서 두 aggregate 를
수정할 수 있게 만든다. 나머지 항목은 "부족"이지만 이것은 "틀림"이다.

## 4. 경계 인과관계 교정

현재 `SpecFactory.aggregateHolderOf()` 는 이렇게 동작한다.

```
어떤 엔티티가 나를 @cascade 로 참조하는가?  ->  그 엔티티가 나의 root
```

인과가 뒤집혀 있다. `@cascade` 는 ORM 의 영속성 전파 설정이고, aggregate 경계는
도메인의 불변식이 결정한다. 지금은 ORM 설정이 도메인 경계를 결정한다.

**교정 방향**: `@root` / `@aggregate(X)` 를 1급으로 만들고, `@cascade` 는 그로부터
따라오게 한다.

```
@aggregate(Order)  선언  ->  경계 확정  ->  cascade 는 결과로 생성
```

`@cascade` 추론은 하위 호환을 위해 남기되, 명시 선언이 없을 때만 쓰는 fallback 으로
강등한다. `EntitySpec.containedIn()` 은 이미 이 우선순위를 갖고 있다
(`@root` > `@aggregate` > cascade 추론). 즉 **읽는 쪽은 이미 맞다.** 고쳐야 할 것은
쓰는 쪽 — cascade 가 없어도 `@aggregate(X)` 만으로 경계가 서고, 그 선언에서
cascade 애노테이션이 생성되어야 한다.

## 5. 새 DSL 어휘 (Tier B)

기존 애노테이션과 충돌하지 않는 이름만 고른다.

### `@invariant`

aggregate root 가 항상 참으로 유지해야 하는 조건.

```yaml
Order:
  meta: [ "@root", "@invariant(items.size > 0, 'an order has at least one line')" ]
```

생성물: root 에 `private void checkInvariants()` 와, 상태를 바꾸는 메서드 끝에서의
호출. 조건식은 필드 이름 + 비교 연산자로 제한한다 (임의 표현식 파서를 만들지
않는다).

### `@factory`

복잡한 생성 로직의 자리.

```yaml
Order:
  meta: [ "@root", "@factory(place)" ]
```

생성물: root 에 `static Order place(...)` 스텁 + "generate once" 마커. 본문은
개발자가 쓴다. 이것이 정직한 범위다 — DSL 은 데이터를 선언하고 행위는 선언하지
않으므로, 생성할 수 있는 것은 행위가 아니라 행위를 쓸 자리다.

### `@emits`

도메인 이벤트. **Tier C** — 새 아티팩트가 필요해 runner 변경 없이는 불가.

## 6. 아티팩트 확장: 이 설계의 핵심 결정

`RulesGenRunner` 의 방출 대상이 고정 리스트라는 것은, DDD 룰셋이 새 산출물을
내려면 **공유 runner 를 고쳐야 한다**는 뜻이다. 그런데 사용자의 요구는 "기존 rule 과
섞이지 않게 package 로 구분"이다. 자기 산출물을 추가하려고 공유 코드를 고쳐야 하는
룰셋은 **분리된 것이 아니다.**

두 가지 선택지가 있다.

**(a) runner 의 고정 리스트에 DDD 아티팩트를 추가한다.**
간단하지만 공유 runner 가 DDD 를 알게 된다. 이후 어떤 룰셋이 새 산출물을 원해도
같은 파일을 고쳐야 한다. 이전에 발견된 `sectionEngine(String... ruleSets)` 고정
리스트 문제와 정확히 같은 부채를 하나 더 만든다.

**(b) 룰셋이 추가 아티팩트를 선언할 수 있는 확장점을 만든다.**
`CodeRules` 가 자신이 만드는 아티팩트를 알릴 수 있게 하고, runner 는 고정 리스트
대신 그것을 순회한다. 작업량은 크지만 이것이 "섞이지 않는다"의 진짜 의미다.

**권고: (b).** 단, 이번 범위에서는 하지 않는다. (b) 는 rules pipeline 의 구조
변경이고, DDD 룰셋과 함께 하면 두 가지가 동시에 검증 불가 상태가 된다. 지금은
**새 아티팩트가 필요 없는 것만 만들고**, Tier C 는 (b) 가 된 뒤로 미룬다.

이 결정의 결과로 이번 범위에서 domain event, domain service, repository
인터페이스/구현 분리는 **제외**된다. 이름으로 약속하지 않는다.

## 7. 패키지 분리

현재 DDD 룰은 `pub/rules/base/` 에 있어 JPA 기본 룰과 섞여 있다.

```
pub/rules/
  base/          <- 기존 JPA/Spring 룰 (26개)
  lang/java|kotlin|typescript
  ddd/           <- 신설
    DddAggregateRules.java        (base/ 에서 이동)
    DddValueObjectRules.java      (base/ 에서 이동)
    DddReferenceRules.java        (신규 - Tier A)
    DddCollectionRules.java       (신규 - Tier A)
    lang/
      DddKotlinValueObjectRules.java  (lang/kotlin/ 에서 이동)
```

`Syntax` 원칙은 유지한다: 문법 차이는 `Syntax` seam 으로, 역할 차이는 별도 룰로.
value object 의 Java/Kotlin 분리는 후자다 (Lombok 은 Kotlin 에 아무것도 하지
않으므로 같은 룰이 될 수 없다).

## 8. 샘플 모듈

`elasticore-template-kt` 를 본떠 `elasticore-template-ddd` 를 신설한다.

- `settings.gradle` 에 include 1줄 추가
- `build.gradle` 은 kt 모듈(170줄)을 기준으로 작성
- blueprint 는 DDD 를 보여주기 위한 도메인 하나 — Order/OrderItem/Customer 처럼
  aggregate 경계와 cross-aggregate 참조가 둘 다 나타나는 모델
- env.yml 의 namespace 로 layering 을 시연한다:

```yaml
namespace:
  entity:     com.example.order.domain.model
  repository: com.example.order.domain.repository
  service:    com.example.order.application
  control:    com.example.order.interfaces.rest
  dto:        com.example.order.application.dto
```

layering 은 룰이 아니라 설정이므로 샘플이 곧 문서가 된다.

## 9. 이번 범위 (확정)

**만든다 (Tier A):**
1. `pub/rules/ddd/` 패키지 분리
2. reference-by-identity — cross-aggregate 객체 참조를 ID 참조로
3. collection 캡슐화 — root 의 자식 컬렉션에 add/remove, setter 제거
4. layering 시연 — 샘플 env.yml namespace
5. `elasticore-template-ddd` 샘플 모듈

**만들지 않는다 (이유 명시):**
- domain event / domain service / repository 분리 — `RulesGenRunner` 확장점(6장 b)
  선행 필요
- invariant / factory — 어휘 설계는 5장에서 마쳤으나, 구현은 Tier A 검증 후

**전제 조건:**
- 2번은 `SpecFactory` 에 "다른 엔티티가 root 인가 / 그 PK 타입은 무엇인가" 를
  넣어야 한다. 공유 코드 변경이며, 기존 룰셋 동작에는 영향이 없어야 한다.
- 기존 Java golden (`elasticore-template`) 은 `rules:` 설정이 없어 DDD 룰이 붙지
  않는다. `elcoreRulesCheck -Pstrict` 142 MATCH / 0 DIFF 는 유지되어야 한다.
