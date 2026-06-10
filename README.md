# Intelligent Event Routing Engine

A Spring Boot + Kafka system that consumes business events, enriches them with LLM-powered analysis (risk scoring, anomaly detection, recommended actions), and routes them to the correct downstream handler based on configurable rules — without hardcoded logic.

---

## What It Does

Most event-driven systems route based on rigid rules: `if event_type == X then send to Y`. This engine adds an intelligence layer — each event is analyzed by an LLM before routing decisions are made. Risk scores, anomaly flags, and recommended actions are attached to the event payload. Routing rules live in a database and can be changed without touching code.

**Core capabilities:**
- Consumes business events from a Kafka topic (order placed, payment failed, user signup, system alert)
- Enriches each event with LLM analysis: risk score (0–1), anomaly flag, recommended action
- Routes enriched events to downstream handlers based on DB-configured rules
- Dead letter queue for failed events with automatic retry
- REST API to query event history, routing decisions, and analytics
- Full event audit trail in PostgreSQL

---

## Architecture

```
Event Producers (Orders, Payments, Auth, Alerts)
        │
        ▼
┌───────────────────┐
│   Kafka Topic     │  events.raw
│   (events.raw)    │
└───────┬───────────┘
        │
        ▼
┌───────────────────┐
│  Consumer Service │  Spring Boot Kafka Consumer
│  (Spring Boot)    │
└───────┬───────────┘
        │
        ▼
┌───────────────────┐
│  LLM Enrichment  │  OpenAI API — risk score,
│  Service          │  anomaly flag, recommended action
└───────┬───────────┘
        │
        ▼
┌───────────────────┐
│  Routing Engine   │  Reads rules from PostgreSQL
│                   │  Routes to correct handler
└──┬──────┬─────────┘
   │      │
   ▼      ▼
Handler A  Handler B  ...  Dead Letter Queue
(Webhook) (DB Write)       (events.dlq)
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 3.x |
| Message Broker | Apache Kafka |
| LLM Integration | OpenAI API (Java HTTP client) |
| Routing Rules Storage | PostgreSQL |
| ORM | Spring Data JPA · Hibernate |
| Containerization | Docker · Docker Compose |
| Build Tool | Maven |
| API | Spring MVC REST |

---

## Features

- **LLM enrichment pipeline** — every event gets risk score + anomaly flag + recommended action before routing
- **DB-driven routing rules** — rules stored in PostgreSQL, updated via API with no redeploy needed
- **Dead letter queue** — failed or unroutable events go to `events.dlq` topic with retry metadata
- **Pluggable handlers** — webhook handler, database write handler, email trigger handler
- **Event audit trail** — every event stored with full enrichment data and routing decision
- **REST API** — query events, update routing rules, view analytics
- **Docker Compose** — Kafka + Zookeeper + PostgreSQL + App in one command

---

## Project Structure

```
event-routing-engine/
├── src/main/java/com/eventrouter/
│   ├── config/
│   │   ├── KafkaConfig.java          # Consumer + producer config
│   │   └── OpenAiConfig.java
│   ├── consumer/
│   │   └── EventConsumer.java        # Kafka listener — main.raw topic
│   ├── service/
│   │   ├── EnrichmentService.java    # LLM call — risk score, anomaly, action
│   │   ├── RoutingService.java       # Reads rules, decides handler
│   │   ├── HandlerService.java       # Dispatches to correct handler
│   │   └── DeadLetterService.java    # DLQ producer + retry logic
│   ├── handler/
│   │   ├── WebhookHandler.java       # Sends enriched event to external URL
│   │   ├── DatabaseHandler.java      # Writes to PostgreSQL
│   │   └── AlertHandler.java         # Triggers alert notification
│   ├── controller/
│   │   ├── EventController.java      # Query event history
│   │   ├── RoutingRuleController.java# CRUD routing rules
│   │   └── AnalyticsController.java  # Volume, routing breakdown, anomalies
│   ├── model/
│   │   ├── Event.java
│   │   ├── EnrichedEvent.java
│   │   └── RoutingRule.java
│   └── repository/
│       ├── EventRepository.java
│       └── RoutingRuleRepository.java
├── src/main/resources/
│   └── application.yml
├── docker-compose.yml        # Kafka + Zookeeper + Postgres + App
├── Dockerfile
└── pom.xml
```

---

## Getting Started

### Prerequisites
- Java 17+
- Docker + Docker Compose
- OpenAI API key

### Run with Docker Compose

```bash
# Clone the repo
git clone https://github.com/parinitaschinchalikar/event-routing-engine.git
cd event-routing-engine

# Configure environment
cp .env.example .env
# Add OPENAI_API_KEY to .env

# Start the full stack
docker-compose up --build
```

API available at `http://localhost:8080`
Kafka running on `localhost:9092`

---

## API Reference

### Publish a test event
```http
POST /events/publish
Content-Type: application/json

{
  "eventType": "payment_failed",
  "payload": {
    "orderId": "ORD-5521",
    "amount": 249.99,
    "customerId": "C-1042",
    "attemptCount": 3
  }
}
```

### View enriched event
```http
GET /events/{eventId}
```

**Response:**
```json
{
  "eventId": "evt_a3f9c1",
  "eventType": "payment_failed",
  "enrichment": {
    "riskScore": 0.87,
    "anomalyFlag": true,
    "recommendedAction": "escalate_to_fraud_review",
    "reasoning": "Third failed attempt within 10 minutes on high-value order"
  },
  "routedTo": "alert_handler",
  "routingRuleId": "rule_003",
  "processedAt": "2026-05-26T10:45:00Z"
}
```

### Create a routing rule
```http
POST /routing-rules
Content-Type: application/json

{
  "eventType": "payment_failed",
  "condition": "riskScore > 0.8",
  "handler": "alert_handler",
  "priority": 1
}
```

### View analytics
```http
GET /analytics/summary
```

**Response:**
```json
{
  "totalEventsProcessed": 4821,
  "last24Hours": 312,
  "anomaliesDetected": 47,
  "routingBreakdown": {
    "webhook_handler": 1840,
    "database_handler": 2710,
    "alert_handler": 271
  },
  "deadLetterCount": 18
}
```

---

## Routing Rules

Rules are stored in PostgreSQL and evaluated in priority order. No code changes needed to update routing logic.

| Field | Description |
|---|---|
| `eventType` | Which event type this rule applies to |
| `condition` | Expression evaluated against enriched event (`riskScore > 0.8`) |
| `handler` | Which handler receives the event |
| `priority` | Lower number = evaluated first |

---

## Environment Variables

```env
OPENAI_API_KEY=your_openai_api_key
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC_RAW=events.raw
KAFKA_TOPIC_DLQ=events.dlq
POSTGRES_URL=jdbc:postgresql://localhost:5432/eventdb
POSTGRES_USER=eventuser
POSTGRES_PASSWORD=eventpassword
```

---

## License

MIT
