import React, { createContext, useState, useCallback, useMemo } from 'react';
import { useFetch } from '../hooks/useFetch';

export const TaskContext = createContext(null);

export const TaskProvider = ({ children }) => {
  const [tasks, setTasks] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  
  // We use useFetch for actual API requests to handle loading/error automatically
  const { request, loading, error, setError } = useFetch();

  const fetchTasks = useCallback(async (filters, page) => {
    try {
      // Clean up filters to remove empty strings or nulls
      const params = { page, size: 10 };
      if (filters.status) params.status = filters.status;
      if (filters.priority) params.priority = filters.priority;
      if (filters.assignedToId) params.assignedToId = filters.assignedToId;

      const data = await request({
        url: '/tasks',
        method: 'GET',
        params
      });
      setTasks(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      // error is handled by useFetch
    }
  }, [request]);

  const createTask = useCallback(async (taskData) => {
    const data = await request({
      url: '/tasks',
      method: 'POST',
      data: taskData
    });
    // Optimistic append, but usually a re-fetch is better for pagination.
    // We'll let the component caller decide if they want to re-fetch.
    return data;
  }, [request]);

  const updateTask = useCallback(async (id, taskData) => {
    const data = await request({
      url: `/tasks/${id}`,
      method: 'PUT',
      data: taskData
    });
    // Update local state to avoid full refetch
    setTasks(prev => prev.map(t => t.id === id ? data : t));
    return data;
  }, [request]);

  const deleteTask = useCallback(async (id) => {
    await request({
      url: `/tasks/${id}`,
      method: 'DELETE'
    });
    setTasks(prev => prev.filter(t => t.id !== id));
  }, [request]);

  const value = useMemo(() => ({
    tasks,
    totalPages,
    loading,
    error,
    setError,
    fetchTasks,
    createTask,
    updateTask,
    deleteTask
  }), [tasks, totalPages, loading, error, setError, fetchTasks, createTask, updateTask, deleteTask]);

  return (
    <TaskContext.Provider value={value}>
      {children}
    </TaskContext.Provider>
  );
};
