# 헥사고날 아키텍처 적용 검토

참고: kakaostyle devblog, "Domain-Driven Hexagonal Architecture by Example" (2025-03-21)

## 0. 결론 먼저

**헥사고날의 대부분은 ElastiCORE 에 이미 있다. 이름이 다를 뿐이다.**

특히 `port` 는 이름 그대로 헥사고날의 **outbound port** 이고, 우리는 어댑터까지
런타임에 제공한다 - 블로그가 손으로 쓰는 `PdpBannerApiAdapter` 를 우리는 쓰지
않는다. 이 부분은 오히려 우리가 더 나아가 있다.

진짜 없는 것은 세 가지이고, 그중 둘은 **새 아티팩트 종류**를 요구하므로
`RulesGenRunner` 확장점에 막혀 있다. 즉,

> **헥사고날은 우리가 미뤄둔 확장점의 첫 번째 진짜 수요다.**

이름만 바꾸는 리팩터링은 하지 않기를 권한다. 순수한 churn 이다.

## 1. 대응 관계

| 헥사고날 | ElastiCORE 현재 | 상태 |
|----------|-----------------|------|
| Inbound adapter (driving) | `control` - REST 컨트롤러 | O |
| **Inbound port (UseCase)** | 없음. 컨트롤러가 **구체 클래스** `OrderService` 에 의존 | **X** |
| Application service | `service`(CoreService) + `service_extend`(개발자 소유) | O |
| Domain model | `entity` + `@root`/`@aggregate` | O (DDD 룰셋) |
| **Outbound port (driven)** | **`port` - http / dbms / grpc** | **O (핵심)** |
| Outbound adapter | 런타임 프록시 (`HttpServiceProxyFactory`, `DbmsServiceProxyFactory`) | O, 코드 없음 |
| Repository as port | `repository` - 단, `extends JpaRepository` | 부분 |
| 레이어 패키지 분리 | env.yml `namespace` | O, 설정만으로 |

### 우리 port 가 이미 outbound port 인 이유

```yaml
port:
  OrderDbService:
    meta: dbms @datasource("order")
    methods:
      findOrders:
        params: { input: OrderSearchDTO }
        return: List<OrderDTO>
        query: |
          SELECT * FROM orders WHERE status = :status
```

생성물은 **인터페이스뿐**이다.

```java
@ExternalService(protocol="http", id="integration.MockApiPortService", url="...")
interface MockApiPortService {
    @HttpEndpoint(url="/echo/{message}", method="GET", paramNames="message")
    HttpTestResponse getEcho(String message);
}
```

구현체는 런타임 프록시가 만든다. 이것이 정확히 port/adapter 다:
**application 은 인터페이스만 알고, 기술은 밖에 있다.**

블로그는 `PdpBannerRedisAdapter`, `PdpBannerMySqlAdapter`, `PdpBannerApiAdapter`
를 손으로 쓴다. 우리는 HTTP 와 SQL 에 한해 그 코드를 아예 쓰지 않는다.

## 2. 진짜 없는 것

### (A) Inbound port - UseCase 인터페이스

지금:

```java
public class OrderController {
    private final OrderService orderService;   // <- 구체 클래스
```

헥사고날:

```java
public class OrderController {
    private final OrderUseCase orderUseCase;   // <- 인터페이스
```

**비용**: 새 아티팩트 1종 (`OrderUseCase`) + 컨트롤러/서비스 시그니처 변경.
`RulesGenRunner` 의 방출 목록이 고정이므로 **공유 코드 변경 필요**.

**가치에 대한 정직한 평가**: 손으로 짠 코드에서 UseCase 인터페이스의 가치는
컨트롤러가 서비스 구현을 몰라도 되게 하는 것이다. 그런데 우리는 **컨트롤러와
서비스를 같은 DSL 에서 함께 생성**하므로 둘은 항상 일치한다. 테스트 대역을
끼우기 쉬워지는 이득은 남지만, 손으로 짠 시스템만큼 크지 않다.

즉 **비용은 확장점, 이득은 중간**. 확장점을 만든 뒤에 하는 것이 맞다.

