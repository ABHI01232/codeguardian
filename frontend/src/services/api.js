import axios from 'axios';

// Create axios instance with base configuration
const api = axios.create({
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// API endpoints configuration
const API_ENDPOINTS = {
  // API Gateway (port 8080)
  gateway: 'http://localhost:8080',
  
  // Git Processor (port 8081) 
  gitProcessor: 'http://localhost:8081',
  
  // Code Analyzer (port 8082)
  codeAnalyzer: 'http://localhost:8082'
};

// Repository API
export const repositoryAPI = {
  // Get all repositories
  getAll: (page = 0, size = 10) => 
    api.get(`${API_ENDPOINTS.gitProcessor}/api/repositories?page=${page}&size=${size}`),
  
  // Get repository by ID
  getById: (id) => 
    api.get(`${API_ENDPOINTS.gitProcessor}/api/repositories/${id}`),
  
  // Get repositories by platform
  getByPlatform: (platform) => 
    api.get(`${API_ENDPOINTS.gitProcessor}/api/repositories/platform/${platform}`),
  
  // Get repository commits
  getCommits: (repoId, page = 0, size = 10) => 
    api.get(`${API_ENDPOINTS.gitProcessor}/api/repositories/${repoId}/commits?page=${page}&size=${size}`)
};

// Analysis API
export const analysisAPI = {
  // Get analysis results for a commit
  getByCommit: (commitId) => 
    api.get(`${API_ENDPOINTS.codeAnalyzer}/api/analysis/commit/${commitId}`),
  
  // Get latest analysis results
  getLatest: (limit = 10) => 
    api.get(`${API_ENDPOINTS.codeAnalyzer}/api/analysis/latest?limit=${limit}`),
  
  // Get analysis summary/stats
  getSummary: () => 
    api.get(`${API_ENDPOINTS.codeAnalyzer}/api/analysis/summary`),
  
  // Trigger manual analysis
  trigger: (repositoryId, commitId) => 
    api.post(`${API_ENDPOINTS.codeAnalyzer}/api/analysis/trigger`, {
      repositoryId,
      commitId
    })
};

// Webhook API
export const webhookAPI = {
  // Get webhook health
  getHealth: () => 
    api.get(`${API_ENDPOINTS.gitProcessor}/webhooks/health`),
  
  // Get webhook configuration
  getConfig: () => 
    api.get(`${API_ENDPOINTS.gitProcessor}/webhooks/config`),
  
  // Test webhook endpoint
  test: (platform = 'github', payload = null) => 
    api.post(`${API_ENDPOINTS.gitProcessor}/webhooks/test`, payload, {
      params: { platform }
    })
};

// Dashboard API - aggregated data
export const dashboardAPI = {
  // Get dashboard overview
  getOverview: async () => {
    try {
      const [repos, health, summary] = await Promise.all([
        repositoryAPI.getAll(0, 5),
        webhookAPI.getHealth().catch(() => ({ data: { status: 'DOWN' } })),
        analysisAPI.getSummary().catch(() => ({ data: {} }))
      ]);
      
      return {
        repositories: repos.data,
        systemHealth: health.data,
        analysisSummary: summary.data
      };
    } catch (error) {
      console.error('Error fetching dashboard overview:', error);
      throw error;
    }
  },
  
  // Get real-time metrics
  getMetrics: async () => {
    try {
      const [latestAnalysis, recentRepos] = await Promise.all([
        analysisAPI.getLatest(5),
        repositoryAPI.getAll(0, 3)
      ]);
      
      return {
        latestAnalysis: latestAnalysis.data,
        recentRepositories: recentRepos.data.content || recentRepos.data
      };
    } catch (error) {
      console.error('Error fetching dashboard metrics:', error);
      // Return mock data for demo
      return {
        latestAnalysis: [],
        recentRepositories: []
      };
    }
  }
};

// Request interceptor for authentication (when implemented)
api.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;