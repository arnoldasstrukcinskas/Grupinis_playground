// src/Pages/History/History.jsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllAnalyses, getAnalysisById, deleteAnalysis, isLoggedIn } from '../../services/api';
import s from './History.module.css';

/* ── Parse AI text into hotel cards (same as Chat) ── */
function parseHotels(text) {
    const blocks = text.split(/\n(?=\d+\.\s)/);
    if (blocks.length < 2) return null;
    return blocks.filter(b => b.trim()).map((block, i) => {
        const nameMatch = block.match(/\*\*([^*]+)\*\*/);
        const name = nameMatch ? nameMatch[1].replace(/\s*\(.*?\)/, '').trim() : null;
        if (!name) return null;
        const ratingMatch = block.match(/(?:Score|Rating)[:\s]+([0-9.]+)\s*\/?\s*([0-9]+)?/i);
        const rating = ratingMatch ? parseFloat(ratingMatch[1]) : null;
        const ratingMax = ratingMatch?.[2] ? parseInt(ratingMatch[2]) : 10;
        const lines = block.split('\n')
            .map(l => l.replace(/^\s*[\*\+\-]\s*/, '').replace(/\*\*/g, '').trim())
            .filter(l => l && !l.match(/^\d+\.\s/) && l.length > 5);
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

function HotelMiniCard({ hotel }) {
    return (
        <div className={s.hotelCard}>
            <p className={s.hotelName}>{hotel.name}</p>
            {hotel.rating && <Stars rating={hotel.rating} max={hotel.ratingMax} />}
            <ul className={s.hotelLines}>
                {hotel.lines.slice(0, 3).map((l, i) => <li key={i}>{l}</li>)}
            </ul>
        </div>
    );
}

export default function History() {
    const navigate = useNavigate();
    useEffect(() => { if (!isLoggedIn()) navigate('/login'); }, []);

    const [analyses, setAnalyses] = useState([]);
    const [selected, setSelected] = useState(null);
    const [loading, setLoading] = useState(true);
    const [deleting, setDeleting] = useState(null);
    const [error, setError] = useState('');

    useEffect(() => {
        (async () => {
            setLoading(true);
            try {
                const res = await getAllAnalyses();
                const items = res?._embedded
                    ? Object.values(res._embedded).flat()
                    : Array.isArray(res) ? res : [];
                setAnalyses(items.reverse()); // newest first
            } catch (err) {
                setError(err.message);
            } finally { setLoading(false); }
        })();
    }, []);

    const handleSelect = async (item) => {
        const id = item?.content?.id ?? item?.id;
        if (selected?.id === id) { setSelected(null); return; }
        try {
            const full = await getAnalysisById(id);
            const data = full?.content ?? full;
            setSelected({ id, ...data });
        } catch (err) { setError(err.message); }
    };

    const handleDelete = async (e, item) => {
        e.stopPropagation();
        const id = item?.content?.id ?? item?.id;
        if (!window.confirm('Delete this analysis?')) return;
        setDeleting(id);
        try {
            await deleteAnalysis(id);
            setAnalyses(prev => prev.filter(a => (a?.content?.id ?? a?.id) !== id));
            if (selected?.id === id) setSelected(null);
        } catch (err) { setError('Delete failed: ' + err.message); }
        finally { setDeleting(null); }
    };

    const getPreview = (item) => {
        const text = item?.content?.analysis ?? item?.analysis ?? '';
        // Try to get first hotel name
        const match = text.match(/\*\*([^*]+)\*\*/);
        return match ? match[1] : text.slice(0, 50) + '…';
    };

    const getDate = (item) => {
        const id = item?.content?.id ?? item?.id;
        return `Analysis #${id}`;
    };

    const hotels = selected?.analysis ? parseHotels(selected.analysis) : null;

    return (
        <div className={s.page}>
            {/* HEADER */}
            <header className={s.header}>
                <button className={s.backBtn} onClick={() => navigate('/chat')}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" width="16" height="16">
                        <polyline points="15 18 9 12 15 6" />
                    </svg>
                    Back to chat
                </button>
                <h1 className={s.title}>Saved Analyses</h1>
                <span className={s.count}>{analyses.length} saved</span>
            </header>

            {error && <p className={s.errorBanner}>{error}</p>}

            <div className={s.body}>
                {/* LIST */}
                <aside className={s.list}>
                    {loading && <p className={s.dim}>Loading…</p>}
                    {!loading && analyses.length === 0 && (
                        <div className={s.empty}>
                            <p>No saved analyses yet.</p>
                            <p>Go to the chat and save a consultation.</p>
                        </div>
                    )}
                    {analyses.map((item, i) => {
                        const id = item?.content?.id ?? item?.id;
                        const isActive = selected?.id === id;
                        return (
                            <div key={id ?? i}
                                className={`${s.listItem} ${isActive ? s.listItemActive : ''}`}
                                onClick={() => handleSelect(item)}>
                                <div className={s.listItemInner}>
                                    <p className={s.listItemTitle}>{getDate(item)}</p>
                                    <p className={s.listItemPreview}>{getPreview(item)}</p>
                                </div>
                                <button
                                    className={s.deleteBtn}
                                    onClick={e => handleDelete(e, item)}
                                    disabled={deleting === id}
                                    aria-label="Delete">
                                    {deleting === id
                                        ? <span className={s.spinner} />
                                        : <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" width="14" height="14">
                                            <polyline points="3 6 5 6 21 6" />
                                            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                                            <path d="M10 11v6M14 11v6" />
                                            <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                                        </svg>
                                    }
                                </button>
                            </div>
                        );
                    })}
                </aside>

                {/* DETAIL */}
                <section className={s.detail}>
                    {!selected && (
                        <div className={s.detailEmpty}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" width="40" height="40" style={{ color: 'var(--sky)', marginBottom: '1rem' }}>
                                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
                            </svg>
                            <p>Select an analysis to view hotels</p>
                        </div>
                    )}

                    {selected && (
                        <div className={s.detailContent}>
                            <div className={s.detailHead}>
                                <h2 className={s.detailTitle}>Analysis #{selected.id}</h2>
                                <button className={s.btnDelete}
                                    onClick={e => handleDelete(e, selected)}>
                                    Delete
                                </button>
                            </div>

                            {/* Hotel cards */}
                            {hotels && hotels.length > 0 ? (
                                <>
                                    <p className={s.sectionLabel}>Recommended Hotels</p>
                                    <div className={s.hotelGrid}>
                                        {hotels.map((h, i) => <HotelMiniCard key={i} hotel={h} />)}
                                    </div>
                                </>
                            ) : (
                                <>
                                    <p className={s.sectionLabel}>AI Analysis</p>
                                    <div className={s.analysisBox}>
                                        <p className={s.analysisText}>{selected.analysis}</p>
                                    </div>
                                </>
                            )}
                        </div>
                    )}
                </section>
            </div>
        </div>
    );
}