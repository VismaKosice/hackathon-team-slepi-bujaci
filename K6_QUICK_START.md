# K6 Load Test - Quick Start Guide

## Overview

A comprehensive k6 load test scenario has been created to measure the performance of the Pension Calculation Engine against the hackathon requirements.

## Files Created

1. **`load-test.js`** - The main k6 test script
   - 2-minute duration with progressive load ramping
   - Tests both simple (3 mutations) and complex (10+ mutations) requests
   - Calculates performance metrics and tier classification

2. **`run-load-test.sh`** - Helper script for easy test execution
   - Multiple test profile options
   - Automatic app reachability checks
   - Report generation

3. **`K6_LOAD_TEST_README.md`** - Comprehensive documentation
   - Detailed setup instructions
   - Performance tier explanations
   - Troubleshooting guide

## Quick Start (3 Steps)

### 1. Install k6 (if not already installed)

```bash
brew install k6
```

Verify installation:
```bash
k6 version
```

### 2. Start Your Application

Make sure your Pension Calculation Engine is running and accessible at `http://localhost:8080`:

```bash
# From the project root
./mvnw quarkus:dev
```

Or if already built:
```bash
docker run -p 8080:8080 hyperspeed-quarkus:latest
```

### 3. Run the Load Test

#### Option A: Using the helper script (recommended)

```bash
# Standard 2-minute test
./run-load-test.sh standard

# Quick 30-second test (for rapid iterations)
./run-load-test.sh quick

# Generate HTML report
./run-load-test.sh report

# Show all available commands
./run-load-test.sh help
```

#### Option B: Run k6 directly

```bash
k6 run load-test.js
```

With custom endpoint:
```bash
k6 run --env BASE_URL=http://your-server:8080 load-test.js
```

## Test Configuration

### Duration
- **Total**: 2 minutes
- **Phases**: Warm-up → Ramp-up → Peak → Sustained → Stress → Cool-down

### Load Profile
```
200 ├─────────────────────────────┐
    │        (Stress Phase)        │
150 ├────────────────┐─────────────┤
    │   (Sustained)  │             │
100 ├──────────┐─────┘             │
    │  (Peak)  │                   │
 50 ├──┐───────┘                   │
    │  │(Ramp)                     │
 10 ├──┘──┐                        │
    │Warm │                        │
  0 └─────┴────────────────────────┴─────
    0    30   60   90   120  150  (seconds)
```

### Request Mix
- **50% Simple Requests**: 3 mutations
  - create_dossier
  - add_policy
  - apply_indexation

- **50% Complex Requests**: 10+ mutations
  - create_dossier
  - 7x add_policy
  - 2x apply_indexation
  - 1x calculate_retirement_benefit

## Understanding Performance Tiers

### Simple Requests (3 mutations)
| Tier | Latency | Goal |
|------|---------|------|
| Excellent ⭐⭐⭐⭐⭐ | < 1ms | `p(95) must be < 1ms` |
| Good ⭐⭐⭐⭐ | 1-5ms | `p(95) must be < 5ms` |
| Acceptable ⭐⭐⭐ | 5-20ms | `p(95) must be < 20ms` |
| Slow ⭐ | > 20ms | Performance needs work |

### Complex Requests (10+ mutations)
| Tier | Latency | Goal |
|------|---------|------|
| Excellent ⭐⭐⭐⭐⭐ | < 5ms | `p(95) must be < 5ms` |
| Good ⭐⭐⭐⭐ | 5-15ms | `p(95) must be < 15ms` |
| Acceptable ⭐⭐⭐ | 15-50ms | `p(95) must be < 50ms` |
| Slow ⭐ | > 50ms | Performance needs work |

### Throughput (sustained, single container)
| Tier | Requests/sec | Goal |
|------|--------------|------|
| Excellent ⭐⭐⭐⭐⭐ | > 10,000 | Handle 10,000+ req/s |
| Good ⭐⭐⭐⭐ | 2,000-10,000 | Handle 2,000-10,000 req/s |
| Acceptable ⭐⭐⭐ | 500-2,000 | Handle 500-2,000 req/s |
| Slow ⭐ | < 500 | Performance needs work |

## Reading the Results

After the test completes, you'll see output like:

```
================================================================================
K6 LOAD TEST SUMMARY - Pension Calculation Engine
================================================================================

THROUGHPUT METRICS:
 Total Requests: 15,234
 Requests/sec (avg): 127
 Test Duration: 120.0s

LATENCY METRICS (ms):
 Simple Requests (3 mutations):
   p(50): 3.45
   p(90): 8.23
   p(95): 12.56
   p(99): 18.90
   max: 42.15

 Complex Requests (10+ mutations):
   p(50): 18.67
   p(90): 32.45
   p(95): 41.23
   p(99): 58.90
   max: 145.67

ERROR METRICS:
 Error Rate: 0.23%
 Success Rate: 99.77%

PERFORMANCE TIER CLASSIFICATION:
 Simple Requests (p95): Acceptable (5-20ms)
 Complex Requests (p95): Acceptable (15-50ms)
 Throughput (avg): Good (2,000-10,000 req/s)
```

