import React, { useState, useEffect } from 'react';
import { 
  Shield, 
  GitBranch, 
  AlertTriangle, 
  CheckCircle, 
  Activity,
  Clock,
  Users,
  FileText
} from 'lucide-react';
import { dashboardAPI } from '../services/api';
import AnalysisResults from './AnalysisResults';
import RepoList from './RepoList';
import RealTimeUpdates from './RealTimeUpdates';
import SystemHealth from './SystemHealth';

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

  // Fetch dashboard data
  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [overviewData, metricsData] = await Promise.all([
        dashboardAPI.getOverview(),
        dashboardAPI.getMetrics()
      ]);
      
      setOverview(overviewData);
      setMetrics(metricsData);
      setLastUpdated(new Date());
      setError(null);
    } catch (err) {
      console.error('Failed to fetch dashboard data:', err);
      setError('Failed to load dashboard data');
      // Set mock data for demo
      setOverview({
        repositories: { 
          content: [
            { id: 1, name: 'banking-api', platform: 'GITHUB', commitCount: 45 },
            { id: 2, name: 'payment-service', platform: 'GITLAB', commitCount: 32 }
          ], 
          totalElements: 2 
        },
        systemHealth: { status: 'UP', service: 'git-processor' },
        analysisSummary: {
          totalAnalyses: 127,
          criticalIssues: 3,
          warningIssues: 15,
          passedChecks: 109
        }
      });
      setMetrics({
        latestAnalysis: [
          {
            id: 1,
            commitId: 'abc123',
            repository: 'banking-api',
            status: 'COMPLETED',
            timestamp: new Date().toISOString(),
            findings: { critical: 1, high: 2, medium: 5, low: 8 }
          }
        ],
        recentRepositories: [
          { id: 1, name: 'banking-api', platform: 'GITHUB' }
        ]
      });
    } finally {
      setLoading(false);
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
              <button 
                onClick={fetchDashboardData}
                className="btn-primary"
                disabled={loading}
              >
                {loading ? <Activity className="w-4 h-4 animate-spin" /> : 'Refresh'}
              </button>
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
              <RepoList repositories={overview.repositories.content || []} />
            </div>
          </div>

          {/* Right Column */}
          <div className="space-y-8">
            {/* System Health */}
            <div className="card">
              <h2 className="text-lg font-semibold text-gray-900 mb-6">System Health</h2>
              <SystemHealth health={overview.systemHealth} />
            </div>

            {/* Real-time Updates */}
            <div className="card">
              <h2 className="text-lg font-semibold text-gray-900 mb-6">Real-time Updates</h2>
              <RealTimeUpdates />
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;