import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend, Counter, Gauge } from 'k6/metrics';

/**
 * K6 Load Test for Pension Calculation Engine
 * 
 * This load test is configured to test the calculation endpoint at:
 * POST /calculation-requests (src/main/java/flyt/inschool/CalculationResource.java)
 * 
 * Payload structure based on the example in README.md (lines 106-148)
 * Tests all core mutations: create_dossier, add_policy, apply_indexation, calculate_retirement_benefit
 */

// Custom metrics for performance analysis
const errorRate = new Rate('errors');
const successRate = new Rate('success');
const requestDuration = new Trend('request_duration');
const simpleRequestDuration = new Trend('simple_request_duration');
const complexRequestDuration = new Trend('complex_request_duration');
const throughputCounter = new Counter('throughput');
const concurrentUsers = new Gauge('concurrent_users');

// Configuration for the load test
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TEST_DURATION = '2m';

export const options = {
  scenarios: {
    load_test: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        // Warm-up phase (0-30s): Gradually increase users to measure baseline
        { duration: '30s', target: 10 },
        // Ramp-up phase (30s-60s): Increase load
        { duration: '30s', target: 50 },
        // Peak phase (60s-90s): High sustained load
        { duration: '30s', target: 100 },
        // Sustained load phase (90s-110s): Test throughput capacity
        { duration: '20s', target: 150 },
        // Stress phase (110s-120s): Push towards limits
        { duration: '10s', target: 200 },
        // Cool-down phase (120s-2m): Graceful ramp down
        { duration: '40s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    // Correctness thresholds
    'success': ['rate>0.99'],

    // Latency thresholds for simple requests (3 mutations)
    'simple_request_duration': [
      'p(95)<20',    // 95% percentile should be acceptable (<20ms)
      'p(99)<50',    // 99% percentile should be slow but acceptable
    ],

    // Latency thresholds for complex requests (10+ mutations)
    'complex_request_duration': [
      'p(95)<50',    // 95% percentile should be acceptable (<50ms)
      'p(99)<100',   // 99% percentile should be slow but acceptable
    ],

    // Overall request duration
    'request_duration': [
      'p(90)<30',
      'p(95)<50',
    ],

    // Error rate threshold
    'errors': ['rate<0.01'],
  },
};

// Generate unique IDs for test data
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

// Create a simple request payload (3 mutations)
function createSimpleRequestPayload() {
  const dossierId = generateUUID();
  const personId = generateUUID();
  const timestamp = new Date().toISOString().split('T')[0];

  return {
    tenant_id: `tenant-${generateUUID().substring(0, 8)}`,
    calculation_instructions: {
      mutations: [
        {
          mutation_id: generateUUID(),
          mutation_definition_name: 'create_dossier',
          mutation_type: 'DOSSIER_CREATION',
          actual_at: timestamp,
          mutation_properties: {
            dossier_id: dossierId,
            person_id: personId,
            name: `Person ${Math.floor(Math.random() * 10000)}`,
            birth_date: '1960-06-15',
          },
        },
        {
          mutation_id: generateUUID(),
          mutation_definition_name: 'add_policy',
          mutation_type: 'DOSSIER',
          actual_at: timestamp,
          dossier_id: dossierId,
          mutation_properties: {
            scheme_id: `SCHEME-${Math.floor(Math.random() * 1000)}`,
            employment_start_date: '2000-01-01',
            salary: 50000 + Math.floor(Math.random() * 50000),
            part_time_factor: 0.8 + Math.random() * 0.2,
          },
        },
        {
          mutation_id: generateUUID(),
          mutation_definition_name: 'apply_indexation',
          mutation_type: 'DOSSIER',
          actual_at: timestamp,
          dossier_id: dossierId,
          mutation_properties: {
            percentage: 0.02 + Math.random() * 0.04,
          },
        },
      ],
    },
  };
}

// Create a complex request payload (10+ mutations)
function createComplexRequestPayload() {
  const dossierId = generateUUID();
  const personId = generateUUID();
  const timestamp = new Date().toISOString().split('T')[0];
  const mutations = [];

  // Create dossier
  mutations.push({
    mutation_id: generateUUID(),
    mutation_definition_name: 'create_dossier',
    mutation_type: 'DOSSIER_CREATION',
    actual_at: timestamp,
    mutation_properties: {
      dossier_id: dossierId,
      person_id: personId,
      name: `Person ${Math.floor(Math.random() * 10000)}`,
      birth_date: '1960-06-15',
    },
  });

  // Add 7 policies
  for (let i = 0; i < 7; i++) {
    mutations.push({
      mutation_id: generateUUID(),
      mutation_definition_name: 'add_policy',
      mutation_type: 'DOSSIER',
      actual_at: timestamp,
      dossier_id: dossierId,
      mutation_properties: {
        scheme_id: `SCHEME-${i % 3}`,
        employment_start_date: `${2000 + i}-01-01`,
        salary: 30000 + i * 5000 + Math.floor(Math.random() * 20000),
        part_time_factor: 0.6 + Math.random() * 0.4,
      },
    });
  }

  // Add 2 indexation mutations with different filters
  mutations.push({
    mutation_id: generateUUID(),
    mutation_definition_name: 'apply_indexation',
    mutation_type: 'DOSSIER',
    actual_at: timestamp,
    dossier_id: dossierId,
    mutation_properties: {
      percentage: 0.03,
      scheme_id: 'SCHEME-0',
    },
  });

  mutations.push({
    mutation_id: generateUUID(),
    mutation_definition_name: 'apply_indexation',
    mutation_type: 'DOSSIER',
    actual_at: timestamp,
    dossier_id: dossierId,
    mutation_properties: {
      percentage: 0.02,
    },
  });

  // Add retirement benefit calculation
  mutations.push({
    mutation_id: generateUUID(),
    mutation_definition_name: 'calculate_retirement_benefit',
    mutation_type: 'DOSSIER',
    actual_at: timestamp,
    dossier_id: dossierId,
    mutation_properties: {
      retirement_date: '2025-01-01',
    },
  });

  return {
    tenant_id: `tenant-${generateUUID().substring(0, 8)}`,
    calculation_instructions: {
      mutations,
    },
  };
}

export default function () {
  concurrentUsers.set(__VU);

  // Alternate between simple and complex requests
  const isSimpleRequest = __ITER % 2 === 0;
  const payload = isSimpleRequest ? createSimpleRequestPayload() : createComplexRequestPayload();
  const requestType = isSimpleRequest ? 'simple' : 'complex';

  group(`${requestType.toUpperCase()} Request (${payload.calculation_instructions.mutations.length} mutations)`, () => {
    const response = http.post(
      `${BASE_URL}/calculation-requests`,
      JSON.stringify(payload),
      {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        timeout: '30s',
      }
    );

    const duration = response.timings.duration;
    requestDuration.add(duration);

    if (isSimpleRequest) {
      simpleRequestDuration.add(duration);
    } else {
      complexRequestDuration.add(duration);
    }

    // Check response
    const success = check(response, {
      'Status is 200': (r) => r.status === 200,
      'Response has calculation_id': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.calculation_metadata && body.calculation_metadata.calculation_id;
        } catch {
          return false;
        }
      },
      'Response has calculation_result': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.calculation_result !== undefined;
        } catch {
          return false;
        }
      },
      'Calculation outcome is SUCCESS or FAILURE': (r) => {
        try {
          const body = JSON.parse(r.body);
          const outcome = body.calculation_metadata?.calculation_outcome;
          return outcome === 'SUCCESS' || outcome === 'FAILURE';
        } catch {
          return false;
        }
      },
    });

    successRate.add(success);
    errorRate.add(!success);
    throughputCounter.add(1);

    // Minimal sleep to avoid hammering the server
    // This is configurable based on the target throughput
    if (__VU <= 50) {
      sleep(0.1);
    }
  });
}

