#!/usr/bin/env node

const fs = require('fs');
const fsp = require('fs/promises');
const path = require('path');
const MarkdownIt = require('markdown-it');
const { chromium } = require('playwright');

const rootDir = path.resolve(__dirname, '..');
const docsDir = path.join(rootDir, 'docs');
const imagesDir = path.join(docsDir, 'images');
const reportPath = path.join(docsDir, 'BACKEND_B_FINAL_REPORT.md');
const printMdPath = path.join(docsDir, 'BACKEND_B_FINAL_REPORT_PRINT.md');
const printHtmlPath = path.join(docsDir, 'BACKEND_B_FINAL_REPORT_PRINT.html');
const pdfPath = path.join(docsDir, 'BACKEND_B_FINAL_REPORT.pdf');
const testLogPath = process.env.MVN_TEST_LOG || '/tmp/backend-b-mvn-test.log';
const apiBase = process.env.BACKEND_BASE_URL || 'http://localhost:8080';
const frontendBase = process.env.FRONTEND_BASE_URL || 'http://localhost:5173';
const mermaidBundlePath = process.env.MERMAID_BUNDLE
    || '/tmp/report-tools/node_modules/mermaid/dist/mermaid.min.js';
const chromiumPath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE
    || '/home/dreamtct/.cache/ms-playwright/chromium-1228/chrome-linux64/chrome';

const diagrams = [
    {
        title: '核心用例图',
        source: 'backend-b-use-case.mmd',
        image: 'backend-b-use-case.png',
    },
    {
        title: '交易场景图',
        source: 'backend-b-reserve-sequence.mmd',
        image: 'backend-b-reserve-sequence.png',
    },
    {
        title: '数据库关系图',
        source: 'backend-b-er.mmd',
        image: 'backend-b-er.png',
    },
    {
        title: '商品状态机',
        source: 'backend-b-item-state.mmd',
        image: 'backend-b-item-state.png',
    },
    {
        title: '订单状态机',
        source: 'backend-b-order-state.mmd',
        image: 'backend-b-order-state.png',
    },
    {
        title: '后端逻辑流程图',
        source: 'backend-b-reserve-flow.mmd',
        image: 'backend-b-reserve-flow.png',
    },
];

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function escapeScript(value) {
    return String(value).replace(/<\/script>/gi, '<\\/script>');
}

function fileUrl(value) {
    return `file://${value}`;
}

function assertToolingReady() {
    if (!fs.existsSync(mermaidBundlePath)) {
        throw new Error(`Mermaid bundle not found: ${mermaidBundlePath}`);
    }
    if (!fs.existsSync(reportPath)) {
        throw new Error(`Report not found: ${reportPath}`);
    }
}

function createBrowserOptions() {
    const options = {
        headless: true,
        args: ['--no-sandbox', '--disable-setuid-sandbox'],
    };

    if (fs.existsSync(chromiumPath)) {
        options.executablePath = chromiumPath;
    }

    return options;
}

