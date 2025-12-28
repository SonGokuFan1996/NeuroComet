import React, { useState } from 'react';

interface AgeGateProps {
  saveBirthdate: (birthdate: string) => Promise<void>;
}

/**
 * AgeGate Component
 * 
 * Prompts users to enter their birthdate if not already provided.
 * After saving, reloads the page to update the user's age group.
 */
export const AgeGate: React.FC<AgeGateProps> = ({ saveBirthdate }) => {
  const [birthdate, setBirthdate] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string>('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!birthdate) {
      setError('Please enter your birthdate');
      return;
    }

    // Validate that the birthdate is not in the future
    const selectedDate = new Date(birthdate);
    const today = new Date();
    
    if (selectedDate > today) {
      setError('Birthdate cannot be in the future');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      await saveBirthdate(birthdate);
      // Reload the page to update the user's age group
      window.location.reload();
    } catch (err) {
      setError('Failed to save birthdate. Please try again.');
      setIsLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h2 style={styles.title}>Welcome!</h2>
        <p style={styles.description}>
          To provide you with age-appropriate content and features, please enter your birthdate.
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
              disabled={isLoading}
              style={styles.input}
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
            disabled={isLoading}
            style={{
              ...styles.button,
              ...(isLoading ? styles.buttonDisabled : {}),
            }}
          >
            {isLoading ? 'Saving...' : 'Continue'}
          </button>
        </form>

        <p style={styles.disclaimer}>
          We collect your birthdate to comply with privacy regulations and provide age-appropriate content.
          Your information is kept secure and private.
        </p>
      </div>
    </div>
  );
};

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
    boxShadow: '0 2px 10px rgba(0, 0, 0, 0.1)',
    padding: '40px',
    maxWidth: '500px',
    width: '100%',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    marginBottom: '10px',
    color: '#333',
  },
  description: {
    fontSize: '16px',
    color: '#666',
    marginBottom: '30px',
    lineHeight: '1.5',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
  },
  inputGroup: {
    marginBottom: '20px',
  },
  label: {
    display: 'block',
    fontSize: '14px',
    fontWeight: '500',
    marginBottom: '8px',
    color: '#333',
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
  buttonDisabled: {
    backgroundColor: '#ccc',
    cursor: 'not-allowed',
  },
  error: {
    backgroundColor: '#fee',
    color: '#c33',
    padding: '10px',
    borderRadius: '4px',
    marginBottom: '15px',
    fontSize: '14px',
  },
  disclaimer: {
    fontSize: '12px',
    color: '#999',
    marginTop: '20px',
    lineHeight: '1.4',
  },
};
