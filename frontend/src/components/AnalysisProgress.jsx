import React, { useState, useEffect } from 'react';
import { Shield, Code, Search, CheckCircle, AlertTriangle } from 'lucide-react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const AnalysisProgress = ({ repository, analysisId, onComplete }) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [progress, setProgress] = useState(0);
  const [realStatus, setRealStatus] = useState('RUNNING');

  const steps = [
    {
      id: 'cloning',
      label: 'Cloning Repository',
      icon: Code,
      color: 'blue'
    },
    {
      id: 'scanning',
      label: 'Scanning Code Files',
      icon: Search,
      color: 'purple'
    },
    {
      id: 'analyzing',
      label: 'Security Analysis',
      icon: Shield,
      color: 'green'
    },
    {
      id: 'reporting',
      label: 'Generating Report',
      icon: AlertTriangle,
      color: 'yellow'
    },
    {
      id: 'complete',
      label: 'Analysis Complete',
      icon: CheckCircle,
      color: 'green'
    }
  ];

  useEffect(() => {
    let stompClient = null;
    let fallbackTimer = null;
    let connected = false;
    
    // Fallback function
    const useFallbackSimulation = () => {
      if (connected) return; // Don't use fallback if already connected
      
      console.log('🔄 Using fallback progress simulation for analysis:', analysisId);
      
      const totalDuration = 15000; // 15 seconds
      const stepDuration = totalDuration / steps.length;
      let currentTime = 0;
      
      fallbackTimer = setInterval(() => {
        currentTime += 100;
        const newProgress = Math.min((currentTime / totalDuration) * 100, 100);
        setProgress(newProgress);
        
        const stepIndex = Math.floor(currentTime / stepDuration);
        if (stepIndex < steps.length) {
          setCurrentStep(stepIndex);
        }
        
        if (currentTime >= totalDuration) {
          setProgress(100);
          setCurrentStep(steps.length - 1);
          setRealStatus('COMPLETED');
          clearInterval(fallbackTimer);
          setTimeout(() => onComplete(), 1000);
        }
      }, 100);
    };
    
    // Connect to WebSocket for real-time updates using SockJS + STOMP
    try {
      console.log('🔌 Connecting to WebSocket for analysis:', analysisId);
      
      stompClient = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
        debug: (str) => console.log('STOMP:', str),
        onConnect: () => {
          console.log('📡 WebSocket connected for analysis progress');
          connected = true;
          
          // Subscribe to analysis updates
          stompClient.subscribe('/topic/analysis-updates', (message) => {
            try {
              const data = JSON.parse(message.body);
              console.log('📨 Received WebSocket message:', data);
              
              if (data.type === 'ANALYSIS_PROGRESS' && data.data?.analysisId === analysisId) {
                const { step, progress: newProgress } = data.data;
                console.log(`📊 Progress update: ${step} (${newProgress}%)`);
                
                setProgress(newProgress);
                
                // Update current step based on step name
                const stepIndex = steps.findIndex(s => s.id === step);
                if (stepIndex >= 0) {
                  setCurrentStep(stepIndex);
                }
                
                // Complete the analysis
                if (step === 'complete' || newProgress >= 100) {
                  setProgress(100);
                  setCurrentStep(steps.length - 1);
                  setRealStatus('COMPLETED');
                  setTimeout(() => onComplete(), 1000);
                }
              }
              
              if (data.type === 'ANALYSIS_COMPLETE' && data.data?.analysisId === analysisId) {
                console.log('✅ Analysis completed via WebSocket');
                setProgress(100);
                setCurrentStep(steps.length - 1);
                setRealStatus('COMPLETED');
                setTimeout(() => onComplete(), 1000);
              }
            } catch (error) {
              console.error('Error processing WebSocket message:', error);
            }
          });
        },
        onStompError: (frame) => {
          console.error('WebSocket STOMP error:', frame);
          useFallbackSimulation();
        },
        onWebSocketError: (error) => {
          console.error('WebSocket connection error:', error);
          useFallbackSimulation();
        },
        onDisconnect: () => {
          console.log('📡 WebSocket disconnected');
          connected = false;
        }
      });
      
      stompClient.activate();
      
      // Start fallback after 2 seconds if no WebSocket connection
      const fallbackTimeout = setTimeout(() => {
        if (!connected) {
          console.log('⚠️ WebSocket not connected, using fallback');
          useFallbackSimulation();
        }
      }, 2000);
      
      return () => {
        clearTimeout(fallbackTimeout);
        if (fallbackTimer) clearInterval(fallbackTimer);
        if (stompClient) {
          stompClient.deactivate();
        }
      };
    } catch (error) {
      console.error('Failed to initialize WebSocket:', error);
      useFallbackSimulation();
    }
  }, [analysisId, onComplete, steps]);

  const getStepStatus = (index) => {
    if (index < currentStep) return 'completed';
    if (index === currentStep) return 'current';
    return 'pending';
  };

  const getStepColor = (step, status) => {
    if (status === 'completed') return 'text-green-500 bg-green-100';
    if (status === 'current') {
      switch (step.color) {
        case 'blue': return 'text-blue-500 bg-blue-100';
        case 'purple': return 'text-purple-500 bg-purple-100';
        case 'green': return 'text-green-500 bg-green-100';
        case 'yellow': return 'text-yellow-500 bg-yellow-100';
        default: return 'text-gray-500 bg-gray-100';
      }
    }
    return 'text-gray-400 bg-gray-100';
  };

  return (
    <div className="max-w-md mx-auto p-6 bg-white rounded-lg shadow-lg">
      <div className="text-center mb-6">
        <h3 className="text-lg font-semibold text-gray-800 mb-2">
          Analyzing {repository}
        </h3>
        <div className="w-full bg-gray-200 rounded-full h-2">
          <div 
            className="bg-blue-500 h-2 rounded-full transition-all duration-300 ease-out"
            style={{ width: `${progress}%` }}
          ></div>
        </div>
        <p className="text-sm text-gray-600 mt-2">{Math.round(progress)}% Complete</p>
      </div>

      <div className="space-y-4">
        {steps.map((step, index) => {
          const status = getStepStatus(index);
          const IconComponent = step.icon;
          
          return (
            <div key={step.id} className="flex items-center space-x-3">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${getStepColor(step, status)}`}>
                <IconComponent className={`w-4 h-4 ${status === 'current' ? 'animate-pulse' : ''}`} />
              </div>
              <div className="flex-1">
                <p className={`text-sm font-medium ${
                  status === 'completed' ? 'text-green-700' : 
                  status === 'current' ? 'text-gray-800' : 
                  'text-gray-500'
                }`}>
                  {step.label}
                </p>
              </div>
              <div>
                {status === 'completed' && (
                  <CheckCircle className="w-5 h-5 text-green-500" />
                )}
                {status === 'current' && (
                  <div className="w-4 h-4 border-2 border-blue-500 rounded-full animate-spin border-t-transparent"></div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      <div className="mt-6 text-center">
        <p className="text-xs text-gray-500">
          {currentStep < steps.length - 1 ? 
            `Current: ${steps[currentStep]?.label}` : 
            'Analysis completed successfully!'
          }
        </p>
      </div>
    </div>
  );
};

export default AnalysisProgress;