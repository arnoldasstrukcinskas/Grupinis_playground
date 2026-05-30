// src/services/api.js
const BASE = 'http://localhost:8085';

export const getToken = () => localStorage.getItem('token');
export const setToken = (t) => localStorage.setItem('token', t);
export const clearToken = () => localStorage.removeItem('token');
export const isLoggedIn = () => !!getToken();

function authHeaders() {
    return { 'Content-Type': 'application/json', 'Authorization': `Bearer ${getToken()}` };
}

async function handleResponse(res) {
    const text = await res.text();
    if (!res.ok) throw new Error(text || `Error ${res.status}`);
    try { return JSON.parse(text); } catch { return text; }
}

/* AUTH */
export async function register({ username, password, email }) {
    const res = await fetch(`${BASE}/auth/register`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, email }),
    });
    return handleResponse(res);
}

export async function login({ username, password }) {
    const res = await fetch(`${BASE}/auth/login`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
    });
    if (!res.ok) { const t = await res.text(); throw new Error(t || 'Login failed'); }
    return res.text();
}

export async function logout() {
    const token = getToken();
    if (!token) return;
    await fetch(`${BASE}/auth/logout?token=${encodeURIComponent(token)}`, {
        method: 'POST', headers: authHeaders(),
    });
    clearToken();
}

/* HOTELS */
// GET /hotels?location=...  returns LocationDto[]
// LocationDto has: destinationId (from dest_id), destinationName (from name), country, destinationType (from dest_type)
export async function getLocations(location) {
    const res = await fetch(`${BASE}/hotels?location=${encodeURIComponent(location)}`, {
        headers: authHeaders(),
    });
    return handleResponse(res);
}

// POST /hotels — field names must match HotelRequestDto exactly
export async function loadHotels(dto) {
    const res = await fetch(`${BASE}/hotels`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({
            destinationId: dto.destinationId,
            destinationType: dto.destinationType || 'city',
            checkInDate: dto.checkInDate,
            checkOutDate: dto.checkOutDate,
            roomNumber: dto.roomNumber || 1,
            adultsNumber: dto.adultsNumber || 2,
            filterByCurrency: dto.filterByCurrency || 'EUR',
            orderBy: dto.orderBy || 'popularity',
            units: dto.units || 'metric',
            locale: 'en-gb',
            includeAdjency: false,
            hobbiesAndInterests: dto.hobbiesAndInterests || '',
            promptToOllama: dto.promptToOllama || '',
        }),
    });
    return handleResponse(res);
}

/* ANALYSIS */
export async function analyze({ userPrompt, userHobbies }) {
    const res = await fetch(`${BASE}/analysis/analyze`, {
        method: 'POST', headers: authHeaders(),
        body: JSON.stringify({ userPrompt, userHobbies }),
    });
    return handleResponse(res);
}

export async function saveAnalysis() {
    const res = await fetch(`${BASE}/analysis`, { method: 'POST', headers: authHeaders() });
    return handleResponse(res);
}

export async function getAllAnalyses() {
    const res = await fetch(`${BASE}/analysis/analyses`, { headers: authHeaders() });
    return handleResponse(res);
}

export async function getAnalysisById(id) {
    const res = await fetch(`${BASE}/analysis/${id}`, { headers: authHeaders() });
    return handleResponse(res);
}

export async function deleteAnalysis(id) {
    const res = await fetch(`${BASE}/analysis/${id}`, { method: 'DELETE', headers: authHeaders() });
    return handleResponse(res);
}

export async function clearAnalysis() {
    const res = await fetch(`${BASE}/analysis/clearAnalysis`, { method: 'DELETE', headers: authHeaders() });
    return handleResponse(res);
}

export async function clearHotels() {
    const res = await fetch(`${BASE}/analysis/clearHotels`, { method: 'DELETE', headers: authHeaders() });
    return handleResponse(res);
}

export async function saveHotel(hotelId) {
    const res = await fetch(`${BASE}/analysis/saveHotel/${hotelId}`, {
        method: 'POST', headers: authHeaders(),
    });
    return handleResponse(res);
}