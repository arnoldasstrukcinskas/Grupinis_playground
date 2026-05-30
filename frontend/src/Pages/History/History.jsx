// src/Pages/History/History.jsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllAnalyses, getAnalysisById, deleteAnalysis, isLoggedIn } from '../../services/api';
import s from './History.module.css';

export default function History() {
    const navigate = useNavigate();

    useEffect(() => { if (!isLoggedIn()) navigate('/login'); }, []);

    const [analyses, setAnalyses] = useState([]);
    const [selected, setSelected] = useState(null); // full analysis object
    const [loading, setLoading] = useState(true);
    const [detailLoading, setDetailLoading] = useState(false);
    const [error, setError] = useState('');

    // Load all analyses on mount
    useEffect(() => {
        (async () => {
            setLoading(true);
            try {
                const res = await getAllAnalyses();
                // res is CollectionModel — items in _embedded or content array
                const items = res?._embedded
                    ? Object.values(res._embedded).flat()
                    : Array.isArray(res) ? res : [];
                setAnalyses(items);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        })();
    }, []);

    const handleSelect = async (item) => {
        const id = item?.content?.id ?? item?.id;
        if (!id) return;
        setDetailLoading(true);
        try {
            const full = await getAnalysisById(id);
            setSelected(full);
        } catch (err) {
            setError(err.message);
        } finally {
            setDetailLoading(false);
        }
    };

    const handleDelete = async (e, item) => {
        e.stopPropagation();
        const id = item?.content?.id ?? item?.id;
        if (!id) return;
        try {
            await deleteAnalysis(id);
            setAnalyses(prev => prev.filter(a => (a?.content?.id ?? a?.id) !== id));
            if ((selected?.content?.id ?? selected?.id) === id) setSelected(null);
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className={s.page}>
            {/* HEADER */}
            <header className={s.header}>
                <div className={s.headerLeft}>
                    <button className={s.backBtn} onClick={() => navigate('/chat')}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" width="16" height="16">
                            <polyline points="15 18 9 12 15 6" />
                        </svg>
                        Back to chat
                    </button>
                    <h1 className={s.title}>Saved Analyses</h1>
                </div>
                <span className={s.count}>{analyses.length} saved</span>
            </header>

            {error && <p className={s.errorBanner}>{error}</p>}

            <div className={s.body}>
                {/* LIST */}
                <aside className={s.list}>
                    {loading && <p className={s.dimText}>Loading…</p>}
                    {!loading && analyses.length === 0 && (
                        <div className={s.empty}>
                            <p>No saved analyses yet.</p>
                            <p>Go to the chat and save a consultation.</p>
                        </div>
                    )}
                    {analyses.map((item, i) => {
                        const id = item?.content?.id ?? item?.id;
                        const text = item?.content?.analysis ?? item?.analysis ?? '';
                        const isActive = (selected?.content?.id ?? selected?.id) === id;
                        return (
                            <div
                                key={id ?? i}
                                className={`${s.item} ${isActive ? s.active : ''}`}
                                onClick={() => handleSelect(item)}
                            >
                                <div className={s.itemId}>#{id}</div>
                                <div className={s.itemPreview}>
                                    {text ? text.slice(0, 80) + (text.length > 80 ? '…' : '') : 'Analysis'}
                                </div>
                                <button
                                    className={s.deleteBtn}
                                    onClick={e => handleDelete(e, item)}
                                    aria-label="Delete"
                                >
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" width="12" height="12">
                                        <polyline points="3 6 5 6 21 6" />
                                        <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                                        <path d="M10 11v6M14 11v6" />
                                        <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                                    </svg>
                                </button>
                            </div>
                        );
                    })}
                </aside>

                {/* DETAIL */}
                <section className={s.detail}>
                    {detailLoading && <p className={s.dimText}>Loading…</p>}

                    {!detailLoading && !selected && (
                        <div className={s.detailEmpty}>
                            <p>Select an analysis from the list to view details.</p>
                        </div>
                    )}

                    {!detailLoading && selected && (() => {
                        const data = selected?.content ?? selected;
                        const id = data?.id;
                        const text = data?.analysis ?? '';
                        const hotels = selected?._links ?? null;
                        return (
                            <div className={s.detailContent}>
                                <div className={s.detailHead}>
                                    <h2 className={s.detailTitle}>Analysis #{id}</h2>
                                    <button className={s.btnRed} onClick={e => { handleDelete(e, selected); setSelected(null); }}>
                                        Delete
                                    </button>
                                </div>

                                <div className={s.analysisBox}>
                                    <p className={s.sectionLabel}>AI Analysis</p>
                                    <p className={s.analysisText}>{text || 'No analysis text.'}</p>
                                </div>

                                {hotels && (
                                    <div className={s.hotelsBox}>
                                        <p className={s.sectionLabel}>Hotels</p>
                                        {Object.entries(hotels)
                                            .filter(([key]) => key !== 'self')
                                            .map(([key, link]) => (
                                                <a
                                                    key={key}
                                                    href={link.href}
                                                    target="_blank"
                                                    rel="noreferrer"
                                                    className={s.hotelLink}
                                                >
                                                    {key}
                                                </a>
                                            ))
                                        }
                                    </div>
                                )}
                            </div>
                        );
                    })()}
                </section>
            </div>
        </div>
    );
}