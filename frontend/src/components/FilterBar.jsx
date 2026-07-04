import React from 'react';
import { useAuth } from '../hooks/useAuth';

const FilterBar = ({ filters, onFilterChange }) => {
  const { user } = useAuth();

  const handleChange = (e) => {
    const { name, value } = e.target;
    onFilterChange({ ...filters, [name]: value });
  };

  return (
    <div className="filter-bar" style={{ 
      padding: '1rem', 
      backgroundColor: 'var(--color-bg-surface)', 
      borderRadius: 'var(--radius-lg)', 
      border: '1px solid var(--color-border)',
      marginBottom: '1.5rem'
    }}>
      <div style={{ flex: '1 1 200px' }}>
        <label className="form-label" htmlFor="statusFilter">Status</label>
        <select 
          id="statusFilter" 
          name="status" 
          className="form-input" 
          value={filters.status || ''} 
          onChange={handleChange}
        >
          <option value="">All</option>
          <option value="TODO">TODO</option>
          <option value="IN_PROGRESS">IN PROGRESS</option>
          <option value="DONE">DONE</option>
        </select>
      </div>

      <div style={{ flex: '1 1 200px' }}>
        <label className="form-label" htmlFor="priorityFilter">Priority</label>
        <select 
          id="priorityFilter" 
          name="priority" 
          className="form-input" 
          value={filters.priority || ''} 
          onChange={handleChange}
        >
          <option value="">All</option>
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
        </select>
      </div>

      {user?.role === 'ADMIN' && (
        <div style={{ flex: '1 1 200px' }}>
          <label className="form-label" htmlFor="assigneeFilter">Assignee ID</label>
          <input 
            id="assigneeFilter" 
            name="assignedToId" 
            type="number"
            placeholder="e.g. 1"
            className="form-input" 
            value={filters.assignedToId || ''} 
            onChange={handleChange}
          />
        </div>
      )}
    </div>
  );
};

export default FilterBar;
