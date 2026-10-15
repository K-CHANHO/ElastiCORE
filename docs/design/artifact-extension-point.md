# 아티팩트 확장점 설계

## 1. 문제

`RulesGenRunner` 는 방출할 파일의 종류를 고정 목록으로 갖는다: entity, dto,
srchdto, repository, service, service_extend, control, service_test, identity,
api, 그리고 도메인 단위의 Q/mapper/helper 등. 룰은 이 목록의 target 에만 붙을
수 있다 (`@Rule(target="entity")`).

**새 종류의 파일을 원하는 룰셋은 runner 를 고쳐야 한다.** 자기 산출물을
추가하려고 공유 코드를 고치는 룰셋은 분리된 것이 아니다 - 이것이 DDD 설계
문서 6장이 확장점을 선행 조건으로 걸고 미룬 이유다.

### 현재까지 확인된 수요 (4건)

| 수요 | 필요한 새 아티팩트 |
|------|--------------------|
| 헥사고날 inbound port | `OrderUseCase` 인터페이스 |
| DDD domain event | `OrderPlaced` 이벤트 클래스 |
| DDD domain service | 도메인 서비스 스텁 |
| repository 인터페이스/구현 분리 | 도메인 소유 인터페이스 + infra 구현 |

## 2. 목표와 비목표

**목표**
1. 룰셋이 **자기 산출물을 스스로 선언**한다. runner 는 목록을 모른다.
2. 아무도 선언하지 않으면 출력이 **바이트 단위로 동일**하다 (골든 142 MATCH 유지).
3. 선언된 아티팩트는 기존 target 과 같은 대우를 받는다: `@Rule(target=...)` 로
   룰이 붙고, `dontMake()` 로 거절되고, generate-once 마커가 동작하고,
   namespace 로 배치된다.

**비목표**
- 2.x 프로덕션 퍼블리셔 경로 변경 (rules pipeline 은 verify/gen runner 로만 돈다)
- 도메인 단위 아티팩트(mapper 류)의 확장 - 수요가 생기면 같은 패턴으로 추가
- 어댑터 합성 등 행위 생성 - DSL 은 행위를 선언하지 않는다

## 3. 설계

### 3.1 선언 인터페이스

```java
package io.elasticore.base.model.pub.rules;

/**
 * A rule set that brings its own artifacts.
 *
 * The runner's built-in list says what every project gets; this says what a
 * rule set adds. The runner walks the configured sets, asks each one, and
 * emits what it answers - so a new kind of file lives entirely inside the
 * rule set that wants it, and a project that configures nothing gets
 * nothing new.
 */
public interface ArtifactContributor {

    /** The artifacts this set adds for one entity - possibly none. */
    java.util.List<ContributedArtifact> artifactsFor(EntitySpec spec);
}
```

```java
public final class ContributedArtifact {
    private final String target;        // rule target key, e.g. "usecase"
    private final String namespaceKey;  // env.yml namespace it is placed by, e.g. "service"
    private final String className;     // e.g. "OrderUseCase"
    private final boolean testTree;     // src/test 에 놓이는가

    public static ContributedArtifact of(String target, String namespaceKey, String className);
    public ContributedArtifact inTestTree();
}
```

조건("root 만", "exposed 만")은 **필드가 아니라 코드**다. `artifactsFor` 가
`EntitySpec` 을 받으므로 룰셋이 `spec.aggregateRoot()`, `spec.hasMeta("expose")`
로 직접 판단해서 빈 목록을 돌려주면 된다. 조건 미니 언어를 만들지 않는다.

### 3.2 Runner 변경 (1곳)

`generateEntityArtifacts` 끝에 한 루프:

```java
// What the configured rule sets bring of their own. The built-in list
// above is what every project gets; this is what a selection adds.
for (CodeRules rs : configuredRuleSetInstances()) {
    if (!(rs instanceof ArtifactContributor)) continue;
    for (ContributedArtifact a : ((ArtifactContributor) rs).artifactsFor(spec)) {
        emit(spec, a.target(),
             a.testTree()
                 ? testTarget(model.getNamespace(a.namespaceKey()), a.className())
                 : target(model.getNamespace(a.namespaceKey()), a.className()));
    }
}
```

- `configuredRuleSetInstances()` 는 `configuredRules()` 이름 목록을
  `RuleCatalog.standard().rulesOf(name)` 으로 인스턴스화한다. 이미 mapper
  section engine 이 같은 일을 한다 (`configuredSectionRules()`).
