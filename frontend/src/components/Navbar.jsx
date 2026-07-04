import React from 'react';
import { useAuth } from '../hooks/useAuth';

const Navbar = () => {
  const { user, logout } = useAuth();

  return (
    <nav style={{ backgroundColor: 'var(--color-bg-surface)', borderBottom: '1px solid var(--color-border)' }}>
      <div className="container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', height: '64px' }}>
        <h2 style={{ margin: 0, color: 'var(--color-primary)' }}>TaskApp</h2>
        
        {user && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div>
              <span style={{ fontWeight: 500 }}>{user.name || user.email}</span>
              <span style={{ 
                marginLeft: '0.5rem', 
                padding: '0.125rem 0.5rem', 
                backgroundColor: 'var(--color-bg-subtle)', 
                borderRadius: '999px',
                fontSize: '0.75rem',
                fontWeight: 600
              }}>
                {user.role}
              </span>
            </div>
            <button onClick={logout} className="btn-outline">Logout</button>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
