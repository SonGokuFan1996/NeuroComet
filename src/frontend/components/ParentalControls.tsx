import React, { useState, useEffect } from 'react';

interface ParentalControlsData {
  userId: string;
  parentId: string;
  contentRestrictionLevel: 'strict' | 'moderate' | 'permissive';
  allowedContentTypes: string[];
  screenTimeLimit: number;
  requireApproval: boolean;
}

interface ParentalControlsProps {
  childId: string;
  onSave?: (controls: ParentalControlsData) => void;
}

const CONTENT_CATEGORIES = [
  { id: 'educational', label: 'Educational' },
  { id: 'entertainment', label: 'Entertainment' },
  { id: 'social', label: 'Social' },
  { id: 'gaming', label: 'Gaming' },
  { id: 'news', label: 'News' },
  { id: 'sports', label: 'Sports' },
  { id: 'music', label: 'Music' },
  { id: 'art', label: 'Art' },
];

/**
 * ParentalControls component with improved UX and whitelist UI
 * Allows parents to manage content restrictions for their children
 */
export const ParentalControls: React.FC<ParentalControlsProps> = ({ childId, onSave }) => {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  
  const [controls, setControls] = useState<ParentalControlsData>({
    userId: childId,
    parentId: '',
    contentRestrictionLevel: 'strict',
    allowedContentTypes: ['educational'],
    screenTimeLimit: 120,
    requireApproval: true,
  });

  /**
   * Load existing parental controls
   */
  useEffect(() => {
    const loadControls = async () => {
      setLoading(true);
      setError('');

      try {
        const response = await fetch(`/api/parental-controls/${childId}`);
        
        if (!response.ok) {
          const data = await response.json();
          throw new Error(data.error || 'Failed to load parental controls');
        }

        const data = await response.json();
        setControls(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'An error occurred');
      } finally {
        setLoading(false);
      }
    };

    loadControls();
  }, [childId]);

  /**
   * Handle form submission
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    setSaving(true);

    try {
      // Validate screen time limit
      if (controls.screenTimeLimit < 0) {
        throw new Error('Screen time limit must be a positive number');
      }

      // Validate at least one content type is selected
      if (controls.allowedContentTypes.length === 0) {
        throw new Error('Please select at least one allowed content type');
      }

      const response = await fetch(`/api/parental-controls/${childId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(controls),
      });

      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.error || data.details?.join(', ') || 'Failed to save parental controls');
      }

      const data = await response.json();
      
      if (data.success) {
        setSuccessMessage('Parental controls updated successfully');
        
        // Call onSave callback if provided
        if (onSave) {
          onSave(data.controls);
        }
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setSaving(false);
    }
  };

  /**
   * Handle content type checkbox change
   */
  const handleContentTypeChange = (categoryId: string, checked: boolean) => {
    setControls(prev => ({
      ...prev,
      allowedContentTypes: checked
        ? [...prev.allowedContentTypes, categoryId]
        : prev.allowedContentTypes.filter(id => id !== categoryId),
    }));
  };

  /**
   * Handle restriction level change
   */
  const handleRestrictionLevelChange = (level: 'strict' | 'moderate' | 'permissive') => {
    setControls(prev => ({
      ...prev,
      contentRestrictionLevel: level,
    }));
  };

  if (loading) {
    return (
      <div className="parental-controls-container">
        <div className="loading-state" role="status" aria-live="polite">
          <div className="spinner" aria-hidden="true"></div>
          <p>Loading parental controls...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="parental-controls-container">
      <div className="parental-controls-content">
        <h1 className="page-title">Parental Controls</h1>
        <p className="page-description">
          Configure content restrictions and screen time limits for your child's account.
        </p>

        {error && (
          <div className="alert alert-error" role="alert">
            <strong>Error:</strong> {error}
          </div>
        )}

        {successMessage && (
          <div className="alert alert-success" role="alert">
            <strong>Success:</strong> {successMessage}
          </div>
        )}

        <form onSubmit={handleSubmit} className="controls-form">
          {/* Content Restriction Level */}
          <div className="form-section">
            <h2 className="section-title">Content Restriction Level</h2>
            <p className="section-description">
              Choose how strictly content should be filtered
            </p>

            <div className="radio-group">
              <label className="radio-label">
                <input
                  type="radio"
                  name="restrictionLevel"
                  value="strict"
                  checked={controls.contentRestrictionLevel === 'strict'}
                  onChange={() => handleRestrictionLevelChange('strict')}
                  disabled={saving}
                />
                <div className="radio-content">
                  <strong>Strict</strong>
                  <p>Maximum filtering, suitable for young children</p>
                </div>
              </label>

              <label className="radio-label">
                <input
                  type="radio"
                  name="restrictionLevel"
                  value="moderate"
                  checked={controls.contentRestrictionLevel === 'moderate'}
                  onChange={() => handleRestrictionLevelChange('moderate')}
                  disabled={saving}
                />
                <div className="radio-content">
                  <strong>Moderate</strong>
                  <p>Balanced filtering, suitable for older children</p>
                </div>
              </label>

              <label className="radio-label">
                <input
                  type="radio"
                  name="restrictionLevel"
                  value="permissive"
                  checked={controls.contentRestrictionLevel === 'permissive'}
                  onChange={() => handleRestrictionLevelChange('permissive')}
                  disabled={saving}
                />
                <div className="radio-content">
                  <strong>Permissive</strong>
                  <p>Light filtering, suitable for mature children</p>
                </div>
              </label>
            </div>
          </div>

          {/* Allowed Content Types */}
          <div className="form-section">
            <h2 className="section-title">Allowed Content Types</h2>
            <p className="section-description">
              Select which types of content your child can access
            </p>

            <div className="checkbox-group">
              {CONTENT_CATEGORIES.map(category => (
                <label key={category.id} className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={controls.allowedContentTypes.includes(category.id)}
                    onChange={(e) => handleContentTypeChange(category.id, e.target.checked)}
                    disabled={saving}
                  />
                  <span>{category.label}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Screen Time Limit */}
          <div className="form-section">
            <h2 className="section-title">Screen Time Limit</h2>
            <p className="section-description">
              Set daily screen time limit in minutes (0 for unlimited)
            </p>

            <div className="input-group">
              <input
                type="number"
                min="0"
                max="1440"
                step="15"
                value={controls.screenTimeLimit}
                onChange={(e) => setControls(prev => ({ 
                  ...prev, 
                  screenTimeLimit: parseInt(e.target.value) || 0 
                }))}
                disabled={saving}
                className="form-input"
                aria-label="Screen time limit in minutes"
              />
              <span className="input-suffix">minutes</span>
            </div>
            <p className="help-text">
              {controls.screenTimeLimit === 0 
                ? 'No screen time limit set' 
                : `${Math.floor(controls.screenTimeLimit / 60)}h ${controls.screenTimeLimit % 60}m per day`}
            </p>
          </div>

          {/* Require Approval */}
          <div className="form-section">
            <label className="checkbox-label-block">
              <input
                type="checkbox"
                checked={controls.requireApproval}
                onChange={(e) => setControls(prev => ({ 
                  ...prev, 
                  requireApproval: e.target.checked 
                }))}
                disabled={saving}
              />
              <div>
                <strong>Require parental approval</strong>
                <p className="checkbox-description">
                  Child must request approval before accessing restricted content
                </p>
              </div>
            </label>
          </div>

          {/* Submit Button */}
          <div className="form-actions">
            <button
              type="submit"
              className="submit-button"
              disabled={saving}
              aria-busy={saving}
            >
              {saving ? 'Saving...' : 'Save Parental Controls'}
            </button>
          </div>
        </form>
      </div>

      <style jsx>{`
        .parental-controls-container {
          max-width: 800px;
          margin: 0 auto;
          padding: 20px;
        }

        .loading-state {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 60px 20px;
          color: #666;
        }

        .spinner {
          width: 40px;
          height: 40px;
          border: 4px solid #f3f3f3;
          border-top: 4px solid #007bff;
          border-radius: 50%;
          animation: spin 1s linear infinite;
          margin-bottom: 16px;
        }

        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }

        .parental-controls-content {
          background: white;
          border-radius: 8px;
          padding: 40px;
          box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .page-title {
          font-size: 28px;
          font-weight: 600;
          margin-bottom: 8px;
          color: #333;
        }

        .page-description {
          font-size: 14px;
          color: #666;
          margin-bottom: 24px;
        }

        .alert {
          padding: 12px 16px;
          border-radius: 4px;
          margin-bottom: 20px;
          font-size: 14px;
        }

        .alert-error {
          background-color: #f8d7da;
          border: 1px solid #f5c6cb;
          color: #721c24;
        }

        .alert-success {
          background-color: #d4edda;
          border: 1px solid #c3e6cb;
          color: #155724;
        }

        .controls-form {
          margin-top: 20px;
        }

        .form-section {
          margin-bottom: 32px;
          padding-bottom: 32px;
          border-bottom: 1px solid #e9ecef;
        }

        .form-section:last-of-type {
          border-bottom: none;
        }

        .section-title {
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 8px;
          color: #333;
        }

        .section-description {
          font-size: 14px;
          color: #666;
          margin-bottom: 16px;
        }

        .radio-group {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .radio-label {
          display: flex;
          align-items: flex-start;
          padding: 16px;
          border: 2px solid #e9ecef;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.2s;
        }

        .radio-label:hover {
          border-color: #007bff;
          background-color: #f8f9fa;
        }

        .radio-label input[type="radio"] {
          margin-right: 12px;
          margin-top: 2px;
          cursor: pointer;
        }

        .radio-content {
          flex: 1;
        }

        .radio-content p {
          margin: 4px 0 0 0;
          font-size: 14px;
          color: #666;
        }

        .checkbox-group {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
          gap: 12px;
        }

        .checkbox-label {
          display: flex;
          align-items: center;
          cursor: pointer;
        }

        .checkbox-label input[type="checkbox"] {
          margin-right: 8px;
          cursor: pointer;
        }

        .checkbox-label-block {
          display: flex;
          align-items: flex-start;
          cursor: pointer;
        }

        .checkbox-label-block input[type="checkbox"] {
          margin-right: 12px;
          margin-top: 2px;
          cursor: pointer;
        }

        .checkbox-description {
          margin: 4px 0 0 0;
          font-size: 14px;
          color: #666;
        }

        .input-group {
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .form-input {
          padding: 12px;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 16px;
          width: 150px;
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

        .input-suffix {
          color: #666;
          font-size: 14px;
        }

        .help-text {
          margin-top: 8px;
          font-size: 14px;
          color: #666;
        }

        .form-actions {
          margin-top: 32px;
          display: flex;
          justify-content: flex-end;
        }

        .submit-button {
          padding: 12px 32px;
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
      `}</style>
    </div>
  );
};

export default ParentalControls;
