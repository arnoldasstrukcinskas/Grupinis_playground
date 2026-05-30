// src/Pages/Login/Login.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login, setToken } from '../../services/api';
import s from './Login.module.css';

export default function Login() {
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [showPw, setShowPw] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        if (!username.trim()) { setError('Username is required.'); return; }
        if (!password) { setError('Password is required.'); return; }

        setLoading(true);
        try {
            const token = await login({ username, password });
            setToken(token);
            navigate('/chat');
        } catch (err) {
            setError(err.message || 'Login failed. Check your credentials.');
        } finally {
            setLoading(false);
        }
    };

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

                <h1 className={s.title}>Sign in</h1>
                <p className={s.sub}>Hotel AI consultation platform</p>

                {error && <p className={s.errorBanner} role="alert">{error}</p>}

                <form onSubmit={handleSubmit} noValidate>
                    <div className={s.field}>
                        <label htmlFor="username">Username</label>
                        <input
                            id="username" type="text"
                            value={username} onChange={e => setUsername(e.target.value)}
                            placeholder="your username" autoComplete="username"
                            disabled={loading}
                        />
                    </div>

                    <div className={s.field}>
                        <label htmlFor="password">Password</label>
                        <div className={s.pwWrap}>
                            <input
                                id="password" type={showPw ? 'text' : 'password'}
                                value={password} onChange={e => setPassword(e.target.value)}
                                placeholder="••••••••" autoComplete="current-password"
                                disabled={loading}
                            />
                            <button type="button" className={s.eyeBtn}
                                onClick={() => setShowPw(v => !v)}
                                aria-label={showPw ? 'Hide' : 'Show'}>
                                {showPw
                                    ? <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" /><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" /><line x1="1" y1="1" x2="23" y2="23" /></svg>
                                    : <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" /></svg>
                                }
                            </button>
                        </div>
                    </div>

                    <button type="submit" className={s.btnGreen} disabled={loading}>
                        {loading ? <span className={s.spinner} /> : null}
                        {loading ? 'Signing in…' : 'Sign in'}
                    </button>
                </form>

                <div className={s.divider}><span>or</span></div>

                <Link to="/register" className={s.btnRed}>Create an account</Link>

                <p className={s.hint}>
                    Demo: username <strong>string</strong> / password <strong>string</strong>
                </p>
            </div>
        </div>
    );
}