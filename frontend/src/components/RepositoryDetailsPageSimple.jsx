import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';

const RepositoryDetailsPageSimple = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  console.log('🔧 Simple Repository Details Page loaded with ID:', id);

  return (
    <div style={{ padding: '20px', minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <h1 style={{ color: 'black', fontSize: '24px', marginBottom: '20px' }}>
        🔧 Repository Details - DEBUG VERSION
      </h1>
      <div style={{ backgroundColor: 'white', padding: '20px', borderRadius: '8px', marginBottom: '20px' }}>
        <p><strong>Repository ID:</strong> {id}</p>
        <p><strong>Current URL:</strong> {window.location.href}</p>
        <p><strong>Navigation Test:</strong> This component is rendering successfully!</p>
      </div>
      <button 
        onClick={() => navigate('/')}
        style={{ 
          padding: '10px 20px', 
          backgroundColor: '#007bff', 
          color: 'white', 
          border: 'none', 
          borderRadius: '4px',
          cursor: 'pointer'
        }}
      >
        Back to Dashboard
      </button>
    </div>
  );
};

export default RepositoryDetailsPageSimple;