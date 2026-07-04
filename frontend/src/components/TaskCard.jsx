import React from 'react';

const getStatusColor = (status) => {
  switch(status) {
    case 'TODO': return { bg: '#e0e7ff', text: '#3730a3' }; // Indigo
    case 'IN_PROGRESS': return { bg: '#fef3c7', text: '#92400e' }; // Amber
    case 'DONE': return { bg: '#d1fae5', text: '#065f46' }; // Emerald
    default: return { bg: '#f3f4f6', text: '#374151' };
  }
};

const getPriorityColor = (priority) => {
  switch(priority) {
    case 'HIGH': return { bg: '#fee2e2', text: '#991b1b' }; // Red
    case 'MEDIUM': return { bg: '#fef3c7', text: '#92400e' }; // Amber
    case 'LOW': return { bg: '#d1fae5', text: '#065f46' }; // Emerald
    default: return { bg: '#f3f4f6', text: '#374151' };
  }
};

const Badge = ({ label, colorConfig }) => (
  <span style={{
    backgroundColor: colorConfig.bg,
    color: colorConfig.text,
    padding: '0.25rem 0.5rem',
    borderRadius: '999px',
    fontSize: '0.75rem',
    fontWeight: 600,
    whiteSpace: 'nowrap'
  }}>
    {label}
  </span>
);

const TaskCard = ({ task, onClick }) => {
  return (
    <div 
      onClick={() => onClick(task)}
      style={{
        backgroundColor: 'var(--color-bg-surface)',
        borderRadius: 'var(--radius-lg)',
        padding: '1.25rem',
        boxShadow: 'var(--shadow-sm)',
        border: '1px solid var(--color-border)',
        cursor: 'pointer',
        transition: 'transform 0.2s, box-shadow 0.2s',
        display: 'flex',
        flexDirection: 'column',
        gap: '0.75rem'
      }}
      onMouseOver={(e) => { e.currentTarget.style.boxShadow = 'var(--shadow-md)'; e.currentTarget.style.transform = 'translateY(-2px)'; }}
      onMouseOut={(e) => { e.currentTarget.style.boxShadow = 'var(--shadow-sm)'; e.currentTarget.style.transform = 'none'; }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <h3 style={{ margin: 0, fontSize: '1.125rem' }}>{task.title}</h3>
      </div>
      
      <p style={{ margin: 0, color: 'var(--color-text-muted)', fontSize: '0.875rem', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
        {task.description || 'No description provided.'}
      </p>
      
      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
        <Badge label={task.status.replace('_', ' ')} colorConfig={getStatusColor(task.status)} />
        <Badge label={task.priority} colorConfig={getPriorityColor(task.priority)} />
      </div>
      
      <div style={{ marginTop: 'auto', paddingTop: '0.75rem', borderTop: '1px solid var(--color-bg-subtle)', display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
        <div>
          <strong>Assignee:</strong> {task.assignedTo ? task.assignedTo.name || task.assignedTo.email : 'Unassigned'}
        </div>
        {task.dueDate && (
          <div>
            <strong>Due:</strong> {new Date(task.dueDate).toLocaleDateString()}
          </div>
        )}
      </div>
    </div>
  );
};

export default TaskCard;