### Key Metrics to Watch

1. **p(95) for Simple Requests**: Should be < 20ms (Acceptable) or better
2. **p(95) for Complex Requests**: Should be < 50ms (Acceptable) or better
3. **Throughput**: Should be > 500 req/s (Acceptable) or better
4. **Error Rate**: Should be < 1%
5. **Success Rate**: Should be > 99%

## Test Scenarios Explained

### Simple Request Example (3 mutations)
```json
{
  "mutations": [
    { "mutation_definition_name": "create_dossier", ... },
    { "mutation_definition_name": "add_policy", ... },
    { "mutation_definition_name": "apply_indexation", ... }
  ]
}
```

### Complex Request Example (10+ mutations)
```json
{
  "mutations": [
    { "mutation_definition_name": "create_dossier", ... },
    { "mutation_definition_name": "add_policy", ... },  // 7 times
    { "mutation_definition_name": "add_policy", ... },
    { "mutation_definition_name": "add_policy", ... },
    { "mutation_definition_name": "add_policy", ... },
    { "mutation_definition_name": "add_policy", ... },
    { "mutation_definition_name": "add_policy", ... },
    { "mutation_definition_name": "add_policy", ... },
    { "mutation_definition_name": "apply_indexation", ... },  // 2 times
    { "mutation_definition_name": "apply_indexation", ... },
    { "mutation_definition_name": "calculate_retirement_benefit", ... }
  ]
}
```

## Performance Optimization Tips

If your results show you're not meeting the desired tier:

### For High Latency (Simple Requests)
1. Check serialization/deserialization performance
2. Profile the request handling code
3. Review database connection pooling
4. Consider caching frequently accessed data

### For High Latency (Complex Requests)
1. Profile mutation processing
2. Optimize policy lookup and filtering (e.g., indexation)
3. Consider parallelizing independent policy calculations
4. Review retirement benefit calculation logic

### For Low Throughput
1. Check thread pool configuration
2. Monitor garbage collection frequency
3. Review connection pool settings
4. Check for resource contention

## Advanced Usage

### Generate HTML Report
```bash
./run-load-test.sh report
```
This generates an interactive HTML report with detailed charts and statistics.

### Custom Endpoint
```bash
./run-load-test.sh standard --url http://production-server:8080
```

### Stress Testing (High Concurrency)
```bash
./run-load-test.sh stress
```
Runs with 500 concurrent users for 2 minutes (more aggressive than standard).

### Extended Test (5 minutes)
```bash
./run-load-test.sh extended
```
Provides longer duration for more stable average metrics.

## Docker Integration

If running the application in Docker:

```bash
# Build and run the application
docker build -t hyperspeed-quarkus .
docker run -d -p 8080:8080 hyperspeed-quarkus

# Run the load test
./run-load-test.sh standard

# Monitor application during load test
docker stats
```

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Performance Tests

on: [push, pull_request]

jobs:
  load-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Start application
        run: docker-compose up -d
      - name: Wait for app
        run: sleep 10
      - name: Install k6
        run: sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69 && echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6-stable.list && sudo apt-get update && sudo apt-get install k6
      - name: Run load test
        run: k6 run load-test.js
```

## Troubleshooting

### "Connection refused" error
```bash
# Ensure the application is running
./mvnw quarkus:dev

# In another terminal, run the test
./run-load-test.sh standard
```

### High error rates
- Check application logs for errors
- Verify the `/calculation-requests` endpoint exists
- Ensure request payload format is correct
- Check if the server is resource-constrained

### Memory issues on test client
```bash
# Run a smaller test with fewer VUs
k6 run --vus 10 --duration 30s load-test.js
```

## Next Steps

1. **Establish Baseline**: Run the test once to see current performance
2. **Identify Bottlenecks**: Use profiling tools to find slow paths
3. **Optimize**: Make targeted improvements
4. **Verify**: Re-run the test to measure improvements
5. **Iterate**: Repeat until you reach your performance goals

## References

- [k6 Documentation](https://k6.io/docs/)
- [k6 JavaScript API](https://k6.io/docs/javascript-api/)
- [Performance Testing Best Practices](https://k6.io/docs/testing-guides/test-types/)
- [Hackathon Requirements](./ZADANIE.md)

---

**Good luck with your performance testing! 🚀**

