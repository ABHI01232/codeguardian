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
  const { id } = useParams();
  const navigate = useNavigate();
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filterSeverity, setFilterSeverity] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    fetchAnalysisDetails();
  }, [id]);

  const fetchAnalysisDetails = async () => {
    try {
      setLoading(true);
      // Mock analysis data
      const mockAnalysis = {
        id: parseInt(id),
        commitId: 'abc123456',
        commitMessage: 'Fix authentication vulnerabilities in login module',
        author: 'Security Team',
        timestamp: new Date().toISOString(),
        repository: {
          id: 1,
          name: 'secure-banking-app',
          fullName: 'company/secure-banking-app',
          platform: 'GITHUB'
        },
        status: 'COMPLETED',
        duration: '2m 34s',
        scanConfig: {
          language: 'Java',
          rules: ['security', 'quality', 'performance'],
          scope: 'full'
        },
        findings: {
          critical: 1,
          high: 2,
          medium: 5,
          low: 8
        },
        details: [
          {
            id: 1,
            type: 'SQL_INJECTION',
            severity: 'CRITICAL',
            title: 'SQL Injection vulnerability in UserService',
            description: 'Direct concatenation of user input in SQL query without parameterization',
            file: 'src/main/java/com/bank/service/UserService.java',
            line: 45,
            column: 23,
            code: 'String query = "SELECT * FROM users WHERE username = \'" + username + "\'";',
            recommendation: 'Use parameterized queries or prepared statements',
            cwe: 'CWE-89',
            owasp: 'A03:2021 - Injection'
          },
          {
            id: 2,
            type: 'HARDCODED_SECRET',
            severity: 'HIGH',
            title: 'Hardcoded database password',
            description: 'Database password is hardcoded in the source code',
            file: 'src/main/resources/application.properties',
            line: 12,
            column: 1,
            code: 'spring.datasource.password=secret123',
            recommendation: 'Use environment variables or secure configuration',
            cwe: 'CWE-798',
            owasp: 'A07:2021 - Identification and Authentication Failures'
          },
          {
            id: 3,
            type: 'XSS',
            severity: 'HIGH',
            title: 'Cross-Site Scripting (XSS) vulnerability',
            description: 'User input is directly rendered without sanitization',
            file: 'src/main/java/com/bank/controller/UserController.java',
            line: 78,
            column: 15,
            code: 'model.addAttribute("userInput", request.getParameter("input"));',
            recommendation: 'Sanitize user input before rendering',
            cwe: 'CWE-79',
            owasp: 'A03:2021 - Injection'
          },
          {
            id: 4,
            type: 'WEAK_CRYPTO',
            severity: 'MEDIUM',
            title: 'Weak cryptographic algorithm',
            description: 'Using deprecated MD5 hash algorithm',
            file: 'src/main/java/com/bank/util/CryptoUtil.java',
            line: 32,
            column: 28,
            code: 'MessageDigest md = MessageDigest.getInstance("MD5");',
            recommendation: 'Use SHA-256 or stronger algorithms',
            cwe: 'CWE-327',
            owasp: 'A02:2021 - Cryptographic Failures'
          }
        ]
      };

      setAnalysis(mockAnalysis);
    } catch (err) {
      setError('Failed to load analysis details');
    } finally {
      setLoading(false);
    }
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

  return (
    <div className="min-h-screen bg-gray-50">
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