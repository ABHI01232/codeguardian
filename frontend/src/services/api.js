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
    api.get(`${API_ENDPOINTS.gateway}/api/repositories`),
  
  // Get repository by ID
  getById: (id) => 
    api.get(`${API_ENDPOINTS.gateway}/api/repositories/${id}`),
  
  // Create new repository
  create: (repository) => 
    api.post(`${API_ENDPOINTS.gateway}/api/repositories`, repository),
  
  // Update repository
  update: (id, repository) => 
    api.put(`${API_ENDPOINTS.gateway}/api/repositories/${id}`, repository),
  
  // Delete repository
  delete: (id) => 
    api.delete(`${API_ENDPOINTS.gateway}/api/repositories/${id}`),
  
  // Trigger analysis for repository
  analyze: (id) => 
    api.post(`${API_ENDPOINTS.gateway}/api/repositories/${id}/analyze`),
  
  // Get repository analyses
  getAnalyses: (id) => 
    api.get(`${API_ENDPOINTS.gateway}/api/repositories/${id}/analyses`),
  
  // Get repository commits
  getCommits: (id) => 
    api.get(`${API_ENDPOINTS.gateway}/api/repositories/${id}/commits`)
};

// Analysis API
export const analysisAPI = {
  // Get all analyses
  getAll: () => 
    api.get(`${API_ENDPOINTS.gateway}/api/analyses`),
  
  // Get analysis by ID
  getById: (id) => 
    api.get(`${API_ENDPOINTS.gateway}/api/analyses/${id}`),
  
  // Get recent analyses
  getRecent: (limit = 10) => 
    api.get(`${API_ENDPOINTS.gateway}/api/analyses/recent?limit=${limit}`),
  
  // Get analyses by repository
  getByRepository: (repositoryId) => 
    api.get(`${API_ENDPOINTS.gateway}/api/analyses/repository/${repositoryId}`),
  
  // Get analyses by status
  getByStatus: (status) => 
    api.get(`${API_ENDPOINTS.gateway}/api/analyses/status/${status}`),
  
  // Get analysis statistics
  getStatistics: () => 
    api.get(`${API_ENDPOINTS.gateway}/api/analyses/statistics`),
  
  // Get analysis findings
  getFindings: (id) => 
    api.get(`${API_ENDPOINTS.gateway}/api/analyses/${id}/findings`),
  
  // Rerun analysis
  rerun: (id) => 
    api.post(`${API_ENDPOINTS.gateway}/api/analyses/${id}/rerun`),
  
  // Delete analysis
  delete: (id) => 
    api.delete(`${API_ENDPOINTS.gateway}/api/analyses/${id}`)
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
  getOverview: () => 
    api.get(`${API_ENDPOINTS.gateway}/api/dashboard/overview`),
  
  // Get dashboard metrics
  getMetrics: () => 
    api.get(`${API_ENDPOINTS.gateway}/api/dashboard/metrics`),
  
  // Get system health
  getHealth: () => 
    api.get(`${API_ENDPOINTS.gateway}/api/dashboard/health`),
  
  // Get dashboard statistics
  getStatistics: () => 
    api.get(`${API_ENDPOINTS.gateway}/api/dashboard/statistics`),
  
  // Get recent activity
  getRecentActivity: (limit = 20) => 
    api.get(`${API_ENDPOINTS.gateway}/api/dashboard/recent-activity?limit=${limit}`)
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