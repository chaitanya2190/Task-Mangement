import { useContext, useEffect } from 'react';
import { TaskContext } from '../context/TaskContext';

export const useTasks = (filters, page) => {
  const context = useContext(TaskContext);
  
  if (!context) {
    throw new Error('useTasks must be used within a TaskProvider');
  }

  const { fetchTasks } = context;

  // Refetch when filters or page change
  useEffect(() => {
    fetchTasks(filters, page);
  }, [filters, page, fetchTasks]);

  return context;
};
