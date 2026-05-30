// src/services/api.js
// All backend API calls. Base URL points to Spring Boot on port 8080.

const BASE = 'http://localhost:8085';

/* ── Token helpers ── */
export const getToken = () => localStorage.getItem('token');
export const setToken = (token) => localStorage.setItem('token', token);
export const clearToken = () => localStorage.removeItem('token');
export const isLoggedIn = () => !!getToken();

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getToken()}`,
    };
}

async function handleResponse(res) {
    const text = await res.text();
    if (!res.ok) throw new Error(text || `Error ${res.status}`);
    try { return JSON.parse(text); } catch { return text; }
}

/* ── AUTH ── */

// POST /auth/register  { username, password, email }
export async function register({ username, password, email }) {
    const res = await fetch(`${BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, email }),
    });
    return handleResponse(res);
}

// POST /auth/login  { username, password }  → returns JWT token string
export async function login({ username, password }) {
    const res = await fetch(`${BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
    });
    if (!res.ok) {
        const text = await res.text();
        throw new Error(text || 'Login failed');
    }
    // Backend returns the token as plain text in the response body
    // OR as "User logged in" — check auth service: it prints token to console
    // and returns "User logged in". We generate it on our side from the header.
    // The token is returned in the response body as plain string.
    const token = await res.text();
    return token;
}

// POST /auth/logout?token=...
export async function logout() {
    const token = getToken();
    if (!token) return;
    await fetch(`${BASE}/auth/logout?token=${encodeURIComponent(token)}`, {
        method: 'POST',
        headers: authHeaders(),
    });
    clearToken();
}

/* ── ANALYSIS (Chat) ── */

// POST /analysis/analyze  { userPrompt, userHobbies, locationId }
export async function analyze({ userPrompt, userHobbies, locationId }) {
    const res = await fetch(`${BASE}/analysis/analyze`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ userPrompt, userHobbies, locationId }),
    });
    return handleResponse(res); // returns Analysis object { id, analysis, hotels }
}

// POST /analysis  — save last analysis to DB
export async function saveAnalysis() {
    const res = await fetch(`${BASE}/analysis`, {
        method: 'POST',
        headers: authHeaders(),
    });
    return handleResponse(res);
}

// GET /analysis/analyses  — get all user analyses
export async function getAllAnalyses() {
    const res = await fetch(`${BASE}/analysis/analyses`, {
        headers: authHeaders(),
    });
    return handleResponse(res);
}

// GET /analysis/{id}
export async function getAnalysisById(id) {
    const res = await fetch(`${BASE}/analysis/${id}`, {
        headers: authHeaders(),
    });
    return handleResponse(res);
}

// GET /analysis/{id}/hotels
export async function getAnalysisHotels(id) {
    const res = await fetch(`${BASE}/analysis/${id}/hotels`, {
        headers: authHeaders(),
    });
    return handleResponse(res);
}

// DELETE /analysis/{id}
export async function deleteAnalysis(id) {
    const res = await fetch(`${BASE}/analysis/${id}`, {
        method: 'DELETE',
        headers: authHeaders(),
    });
    return handleResponse(res);
}

// DELETE /analysis/clearAnalysis
export async function clearAnalysis() {
    const res = await fetch(`${BASE}/analysis/clearAnalysis`, {
        method: 'DELETE',
        headers: authHeaders(),
    });
    return handleResponse(res);
}

/* ── HOTELS ── */

// GET /hotels?location=...  → List<LocationDto>
export async function getLocations(location) {
    const res = await fetch(`${BASE}/hotels?location=${encodeURIComponent(location)}`, {
        headers: authHeaders(),
    });
    return handleResponse(res);
}

// POST /hotels  { locationId, ... }  → List<HotelDto>
export async function getHotels(requestDto) {
    const res = await fetch(`${BASE}/hotels`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify(requestDto),
    });
    return handleResponse(res);
}