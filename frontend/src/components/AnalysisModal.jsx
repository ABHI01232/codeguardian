import React from 'react';
import { X } from 'lucide-react';
import AnalysisProgress from './AnalysisProgress';

const AnalysisModal = ({ isOpen, onClose, repository, analysisId }) => {
  if (!isOpen) return null;

  const handleComplete = () => {
    window.showToast && window.showToast(
      `Analysis completed for ${repository}! Check the results in the dashboard.`,
      'success',
      8000
    );
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-lg w-full mx-4">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900">Security Analysis</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Progress Content */}
        <div className="p-6">
          <AnalysisProgress 
            repository={repository} 
            analysisId={analysisId}
            onComplete={handleComplete}
          />
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-200">
          <div className="flex items-center justify-between">
            <p className="text-sm text-gray-600">
              Analysis ID: <span className="font-mono text-xs">{analysisId}</span>
            </p>
            <button
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200 transition-colors"
            >
              Run in Background
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AnalysisModal;