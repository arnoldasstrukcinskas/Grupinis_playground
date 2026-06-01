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

function corsHeaders(contentType = 'application/json') {
  return {
    'access-control-allow-origin': '*',
    'access-control-allow-methods': 'GET,POST,DELETE,OPTIONS',
    'access-control-allow-headers': 'authorization,content-type',
    'content-type': contentType,
  };
}

async function fulfillJson(route, body, status = 200) {
  await route.fulfill({
    status,
    headers: corsHeaders(),
    body: JSON.stringify(body),
  });
}

async function fulfillText(route, body, status = 200) {
  await route.fulfill({
    status,
    headers: corsHeaders('text/plain'),
    body,
  });
}

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

Given('I am authenticated in the frontend', async function () {
  await this.context.addInitScript(() => {
    window.localStorage.setItem('token', 'mock.header.payload');
  });
});

When('I open the chat page without a token', async function () {
  await this.page.goto(`${this.baseUrl}/chat`);
});

When('I open the login page', async function () {
  await this.page.goto(`${this.baseUrl}/login`);
});

When('I open the chat page', async function () {
  await this.page.goto(`${this.baseUrl}/chat`);
});

When('I open the history page', async function () {
  await this.page.goto(`${this.baseUrl}/history`);
});

When('I submit the login form without a username', async function () {
  await this.page.getByRole('button', { name: /^Sign in$/ }).click();
});

When('I login through the frontend as {string} with password {string}', async function (username, password) {
  await this.page.getByLabel('Username').fill(username);
  await this.page.getByLabel('Password').fill(password);
  await this.page.getByRole('button', { name: /^Sign in$/ }).click();
});

When('I choose destination {string}', async function (destination) {
  await this.page.getByRole('textbox', { name: 'Destination' }).fill(destination);
  await this.page.locator('li').filter({ hasText: destination }).first().click();
  await this.page.getByLabel('Confirm destination').click();
});

When('I enter stay dates {string} to {string}', async function (checkIn, checkOut) {
  await this.page.getByLabel('Check-in date').fill(checkIn);
  await this.page.getByLabel('Check-out date').fill(checkOut);
});

When('I search hotels from the frontend', async function () {
  await this.page.getByRole('button', { name: /Search hotels/i }).click();
});

When('I ask the frontend for recommendations {string}', async function (request) {
  await this.page.getByLabel('Travel request').fill(request);
  await this.page.getByLabel('Send request').click();
});

When('I save the frontend analysis', async function () {
  await this.page.getByRole('button', { name: /Save this analysis/i }).click();
});

Then('I should be on the login page', async function () {
  await this.page.waitForURL('**/login');
  assert.equal(new URL(this.page.url()).pathname, '/login');
});

Then('I should be on the chat page', async function () {
  await this.page.waitForURL('**/chat');
  assert.equal(new URL(this.page.url()).pathname, '/chat');
});

Then('I should see {string}', async function (text) {
  await this.page.getByText(text, { exact: false }).first().waitFor();
});
