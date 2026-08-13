# Azimuth refund lab

This repository is a focused public reference application for the Joker 2026 presentation on
progressive verification of agent-written code. The application demonstrates a refund worker that
may be redelivered after provider success and must not apply the financial effect twice.

## Working rules

- Read the active Azimuth change and the affected model package before implementation.
- Java targets Java 21 and Spring Boot 4.1.0. Use Spring JDBC for financial state transitions.
- Component tests enter through HTTP or AMQP and use real PostgreSQL and RabbitMQ containers.
- The provider ledger is the authority for successful financial effects. API projections are not a
  substitute oracle.
- Delivery is at least once. Describe effects as idempotent, never as exactly-once delivery.
- The demo failpoint exists only under the `demo` Spring profile and never enters business events.
- Comments explain why. Names describe domain purpose rather than implementation history.
- Do not commit, tag, push, publish content, or manufacture manual evidence unless Dima explicitly
  authorizes that external boundary.

Canonical agent skills live in `.agents/skills/`. Claude Code compatibility links live in
`.claude/skills/`; `CLAUDE.md` imports this file.
