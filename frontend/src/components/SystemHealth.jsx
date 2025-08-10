import React, { useState, useEffect } from 'react';
import { 
  Server, 
  Database, 
  Activity, 
  CheckCircle, 
  AlertTriangle, 
  XCircle,
  Clock,
  Cpu,
  HardDrive,
  Wifi
} from 'lucide-react';

const SystemHealth = ({ health = {} }) => {
  const [services, setServices] = useState([]);
  const [systemMetrics, setSystemMetrics] = useState({});

  useEffect(() => {
    // Only show real health data - no mock data
    if (health && health.services) {
      setServices(health.services);
    } else {
      setServices([]);
    }

    if (health && health.metrics) {
      setSystemMetrics(health.metrics);
    } else {
      setSystemMetrics({});
    }
  }, [health]);

  const getStatusIcon = (status) => {
    switch (status?.toUpperCase()) {
      case 'UP':
        return <CheckCircle className="w-4 h-4 text-success-500" />;
      case 'DOWN':
        return <XCircle className="w-4 h-4 text-danger-500" />;
      case 'WARNING':
        return <AlertTriangle className="w-4 h-4 text-warning-500" />;
      default:
        return <Clock className="w-4 h-4 text-gray-500" />;
    }
  };

  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case 'UP':
        return 'text-success-600 bg-success-50 border-success-200';
      case 'DOWN':
        return 'text-danger-600 bg-danger-50 border-danger-200';
      case 'WARNING':
        return 'text-warning-600 bg-warning-50 border-warning-200';
      default:
        return 'text-gray-600 bg-gray-50 border-gray-200';
    }
  };

  const formatTimestamp = (timestamp) => {
    return timestamp.toLocaleTimeString();
  };

  const getOverallHealth = () => {
    const downServices = services.filter(s => s.status === 'DOWN').length;
    const warningServices = services.filter(s => s.status === 'WARNING').length;
    
    if (downServices > 0) return 'DOWN';
    if (warningServices > 0) return 'WARNING';
    return 'UP';
  };

  const overallHealth = getOverallHealth();

  return (
    <div className="space-y-4">
      {/* Overall Status */}
      <div className="text-center">
        <div className="flex items-center justify-center space-x-2 mb-2">
          {getStatusIcon(overallHealth)}
          <span className={`px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(overallHealth)}`}>
            System {overallHealth}
          </span>
        </div>
        <p className="text-xs text-gray-500">
          Last updated: {formatTimestamp(new Date())}
        </p>
      </div>

      {/* System Metrics */}
      <div className="grid grid-cols-2 gap-3">
        <div className="text-center p-2 bg-gray-50 rounded">
          <Cpu className="w-4 h-4 text-gray-600 mx-auto mb-1" />
          <div className="text-sm font-semibold">{systemMetrics.cpu || '-'}</div>
          <div className="text-xs text-gray-500">CPU</div>
        </div>
        <div className="text-center p-2 bg-gray-50 rounded">
          <HardDrive className="w-4 h-4 text-gray-600 mx-auto mb-1" />
          <div className="text-sm font-semibold">{systemMetrics.memory || '-'}</div>
          <div className="text-xs text-gray-500">Memory</div>
        </div>
        <div className="text-center p-2 bg-gray-50 rounded">
          <Database className="w-4 h-4 text-gray-600 mx-auto mb-1" />
          <div className="text-sm font-semibold">{systemMetrics.disk || '-'}</div>
          <div className="text-xs text-gray-500">Disk</div>
        </div>
        <div className="text-center p-2 bg-gray-50 rounded">
          <Wifi className="w-4 h-4 text-gray-600 mx-auto mb-1" />
          <div className="text-sm font-semibold">{systemMetrics.network || '-'}</div>
          <div className="text-xs text-gray-500">Network</div>
        </div>
      </div>

      {/* Services Status */}
      <div className="space-y-2">
        <h3 className="text-sm font-medium text-gray-700 mb-2">Services</h3>
        {services.length === 0 ? (
          <div className="text-center py-4">
            <Server className="w-8 h-8 text-gray-400 mx-auto mb-2" />
            <p className="text-sm text-gray-500">No health data available</p>
            <p className="text-xs text-gray-400">Services will appear here when available</p>
          </div>
        ) : (
          services.map((service, index) => (
            <div key={index} className="flex items-center justify-between p-2 bg-gray-50 rounded">
              <div className="flex items-center space-x-2">
                {getStatusIcon(service.status)}
                <span className="text-sm font-medium">{service.name}</span>
              </div>
              <div className="text-right">
                <div className="text-xs text-gray-600">{service.responseTime}</div>
                <div className="text-xs text-gray-500">Uptime: {service.uptime}</div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Health Summary */}
      <div className="border-t pt-3">
        <div className="grid grid-cols-3 gap-2 text-center">
          <div>
            <div className="text-lg font-semibold text-success-600">
              {services.filter(s => s.status === 'UP').length}
            </div>
            <div className="text-xs text-gray-500">Healthy</div>
          </div>
          <div>
            <div className="text-lg font-semibold text-warning-600">
              {services.filter(s => s.status === 'WARNING').length}
            </div>
            <div className="text-xs text-gray-500">Warning</div>
          </div>
          <div>
            <div className="text-lg font-semibold text-danger-600">
              {services.filter(s => s.status === 'DOWN').length}
            </div>
            <div className="text-xs text-gray-500">Down</div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="border-t pt-3">
        <div className="flex space-x-2">
          <button className="flex-1 text-xs text-gray-600 hover:text-gray-700 px-2 py-1 rounded border border-gray-200 hover:bg-gray-50 flex items-center justify-center space-x-1">
            <Activity className="w-3 h-3" />
            <span>Refresh</span>
          </button>
          <button className="flex-1 text-xs text-primary-600 hover:text-primary-700 px-2 py-1 rounded border border-primary-200 hover:bg-primary-50 flex items-center justify-center space-x-1">
            <Server className="w-3 h-3" />
            <span>Details</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default SystemHealth;