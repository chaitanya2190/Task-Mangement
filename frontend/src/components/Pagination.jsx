import React from 'react';

const Pagination = ({ page, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', marginTop: '2rem' }}>
      <button 
        className="btn-outline" 
        disabled={page === 0} 
        onClick={() => onPageChange(page - 1)}
      >
        Previous
      </button>
      
      <span style={{ fontSize: '0.875rem', fontWeight: 500 }}>
        Page {page + 1} of {totalPages}
      </span>
      
      <button 
        className="btn-outline" 
        disabled={page >= totalPages - 1} 
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </button>
    </div>
  );
};

export default Pagination;
