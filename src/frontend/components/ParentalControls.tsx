import React, { useState, useEffect } from 'react';

/**
 * Parental controls data structure
 */
interface ParentalControlsData {
  enabled: boolean;
  allow_chat: boolean;
  allow_profile_public: boolean;
  whitelist: string[];
}

/**
 * Props for ParentalControls component
 */
interface ParentalControlsProps {
  /**
   * Child user ID whose settings to manage
   */
  childId: number;
  
  /**
   * Optional: Base API URL (defaults to '/api')
   */
  apiBaseUrl?: string;
}

/**
 * ParentalControls Component
 * 
 * Allows parents to manage their child's account settings including:
 * - Enable/disable parental controls
 * - Allow/disallow chat features
 * - Control profile visibility
 * - Manage content whitelist (future use)
 * 
 * Fetches current settings from GET /api/parental-controls/:childId
 * Updates settings via POST /api/parental-controls/:childId
 * 
 * @example
 * ```tsx
 * <ParentalControls childId={123} />
 * ```
 */
const ParentalControls: React.FC<ParentalControlsProps> = ({ 
  childId,
  apiBaseUrl = '/api'
}) => {
  const [settings, setSettings] = useState<ParentalControlsData>({
    enabled: true,
    allow_chat: false,
    allow_profile_public: false,
    whitelist: [],
  });
  const [whitelistInput, setWhitelistInput] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [error, setError] = useState<string>('');
  const [successMessage, setSuccessMessage] = useState<string>('');

  /**
   * Fetch current parental controls settings
   */
  useEffect(() => {
    const fetchSettings = async () => {
      try {
        setLoading(true);
        setError('');
        
        const response = await fetch(`${apiBaseUrl}/parental-controls/${childId}`, {
          credentials: 'include', // Include cookies for authentication
        });

        if (!response.ok) {
          if (response.status === 403) {
            throw new Error('You are not authorized to manage this child account');
          } else if (response.status === 404) {
            throw new Error('Child account not found');
          } else {
            throw new Error('Failed to load parental controls');
          }
        }

        const data = await response.json();
        setSettings({
          enabled: data.enabled ?? true,
          allow_chat: data.allow_chat ?? false,
          allow_profile_public: data.allow_profile_public ?? false,
          whitelist: data.whitelist ?? [],
        });
        setWhitelistInput((data.whitelist ?? []).join(', '));
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Failed to load settings';
        setError(message);
        console.error('Error loading parental controls:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchSettings();
  }, [childId, apiBaseUrl]);

  /**
   * Save updated settings
   */
  const handleSave = async () => {
    try {
      setSaving(true);
      setError('');
      setSuccessMessage('');

      // Parse whitelist from comma-separated input
      const whitelist = whitelistInput
        .split(',')
        .map(item => item.trim())
        .filter(item => item.length > 0);

      const payload = {
        enabled: settings.enabled,
        allow_chat: settings.allow_chat,
        allow_profile_public: settings.allow_profile_public,
        whitelist,
      };

      const response = await fetch(`${apiBaseUrl}/parental-controls/${childId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        if (response.status === 403) {
          throw new Error('You are not authorized to update this child account');
        } else {
          throw new Error('Failed to save settings');
        }
      }

      const updated = await response.json();
      setSettings({
        enabled: updated.enabled,
        allow_chat: updated.allow_chat,
        allow_profile_public: updated.allow_profile_public,
        whitelist: updated.whitelist,
      });
      setSuccessMessage('Settings saved successfully!');
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to save settings';
      setError(message);
      console.error('Error saving parental controls:', err);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div style={styles.container}>
        <div style={styles.loading}>Loading parental controls...</div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h2 style={styles.title}>Parental Controls</h2>
        <p style={styles.description}>
          Manage your child's account settings and restrictions
        </p>

        {error && (
          <div style={styles.error}>
            {error}
          </div>
        )}

        {successMessage && (
          <div style={styles.success}>
            {successMessage}
          </div>
        )}

        <div style={styles.section}>
          <div style={styles.toggleRow}>
            <div>
              <h3 style={styles.toggleLabel}>Parental Controls</h3>
              <p style={styles.toggleDescription}>
                Enable or disable all parental control restrictions
              </p>
            </div>
            <label style={styles.switch}>
              <input
                type="checkbox"
                checked={settings.enabled}
                onChange={(e) => setSettings({ ...settings, enabled: e.target.checked })}
                style={styles.checkbox}
              />
              <span style={styles.slider}></span>
            </label>
          </div>
        </div>

        <div style={styles.section}>
          <div style={styles.toggleRow}>
            <div>
              <h3 style={styles.toggleLabel}>Allow Chat</h3>
              <p style={styles.toggleDescription}>
                Allow your child to use chat and messaging features
              </p>
            </div>
            <label style={styles.switch}>
              <input
                type="checkbox"
                checked={settings.allow_chat}
                onChange={(e) => setSettings({ ...settings, allow_chat: e.target.checked })}
                disabled={!settings.enabled}
                style={styles.checkbox}
              />
              <span style={styles.slider}></span>
            </label>
          </div>
        </div>

        <div style={styles.section}>
          <div style={styles.toggleRow}>
            <div>
              <h3 style={styles.toggleLabel}>Public Profile</h3>
              <p style={styles.toggleDescription}>
                Allow your child's profile to be visible to other users
              </p>
            </div>
            <label style={styles.switch}>
              <input
                type="checkbox"
                checked={settings.allow_profile_public}
                onChange={(e) => setSettings({ ...settings, allow_profile_public: e.target.checked })}
                disabled={!settings.enabled}
                style={styles.checkbox}
              />
              <span style={styles.slider}></span>
            </label>
          </div>
        </div>

        <div style={styles.section}>
          <h3 style={styles.label}>Content Whitelist</h3>
          <p style={styles.inputDescription}>
            Comma-separated list of whitelisted items (for future use)
          </p>
          <input
            type="text"
            value={whitelistInput}
            onChange={(e) => setWhitelistInput(e.target.value)}
            placeholder="e.g., user123, topic456"
            style={styles.input}
            disabled={!settings.enabled}
          />
        </div>

        <div style={styles.buttonGroup}>
          <button
            onClick={handleSave}
            disabled={saving}
            style={{
              ...styles.button,
              ...(saving ? styles.buttonDisabled : {}),
            }}
          >
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
        </div>
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
    maxWidth: '800px',
    margin: '0 auto',
    padding: '20px',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
    padding: '32px',
  },
  title: {
    fontSize: '24px',
    fontWeight: 'bold',
    marginBottom: '8px',
    color: '#333',
  },
  description: {
    fontSize: '14px',
    color: '#666',
    marginBottom: '32px',
  },
  section: {
    marginBottom: '24px',
    paddingBottom: '24px',
    borderBottom: '1px solid #eee',
  },
  toggleRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  toggleLabel: {
    fontSize: '16px',
    fontWeight: '500',
    color: '#333',
    marginBottom: '4px',
  },
  toggleDescription: {
    fontSize: '14px',
    color: '#666',
    margin: 0,
  },
  switch: {
    position: 'relative',
    display: 'inline-block',
    width: '60px',
    height: '34px',
  },
  checkbox: {
    opacity: 0,
    width: 0,
    height: 0,
  },
  slider: {
    position: 'absolute',
    cursor: 'pointer',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: '#ccc',
    transition: '0.4s',
    borderRadius: '34px',
  },
  label: {
    fontSize: '16px',
    fontWeight: '500',
    color: '#333',
    marginBottom: '8px',
  },
  inputDescription: {
    fontSize: '14px',
    color: '#666',
    marginBottom: '8px',
  },
  input: {
    width: '100%',
    padding: '12px',
    fontSize: '14px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    boxSizing: 'border-box',
  },
  buttonGroup: {
    display: 'flex',
    justifyContent: 'flex-end',
    marginTop: '24px',
  },
  button: {
    backgroundColor: '#007bff',
    color: 'white',
    padding: '12px 32px',
    fontSize: '16px',
    fontWeight: '500',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    transition: 'background-color 0.2s',
  },
  buttonDisabled: {
    backgroundColor: '#cccccc',
    cursor: 'not-allowed',
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
  success: {
    backgroundColor: '#f1f8f4',
    color: '#2e7d32',
    padding: '12px',
    borderRadius: '4px',
    marginBottom: '16px',
    fontSize: '14px',
    border: '1px solid #c8e6c9',
  },
  loading: {
    textAlign: 'center',
    padding: '40px',
    fontSize: '16px',
    color: '#666',
  },
};

export default ParentalControls;
