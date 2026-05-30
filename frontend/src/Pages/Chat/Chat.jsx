// src/Pages/Chat/Chat.jsx
import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { analyze, loadHotels, saveAnalysis, clearAnalysis, clearHotels, getLocations, logout, isLoggedIn } from '../../services/api';
import s from './Chat.module.css';

/* ── Location Combobox with confirm button ── */
function LocationCombobox({ value, onChange, onSelect, onConfirm, confirmed, loading }) {
    const [open, setOpen] = useState(false);
    const [options, setOptions] = useState([]);
    const ref = useRef(null);

    useEffect(() => {
        const h = e => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
        document.addEventListener('mousedown', h);
        return () => document.removeEventListener('mousedown', h);
    }, []);

    useEffect(() => {
        if (value.length < 2) { setOptions([]); return; }
        const t = setTimeout(async () => {
            try { const res = await getLocations(value); setOptions(res || []); setOpen(true); }
            catch { setOptions([]); }
        }, 350);
        return () => clearTimeout(t);
    }, [value]);

    return (
        <div className={s.locRow} ref={ref}>
            <div className={s.locInputWrap}>
                <input
                    className={`${s.locInput} ${confirmed ? s.locConfirmed : ''}`}
                    value={value}
                    onChange={e => { onChange(e.target.value); }}
                    placeholder="Search destination…"
                    autoComplete="off"
                    disabled={confirmed}
                />
                {confirmed && (
                    <button className={s.btnLocReset} onClick={onConfirm} title="Change destination">
                        ✕
                    </button>
                )}
                {open && options.length > 0 && !confirmed && (
                    <ul className={s.locList}>
                        {options.map(loc => (
                            <li key={loc.dest_id || loc.destinationId || loc.name}
                                onMouseDown={() => {
                                    onChange(loc.name || loc.destinationName || '');
                                    onSelect(loc);
                                    setOpen(false);
                                }}>
                                <span className={s.locName}>{loc.name || loc.destinationName}</span>
                                {loc.country && <span className={s.locCountry}>{loc.country}</span>}
                            </li>
                        ))}
                    </ul>
                )}
            </div>
            {!confirmed && (
                <button
                    className={s.btnConfirmLoc}
                    onClick={onConfirm}
                    disabled={loading || !value.trim()}
                    title="Confirm destination"
                >
                    {loading ? <span className={s.spinnerSm} /> : '✓'}
                </button>
            )}
        </div>
    );
}

/* ── Parse AI text into hotel cards ── */
function parseHotels(text) {
    const blocks = text.split(/\n(?=\d+\.\s)/);
    if (blocks.length < 2) return null;
    return blocks.filter(b => b.trim()).map((block, i) => {
        const nameMatch = block.match(/\*\*([^*]+)\*\*/);
        const name = nameMatch ? nameMatch[1].replace(/\s*\(.*?\)/, '').trim() : `Hotel ${i + 1}`;
        const ratingMatch = block.match(/(?:Score|Rating)[:\s]+([0-9.]+)\s*\/?\s*([0-9]+)?/i);
        const rating = ratingMatch ? parseFloat(ratingMatch[1]) : null;
        const ratingMax = ratingMatch?.[2] ? parseInt(ratingMatch[2]) : 10;
        const lines = block.split('\n')
            .map(l => l.replace(/^\s*[\*\+\-]\s*/, '').replace(/\*\*/g, '').trim())
            .filter(l => l && !l.match(/^\d+\.\s/) && l.length > 5);
        // Skip intro/junk blocks that have no rating and generic name
        if (!rating && name.match(/^Hotel \d+$/)) return null;
        return { name, rating, ratingMax, lines };
    }).filter(Boolean);
}

function Stars({ rating, max = 10 }) {
    const pct = Math.round((rating / max) * 5);
    return (
        <div className={s.stars}>
            {[1, 2, 3, 4, 5].map(i => (
                <span key={i} className={i <= pct ? s.starFilled : s.starEmpty}>★</span>
            ))}
            <span className={s.starNum}>{rating}/{max}</span>
        </div>
    );
}

function HotelCard({ hotel }) {
    return (
        <div className={s.hotelCard}>
            <p className={s.hotelName}>{hotel.name}</p>
            {hotel.rating && <Stars rating={hotel.rating} max={hotel.ratingMax} />}
            <ul className={s.hotelLines}>
                {hotel.lines.slice(0, 5).map((l, i) => <li key={i}>{l}</li>)}
            </ul>
        </div>
    );
}

function AIMessage({ text, onSaveAll, saved }) {
    const hotels = parseHotels(text);
    if (hotels && hotels.length >= 2) {
        return (
            <div>
                <div className={s.hotelGrid}>
                    {hotels.map((h, i) => (
                        <HotelCard key={i} hotel={h} />
                    ))}
                </div>
                <button className={saved ? s.btnSavedAll : s.btnSaveAll} onClick={onSaveAll} disabled={saved}>
                    {saved ? '✓ Analysis saved' : '💾 Save this analysis'}
                </button>
            </div>
        );
    }
    return <div className={s.bubble} style={{ whiteSpace: 'pre-wrap' }}>{text}</div>;
}

