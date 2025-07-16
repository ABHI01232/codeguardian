import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import React from 'react';

class WebSocketService {
  constructor() {
    this.client = null;
    this.listeners = new Map();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 1000;
    this.subscriptions = new Map();
  }

  connect(url = 'http://localhost:8080/ws') {
    try {
      this.client = new Client({
        webSocketFactory: () => new SockJS(url),
        reconnectDelay: this.reconnectDelay,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        
        onConnect: () => {
          console.log('✅ WebSocket connected');
          this.reconnectAttempts = 0;
          this.notifyListeners('connection', { status: 'connected' });
          this.subscribeToNotifications();
        },
        
        onDisconnect: () => {
          console.log('❌ WebSocket disconnected');
          this.notifyListeners('connection', { status: 'disconnected' });
          this.subscriptions.clear();
        },
        
        onStompError: (frame) => {
          console.error('🔌 WebSocket STOMP error:', frame);
          this.notifyListeners('connection', { status: 'error', error: frame.body });
        },
        
        onWebSocketError: (error) => {
          console.error('🔌 WebSocket error:', error);
          this.notifyListeners('connection', { status: 'error', error: error.message });
        }
      });

      this.client.activate();

    } catch (error) {
      console.error('Failed to initialize WebSocket:', error);
    }
  }

  subscribeToNotifications() {
    if (!this.client || !this.client.connected) {
      console.warn('Cannot subscribe: WebSocket not connected');
      return;
    }

    // Subscribe to notifications topic
    const subscription = this.client.subscribe('/topic/notifications', (message) => {
      try {
        const data = JSON.parse(message.body);
        console.log('📱 Notification received:', data);
        this.notifyListeners('notification', data);
      } catch (error) {
        console.error('Error parsing notification:', error);
      }
    });

    this.subscriptions.set('/topic/notifications', subscription);
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.listeners.clear();
      this.subscriptions.clear();
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
  emit(destination, data) {
    if (this.client && this.client.connected) {
      this.client.publish({
        destination: destination,
        body: JSON.stringify(data)
      });
    } else {
      console.warn('Cannot send message: WebSocket not connected');
    }
  }

  // Check connection status
  isConnected() {
    return this.client && this.client.connected;
  }

  // Get connection status
  getStatus() {
    if (!this.client) return 'disconnected';
    return this.client.connected ? 'connected' : 'connecting';
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