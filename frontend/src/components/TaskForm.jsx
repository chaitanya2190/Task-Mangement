import React, { useState, useContext } from 'react';
import { useAuth } from '../hooks/useAuth';
import { TaskContext } from '../context/TaskContext';

const TaskForm = ({ initialTask, onClose }) => {
  const { user } = useAuth();
  const { createTask, updateTask } = useContext(TaskContext);
  
  const isEdit = !!initialTask;
  
  // Role checks
  const isAdmin = user?.role === 'ADMIN';
  const isCreator = isEdit && initialTask?.createdBy?.id === user?.id;
  const isAssignee = isEdit && initialTask?.assignedTo?.id === user?.id;
  
  // pureAssignee means they can only edit status and description
  const pureAssignee = isEdit && !isAdmin && !isCreator && isAssignee;

  // Initialize form state
  const [formData, setFormData] = useState({
    title: initialTask?.title || '',
    description: initialTask?.description || '',
    status: initialTask?.status || 'TODO',
    priority: initialTask?.priority || 'MEDIUM',
    dueDate: initialTask?.dueDate ? initialTask.dueDate.substring(0, 16) : '', // format for datetime-local
    assignedToId: initialTask?.assignedTo?.id || ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    const payload = {
      ...formData,
      assignedToId: formData.assignedToId ? parseInt(formData.assignedToId) : null,
      dueDate: formData.dueDate ? new Date(formData.dueDate).toISOString() : null
    };

    try {
      if (isEdit) {
        await updateTask(initialTask.id, payload);
      } else {
        await createTask(payload);
      }
      onClose(); // Close modal on success
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="text-danger" style={{ marginBottom: '1rem', backgroundColor: '#fef2f2', padding: '0.5rem', borderRadius: '4px' }}>{error}</div>}
      
      {pureAssignee && (
        <div style={{ marginBottom: '1rem', padding: '0.75rem', backgroundColor: '#eff6ff', color: '#1e40af', borderRadius: '4px', fontSize: '0.875rem' }}>
          <strong>Note:</strong> You are the assignee of this task. You can only update the status and description.
        </div>
      )}

      <div className="form-group">
        <label className="form-label">Title</label>
        <input 
          type="text" 
          name="title" 
          className="form-input" 
          value={formData.title} 
          onChange={handleChange} 
          required 
          disabled={pureAssignee}
        />
      </div>
      
      <div className="form-group">
        <label className="form-label">Description</label>
        <textarea 
          name="description" 
          className="form-input" 
          rows="3" 
          value={formData.description} 
          onChange={handleChange} 
        />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
        <div className="form-group">
          <label className="form-label">Status</label>
          <select name="status" className="form-input" value={formData.status} onChange={handleChange}>
            <option value="TODO">TODO</option>
            <option value="IN_PROGRESS">IN PROGRESS</option>
            <option value="DONE">DONE</option>
          </select>
        </div>

        <div className="form-group">
          <label className="form-label">Priority</label>
          <select 
            name="priority" 
            className="form-input" 
            value={formData.priority} 
            onChange={handleChange}
            disabled={pureAssignee}
          >
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
          </select>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
        <div className="form-group">
          <label className="form-label">Due Date</label>
          <input 
            type="datetime-local" 
            name="dueDate" 
            className="form-input" 
            value={formData.dueDate} 
            onChange={handleChange}
            disabled={pureAssignee}
          />
        </div>

        <div className="form-group">
          <label className="form-label">Assignee ID (Optional)</label>
          <input 
            type="number" 
            name="assignedToId" 
            className="form-input" 
            value={formData.assignedToId} 
            onChange={handleChange}
            disabled={pureAssignee && !isAdmin && !isCreator} // Only Admin/Creator can reassign
          />
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
        <button type="button" className="btn-outline" onClick={onClose} disabled={loading}>
          Cancel
        </button>
        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Saving...' : (isEdit ? 'Update Task' : 'Create Task')}
        </button>
      </div>
    </form>
  );
};

export default TaskForm;
