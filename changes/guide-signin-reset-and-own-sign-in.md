### Documentation (sign-in - around the generated `@signin`)

#### The annotation guide shows signing in from your own code and a password reset by mail, as code the pet store runs
- Why (issue 181, items 2 and 3): applications write both by hand next to the generated sign-in, and the guide only
  showed `SessionCurrentUser.signIn` after a sign-up. A reset by mail is mail and token work that the application
  owns; signing in someone identified elsewhere (a reset link, another provider) needs the rules the generated `signin`
  follows, which were not written down in one place.
- What: a section of the annotation guide (en, ko) after the `@signin` part, "Around the generated sign-in":
  - Signing in from your own code: `SessionCurrentUser.signIn` once your code knows who it is - it starts a new session,
    so an id fixed before is worthless (what the visitor's session should keep is put back after); the key is the
    `@signin` entity's key, of its type; the role comes from the row, never from the request or a provider; another
    provider's protocol and account link stay the application's, or Spring Security with a `CurrentUser` bean.
  - A password reset by mail, on the pet store's `Customer`: an application service of the developer's that asks with
    the login and confirms with a token; a token that needs no table (the key, a 30-minute expiry, an HMAC over both
    and the row's password hash, so a new password from any write ends the link); the hash read through the generated
    ports, since no DTO carries it - a token bound to a DTO's `null` would work forever, and `issue` refuses it; the
    new password written through the use case as the application itself (`CurrentUser.callAsSystem`), hashed there;
    the same 202 for every login; nothing sent when the same login asked less than a minute before; the secret from
    the configuration; the confirm signing the customer in. What it does not do is said: sessions open elsewhere stay
    signed in.
- The code is the pet store's: `elasticore-sample-petstore/src/test/java/io/elasticore/sample/guide/signin`, outside the
  application's packages, so the shop itself and its other tests do not change.
- Checked:
  - `PasswordResetGuideTest` (HTTP, the pet store's generated services): the mail goes to the row's address; confirm
    sets the password, keeps the rest of the row, signs the customer in with a new session id and the one before is
    invalid; the old password is refused and the new one signs in; the same link twice, a link after the customer
    changed the password themselves, an altered, malformed or unknown link and a short password are refused (and the
    refusals do not use the link up); an unknown login and a row without an address get the same 202 and no mail; a
    second ask within a minute sends nothing; a token names its customer for 30 minutes and only with the hash and the
    secret it was made with, and no hash binds nothing.
  - The first version of the example read the hash from the DTO and its token kept working after the reset; the test
    failed, and the example reads the row.
  - `GuideCodeTest`: every code block of the section stands, line by line in its order, in those files, and the Korean
    guide shows the same blocks (a line changed in the guide fails it). `GuideTranslationTest`.
- Files: `elasticore-annotation-guide.md` (en, ko); in the pet store's tests `guide/signin/` - `PasswordReset`,
  `PasswordResetTokens`, `PasswordResetMail`, `PasswordResetConfig`, `PasswordResetController`,
  `PasswordResetGuideTest`, `GuideCodeTest`.
