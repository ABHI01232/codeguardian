import React, { useState, useEffect, useRef } from 'react';
import { Terminal, X, Maximize2, Minimize2 } from 'lucide-react';

const AnalysisLogs = ({ isOpen, onClose, repositoryName, analysisId }) => {
  const [logs, setLogs] = useState([]);
  const [isMaximized, setIsMaximized] = useState(false);
  const logsEndRef = useRef(null);
  const logsContainerRef = useRef(null);

  useEffect(() => {
    if (isOpen && analysisId) {
      // Simulate real-time logs
      const logMessages = [
        `🚀 Starting security analysis for ${repositoryName}`,
        `📁 Cloning repository...`,
        `✅ Repository cloned successfully`,
        `🔍 Initializing code scanners...`,
        `📋 Loading security rules: OWASP Top 10, CWE, SANS`,
        `🔐 Running security vulnerability scan...`,
        `⚡ Scanning for injection vulnerabilities...`,
        `🛡️ Checking authentication mechanisms...`,
        `🔒 Analyzing cryptographic implementations...`,
        `📊 Processing scan results...`,
        `🎯 Identifying critical issues...`,
        `📈 Calculating security score...`,
        `📝 Generating vulnerability recommendations...`,
        `✨ Analysis completed successfully!`,
        `📊 Found 3 critical, 5 high, 8 medium, 12 low severity issues`,
        `🔗 Report available at /analysis/${analysisId}`
      ];

      let currentIndex = 0;
      const interval = setInterval(() => {
        if (currentIndex < logMessages.length) {
          const timestamp = new Date().toLocaleTimeString();
          setLogs(prev => [...prev, {
            id: Date.now() + Math.random(),
            timestamp,
            message: logMessages[currentIndex],
            type: currentIndex === logMessages.length - 1 ? 'success' : 'info'
          }]);
          currentIndex++;
        } else {
          clearInterval(interval);
        }
      }, 800);

      return () => clearInterval(interval);
    }
  }, [isOpen, analysisId, repositoryName]);

  useEffect(() => {
    // Auto-scroll to bottom when new logs are added
    if (logsEndRef.current && logsContainerRef.current) {
      const container = logsContainerRef.current;
      const isScrolledToBottom = container.scrollHeight - container.clientHeight <= container.scrollTop + 1;
      
      if (isScrolledToBottom) {
        logsEndRef.current.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }, [logs]);

  const handleClose = () => {
    setLogs([]);
    setIsMaximized(false);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className={`bg-white rounded-lg shadow-xl transition-all duration-300 ${
        isMaximized ? 'w-full h-full m-4' : 'w-3/4 h-3/4 max-w-4xl'
      }`}>
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <Terminal className="w-5 h-5 text-gray-600" />
            <div>
              <h3 className="text-lg font-semibold text-gray-900">
                Analysis Logs
              </h3>
              <p className="text-sm text-gray-500">
                {repositoryName} • Analysis ID: {analysisId}
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setIsMaximized(!isMaximized)}
              className="p-2 text-gray-400 hover:text-gray-600 rounded"
            >
              {isMaximized ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
            </button>
            <button
              onClick={handleClose}
              className="p-2 text-gray-400 hover:text-gray-600 rounded"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Logs Container */}
        <div 
          ref={logsContainerRef}
          className="flex-1 overflow-y-auto bg-gray-900 text-green-400 font-mono text-sm p-4"
          style={{ height: isMaximized ? 'calc(100vh - 160px)' : '500px' }}
        >
          <div className="space-y-1">
            {logs.map((log) => (
              <div key={log.id} className="flex items-start space-x-3">
                <span className="text-gray-500 text-xs whitespace-nowrap">
                  [{log.timestamp}]
                </span>
                <span className={`flex-1 ${
                  log.type === 'success' ? 'text-green-400' : 
                  log.type === 'error' ? 'text-red-400' : 'text-green-400'
                }`}>
                  {log.message}
                </span>
              </div>
            ))}
            <div ref={logsEndRef} />
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between p-4 border-t border-gray-200 bg-gray-50">
          <div className="flex items-center space-x-2 text-sm text-gray-600">
            <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
            <span>Analysis in progress...</span>
          </div>
          <div className="flex space-x-2">
            <button
              onClick={() => {
                // Copy logs to clipboard
                const logText = logs.map(log => `[${log.timestamp}] ${log.message}`).join('\n');
                navigator.clipboard.writeText(logText);
                window.showToast && window.showToast('Logs copied to clipboard', 'success', 2000);
              }}
              className="px-3 py-1 text-sm text-gray-600 hover:text-gray-800 border border-gray-300 rounded hover:bg-gray-100"
            >
              Copy Logs
            </button>
            <button
              onClick={handleClose}
              className="px-3 py-1 text-sm bg-primary-600 text-white rounded hover:bg-primary-700"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AnalysisLogs;