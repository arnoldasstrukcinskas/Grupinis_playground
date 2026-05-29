// src/pages/Register/Register.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import styles from './Register.module.css';

/* ── Eye icons ── */
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

/* ── Password strength ── */
function getStrength(pw) {
    if (!pw) return 0;
    let score = 0;
    if (pw.length >= 8) score++;
    if (/[A-Z]/.test(pw)) score++;
    if (/[0-9]/.test(pw)) score++;
    if (/[^A-Za-z0-9]/.test(pw)) score++;
    return score; // 0-4
}
const STRENGTH_LABELS = ['', 'Weak', 'Fair', 'Good', 'Strong'];
const STRENGTH_CLASS = ['', 'weak', 'medium', 'medium', 'strong'];

/* ── Validation ── */
const isValidEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim());

export default function Register() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirm: '',
    });
    const [showPw, setShowPw] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [agreed, setAgreed] = useState(false);
    const [loading, setLoading] = useState(false);
    const [errors, setErrors] = useState({});

    const strength = getStrength(form.password);

    /* ── Field change ── */
    const set = (field) => (e) =>
        setForm((prev) => ({ ...prev, [field]: e.target.value }));

    /* ── Validate ── */
    const validate = () => {
        const e = {};
        if (!form.firstName.trim()) e.firstName = 'First name is required.';
        if (!form.lastName.trim()) e.lastName = 'Last name is required.';
        if (!isValidEmail(form.email)) e.email = 'Please enter a valid email.';
        if (form.password.length < 8) e.password = 'Password must be at least 8 characters.';
        if (form.confirm !== form.password) e.confirm = 'Passwords do not match.';
        if (!agreed) e.agreed = 'You must accept the terms to continue.';
        setErrors(e);
        return Object.keys(e).length === 0;
    };

    /* ── Submit ── */
    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validate()) return;

        setLoading(true);
        try {
            // TODO: replace with real register API call
            // await authService.register({ ...form });
            await new Promise((r) => setTimeout(r, 1200)); // simulate network
            navigate('/login');
        } catch (err) {
            setErrors({ email: err.message || 'Registration failed. Please try again.' });
        } finally {
            setLoading(false);
        }
    };

    /* ── Strength segments ── */
    const segments = [1, 2, 3, 4].map((i) => {
        let cls = '';
        if (i <= strength) cls = styles[STRENGTH_CLASS[strength]];
        return <div key={i} className={`${styles.strengthSeg} ${cls}`} />;
    });

    return (
        <div className={styles.page}>
            <div className={styles.card}>

                {/* Logo */}
                <div className={styles.logo}>
                    <div className={styles.logoIcon}>
                        <svg viewBox="0 0 24 24">
                            <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm-8 2.75c1.24 0 2.25 1.01 2.25 2.25S13.24 11.25 12 11.25 9.75 10.24 9.75 9 10.76 6.75 12 6.75zM17 17H7v-.75c0-1.67 3.33-2.5 5-2.5s5 .83 5 2.5V17z" />
                        </svg>
                    </div>
                    <span className={styles.logoText}>Concierge AI</span>
                </div>

                <h1 className={styles.heading}>Create your <em>account</em></h1>
                <p className={styles.sub}>Free forever. No credit card needed.</p>

                <form onSubmit={handleSubmit} noValidate>

                    {/* Name row */}
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <div className={`${styles.field} ${errors.firstName ? styles.hasError : ''}`} style={{ flex: 1 }}>
                            <label htmlFor="firstName">First name</label>
                            <input
                                id="firstName"
                                type="text"
                                value={form.firstName}
                                onChange={set('firstName')}
                                placeholder="Jane"
                                autoComplete="given-name"
                                disabled={loading}
                            />
                            {errors.firstName && <p className={styles.errorMsg}>{errors.firstName}</p>}
                        </div>
                        <div className={`${styles.field} ${errors.lastName ? styles.hasError : ''}`} style={{ flex: 1 }}>
                            <label htmlFor="lastName">Last name</label>
                            <input
                                id="lastName"
                                type="text"
                                value={form.lastName}
                                onChange={set('lastName')}
                                placeholder="Doe"
                                autoComplete="family-name"
                                disabled={loading}
                            />
                            {errors.lastName && <p className={styles.errorMsg}>{errors.lastName}</p>}
                        </div>
                    </div>

                    {/* Email */}
                    <div className={`${styles.field} ${errors.email ? styles.hasError : ''}`}>
                        <label htmlFor="email">Email</label>
                        <input
                            id="email"
                            type="email"
                            value={form.email}
                            onChange={set('email')}
                            placeholder="you@example.com"
                            autoComplete="email"
                            disabled={loading}
                        />
                        {errors.email && <p className={styles.errorMsg}>{errors.email}</p>}
                    </div>

                    {/* Password */}
                    <div className={`${styles.field} ${errors.password ? styles.hasError : ''}`}>
                        <label htmlFor="password">Password</label>
                        <div className={styles.pwWrap}>
                            <input
                                id="password"
                                type={showPw ? 'text' : 'password'}
                                value={form.password}
                                onChange={set('password')}
                                placeholder="Min. 8 characters"
                                autoComplete="new-password"
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
                        {/* Strength bar */}
                        {form.password && (
                            <>
                                <div className={styles.strengthBar}>{segments}</div>
                                <p className={styles.strengthLabel}>
                                    {STRENGTH_LABELS[strength]}
                                </p>
                            </>
                        )}
                        {errors.password && <p className={styles.errorMsg}>{errors.password}</p>}
                    </div>

                    {/* Confirm password */}
                    <div className={`${styles.field} ${errors.confirm ? styles.hasError : ''}`}>
                        <label htmlFor="confirm">Confirm password</label>
                        <div className={styles.pwWrap}>
                            <input
                                id="confirm"
                                type={showConfirm ? 'text' : 'password'}
                                value={form.confirm}
                                onChange={set('confirm')}
                                placeholder="Repeat password"
                                autoComplete="new-password"
                                disabled={loading}
                            />
                            <button
                                type="button"
                                className={styles.togglePw}
                                onClick={() => setShowConfirm((v) => !v)}
                                aria-label={showConfirm ? 'Hide password' : 'Show password'}
                            >
                                <svg viewBox="0 0 24 24">{showConfirm ? EYE_CLOSED : EYE_OPEN}</svg>
                            </button>
                        </div>
                        {errors.confirm && <p className={styles.errorMsg}>{errors.confirm}</p>}
                    </div>

                    {/* Terms */}
                    <label className={styles.terms}>
                        <input
                            type="checkbox"
                            checked={agreed}
                            onChange={(e) => setAgreed(e.target.checked)}
                            disabled={loading}
                        />
                        I agree to the <a href="/terms">Terms of Service</a> and{' '}
                        <a href="/privacy">Privacy Policy</a>
                    </label>
                    {errors.agreed && (
                        <p className={styles.termsError}>{errors.agreed}</p>
                    )}

                    {/* Create account — GREEN */}
                    <button
                        type="submit"
                        className={styles.btnSubmit}
                        disabled={loading}
                    >
                        {loading && <span className={styles.spinner} aria-hidden="true" />}
                        {loading ? 'Creating account…' : 'Create account'}
                    </button>

                </form>

                <div className={styles.divider}><span>or</span></div>

                {/* Back to login — RED */}
                <button
                    className={styles.btnBack}
                    onClick={() => navigate('/login')}
                    disabled={loading}
                >
                    Back to sign in
                </button>

                <p className={styles.loginRow}>
                    Already have an account?{' '}
                    <Link to="/login">Sign in</Link>
                </p>

            </div>
        </div>
    );
}