import React, { useState, useEffect } from 'react';
import { api } from '../services/api';

const RiskDashboard = () => {
  const [riskOverview, setRiskOverview] = useState(null);
  const [owaspCompliance, setOwaspCompliance] = useState(null);
  const [bankingCompliance, setBankingCompliance] = useState(null);
  const [executiveSummary, setExecutiveSummary] = useState(null);
  const [riskMatrix, setRiskMatrix] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchRiskData();
  }, []);

  const fetchRiskData = async () => {
    try {
      setLoading(true);
      const [overview, owasp, banking, executive, matrix] = await Promise.all([
        api.get('/api/risk-dashboard/overview'),
        api.get('/api/risk-dashboard/compliance/owasp-top10'),
        api.get('/api/risk-dashboard/compliance/banking'),
        api.get('/api/risk-dashboard/executive-summary'),
        api.get('/api/risk-dashboard/risk-matrix')
      ]);

      setRiskOverview(overview.data);
      setOwaspCompliance(owasp.data);
      setBankingCompliance(banking.data);
      setExecutiveSummary(executive.data);
      setRiskMatrix(matrix.data);
    } catch (err) {
      setError('Failed to fetch risk dashboard data');
      console.error('Risk dashboard error:', err);
    } finally {
      setLoading(false);
    }
  };

  const getRiskColorClass = (riskLevel) => {
    switch (riskLevel) {
      case 'CRITICAL': return 'bg-red-100 text-red-800 border-red-200';
      case 'HIGH': return 'bg-orange-100 text-orange-800 border-orange-200';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'LOW': return 'bg-green-100 text-green-800 border-green-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getComplianceColorClass = (score) => {
    if (score >= 80) return 'text-green-600';
    if (score >= 60) return 'text-yellow-600';
    return 'text-red-600';
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <div className="flex">
          <div className="ml-3">
            <h3 className="text-sm font-medium text-red-800">Error</h3>
            <div className="mt-2 text-sm text-red-700">{error}</div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div className="bg-white shadow-sm rounded-lg p-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">🛡️ Risk Score Dashboard</h1>
            <p className="mt-2 text-gray-600">Security Risk Analysis & Compliance Reporting</p>
          </div>
          <div className="flex items-center space-x-4">
            <button 
              onClick={fetchRiskData}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium"
            >
              🔄 Refresh
            </button>
          </div>
        </div>
      </div>

      {/* Risk Overview Cards */}
      {riskOverview && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="bg-white rounded-lg shadow-sm p-6 border-l-4 border-blue-500">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="flex items-center justify-center h-12 w-12 rounded-md bg-blue-500 text-white">
                  📊
                </div>
              </div>
              <div className="ml-4">
                <dt className="text-sm font-medium text-gray-500 truncate">Global Risk Score</dt>
                <dd className="text-3xl font-bold text-gray-900">{riskOverview.globalRiskScore}</dd>
                <dd className={`text-sm font-medium px-2 py-1 rounded-full border ${getRiskColorClass(riskOverview.riskLevel)}`}>
                  {riskOverview.riskLevel}
                </dd>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6 border-l-4 border-red-500">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="flex items-center justify-center h-12 w-12 rounded-md bg-red-500 text-white">
                  🚨
                </div>
              </div>
              <div className="ml-4">
                <dt className="text-sm font-medium text-gray-500 truncate">Critical Findings</dt>
                <dd className="text-3xl font-bold text-red-600">{riskOverview.criticalFindings}</dd>
                <dd className="text-sm text-gray-500">Requires immediate attention</dd>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6 border-l-4 border-green-500">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="flex items-center justify-center h-12 w-12 rounded-md bg-green-500 text-white">
                  🏦
                </div>
              </div>
              <div className="ml-4">
                <dt className="text-sm font-medium text-gray-500 truncate">Banking Compliance</dt>
                <dd className={`text-3xl font-bold ${getComplianceColorClass(riskOverview.bankingComplianceScore)}`}>
                  {riskOverview.bankingComplianceScore}%
                </dd>
                <dd className="text-sm text-gray-500">PCI DSS, SOX, GDPR</dd>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6 border-l-4 border-purple-500">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="flex items-center justify-center h-12 w-12 rounded-md bg-purple-500 text-white">
                  🏢
                </div>
              </div>
              <div className="ml-4">
                <dt className="text-sm font-medium text-gray-500 truncate">Total Repositories</dt>
                <dd className="text-3xl font-bold text-gray-900">{riskOverview.totalRepositories}</dd>
                <dd className="text-sm text-gray-500">Under monitoring</dd>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* OWASP Top 10 Compliance */}
      {owaspCompliance && (
        <div className="bg-white shadow-sm rounded-lg p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">🔒 OWASP Top 10 Compliance</h2>
          <div className="mb-4">
            <div className="flex justify-between items-center mb-2">
              <span className="text-sm font-medium text-gray-700">Overall Compliance Score</span>
              <span className={`text-lg font-bold ${getComplianceColorClass(owaspCompliance.complianceScore)}`}>
                {owaspCompliance.complianceScore}%
              </span>
            </div>
            <div className="bg-gray-200 rounded-full h-3">
              <div 
                className={`h-3 rounded-full ${owaspCompliance.complianceScore >= 80 ? 'bg-green-500' : 
                  owaspCompliance.complianceScore >= 60 ? 'bg-yellow-500' : 'bg-red-500'}`}
                style={{width: `${owaspCompliance.complianceScore}%`}}
              ></div>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {Object.entries(owaspCompliance.owaspTop10Compliance).map(([category, count]) => (
              <div key={category} className="bg-gray-50 rounded-lg p-4">
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium text-gray-700 truncate">{category}</span>
                  <span className={`text-lg font-bold ${count > 0 ? 'text-red-600' : 'text-green-600'}`}>
                    {count}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Banking Compliance Details */}
      {bankingCompliance && (
        <div className="bg-white shadow-sm rounded-lg p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">🏦 Banking Security Compliance</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-blue-50 rounded-lg p-4">
              <h3 className="text-lg font-medium text-blue-900 mb-2">PCI DSS Compliance</h3>
              <div className={`text-3xl font-bold ${getComplianceColorClass(bankingCompliance.pciDssCompliance)}`}>
                {bankingCompliance.pciDssCompliance}%
              </div>
              <p className="text-sm text-blue-700 mt-2">{bankingCompliance.pciDssIssues} issues found</p>
            </div>
            <div className="bg-green-50 rounded-lg p-4">
              <h3 className="text-lg font-medium text-green-900 mb-2">Data Protection</h3>
              <div className={`text-3xl font-bold ${getComplianceColorClass(bankingCompliance.dataProtectionCompliance)}`}>
                {bankingCompliance.dataProtectionCompliance}%
              </div>
              <p className="text-sm text-green-700 mt-2">{bankingCompliance.dataProtectionIssues} issues found</p>
            </div>
            <div className="bg-purple-50 rounded-lg p-4">
              <h3 className="text-lg font-medium text-purple-900 mb-2">Cryptographic</h3>
              <div className={`text-3xl font-bold ${getComplianceColorClass(bankingCompliance.cryptographicCompliance)}`}>
                {bankingCompliance.cryptographicCompliance}%
              </div>
              <p className="text-sm text-purple-700 mt-2">{bankingCompliance.cryptographicIssues} issues found</p>
            </div>
          </div>
          <div className="mt-6 p-4 bg-gray-50 rounded-lg">
            <div className="flex items-center justify-between">
              <span className="text-lg font-medium text-gray-900">Overall Banking Compliance</span>
              <div className="flex items-center space-x-2">
                <span className={`text-2xl font-bold ${getComplianceColorClass(bankingCompliance.overallBankingCompliance)}`}>
                  {bankingCompliance.overallBankingCompliance}%
                </span>
                <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                  bankingCompliance.complianceStatus === 'COMPLIANT' ? 'bg-green-100 text-green-800' :
                  bankingCompliance.complianceStatus === 'NEEDS_IMPROVEMENT' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-red-100 text-red-800'
                }`}>
                  {bankingCompliance.complianceStatus.replace('_', ' ')}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Executive Summary */}
      {executiveSummary && (
        <div className="bg-white shadow-sm rounded-lg p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">📊 Executive Summary</h2>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-3">Key Metrics</h3>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-gray-600">Organization Risk Level:</span>
                  <span className={`font-semibold px-2 py-1 rounded ${getRiskColorClass(executiveSummary.organizationRiskLevel)}`}>
                    {executiveSummary.organizationRiskLevel}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Critical Issues:</span>
                  <span className="font-semibold text-red-600">{executiveSummary.criticalIssues}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Banking Compliance:</span>
                  <span className={`font-semibold ${getComplianceColorClass(executiveSummary.bankingCompliance)}`}>
                    {executiveSummary.bankingCompliance}%
                  </span>
                </div>
              </div>
            </div>
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-3">Key Recommendations</h3>
              <ul className="space-y-2">
                {executiveSummary.keyRecommendations?.map((recommendation, index) => (
                  <li key={index} className="flex items-start">
                    <span className="flex-shrink-0 h-2 w-2 bg-blue-500 rounded-full mt-2 mr-3"></span>
                    <span className="text-sm text-gray-700">{recommendation}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      )}

      {/* Risk Matrix */}
      {riskMatrix && (
        <div className="bg-white shadow-sm rounded-lg p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">🎯 Risk Assessment Matrix</h2>
          <div className="overflow-x-auto">
            <table className="w-full border-collapse border border-gray-300">
              <thead>
                <tr className="bg-gray-50">
                  <th className="border border-gray-300 p-3 text-left">Impact / Likelihood</th>
                  <th className="border border-gray-300 p-3 text-center">High Likelihood</th>
                  <th className="border border-gray-300 p-3 text-center">Medium Likelihood</th>
                  <th className="border border-gray-300 p-3 text-center">Low Likelihood</th>
                </tr>
              </thead>
              <tbody>
                {Object.entries(riskMatrix.riskMatrix).map(([impact, likelihoods]) => (
                  <tr key={impact}>
                    <td className="border border-gray-300 p-3 font-medium bg-gray-50">
                      {impact.replace('_', ' ')}
                    </td>
                    {Object.entries(likelihoods).map(([likelihood, count]) => (
                      <td key={likelihood} className={`border border-gray-300 p-3 text-center font-bold ${
                        impact === 'HIGH_IMPACT' && likelihood === 'HIGH_LIKELIHOOD' ? 'bg-red-100 text-red-800' :
                        impact === 'HIGH_IMPACT' || likelihood === 'HIGH_LIKELIHOOD' ? 'bg-orange-100 text-orange-800' :
                        impact === 'MEDIUM_IMPACT' && likelihood === 'MEDIUM_LIKELIHOOD' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-green-100 text-green-800'
                      }`}>
                        {count}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default RiskDashboard;