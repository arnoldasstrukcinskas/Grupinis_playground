// src/Pages/SingUp/Register.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../../services/api';
import s from './Register.module.css';

function strength(pw) {
    if (!pw) return 0;
    let n = 0;
    if (pw.length >= 8) n++;
    if (/[A-Z]/.test(pw)) n++;
    if (/[0-9]/.test(pw)) n++;
    if (/[^A-Za-z0-9]/.test(pw)) n++;
    return n;
}
const LABELS = ['', 'Weak', 'Fair', 'Good', 'Strong'];
const COLORS = ['', '#C0392B', '#F39C12', '#2260B4', '#1E7E4A'];

export default function Register() {
    const navigate = useNavigate();
    const [form, setForm] = useState({ username: '', email: '', password: '', confirm: '' });
    const [showPw, setShowPw] = useState(false);
    const [loading, setLoad] = useState(false);
    const [errors, setErrors] = useState({});
    const [success, setSuccess] = useState('');

    const set = f => e => setForm(p => ({ ...p, [f]: e.target.value }));

    const validate = () => {
        const e = {};
        if (!form.username.trim()) e.username = 'Username is required.';
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Valid email required.';
        if (form.password.length < 8) e.password = 'Min. 8 characters.';
        if (form.confirm !== form.password) e.confirm = 'Passwords do not match.';
        setErrors(e);
        return Object.keys(e).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validate()) return;
        setLoad(true);
        setSuccess('');
        try {
            await register({ username: form.username, email: form.email, password: form.password });
            setSuccess('Account created! Redirecting to login…');
            setTimeout(() => navigate('/login'), 1500);
        } catch (err) {
            setErrors({ username: err.message || 'Registration failed.' });
        } finally {
            setLoad(false);
        }
    };

    const pw = form.password;
    const str = strength(pw);

    return (
        <div className={s.page}>
            <div className={s.card}>

                <div className={s.logo}>
                    <div className={s.logoIcon}>
                        <svg viewBox="0 0 24 24" fill="white" width="18" height="18">
                            <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm-8 2.75c1.24 0 2.25 1.01 2.25 2.25S13.24 11.25 12 11.25 9.75 10.24 9.75 9 10.76 6.75 12 6.75zM17 17H7v-.75c0-1.67 3.33-2.5 5-2.5s5 .83 5 2.5V17z" />
                        </svg>
                    </div>
                    <span className={s.logoText}>Concierge AI</span>
                </div>

                <h1 className={s.title}>Create account</h1>
                <p className={s.sub}>Free forever. No credit card needed.</p>

                {success && <p className={s.successBanner}>{success}</p>}

                <form onSubmit={handleSubmit} noValidate>

                    {/* Username */}
                    <div className={`${s.field} ${errors.username ? s.hasError : ''}`}>
                        <label htmlFor="username">Username</label>
                        <input id="username" type="text" value={form.username}
                            onChange={set('username')} placeholder="choose a username"
                            autoComplete="username" disabled={loading} />
                        {errors.username && <p className={s.err}>{errors.username}</p>}
                    </div>

                    {/* Email */}
                    <div className={`${s.field} ${errors.email ? s.hasError : ''}`}>
                        <label htmlFor="email">Email</label>
                        <input id="email" type="email" value={form.email}
                            onChange={set('email')} placeholder="you@example.com"
                            autoComplete="email" disabled={loading} />
                        {errors.email && <p className={s.err}>{errors.email}</p>}
                    </div>

                    {/* Password */}
                    <div className={`${s.field} ${errors.password ? s.hasError : ''}`}>
                        <label htmlFor="password">Password</label>
                        <div className={s.pwWrap}>
                            <input id="password" type={showPw ? 'text' : 'password'}
                                value={form.password} onChange={set('password')}
                                placeholder="Min. 8 characters" autoComplete="new-password"
                                disabled={loading} />
                            <button type="button" className={s.eyeBtn}
                                onClick={() => setShowPw(v => !v)}>
                                {showPw
                                    ? <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" /><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" /><line x1="1" y1="1" x2="23" y2="23" /></svg>
                                    : <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" /></svg>
                                }
                            </button>
                        </div>
                        {/* Strength bar */}
                        {pw && (
                            <div className={s.strengthWrap}>
                                <div className={s.strengthBar}>
                                    {[1, 2, 3, 4].map(i => (
                                        <div key={i} className={s.seg}
                                            style={{ background: i <= str ? COLORS[str] : '' }} />
                                    ))}
                                </div>
                                <span className={s.strengthLabel} style={{ color: COLORS[str] }}>
                                    {LABELS[str]}
                                </span>
                            </div>
                        )}
                        {errors.password && <p className={s.err}>{errors.password}</p>}
                    </div>

                    {/* Confirm */}
                    <div className={`${s.field} ${errors.confirm ? s.hasError : ''}`}>
                        <label htmlFor="confirm">Confirm password</label>
                        <input id="confirm" type="password" value={form.confirm}
                            onChange={set('confirm')} placeholder="Repeat password"
                            autoComplete="new-password" disabled={loading} />
                        {errors.confirm && <p className={s.err}>{errors.confirm}</p>}
                    </div>

                    <button type="submit" className={s.btnGreen} disabled={loading}>
                        {loading && <span className={s.spinner} />}
                        {loading ? 'Creating…' : 'Create account'}
                    </button>
                </form>

                <div className={s.divider}><span>or</span></div>
                <Link to="/login" className={s.btnRed}>Back to sign in</Link>

                <p className={s.loginRow}>
                    Already have an account? <Link to="/login">Sign in</Link>
                </p>
            </div>
        </div>
    );
}