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
                    aria-label="Destination"
                    placeholder="Search destination…"
                    autoComplete="off"
                    disabled={confirmed}
                />
                {confirmed && (
                    <button className={s.btnLocReset} onClick={onConfirm} title="Change destination" aria-label="Change destination">
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
                    aria-label="Confirm destination"
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

/* ── Hotel Detail Modal ── */
function HotelModal({ hotel, onClose }) {
    const h = hotel?.content ?? hotel;
    // Upgrade Booking.com image to highest resolution
    const upgradePhoto = (url) => {
        if (!url) return null;
        // Replace any size suffix with max resolution
        return url
            .replace(/square\d+/, 'max1280x900')
            .replace(/max\d+x\d+/, 'max1280x900')
            .replace(/\/\/[a-z]+\.bstatic\.com/, '//cf.bstatic.com');
    };
    const photo = upgradePhoto(h.maxPhotoUrl) || upgradePhoto(h.photoUrl1440) || upgradePhoto(h.mainPhotoUrl);
    const scoreColor = h.reviewScoreNumber >= 9 ? '#1B5E20' : h.reviewScoreNumber >= 8 ? '#2E7D32' : '#1565C0';

    // Close on backdrop click
    const handleBackdrop = (e) => { if (e.target === e.currentTarget) onClose(); };

    return (
        <div className={s.modalBg} onClick={handleBackdrop}>
            <div className={s.modalBox}>
                <button className={s.modalClose} onClick={onClose}>✕</button>

                {photo && (
                    <div className={s.modalPhoto}>
                        <img src={photo} alt={h.hotelName}
                            onError={e => e.target.parentElement.style.display = 'none'} />
                        {h.reviewScoreNumber > 0 && (
                            <div className={s.modalScore} style={{ background: scoreColor }}>
                                <span className={s.scoreNum}>{h.reviewScoreNumber.toFixed(1)}</span>
                                <span className={s.scoreWord}>
                                    {h.reviewScoreNumber >= 9 ? 'Exceptional' : h.reviewScoreNumber >= 8 ? 'Excellent' : h.reviewScoreNumber >= 7 ? 'Good' : 'Fair'}
                                </span>
                            </div>
                        )}
                    </div>
                )}

                <div className={s.modalBody}>
                    {/* Header */}
                    <div className={s.modalHeader}>
                        <div>
                            <h2 className={s.modalTitle}>{h.hotelName}</h2>
                            <div className={s.modalMeta}>
                                {h.accomodationType && <span className={s.accomType}>{h.accomodationType}</span>}
                                {h.hotelStars > 0 && <span className={s.starStr}>{'★'.repeat(Math.min(h.hotelStars, 5))}</span>}
                                {h.isBeachFront && <span className={s.beachTag}>🏖 Beachfront</span>}
                            </div>
                        </div>
                        {(h.price || h.priceAllInclusive) && (
                            <div className={s.modalPrice}>
                                {h.price && <><span className={s.price}>{h.price}</span><span className={s.perNight}>/night</span></>}
                                {h.priceAllInclusive && <div className={s.priceAI}>All‑incl: {h.priceAllInclusive}</div>}
                            </div>
                        )}
                    </div>

                    {/* Info grid */}
                    <div className={s.modalGrid}>
                        {/* Location */}
                        {(h.district || h.distanceToCenter || h.address) && (
                            <div className={s.modalSection}>
                                <p className={s.modalSectionTitle}>📍 Location</p>
                                {h.district && <p className={s.modalSectionText}>{h.district}</p>}
                                {h.distanceToCenter && <p className={s.modalSectionText}>{h.distanceToCenter} from city center</p>}
                                {h.address && <p className={s.modalSectionText}>{h.address}</p>}
                            </div>
                        )}

                        {/* Reviews */}
                        {h.reviewScoreNumber > 0 && (
                            <div className={s.modalSection}>
                                <p className={s.modalSectionTitle}>💬 Guest Reviews</p>
                                <p className={s.modalSectionText}><strong>{h.reviewScoreNumber.toFixed(1)}/10</strong> — {h.reviewScoreWord}</p>
                                {h.reviewNumber > 0 && <p className={s.modalSectionText}>{h.reviewNumber.toLocaleString()} verified reviews</p>}
                            </div>
                        )}

                        {/* Amenities */}
                        {h.additionals && (
                            <div className={s.modalSection}>
                                <p className={s.modalSectionTitle}>✨ Amenities & Features</p>
                                <p className={s.modalSectionText}>{h.additionals}</p>
                            </div>
                        )}

                        {/* Pricing */}
                        {(h.price || h.priceAllInclusive) && (
                            <div className={s.modalSection}>
                                <p className={s.modalSectionTitle}>💰 Pricing</p>
                                {h.price && <p className={s.modalSectionText}>Standard: <strong>{h.price}</strong>/night</p>}
                                {h.priceAllInclusive && <p className={s.modalSectionText}>All-inclusive: <strong>{h.priceAllInclusive}</strong>/night</p>}
                            </div>
                        )}
                    </div>

                    {/* Book button */}
                    {h.hotelUrl && (
                        <a href={h.hotelUrl} target="_blank" rel="noreferrer" className={s.modalBookBtn}>
                            View & Book on Booking.com →
                        </a>
                    )}
                </div>
            </div>
        </div>
    );
}

/* ── Real hotel card with photo from API ── */
function RealHotelCard({ hotel }) {
    const [showModal, setShowModal] = useState(false);
    const h = hotel?.content ?? hotel;
    // Use highest quality photo available
    // Upgrade Booking.com image to highest resolution
    const upgradePhoto = (url) => {
        if (!url) return null;
        // Replace any size suffix with max resolution
        return url
            .replace(/square\d+/, 'max1280x900')
            .replace(/max\d+x\d+/, 'max1280x900')
            .replace(/\/\/[a-z]+\.bstatic\.com/, '//cf.bstatic.com');
    };
    const photo = upgradePhoto(h.maxPhotoUrl) || upgradePhoto(h.photoUrl1440) || upgradePhoto(h.mainPhotoUrl);
    // Star icons
    const stars = h.hotelStars > 0 ? '★'.repeat(Math.min(h.hotelStars, 5)) : null;
    // Score color
    const scoreColor = h.reviewScoreNumber >= 9 ? '#1B5E20' : h.reviewScoreNumber >= 8 ? '#2E7D32' : h.reviewScoreNumber >= 7 ? '#1565C0' : '#555';

    return (
        <>
            <div className={s.hotelCard} onClick={() => setShowModal(true)} style={{ cursor: 'pointer' }}>
                {/* High quality photo */}
                {photo && (
                    <div className={s.hotelPhoto}>
                        <img src={photo} alt={h.hotelName}
                            onError={e => e.target.parentElement.style.display = 'none'} />
                        {h.reviewScoreNumber > 0 && (
                            <div className={s.photoScore} style={{ background: scoreColor }}>
                                <span className={s.scoreNum}>{h.reviewScoreNumber.toFixed(1)}</span>
                                <span className={s.scoreWord}>
                                    {h.reviewScoreNumber >= 9 ? 'Exceptional' : h.reviewScoreNumber >= 8 ? 'Excellent' : h.reviewScoreNumber >= 7 ? 'Good' : 'Fair'}
                                </span>
                            </div>
                        )}
                        {h.isBeachFront && <div className={s.beachBadge}>🏖 Beachfront</div>}
                    </div>
                )}

                <div className={s.hotelCardBody}>
                    {/* Name + stars */}
                    <div className={s.hotelCardTop}>
                        <div>
                            <p className={s.hotelName}>{h.hotelName}</p>
                            <div className={s.hotelMeta2}>
                                {h.accomodationType && <span className={s.accomType}>{h.accomodationType}</span>}
                                {stars && <span className={s.starStr}>{stars}</span>}
                            </div>
                        </div>
                    </div>

                    {/* Location */}
                    {(h.district || h.distanceToCenter || h.address) && (
                        <div className={s.infoRow}>
                            <span className={s.infoIcon}>📍</span>
                            <span className={s.infoText}>
                                {[h.district, h.distanceToCenter, h.address].filter(Boolean).join(' · ')}
                            </span>
                        </div>
                    )}

                    {/* Reviews */}
                    {h.reviewNumber > 0 && (
                        <div className={s.infoRow}>
                            <span className={s.infoIcon}>💬</span>
                            <span className={s.infoText}>
                                {h.reviewNumber.toLocaleString()} reviews · {h.reviewScoreWord}
                            </span>
                        </div>
                    )}

                    {/* Features / amenities */}
                    {h.additionals && (
                        <div className={s.infoRow}>
                            <span className={s.infoIcon}>✨</span>
                            <span className={s.infoText}>{h.additionals}</span>
                        </div>
                    )}

                    {/* Price */}
                    {(h.price || h.priceAllInclusive) && (
                        <div className={s.priceBlock}>
                            {h.price && (
                                <div className={s.priceMain}>
                                    <span className={s.hotelPrice}>{h.price}</span>
                                    <span className={s.perNight}>/night</span>
                                </div>
                            )}
                            {h.priceAllInclusive && (
                                <div className={s.priceAIRow}>All‑incl: <strong>{h.priceAllInclusive}</strong>/night</div>
                            )}
                        </div>
                    )}

                    {/* Book button */}
                    {h.hotelUrl && (
                        <a href={h.hotelUrl} target="_blank" rel="noreferrer" className={s.bookBtnFull}
                            onClick={e => e.stopPropagation()}>
                            View & Book on Booking.com →
                        </a>
                    )}
                </div>
            </div>
            {showModal && <HotelModal hotel={hotel} onClose={() => setShowModal(false)} />}
        </>
    );
}

function AIMessage({ text, hotels, onSaveAll, saved }) {
    const parsedHotels = parseHotels(text);
    const hasParsedHotels = parsedHotels && parsedHotels.length >= 2;

    // Match only the hotels the AI actually recommended
    const getMatchedHotels = () => {
        if (!hotels || hotels.length === 0) return [];
        const mentionedNames = [];
        const namePattern = /[0-9]+\.\s+\*\*([^*(\n]+)/g;
        let m;
        while ((m = namePattern.exec(text)) !== null) {
            mentionedNames.push(m[1].trim().toLowerCase());
        }
        if (mentionedNames.length === 0) return hotels.slice(0, 5);
        const matched = mentionedNames
            .map(name => hotels.find(h => {
                const hn = (h?.content?.hotelName ?? h?.hotelName ?? '').toLowerCase();
                return hn.includes(name.slice(0, 8)) || name.includes(hn.slice(0, 8));
            }))
            .filter(Boolean);
        return matched.length >= 2 ? matched.slice(0, 5) : hotels.slice(0, 5);
    };

    const matchedHotels = getMatchedHotels();
    const hasRealHotels = matchedHotels.length > 0;

    // Extract only the intro text before the numbered list
    const getCleanText = (t) => {
        // Find where numbered list starts
        const lines = t.split('\n');
        const introLines = [];
        for (const line of lines) {
            if (/^\d+\.\s/.test(line)) break; // stop at first numbered item
            introLines.push(line);
        }
        return introLines.join('\n')
            .replace(/Based on the provided HOTEL_LIST[^.\n]*/gi, '')
            .replace(/Please note that[^.\n]*/gi, '')
            .replace(/These recommendations are based[^.\n]*/gi, '')
            .replace(/\*\*Top \d+ Hotel Recommendations:\*\*/gi, '')
            .replace(/\*\*Comparison of[^\n]*/gi, '')
            .replace(/\n{2,}/g, '\n')
            .trim();
    };
    const cleanText = getCleanText(text);

    if (hasRealHotels) {
        return (
            <div>
                {/* AI analysis text as paragraph */}
                {cleanText && <div className={s.aiText}>{cleanText}</div>}
                {/* Real hotel cards with best quality photos */}
                <div className={s.hotelGrid}>
                    {matchedHotels.map((h, i) => <RealHotelCard key={i} hotel={h} />)}
                </div>
                <button className={saved ? s.btnSavedAll : s.btnSaveAll} onClick={onSaveAll} disabled={saved}>
                    {saved ? '✓ Analysis saved' : '💾 Save this analysis'}
                </button>
            </div>
        );
    }
    if (hasParsedHotels) {
        return (
            <div>
                {cleanText && <div className={s.aiText}>{cleanText}</div>}
                <div className={s.hotelGrid}>
                    {parsedHotels.map((h, i) => <HotelCard key={i} hotel={h} />)}
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
    const [loadedHotels, setLoadedHotels] = useState([]); // hotels from POST /hotels
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

    const addMsg = (role, text, hotels = []) =>
        setMessages(prev => [...prev, {
            role, text, hotels,
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
            let loadResult = [];
            loadResult = await loadHotels({
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
            // Store real hotels — handle array or HATEOAS wrapper
            console.log('Hotels response:', loadResult);
            let hotelsList = [];
            if (Array.isArray(loadResult)) {
                hotelsList = loadResult;
            } else if (loadResult?._embedded) {
                hotelsList = Object.values(loadResult._embedded).flat();
            } else if (loadResult?.hotels) {
                hotelsList = loadResult.hotels;
            }
            console.log('Parsed hotels count:', hotelsList.length);
            if (hotelsList.length > 0) {
                setLoadedHotels(hotelsList);
            }
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
            // Use hotels from response; fall back to stored loadedHotels
            const resHotels = res.hotels && res.hotels.length > 0 ? res.hotels : null;
            addMsg('ai', res.analysis || JSON.stringify(res), resHotels || loadedHotels);
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
        setMessages([]); setSavedMessages([]); setHotelsLoaded(false); setLoadedHotels([]);
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
                                                        aria-label="Check-in date"
                                                        value={checkinDate} min={today}
                                                        onChange={e => setCheckinDate(e.target.value)} />
                                                </div>
                                                <div className={s.dateArrow}>→</div>
                                                <div className={s.dateField}>
                                                    <p className={s.dateLabel}>Check-out</p>
                                                    <input className={s.dateInput} type="date"
                                                        aria-label="Check-out date"
                                                        value={checkoutDate} min={checkinDate || today}
                                                        onChange={e => setCheckoutDate(e.target.value)} />
                                                </div>
                                                <div className={s.dateField}>
                                                    <p className={s.dateLabel}>Adults</p>
                                                    <input className={s.dateInput} type="number"
                                                        aria-label="Adults"
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
                                                aria-label="Interests"
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
                                            hotels={m.hotels || []}
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
                                aria-label="Travel request"
                                onChange={e => setInput(e.target.value)}
                                onKeyDown={e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend(); } }}
                                placeholder="Ask for recommendations, comparisons, or anything about these hotels…"
                                rows={1} disabled={loading} />
                            <button className={s.sendBtn} onClick={handleSend} disabled={loading || !input.trim()} aria-label="Send request">
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
