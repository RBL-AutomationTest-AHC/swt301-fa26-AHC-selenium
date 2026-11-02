# An Cư PropTech automation

Java 21, Maven 3.9+, Selenium 4.21.0, JUnit Jupiter 5.10.2.

## Configuration

Start an isolated An Cư frontend/API/database before UI tests. Defaults are
`http://localhost:3000`, Chrome, and a 45 second explicit wait.
Override with JVM properties (`-Dbase.url=...`, `-Dheadless=true`,
`-Dbrowser=chrome`, `-Dtimeout.seconds=45`) or environment variables
(`BASE_URL`, `HEADLESS`, `BROWSER`, `TIMEOUT_SECONDS`).

Set `SUT_ENV_FILE` or `-Dsut.env.file=<absolute-path>` to a local UTF-8 dotenv
file containing fixture values. `.env.example` lists the supported keys; it is
an example and is not loaded automatically. Never commit real credentials.
There is no machine-specific backend path in the tracked configuration.

Keep test fixtures in **this project's `.env.local`**. Copy `.env.example` to
`.env.local` and fill `EXISTING_EMAIL` with an account already in the test DB
to run Register. Register does not require the account's password. Login and
Search need their own additional fixture keys from the example.

For IntelliJ gutter Run or Maven without extra options, copy
`config.local.properties.example` to `config.local.properties` in the project
root and keep `sut.env.file=.env.local` there. Run with the project root as the working
directory. This optional UTF-8 file overrides classpath configuration and is
ignored by Git. Explicit `-Dsut.env.file` and `SUT_ENV_FILE` still take priority.

Fixture/secret priority: JVM property, environment variable, local fixture file.
The backend aliases `LOCAL_CUSTOMER_EMAIL` and `LOCAL_CUSTOMER_PASSWORD` are
supported for `ACTIVE_EMAIL`, `EXISTING_EMAIL` and `ACTIVE_PASSWORD`.
Fixtures must belong to the isolated test database, not real users.

## Run

```powershell
mvn test-compile
mvn "-Dtest=DataUtilitiesTest,ScreenshotExtensionTest" test
mvn "-Dtest=LoginTest" "-Dheadless=true" test
mvn "-Dtest=ApartmentSearchTest" "-Dheadless=true" test
mvn "-Dtest=RegisterTest" "-Dheadless=true" test
mvn clean test
```

Surefire XML/text reports: `target/surefire-reports/`.
Failed UI tests capture PNGs under `target/screenshots/` before the browser is
closed. Passing and aborted tests do not create failure screenshots. Screenshot
errors are attached to the original failure rather than replacing it.

The framework tests run without a website, browser or account fixture. They do
not prove the UI suite passes. Archive UI results against the tested Git commit;
do not mark BM03 PASS without corresponding execution evidence.

## Register

In IntelliJ, select **RegisterTest (An Cu local)** to run all ten cases or
**REG-004 (An Cu local)** to run the duplicate-email case. Shared `.run/`
configurations load `.env.local` from this test project.
If using another fixture file, edit the `sut.env.file` VM
option in Run > Edit Configurations. The configurations contain a path, not
credentials. They open a visible Chrome window.

A temporary configuration created by the gutter Run button does not inherit
these VM options. Use `config.local.properties`, add
`-Dsut.env.file=<test .env.local path>` to that configuration, or use the
named configuration above. Without any fixture-file configuration REG-004 fails
with `Thiếu fixture: EXISTING_EMAIL` before it opens the form; the blank failure
screenshot in that situation is an environment failure, not a duplicate-email
assertion failure. CDP warnings are separate from the missing-fixture exception.

Ten Register cases use `testdata/register_data.csv`. Set `EXISTING_EMAIL` to an
existing isolated account for TC-REG-004 (or use the backend fixture alias
`LOCAL_CUSTOMER_EMAIL`). The fixture is uppercased to check case independence.
Other cases generate unique addresses under `example.invalid`; password values
are synthetic test data. Successful cases create pending accounts in the test
database. They stop at email verification and do not read inboxes or confirm OTP.
The SUT's account-created login fallback is accepted only with its exact notice.

The server may report unavailable SMTP after creating an account; this is not
registration failure. Use an isolated environment with mail disabled or a test
mail service. Keep generated accounts for evidence or clean only accounts with
the test prefix after review; this suite does not delete database records.
