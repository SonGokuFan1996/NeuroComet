import React, { useState } from 'react';

/**
 * Props for AgeGate component
 */
interface AgeGateProps {
  /**
   * Callback function to save birthdate
   * Should call API to update user profile with birthdate
   */
  saveBirthdate: (birthdate: string) => Promise<void>;
}

/**
 * AgeGate Component
 * 
 * Prompts user to enter their birthdate for age verification.
 * Should be displayed when user.birthdate is null/undefined.
 * 
 * After successful save, reloads the page to apply age-based restrictions.
 * 
 * @example
 * ```tsx
 * <AgeGate saveBirthdate={async (bd) => {
 *   await api.updateProfile({ birthdate: bd });
 * }} />
 * ```
 */
const AgeGate: React.FC<AgeGateProps> = ({ saveBirthdate }) => {
  const [birthdate, setBirthdate] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string>('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // Validate birthdate is provided
    if (!birthdate) {
      setError('Please enter your birthdate');
      return;
    }

    // Validate birthdate is a valid date
    const date = new Date(birthdate);
    if (isNaN(date.getTime())) {
      setError('Please enter a valid date');
      return;
    }

    // Validate birthdate is not in the future
    if (date > new Date()) {
      setError('Birthdate cannot be in the future');
      return;
    }

    // Validate user is at least 1 year old (sanity check)
    const oneYearAgo = new Date();
    oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1);
    if (date > oneYearAgo) {
      setError('Invalid birthdate');
      return;
    }

    try {
      setLoading(true);
      await saveBirthdate(birthdate);
      
      // Reload page to apply age-based restrictions
      window.location.reload();
    } catch (err) {
      setError('Failed to save birthdate. Please try again.');
      console.error('Error saving birthdate:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>Welcome to NeuroNet</h1>
        <p style={styles.description}>
          To provide you with an age-appropriate experience, please enter your birthdate.
        </p>
        
        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.inputGroup}>
            <label htmlFor="birthdate" style={styles.label}>
              Birthdate
            </label>
            <input
              type="date"
              id="birthdate"
              value={birthdate}
              onChange={(e) => setBirthdate(e.target.value)}
              style={styles.input}
              disabled={loading}
              required
            />
          </div>

          {error && (
            <div style={styles.error}>
              {error}
            </div>
          )}

          <button
            type="submit"
            style={styles.button}
            disabled={loading}
          >
            {loading ? 'Saving...' : 'Continue'}
          </button>
        </form>

        <p style={styles.privacyNote}>
          Your birthdate is used to provide age-appropriate content and comply with privacy regulations.
        </p>
      </div>
    </div>
  );
};

/**
 * Basic inline styles for the component
 * In production, use CSS modules, styled-components, or your preferred styling solution
 */
const styles: { [key: string]: React.CSSProperties } = {
  container: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '100vh',
    backgroundColor: '#f5f5f5',
    padding: '20px',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
    padding: '40px',
    maxWidth: '500px',
    width: '100%',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    marginBottom: '16px',
    color: '#333',
    textAlign: 'center',
  },
  description: {
    fontSize: '16px',
    color: '#666',
    marginBottom: '32px',
    textAlign: 'center',
    lineHeight: '1.5',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
  },
  inputGroup: {
    marginBottom: '24px',
  },
  label: {
    display: 'block',
    fontSize: '14px',
    fontWeight: '500',
    color: '#333',
    marginBottom: '8px',
  },
  input: {
    width: '100%',
    padding: '12px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    boxSizing: 'border-box',
  },
  button: {
    backgroundColor: '#007bff',
    color: 'white',
    padding: '12px 24px',
    fontSize: '16px',
    fontWeight: '500',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    transition: 'background-color 0.2s',
  },
  error: {
    backgroundColor: '#fff3f3',
    color: '#d32f2f',
    padding: '12px',
    borderRadius: '4px',
    marginBottom: '16px',
    fontSize: '14px',
    border: '1px solid #ffcdd2',
  },
  privacyNote: {
    fontSize: '12px',
    color: '#999',
    marginTop: '24px',
    textAlign: 'center',
    lineHeight: '1.4',
  },
};

export default AgeGate;
