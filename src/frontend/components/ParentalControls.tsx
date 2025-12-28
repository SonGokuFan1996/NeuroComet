import React, { useState, useEffect } from 'react';

interface ParentalControlsSettings {
  user_id: number;
  enabled: boolean;
  allow_chat: boolean;
  allow_profile_public: boolean;
  whitelist: string[];
}

interface ParentalControlsProps {
  /**
   * The child user ID whose parental controls are being managed
   */
  childId: number;
  
  /**
   * Optional base API URL (defaults to '/api')
   */
  apiBaseUrl?: string;
}

/**
 * ParentalControls Component
 * 
 * Allows parents to view and update parental control settings for their children.
 * Loads settings from GET /api/parental-controls/:childId
 * Updates settings via POST /api/parental-controls/:childId
 * 
 * Usage:
 * ```tsx
 * <ParentalControls childId={123} />
 * ```
 */
const ParentalControls: React.FC<ParentalControlsProps> = ({ 
  childId, 
  apiBaseUrl = '/api' 
}) => {
  const [settings, setSettings] = useState<ParentalControlsSettings>({
    user_id: childId,
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

  // Load parental controls on mount
  useEffect(() => {
    loadParentalControls();
  }, [childId]);

  const loadParentalControls = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await fetch(`${apiBaseUrl}/parental-controls/${childId}`, {
        credentials: 'include', // Include cookies for authentication
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || 'Failed to load parental controls');
      }

      const data = await response.json();
      setSettings(data);
      setWhitelistInput(data.whitelist.join(', '));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load settings');
      console.error('Error loading parental controls:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccessMessage('');

    // Parse whitelist from comma-separated input
    const whitelist = whitelistInput
      .split(',')
      .map((item) => item.trim())
      .filter((item) => item.length > 0);

    const updatedSettings = {
      ...settings,
      whitelist,
    };

    try {
      const response = await fetch(`${apiBaseUrl}/parental-controls/${childId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(updatedSettings),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || 'Failed to update parental controls');
      }

      const data = await response.json();
      setSettings(data);
      setSuccessMessage('Settings saved successfully!');
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save settings');
      console.error('Error updating parental controls:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleToggle = (field: keyof ParentalControlsSettings) => {
    if (typeof settings[field] === 'boolean') {
      setSettings((prev) => ({
        ...prev,
        [field]: !prev[field],
      }));
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
      <h2 style={styles.title}>Parental Controls</h2>
      <p style={styles.subtitle}>
        Manage safety settings for child user ID: {childId}
      </p>

      {error && (
        <div style={styles.error} role="alert">
          {error}
        </div>
      )}

      {successMessage && (
        <div style={styles.success} role="status">
          {successMessage}
        </div>
      )}

      <div style={styles.settingsContainer}>
        {/* Enabled toggle */}
        <div style={styles.settingRow}>
          <div style={styles.settingInfo}>
            <label style={styles.settingLabel}>Parental Controls Enabled</label>
            <p style={styles.settingDescription}>
              Master switch for all parental control features
            </p>
          </div>
          <label style={styles.switch}>
            <input
              type="checkbox"
              checked={settings.enabled}
              onChange={() => handleToggle('enabled')}
              style={styles.checkbox}
            />
            <span style={styles.slider}></span>
          </label>
        </div>

        {/* Allow chat toggle */}
        <div style={styles.settingRow}>
          <div style={styles.settingInfo}>
            <label style={styles.settingLabel}>Allow Chat</label>
            <p style={styles.settingDescription}>
              Allow this child to use chat and messaging features
            </p>
          </div>
          <label style={styles.switch}>
            <input
              type="checkbox"
              checked={settings.allow_chat}
              onChange={() => handleToggle('allow_chat')}
              disabled={!settings.enabled}
              style={styles.checkbox}
            />
            <span style={styles.slider}></span>
          </label>
        </div>

        {/* Allow profile public toggle */}
        <div style={styles.settingRow}>
          <div style={styles.settingInfo}>
            <label style={styles.settingLabel}>Public Profile</label>
            <p style={styles.settingDescription}>
              Allow this child's profile to be publicly visible
            </p>
          </div>
          <label style={styles.switch}>
            <input
              type="checkbox"
              checked={settings.allow_profile_public}
              onChange={() => handleToggle('allow_profile_public')}
              disabled={!settings.enabled}
              style={styles.checkbox}
            />
            <span style={styles.slider}></span>
          </label>
        </div>

        {/* Whitelist input */}
        <div style={styles.settingRow}>
          <div style={styles.settingInfo}>
            <label htmlFor="whitelist" style={styles.settingLabel}>
              Whitelisted Contacts
            </label>
            <p style={styles.settingDescription}>
              Comma-separated list of allowed user IDs or usernames
            </p>
          </div>
        </div>
        <input
          id="whitelist"
          type="text"
          value={whitelistInput}
          onChange={(e) => setWhitelistInput(e.target.value)}
          placeholder="user123, user456, user789"
          disabled={!settings.enabled}
          style={styles.whitelistInput}
        />
      </div>

      <button
        onClick={handleSave}
        disabled={saving}
        style={{
          ...styles.saveButton,
          ...(saving ? styles.saveButtonDisabled : {}),
        }}
      >
        {saving ? 'Saving...' : 'Save Changes'}
      </button>
    </div>
  );
};

// Inline styles
const styles: { [key: string]: React.CSSProperties } = {
  container: {
    maxWidth: '800px',
    margin: '0 auto',
    padding: '24px',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    marginBottom: '8px',
    color: '#333',
  },
  subtitle: {
    fontSize: '16px',
    color: '#666',
    marginBottom: '24px',
  },
  loading: {
    textAlign: 'center',
    fontSize: '16px',
    color: '#666',
    padding: '40px',
  },
  error: {
    color: '#dc3545',
    backgroundColor: '#f8d7da',
    border: '1px solid #f5c6cb',
    borderRadius: '4px',
    padding: '12px 16px',
    marginBottom: '20px',
  },
  success: {
    color: '#155724',
    backgroundColor: '#d4edda',
    border: '1px solid #c3e6cb',
    borderRadius: '4px',
    padding: '12px 16px',
    marginBottom: '20px',
  },
  settingsContainer: {
    backgroundColor: '#f8f9fa',
    borderRadius: '8px',
    padding: '24px',
    marginBottom: '24px',
  },
  settingRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingBottom: '16px',
    marginBottom: '16px',
    borderBottom: '1px solid #dee2e6',
  },
  settingInfo: {
    flex: 1,
  },
  settingLabel: {
    fontSize: '16px',
    fontWeight: '600',
    color: '#333',
    display: 'block',
    marginBottom: '4px',
  },
  settingDescription: {
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
  whitelistInput: {
    width: '100%',
    padding: '10px 12px',
    fontSize: '14px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    boxSizing: 'border-box',
    fontFamily: 'monospace',
  },
  saveButton: {
    padding: '12px 24px',
    fontSize: '16px',
    fontWeight: '600',
    color: '#ffffff',
    backgroundColor: '#28a745',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    transition: 'background-color 0.2s',
  },
  saveButtonDisabled: {
    backgroundColor: '#6c757d',
    cursor: 'not-allowed',
  },
};

export default ParentalControls;
