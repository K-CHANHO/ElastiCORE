### New Features (v3 entity - an abstract root)

#### `@abstract(entity)`: an abstract `@Entity`, the root of a hierarchy read through one repository and one service
- Why: the v3 output writes every `@abstract` as `@MappedSuperclass`, so a hierarchy whose root has a key and is never
  stored as itself, read together through one repository, had no form: a repository for it could not start ("Not a
  managed type"), and an `@Inheritance` it kept did nothing. The 2.x generator writes such a root as an abstract
  `@Entity`, but both mappers wrote `new X()`, so with a service it did not compile. Reading every keyed `@abstract` as
  an entity would silently turn v3's keyed mapped superclasses into a hierarchy, so it is opt-in (#74).
- What: `meta: entity @abstract(entity)` gets `@Entity` and `@Table` on the abstract class, keeps the model's
  `@jpa:Inheritance`, and has a repository, a Q class and a service that reads, searches, updates and deletes. Java and
  Kotlin. Plain `@abstract` stays as it is.
- It has no create: no `save`, no POST (405), no `toEntity`, no `create` in the TypeScript client, no create use case
  under `ddd` and `hexagonal`, and the generated Kotlin service test makes its fixture with a mock and has no save
  test. A nested DTO of it is not written back. A relation to it is written with `em.getReference`, where another
  entity's would make a stub.
- The project DDL under `TABLE_PER_CLASS` (`JpaSchemaReader`): an abstract class has no table, as Hibernate makes none,
  and a relation to it has no foreign key, as its rows are in several tables. With a strategy that gives the root a
  table, the table and the foreign key are written.
- Refused: a root without a key, and `mode: mybatis` ("does not write an abstract entity"). The 2.x generator and
  `output = 'v2'` read it as plain `@abstract`, as they did; the compatible output is unchanged.
- Limits, said in the guide: a subclass, whose key is inherited, gets no repository or service of its own, as under a
  keyed `@abstract`; its rows go through a repository of the project's. The Ossie export has no dataset for the root, as
  for plain `@abstract`, which fits `TABLE_PER_CLASS`; a relation to the root is not exported either.
- Checked: `AbstractEntityTest` - the entity, the repository and Q class, the service, the controller, the mapper, the
  hexagonal use case, the Kotlin service test, the TypeScript client, the schema under `TABLE_PER_CLASS` and `JOINED`,
  plain `@abstract` as it was, the two refusals and the compatible output. A `TABLE_PER_CLASS` sample,
  `Code` / `StatusCode` / `RegionCode`, is generated, compiled and run in three modules: `elasticore-sample-petstore`
  (hexagonal; a polymorphic `findAll` and the API on H2, and in `SchemaValidateOnDatabase` on PostgreSQL, Oracle and SQL
  Server), `elasticore-template-ddd`'s `catalog` (the plain v3 bundle) and `elasticore-template-kt` (Kotlin, with its
  generated service test). `elcoreRulesCheck` and `elcoreTreeCheck` are unchanged.
- The pet store's regenerated schemas also take `credit_card_type VARCHAR(4)`, which the committed files missed since
  the enum column length fix. Its build sets `ossieName = 'petstore'`, so its semantic model keeps its name beside a
  second domain.
- Files: `EntitySpec.java`, `SpecFactory.java`, `JpaEntityAnnotationRules.java`, `QBaseRules.java`, `KotlinQRules.java`,
  `ControllerBaseRules.java`, `MapperAssembleRules.java`, `KotlinMapperAssembleRules.java`, `MapperBaseRules.java`,
  `ServiceTestBaseRules.java`, `JpaServiceFileRules.java`, `MyBatisSql.java`, `RulesGenRunner.java`,
  `JpaSchemaReader.java`, `kt/service.tmpl`, `kt/service_test.tmpl`, `AbstractEntityTest.java`, the three samples and
  their regenerated schemas, `elasticore-annotation-guide.md` `@abstract`, `elasticore-dsl-guide.md` and
  `elasticore-migration-guide.md` (en, ko)
