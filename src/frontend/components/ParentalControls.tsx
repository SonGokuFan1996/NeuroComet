// React component for parent to manage child settings. Replace API calls with your client's API helpers.
import React, { useEffect, useState } from 'react';
import axios from 'axios';

export default function ParentalControls({ childId }: { childId: string }) {
  const [settings, setSettings] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      const res = await axios.get(`/api/parental-controls/${childId}`);
      setSettings(res.data.settings ?? {});
      setLoading(false);
    }
    load();
  }, [childId]);

  const save = async () => {
    await axios.post(`/api/parental-controls/${childId}`, settings);
    alert('Saved');
  };

  if (loading) return <div>Loading parental controls...</div>;

  return (
    <div>
      <h3>Parental Controls for Child #{childId}</h3>
      <label>
        <input
          type="checkbox"
          checked={settings.enabled}
          onChange={e => setSettings({ ...settings, enabled: e.target.checked })}
        /> Enable parental controls
      </label>
      <div>
        <label>
          <input
            type="checkbox"
            checked={settings.allow_chat}
            onChange={e => setSettings({ ...settings, allow_chat: e.target.checked })}
          /> Allow chat
        </label>
      </div>
      <div>
        <label>
          Whitelist (comma-separated IDs)
          <input
            value={(settings.whitelist || []).join(',')}
            onChange={e => setSettings({ ...settings, whitelist: e.target.value.split(',').map(s=>s.trim()).filter(Boolean) })}
          />
        </label>
      </div>
      <button onClick={save}>Save</button>
    </div>
  );
}
