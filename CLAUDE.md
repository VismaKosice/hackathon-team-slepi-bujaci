# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

High-performance pension calculation engine built for the Visma Performance Hackathon. The engine processes ordered sequences of mutations (operations) to calculate pension entitlements. **Correctness is paramount, then performance.**

## Technology Stack

- **Framework**: Quarkus 3.31.2 (Java 25)
- **REST**: RESTEasy Reactive (quarkus-rest)
- **DI**: Quarkus Arc (CDI)
- **Build**: Maven with Quarkus plugin
- **Deployment**: Docker (JVM mode)
- **Testing**: k6 for load testing

## Build & Run Commands

```bash
# Development mode (hot reload)
./mvnw quarkus:dev

# Build JAR
./mvnw package -DskipTests

# Build Docker image
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/hyperspeed-quarkus-jvm .

# Run container
docker compose up

# Load testing
K6_WEB_DASHBOARD=true k6 run load-test.js
```

## Core Architecture

### Mutation Processing Pipeline

The engine implements a **sequential state transformation model**:

1. **Request** → Contains ordered list of mutations
2. **Initial Situation** → Empty state (`{ "dossier": null }`)
3. **Sequential Processing** → Each mutation validates then transforms the situation
4. **End Situation** → Final calculated state
5. **Response** → Metadata + messages + processed mutations + end situation

**Critical**: Mutations MUST be processed sequentially in array order. State from one mutation feeds into the next.

### State Management

The **Situation** object is the core state container:
- Contains a **Dossier** (or null before creation)
- Dossier contains **Persons** (participant info) and **Policies** (pension schemes)
- Each mutation transforms this state immutably or mutably (design choice affects JSON Patch bonus)

### Mutation Architecture

Each mutation must implement two phases:

1. **Validation Phase**: Check prerequisites, produce CRITICAL or WARNING messages
   - CRITICAL halts processing immediately (calculation outcome: FAILURE)
   - WARNING continues processing but records message
2. **Application Phase**: Transform the situation state

**Bonus**: For "Clean Mutation Architecture" (4 points), implement a common interface/abstract class with a registry-based dispatcher (no switch statements on mutation names).

### Required Mutations

1. **create_dossier**: Creates initial dossier with participant
2. **add_policy**: Adds pension policy to dossier (generates policy_id as `{dossier_id}-{sequence}`)
3. **apply_indexation**: Batch salary adjustments with filtering (scheme_id, effective_before)
4. **calculate_retirement_benefit**: Complex calculation including eligibility checks, years of service, weighted average salary, pension distribution

### Performance Hotspots

Based on hackathon scoring criteria:

1. **JSON Serialization**: Largest time cost. Consider Jackson configuration, streaming, avoiding deep copies
2. **Policy Lookups/Filtering**: Used heavily by `apply_indexation` and `calculate_retirement_benefit`. Data structure choice (List vs indexed structures) matters
3. **Duplicate Detection**: `add_policy` must check for duplicates (scheme_id + employment_start_date). With many policies, O(n) vs O(1) matters
4. **Date Arithmetic**: `calculate_retirement_benefit` uses formula `days_between / 365.25`. Efficient date handling matters at scale
5. **Batch Operations**: `apply_indexation` updates multiple policies - batch processing vs one-by-one affects performance
6. **Concurrency Model**: Quarkus default thread pool vs virtual threads vs reactive - impacts throughput and concurrency scores

### Error Handling Semantics

**CRITICAL errors**:
- Halt processing immediately
- `end_situation` reflects state BEFORE failing mutation
- `mutations` array includes the failing mutation
- `calculation_outcome`: FAILURE
- Return HTTP 200 (not error status)

**WARNING messages**:
- Continue processing
- Record in messages array with references to mutation

**HTTP error codes**:
- 200: All successfully processed calculations (SUCCESS or FAILURE outcome)
- 400: Malformed request (cannot parse)
- 500: Unexpected server errors

## Key Design Decisions

### Policy ID Generation
Format: `{dossier_id}-{sequence_number}` where sequence starts at 1 and increments per `add_policy` mutation.

### Numeric Precision
- Years of service: `days_between(start, end) / 365.25` (exact formula required)
- Testing tolerance: ±0.01 for floating point comparisons
- No specific rounding required for monetary values

### Bonus Features (30 points total)

1. **Forward JSON Patch** (7pts): RFC 6902 patches transforming previous → current situation
2. **Backward JSON Patch** (4pts): Reverse patches (requires forward patch first)
3. **Clean Mutation Architecture** (4pts): Interface/registry pattern for mutations
4. **Cold Start Performance** (5pts): < 500ms Docker start → first HTTP response
5. **External Scheme Registry** (5pts): HTTP client for scheme accrual rates with caching/pooling
6. **project_future_benefits** (5pts): Bonus mutation projecting multiple retirement dates

## File Organization

- `src/main/java/flyt/inschool/` - Java source (currently minimal, expects expansion)
- `mutation-definitions/` - JSON schema examples for each mutation type
- `api-spec.yaml` - OpenAPI 3.0 specification (endpoint: POST /calculation-requests)
- `data-model.md` - ERD and entity descriptions
- `README.md` / `ZADANIE.md` - Full hackathon requirements and examples

## Testing Strategy

- `load-test.js` - k6 scenarios (smoke, load, ramp, stress)
- Hackathon tests verify: correctness (40pts), performance (40pts), bonus features (30pts)
- Performance measured only on tests that pass correctness
- Scoring is relative (team vs team) not absolute

## Performance Targets (Reference)

These are calibration points, not strict thresholds:

**Simple request (3 mutations)**:
- Excellent: < 1ms
- Good: 1-5ms

**Complex request (10+ mutations)**:
- Excellent: < 5ms
- Good: 5-15ms

**Throughput**:
- Excellent: > 10,000 req/s
- Good: 2,000-10,000 req/s

## Development Approach

1. **Correctness first**: Get all 4 mutations working with proper validation
2. **Performance optimization**: Focus on hotspots (JSON, indexing, batching)
3. **Bonus features**: Only after core is working and optimized

## Important Notes

- The single endpoint (`POST /calculation-requests`) handles all calculation requests
- No database or persistence layer mentioned - pure in-memory computation
- Maven wrapper (`./mvnw`) is included - no need for global Maven installation
- Java 25 is used (modern features available)
- Quarkus packaging type handles all runtime dependencies
