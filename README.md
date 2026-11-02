# An Cư PropTech automation framework

Requires Java 21 and Maven 3.9+. Uses Selenium 4.21.0 and JUnit 5.10.2.

## Local configuration

Copy `.env.example` to `.env.local` in this project and fill the fixture values
for the modules being tested. Copy `config.local.properties.example` to
`config.local.properties`; its `sut.env.file=.env.local` loads the test project's
fixture file when running Maven or IntelliJ from the project root. Both local
files are ignored by Git. Never commit real credentials.

JVM properties and environment variables override file configuration:
`base.url`/`BASE_URL`, `browser`/`BROWSER`, `headless`/`HEADLESS`,
`timeout.seconds`/`TIMEOUT_SECONDS`. Set `-Dsut.env.file=<path>` or `SUT_ENV_FILE`
to load another fixture file. Backend aliases `LOCAL_CUSTOMER_EMAIL` and
`LOCAL_CUSTOMER_PASSWORD` remain supported for local test accounts.

## Framework checks

```powershell
mvn test-compile
mvn "-Dtest=ConfigTest,DataUtilitiesTest,ScreenshotExtensionTest" test
```

These checks do not need a running website. UI tests require the An Cư frontend,
API, database and matching fixtures. See `docs/AUTOMATION_GUIDE.md` for the
CSV/token and Page Object contracts.

Surefire results are written to `target/surefire-reports/`. Test-body failures
capture screenshots before browser teardown under `target/screenshots/`.
Passing/aborted tests do not produce failure screenshots. Screenshot errors are
attached to the original exception. Archive evidence against the tested commit.
