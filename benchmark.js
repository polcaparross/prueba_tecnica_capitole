import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// Custom metrics
const creationDuration = new Trend('creation_duration');
const queryDuration = new Trend('query_duration');
const historyDuration = new Trend('history_duration');
const errorRate = new Rate('errors');
const successCounter = new Counter('success_requests');

export const options = {
  scenarios: {
    // 1. Constant load for price queries
    price_queries: {
      executor: 'constant-arrival-rate',
      rate: 100,
      timeUnit: '1s',
      duration: '1m',
      preAllocatedVUs: 20,
      maxVUs: 100,
      exec: 'priceQuery',
    },
    // 2. Product creation
    product_creation: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      stages: [
        { target: 50, duration: '30s' },
        { target: 50, duration: '30s' },
        { target: 0, duration: '30s' },
      ],
      preAllocatedVUs: 10,
      maxVUs: 50,
      exec: 'createProduct',
    },
    // 3. Stress test for history
    history_stress: {
      executor: 'constant-vus',
      vus: 10,
      duration: '1m',
      exec: 'getHistory',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<200'],
  },
};

const BASE_URL = 'http://product-api:8080';

export function setup() {
  const payload = JSON.stringify({
    name: 'Benchmark Product',
    description: 'Initial product for benchmarking'
  });
  const params = { headers: { 'Content-Type': 'application/json' } };
  const res = http.post(`${BASE_URL}/products`, payload, params);
  
  if (res.status !== 201) {
    throw new Error(`Failed to create initial product: ${res.status} ${res.body}`);
  }
  
  const productId = res.json().id;
  
  const prices = [
    { value: 10.00, initDate: '2023-01-01', endDate: '2023-12-31' },
    { value: 15.00, initDate: '2024-01-01', endDate: '2024-06-30' },
    { value: 20.00, initDate: '2024-07-01', endDate: null },
  ];
  
  for (const price of prices) {
    http.post(`${BASE_URL}/products/${productId}/prices`, JSON.stringify(price), params);
  }
  
  return { productId };
}

export function createProduct() {
  const payload = JSON.stringify({
    name: `Product ${__ITER}`,
    description: `Auto-generated product during benchmark iteration ${__ITER}`
  });
  const params = { headers: { 'Content-Type': 'application/json' } };
  
  const res = http.post(`${BASE_URL}/products`, payload, params);
  
  const success = check(res, {
    'status is 201': (r) => r.status === 201,
  });
  
  if (success) {
    creationDuration.add(res.timings.duration);
    successCounter.add(1);
  } else {
    errorRate.add(1);
  }
}

export function priceQuery(data) {
  const dates = ['2023-05-15', '2024-03-10', '2025-01-01'];
  const randomDate = dates[Math.floor(Math.random() * dates.length)];
  
  const res = http.get(`${BASE_URL}/products/${data.productId}/prices?date=${randomDate}`);
  
  const success = check(res, {
    'status is 200': (r) => r.status === 200,
  });
  
  if (success) {
    queryDuration.add(res.timings.duration);
    successCounter.add(1);
  } else {
    errorRate.add(1);
  }
}

export function getHistory(data) {
  const res = http.get(`${BASE_URL}/products/${data.productId}/prices`);
  
  const success = check(res, {
    'status is 200': (r) => r.status === 200,
  });
  
  if (success) {
    historyDuration.add(res.timings.duration);
    successCounter.add(1);
  } else {
    errorRate.add(1);
  }
}