async function renderMermaid(browser, diagram) {
    const sourcePath = path.join(imagesDir, diagram.source);
    const imagePath = path.join(imagesDir, diagram.image);
    const source = await fsp.readFile(sourcePath, 'utf8');
    const mermaidBundle = escapeScript(await fsp.readFile(mermaidBundlePath, 'utf8'));
    const page = await browser.newPage({
        viewport: { width: 1400, height: 1000 },
        deviceScaleFactor: 2,
    });

    const html = `
<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <style>
    body {
      margin: 0;
      background: #f6f7fb;
      font-family: "Noto Sans CJK SC", "Microsoft YaHei", Arial, sans-serif;
    }
    .diagram-frame {
      display: inline-block;
      min-width: 980px;
      max-width: 1280px;
      margin: 24px;
      padding: 28px 32px;
      background: #ffffff;
      border: 1px solid #dde3ef;
      border-radius: 12px;
      box-shadow: 0 14px 36px rgba(24, 39, 75, 0.10);
    }
    .diagram-title {
      margin: 0 0 18px;
      color: #14213d;
      font-size: 24px;
      font-weight: 700;
    }
    .mermaid {
      text-align: center;
    }
    .mermaid svg {
      max-width: 100%;
      height: auto;
    }
  </style>
</head>
<body>
  <section class="diagram-frame">
    <h1 class="diagram-title">${escapeHtml(diagram.title)}</h1>
    <pre class="mermaid">${escapeHtml(source)}</pre>
  </section>
  <script>${mermaidBundle}</script>
  <script>
    mermaid.initialize({
      startOnLoad: false,
      theme: 'default',
      securityLevel: 'loose',
      flowchart: { htmlLabels: true, useMaxWidth: true },
      sequence: { useMaxWidth: true, wrap: true },
      er: { useMaxWidth: true }
    });
    mermaid.run({ querySelector: '.mermaid' })
      .then(() => { window.renderStatus = 'done'; })
      .catch((error) => {
        window.renderStatus = 'failed';
        window.renderError = String(error && error.stack ? error.stack : error);
      });
  </script>
</body>
</html>`;

    await page.setContent(html, { waitUntil: 'load' });
    await page.waitForFunction(() => window.renderStatus === 'done' || window.renderStatus === 'failed', null, {
        timeout: 30000,
    });

    const status = await page.evaluate(() => window.renderStatus);
    if (status !== 'done') {
        const error = await page.evaluate(() => window.renderError);
        throw new Error(`Mermaid render failed for ${diagram.source}: ${error}`);
    }

    await page.locator('.diagram-frame').screenshot({ path: imagePath });
    await page.close();
    console.log(`rendered ${path.relative(rootDir, imagePath)}`);
}

async function renderTestScreenshot(browser) {
    if (!fs.existsSync(testLogPath)) {
        throw new Error(`Test log not found: ${testLogPath}. Run backend tests first.`);
    }

    const raw = await fsp.readFile(testLogPath, 'utf8');
    if (!raw.includes('BUILD SUCCESS')) {
        throw new Error(`Test log does not contain BUILD SUCCESS: ${testLogPath}`);
    }

    const lines = raw.trimEnd().split(/\r?\n/);
    const successIndex = Math.max(0, lines.findIndex((line) => line.includes('BUILD SUCCESS')));
    const start = Math.max(0, successIndex - 38);
    const end = Math.min(lines.length, successIndex + 8);
    const excerpt = lines.slice(start, end).join('\n');
    const page = await browser.newPage({
        viewport: { width: 1280, height: 760 },
        deviceScaleFactor: 2,
    });

    const html = `
<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <style>
    body {
      margin: 0;
      background: #101317;
      color: #e7edf3;
      font-family: "JetBrains Mono", Consolas, "SFMono-Regular", monospace;
    }
    .terminal {
      margin: 28px;
      border: 1px solid #2d3748;
      border-radius: 10px;
      overflow: hidden;
      box-shadow: 0 18px 44px rgba(0, 0, 0, 0.32);
      background: #0d1117;
    }
    .bar {
      display: flex;
      align-items: center;
      gap: 8px;
      height: 36px;
      padding: 0 14px;
      background: #1f2937;
      color: #aeb8c4;
      font-family: Arial, sans-serif;
      font-size: 14px;
    }
    .dot {
      width: 11px;
      height: 11px;
      border-radius: 50%;
      background: #34d399;
    }
    .dot:nth-child(1) { background: #f87171; }
    .dot:nth-child(2) { background: #fbbf24; }
    pre {
      margin: 0;
      padding: 22px 24px 28px;
      white-space: pre-wrap;
      word-break: break-word;
      font-size: 17px;
      line-height: 1.55;
    }
    .success {
      color: #7ee787;
      font-weight: 700;
    }
  </style>
</head>
<body>
  <section class="terminal">
    <div class="bar"><span class="dot"></span><span class="dot"></span><span class="dot"></span><span>backend ./mvnw test</span></div>
    <pre>${escapeHtml(excerpt).replace(/BUILD SUCCESS/g, '<span class="success">BUILD SUCCESS</span>')}</pre>
  </section>
</body>
</html>`;

    await page.setContent(html, { waitUntil: 'load' });
    await page.screenshot({
        path: path.join(imagesDir, 'backend-b-test-success.png'),
        fullPage: true,
    });
    await page.close();
    console.log('captured docs/images/backend-b-test-success.png');
}

