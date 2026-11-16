### New Features (sign-in - `@signin(refresh=request)`)

#### `@signin(refresh=request)`: a signed-in session follows its row
- Why (issue 181, item 1): the generated sign-in puts the row's key and role in the session, and the session kept
  them until the next sign-in. With a session kept for days, a role the administrator gave or took away waited for
  that sign-in, and a member whose row was deleted kept a working session with their old role. Applications
  re-read the row in a filter of their own.
- What: `@signin(refresh=request)` on the entity that signs in. The entity also gets `XxxSessionRefresh`, a
  `@Component` filter beside its controller, regenerated with the model. Before each request whose session holds a
  signed-in key, it reads the row again through the service's `me()`, the read `GET /me` makes, so a row kept under
  `@deleted` counts as gone:
  - no row: the session ends (`SessionCurrentUser.signOut`), and the request goes on as a visitor's - what needs a
    signed-in user answers 401;
  - a row whose role differs: the session takes it from that request on and keeps its id
    (`SessionCurrentUser.refresh`, new). The attribute is written only when the roles differ.
  - A session with no signed-in key (a visitor, or an application with a `CurrentUser` of its own) is not read.
- A file of its own rather than code in the controller: the controller is the developer's once written, so a
  project whose controller is already its own still gets it. A hexagonal bundle injects the use case, as for the
  controller (`hex-usecase-refresh`); the role is read as the sign-in reads it. Removing the option removes the file.
- Runtime: `io.elasticore.springboot3.security.SessionRefreshFilter` (a `OncePerRequestFilter` the generated class
  extends) and `SessionCurrentUser.refresh(request, roles)`. The filter lends the request context to the read when
  nothing set it before (a MockMvc built by hand), and gives it back.
- Cost: one read of the row per request with a signed-in session. The MyBatis pet store's statement count for a
  page of orders says so: the customer, then the page's three statements.
- `@signin` takes no other option: another parameter, another value of `refresh` or `@signin(request)` is a DSL
  warning, and nothing is generated for it.
- Works in: v3 and Kotlin, JPA and MyBatis, layered and hexagonal. The 2.x-compatible output has no generated
  sign-in, so no filter (new rule set `spring-signin-refresh`, in every v3 bundle).
- Unchanged: a model without `refresh=request` generates the same.
- Samples: the pet store, the MyBatis pet store (`Customer`) and `elasticore-template-kt` (`Agent`, a string role)
  say `@signin(refresh=request)`.
- Checked:
  - `SessionRefreshFilterTest` (runtime, springboot3 and springboot4): a raised role is the session's and the
    session id stays; a role taken away; the same role writes nothing; a row that is gone ends the session and the
    rest of the request sees a visitor; a session nobody signed in to is not read; the request context lent for the
    read alone.
  - `SignInRefreshTest` (generator): hexagonal Java (the use case), Kotlin (keywords quoted in the package and the
    import), layered Java and Kotlin (the service), MyBatis; no filter without the option, without a sign-in or in
    the 2.x-compatible output; an entity without a role field; the DSL warnings; `me()` reading past `@deleted`.
  - HTTP, failing before the change: `ShopFlowTest.theSessionFollowsTheCustomersRow` (pet store, JPA) and the same
    in the MyBatis pet store's `ShopFlow` (H2 here; PostgreSQL, Oracle and SQL Server in CI): a customer made an
    administrator searches every customer in the same session, made a user again sees only themselves, deleted
    gets 401; `ShopApiContractTest.theSessionFollowsTheAgentsRow` (Kotlin): a grade changed in the row, then the
    row deleted.
  - The whole suites: `elasticore-base`'s 1428 tests, `elasticore-springboot3`'s 284 and `elasticore-springboot4`'s
    285, the pet store's 38, the MyBatis pet store's 13 and `elasticore-template-kt`'s 181.
- Files: `SignInRefreshRules` (new), `ServiceAccessRules` (`refreshesPerRequest`, the option warnings, `roleText`
  shared with the sign-in), `HexUseCaseRules` (`hex-usecase-refresh`), `RuleCatalog`, `RulesGenRunner.needsV3`,
  templates `v3/sessionRefresh.tmpl`, `kt/sessionRefresh.tmpl`; runtime `SessionRefreshFilter` (new),
  `SessionCurrentUser`; the annotation guide's `@signin`, the DSL guide's table and the rule guide (en, ko).
