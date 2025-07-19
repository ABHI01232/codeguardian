import React, { useState, useEffect } from 'react';
import { 
  Shield, 
  GitBranch, 
  AlertTriangle, 
  CheckCircle, 
  Activity,
  Clock,
  Users,
  FileText,
  Plus
} from 'lucide-react';
import api, { dashboardAPI, repositoryAPI } from '../services/api';
import AnalysisResults from './AnalysisResults';
import RepoList from './RepoList';
import RealTimeUpdates from './RealTimeUpdates';
import SystemHealth from './SystemHealth';
import AddRepositoryModal from './AddRepositoryModal';
import ToastContainer from './ToastContainer';
import LoadingSpinner from './LoadingSpinner';

console.log('Dashboard component - repositoryAPI:', repositoryAPI);

const Dashboard = () => {
  const [overview, setOverview] = useState({
    repositories: { content: [], totalElements: 0 },
    systemHealth: { status: 'UNKNOWN' },
    analysisSummary: {}
  });
  const [metrics, setMetrics] = useState({
    latestAnalysis: [],
    recentRepositories: []
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(new Date());
  const [showAddRepoModal, setShowAddRepoModal] = useState(false);
  const [addingRepository, setAddingRepository] = useState(false);
  const [analyzingRepos, setAnalyzingRepos] = useState(new Set());
  const [analysisProgress, setAnalysisProgress] = useState({});

  // Fetch dashboard data
  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [overviewResponse, metricsResponse] = await Promise.all([
        dashboardAPI.getOverview(),
        dashboardAPI.getMetrics()
      ]);
      
      setOverview(overviewResponse.data);
      setMetrics(metricsResponse.data);
      setLastUpdated(new Date());
      setError(null);
    } catch (err) {
      console.error('Failed to fetch dashboard data:', err);
      setError('Failed to load dashboard data');
      // Set empty data instead of mock data
      setOverview({
        repositories: { 
          content: [], 
          totalElements: 0 
        },
        systemHealth: { status: 'UNKNOWN', service: 'api-gateway' },
        analysisSummary: {
          totalAnalyses: 0,
          criticalIssues: 0,
          warningIssues: 0,
          passedChecks: 0
        }
      });
      setMetrics({
        latestAnalysis: [],
        recentRepositories: []
      });
    } finally {
      setLoading(false);
    }
  };

  const handleAddRepository = async (repoData) => {
    try {
      setAddingRepository(true);
      console.log('handleAddRepository called with:', repoData);
      
      // Show loading toast
      window.showToast && window.showToast(
        'Adding repository...',
        'info',
        3000
      );
      
      // Direct API call to avoid import issues
      const response = await api.post('http://localhost:8080/api/repositories', repoData);
      const newRepo = response.data;
      
      // Update local state
      setOverview(prev => ({
        ...prev,
        repositories: {
          content: [newRepo, ...prev.repositories.content],
          totalElements: prev.repositories.totalElements + 1
        }
      }));
      
      // Show success toast with analysis option
      window.showToast && window.showToast(
        `Repository "${newRepo.name}" added successfully! Click "Analyze Now" to start security scan.`,
        'success',
        8000
      );
      
      console.log('Repository added successfully:', newRepo);
    } catch (error) {
      console.error('Failed to add repository:', error);
      
      // Show error toast
      window.showToast && window.showToast(
        'Failed to add repository. Please try again.',
        'error',
        5000
      );
      
      throw error;
    } finally {
      setAddingRepository(false);
    }
  };

  const handleAnalyzeRepository = async (repositoryId, repositoryName) => {
    try {
      console.log('Starting analysis for repository:', repositoryId);
      
      // Add to analyzing set
      setAnalyzingRepos(prev => new Set([...prev, repositoryId]));
      
      // Initialize progress
      setAnalysisProgress(prev => ({
        ...prev,
        [repositoryId]: {
          status: 'STARTING',
          progress: 0,
          message: 'Initializing analysis...'
        }
      }));
      
      // Show toast
      window.showToast && window.showToast(
        `Starting analysis for ${repositoryName}...`,
        'info',
        3000
      );
      
      // Trigger analysis
      const response = await api.post(`http://localhost:8080/api/repositories/${repositoryId}/analyze`);
      const analysisId = response.data;
      
      // Update progress
      setAnalysisProgress(prev => ({
        ...prev,
        [repositoryId]: {
          status: 'RUNNING',
          progress: 25,
          message: 'Analysis in progress...',
          analysisId: analysisId
        }
      }));
      
      // Simulate progress updates
      setTimeout(() => {
        setAnalysisProgress(prev => ({
          ...prev,
          [repositoryId]: {
            ...prev[repositoryId],
            progress: 50,
            message: 'Scanning code for vulnerabilities...'
          }
        }));
      }, 2000);
      
      setTimeout(() => {
        setAnalysisProgress(prev => ({
          ...prev,
          [repositoryId]: {
            ...prev[repositoryId],
            progress: 75,
            message: 'Generating security report...'
          }
        }));
      }, 4000);
      
      setTimeout(() => {
        setAnalysisProgress(prev => ({
          ...prev,
          [repositoryId]: {
            ...prev[repositoryId],
            status: 'COMPLETED',
            progress: 100,
            message: 'Analysis completed!'
          }
        }));
        
        // Remove from analyzing set after a delay
        setTimeout(() => {
          setAnalyzingRepos(prev => {
            const newSet = new Set(prev);
            newSet.delete(repositoryId);
            return newSet;
          });
        }, 3000);
        
        // Show completion toast with link
        window.showToast && window.showToast(
          `Analysis completed for ${repositoryName}! Click here to view results.`,
          'success',
          10000,
          () => window.open(`/analysis/${analysisId}`, '_blank')
        );
        
        // Refresh dashboard data
        fetchDashboardData();
      }, 6000);
      
    } catch (error) {
      console.error('Failed to trigger analysis:', error);
      
      // Update progress with error
      setAnalysisProgress(prev => ({
        ...prev,
        [repositoryId]: {
          status: 'FAILED',
          progress: 0,
          message: 'Analysis failed'
        }
      }));
      
      // Remove from analyzing set
      setAnalyzingRepos(prev => {
        const newSet = new Set(prev);
        newSet.delete(repositoryId);
        return newSet;
      });
      
      window.showToast && window.showToast(
        `Failed to start analysis for ${repositoryName}`,
        'error',
        5000
      );
    }
  };

  useEffect(() => {
    fetchDashboardData();
    
    // Refresh data every 30 seconds
    const interval = setInterval(fetchDashboardData, 30000);
    return () => clearInterval(interval);
  }, []);

  // Calculate summary stats
  const summaryStats = {
    totalRepos: overview.repositories.totalElements || 0,
    totalAnalyses: overview.analysisSummary.totalAnalyses || 0,
    criticalIssues: overview.analysisSummary.criticalIssues || 0,
    systemStatus: overview.systemHealth.status
  };

  if (loading && !overview.repositories.content.length) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <Activity className="w-8 h-8 animate-spin text-primary-600 mx-auto mb-4" />
          <p className="text-gray-600">Loading CodeGuardian Dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center space-x-3">
              <Shield className="w-8 h-8 text-primary-600" />
              <div>
                <h1 className="text-2xl font-bold text-gray-900">CodeGuardian</h1>
                <p className="text-sm text-gray-500">Secure Code Analysis Platform</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <div className="text-right">
                <p className="text-sm text-gray-500">Last Updated</p>
                <p className="text-sm font-medium">{lastUpdated.toLocaleTimeString()}</p>
              </div>
              <div className="flex space-x-2">
                <button 
                  onClick={() => setShowAddRepoModal(true)}
                  className="btn-primary flex items-center space-x-2"
                >
                  <Plus className="w-4 h-4" />
                  <span>Add Repository</span>
                </button>
                <button 
                  onClick={fetchDashboardData}
                  className="btn-secondary"
                  disabled={loading}
                >
                  {loading ? <Activity className="w-4 h-4 animate-spin" /> : 'Refresh'}
                </button>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Error Banner */}
      {error && (
        <div className="bg-danger-50 border-l-4 border-danger-500 p-4 mx-4 mt-4 rounded">
          <div className="flex">
            <AlertTriangle className="w-5 h-5 text-danger-500" />
            <div className="ml-3">
              <p className="text-sm text-danger-700">{error}</p>
              <p className="text-xs text-danger-600 mt-1">Showing demo data below</p>
            </div>
          </div>
        </div>
      )}

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="card">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <GitBranch className="w-8 h-8 text-primary-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Repositories</p>
                <p className="text-2xl font-semibold text-gray-900">{summaryStats.totalRepos}</p>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <FileText className="w-8 h-8 text-blue-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Total Analyses</p>
                <p className="text-2xl font-semibold text-gray-900">{summaryStats.totalAnalyses}</p>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <AlertTriangle className="w-8 h-8 text-warning-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Critical Issues</p>
                <p className="text-2xl font-semibold text-gray-900">{summaryStats.criticalIssues}</p>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className={`status-indicator ${
                  summaryStats.systemStatus === 'UP' ? 'bg-success-500' : 'bg-danger-500'
                }`} />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">System Status</p>
                <p className="text-2xl font-semibold text-gray-900">{summaryStats.systemStatus}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Main Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column */}
          <div className="lg:col-span-2 space-y-8">
            {/* Recent Analysis Results */}
            <div className="card">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-lg font-semibold text-gray-900">Recent Analysis Results</h2>
                <span className="text-sm text-gray-500">
                  {metrics.latestAnalysis.length} results
                </span>
              </div>
              <AnalysisResults analyses={metrics.latestAnalysis} />
            </div>

            {/* Repository List */}
            <div className="card">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-lg font-semibold text-gray-900">Repositories</h2>
                <span className="text-sm text-gray-500">
                  {overview.repositories.totalElements} total
                </span>
              </div>
              <RepoList 
                repositories={overview.repositories.content || []} 
                onAnalyze={handleAnalyzeRepository}
                analyzingRepos={analyzingRepos}
                analysisProgress={analysisProgress}
              />
            </div>
          </div>

          {/* Right Column */}
          <div className="space-y-8">
            {/* Real-time Updates */}
            <div className="card">
              <h2 className="text-lg font-semibold text-gray-900 mb-6">Real-time Updates</h2>
              <RealTimeUpdates />
            </div>

            {/* System Health */}
            <div className="card">
              <h2 className="text-lg font-semibold text-gray-900 mb-6">System Health</h2>
              <SystemHealth health={overview.systemHealth} />
            </div>
          </div>
        </div>
      </main>

      {/* Add Repository Modal */}
      <AddRepositoryModal 
        isOpen={showAddRepoModal}
        onClose={() => setShowAddRepoModal(false)}
        onSubmit={handleAddRepository}
      />

      {/* Toast Container */}
      <ToastContainer />

      {/* Loading Overlay */}
      {addingRepository && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8">
            <LoadingSpinner 
              message="Adding repository..." 
              type="repository"
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;