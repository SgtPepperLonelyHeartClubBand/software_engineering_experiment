#!/usr/bin/env node

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';
const TOTAL = Number(process.env.TOTAL || 100);
const CONCURRENCY = Number(process.env.CONCURRENCY || 10);
const DEV_SELLER_ID = process.env.DEV_SELLER_ID || '1';

function percentile(values, p) {
  if (!values.length) return 0;
  const sorted = [...values].sort((a, b) => a - b);
  const index = Math.min(sorted.length - 1, Math.ceil((p / 100) * sorted.length) - 1);
  return sorted[index];
}

async function request(path, options = {}) {
  const headers = {
    Accept: 'application/json',
    ...(options.headers || {})
  };
  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }
  const response = await fetch(`${BASE_URL}${path}`, {
    method: options.method || 'GET',
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body)
  });
  let payload = null;
  try {
    payload = await response.json();
  } catch {
    throw new Error(`HTTP ${response.status}: response is not JSON`);
  }
  return { httpStatus: response.status, payload };
}

async function runPool(total, concurrency, task) {
  let next = 0;
  const results = [];

  async function worker() {
    while (next < total) {
      const current = next++;
      const started = performance.now();
      try {
        const result = await task(current);
        results.push({
          ok: result.httpStatus >= 200 && result.httpStatus < 300 && result.payload?.code === 0,
          code: result.payload?.code,
          ms: performance.now() - started
        });
      } catch (error) {
        results.push({
          ok: false,
          code: 'ERR',
          error: error.message,
          ms: performance.now() - started
        });
      }
    }
  }

  await Promise.all(Array.from({ length: concurrency }, worker));
  return results;
}

function printSummary(name, results) {
  const durations = results.map(result => result.ms);
  const okCount = results.filter(result => result.ok).length;
  const failed = results.length - okCount;
  const avg = durations.reduce((sum, value) => sum + value, 0) / Math.max(1, durations.length);
  const codes = results.reduce((acc, result) => {
    const key = String(result.code);
    acc[key] = (acc[key] || 0) + 1;
    return acc;
  }, {});

  console.log(`\n[${name}]`);
  console.log(`total=${results.length}, success=${okCount}, failed=${failed}, successRate=${((okCount / results.length) * 100).toFixed(2)}%`);
  console.log(`avg=${avg.toFixed(1)}ms, p95=${percentile(durations, 95).toFixed(1)}ms, max=${Math.max(...durations).toFixed(1)}ms`);
  console.log(`codes=${JSON.stringify(codes)}`);
}

async function createItem(title) {
  const result = await request('/api/items', {
    method: 'POST',
    headers: { 'X-Dev-User-Id': DEV_SELLER_ID },
    body: {
      title,
      category: '专业书籍',
      condition: '9成新',
      price: 25,
      locationCode: 'JLH-MY-01',
      description: 'QA性能压测自动生成商品',
      imageUrls: ['/uploads/qa-load-test.png']
    }
  });
  if (result.payload?.code !== 0) {
    throw new Error(`create item failed: ${JSON.stringify(result.payload)}`);
  }
  return result.payload.data.id;
}

async function loginNewBuyer(index, seed) {
  const studentId = String(230000000 + seed * 100 + index);
  await request('/api/auth/send-code', {
    method: 'POST',
    body: { studentId }
  });
  const loginResult = await request('/api/auth/login', {
    method: 'POST',
    body: { studentId, verifyCode: '123456' }
  });
  if (loginResult.payload?.code !== 0) {
    throw new Error(`login buyer failed: ${JSON.stringify(loginResult.payload)}`);
  }
  return loginResult.payload.data.userId;
}

async function main() {
  console.log(`QA load test target: ${BASE_URL}`);
  console.log(`TOTAL=${TOTAL}, CONCURRENCY=${CONCURRENCY}`);
  console.log('Note: this script expects the backend dev auth header to be enabled.');

  const health = await request('/api/health');
  if (health.payload?.code !== 0) {
    throw new Error(`backend health check failed: ${JSON.stringify(health.payload)}`);
  }

  const detailItemId = await createItem(`QA压测详情商品-${Date.now()}`);

  const listResults = await runPool(TOTAL, CONCURRENCY, () =>
    request('/api/items', { headers: { 'X-Dev-User-Id': DEV_SELLER_ID } })
  );
  printSummary('商品列表接口 GET /api/items', listResults);

  const searchResults = await runPool(TOTAL, CONCURRENCY, () =>
    request('/api/items?keyword=QA', { headers: { 'X-Dev-User-Id': DEV_SELLER_ID } })
  );
  printSummary('商品搜索接口 GET /api/items?keyword=QA', searchResults);

  const detailResults = await runPool(TOTAL, CONCURRENCY, () =>
    request(`/api/items/${detailItemId}`, { headers: { 'X-Dev-User-Id': DEV_SELLER_ID } })
  );
  printSummary('商品详情接口 GET /api/items/{id}', detailResults);

  const reserveItemId = await createItem(`QA并发预定压测商品-${Date.now()}`);
  const seed = Date.now() % 100000;
  const buyerIds = [];
  for (let i = 0; i < CONCURRENCY; i++) {
    buyerIds.push(await loginNewBuyer(i, seed));
  }

  const reserveResults = await Promise.all(buyerIds.map(async buyerId => {
    const started = performance.now();
    try {
      const result = await request(`/api/items/${reserveItemId}/reserve`, {
        method: 'POST',
        headers: { 'X-Dev-User-Id': String(buyerId) }
      });
      return {
        ok: result.httpStatus >= 200 && result.httpStatus < 300 && result.payload?.code === 0,
        code: result.payload?.code,
        ms: performance.now() - started
      };
    } catch (error) {
      return {
        ok: false,
        code: 'ERR',
        error: error.message,
        ms: performance.now() - started
      };
    }
  }));
  printSummary('并发预定接口 POST /api/items/{id}/reserve', reserveResults);
  console.log('Expected reserve result: exactly one success and the remaining requests return business code 400.');
}

main().catch(error => {
  console.error(`QA load test failed: ${error.message}`);
  process.exitCode = 1;
});
