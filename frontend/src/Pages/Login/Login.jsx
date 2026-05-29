// src/pages/Login/Login.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './Login.module.css';

/* ─── Eye icon SVG paths ─── */
const EYE_OPEN = (
    <>
        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
        <circle cx="12" cy="12" r="3" />
    </>
);
const EYE_CLOSED = (
    <>
        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
        <line x1="1" y1="1" x2="23" y2="23" />
    </>
);

/* ─── Validation helpers ─── */
const isValidEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim());
const isValidPassword = (v) => v.length >= 8;

export default function Login() {
    const navigate = useNavigate();

    /* Form state */
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [remember, setRemember] = useState(false);
    const [showPw, setShowPw] = useState(false);
    const [loading, setLoading] = useState(false);

    /* Validation errors */
    const [errors, setErrors] = useState({ email: '', password: '' });

    /* ─── Validate ─── */
    const validate = () => {
        const next = { email: '', password: '' };
        if (!isValidEmail(email)) next.email = 'Please enter a valid email address.';
        if (!isValidPassword(password)) next.password = 'Password must be at least 8 characters.';
        setErrors(next);
        return !next.email && !next.password;
    };

    /* ─── Submit ─── */
    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validate()) return;

        setLoading(true);
        try {
            // TODO: replace with real auth API call
            // const res = await authService.login({ email, password, remember });
            await new Promise((r) => setTimeout(r, 1200)); // simulate network
            navigate('/chat');
        } catch (err) {
            setErrors((prev) => ({
                ...prev,
                password: err.message || 'Login failed. Please try again.',
            }));
        } finally {
            setLoading(false);
        }
    };

    /* ─── Guest ─── */
    const handleGuest = () => navigate('/chat');

    return (
        <div className={styles.page}>

            {/* ── LEFT PANEL ── */}
            <div className={styles.left}>
                <div className={styles.circle1} aria-hidden="true" />
                <div className={styles.circle2} aria-hidden="true" />

                {/* Logo */}
                <div className={styles.logo}>
                    <div className={styles.logoIcon} aria-hidden="true">
                        <svg viewBox="0 0 24 24">
                            <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm-8 2.75c1.24 0 2.25 1.01 2.25 2.25S13.24 11.25 12 11.25 9.75 10.24 9.75 9 10.76 6.75 12 6.75zM17 17H7v-.75c0-1.67 3.33-2.5 5-2.5s5 .83 5 2.5V17z" />
                        </svg>
                    </div>
                    <span className={styles.logoText}>Concierge AI</span>
                </div>

                {/* Hero */}
                <div className={styles.hero}>
                    <p className={styles.heroLabel}>AI Hotel Advisor</p>
                    <h1 className={styles.heroTitle}>
                        Your personal<br />
                        <em>hotel expert,</em><br />
                        always on call.
                    </h1>
                    <p className={styles.heroBody}>
                        Ask anything about hotels worldwide — from comparing stays in Kyoto
                        to finding the quietest room in a Paris boutique. Our AI knows the
                        details travel sites don't show you.
                    </p>
                    <div className={styles.features}>
                        {[
                            ['Honest comparisons', '— beyond star ratings and sponsored rankings'],
                            ['Local insight', '— neighbourhood feel, noise levels, hidden perks'],
                            ['Tailored advice', '— based on your travel style and priorities'],
                        ].map(([bold, rest]) => (
                            <div key={bold} className={styles.feat}>
                                <div className={styles.featDot} aria-hidden="true" />
                                <p className={styles.featText}>
                                    <strong>{bold}</strong>{rest}
                                </p>
                            </div>
                        ))}
                    </div>
                </div>

                <p className={styles.leftFoot}>© 2026 Concierge AI</p>
            </div>

            {/* ── RIGHT PANEL ── */}
            <div className={styles.right}>
                <p className={styles.formEyebrow}>Welcome back</p>
                <h2 className={styles.formTitle}>
                    Sign in to your<br /><em>advisor</em>
                </h2>

                <form onSubmit={handleSubmit} noValidate>

                    {/* Email */}
                    <div className={`${styles.field} ${errors.email ? styles.hasError : ''}`}>
                        <label htmlFor="email">Email</label>
                        <input
                            id="email"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            onBlur={() => {
                                if (email && !isValidEmail(email))
                                    setErrors((p) => ({ ...p, email: 'Please enter a valid email address.' }));
                                else
                                    setErrors((p) => ({ ...p, email: '' }));
                            }}
                            placeholder="you@example.com"
                            autoComplete="email"
                            aria-describedby="email-error"
                            disabled={loading}
                        />
                        {errors.email && (
                            <p id="email-error" className={styles.errorMsg} role="alert">
                                {errors.email}
                            </p>
                        )}
                    </div>

                    {/* Password */}
                    <div className={`${styles.field} ${errors.password ? styles.hasError : ''}`}>
                        <label htmlFor="password">Password</label>
                        <div className={styles.pwWrap}>
                            <input
                                id="password"
                                type={showPw ? 'text' : 'password'}
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="••••••••"
                                autoComplete="current-password"
                                aria-describedby="password-error"
                                disabled={loading}
                            />
                            <button
                                type="button"
                                className={styles.togglePw}
                                onClick={() => setShowPw((v) => !v)}
                                aria-label={showPw ? 'Hide password' : 'Show password'}
                            >
                                <svg viewBox="0 0 24 24">{showPw ? EYE_CLOSED : EYE_OPEN}</svg>
                            </button>
                        </div>
                        {errors.password && (
                            <p id="password-error" className={styles.errorMsg} role="alert">
                                {errors.password}
                            </p>
                        )}
                    </div>

                    {/* Options row */}
                    <div className={styles.rowOpts}>
                        <label className={styles.remember}>
                            <input
                                type="checkbox"
                                checked={remember}
                                onChange={(e) => setRemember(e.target.checked)}
                            />
                            Keep me signed in
                        </label>
                        <a href="/forgot-password" className={styles.forgot}>
                            Forgot password?
                        </a>
                    </div>

                    {/* Sign in — GREEN */}
                    <button
                        type="submit"
                        className={styles.btnSignIn}
                        disabled={loading}
                        aria-busy={loading}
                    >
                        {loading && <span className={styles.spinner} aria-hidden="true" />}
                        {loading ? 'Signing in…' : 'Sign in'}
                    </button>

                </form>

                <div className={styles.divider}><span>or</span></div>

                {/* Guest — RED */}
                <button
                    className={styles.btnGuest}
                    onClick={handleGuest}
                    disabled={loading}
                >
                    Continue without an account
                </button>

                <p className={styles.signupRow}>
                    New here?{' '}
                    <a href="/register">Create a free account</a>
                </p>
            </div>
        </div>
    );
}