### (B) Repository 를 도메인이 소유하는 port 로

지금 `domain/repository/OrderRepository.java` 는 이렇게 시작한다.

```java
package io.elasticore.sample.ddd.shop.domain.repository;
import org.springframework.data.jpa.repository.JpaRepository;   // <- 도메인이 Spring Data 를 안다
```

순수형은 도메인이 `OrderRepository` 인터페이스를 정의하고 infrastructure 가
Spring Data 로 구현한다 (인터페이스 1 + 구현 1 = 아티팩트 2종).

**비용**: 새 아티팩트 + 매핑 계층 추가. **막힘: 확장점.**

**정직한 평가**: 실무에서 이 뒤집기의 이득은 "JPA 를 걷어낼 수 있다" 인데,
ElastiCORE 프로젝트가 JPA 를 걷어낼 일은 드물다. 우선순위 낮음.

### (C) 어댑터 합성 - 블로그가 우리보다 앞선 지점

블로그는 하나의 port 에 여러 어댑터를 두고 우선순위로 엮는다.

```
PdpBannerPrimaryAdapter
  -> Redis 조회 -> 없으면 외부 API -> 결과를 로컬 캐시에 저장
```

우리는 **port 하나 = 어댑터 하나**(프로토콜이 결정)라 이 구성을 표현할 수 없다.

**권고: 생성하지 말 것.** 폴백 순서, 캐시 무효화, 부분 실패 처리는 전부
*행위*다. DSL 은 데이터를 선언하지 행위를 선언하지 않는다. 생성해야 할 것은
행위가 아니라 **행위를 쓸 자리**이고, 그 자리는 이미 있다 - `service_extend`
는 generate-once 라 개발자 소유다. 여기에 손으로 쓰는 것이 옳다.

## 3. 지금 당장 되는 것 (공짜)

레이어 배치는 룰이 아니라 `namespace` 설정이다. 헥사고날 이름으로 바꾸는 데
코드 변경이 0 이다.

```yaml
namespace:
  entity:      com.acme.shop.domain.model
  enumeration: com.acme.shop.domain.model
  repository:  com.acme.shop.domain.repository
  service:     com.acme.shop.application
  dto:         com.acme.shop.application.dto
  port:        com.acme.shop.application.port.out    # <- 아래 참고
  control:     com.acme.shop.adapter.in.web
```

### 검토 중 발견: port 네임스페이스 위치가 틀렸다

DDD 샘플은 `port` 를 `infrastructure.port` 에 둔다. 그런데 우리 port 는
**인터페이스**이고 어댑터는 런타임 프록시라 생성 코드가 없다. 헥사고날에서
인터페이스는 application 쪽에, 어댑터가 infrastructure 에 있다.

즉 **생성되는 유일한 파일이 잘못된 층에 놓여 있다.** `application.port.out`
쪽이 맞다. 설정 한 줄이고, 지금 고칠 수 있다.

## 4. 권고

| 항목 | 권고 | 이유 |
|------|------|------|
| 이름만 헥사고날로 바꾸기 | **하지 말 것** | 이미 있는 것의 churn |
| port 네임스페이스 교정 | **지금** | 설정 한 줄, 명백히 틀림 |
| 헥사고날 namespace 프리셋 문서화 | **지금** | 공짜, 의도가 문서가 됨 |
| Inbound port (UseCase) | 확장점 이후 | 비용=확장점, 이득=중간 |
| Repository 뒤집기 | 보류 | 이득 낮음 |
| 어댑터 합성 | **생성하지 말 것** | 행위. `service_extend` 가 그 자리 |

## 5. 이 검토가 확인해 준 것

DDD 설계 문서 6장에서 "새 아티팩트가 필요한 것은 확장점 이후"라고 미뤄둔
항목들이 있었다 - domain event, domain service, repository 분리. 헥사고날의
inbound port 도 정확히 같은 벽에 부딪힌다.

수요가 하나였을 때는 확장점을 만들 근거가 약했다. 지금은 넷이다.
**확장점을 만들 시점이 되었다는 것이, 이 검토의 가장 실질적인 결론이다.**
