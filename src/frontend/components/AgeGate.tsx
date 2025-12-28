import React, { useState } from 'react';

interface AgeGateProps {
  onVerified?: (birthdate: string) => void;
  saveBirthdate?: (birthdate: string) => Promise<void>;
  minAge?: number;
}

/**
 * AgeGate component with improved validation and accessibility
 * Verifies user's age before allowing access to the platform
 */
export const AgeGate: React.FC<AgeGateProps> = ({ 
  onVerified, 
  saveBirthdate,
  minAge = 13 
}) => {
  const [birthdate, setBirthdate] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  /**
   * Calculate age from birthdate
   */
  const calculateAge = (birthdate: string): number => {
    const birth = new Date(birthdate);
    const today = new Date();
    
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    const dayDiff = today.getDate() - birth.getDate();
    
    // Adjust if birthday hasn't occurred this year yet
    if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
      age--;
    }
    
    return age;
  };

  /**
   * Validate birthdate input
   */
  const validateBirthdate = (date: string): { valid: boolean; error?: string } => {
    if (!date) {
      return { valid: false, error: 'Please enter your birthdate' };
    }

    const birth = new Date(date);
    const today = new Date();

    // Check if date is valid
    if (isNaN(birth.getTime())) {
      return { valid: false, error: 'Please enter a valid date' };
    }

    // Check if date is in the future
    if (birth > today) {
      return { valid: false, error: 'Birthdate cannot be in the future' };
    }

    // Check if user is too old (reasonable limit: 120 years)
    const age = calculateAge(date);
    if (age > 120) {
      return { valid: false, error: 'Please enter a valid birthdate' };
    }

    // Check minimum age requirement
    if (age < minAge) {
      return { 
        valid: false, 
        error: `You must be at least ${minAge} years old to use this platform` 
      };
    }

    return { valid: true };
  };

  /**
   * Handle form submission
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const validation = validateBirthdate(birthdate);
    if (!validation.valid) {
      setError(validation.error || 'Invalid birthdate');
      return;
    }

    setLoading(true);

    try {
      // If saveBirthdate prop is provided, use it
      if (saveBirthdate) {
        await saveBirthdate(birthdate);
      } else {
        // Otherwise, make API call to save birthdate
        const response = await fetch('/api/users/me/birthdate', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ birthdate }),
        });

        if (!response.ok) {
          const data = await response.json();
          throw new Error(data.error || 'Failed to save birthdate');
        }
      }

      // Call onVerified callback if provided
      if (onVerified) {
        onVerified(birthdate);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Get max date (today)
   */
  const getMaxDate = (): string => {
    const today = new Date();
    return today.toISOString().split('T')[0];
  };

  /**
   * Get min date (120 years ago)
   */
  const getMinDate = (): string => {
    const minDate = new Date();
    minDate.setFullYear(minDate.getFullYear() - 120);
    return minDate.toISOString().split('T')[0];
  };

  return (
    <div className="age-gate-container" role="dialog" aria-labelledby="age-gate-title" aria-modal="true">
      <div className="age-gate-content">
        <h1 id="age-gate-title" className="age-gate-title">
          Age Verification
        </h1>
        
        <p className="age-gate-description">
          To continue, please verify your age by entering your birthdate.
          You must be at least {minAge} years old to use this platform.
        </p>

        <form onSubmit={handleSubmit} className="age-gate-form">
          <div className="form-group">
            <label htmlFor="birthdate" className="form-label">
              Birthdate
              <span className="required" aria-label="required">*</span>
            </label>
            
            <input
              type="date"
              id="birthdate"
              name="birthdate"
              value={birthdate}
              onChange={(e) => setBirthdate(e.target.value)}
              min={getMinDate()}
              max={getMaxDate()}
              required
              aria-required="true"
              aria-invalid={!!error}
              aria-describedby={error ? 'birthdate-error' : undefined}
              className="form-input"
              disabled={loading}
            />
            
            {error && (
              <div id="birthdate-error" className="error-message" role="alert">
                {error}
              </div>
            )}
          </div>

          <button
            type="submit"
            className="submit-button"
            disabled={loading || !birthdate}
            aria-busy={loading}
          >
            {loading ? 'Verifying...' : 'Continue'}
          </button>
        </form>

        <p className="privacy-notice">
          Your birthdate is used only for age verification and content filtering
          in accordance with our privacy policy.
        </p>
      </div>

      <style jsx>{`
        .age-gate-container {
          display: flex;
          align-items: center;
          justify-content: center;
          min-height: 100vh;
          background-color: #f5f5f5;
          padding: 20px;
        }

        .age-gate-content {
          background: white;
          border-radius: 8px;
          padding: 40px;
          max-width: 500px;
          width: 100%;
          box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .age-gate-title {
          font-size: 24px;
          font-weight: 600;
          margin-bottom: 16px;
          color: #333;
        }

        .age-gate-description {
          font-size: 14px;
          color: #666;
          margin-bottom: 24px;
          line-height: 1.5;
        }

        .age-gate-form {
          margin-bottom: 20px;
        }

        .form-group {
          margin-bottom: 20px;
        }

        .form-label {
          display: block;
          font-size: 14px;
          font-weight: 500;
          margin-bottom: 8px;
          color: #333;
        }

        .required {
          color: #dc3545;
          margin-left: 4px;
        }

        .form-input {
          width: 100%;
          padding: 12px;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 16px;
          transition: border-color 0.2s;
        }

        .form-input:focus {
          outline: none;
          border-color: #007bff;
          box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
        }

        .form-input:disabled {
          background-color: #f5f5f5;
          cursor: not-allowed;
        }

        .form-input[aria-invalid="true"] {
          border-color: #dc3545;
        }

        .error-message {
          color: #dc3545;
          font-size: 14px;
          margin-top: 8px;
        }

        .submit-button {
          width: 100%;
          padding: 12px 24px;
          background-color: #007bff;
          color: white;
          border: none;
          border-radius: 4px;
          font-size: 16px;
          font-weight: 500;
          cursor: pointer;
          transition: background-color 0.2s;
        }

        .submit-button:hover:not(:disabled) {
          background-color: #0056b3;
        }

        .submit-button:disabled {
          background-color: #ccc;
          cursor: not-allowed;
        }

        .submit-button:focus {
          outline: none;
          box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.3);
        }

        .privacy-notice {
          font-size: 12px;
          color: #999;
          line-height: 1.4;
          text-align: center;
        }
      `}</style>
    </div>
  );
};

export default AgeGate;