async function api(pathname, options = {}) {
    const response = await fetch(`${apiBase}${pathname}`, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {}),
        },
    });
    const text = await response.text();
    let body;
    try {
        body = text ? JSON.parse(text) : null;
    } catch (error) {
        throw new Error(`Invalid JSON from ${pathname}: ${text}`);
    }

    if (!response.ok || (body && body.code !== 0)) {
        throw new Error(`API ${pathname} failed: HTTP ${response.status} ${text}`);
    }

    return body ? body.data : null;
}

async function login(studentId) {
    await api('/api/auth/send-code', {
        method: 'POST',
        body: JSON.stringify({ studentId }),
    });

    return api('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ studentId, verifyCode: '123456' }),
    });
}

async function prepareIntegrationData() {
    const stamp = new Date().toISOString().replace(/[-:.TZ]/g, '').slice(0, 14);
    const studentId = String(220000000 + (Date.now() % 10000000)).slice(0, 9);
    const buyer = await login(studentId);
    const token = buyer.token;
    const authHeaders = { Authorization: `Bearer ${token}` };

    await api('/api/users/me', {
        method: 'PUT',
        headers: authHeaders,
        body: JSON.stringify({
            nickname: '后端B联调买家',
            wechat: 'backend_b_buyer',
            locationCode: 'JLH-MY-03',
            avatarUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
        }),
    });

    const item = await api('/api/items', {
        method: 'POST',
        headers: { 'X-Dev-User-Id': '1' },
        body: JSON.stringify({
            title: `后端B报告联调商品${stamp}`,
            category: '教材',
            condition: '九成新',
            price: 18.50,
            locationCode: 'JLH-MY-01',
            description: '用于后端B最终报告截图，演示预定后系统通知到达卖家消息页。',
            imageUrls: ['/uploads/report-demo-item.png'],
        }),
    });

    const order = await api(`/api/items/${item.id}/reserve`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({}),
    });

    const detail = await api(`/api/items/${item.id}`, {
        method: 'GET',
        headers: authHeaders,
    });

    console.log(`prepared integration item=${item.id}, order=${order.id}, status=${detail.status}`);
    return { item, order, detail };
}

async function captureFrontendScreenshot(browser) {
    await prepareIntegrationData();

    const page = await browser.newPage({
        viewport: { width: 390, height: 844 },
        deviceScaleFactor: 2,
        isMobile: true,
    });

    await page.goto(frontendBase, { waitUntil: 'domcontentloaded' });
    await page.evaluate(() => {
        localStorage.setItem('market_dev_user_id', '1');
        localStorage.removeItem('token');
    });
    await page.goto(`${frontendBase}/messages`, { waitUntil: 'networkidle' });
    const noticeTab = page.getByText('系统通知', { exact: false }).first();
    await noticeTab.click({ timeout: 15000 });
    await page.waitForSelector('text=商品被预定', { timeout: 15000 });
    await page.screenshot({
        path: path.join(imagesDir, 'backend-b-frontend-notification.png'),
        fullPage: true,
    });
    await page.close();
    console.log('captured docs/images/backend-b-frontend-notification.png');
}

