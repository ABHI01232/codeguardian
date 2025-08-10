import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, 
  Shield, 
  AlertTriangle, 
  CheckCircle, 
  Clock,
  FileText,
  GitCommit,
  ExternalLink,
  Filter,
  Search,
  Calendar,
  User,
  Code,
  Bug,
  Eye
} from 'lucide-react';

const AnalysisResultsPage = () => {
  console.log('🚀 AnalysisResultsPage component mounted');
  
  const { id } = useParams();
  const navigate = useNavigate();
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filterSeverity, setFilterSeverity] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  
  console.log('🔍 AnalysisResultsPage initialized with ID:', id);

  useEffect(() => {
    fetchAnalysisDetails();
  }, [id]);

  const fetchAnalysisDetails = async () => {
    try {
      setLoading(true);
      console.log('🔍 Fetching analysis details for ID:', id);
      
      // Try individual analysis endpoint first
      try {
        const response = await fetch(`http://localhost:8080/api/analyses/${id}`);
        console.log('📡 Individual analysis response status:', response.status);
        
        if (response.ok) {
          const analysisData = await response.json();
          console.log('📊 Individual analysis data:', analysisData);
          
          // Check if we have meaningful data
          const hasDetails = analysisData.details && analysisData.details.length > 0;
          const hasFindings = analysisData.findings && Object.values(analysisData.findings).some(count => count > 0);
          const isCompleted = analysisData.status === 'COMPLETED';
          
          console.log('🔍 Data quality check:', {
            hasDetails,
            hasFindings,
            isCompleted,
            status: analysisData.status,
            findingsCount: analysisData.details?.length || 0,
            totalFindings: hasFindings ? Object.values(analysisData.findings).reduce((a, b) => a + b, 0) : 0
          });
          
          // Only use individual data if it's meaningful (has details/findings AND is completed)
          if ((hasDetails || hasFindings) && isCompleted) {
            console.log('✅ Using individual analysis data - has meaningful data and is completed');
            setAnalysis(transformAnalysisData(analysisData, id));
            return;
          } else {
            console.log('⚠️ Individual analysis insufficient - trying list endpoint', {
              reason: !isCompleted ? 'not completed' : 'no meaningful data'
            });
          }
        }
      } catch (error) {
        console.error('❌ Individual analysis fetch failed:', error);
      }
      
      // Fallback: Try to find the analysis in the list endpoint
      console.log('📡 Trying to fetch from analyses list');
      const listResponse = await fetch('http://localhost:8080/api/analyses');
      if (listResponse.ok) {
        const analysesList = await listResponse.json();
        console.log('📊 Got analyses list, length:', analysesList.length);
        
        const foundAnalysis = analysesList.find(analysis => analysis.id === id);
        if (foundAnalysis) {
          console.log('✅ Found analysis in list:', foundAnalysis);
          setAnalysis(transformAnalysisData(foundAnalysis, id));
          return;
        }
      }
      
      throw new Error('Analysis not found in individual or list endpoints');
      
    } catch (err) {
      console.error('❌ Error fetching analysis details:', err);
      setError('Failed to load analysis details: ' + err.message);
    } finally {
      setLoading(false);
    }
  };
  
  const transformAnalysisData = (analysisData, id) => {
    return {
      id: analysisData.id || id,
      commitId: analysisData.commitId || 'unknown',
      commitMessage: analysisData.commitMessage || 'No commit message',
      author: analysisData.author || 'Unknown',
      timestamp: analysisData.timestamp || new Date().toISOString(),
      repository: {
        id: analysisData.repositoryId,
        name: analysisData.repository || 'Unknown Repository',
        fullName: analysisData.repository || 'Unknown Repository',
        platform: 'GITHUB'
      },
      status: analysisData.status || 'UNKNOWN',
      duration: analysisData.duration || 'N/A',
      scanConfig: analysisData.scanConfig || {
        language: 'Java',
        rules: ['security', 'quality'],
        scope: 'full'
      },
      findings: analysisData.findings || {
        critical: 0,
        high: 0,
        medium: 0,
        low: 0
      },
      details: analysisData.details || []
    };
  };

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

  const getSeverityIcon = (severity) => {
    switch (severity?.toLowerCase()) {
      case 'critical':
        return <AlertTriangle className="w-5 h-5 text-red-500" />;
      case 'high':
        return <AlertTriangle className="w-5 h-5 text-orange-500" />;
      case 'medium':
        return <AlertTriangle className="w-5 h-5 text-yellow-500" />;
      case 'low':
        return <AlertTriangle className="w-5 h-5 text-blue-500" />;
      default:
        return <Bug className="w-5 h-5 text-gray-500" />;
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString();
  };

  const getTotalIssues = () => {
    if (!analysis?.findings) return 0;
    return Object.values(analysis.findings).reduce((sum, count) => sum + count, 0);
  };

  const filteredFindings = analysis?.details?.filter(finding => {
    const matchesSeverity = filterSeverity === 'all' || finding.severity?.toLowerCase() === filterSeverity;
    const matchesSearch = !searchQuery || 
      finding.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      finding.type?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      finding.file?.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesSeverity && matchesSearch;
  }) || [];

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <Shield className="w-8 h-8 animate-spin text-primary-600 mx-auto mb-4" />
          <p className="text-gray-600">Loading analysis details...</p>
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
            onClick={() => navigate(-1)}
            className="btn-primary"
          >
            Go Back
          </button>
        </div>
      </div>
    );
  }

  // Debug function to show current state
  const debugCurrentState = () => {
    console.log('🐛 DEBUG - Current Analysis State:', {
      analysis,
      loading,
      error,
      id,
      totalIssues: getTotalIssues(),
      filteredFindingsLength: filteredFindings.length
    });
    alert(`Debug Info:
Analysis ID: ${id}
Loading: ${loading}
Error: ${error || 'none'}
Analysis Object: ${analysis ? 'exists' : 'null'}
Total Issues: ${getTotalIssues()}
Filtered Findings: ${filteredFindings.length}
Status: ${analysis?.status || 'unknown'}
Check console for full details`);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* DEBUG BUTTON - Remove after fixing */}
      <div style={{position: 'fixed', top: '10px', right: '10px', zIndex: 9999}}>
        <button 
          onClick={debugCurrentState}
          style={{
            background: 'red', 
            color: 'white', 
            padding: '10px', 
            border: 'none', 
            borderRadius: '4px',
            fontSize: '12px',
            cursor: 'pointer'
          }}
        >
          🐛 DEBUG
        </button>
      </div>
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between py-6">
            <div className="flex items-center space-x-4">
              <button
                onClick={() => navigate(-1)}
                className="flex items-center space-x-2 text-gray-600 hover:text-gray-900"
              >
                <ArrowLeft className="w-5 h-5" />
                <span>Back</span>
              </button>
              <div className="flex items-center space-x-3">
                <Shield className="w-6 h-6 text-primary-600" />
                <div>
                  <h1 className="text-2xl font-bold text-gray-900">Analysis Results</h1>
                  <p className="text-sm text-gray-500">
                    {analysis.repository.name} • {analysis.commitId.substring(0, 8)}
                  </p>
                </div>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <div className={`px-3 py-1 rounded-full text-sm font-medium ${
                analysis.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                analysis.status === 'FAILED' ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'
              }`}>
                {analysis.status}
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Analysis Info */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="space-y-4">
              <div>
                <div className="flex items-center space-x-2 mb-2">
                  <GitCommit className="w-4 h-4 text-gray-500" />
                  <span className="text-sm font-medium text-gray-700">Commit Details</span>
                </div>
                <p className="text-sm text-gray-900 font-medium">{analysis.commitMessage}</p>
                <div className="flex items-center space-x-2 mt-1 text-sm text-gray-500">
                  <User className="w-3 h-3" />
                  <span>{analysis.author}</span>
                  <span>•</span>
                  <Calendar className="w-3 h-3" />
                  <span>{formatDate(analysis.timestamp)}</span>
                </div>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <div className="flex items-center space-x-2 mb-2">
                  <Code className="w-4 h-4 text-gray-500" />
                  <span className="text-sm font-medium text-gray-700">Scan Configuration</span>
                </div>
                <div className="space-y-1 text-sm text-gray-600">
                  <div>Language: {analysis.scanConfig.language}</div>
                  <div>Rules: {analysis.scanConfig.rules.join(', ')}</div>
                  <div>Duration: {analysis.duration}</div>
                </div>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <div className="flex items-center space-x-2 mb-2">
                  <Bug className="w-4 h-4 text-gray-500" />
                  <span className="text-sm font-medium text-gray-700">Findings Summary</span>
                </div>
                <div className="grid grid-cols-2 gap-2 text-sm">
                  <div className="flex items-center space-x-1">
                    <div className="w-2 h-2 bg-red-500 rounded-full"></div>
                    <span>{analysis.findings.critical} Critical</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <div className="w-2 h-2 bg-orange-500 rounded-full"></div>
                    <span>{analysis.findings.high} High</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <div className="w-2 h-2 bg-yellow-500 rounded-full"></div>
                    <span>{analysis.findings.medium} Medium</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                    <span>{analysis.findings.low} Low</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Filters */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <Filter className="w-4 h-4 text-gray-500" />
                <span className="text-sm font-medium text-gray-700">Filter by severity:</span>
                <select
                  value={filterSeverity}
                  onChange={(e) => setFilterSeverity(e.target.value)}
                  className="px-2 py-1 border border-gray-300 rounded text-sm"
                >
                  <option value="all">All</option>
                  <option value="critical">Critical</option>
                  <option value="high">High</option>
                  <option value="medium">Medium</option>
                  <option value="low">Low</option>
                </select>
              </div>
              <div className="flex items-center space-x-2">
                <Search className="w-4 h-4 text-gray-500" />
                <input
                  type="text"
                  placeholder="Search findings..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="px-3 py-1 border border-gray-300 rounded text-sm w-64"
                />
              </div>
            </div>
            <div className="text-sm text-gray-500">
              {filteredFindings.length} of {getTotalIssues()} findings
            </div>
          </div>
        </div>

        {/* Findings */}
        <div className="space-y-4">
          {filteredFindings.map((finding) => (
            <div key={finding.id} className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-start space-x-3">
                  {getSeverityIcon(finding.severity)}
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">{finding.title}</h3>
                    <div className="flex items-center space-x-2 mt-1">
                      <span className={`px-2 py-1 rounded text-xs font-medium ${getSeverityColor(finding.severity)}`}>
                        {finding.severity}
                      </span>
                      <span className="text-sm text-gray-500">{finding.type}</span>
                    </div>
                  </div>
                </div>
                <div className="flex items-center space-x-2 text-sm text-gray-500">
                  <span>{finding.cwe}</span>
                  <span>•</span>
                  <span>{finding.owasp}</span>
                </div>
              </div>

              <p className="text-gray-700 mb-4">{finding.description}</p>

              <div className="bg-gray-50 rounded-lg p-4 mb-4">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center space-x-2">
                    <FileText className="w-4 h-4 text-gray-500" />
                    <span className="text-sm font-medium text-gray-700">
                      {finding.file}:{finding.line}
                    </span>
                  </div>
                  <button className="text-xs text-primary-600 hover:text-primary-700 flex items-center space-x-1">
                    <Eye className="w-3 h-3" />
                    <span>View File</span>
                  </button>
                </div>
                <pre className="text-sm text-gray-800 font-mono bg-white p-2 rounded border">
                  {finding.code}
                </pre>
              </div>

              <div className="bg-blue-50 rounded-lg p-4">
                <div className="flex items-center space-x-2 mb-2">
                  <CheckCircle className="w-4 h-4 text-blue-600" />
                  <span className="text-sm font-medium text-blue-900">Recommendation</span>
                </div>
                <p className="text-sm text-blue-800">{finding.recommendation}</p>
              </div>
            </div>
          ))}
        </div>

        {filteredFindings.length === 0 && (
          <div className="text-center py-8">
            <Bug className="w-12 h-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-500 text-lg">No findings match your filters</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default AnalysisResultsPage;