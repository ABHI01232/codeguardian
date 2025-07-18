import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  GitBranch, 
  ExternalLink, 
  Calendar, 
  Users,
  Star,
  AlertCircle,
  CheckCircle
} from 'lucide-react';
import { repositoryAPI } from '../services/api';
import AnalysisModal from './AnalysisModal';

const RepoList = ({ repositories = [], onAnalyze, analyzingRepos = new Set(), analysisProgress = {} }) => {
  const navigate = useNavigate();
  const [showAnalysisModal, setShowAnalysisModal] = useState(false);
  const [currentAnalysis, setCurrentAnalysis] = useState(null);
  
  const handleAnalyzeNow = async (repo) => {
    if (onAnalyze) {
      await onAnalyze(repo.id, repo.name);
    } else {
      try {
        // Fallback to original implementation
        console.log('Starting analysis for repository:', repo.name);
        
        const response = await repositoryAPI.analyze(repo.id);
        const result = response.data;
        
        setCurrentAnalysis({
          repository: repo.name,
          analysisId: result.analysisId,
          repositoryId: repo.id
        });
        setShowAnalysisModal(true);
        
        window.showToast && window.showToast(
          `Analysis started for ${repo.name}`,
          'success',
          3000
        );
        
      } catch (error) {
        console.error('Failed to start analysis:', error);
        window.showToast && window.showToast(
          'Failed to start analysis. Please try again.',
          'error',
          5000
        );
      }
    }
  };
  
  const getPlatformColor = (platform) => {
    switch (platform?.toLowerCase()) {
      case 'github':
        return 'bg-gray-900 text-white';
      case 'gitlab':
        return 'bg-orange-500 text-white';
      case 'bitbucket':
        return 'bg-blue-600 text-white';
      default:
        return 'bg-gray-500 text-white';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Unknown';
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  const getHealthStatus = (repo) => {
    // Mock health status based on recent activity
    const lastCommit = repo.lastCommitDate ? new Date(repo.lastCommitDate) : null;
    const daysSinceLastCommit = lastCommit ? 
      Math.floor((Date.now() - lastCommit.getTime()) / (1000 * 60 * 60 * 24)) : 999;
    
    if (daysSinceLastCommit <= 7) return 'healthy';
    if (daysSinceLastCommit <= 30) return 'warning';
    return 'stale';
  };

  const getHealthIcon = (status) => {
    switch (status) {
      case 'healthy':
        return <CheckCircle className="w-4 h-4 text-success-500" />;
      case 'warning':
        return <AlertCircle className="w-4 h-4 text-warning-500" />;
      case 'stale':
        return <AlertCircle className="w-4 h-4 text-gray-400" />;
      default:
        return <AlertCircle className="w-4 h-4 text-gray-400" />;
    }
  };

  if (!repositories || repositories.length === 0) {
    return (
      <div className="text-center py-8">
        <GitBranch className="w-12 h-12 text-gray-400 mx-auto mb-4" />
        <p className="text-gray-500 text-lg">No repositories found</p>
        <p className="text-gray-400 text-sm mt-2">
          Connect your Git repositories to start analyzing code
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {repositories.map((repo) => {
        const healthStatus = getHealthStatus(repo);
        
        return (
          <div 
            key={repo.id} 
            className="border rounded-lg p-4 hover:shadow-md transition-shadow bg-white"
          >
            {/* Header */}
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center space-x-3">
                <GitBranch className="w-5 h-5 text-gray-600" />
                <div>
                  <h3 className="font-semibold text-gray-900">
                    {repo.name || 'Unknown Repository'}
                  </h3>
                  {repo.fullName && repo.fullName !== repo.name && (
                    <p className="text-sm text-gray-500">{repo.fullName}</p>
                  )}
                </div>
              </div>
              <div className="flex items-center space-x-2">
                {getHealthIcon(healthStatus)}
                <span className={`px-2 py-1 rounded-full text-xs font-medium ${getPlatformColor(repo.platform)}`}>
                  {repo.platform || 'Unknown'}
                </span>
              </div>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-3">
              <div className="text-center">
                <div className="text-lg font-semibold text-gray-900">
                  {repo.commitCount || 0}
                </div>
                <div className="text-xs text-gray-500">Commits</div>
              </div>
              <div className="text-center">
                <div className="text-lg font-semibold text-gray-900">
                  {repo.branchCount || 1}
                </div>
                <div className="text-xs text-gray-500">Branches</div>
              </div>
              <div className="text-center">
                <div className="text-lg font-semibold text-gray-900">
                  {repo.analysisCount || 0}
                </div>
                <div className="text-xs text-gray-500">Analyses</div>
              </div>
              <div className="text-center">
                <div className="text-lg font-semibold text-gray-900">
                  {repo.issueCount || 0}
                </div>
                <div className="text-xs text-gray-500">Issues</div>
              </div>
            </div>

            {/* Repository Info */}
            <div className="space-y-2 text-sm text-gray-600">
              {repo.description && (
                <p className="text-gray-700">{repo.description}</p>
              )}
              
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  {repo.language && (
                    <div className="flex items-center space-x-1">
                      <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
                      <span>{repo.language}</span>
                    </div>
                  )}
                  {repo.defaultBranch && (
                    <div className="flex items-center space-x-1">
                      <GitBranch className="w-3 h-3" />
                      <span>{repo.defaultBranch}</span>
                    </div>
                  )}
                  {repo.lastCommitDate && (
                    <div className="flex items-center space-x-1">
                      <Calendar className="w-3 h-3" />
                      <span>Updated {formatDate(repo.lastCommitDate)}</span>
                    </div>
                  )}
                </div>
                
                {repo.url && (
                  <a 
                    href={repo.url} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="text-primary-600 hover:text-primary-700 flex items-center space-x-1"
                  >
                    <span>View</span>
                    <ExternalLink className="w-3 h-3" />
                  </a>
                )}
              </div>
            </div>

            {/* Security Status */}
            {repo.securityStatus && (
              <div className="mt-3 pt-3 border-t border-gray-100">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-gray-700">Security Status</span>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                    repo.securityStatus === 'SECURE' ? 'badge-success' :
                    repo.securityStatus === 'WARNING' ? 'badge-warning' : 'badge-danger'
                  }`}>
                    {repo.securityStatus}
                  </span>
                </div>
                {repo.lastAnalysisDate && (
                  <p className="text-xs text-gray-500 mt-1">
                    Last analyzed: {formatDate(repo.lastAnalysisDate)}
                  </p>
                )}
              </div>
            )}

            {/* Analysis Progress */}
            {analyzingRepos.has(repo.id) && analysisProgress[repo.id] && (
              <div className="mt-3 pt-3 border-t border-gray-100">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-gray-700">Analysis Progress</span>
                  <span className="text-xs text-gray-500">
                    {analysisProgress[repo.id].progress}%
                  </span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div 
                    className="bg-primary-600 h-2 rounded-full transition-all duration-500"
                    style={{ width: `${analysisProgress[repo.id].progress}%` }}
                  ></div>
                </div>
                <p className="text-xs text-gray-500 mt-1">
                  {analysisProgress[repo.id].message}
                </p>
                {analysisProgress[repo.id].status === 'COMPLETED' && (
                  <button 
                    onClick={() => navigate(`/analysis/${analysisProgress[repo.id].analysisId}`)}
                    className="text-xs text-green-600 hover:text-green-700 mt-2 flex items-center space-x-1"
                  >
                    <CheckCircle className="w-3 h-3" />
                    <span>View Analysis Results</span>
                  </button>
                )}
              </div>
            )}

            {/* Quick Actions */}
            <div className="flex items-center justify-between mt-4 pt-3 border-t border-gray-100">
              <div className="flex items-center space-x-2 text-xs text-gray-500">
                <span>ID: {repo.id}</span>
                {repo.isPrivate && (
                  <>
                    <span>•</span>
                    <span>Private</span>
                  </>
                )}
              </div>
              <div className="flex space-x-2">
                {!analyzingRepos.has(repo.id) ? (
                  <button 
                    onClick={() => handleAnalyzeNow(repo)}
                    className="text-xs text-primary-600 hover:text-primary-700 px-2 py-1 rounded border border-primary-200 hover:bg-primary-50"
                  >
                    {repo.analysisCount > 0 ? 'Analyze Again' : 'Analyze Now'}
                  </button>
                ) : (
                  <span className="text-xs text-gray-500 px-2 py-1">
                    Analyzing...
                  </span>
                )}
                <button 
                  onClick={() => navigate(`/repository/${repo.id}`)}
                  className="text-xs text-gray-600 hover:text-gray-700 px-2 py-1 rounded border border-gray-200 hover:bg-gray-50"
                >
                  View Details
                </button>
              </div>
            </div>
          </div>
        );
      })}

      {/* Analysis Modal */}
      <AnalysisModal 
        isOpen={showAnalysisModal}
        onClose={() => setShowAnalysisModal(false)}
        repository={currentAnalysis?.repository}
        analysisId={currentAnalysis?.analysisId}
      />
    </div>
  );
};

export default RepoList;