async function generatePrintableReport(browser) {
    const original = await fsp.readFile(reportPath, 'utf8');
    let index = 0;
    const withImages = original.replace(/```mermaid\n[\s\S]*?\n```/g, () => {
        const diagram = diagrams[index++];
        if (!diagram) {
            return '';
        }
        return `![${diagram.title}](images/${diagram.image})`;
    });

    const appendix = `

## 五、运行截图与证据

### 1. 后端自动化测试通过截图

![./mvnw test BUILD SUCCESS](images/backend-b-test-success.png)

### 2. 前后端联调截图

下图展示买家预定商品后，卖家消息页出现“商品被预定”系统通知，说明预定状态、会话与通知链路已经联通。

![商品预定后消息页通知](images/backend-b-frontend-notification.png)
`;

    await fsp.writeFile(printMdPath, withImages + appendix, 'utf8');

    const md = new MarkdownIt({
        html: true,
        linkify: true,
        typographer: true,
    });
    const body = md.render(withImages + appendix);
    const html = `
<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <base href="${fileUrl(docsDir)}/">
  <title>后端开发 B 最终报告</title>
  <style>
    @page {
      size: A4;
      margin: 16mm 14mm;
    }
    body {
      margin: 0;
      color: #1f2937;
      font-family: "Noto Sans CJK SC", "Microsoft YaHei", Arial, sans-serif;
      font-size: 12.5px;
      line-height: 1.62;
    }
    h1 {
      margin: 0 0 18px;
      color: #111827;
      font-size: 24px;
      line-height: 1.3;
      border-bottom: 2px solid #dbe4f0;
      padding-bottom: 10px;
    }
    h2 {
      margin: 26px 0 10px;
      color: #12305a;
      font-size: 18px;
      border-left: 4px solid #2563eb;
      padding-left: 10px;
      break-after: avoid;
    }
    h3 {
      margin: 18px 0 8px;
      color: #1f3a5f;
      font-size: 14.5px;
      break-after: avoid;
    }
    p, li {
      margin-top: 4px;
      margin-bottom: 4px;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin: 10px 0 14px;
      font-size: 11.5px;
    }
    th, td {
      border: 1px solid #d6dce8;
      padding: 6px 8px;
      vertical-align: top;
    }
    th {
      background: #eef4ff;
      color: #17345f;
    }
    code {
      padding: 1px 4px;
      border-radius: 4px;
      background: #f3f6fb;
      color: #0f3b6d;
      font-family: "JetBrains Mono", Consolas, monospace;
      font-size: 11.5px;
    }
    pre {
      padding: 10px 12px;
      background: #0f172a;
      color: #e5edf7;
      border-radius: 8px;
      overflow: hidden;
      font-size: 10.5px;
      line-height: 1.5;
      white-space: pre-wrap;
      word-break: break-word;
    }
    pre code {
      padding: 0;
      background: transparent;
      color: inherit;
    }
    img {
      display: block;
      max-width: 100%;
      margin: 10px auto 16px;
      border: 1px solid #d8e0ec;
      border-radius: 8px;
      box-shadow: 0 6px 18px rgba(17, 24, 39, 0.08);
    }
    a {
      color: #1d4ed8;
      text-decoration: none;
    }
  </style>
</head>
<body>
${body}
</body>
</html>`;

    await fsp.writeFile(printHtmlPath, html, 'utf8');

    const page = await browser.newPage({
        viewport: { width: 1280, height: 1800 },
    });
    await page.setContent(html, { waitUntil: 'load' });
    await page.pdf({
        path: pdfPath,
        format: 'A4',
        printBackground: true,
        margin: {
            top: '16mm',
            right: '14mm',
            bottom: '16mm',
            left: '14mm',
        },
    });
    await page.close();

    console.log(`generated ${path.relative(rootDir, printMdPath)}`);
    console.log(`generated ${path.relative(rootDir, printHtmlPath)}`);
    console.log(`generated ${path.relative(rootDir, pdfPath)}`);
}

async function main() {
    assertToolingReady();
    await fsp.mkdir(imagesDir, { recursive: true });

    const browser = await chromium.launch(createBrowserOptions());
    try {
        for (const diagram of diagrams) {
            await renderMermaid(browser, diagram);
        }
        await renderTestScreenshot(browser);
        await captureFrontendScreenshot(browser);
        await generatePrintableReport(browser);
    } finally {
        await browser.close();
    }
}

main().catch((error) => {
    console.error(error);
    process.exit(1);
});
