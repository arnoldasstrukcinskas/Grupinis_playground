import assert from 'node:assert/strict';

import { Given, Then, When } from '@cucumber/cucumber';

const API_BASE = 'http://localhost:8085';

const sampleHotels = [
  {
    id: 101,
    hotelName: 'Catalonia Sagrada Familia',
    accomodationType: 'Hotel',
    hotelStars: 3,
    district: 'Sant Marti',
    distanceToCenter: '2.6 km',
    address: 'Arago 577-579',
    price: '481.20',
    priceAllInclusive: '481.20',
    reviewScoreNumber: 8.4,
    reviewScoreWord: 'Very good',
    reviewNumber: 1000,
    additionals: 'Close to Sagrada Familia',
    mainPhotoUrl: 'https://example.com/hotel.jpg',
  },
  {
    id: 102,
    hotelName: 'Catalonia La Boqueria',
    accomodationType: 'Hotel',
    hotelStars: 4,
    district: 'Ciutat Vella',
    distanceToCenter: '0.7 km',
    address: 'Hospital, 26',
    price: '505.44',
    priceAllInclusive: '505.44',
    reviewScoreNumber: 8.5,
    reviewScoreWord: 'Very good',
    reviewNumber: 900,
    additionals: 'Central location',
    mainPhotoUrl: 'https://example.com/hotel-2.jpg',
  },
];

const sampleAnalysis = `Based on the provided hotel list:

1. **Catalonia Sagrada Familia**
   * Score: 8.4
   * Great for museums, architecture, and comfortable city access.
2. **Catalonia La Boqueria**
   * Score: 8.5
   * Central location with restaurants and cafes nearby.`;

/**
 * Builds CORS headers used by mocked backend responses in browser tests.
 *
 * @param {string} contentType response content type
 * @returns {Record<string, string>} headers accepted by browser fetch calls
 */
function corsHeaders(contentType = 'application/json') {
  return {
    'access-control-allow-origin': '*',
    'access-control-allow-methods': 'GET,POST,DELETE,OPTIONS',
    'access-control-allow-headers': 'authorization,content-type',
    'content-type': contentType,
  };
}

/**
 * Completes a Playwright route with a JSON response body.
 *
 * @param {import('playwright').Route} route intercepted browser request
 * @param {unknown} body response body to serialize as JSON
 * @param {number} status HTTP status code to return
 * @returns {Promise<void>} resolves after the route is fulfilled
 */
async function fulfillJson(route, body, status = 200) {
  await route.fulfill({
    status,
    headers: corsHeaders(),
    body: JSON.stringify(body),
  });
}

/**
 * Completes a Playwright route with a plain text response body.
 *
 * @param {import('playwright').Route} route intercepted browser request
 * @param {string} body response body text
 * @param {number} status HTTP status code to return
 * @returns {Promise<void>} resolves after the route is fulfilled
 */
async function fulfillText(route, body, status = 200) {
  await route.fulfill({
    status,
    headers: corsHeaders('text/plain'),
    body,
  });
}

/**
 * Registers mocked API responses for the backend endpoints used by frontend scenarios.
 */
Given('the frontend backend API is mocked', async function () {
  await this.page.route(`${API_BASE}/**`, async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const method = request.method();
    const path = url.pathname;

    if (method === 'OPTIONS') {
      await route.fulfill({ status: 204, headers: corsHeaders() });
      return;
    }

    if (method === 'POST' && path === '/auth/login') {
      await fulfillText(route, 'mock.header.payload');
      return;
    }

    if (method === 'POST' && path === '/auth/register') {
      await fulfillText(route, 'User registered: frontend-user');
      return;
    }

    if (method === 'GET' && path === '/hotels') {
      await fulfillJson(route, [
        {
          dest_id: -372490,
          name: 'Barcelona',
          country: 'Spain',
          dest_type: 'city',
        },
      ]);
      return;
    }

    if (method === 'POST' && path === '/hotels') {
      await fulfillJson(route, sampleHotels);
      return;
    }

    if (method === 'POST' && path === '/analysis/analyze') {
      await fulfillJson(route, {
        id: 0,
        analysis: sampleAnalysis,
        hotels: sampleHotels,
      });
      return;
    }

    if (method === 'POST' && path === '/analysis') {
      await fulfillText(route, 'Analysis with id: 42, saved');
      return;
    }

    if (method === 'GET' && path === '/analysis/analyses') {
      await fulfillJson(route, [{ id: 42, analysis: sampleAnalysis }]);
      return;
    }

    if (method === 'GET' && path === '/analysis/42') {
      await fulfillJson(route, { id: 42, analysis: sampleAnalysis });
      return;
    }

    if (method === 'GET' && path === '/analysis/42/hotels') {
      await fulfillJson(route, { _links: {} });
      return;
    }

    if (method === 'DELETE' && path.startsWith('/analysis/')) {
      await fulfillText(route, '42');
      return;
    }

    await fulfillText(route, `No mock configured for ${method} ${path}`, 404);
  });
});

