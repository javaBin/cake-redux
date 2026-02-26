# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**cake-redux** is an administrative web application for JavaZone program committees to review and manage talk proposals. It reads from and writes to EMS (Event Management System) via HTTP — there is no direct database.

## Build & Run

```bash
# Build (creates uber-jar)
mvn clean package

# Skip tests during build
mvn clean package -Dmaven.test.skip=true

# Run the application (requires config file)
java -jar target/cake-redux-0.3-SNAPSHOT-jar-with-dependencies.jar <config_file>

# Run with Docker locally
./run-local.sh [path_prefix]
```

The app runs on port 8081 by default. Access it at `http://localhost:8081/secured/`.

## Testing

```bash
# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=SlotTimeFormatterTest

# Run a specific test method
mvn test -Dtest=SlotTimeFormatterTest#shouldReturnFormattedSlots
```

Test frameworks: JUnit 4, Mockito 1.9.5, AssertJ 1.7.0. There are also JavaScript tests using Jasmine in `src/test/javascript/`.

## Architecture

**Entry point**: `WebServer.java` — starts embedded Jetty and registers all servlets and filters.

**Servlet routing**:
- `/secured/data/*` → `DataServlet` (main REST API, authentication required)
- `/data/*` → `OpenDataServlet` (public)
- `/signin/` → `SigninServlet`
- `/entrance` → `EntranceServlet`
- `/videoadmin/api/*` → `VideoAdminServlet`

**Authentication**: Google OAuth (default), with optional Whyda and Slack auth. `SecurityFilter` enforces access on secured routes. Set `noAuthMode=true` in config to bypass for local development.

**Configuration**: `Configuration.java` is a singleton reading from a `key=value` config file (path passed as CLI arg) or environment variables. Required keys include `emsEventLocation`, `emsUser`, `emsPassword`, `googleClientId`, `googleClientSecret`, `authorizedUsers`, `cakeLocation`.

**Key integrations**:
- EMS: source of truth for sessions/proposals
- Sleepingpill: slot/schedule management (`SleepingpillCommunicator.java`)
- SendGrid / SMTP: email via `MailSenderService` (controlled by `mailSenderImplementation` config)
- AWS KMS + SOPS: secret decryption in Docker deployments

**Mixed language**: Most backend is Java; `PkomFeedbackCompute.kt` and `TagsHandler.kt` are Kotlin. Maven compiles Kotlin first, then Java.

**Frontend**: AngularJS 1.5.2 served as static resources from `src/main/resources/webapp/`. Controllers live in `webapp/scripts/`, communicating with the backend via `DataServlet`.

## Technology Stack

- Java 11 source/target (CI builds with Java 21 / Corretto)
- Kotlin 1.8.0
- Jetty (embedded)
- Maven (assembly plugin produces fat JAR)
- AngularJS 1.5.2 (frontend)
- Docker (multi-platform: linux/amd64, linux/arm64)
- GitHub Actions CI → pushes to `ghcr.io/javabin/cake-redux:edge`
