<!--
SYNC IMPACT REPORT
==================
Version change: (none — initial ratification) → 1.0.0

Modified principles: N/A (initial constitution)

Added sections:
  - I. Clean Architecture
  - II. BDD Testing (Unit, Integration & Functional)
  - III. Programming Best Practices (SOLID, YAGNI, DRY)
  - IV. API First with OpenAPI
  - V. Code Coverage & Quality Metrics
  - Technology Standards
  - Quality Gates
  - Governance

Templates updated:
  ✅ .specify/templates/plan-template.md — Constitution Check gates aligned
  ✅ .specify/templates/spec-template.md — Acceptance Scenarios use Given/When/Then (BDD-compatible)
  ✅ .specify/templates/tasks-template.md — test task categories aligned with BDD layers

Follow-up TODOs:
  - None. All placeholders resolved.
-->

# Dropshipping Service Constitution

## Core Principles

### I. Clean Architecture (NON-NEGOTIABLE)

The project MUST follow the Clean Architecture as defined by Robert C. Martin:

- **Dependency Rule**: Source code dependencies MUST point inward only. Inner layers (Entities, Use
  Cases) MUST NOT depend on outer layers (Controllers, Gateways, Frameworks).
- **Layer separation** (inner → outer):
  1. **Entities** — Enterprise business rules. Pure POJOs/domain objects with no framework imports.
  2. **Use Cases** — Application business rules. Orchestrate entities; no direct infrastructure
     dependencies.
  3. **Interface Adapters** — Controllers, Presenters, Gateways. Translate between use cases and
     external formats.
  4. **Frameworks & Drivers** — Spring Boot, databases, HTTP clients, message brokers. Treated as
     plugins; replaceable without touching inner layers.
- Each layer MUST be independently compilable and testable.
- Cross-layer communication MUST go through defined port/adapter interfaces (no leaking concrete
  infrastructure types into use cases or entities).
- No framework annotations (e.g., `@Autowired`, `@Entity`) are allowed inside Entities or Use Cases.

**Rationale**: Isolates business logic from volatile infrastructure choices, enabling independent
testing of each layer and long-term maintainability.

### II. BDD Testing — Unit, Integration & Functional (NON-NEGOTIABLE)

All automated tests MUST follow the Behaviour-Driven Development (BDD) style using
Given / When / Then scenarios:

- **Unit tests**: MUST cover every Use Case, Entity, and domain service in isolation. Mocks/stubs
  are allowed only for dependencies crossing layer boundaries.
- **Integration tests**: MUST exercise at least one real infrastructure adapter (database, broker,
  HTTP client) per flow. No in-memory fakes substituting the real adapter unless the integration
  scope is explicitly narrowed and documented.
- **Functional (end-to-end) tests**: MUST exercise full request/response cycles through the HTTP
  interface, validating acceptance scenarios defined in `spec.md`.
- Every test MUST be named with the pattern:
  `given_<context>_when_<action>_then_<expected_outcome>`.
- Acceptance scenarios in `spec.md` (Given/When/Then) MUST map 1-to-1 to at least one functional
  test.
- Test-first order MUST be respected: write tests → confirm they FAIL → implement → confirm they
  PASS.
- Recommended frameworks: JUnit 5 + Mockito (unit), Spring Boot Test (integration),
  Cucumber or REST-assured (functional).

**Rationale**: BDD bridges requirements and code, making regression detection and stakeholder
communication explicit.

### III. Programming Best Practices — SOLID, YAGNI, DRY (NON-NEGOTIABLE)

Every production class and module MUST comply with:

- **SOLID**:
  - *Single Responsibility*: Each class has exactly one reason to change.
  - *Open/Closed*: Open for extension (via interfaces/abstract types), closed for modification.
  - *Liskov Substitution*: Subclasses/implementations are fully substitutable for their parent type.
  - *Interface Segregation*: Clients MUST NOT be forced to depend on methods they do not use;
    prefer narrow, focused interfaces.
  - *Dependency Inversion*: Depend on abstractions (interfaces/ports), not concretions.
