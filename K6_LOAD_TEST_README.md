# K6 Load Testing Guide

This document describes how to run the k6 load test for the Pension Calculation Engine.

## Prerequisites

- k6 installed ([Installation Guide](https://k6.io/docs/getting-started/installation/))
- The Pension Calculation Engine running and accessible (default: `http://localhost:8080`)

## Installation

1. **Install k6:**
   - **macOS (using Homebrew):**
     ```bash
     brew install k6
     ```
   - **Other platforms:** See [k6 Installation Documentation](https://k6.io/docs/getting-started/installation/)

2. **Verify installation:**
   ```bash
   k6 version
   ```

## Running the Load Test

### Basic Run (Default Configuration)

Run the test against the default endpoint (`http://localhost:8080`):

```bash
k6 run load-test.js
```

### Custom Endpoint

Specify a custom base URL:

```bash
k6 run --env BASE_URL=http://your-server:8080 load-test.js
```

### With HTML Report Output

Generate an HTML report after the test completes:

```bash
k6 run --out=html=report.html load-test.js
```

### With InfluxDB Integration (for CI/CD)

Send metrics to InfluxDB for long-term monitoring:

```bash
k6 run --out=influxdb=http://localhost:8086/mydb load-test.js
```

## Test Configuration

The test is configured with the following characteristics:

### Load Profile (2-minute duration)

1. **Warm-up (0-30s):** Gradually increase from 0 to 10 users
2. **Ramp-up (30-60s):** Increase from 10 to 50 users
3. **Peak (60-90s):** Maintain 100 users
4. **Sustained (90-110s):** Increase to 150 users
5. **Stress (110-120s):** Push to 200 users
6. **Cool-down (120-120s+):** Gracefully ramp down to 0 users

### Request Mix

- **50% Simple Requests:** 3 mutations each
  - `create_dossier`
  - `add_policy`
  - `apply_indexation`

- **50% Complex Requests:** 10+ mutations each
  - `create_dossier`
  - 7x `add_policy`
  - 2x `apply_indexation` (with various filters)
  - 1x `calculate_retirement_benefit`

### Performance Thresholds (Automatically Checked)

**Latency:**
- Simple requests: p95 < 20ms (Acceptable tier)
- Complex requests: p95 < 50ms (Acceptable tier)

**Error Rate:**
- < 1% errors allowed
- > 99% success rate required

## Interpreting Results

The test output provides the following metrics:

### Throughput Metrics
- **Total Requests:** Number of requests processed
- **Requests/sec:** Average throughput during the test
- **Test Duration:** Total test execution time

### Latency Metrics
- **p(50):** Median latency (50th percentile)
- **p(90):** 90th percentile latency
- **p(95):** 95th percentile latency (used for SLA compliance)
- **p(99):** 99th percentile latency
- **max:** Maximum observed latency

### Performance Tier Classification

#### Simple Requests (3 mutations)
| Tier       | Latency | Performance |
|------------|---------|-------------|
| Excellent  | < 1ms   | ⭐⭐⭐⭐⭐ |
| Good       | 1-5ms   | ⭐⭐⭐⭐ |
| Acceptable | 5-20ms  | ⭐⭐⭐ |
| Slow       | > 20ms  | ⭐ |

#### Complex Requests (10+ mutations)
| Tier       | Latency | Performance |
|------------|---------|-------------|
| Excellent  | < 5ms   | ⭐⭐⭐⭐⭐ |
| Good       | 5-15ms  | ⭐⭐⭐⭐ |
| Acceptable | 15-50ms | ⭐⭐⭐ |
| Slow       | > 50ms  | ⭐ |

#### Throughput (sustained, single container)
| Tier       | Requests/sec | Performance |
|------------|--------------|-------------|
| Excellent  | > 10,000     | ⭐⭐⭐⭐⭐ |
| Good       | 2,000-10,000 | ⭐⭐⭐⭐ |
| Acceptable | 500-2,000    | ⭐⭐⭐ |
| Slow       | < 500        | ⭐ |

## Example Output

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

================================================================================
```

## Tips for Performance Testing

1. **Baseline Test:** Run the test once to establish a baseline before optimizations
2. **Consistent Environment:** Run tests on the same hardware/container for fair comparisons
3. **Monitor Server:** Watch server metrics (CPU, memory, connections) during the test
4. **Multiple Runs:** Run the test multiple times and compare results for consistency
5. **Isolated Testing:** Ensure no other services are competing for resources

## Analyzing Performance Issues

If your application is not meeting the performance tiers:

### High Latency Issues
- Profile your code for bottlenecks
- Check database query performance
- Review serialization/deserialization costs
- Consider caching strategies

### Low Throughput Issues
- Monitor thread pool utilization
- Check for resource contention
- Review connection pooling configuration
- Profile garbage collection impact

### Error Rate Issues
- Check application logs for errors
- Verify request payload validation
- Monitor resource exhaustion (memory, connections)
- Check for race conditions under load

## Advanced Options

### Run with Custom VU Count (ignoring ramping)

```bash
k6 run --vus 50 --duration 2m load-test.js
```

### Run with Custom Thresholds

Modify the `thresholds` object in `load-test.js` to match your SLA requirements.

### Integration with CI/CD

Example GitHub Actions workflow:
```yaml
- name: Run k6 load test
  run: k6 run --out=html=report.html load-test.js
  
- name: Upload report
  uses: actions/upload-artifact@v2
  with:
    name: k6-report
    path: report.html
```

## Troubleshooting

### Connection Refused
- Ensure the application is running on the specified endpoint
- Check firewall rules
- Verify the correct port number

### High Error Rate
- Review application logs
- Check if the `/calculation-requests` endpoint is properly implemented
- Validate request payload format

### Memory Issues on Client
- Reduce the number of VUs
- Enable disk-based results streaming with `--out`
- Run on a machine with more available memory

## References

- [k6 Official Documentation](https://k6.io/docs/)
- [k6 HTTP Module](https://k6.io/docs/javascript-api/k6-http/)
- [k6 Metrics and Thresholds](https://k6.io/docs/using-k6/metrics/)
- [k6 Scenarios](https://k6.io/docs/using-k6/scenarios/)