- `emit` 에 추가 룰셋 인자를 **넘기지 않는다**. 기여한 룰셋은 정의상 이미
  configured 선택에 들어 있으므로 `engine()` 이 포함한다.
- 기여 아티팩트의 렌더링은 기존과 동일: 룰셋 안의
  `@Rule(target="usecase", ...)` 메서드들이 본문을 쓴다. 템플릿이 필요하면
  룰이 `renderTemplateResource(...)` 로 자기 것을 가리킨다 - 새 target 마다
  전역 tmpl 을 요구하지 않는다.

### 3.3 불변 조건

| 조건 | 어떻게 보장되나 |
|------|-----------------|
| 아무도 기여 안 하면 출력 불변 | 루프가 0회 돈다. 구조적 보장 |
| 골든(elasticore-template) 무결 | 골든은 `rules:` 설정이 없어 configured 셋이 기본 번들뿐이고, 기본 번들의 어떤 룰셋도 Contributor 가 아니다 |
| 기여 아티팩트도 거절 가능 | `emit` 이 `dontMake()` 를 이미 본다 |
| 사용자 소유 파일 보존 | `write` 의 UNTRACKED 검사가 target 종류를 모른다. 그대로 동작 |
| 언어 분리 | `target()` 이 kotlin/ts 를 이미 안다. Kotlin 룰셋이 자기 Contributor 를 가지면 된다 |

### 3.4 첫 소비자: 헥사고날 UseCase

설계 검증은 소비자 하나로 한다. 후보 넷 중 UseCase 가 가장 작다
(인터페이스 1개, 매핑 계층 없음, 이벤트 발행 배선 없음).

```java
public class HexUseCaseRules implements CodeRules, ArtifactContributor {

    @Override
    public List<ContributedArtifact> artifactsFor(EntitySpec e) {
        if (!e.hasMeta("expose") || e.pkField() == null) return emptyList();
        return singletonList(
            ContributedArtifact.of("usecase", "service", e.getName() + "UseCase"));
    }

    @Rule(value = "hex-usecase", target = "usecase", order = 10)
    void shell(EntitySpec e) { /* 인터페이스 본문 */ }
}
```

번들: `c.bundle("hexagonal-springboot", "ddd-springboot", "hex-usecase")`.
컨트롤러가 UseCase 를 주입받게 하는 것은 기존 `control` target 의 룰로 같은
셋 안에서 해결한다 - 이것이 "관련 변경은 한 룰셋 안에" 원칙이다.

## 4. 검토한 대안

**(a) `@Artifact` 애노테이션 (선언적)** - 조건("root 만 원한다")이 애노테이션
문자열 미니 언어가 된다. `EntitySpec` 이 이미 갖고 있는 표현력을 버리는 셈.
기각.

**(b) 룰 실행 중 `ctx(e).alsoEmit(...)`** - 아티팩트 존재가 다른 룰의 실행
부수효과가 된다. 존재 결정과 내용 결정이 섞이고, 실행 순서 의존이 생긴다.
기각.

**(c) env.yml 에 아티팩트 목록 선언** - 사용자가 룰셋 내부 구조를 알아야
한다. 아티팩트는 룰셋의 구현 세부이지 프로젝트 설정이 아니다. 기각.

선택한 (interface + spec 질의) 방식은 mapper 확장 때와 같은 형태다:
중립적 seam (`refReadOnly`) 하나, 물어보는 쪽 하나, 선언하는 룰 하나.

## 5. 확장점이 열어주는 것 (이후 작업)

| 작업 | 확장점 위에서의 크기 |
|------|---------------------|
| 헥사고날 UseCase | 소 - 첫 소비자로 함께 구현 |
| DDD domain event | 중 - 어휘(@emits) 설계 선행 |
| domain service | 소 |
| repository 분리 | 중 - 기존 repository target 과의 관계 정리 필요 |

## 6. 검증 계획

1. 인터페이스 + runner 루프만 넣고 (소비자 0) 전체 검증: 골든 142 MATCH,
   base 478, kt 98, ddd 15 전부 그대로여야 한다.
2. `HexUseCaseRules` 구현, hexagonal 번들 신설, DDD 샘플에 UseCase 검증 추가.
3. 불변식 검사에 추가: 기여 아티팩트도 경계를 존중하는가
   (멤버 엔티티에 UseCase 가 생기지 않는가).
