import React, { useState, useEffect } from 'react';
import { 
  Activity, 
  Wifi, 
  WifiOff, 
  Clock, 
  AlertTriangle,
  CheckCircle,
  GitCommit,
  Shield,
  Bell
} from 'lucide-react';

const RealTimeUpdates = () => {
  const [updates, setUpdates] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const [lastUpdate, setLastUpdate] = useState(null);

  // Simulate real-time updates (replace with actual WebSocket connection)
  useEffect(() => {
    // Simulate connection status
    setIsConnected(true);
    
    // Add initial demo updates
    const initialUpdates = [
      {
        id: 1,
        type: 'analysis_complete',
        title: 'Security Analysis Complete',
        message: 'banking-api: 2 critical issues found',
        timestamp: new Date(Date.now() - 5 * 60 * 1000),
        severity: 'high',
        repository: 'banking-api'
      },
      {
        id: 2,
        type: 'webhook_received',
        title: 'New Commit Received',
        message: 'payment-service: commit abc123 pushed',
        timestamp: new Date(Date.now() - 15 * 60 * 1000),
        severity: 'info',
        repository: 'payment-service'
      },
      {
        id: 3,
        type: 'system_health',
        title: 'System Health Check',
        message: 'All services operational',
        timestamp: new Date(Date.now() - 30 * 60 * 1000),
        severity: 'success',
        repository: null
      }
    ];
    
    setUpdates(initialUpdates);
    setLastUpdate(new Date());

    // Simulate periodic updates
    const interval = setInterval(() => {
      const mockUpdates = [
        {
          type: 'analysis_complete',
          title: 'Analysis Complete',
          message: `Repository analyzed: ${Math.floor(Math.random() * 10)} issues found`,
          severity: Math.random() > 0.7 ? 'high' : Math.random() > 0.4 ? 'medium' : 'low',
          repository: `repo-${Math.floor(Math.random() * 5) + 1}`
        },
        {
          type: 'webhook_received',
          title: 'New Webhook',
          message: `Commit ${Math.random().toString(36).substring(7)} received`,
          severity: 'info',
          repository: `service-${Math.floor(Math.random() * 3) + 1}`
        },
        {
          type: 'security_alert',
          title: 'Security Alert',
          message: 'Potential vulnerability detected',
          severity: 'high',
          repository: 'critical-service'
        }
      ];

      const randomUpdate = mockUpdates[Math.floor(Math.random() * mockUpdates.length)];
      const newUpdate = {
        id: Date.now(),
        ...randomUpdate,
        timestamp: new Date()
      };

      setUpdates(prev => [newUpdate, ...prev.slice(0, 9)]); // Keep last 10 updates
      setLastUpdate(new Date());
    }, 10000); // New update every 10 seconds

    return () => clearInterval(interval);
  }, []);

  const getUpdateIcon = (type) => {
    switch (type) {
      case 'analysis_complete':
        return <Shield className="w-4 h-4" />;
      case 'webhook_received':
        return <GitCommit className="w-4 h-4" />;
      case 'security_alert':
        return <AlertTriangle className="w-4 h-4" />;
      case 'system_health':
        return <CheckCircle className="w-4 h-4" />;
      default:
        return <Bell className="w-4 h-4" />;
    }
  };

  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'high':
      case 'critical':
        return 'text-red-600 bg-red-50 border-l-red-500';
      case 'medium':
        return 'text-yellow-600 bg-yellow-50 border-l-yellow-500';
      case 'low':
      case 'info':
        return 'text-blue-600 bg-blue-50 border-l-blue-500';
      case 'success':
        return 'text-green-600 bg-green-50 border-l-green-500';
      default:
        return 'text-gray-600 bg-gray-50 border-l-gray-500';
    }
  };

  const formatTimestamp = (timestamp) => {
    const now = new Date();
    const diff = now - timestamp;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    return `${days}d ago`;
  };

  return (
    <div className="space-y-4">
      {/* Connection Status */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2">
          {isConnected ? (
            <>
              <Wifi className="w-4 h-4 text-success-500" />
              <span className="text-sm text-success-600 font-medium">Connected</span>
            </>
          ) : (
            <>
              <WifiOff className="w-4 h-4 text-danger-500" />
              <span className="text-sm text-danger-600 font-medium">Disconnected</span>
            </>
          )}
        </div>
        <div className="flex items-center space-x-1 text-xs text-gray-500">
          <Clock className="w-3 h-3" />
          <span>
            {lastUpdate ? formatTimestamp(lastUpdate) : 'Never'}
          </span>
        </div>
      </div>

      {/* Updates List */}
      <div className="space-y-2 max-h-96 overflow-y-auto">
        {updates.length === 0 ? (
          <div className="text-center py-8">
            <Activity className="w-8 h-8 text-gray-400 mx-auto mb-2" />
            <p className="text-gray-500 text-sm">No recent updates</p>
            <p className="text-gray-400 text-xs">Real-time updates will appear here</p>
          </div>
        ) : (
          updates.map((update) => (
            <div
              key={update.id}
              className={`border-l-4 pl-3 py-2 ${getSeverityColor(update.severity)}`}
            >
              <div className="flex items-center justify-between mb-1">
                <div className="flex items-center space-x-2">
                  {getUpdateIcon(update.type)}
                  <span className="text-sm font-medium">{update.title}</span>
                </div>
                <span className="text-xs opacity-75">
                  {formatTimestamp(update.timestamp)}
                </span>
              </div>
              <p className="text-sm opacity-90 mb-1">{update.message}</p>
              {update.repository && (
                <div className="flex items-center space-x-1">
                  <GitCommit className="w-3 h-3 opacity-60" />
                  <span className="text-xs opacity-75 font-mono">
                    {update.repository}
                  </span>
                </div>
              )}
            </div>
          ))
        )}
      </div>

      {/* Update Statistics */}
      <div className="border-t pt-3">
        <div className="grid grid-cols-2 gap-4 text-center">
          <div>
            <div className="text-lg font-semibold text-gray-900">{updates.length}</div>
            <div className="text-xs text-gray-500">Recent Updates</div>
          </div>
          <div>
            <div className="text-lg font-semibold text-gray-900">
              {updates.filter(u => u.severity === 'high' || u.severity === 'critical').length}
            </div>
            <div className="text-xs text-gray-500">High Priority</div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="border-t pt-3">
        <div className="flex space-x-2">
          <button className="flex-1 text-xs text-gray-600 hover:text-gray-700 px-2 py-1 rounded border border-gray-200 hover:bg-gray-50">
            View All
          </button>
          <button className="flex-1 text-xs text-primary-600 hover:text-primary-700 px-2 py-1 rounded border border-primary-200 hover:bg-primary-50">
            Settings
          </button>
        </div>
      </div>
    </div>
  );
};

export default RealTimeUpdates;