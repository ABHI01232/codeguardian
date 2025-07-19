import React, { useState, useEffect } from 'react';
import { Shield, Clock, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';

const AnalysisStatusIndicator = ({ analysisId }) => {
  const [status, setStatus] = useState('RUNNING');
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    if (!analysisId) return;

    const interval = setInterval(async () => {
      try {
        const response = await fetch('http://localhost:8080/api/analyses');
        const analyses = await response.json();
        const currentAnalysis = analyses.find(a => a.id === analysisId);
        
        if (currentAnalysis) {
          setStatus(currentAnalysis.status);
          
          // Simulate progress based on status
          if (currentAnalysis.status === 'COMPLETED') {
            setProgress(100);
            clearInterval(interval);
          } else if (currentAnalysis.status === 'RUNNING') {
            setProgress(prev => Math.min(prev + 5, 95));
          } else if (currentAnalysis.status === 'FAILED') {
            clearInterval(interval);
          }
        }
      } catch (error) {
        console.error('Failed to fetch analysis status:', error);
      }
    }, 2000);

    return () => clearInterval(interval);
  }, [analysisId]);

  const getStatusIcon = () => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case 'FAILED':
        return <AlertCircle className="w-5 h-5 text-red-500" />;
      case 'RUNNING':
        return <Loader2 className="w-5 h-5 text-blue-500 animate-spin" />;
      default:
        return <Clock className="w-5 h-5 text-gray-500" />;
    }
  };

  const getStatusColor = () => {
    switch (status) {
      case 'COMPLETED':
        return 'text-green-600 bg-green-50 border-green-200';
      case 'FAILED':
        return 'text-red-600 bg-red-50 border-red-200';
      case 'RUNNING':
        return 'text-blue-600 bg-blue-50 border-blue-200';
      default:
        return 'text-gray-600 bg-gray-50 border-gray-200';
    }
  };

  return (
    <div className={`inline-flex items-center px-3 py-1 rounded-full border text-sm font-medium ${getStatusColor()}`}>
      {getStatusIcon()}
      <span className="ml-2">{status}</span>
      {status === 'RUNNING' && (
        <div className="ml-2 w-12 h-2 bg-gray-200 rounded-full overflow-hidden">
          <div 
            className="h-full bg-blue-500 transition-all duration-300"
            style={{ width: `${progress}%` }}
          />
        </div>
      )}
    </div>
  );
};

export default AnalysisStatusIndicator;