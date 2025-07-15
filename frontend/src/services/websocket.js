import { io } from 'socket.io-client';
import React from 'react';

class WebSocketService {
  constructor() {
    this.socket = null;
    this.listeners = new Map();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 1000;
  }

  connect(url = 'ws://localhost:8080') {
    try {
      this.socket = io(url, {
        transports: ['websocket'],
        autoConnect: true,
        reconnection: true,
        reconnectionAttempts: this.maxReconnectAttempts,
        reconnectionDelay: this.reconnectDelay,
      });

      this.socket.on('connect', () => {
        console.log('✅ WebSocket connected');
        this.reconnectAttempts = 0;
        this.notifyListeners('connection', { status: 'connected' });
      });

      this.socket.on('disconnect', (reason) => {
        console.log('❌ WebSocket disconnected:', reason);
        this.notifyListeners('connection', { status: 'disconnected', reason });
      });

      this.socket.on('connect_error', (error) => {
        console.error('🔌 WebSocket connection error:', error);
        this.notifyListeners('connection', { status: 'error', error: error.message });
      });

      this.socket.on('reconnect', (attemptNumber) => {
        console.log(`🔄 WebSocket reconnected after ${attemptNumber} attempts`);
        this.notifyListeners('connection', { status: 'reconnected', attemptNumber });
      });

      // Listen for analysis updates
      this.socket.on('analysis-update', (data) => {
        console.log('📊 Analysis update received:', data);
        this.notifyListeners('analysisUpdate', data);
      });

      // Listen for repository updates  
      this.socket.on('repository-update', (data) => {
        console.log('📁 Repository update received:', data);
        this.notifyListeners('repositoryUpdate', data);
      });

      // Listen for system alerts
      this.socket.on('system-alert', (data) => {
        console.log('🚨 System alert received:', data);
        this.notifyListeners('systemAlert', data);
      });

    } catch (error) {
      console.error('Failed to initialize WebSocket:', error);
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
      this.listeners.clear();
    }
  }

  // Subscribe to specific events
  subscribe(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set());
    }
    this.listeners.get(event).add(callback);

    // Return unsubscribe function
    return () => {
      const eventListeners = this.listeners.get(event);
      if (eventListeners) {
        eventListeners.delete(callback);
        if (eventListeners.size === 0) {
          this.listeners.delete(event);
        }
      }
    };
  }

  // Notify all listeners for an event
  notifyListeners(event, data) {
    const eventListeners = this.listeners.get(event);
    if (eventListeners) {
      eventListeners.forEach(callback => {
        try {
          callback(data);
        } catch (error) {
          console.error(`Error in WebSocket listener for ${event}:`, error);
        }
      });
    }
  }

  // Send data to server
  emit(event, data) {
    if (this.socket && this.socket.connected) {
      this.socket.emit(event, data);
    } else {
      console.warn('Cannot emit event: WebSocket not connected');
    }
  }

  // Check connection status
  isConnected() {
    return this.socket && this.socket.connected;
  }

  // Get connection status
  getStatus() {
    if (!this.socket) return 'disconnected';
    return this.socket.connected ? 'connected' : 'connecting';
  }
}

// Create singleton instance
const webSocketService = new WebSocketService();

// React hook for WebSocket
export const useWebSocket = (event, callback) => {
  const [isConnected, setIsConnected] = React.useState(webSocketService.isConnected());

  React.useEffect(() => {
    // Subscribe to connection status
    const unsubscribeConnection = webSocketService.subscribe('connection', (data) => {
      setIsConnected(data.status === 'connected' || data.status === 'reconnected');
    });

    // Subscribe to specific event if provided
    let unsubscribeEvent;
    if (event && callback) {
      unsubscribeEvent = webSocketService.subscribe(event, callback);
    }

    // Cleanup
    return () => {
      unsubscribeConnection();
      if (unsubscribeEvent) {
        unsubscribeEvent();
      }
    };
  }, [event, callback]);

  return {
    isConnected,
    emit: webSocketService.emit.bind(webSocketService),
    subscribe: webSocketService.subscribe.bind(webSocketService)
  };
};

export default webSocketService;