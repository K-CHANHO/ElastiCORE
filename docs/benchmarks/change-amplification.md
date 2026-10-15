<p align="right"><b>English</b> · <a href="change-amplification.ko.md">한국어</a></p>

# Change amplification: what one line of DSL moves

When a business rule changes, how much code changes with it - and how much of that does a person have to read?
This benchmark makes the same business changes to the pet store sample's DSL and counts what the generated
application changes by. It also checks that generating twice from the same DSL gives the same code.

## Results

Measured on `elasticore-sample-petstore` as its build generates it: hexagonal, Java, Spring Boot 4, the Apache Ossie
export and the TypeScript client.

**The model**: 151 non-blank DSL lines generate 130 files with 9,341 non-blank lines.

**Determinism**: generated twice from the same DSL, **0 of 130 files differ** (the first line of a generated file -
its content hash and generation time - excepted).

| Business change | DSL lines | Generated files | Generated lines | Java | TypeScript | Other | Per DSL line |
|---|---:|---:|---:|---:|---:|---:|---:|
| Add a searchable field (Customer.nickname) | 1 | 9 | 39 | 26 | 6 | 7 | 39 |
| Add a lifecycle step (deliver: SHIPPED -> DELIVERED) | 2 | 12 | 69 | 47 | 14 | 8 | 35 |
| Change who may do something (cancel: owner -> admin) | 2 | 2 | 4 | 2 | 0 | 2 | 2 |
| Add a business rule (a price ceiling) | 2 | 5 | 12 | 5 | 6 | 1 | 6 |
| Order a search by default (Category @sort) | 2 | 1 | 3 | 3 | 0 | 0 | 2 |

"Lines" are lines removed plus lines added, as a diff shows them - so changing one line counts 2 and adding one
counts 1. "Other" is the semantic model, the message bundle and the schema.

## What it says

- **A field is not one line of code.** Adding a searchable field touches the entity, the DTO, the search DTO, the
  mapper, the query metadata (`Q`), the TypeScript DTO and search DTO, the schema and the semantic model - 9 files.
  Written by hand, each of those is a place to forget; here the review is the one DSL line.
- **A lifecycle step is a dozen files.** The enum (Java and TypeScript), the command and its check in the aggregate,
  the use case port, the core service, the REST endpoint, the client call and `canDeliver()` for the screen, the
  refusal's message code, the schema and the semantic model.
- **Rules that change a decision are small and contained.** Who may cancel, a price ceiling, a default order: the
  generated diff is a handful of lines, in exactly the files that enforce the rule.
- **Regenerating is safe.** Same DSL, same code: a regeneration either changes nothing or changes exactly what the
  model changed, so the generated diff can be trusted without being read line by line.

## Files each change touched

The number is the changed lines in that file.

| Business change | Files |
|---|---|
| Add a searchable field | `Customer.java` (4), `CustomerDTO.java` (4), `CustomerSrchDTO.java` (3), `PetstoreMapper.java` (10), `Q.java` (5), `CustomerDTO.ts` (4), `CustomerSrchDTO.ts` (2), `schema.yml` (1), `semantic_model.yml` (6) |
| Add a lifecycle step | `PurchaseOrderBase.java` (16), `PurchaseOrderCoreService.java` (16), `PurchaseOrderController.java` (6), `PurchaseOrderUseCase.java` (2), `PurchaseOrderDTO.java` (2), `PurchaseOrderSrchDTO.java` (2), `OrderStatus.java` (3), `PurchaseOrderApi.ts` (10), `OrderStatus.ts` (4), `petstore-messages.properties` (1), `schema.yml` (3), `semantic_model.yml` (4) |
| Change who may do something | `PurchaseOrderCoreService.java` (2), `semantic_model.yml` (2) |
| Add a business rule | `ItemInvariants.java` (3), `ItemDTO.java` (2), `ItemInvariants.ts` (3), `ItemDTO.ts` (3), `petstore-messages.properties` (1) |
| Order a search by default | `CategorySrchDTO.java` (3) |

## What it does not say

- It does not measure an AI's code against ElastiCORE's. It measures the **review surface**: with ElastiCORE the
  change a person reviews is the DSL diff; without it, someone writes - and someone reviews - the generated column.
- Generated lines are not all lines a person would have written: a hand-written application would be shaped
  differently. The column is the amount of consistent change the rule implies across the layers, not a productivity
  multiplier.
- One sample, five changes. The scenarios and the counting are in the test below; add your own.
- Determinism is checked by generating twice in one run of the generator's JVM; a run on another machine is the
  compatibility check's job (`elcoreRulesCheck`, which compares with committed output).

## Reproduce

```bash
ELASTICORE_BENCH=1 ./gradlew :elasticore-project:elasticore-base:test --tests "*ChangeAmplificationBenchmark"
cat elasticore-project/elasticore-base/build/bench/change-amplification.md
```

The benchmark is
[`ChangeAmplificationBenchmark`](../../elasticore-project/elasticore-base/src/test/java/io/elasticore/base/model/pub/rules/verify/ChangeAmplificationBenchmark.java):
each scenario is applied to a fresh copy of the DSL, generated into a fresh directory with the same settings the
sample's build uses, and diffed against the unchanged model generated the same way. It does not run in the normal
build.
