// src/Pages/Chat/Chat.jsx
import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { analyze, saveAnalysis, clearAnalysis, getLocations, logout, isLoggedIn } from '../../services/api';
import s from './Chat.module.css';

export default function Chat() {
    const navigate = useNavigate();

    // Redirect if not logged in
    useEffect(() => { if (!isLoggedIn()) navigate('/login'); }, []);

    const [messages, setMessages] = useState([]);        // { role, text }
    const [input, setInput] = useState('');
    const [hobbies, setHobbies] = useState('');
    const [location, setLocation] = useState('');
    const [locationId, setLocationId] = useState(null);
    const [locations, setLocations] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [saveMsg, setSaveMsg] = useState('');
    const [error, setError] = useState('');
    const bottomRef = useRef(null);

    useEffect(() => {
        bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    // Search locations as user types
    useEffect(() => {
        if (location.length < 2) { setLocations([]); return; }
        const t = setTimeout(async () => {
            try {
                const res = await getLocations(location);
                setLocations(res || []);
            } catch { setLocations([]); }
        }, 400);
        return () => clearTimeout(t);
    }, [location]);

    const addMsg = (role, text) =>
        setMessages(prev => [...prev, { role, text, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }]);

    const handleSend = async () => {
        const prompt = input.trim();
        if (!prompt) { setError('Please enter a message.'); return; }
        setError('');
        setInput('');
        addMsg('user', prompt);
        setLoading(true);

        try {
            const res = await analyze({
                userPrompt: prompt,
                userHobbies: hobbies,
                locationId: locationId || 0,
            });
            // res = { id, analysis, hotels:[...] }
            addMsg('ai', res.analysis || JSON.stringify(res));
        } catch (err) {
            addMsg('ai', `⚠ ${err.message}`);
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async () => {
        setSaving(true); setSaveMsg('');
        try {
            await saveAnalysis();
            setSaveMsg('Saved to history!');
            setTimeout(() => setSaveMsg(''), 3000);
        } catch (err) {
            setSaveMsg('Save failed: ' + err.message);
        } finally {
            setSaving(false);
        }
    };

    const handleClear = async () => {
        setMessages([]);
        try { await clearAnalysis(); } catch { }
    };

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <div className={s.app}>
            {/* SIDEBAR */}
            <aside className={s.sidebar}>
                <div className={s.sideTop}>
                    <div className={s.logo}>
                        <div className={s.logoIcon}>
                            <svg viewBox="0 0 24 24" fill="white" width="16" height="16">
                                <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm-8 2.75c1.24 0 2.25 1.01 2.25 2.25S13.24 11.25 12 11.25 9.75 10.24 9.75 9 10.76 6.75 12 6.75zM17 17H7v-.75c0-1.67 3.33-2.5 5-2.5s5 .83 5 2.5V17z" />
                            </svg>
                        </div>
                        <span className={s.logoText}>Concierge AI</span>
                    </div>

                    {/* Location search */}
                    <div className={s.sideSection}>
                        <p className={s.sideLabel}>Location</p>
                        <div className={s.locationWrap}>
                            <input
                                className={s.sideInput}
                                type="text" value={location}
                                onChange={e => { setLocation(e.target.value); setLocationId(null); }}
                                placeholder="Search city…"
                            />
                            {locations.length > 0 && (
                                <ul className={s.locList}>
                                    {locations.map(loc => (
                                        <li key={loc.id || loc.dest_id || loc.name}
                                            onClick={() => {
                                                setLocation(loc.name || loc.label || loc.city_name || '');
                                                setLocationId(loc.id || loc.dest_id);
                                                setLocations([]);
                                            }}>
                                            {loc.name || loc.label || loc.city_name}
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </div>
                    </div>

                    {/* Hobbies */}
                    <div className={s.sideSection}>
                        <p className={s.sideLabel}>Your hobbies / interests</p>
                        <textarea
                            className={s.sideTextarea}
                            value={hobbies}
                            onChange={e => setHobbies(e.target.value)}
                            placeholder="e.g. hiking, beaches, museums…"
                            rows={3}
                        />
                    </div>

                    {/* Actions */}
                    <button className={s.btnGreen} onClick={handleSave} disabled={saving || messages.length === 0}>
                        {saving ? <span className={s.spinner} /> : null}
                        {saving ? 'Saving…' : 'Save to history'}
                    </button>
                    {saveMsg && <p className={s.saveMsg}>{saveMsg}</p>}

                    <button className={s.btnBlue} onClick={() => navigate('/history')}>
                        View history
                    </button>

                    <button className={s.btnRed} onClick={handleClear} disabled={messages.length === 0}>
                        Clear chat
                    </button>
                </div>

                <button className={s.btnLogout} onClick={handleLogout}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="14" height="14">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                        <polyline points="16 17 21 12 16 7" />
                        <line x1="21" y1="12" x2="9" y2="12" />
                    </svg>
                    Log out
                </button>
            </aside>

            {/* MAIN */}
            <main className={s.main}>
                <div className={s.topbar}>
                    <div>
                        <span className={s.topTitle}>Hotel Consultation</span>
                        <span className={s.topBadge}>AI online</span>
                    </div>
                </div>

                {/* Messages */}
                <div className={s.messages}>
                    {messages.length === 0 && (
                        <div className={s.welcome}>
                            <p className={s.welcomeTitle}>How can I help you today?</p>
                            <p className={s.welcomeSub}>
                                Set a location and your interests in the sidebar, then ask anything about hotels.
                            </p>
                            <div className={s.chips}>
                                {['Best hotels in Santorini', 'Family stays in Tokyo', 'Budget options in Rome', 'Hidden gems in Portugal'].map(c => (
                                    <button key={c} className={s.chip} onClick={() => setInput(c)}>{c}</button>
                                ))}
                            </div>
                        </div>
                    )}

                    {messages.map((m, i) => (
                        <div key={i} className={`${s.msg} ${m.role === 'user' ? s.user : s.ai}`}>
                            <div className={s.avatar}>{m.role === 'user' ? 'U' : 'AI'}</div>
                            <div>
                                <div className={s.bubble}>{m.text}</div>
                                <div className={s.time}>{m.time}</div>
                            </div>
                        </div>
                    ))}

                    {loading && (
                        <div className={`${s.msg} ${s.ai}`}>
                            <div className={s.avatar}>AI</div>
                            <div className={s.bubble}>
                                <div className={s.typing}><span /><span /><span /></div>
                            </div>
                        </div>
                    )}
                    <div ref={bottomRef} />
                </div>

                {/* Input */}
                <div className={s.inputBar}>
                    {error && <p className={s.errorMsg}>{error}</p>}
                    <div className={s.inputRow}>
                        <textarea
                            className={s.textarea}
                            value={input}
                            onChange={e => setInput(e.target.value)}
                            onKeyDown={e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend(); } }}
                            placeholder="Ask about hotels, destinations, comparisons…"
                            rows={1}
                            disabled={loading}
                        />
                        <button className={s.sendBtn} onClick={handleSend} disabled={loading || !input.trim()}>
                            {loading
                                ? <span className={s.spinner} />
                                : <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" width="18" height="18"><line x1="22" y1="2" x2="11" y2="13" /><polygon points="22 2 15 22 11 13 2 9 22 2" /></svg>
                            }
                        </button>
                    </div>
                    <p className={s.hint}>Enter to send · Shift+Enter for new line</p>
                </div>
            </main>
        </div>
    );
}