export function handleSummary(data) {
  return {
    'summary.txt': textSummary(data, { indent: ' ' }),
    stdout: textSummary(data, { indent: ' ' }),
  };
}

// Custom text summary function
function textSummary(data, options) {
  const indent = options?.indent || '';
  let summary = '\n' + '='.repeat(80) + '\n';
  summary += 'K6 LOAD TEST SUMMARY - Pension Calculation Engine\n';
  summary += '='.repeat(80) + '\n\n';

  // Test results
  if (data.metrics) {
    const metrics = data.metrics;

    summary += 'THROUGHPUT METRICS:\n';
    summary += `${indent}Total Requests: ${Math.round(metrics.throughput?.values?.count || 0)}\n`;

    const duration = data.state?.testRunDurationMs || 120000;
    const throughput = Math.round((metrics.throughput?.values?.count || 0) / (duration / 1000));
    summary += `${indent}Requests/sec (avg): ${throughput}\n`;
    summary += `${indent}Test Duration: ${(duration / 1000).toFixed(1)}s\n\n`;

    summary += 'LATENCY METRICS (ms):\n';

    if (metrics.simple_request_duration?.values) {
      const simpleMetrics = metrics.simple_request_duration.values;
      summary += `${indent}Simple Requests (3 mutations):\n`;
      summary += `${indent}  p(50): ${simpleMetrics['p(50)']?.toFixed(2) || 'N/A'}\n`;
      summary += `${indent}  p(90): ${simpleMetrics['p(90)']?.toFixed(2) || 'N/A'}\n`;
      summary += `${indent}  p(95): ${simpleMetrics['p(95)']?.toFixed(2) || 'N/A'}\n`;
      summary += `${indent}  p(99): ${simpleMetrics['p(99)']?.toFixed(2) || 'N/A'}\n`;
      summary += `${indent}  max: ${simpleMetrics['max']?.toFixed(2) || 'N/A'}\n\n`;
    }

    if (metrics.complex_request_duration?.values) {
      const complexMetrics = metrics.complex_request_duration.values;
      summary += `${indent}Complex Requests (10+ mutations):\n`;
      summary += `${indent}  p(50): ${complexMetrics['p(50)']?.toFixed(2) || 'N/A'}\n`;
      summary += `${indent}  p(90): ${complexMetrics['p(90)']?.toFixed(2) || 'N/A'}\n`;
      summary += `${indent}  p(95): ${complexMetrics['p(95)']?.toFixed(2) || 'N/A'}\n`;
      summary += `${indent}  p(99): ${complexMetrics['p(99)']?.toFixed(2) || 'N/A'}\n`;
      summary += `${indent}  max: ${complexMetrics['max']?.toFixed(2) || 'N/A'}\n\n`;
    }

    summary += 'ERROR METRICS:\n';
    summary += `${indent}Error Rate: ${((metrics.errors?.values?.rate || 0) * 100).toFixed(2)}%\n`;
    summary += `${indent}Success Rate: ${((metrics.success?.values?.rate || 0) * 100).toFixed(2)}%\n\n`;

    // Performance tier classification
    summary += 'PERFORMANCE TIER CLASSIFICATION:\n';

    if (metrics.simple_request_duration?.values?.['p(95)']) {
      const p95Simple = metrics.simple_request_duration.values['p(95)'];
      let tier = 'Slow (> 20ms)';
      if (p95Simple < 1) tier = 'Excellent (< 1ms)';
      else if (p95Simple < 5) tier = 'Good (1-5ms)';
      else if (p95Simple < 20) tier = 'Acceptable (5-20ms)';
      summary += `${indent}Simple Requests (p95): ${tier}\n`;
    }

    if (metrics.complex_request_duration?.values?.['p(95)']) {
      const p95Complex = metrics.complex_request_duration.values['p(95)'];
      let tier = 'Slow (> 50ms)';
      if (p95Complex < 5) tier = 'Excellent (< 5ms)';
      else if (p95Complex < 15) tier = 'Good (5-15ms)';
      else if (p95Complex < 50) tier = 'Acceptable (15-50ms)';
      summary += `${indent}Complex Requests (p95): ${tier}\n`;
    }

    if (throughput) {
      let tier = 'Slow (< 500 req/s)';
      if (throughput > 10000) tier = 'Excellent (> 10,000 req/s)';
      else if (throughput >= 2000) tier = 'Good (2,000-10,000 req/s)';
      else if (throughput >= 500) tier = 'Acceptable (500-2,000 req/s)';
      summary += `${indent}Throughput (avg): ${tier}\n`;
    }
  }

  summary += '\n' + '='.repeat(80) + '\n';

  return summary;
}

