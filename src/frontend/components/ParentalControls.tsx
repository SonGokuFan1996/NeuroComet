import React, { useState, useEffect } from 'react';

interface ParentalControlsProps {
  childId: number;
}

interface ParentalControlsData {
  user_id: number;
  enabled: boolean;
  allow_chat: boolean;
  allow_profile_public: boolean;
  whitelist: string[];
}

/**
 * ParentalControls Component
 * 
 * Allows parents to manage parental control settings for their children.
 * Loads current settings from the API and provides UI to update them.
 */
export const ParentalControls: React.FC<ParentalControlsProps> = ({ childId }) => {
  const [settings, setSettings] = useState<ParentalControlsData>({
    user_id: childId,
    enabled: true,
    allow_chat: false,
    allow_profile_public: false,
    whitelist: [],
  });
  const [whitelistInput, setWhitelistInput] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSaving, setIsSaving] = useState<boolean>(false);
  const [error, setError] = useState<string>('');
  const [successMessage, setSuccessMessage] = useState<string>('');

  // Load parental controls on component mount
  useEffect(() => {
    loadParentalControls();
  }, [childId]);

  const loadParentalControls = async () => {
    setIsLoading(true);
    setError('');

    try {
      const response = await fetch(`/api/parental-controls/${childId}`, {
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to load parental controls');
      }

      const data = await response.json();
      setSettings(data);
      setWhitelistInput(data.whitelist.join(', '));
    } catch (err) {
      setError('Failed to load parental controls. Please try again.');
      console.error('Error loading parental controls:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSave = async () => {
    setIsSaving(true);
    setError('');
    setSuccessMessage('');

    // Parse whitelist from comma-separated input
    const whitelist = whitelistInput
      .split(',')
      .map(item => item.trim())
      .filter(item => item.length > 0);

    const updatedSettings = {
      ...settings,
      whitelist,
    };

    try {
      const response = await fetch(`/api/parental-controls/${childId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(updatedSettings),
      });

      if (!response.ok) {
        throw new Error('Failed to update parental controls');
      }

      const data = await response.json();
      setSettings(data);
      setSuccessMessage('Parental controls updated successfully!');
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      setError('Failed to save parental controls. Please try again.');
      console.error('Error updating parental controls:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const toggleSetting = (key: keyof ParentalControlsData) => {
    if (typeof settings[key] === 'boolean') {
      setSettings({
        ...settings,
        [key]: !settings[key],
      });
    }
  };

  if (isLoading) {
    return (
      <div style={styles.container}>
        <div style={styles.card}>
          <p>Loading parental controls...</p>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h2 style={styles.title}>Parental Controls</h2>
        <p style={styles.description}>
          Manage your child's account settings and restrictions.
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
          <label style={styles.toggleLabel}>
            <input
              type="checkbox"
              checked={settings.enabled}
              onChange={() => toggleSetting('enabled')}
              style={styles.checkbox}
            />
            <span style={styles.toggleText}>Enable Parental Controls</span>
          </label>
          <p style={styles.helpText}>
            When enabled, restrictions will be enforced on the child's account.
          </p>
        </div>

        <div style={styles.section}>
          <label style={styles.toggleLabel}>
            <input
              type="checkbox"
              checked={settings.allow_chat}
              onChange={() => toggleSetting('allow_chat')}
              disabled={!settings.enabled}
              style={styles.checkbox}
            />
            <span style={styles.toggleText}>Allow Chat</span>
          </label>
          <p style={styles.helpText}>
            Allow the child to use chat and messaging features.
          </p>
        </div>

        <div style={styles.section}>
          <label style={styles.toggleLabel}>
            <input
              type="checkbox"
              checked={settings.allow_profile_public}
              onChange={() => toggleSetting('allow_profile_public')}
              disabled={!settings.enabled}
              style={styles.checkbox}
            />
            <span style={styles.toggleText}>Allow Public Profile</span>
          </label>
          <p style={styles.helpText}>
            Make the child's profile visible to other users.
          </p>
        </div>

        <div style={styles.section}>
          <label style={styles.label}>
            Approved Usernames/Domains (Whitelist)
          </label>
          <textarea
            value={whitelistInput}
            onChange={(e) => setWhitelistInput(e.target.value)}
            disabled={!settings.enabled}
            placeholder="Enter comma-separated usernames or domains"
            rows={4}
            style={styles.textarea}
          />
          <p style={styles.helpText}>
            List of approved usernames or domains that the child can interact with.
            Separate multiple entries with commas.
          </p>
        </div>

        <button
          onClick={handleSave}
          disabled={isSaving}
          style={{
            ...styles.button,
            ...(isSaving ? styles.buttonDisabled : {}),
          }}
        >
          {isSaving ? 'Saving...' : 'Save Changes'}
        </button>
      </div>
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    display: 'flex',
    justifyContent: 'center',
    padding: '20px',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 2px 10px rgba(0, 0, 0, 0.1)',
    padding: '40px',
    maxWidth: '700px',
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
  section: {
    marginBottom: '25px',
    paddingBottom: '25px',
    borderBottom: '1px solid #eee',
  },
  toggleLabel: {
    display: 'flex',
    alignItems: 'center',
    cursor: 'pointer',
    fontSize: '16px',
    fontWeight: '500',
    color: '#333',
  },
  checkbox: {
    width: '20px',
    height: '20px',
    marginRight: '12px',
    cursor: 'pointer',
  },
  toggleText: {
    userSelect: 'none',
  },
  label: {
    display: 'block',
    fontSize: '14px',
    fontWeight: '500',
    marginBottom: '8px',
    color: '#333',
  },
  textarea: {
    width: '100%',
    padding: '12px',
    fontSize: '14px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    boxSizing: 'border-box',
    fontFamily: 'inherit',
    resize: 'vertical',
  },
  helpText: {
    fontSize: '13px',
    color: '#888',
    marginTop: '8px',
    lineHeight: '1.4',
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
    width: '100%',
  },
  buttonDisabled: {
    backgroundColor: '#ccc',
    cursor: 'not-allowed',
  },
  error: {
    backgroundColor: '#fee',
    color: '#c33',
    padding: '12px',
    borderRadius: '4px',
    marginBottom: '20px',
    fontSize: '14px',
  },
  success: {
    backgroundColor: '#efe',
    color: '#3c3',
    padding: '12px',
    borderRadius: '4px',
    marginBottom: '20px',
    fontSize: '14px',
  },
};
