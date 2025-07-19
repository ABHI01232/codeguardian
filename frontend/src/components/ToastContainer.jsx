import React, { useState, useCallback } from 'react';
import Toast from './Toast';

const ToastContainer = () => {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((message, type = 'info', duration = 5000, onClick = null) => {
    const id = Date.now() + Math.random();
    const newToast = { id, message, type, duration, onClick };
    
    setToasts(prev => [...prev, newToast]);
    
    return id;
  }, []);

  const removeToast = useCallback((id) => {
    setToasts(prev => prev.filter(toast => toast.id !== id));
  }, []);

  // Global toast function
  window.showToast = addToast;

  return (
    <div className="fixed top-4 right-4 z-50 space-y-2">
      {toasts.map((toast, index) => (
        <div
          key={toast.id}
          style={{ 
            transform: `translateY(${index * 80}px)`,
            zIndex: 1000 - index 
          }}
          className="absolute top-0 right-0"
        >
          <Toast
            message={toast.message}
            type={toast.type}
            duration={toast.duration}
            onClick={toast.onClick}
            onClose={() => removeToast(toast.id)}
          />
        </div>
      ))}
    </div>
  );
};

export default ToastContainer;