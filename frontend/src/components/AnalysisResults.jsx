import React from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  AlertTriangle, 
  Shield, 
  CheckCircle, 
  Clock, 
  ExternalLink,
  FileText,
  GitCommit
} from 'lucide-react';

const AnalysisResults = ({ analyses = [] }) => {
  const navigate = useNavigate();
  
  const getSeverityColor = (severity) => {
    switch (severity?.toLowerCase()) {
      case 'critical':
        return 'text-red-600 bg-red-50 border-red-200';
      case 'high':
        return 'text-orange-600 bg-orange-50 border-orange-200';
      case 'medium':
        return 'text-yellow-600 bg-yellow-50 border-yellow-200';
      case 'low':
        return 'text-blue-600 bg-blue-50 border-blue-200';
      default:
        return 'text-gray-600 bg-gray-50 border-gray-200';
    }
  };

  const getStatusIcon = (status) => {
    switch (status?.toLowerCase()) {
      case 'completed':
        return <CheckCircle className="w-5 h-5 text-success-500" />;
      case 'failed':
        return <AlertTriangle className="w-5 h-5 text-danger-500" />;
      case 'pending':
      case 'running':
        return <Clock className="w-5 h-5 text-warning-500" />;
      default:
        return <FileText className="w-5 h-5 text-gray-500" />;
    }
  };

  const formatTimestamp = (timestamp) => {
    if (!timestamp) return 'Unknown';
    const date = new Date(timestamp);
    return date.toLocaleString();
  };

  const getTotalIssues = (findings) => {
    if (!findings) return 0;
    return (findings.critical || 0) + (findings.high || 0) + (findings.medium || 0) + (findings.low || 0);
  };

  if (!analyses || analyses.length === 0) {
    return (
      <div className="text-center py-8">
        <FileText className="w-12 h-12 text-gray-400 mx-auto mb-4" />
        <p className="text-gray-500 text-lg">No analysis results available</p>
        <p className="text-gray-400 text-sm mt-2">
          Analysis results will appear here when code is processed
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {analyses.map((analysis, index) => (
        <div 
          key={analysis.id || index} 
          className="border rounded-lg p-4 hover:shadow-md transition-shadow bg-white"
        >
          {/* Header */}
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center space-x-3">
              {getStatusIcon(analysis.status)}
              <div>
                <h3 className="font-semibold text-gray-900">
                  {analysis.repository || 'Unknown Repository'}
                </h3>
                <div className="flex items-center space-x-2 text-sm text-gray-500">
                  <GitCommit className="w-4 h-4" />
                  <span className="font-mono">
                    {analysis.commitId?.substring(0, 8) || 'Unknown'}
                  </span>
                  <span>•</span>
                  <span>{formatTimestamp(analysis.timestamp)}</span>
                </div>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                analysis.status === 'COMPLETED' ? 'badge-success' :
                analysis.status === 'FAILED' ? 'badge-danger' : 'badge-warning'
              }`}>
                {analysis.status || 'Unknown'}
              </span>
            </div>
          </div>

          {/* Findings Summary */}
          {analysis.findings && (
            <div className="mb-3">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700">Security Findings</span>
                <span className="text-sm text-gray-500">
                  {getTotalIssues(analysis.findings)} total issues
                </span>
              </div>
              <div className="flex space-x-4">
                {analysis.findings.critical > 0 && (
                  <div className="flex items-center space-x-1">
                    <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                    <span className="text-sm text-red-600 font-medium">
                      {analysis.findings.critical} Critical
                    </span>
                  </div>
                )}
                {analysis.findings.high > 0 && (
                  <div className="flex items-center space-x-1">
                    <div className="w-3 h-3 bg-orange-500 rounded-full"></div>
                    <span className="text-sm text-orange-600 font-medium">
                      {analysis.findings.high} High
                    </span>
                  </div>
                )}
                {analysis.findings.medium > 0 && (
                  <div className="flex items-center space-x-1">
                    <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
                    <span className="text-sm text-yellow-600 font-medium">
                      {analysis.findings.medium} Medium
                    </span>
                  </div>
                )}
                {analysis.findings.low > 0 && (
                  <div className="flex items-center space-x-1">
                    <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
                    <span className="text-sm text-blue-600 font-medium">
                      {analysis.findings.low} Low
                    </span>
                  </div>
                )}
                {getTotalIssues(analysis.findings) === 0 && (
                  <div className="flex items-center space-x-1">
                    <CheckCircle className="w-4 h-4 text-success-500" />
                    <span className="text-sm text-success-600 font-medium">
                      No issues found
                    </span>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Analysis Details */}
          {analysis.details && (
            <div className="space-y-2">
              {analysis.details.slice(0, 3).map((detail, idx) => (
                <div 
                  key={idx}
                  className={`text-sm p-2 rounded border ${getSeverityColor(detail.severity)}`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-medium">{detail.type || 'Security Issue'}</span>
                    <span className="text-xs uppercase font-medium">
                      {detail.severity || 'Medium'}
                    </span>
                  </div>
                  <p className="text-xs mt-1 opacity-90">
                    {detail.description || 'Security vulnerability detected'}
                  </p>
                  {detail.file && (
                    <p className="text-xs mt-1 font-mono opacity-75">
                      {detail.file}:{detail.line || '?'}
                    </p>
                  )}
                </div>
              ))}
              {analysis.details.length > 3 && (
                <p className="text-xs text-gray-500 text-center py-1">
                  ... and {analysis.details.length - 3} more issues
                </p>
              )}
            </div>
          )}

          {/* Actions */}
          <div className="flex items-center justify-between mt-4 pt-3 border-t border-gray-100">
            <div className="flex items-center space-x-2 text-sm text-gray-500">
              <Shield className="w-4 h-4" />
              <span>Analysis ID: {analysis.id || 'N/A'}</span>
            </div>
            <button 
              onClick={() => navigate(`/analysis/${analysis.id}`)}
              className="text-sm text-primary-600 hover:text-primary-700 flex items-center space-x-1"
            >
              <span>View Details</span>
              <ExternalLink className="w-4 h-4" />
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};

export default AnalysisResults;