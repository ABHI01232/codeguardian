import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, 
  GitBranch, 
  ExternalLink, 
  Calendar, 
  Users,
  FileText,
  AlertTriangle,
  CheckCircle,
  Play,
  Settings,
  Clock,
  Shield
} from 'lucide-react';
import { repositoryAPI } from '../services/api';

const RepositoryDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [repository, setRepository] = useState(null);
  const [commits, setCommits] = useState([]);
  const [analyses, setAnalyses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    fetchRepositoryDetails();
  }, [id]);

  const fetchRepositoryDetails = async () => {
    try {
      setLoading(true);
      
      // Try to fetch real repository data
      try {
        const response = await repositoryAPI.getById(id);
        const repoData = response.data;
        
        // Fetch commits for this repository
        const commitsResponse = await repositoryAPI.getCommits(id);
        const commitsData = commitsResponse.data;
        
        // Fetch analyses for this repository
        const analysesResponse = await repositoryAPI.getAnalyses(id);
        const analysesData = analysesResponse.data;
        
        setRepository(repoData);
        setCommits(commitsData);
        setAnalyses(analysesData);
      } catch (apiError) {
        console.error('Failed to fetch repository details:', apiError);
        
        // Try to get individual pieces of data
        try {
          const response = await repositoryAPI.getById(id);
          setRepository(response.data);
        } catch (repoError) {
          console.error('Failed to fetch repository:', repoError);
          setError('Repository not found');
          return;
        }
        
        try {
          const commitsResponse = await repositoryAPI.getCommits(id);
          setCommits(commitsResponse.data);
        } catch (commitsError) {
          console.error('Failed to fetch commits:', commitsError);
          setCommits([]);
        }
        
        try {
          const analysesResponse = await repositoryAPI.getAnalyses(id);
          setAnalyses(analysesResponse.data);
        } catch (analysesError) {
          console.error('Failed to fetch analyses:', analysesError);
          setAnalyses([]);
        }
      }
    } catch (err) {
      console.error('Error in fetchRepositoryDetails:', err);
      setError('Failed to load repository details');
    } finally {
      setLoading(false);
    }
  };

  const handleAnalyzeNow = async () => {
    try {
      console.log('Triggering analysis for repository:', id);
      
      // Call the actual API endpoint
      const response = await fetch(`http://localhost:8080/api/repositories/${id}/analyze`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const analysisId = await response.text();
        console.log('Analysis started:', analysisId);
        
        // Set the analysis ID and show logs
        setCurrentAnalysisId(analysisId);
        setShowAnalysisLogs(true);
        
        window.showToast && window.showToast(
          `Analysis started for ${repository?.name}! Analysis ID: ${analysisId}`,
          'success',
          8000,
          () => {
            // When toast is clicked, navigate to analysis results when completed
            setTimeout(() => {
              window.open(`/analysis/${analysisId}`, '_blank');
            }, 10000); // Wait for analysis to complete
          }
        );
        
        // Refresh repository data after a delay
        setTimeout(() => {
          fetchRepositoryDetails();
        }, 12000);
        
      } else {
        const errorText = await response.text();
        throw new Error(`Failed to start analysis: ${errorText}`);
      }
    } catch (err) {
      console.error('Failed to trigger analysis:', err);
      window.showToast && window.showToast(
        `Failed to start analysis: ${err.message}`,
        'error',
        5000
      );
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'text-green-600 bg-green-50';
      case 'FAILED':
        return 'text-red-600 bg-red-50';
      case 'PENDING':
        return 'text-yellow-600 bg-yellow-50';
      default:
        return 'text-gray-600 bg-gray-50';
    }
  };

  const getTotalIssues = (findings) => {
    return (findings.critical || 0) + (findings.high || 0) + (findings.medium || 0) + (findings.low || 0);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <Shield className="w-8 h-8 animate-spin text-primary-600 mx-auto mb-4" />
          <p className="text-gray-600">Loading repository details...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <AlertTriangle className="w-12 h-12 text-red-500 mx-auto mb-4" />
          <p className="text-red-600 text-lg mb-2">{error}</p>
          <button 
            onClick={() => navigate('/')}
            className="btn-primary"
          >
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between py-6">
            <div className="flex items-center space-x-4">
              <button
                onClick={() => navigate('/')}
                className="flex items-center space-x-2 text-gray-600 hover:text-gray-900"
              >
                <ArrowLeft className="w-5 h-5" />
                <span>Back to Dashboard</span>
              </button>
              <div className="flex items-center space-x-3">
                <GitBranch className="w-6 h-6 text-primary-600" />
                <div>
                  <h1 className="text-2xl font-bold text-gray-900">{repository.name}</h1>
                  <p className="text-sm text-gray-500">{repository.fullName}</p>
                </div>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <button
                onClick={handleAnalyzeNow}
                className="btn-primary flex items-center space-x-2"
              >
                <Play className="w-4 h-4" />
                <span>{repository?.analysisCount > 0 ? 'Analyze Again' : 'Analyze Now'}</span>
              </button>
              <button 
                onClick={() => setShowAnalysisLogs(true)}
                className="btn-secondary flex items-center space-x-2"
              >
                <Settings className="w-4 h-4" />
                <span>View Logs</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Repository Info */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            <div className="text-center">
              <div className="text-2xl font-bold text-gray-900">{repository.commitCount}</div>
              <div className="text-sm text-gray-500">Commits</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-gray-900">{repository.analysisCount}</div>
              <div className="text-sm text-gray-500">Analyses</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-gray-900">{repository.issueCount}</div>
              <div className="text-sm text-gray-500">Issues Found</div>
            </div>
            <div className="text-center">
              <div className={`text-2xl font-bold ${
                repository.securityStatus === 'SECURE' ? 'text-green-600' :
                repository.securityStatus === 'WARNING' ? 'text-yellow-600' : 'text-red-600'
              }`}>
                {repository.securityStatus}
              </div>
              <div className="text-sm text-gray-500">Security Status</div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="border-b border-gray-200">
            <nav className="flex space-x-8 px-6">
              {['overview', 'commits', 'analyses', 'settings'].map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  className={`py-4 px-1 border-b-2 font-medium text-sm ${
                    activeTab === tab
                      ? 'border-primary-500 text-primary-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700'
                  }`}
                >
                  {tab.charAt(0).toUpperCase() + tab.slice(1)}
                </button>
              ))}
            </nav>
          </div>

          <div className="p-6">
            {activeTab === 'overview' && (
              <div className="space-y-6">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">Repository Information</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <span className="text-sm font-medium text-gray-500">Platform:</span>
                      <span className="ml-2 text-sm text-gray-900">{repository.platform}</span>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-gray-500">Language:</span>
                      <span className="ml-2 text-sm text-gray-900">{repository.language}</span>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-gray-500">Default Branch:</span>
                      <span className="ml-2 text-sm text-gray-900">{repository.defaultBranch}</span>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-gray-500">Last Commit:</span>
                      <span className="ml-2 text-sm text-gray-900">{formatDate(repository.lastCommitDate)}</span>
                    </div>
                  </div>
                  {repository.description && (
                    <div className="mt-4">
                      <span className="text-sm font-medium text-gray-500">Description:</span>
                      <p className="mt-1 text-sm text-gray-900">{repository.description}</p>
                    </div>
                  )}
                </div>
                
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">Recent Analyses</h3>
                  <div className="space-y-3">
                    {analyses.slice(0, 3).map((analysis) => (
                      <div key={analysis.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                        <div className="flex items-center space-x-3">
                          <div className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(analysis.status)}`}>
                            {analysis.status}
                          </div>
                          <span className="text-sm font-mono text-gray-600">{analysis.commitId}</span>
                        </div>
                        <div className="flex items-center space-x-4">
                          <span className="text-sm text-gray-500">{getTotalIssues(analysis.findings)} issues</span>
                          <button
                            onClick={() => navigate(`/analysis/${analysis.id}`)}
                            className="text-primary-600 hover:text-primary-700 text-sm"
                          >
                            View Details
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'commits' && (
              <div className="space-y-4">
                <h3 className="text-lg font-semibold text-gray-900">Recent Commits</h3>
                <div className="space-y-3">
                  {commits.map((commit) => (
                    <div key={commit.id} className="border rounded-lg p-4">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="font-medium text-gray-900">{commit.message}</p>
                          <div className="flex items-center space-x-2 mt-1 text-sm text-gray-500">
                            <span>{commit.author}</span>
                            <span>•</span>
                            <span>{formatDate(commit.timestamp || commit.date)}</span>
                            <span>•</span>
                            <span className="font-mono">{commit.id.substring(0, 8)}</span>
                          </div>
                        </div>
                        <div className="flex items-center space-x-3">
                          <div className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(commit.analysisStatus)}`}>
                            {commit.analysisStatus}
                          </div>
                          <span className="text-sm text-gray-500">{commit.issueCount} issues</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'analyses' && (
              <div className="space-y-4">
                <h3 className="text-lg font-semibold text-gray-900">Analysis History</h3>
                <div className="space-y-3">
                  {analyses.map((analysis) => (
                    <div key={analysis.id} className="border rounded-lg p-4">
                      <div className="flex items-center justify-between">
                        <div>
                          <div className="flex items-center space-x-2">
                            <span className="font-mono text-sm text-gray-600">{analysis.commitId}</span>
                            <div className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(analysis.status)}`}>
                              {analysis.status}
                            </div>
                          </div>
                          <p className="text-sm text-gray-500 mt-1">{formatDate(analysis.timestamp)}</p>
                        </div>
                        <div className="flex items-center space-x-4">
                          <div className="text-right">
                            <div className="text-sm font-medium text-gray-900">{getTotalIssues(analysis.findings)} issues</div>
                            <div className="text-xs text-gray-500">
                              {analysis.findings.critical}C {analysis.findings.high}H {analysis.findings.medium}M {analysis.findings.low}L
                            </div>
                          </div>
                          <button
                            onClick={() => navigate(`/analysis/${analysis.id}`)}
                            className="text-primary-600 hover:text-primary-700 text-sm"
                          >
                            View Details
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'settings' && (
              <div className="space-y-6">
                <h3 className="text-lg font-semibold text-gray-900">Repository Settings</h3>
                
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="font-medium text-gray-900">Webhook Configuration</h4>
                      <p className="text-sm text-gray-500">Automatic analysis on code push</p>
                    </div>
                    <div className={`px-3 py-1 rounded-full text-xs font-medium ${
                      repository.webhookConfigured ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }`}>
                      {repository.webhookConfigured ? 'Configured' : 'Not Configured'}
                    </div>
                  </div>
                  
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="font-medium text-gray-900">Analysis Schedule</h4>
                      <p className="text-sm text-gray-500">Periodic security scans</p>
                    </div>
                    <select className="px-3 py-1 border border-gray-300 rounded-md text-sm">
                      <option>Daily</option>
                      <option>Weekly</option>
                      <option>Manual</option>
                    </select>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Analysis Logs Modal */}
      <AnalysisLogs
        isOpen={showAnalysisLogs}
        onClose={() => setShowAnalysisLogs(false)}
        repositoryName={repository?.name || 'Unknown'}
        analysisId={currentAnalysisId}
      />
    </div>
  );
};

export default RepositoryDetailsPage;