/* ── Main ── */
export default function Chat() {
    const navigate = useNavigate();
    useEffect(() => { if (!isLoggedIn()) navigate('/login'); }, []);

    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const [hotelsLoaded, setHotelsLoaded] = useState(false);
    const [loadingHotels, setLoadingHotels] = useState(false);
    const [saveMsg, setSaveMsg] = useState('');
    const [error, setError] = useState('');
    const [savedMessages, setSavedMessages] = useState([]);
    const bottomRef = useRef(null);

    // Step 1: destination
    const [locationText, setLocationText] = useState('');
    const [locationObj, setLocationObj] = useState(null);
    const [locConfirmed, setLocConfirmed] = useState(false);

    // Step 2: dates (shown after destination confirmed)
    const [checkinDate, setCheckinDate] = useState('');
    const [checkoutDate, setCheckoutDate] = useState('');
    const [adultsNumber, setAdultsNumber] = useState('2');
    const [hobbies, setHobbies] = useState('');

    useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

    const addMsg = (role, text) =>
        setMessages(prev => [...prev, {
            role, text,
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        }]);

    // Confirm destination → load hotels
    const handleConfirmLocation = async () => {
        if (locConfirmed) {
            // Reset
            setLocConfirmed(false); setLocationObj(null); setLocationText('');
            setHotelsLoaded(false); setCheckinDate(''); setCheckoutDate('');
            return;
        }
        if (!locationObj) { setError('Please select a destination from the list.'); return; }
        setLocConfirmed(true); setError('');
    };

    // Load hotels when dates are set and user clicks send
    const handleLoadHotels = async () => {
        if (!checkinDate || !checkoutDate) { setError('Please set check-in and check-out dates.'); return; }
        if (new Date(checkoutDate) <= new Date(checkinDate)) { setError('Check-out must be after check-in.'); return; }
        setError(''); setLoadingHotels(true);
        try {
            await loadHotels({
                destinationId: locationObj.destinationId || locationObj.dest_id,
                destinationType: locationObj.destinationType || locationObj.dest_type || 'city',
                checkInDate: checkinDate,
                checkOutDate: checkoutDate,
                roomNumber: 1,
                adultsNumber: parseInt(adultsNumber),
                filterByCurrency: 'EUR',
                orderBy: 'popularity',
                units: 'metric',
                hobbiesAndInterests: hobbies,
            });
            setHotelsLoaded(true);
            addMsg('ai', `Hotels loaded for **${locationText}** (${checkinDate} → ${checkoutDate}). Ask me for recommendations!`);
        } catch (err) {
            setError('Failed to load hotels: ' + err.message);
        } finally { setLoadingHotels(false); }
    };

    const handleSend = async () => {
        const prompt = input.trim();
        if (!prompt) { setError('Please enter a message.'); return; }
        setError(''); setInput('');
        addMsg('user', prompt);
        setLoading(true);
        try {
            const res = await analyze({ userPrompt: prompt, userHobbies: hobbies });
            addMsg('ai', res.analysis || JSON.stringify(res));
        } catch (err) {
            addMsg('ai', `⚠ ${err.message}`);
        } finally { setLoading(false); }
    };

    const handleSaveAll = async (msgIndex) => {
        if (savedMessages.includes(msgIndex)) return;
        setSavedMessages(prev => [...prev, msgIndex]);
        try {
            await saveAnalysis();
            setSaveMsg('Analysis saved!');
            setTimeout(() => setSaveMsg(''), 3000);
        } catch (err) {
            setSavedMessages(prev => prev.filter(i => i !== msgIndex));
            setSaveMsg('Save failed: ' + err.message);
        }
    };

    const handleClear = async () => {
        setMessages([]); setSavedMessages([]); setHotelsLoaded(false);
        setLocConfirmed(false); setLocationObj(null); setLocationText('');
        setCheckinDate(''); setCheckoutDate('');
        try { await clearAnalysis(); await clearHotels(); } catch { }
    };

    const handleLogout = async () => { await logout(); navigate('/login'); };

    // Min date = today
    const today = new Date().toISOString().split('T')[0];

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
                    {saveMsg && <p className={s.saveMsg}>{saveMsg}</p>}
                    {hotelsLoaded && <p className={s.hotelsStatus}>✓ Hotels loaded</p>}
                    <button className={s.btnBlue} onClick={() => navigate('/history')}>View history</button>
                    <button className={s.btnRed} onClick={handleClear} disabled={messages.length === 0 && !hotelsLoaded}>Clear</button>
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
                    <span className={s.topTitle}>Hotel Consultation</span>
                    <span className={s.topBadge}>AI online</span>
                </div>

                <div className={s.messages}>

                    {/* ── STEP 1+2 FORM ── */}
                    {!hotelsLoaded && messages.length === 0 && (
                        <div className={s.welcome}>
                            <p className={s.welcomeTitle}>Find your perfect hotel</p>
                            <p className={s.welcomeSub}>Search a destination, set your dates, then ask the AI.</p>

                            <div className={s.searchFlow}>

                                {/* Step 1: Destination */}
                                <div className={s.stepRow}>
                                    <div className={s.stepNum}>1</div>
                                    <div className={s.stepContent}>
                                        <p className={s.stepLabel}>Where are you going?</p>
                                        <LocationCombobox
                                            value={locationText}
                                            onChange={setLocationText}
                                            onSelect={loc => setLocationObj(loc)}
                                            onConfirm={handleConfirmLocation}
                                            confirmed={locConfirmed}
                                            loading={loadingHotels}
                                        />
                                    </div>
                                </div>

                                {/* Step 2: Dates — shown after destination confirmed */}
                                {locConfirmed && (
                                    <div className={s.stepRow}>
                                        <div className={s.stepNum}>2</div>
                                        <div className={s.stepContent}>
                                            <p className={s.stepLabel}>When are you staying?</p>
                                            <div className={s.datesRow}>
                                                <div className={s.dateField}>
                                                    <p className={s.dateLabel}>Check-in</p>
                                                    <input className={s.dateInput} type="date"
                                                        value={checkinDate} min={today}
                                                        onChange={e => setCheckinDate(e.target.value)} />
                                                </div>
                                                <div className={s.dateArrow}>→</div>
                                                <div className={s.dateField}>
                                                    <p className={s.dateLabel}>Check-out</p>
                                                    <input className={s.dateInput} type="date"
                                                        value={checkoutDate} min={checkinDate || today}
                                                        onChange={e => setCheckoutDate(e.target.value)} />
                                                </div>
                                                <div className={s.dateField}>
                                                    <p className={s.dateLabel}>Adults</p>
                                                    <input className={s.dateInput} type="number"
                                                        min="1" max="30" value={adultsNumber}
                                                        onChange={e => setAdultsNumber(e.target.value)}
                                                        style={{ width: '70px' }} />
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                )}

                                {/* Step 3: Interests — shown after dates */}
                                {locConfirmed && checkinDate && checkoutDate && (
                                    <div className={s.stepRow}>
                                        <div className={s.stepNum}>3</div>
                                        <div className={s.stepContent}>
                                            <p className={s.stepLabel}>Any interests or preferences? <span className={s.optional}>(optional)</span></p>
                                            <input className={s.interestsInput} type="text"
                                                value={hobbies} onChange={e => setHobbies(e.target.value)}
                                                placeholder="e.g. museums, beach, spa, family-friendly…" />
                                        </div>
                                    </div>
                                )}

                                {/* Load button */}
                                {locConfirmed && checkinDate && checkoutDate && (
                                    <button className={s.btnSearch} onClick={handleLoadHotels} disabled={loadingHotels}>
                                        {loadingHotels
                                            ? <><span className={s.spinner} /> Searching hotels…</>
                                            : <>🔍 Search hotels</>
                                        }
                                    </button>
                                )}

                                {error && <p className={s.formError}>{error}</p>}
                            </div>
                        </div>
                    )}

                    {messages.map((m, i) => (
                        <div key={i} className={`${s.msg} ${m.role === 'user' ? s.user : s.ai}`}>
                            {m.role === 'user' ? (
                                <>
                                    <div className={s.avatar}>U</div>
                                    <div>
                                        <div className={s.bubble}>{m.text}</div>
                                        <div className={s.time}>{m.time}</div>
                                    </div>
                                </>
                            ) : (
                                <>
                                    <div className={s.avatar}>AI</div>
                                    <div style={{ flex: 1 }}>
                                        <AIMessage
                                            text={m.text}
                                            onSaveAll={() => handleSaveAll(i)}
                                            saved={savedMessages.includes(i)}
                                        />
                                        <div className={s.time}>{m.time}</div>
                                    </div>
                                </>
                            )}
                        </div>
                    ))}

                    {loading && (
                        <div className={`${s.msg} ${s.ai}`}>
                            <div className={s.avatar}>AI</div>
                            <div className={s.bubble}><div className={s.typing}><span /><span /><span /></div></div>
                        </div>
                    )}
                    <div ref={bottomRef} />
                </div>

                {/* INPUT BAR */}
                {hotelsLoaded && (
                    <div className={s.inputBar}>
                        {error && <p className={s.errorMsg}>{error}</p>}
                        <div className={s.inputRow}>
                            <textarea className={s.textarea} value={input}
                                onChange={e => setInput(e.target.value)}
                                onKeyDown={e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend(); } }}
                                placeholder="Ask for recommendations, comparisons, or anything about these hotels…"
                                rows={1} disabled={loading} />
                            <button className={s.sendBtn} onClick={handleSend} disabled={loading || !input.trim()}>
                                {loading
                                    ? <span className={s.spinner} />
                                    : <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" width="18" height="18"><line x1="22" y1="2" x2="11" y2="13" /><polygon points="22 2 15 22 11 13 2 9 22 2" /></svg>
                                }
                            </button>
                        </div>
                        <p className={s.hint}>Enter to send · Shift+Enter for new line</p>
                    </div>
                )}
            </main>
        </div>
    );
}