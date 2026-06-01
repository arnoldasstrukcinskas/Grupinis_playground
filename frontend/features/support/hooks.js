import { After, AfterAll, Before, BeforeAll, setDefaultTimeout } from '@cucumber/cucumber';
import { chromium } from 'playwright';
import { createServer } from 'vite';

setDefaultTimeout(30_000);

let browser;
let viteServer;
let baseUrl;

BeforeAll(async function () {
  viteServer = await createServer({
    server: {
      host: '127.0.0.1',
      port: 5173,
      strictPort: false,
    },
    logLevel: 'error',
  });

  await viteServer.listen();
  baseUrl = viteServer.resolvedUrls.local[0].replace(/\/$/, '');
  browser = await chromium.launch({ headless: true });
});

Before(async function () {
  this.baseUrl = baseUrl;
  this.context = await browser.newContext();
  this.page = await this.context.newPage();
});

After(async function () {
  await this.context?.close();
});

AfterAll(async function () {
  await browser?.close();
  await viteServer?.close();
});