- **YAGNI** (You Aren't Gonna Need It): Features, abstractions, or configurations MUST NOT be added
  speculatively. Every addition requires a concrete, current requirement.
- **DRY** (Don't Repeat Yourself): Duplicated logic MUST be extracted into a shared abstraction.
  A pattern appearing three or more times in the codebase MUST be refactored before merging.
- Code review gates MUST flag SOLID violations, speculative abstractions, and duplicated logic as
  blocking defects.

**Rationale**: These practices minimise coupling, reduce defect density, and keep the codebase
evolvable without accumulating technical debt.

### IV. API First with OpenAPI & openapi-generator (NON-NEGOTIABLE)

All HTTP APIs MUST follow the API First approach:

- The OpenAPI contract (`openapi.yml`) MUST be authored and reviewed **before** any implementation
  work begins for a given endpoint.
- The contract MUST be stored at `src/main/resources/openapi/openapi.yml` (or an equivalent
  canonical location documented in `plan.md`).
- Server-side stubs (controllers/interfaces) MUST be generated from the contract using
  **openapi-generator** (`openapi-generator-maven-plugin` or equivalent build-tool integration).
  Hand-writing controller signatures that duplicate the contract is prohibited.
- The generated interfaces MUST NOT be modified manually; business logic lives in Use Case
  implementations that the generated controllers delegate to.
- Contract changes MUST be proposed as a diff to `openapi.yml` first; implementation follows only
  after the contract is approved.
- The contract MUST include: summary, description, request/response schemas (with examples),
  error responses (4xx, 5xx), and security schemes where applicable.

**Rationale**: Contract-first development prevents interface drift, enables parallel frontend/backend
work, and provides machine-readable documentation automatically.

### V. Code Coverage & Quality Metrics with JaCoCo (NON-NEGOTIABLE)

Coverage is enforced via **JaCoCo** as a build gate:

- **Per-class line/branch coverage**: MUST be > 80 % for every class under `src/main/java`.
- **Global (aggregate) coverage**: MUST be ≥ 80 % across all modules.
- The build MUST fail (`mvn verify` or `gradle check`) if either threshold is breached.
- JaCoCo HTML and XML reports MUST be generated on every CI run and stored as build artefacts.
- Generated sources (output of openapi-generator) MUST be excluded from JaCoCo measurement via
  `<excludes>` configuration.
- Mutation testing with PIT (Pitest) is RECOMMENDED quarterly to validate test quality beyond line
  coverage.

**Rationale**: Objective coverage thresholds prevent coverage regression and give the team
confidence that the BDD test suite exercises real behaviour.

## Technology Standards

- **Language**: Java 17 (LTS) or higher.
- **Build tool**: Maven or Gradle (Kotlin DSL preferred for Gradle projects).
- **Framework**: Spring Boot 3.x (or the latest LTS at project start).
- **Code generation**: `openapi-generator-maven-plugin` ≥ 7.x or equivalent Gradle plugin.
- **Coverage tool**: JaCoCo ≥ 0.8.x, integrated as a Maven/Gradle plugin.
- **Testing libraries**: JUnit 5, Mockito 5, AssertJ; Cucumber (optional for BDD scenarios).
- **Static analysis**: Checkstyle + SpotBugs as part of the standard build lifecycle.
- Framework or library upgrades that affect generated code or coverage configuration MUST trigger a
  full test-suite rerun and coverage report review before merging.

## Quality Gates

Every pull request MUST pass all of the following gates before merge:

1. **OpenAPI contract present**: `openapi.yml` exists and is valid for every new/modified endpoint.
2. **Generated code up-to-date**: `mvn generate-sources` (or equivalent) produces no diff against
   committed generated files.
3. **All BDD tests green**: Unit, integration, and functional test suites pass with zero failures.
4. **Coverage thresholds met**: JaCoCo reports per-class > 80 % and global ≥ 80 %; build MUST NOT
   be bypassed with `maven.test.skip` or equivalent.
5. **Clean Architecture compliance**: No inner-layer class imports an outer-layer type (enforced via
   ArchUnit tests or manual review checklist in `plan.md`).
6. **Static analysis clean**: No new Checkstyle or SpotBugs violations above severity WARNING.
7. **YAGNI/DRY review**: Code review explicitly confirms no speculative abstractions or triplicated
   logic.

Failing any gate is a blocking defect; the author MUST resolve it before re-requesting review.

## Governance

- This constitution supersedes all prior verbal agreements, wiki entries, or informal conventions.
- **Amendment procedure**: Any proposed change requires (a) a written rationale, (b) a pull request
  updating this file, (c) approval from the tech lead or project owner, and (d) a migration plan
  if existing code is non-compliant.
- **Versioning policy** (semantic):
  - MAJOR: Backward-incompatible removal or redefinition of a principle.
  - MINOR: New principle or section added; material expansion of existing guidance.
  - PATCH: Clarifications, wording improvements, typo corrections.
- **Compliance review**: Every sprint retrospective MUST include a brief check that the five
  non-negotiable principles are being upheld; violations discovered must be logged as technical-debt
  items with a resolution sprint assigned.
- All feature plans (`plan.md`) MUST include a **Constitution Check** section verifying compliance
  with all five principles before Phase 0 research proceeds.
- The `CLAUDE.md` runtime guidance file serves as the operative development guide; it MUST remain
  consistent with this constitution.

**Version**: 1.0.0 | **Ratified**: 2026-07-05 | **Last Amended**: 2026-07-05
