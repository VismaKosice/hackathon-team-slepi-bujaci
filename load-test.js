import http from 'k6/http';
import {check} from 'k6';

const BASE_URL = 'http://localhost:8080';
const SCENARIO = 'load';
const TARGET_RPS = '50';
const DURATION = '2m';

export const options = {
  scenarios: {
    smoke: {
      executor: 'constant-vus',
      vus: 1,
      duration: '30s',
      exec: 'searchBooks',
      tags: {test_type: 'smoke'},
      gracefulStop: '5s',
    },
    load: {
      executor: 'constant-arrival-rate',
      rate: TARGET_RPS,
      timeUnit: '1s',
      duration: DURATION,
      preAllocatedVUs: 50,
      maxVUs: 500,
      exec: 'searchBooks',
      tags: {test_type: 'load'},
      gracefulStop: '10s',
    },
    ramp: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 500,
      exec: 'searchBooks',
      tags: {test_type: 'ramp'},
      gracefulStop: '10s',
      stages: [
        {duration: '1m', target: TARGET_RPS},
        {duration: '3m', target: TARGET_RPS},
        {duration: '1m', target: TARGET_RPS * 1.5},
        {duration: '2m', target: TARGET_RPS * 1.5},
        {duration: '1m', target: TARGET_RPS * 2},
        {duration: '1m', target: TARGET_RPS * 2},
        {duration: '1m', target: 10},
      ],
    },
    stress: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 20,
      maxVUs: 1000,
      exec: 'searchBooks',
      tags: {test_type: 'stress'},
      gracefulStop: '15s',
      stages: [
        {duration: '1m', target: TARGET_RPS},
        {duration: '2m', target: TARGET_RPS * 2},
        {duration: '2m', target: TARGET_RPS * 4},
        {duration: '2m', target: TARGET_RPS * 6},
        {duration: '1m', target: TARGET_RPS * 8},
        {duration: '2m', target: 10},
      ],
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<50', 'p(99)<100'],

    'http_req_failed': ['rate<0.01']
  },
};

// Only run the scenario specified by SCENARIO env variable
if (SCENARIO !== 'all') {
  for (const key of Object.keys(options.scenarios)) {
    if (key !== SCENARIO) {
      delete options.scenarios[key];
    }
  }
}

export function setup() {
  console.log(`Starting performance test with scenario: ${SCENARIO}`);
  console.log(`Target RPS: ${TARGET_RPS}`);
  console.log(`Base URL: ${BASE_URL}`);
  console.log(`Duration: ${DURATION}`);

  return {startTime: new Date()};
}

export function searchBooks() {
  const response = http.get(`${BASE_URL}/hello`, {
    headers: {
      'Accept': 'application/json',
    },
    tags: {
      endpoint: 'search-books',
    },
  });

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
    'response time < 1000ms': (r) => r.timings.duration < 1000,
    'content-type is JSON': (r) => r.headers['Content-Type']?.includes('application/json'),
    'response body is valid': (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body);
      } catch (e) {
        return false;
      }
    },
  });
}