/**
 * Seeds localStorage with a JWT-shaped token before opening protected frontend pages.
 */
Given('I am authenticated in the frontend', async function () {
  await this.context.addInitScript(() => {
    window.localStorage.setItem('token', 'mock.header.payload');
  });
});

/**
 * Opens the protected chat route without authentication to verify redirect behavior.
 */
When('I open the chat page without a token', async function () {
  await this.page.goto(`${this.baseUrl}/chat`);
});

/**
 * Opens the login route in the test browser.
 */
When('I open the login page', async function () {
  await this.page.goto(`${this.baseUrl}/login`);
});

/**
 * Opens the chat route in the test browser.
 */
When('I open the chat page', async function () {
  await this.page.goto(`${this.baseUrl}/chat`);
});

/**
 * Opens the saved analysis history route in the test browser.
 */
When('I open the history page', async function () {
  await this.page.goto(`${this.baseUrl}/history`);
});

/**
 * Submits the login form without filling credentials.
 */
When('I submit the login form without a username', async function () {
  await this.page.getByRole('button', { name: /^Sign in$/ }).click();
});

/**
 * Fills and submits the frontend login form.
 *
 * @param {string} username username entered in the login form
 * @param {string} password password entered in the login form
 */
When('I login through the frontend as {string} with password {string}', async function (username, password) {
  await this.page.getByLabel('Username').fill(username);
  await this.page.getByLabel('Password').fill(password);
  await this.page.getByRole('button', { name: /^Sign in$/ }).click();
});

/**
 * Selects a destination from the mocked location autocomplete.
 *
 * @param {string} destination visible destination name to select
 */
When('I choose destination {string}', async function (destination) {
  await this.page.getByRole('textbox', { name: 'Destination' }).fill(destination);
  await this.page.locator('li').filter({ hasText: destination }).first().click();
  await this.page.getByLabel('Confirm destination').click();
});

/**
 * Enters check-in and check-out dates in the chat search form.
 *
 * @param {string} checkIn check-in date in ISO format
 * @param {string} checkOut check-out date in ISO format
 */
When('I enter stay dates {string} to {string}', async function (checkIn, checkOut) {
  await this.page.getByLabel('Check-in date').fill(checkIn);
  await this.page.getByLabel('Check-out date').fill(checkOut);
});

/**
 * Submits the frontend hotel search form.
 */
When('I search hotels from the frontend', async function () {
  await this.page.getByRole('button', { name: /Search hotels/i }).click();
});

/**
 * Sends a travel request through the chat input.
 *
 * @param {string} request user request shown in the chat scenario
 */
When('I ask the frontend for recommendations {string}', async function (request) {
  await this.page.getByLabel('Travel request').fill(request);
  await this.page.getByLabel('Send request').click();
});

/**
 * Clicks the save button for the displayed AI analysis.
 */
When('I save the frontend analysis', async function () {
  await this.page.getByRole('button', { name: /Save this analysis/i }).click();
});

/**
 * Asserts that the current frontend URL is the login page.
 */
Then('I should be on the login page', async function () {
  await this.page.waitForURL('**/login');
  assert.equal(new URL(this.page.url()).pathname, '/login');
});

/**
 * Asserts that the current frontend URL is the chat page.
 */
Then('I should be on the chat page', async function () {
  await this.page.waitForURL('**/chat');
  assert.equal(new URL(this.page.url()).pathname, '/chat');
});

/**
 * Waits until expected text is visible on the page.
 *
 * @param {string} text text expected somewhere in the rendered page
 */
Then('I should see {string}', async function (text) {
  await this.page.getByText(text, { exact: false }).first().waitFor();
});
