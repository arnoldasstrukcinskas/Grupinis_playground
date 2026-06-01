// src/Pages/History/History.jsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllAnalyses, getAnalysisById, getAnalysisHotels, deleteAnalysis, isLoggedIn } from '../../services/api';
import s from './History.module.css';

function Stars({ score, max = 10 }) {
    const pct = Math.round((score / max) * 5);
    return (
        <div className={s.stars}>
            {[1, 2, 3, 4, 5].map(i => (
                <span key={i} className={i <= pct ? s.starFilled : s.starEmpty}>★</span>
            ))}
            <span className={s.starNum}>{score.toFixed(1)}</span>
            <span className={s.starWord}>{score >= 9 ? 'Exceptional' : score >= 8 ? 'Excellent' : score >= 7 ? 'Good' : 'Fair'}</span>
        </div>
    );
}

function HotelModal({ hotel, onClose }) {
    const h = hotel?.content ?? hotel;
    const photo = h.photoUrl1440 || h.maxPhotoUrl || h.mainPhotoUrl;
    const scoreColor = h.reviewScoreNumber >= 9 ? '#1B5E20' : h.reviewScoreNumber >= 8 ? '#2E7D32' : '#1565C0';
    return (
        <div className={s.modalBg} onClick={e => { if (e.target === e.currentTarget) onClose(); }}>
            <div className={s.modalBox}>
                <button className={s.modalClose} onClick={onClose}>✕</button>
                {photo && (
                    <div className={s.modalPhoto}>
                        <img src={photo} alt={h.hotelName} onError={e => e.target.parentElement.style.display = 'none'} />
                        {h.reviewScoreNumber > 0 && (
                            <div className={s.modalScore} style={{ background: scoreColor }}>
                                <span className={s.scoreNum}>{h.reviewScoreNumber.toFixed(1)}</span>
                                <span className={s.scoreWord}>{h.reviewScoreNumber >= 9 ? 'Exceptional' : h.reviewScoreNumber >= 8 ? 'Excellent' : h.reviewScoreNumber >= 7 ? 'Good' : 'Fair'}</span>
                            </div>
                        )}
                    </div>
                )}
                <div className={s.modalBody}>
                    <div className={s.modalHeader}>
                        <div>
                            <h2 className={s.modalTitle}>{h.hotelName}</h2>
                            <div className={s.modalMeta}>
                                {h.accomodationType && <span className={s.accomType}>{h.accomodationType}</span>}
                                {h.hotelStars > 0 && <span className={s.starStr}>{'★'.repeat(Math.min(h.hotelStars, 5))}</span>}
                                {h.isBeachFront && <span className={s.beachTag}>🏖 Beachfront</span>}
                            </div>
                        </div>
                        {h.price && (
                            <div className={s.modalPrice}>
                                <span className={s.price}>{h.price}</span><span className={s.perNight}>/night</span>
                                {h.priceAllInclusive && <div className={s.priceAI}>All‑incl: {h.priceAllInclusive}</div>}
                            </div>
                        )}
                    </div>
                    <div className={s.modalGrid}>
                        {(h.district || h.distanceToCenter || h.address) && (
                            <div className={s.modalSection}>
                                <p className={s.modalSectionTitle}>📍 Location</p>
                                {h.district && <p className={s.modalSectionText}>{h.district}</p>}
                                {h.distanceToCenter && <p className={s.modalSectionText}>{h.distanceToCenter} from center</p>}
                                {h.address && <p className={s.modalSectionText}>{h.address}</p>}
                            </div>
                        )}
                        {h.reviewScoreNumber > 0 && (
                            <div className={s.modalSection}>
                                <p className={s.modalSectionTitle}>💬 Reviews</p>
                                <p className={s.modalSectionText}><strong>{h.reviewScoreNumber.toFixed(1)}/10</strong> — {h.reviewScoreWord}</p>
                                {h.reviewNumber > 0 && <p className={s.modalSectionText}>{h.reviewNumber.toLocaleString()} verified reviews</p>}
                            </div>
                        )}
                        {h.additionals && (
                            <div className={s.modalSection}>
                                <p className={s.modalSectionTitle}>✨ Amenities</p>
                                <p className={s.modalSectionText}>{h.additionals}</p>
                            </div>
                        )}
                        {(h.price || h.priceAllInclusive) && (
                            <div className={s.modalSection}>
                                <p className={s.modalSectionTitle}>💰 Pricing</p>
                                {h.price && <p className={s.modalSectionText}>Standard: <strong>{h.price}</strong>/night</p>}
                                {h.priceAllInclusive && <p className={s.modalSectionText}>All-incl: <strong>{h.priceAllInclusive}</strong>/night</p>}
                            </div>
                        )}
                    </div>
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

function HotelCard({ hotel }) {
    const [showModal, setShowModal] = useState(false);
    const h = hotel?.content ?? hotel;
    const photo = h.photoUrl1440 || h.maxPhotoUrl || h.mainPhotoUrl;
    const stars = h.hotelStars > 0 ? '★'.repeat(Math.min(h.hotelStars, 5)) : null;
    const scoreColor = h.reviewScoreNumber >= 9 ? '#1B5E20' : h.reviewScoreNumber >= 8 ? '#2E7D32' : '#1565C0';

    return (
        <>
            <div className={s.hotelCard} onClick={() => setShowModal(true)} style={{ cursor: 'pointer' }}>
                {photo && (
                    <div className={s.hotelPhoto}>
                        <img src={photo} alt={h.hotelName} onError={e => e.target.parentElement.style.display = 'none'} />
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
                <div className={s.hotelBody}>
                    <div className={s.hotelTop}>
                        <div>
                            <p className={s.hotelName}>{h.hotelName || 'Unknown Hotel'}</p>
                            <div className={s.hotelMeta2}>
                                {h.accomodationType && <span className={s.accomType}>{h.accomodationType}</span>}
                                {stars && <span className={s.starStr}>{stars}</span>}
                            </div>
                        </div>
                    </div>
                    {(h.district || h.distanceToCenter) && (
                        <div className={s.infoRow}><span>📍</span><span className={s.infoText}>{[h.district, h.distanceToCenter].filter(Boolean).join(' · ')}</span></div>
                    )}
                    {h.address && (
                        <div className={s.infoRow}><span>🏠</span><span className={s.infoText}>{h.address}</span></div>
                    )}
                    {h.reviewNumber > 0 && (
                        <div className={s.infoRow}><span>💬</span><span className={s.infoText}>{h.reviewNumber.toLocaleString()} reviews · {h.reviewScoreWord}</span></div>
                    )}
                    {h.additionals && (
                        <div className={s.infoRow}><span>✨</span><span className={s.infoText}>{h.additionals}</span></div>
                    )}
                    {h.isBeachFront && (
                        <div className={s.infoRow}><span>🏖</span><span className={s.infoText}>Beachfront property</span></div>
                    )}
                    <div className={s.priceRow}>
                        {h.price && <div><span className={s.price}>{h.price}</span><span className={s.perNight}>/night</span></div>}
                        {h.priceAllInclusive && <span className={s.priceAI}>All‑incl: {h.priceAllInclusive}</span>}
                    </div>
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

export default function History() {
    const navigate = useNavigate();
    useEffect(() => { if (!isLoggedIn()) navigate('/login'); }, []);

    const [analyses, setAnalyses] = useState([]);
    const [selected, setSelected] = useState(null);
    const [hotels, setHotels] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadingDetail, setLoadingDetail] = useState(false);
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
                setAnalyses(items.reverse());
            } catch (err) { setError(err.message); }
            finally { setLoading(false); }
        })();
    }, []);

    const handleSelect = async (item) => {
        const id = item?.content?.id ?? item?.id;
        if (selected?.id === id) { setSelected(null); setHotels([]); return; }
        setLoadingDetail(true); setHotels([]);
        try {
            const [full, hotelLinks] = await Promise.all([
                getAnalysisById(id),
                getAnalysisHotels(id),
            ]);
            const data = full?.content ?? full;
            setSelected({ id, ...data });

            // hotelLinks is CollectionModel with _links — each link points to a hotel
            // Extract hotel IDs from links and fetch each
            if (hotelLinks?._links) {
                const links = Object.values(hotelLinks._links);
                const hotelData = await Promise.all(
                    links.map(async link => {
                        try {
                            const res = await fetch(link.href, {
                                headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
                            });
                            return res.ok ? res.json() : null;
                        } catch { return null; }
                    })
                );
                setHotels(hotelData.filter(Boolean));
            }
        } catch (err) { setError(err.message); }
        finally { setLoadingDetail(false); }
    };

    const handleDelete = async (e, item) => {
        e.stopPropagation();
        const id = item?.content?.id ?? item?.id ?? item;
        if (!window.confirm('Delete this analysis?')) return;
        setDeleting(id);
        try {
            await deleteAnalysis(id);
            setAnalyses(prev => prev.filter(a => (a?.content?.id ?? a?.id) !== id));
            if (selected?.id === id) { setSelected(null); setHotels([]); }
        } catch (err) { setError('Delete failed: ' + err.message); }
        finally { setDeleting(null); }
    };

    const getPreview = (item) => {
        const text = item?.content?.analysis ?? item?.analysis ?? '';
        const match = text.match(/\*\*([^*]+)\*\*/);
        return match ? match[1] : text.slice(0, 50) + '…';
    };

    return (
        <div className={s.page}>
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
                            <p>Go to chat and save a consultation.</p>
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
                                    <p className={s.listItemTitle}>Analysis #{id}</p>
                                    <p className={s.listItemPreview}>{getPreview(item)}</p>
                                </div>
                                <button className={s.deleteBtn}
                                    onClick={e => handleDelete(e, item)}
                                    disabled={deleting === id}>
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
                    {!selected && !loadingDetail && (
                        <div className={s.detailEmpty}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" width="40" height="40" style={{ color: 'var(--sky)', marginBottom: '1rem' }}>
                                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
                            </svg>
                            <p>Select an analysis to view hotels</p>
                        </div>
                    )}

                    {loadingDetail && <p className={s.dim} style={{ textAlign: 'center', paddingTop: '3rem' }}>Loading hotels…</p>}

                    {selected && !loadingDetail && (
                        <div className={s.detailContent}>
                            <div className={s.detailHead}>
                                <h2 className={s.detailTitle}>Analysis #{selected.id}</h2>
                                <button className={s.btnDelete} onClick={e => handleDelete(e, selected)}>Delete</button>
                            </div>

                            {hotels.length > 0 ? (
                                <>
                                    <p className={s.sectionLabel}>{hotels.length} Hotels found</p>
                                    <div className={s.hotelGrid}>
                                        {hotels.map((h, i) => <HotelCard key={i} hotel={h} />)}
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