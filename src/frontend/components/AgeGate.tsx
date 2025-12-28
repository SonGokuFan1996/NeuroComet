import React, { useState } from 'react';

interface AgeGateProps {
  /**
   * Callback function to save the user's birthdate
   * @param birthdate - ISO date string (YYYY-MM-DD)
   */
  saveBirthdate: (birthdate: string) => Promise<void>;
}

/**
 * AgeGate Component
 * 
 * Prompts users without a birthdate to provide their date of birth.
 * After submission, the page reloads to reflect the updated age group.
 * 
 * Usage:
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

    if (!birthdate) {
      setError('Please enter your date of birth');
      return;
    }

    // Validate date format and reasonable range
    const selectedDate = new Date(birthdate);
    const today = new Date();
    const minDate = new Date('1900-01-01');

    if (isNaN(selectedDate.getTime())) {
      setError('Please enter a valid date');
      return;
    }

    if (selectedDate > today) {
      setError('Birthdate cannot be in the future');
      return;
    }

    if (selectedDate < minDate) {
      setError('Please enter a valid birthdate');
      return;
    }

    setLoading(true);

    try {
      await saveBirthdate(birthdate);
      
      // Reload the page to refresh user data and age group
      window.location.reload();
    } catch (err) {
      setError('Failed to save birthdate. Please try again.');
      setLoading(false);
      console.error('Error saving birthdate:', err);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.modal}>
        <h2 style={styles.title}>Welcome to NeuroNet</h2>
        <p style={styles.description}>
          To continue, please provide your date of birth. This helps us provide
          age-appropriate content and comply with privacy regulations.
        </p>

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.inputGroup}>
            <label htmlFor="birthdate" style={styles.label}>
              Date of Birth
            </label>
            <input
              type="date"
              id="birthdate"
              value={birthdate}
              onChange={(e) => setBirthdate(e.target.value)}
              max={new Date().toISOString().split('T')[0]}
              required
              style={styles.input}
              disabled={loading}
            />
          </div>

          {error && (
            <div style={styles.error} role="alert">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            style={{
              ...styles.button,
              ...(loading ? styles.buttonDisabled : {}),
            }}
          >
            {loading ? 'Saving...' : 'Continue'}
          </button>
        </form>

        <p style={styles.privacyNote}>
          Your date of birth is used solely for age verification and will be kept
          private in accordance with our privacy policy.
        </p>
      </div>
    </div>
  );
};

// Inline styles for simplicity (consider using CSS modules or styled-components in production)
const styles: { [key: string]: React.CSSProperties } = {
  container: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
  },
  modal: {
    backgroundColor: '#ffffff',
    borderRadius: '8px',
    padding: '32px',
    maxWidth: '500px',
    width: '90%',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
  },
  title: {
    fontSize: '24px',
    fontWeight: 'bold',
    marginBottom: '16px',
    color: '#333',
  },
  description: {
    fontSize: '16px',
    color: '#666',
    marginBottom: '24px',
    lineHeight: '1.5',
  },
  form: {
    marginBottom: '16px',
  },
  inputGroup: {
    marginBottom: '20px',
  },
  label: {
    display: 'block',
    fontSize: '14px',
    fontWeight: '600',
    marginBottom: '8px',
    color: '#333',
  },
  input: {
    width: '100%',
    padding: '10px 12px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    boxSizing: 'border-box',
  },
  button: {
    width: '100%',
    padding: '12px',
    fontSize: '16px',
    fontWeight: '600',
    color: '#ffffff',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    transition: 'background-color 0.2s',
  },
  buttonDisabled: {
    backgroundColor: '#6c757d',
    cursor: 'not-allowed',
  },
  error: {
    color: '#dc3545',
    fontSize: '14px',
    marginBottom: '16px',
    padding: '8px 12px',
    backgroundColor: '#f8d7da',
    border: '1px solid #f5c6cb',
    borderRadius: '4px',
  },
  privacyNote: {
    fontSize: '12px',
    color: '#888',
    textAlign: 'center',
    marginTop: '16px',
  },
};

export default AgeGate;
