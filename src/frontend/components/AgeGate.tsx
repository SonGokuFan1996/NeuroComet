// React (TSX) AgeGate component — prompt for birthdate if missing and show tailored content route.
// Adapt to your frontend stack, routing, state management (Redux, Context, etc.)
import React, { useState } from 'react';

type Props = {
  user: any;
  saveBirthdate: (bd: string) => Promise<void>;
};

export default function AgeGate({ user, saveBirthdate }: Props) {
  const [birthdate, setBirthdate] = useState('');
  const [error, setError] = useState<string | null>(null);

  const submit = async () => {
    try {
      setError(null);
      // basic validation
      if (!birthdate) {
        setError('Please enter your birthdate');
        return;
      }
      await saveBirthdate(birthdate);
      // refresh or redirect — implement as needed
      window.location.reload();
    } catch (e: any) {
      setError(e.message || 'Failed to save');
    }
  };

  return (
    <div className="age-gate">
      <h2>Confirm your age</h2>
      <p>We ask for your birthdate to provide age-appropriate content and parental control options.</p>
      <input
        type="date"
        value={birthdate}
        onChange={e => setBirthdate(e.target.value)}
        aria-label="birthdate"
      />
      {error && <div className="error">{error}</div>}
      <button onClick={submit}>Continue</button>
    </div>
  );
}
