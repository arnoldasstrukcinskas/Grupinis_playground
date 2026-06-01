import { After, AfterAll, Before, BeforeAll, setDefaultTimeout } from '@cucumber/cucumber';
import { chromium } from 'playwright';
import { createServer } from 'vite';

setDefaultTimeout(30_000);

let browser;
let viteServer;
let baseUrl;

/**
 * Starts one Vite dev server and one Chromium browser for the whole frontend Cucumber run.
 */
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

/**
 * Creates a fresh browser context and page for each scenario so tests do not share localStorage or cookies.
 */
Before(async function () {
  this.baseUrl = baseUrl;
  this.context = await browser.newContext();
  this.page = await this.context.newPage();
});

/**
 * Closes the scenario browser context after each scenario.
 */
After(async function () {
  await this.context?.close();
});

/**
 * Shuts down shared Playwright and Vite resources after all frontend scenarios finish.
 */
AfterAll(async function () {
  await browser?.close();
  await viteServer?.close();
});
