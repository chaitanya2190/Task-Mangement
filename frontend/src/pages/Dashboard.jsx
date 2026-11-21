import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import FilterBar from '../components/FilterBar';
import TaskCard from '../components/TaskCard';
import Pagination from '../components/Pagination';
import Modal from '../components/Modal';
import TaskForm from '../components/TaskForm';
import { TaskProvider } from '../context/TaskContext';
import { useTasks } from '../hooks/useTasks';
import { useAuth } from '../hooks/useAuth';

const DashboardContent = () => {
  const { user } = useAuth();
  const [filters, setFilters] = useState({ status: '', priority: '', assignedToId: '' });
  const [page, setPage] = useState(0);
  
  // Custom hook that consumes TaskContext and triggers auto-fetch
  const { tasks, totalPages, loading, error, deleteTask, fetchTasks } = useTasks(filters, page);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState(null);

  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
    setPage(0); // Reset to first page on filter change
  };

  const openCreateModal = () => {
    setEditingTask(null);
    setIsModalOpen(true);
  };

  const openEditModal = (task) => {
    setEditingTask(task);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingTask(null);
    fetchTasks(filters, page); // Refetch to show updated data
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      try {
        await deleteTask(id);
        fetchTasks(filters, page); // Refetch list
      } catch (err) {
        alert('Failed to delete task: ' + (err.response?.data?.error || err.message));
      }
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar />
      
      <main className="container" style={{ flex: 1, padding: '2rem 1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
          <h1 style={{ margin: 0 }}>Tasks Dashboard</h1>
          <button className="btn-primary" onClick={openCreateModal}>+ Create Task</button>
        </div>

        <FilterBar filters={filters} onFilterChange={handleFilterChange} />

        {error && (
          <div className="text-danger" style={{ marginBottom: '1rem', backgroundColor: '#fef2f2', padding: '1rem', borderRadius: '4px' }}>
            Failed to load tasks: {error}
          </div>
        )}

        {loading ? (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1rem' }}>
            {[1, 2, 3, 4, 5, 6].map(i => (
              <div key={i} style={{ height: '200px', backgroundColor: 'var(--color-bg-surface)', borderRadius: 'var(--radius-lg)', animation: 'pulse 1.5s infinite ease-in-out' }}></div>
            ))}
          </div>
        ) : tasks.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--color-text-muted)', backgroundColor: 'var(--color-bg-surface)', borderRadius: 'var(--radius-lg)' }}>
            <p>No tasks found matching your filters.</p>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1rem' }}>
            {tasks.map(task => (
              <div key={task.id} style={{ position: 'relative' }}>
                <TaskCard task={task} onClick={() => openEditModal(task)} />
                
                {/* Delete button: Note Backend is source of truth. We only render if Admin or Creator */}
                {(user?.role === 'ADMIN' || task.createdBy?.id === user?.id) && (
                  <button 
                    onClick={(e) => { e.stopPropagation(); handleDelete(task.id); }}
                    style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'none', border: 'none', color: 'var(--color-text-muted)', cursor: 'pointer', padding: '0.25rem' }}
                    title="Delete Task"
                  >
                    🗑️
                  </button>
                )}
              </div>
            ))}
          </div>
        )}

        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </main>

      {isModalOpen && (
        <Modal title={editingTask ? 'Edit Task' : 'Create Task'} onClose={handleCloseModal}>
          <TaskForm initialTask={editingTask} onClose={handleCloseModal} />
        </Modal>
      )}
      
      {/* Pulse animation keyframes injected globally */}
      <style>{`
        @keyframes pulse {
          0%, 100% { opacity: 1; }
          50% { opacity: 0.5; }
        }
      `}</style>
    </div>
  );
};

// Wrap DashboardContent with TaskProvider to provide context
const Dashboard = () => (
  <TaskProvider>
    <DashboardContent />
  </TaskProvider>
);

export default Dashboard;
