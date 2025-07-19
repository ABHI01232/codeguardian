import React from 'react';
import { Shield, Code, Search, CheckCircle } from 'lucide-react';

const LoadingSpinner = ({ message = 'Processing...', type = 'default' }) => {
  const getAnimation = () => {
    switch (type) {
      case 'repository':
        return (
          <div className="flex items-center space-x-2">
            <div className="relative">
              <div className="w-12 h-12 border-4 border-blue-200 rounded-full animate-spin border-t-blue-500"></div>
              <Code className="w-6 h-6 text-blue-500 absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 animate-pulse" />
            </div>
          </div>
        );
      case 'analysis':
        return (
          <div className="flex items-center space-x-2">
            <div className="relative">
              <div className="w-12 h-12 border-4 border-green-200 rounded-full animate-spin border-t-green-500"></div>
              <Shield className="w-6 h-6 text-green-500 absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 animate-pulse" />
            </div>
          </div>
        );
      case 'scanning':
        return (
          <div className="flex items-center space-x-2">
            <div className="relative">
              <div className="w-12 h-12 border-4 border-purple-200 rounded-full animate-spin border-t-purple-500"></div>
              <Search className="w-6 h-6 text-purple-500 absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 animate-bounce" />
            </div>
          </div>
        );
      case 'success':
        return (
          <div className="flex items-center space-x-2">
            <div className="relative">
              <div className="w-12 h-12 border-4 border-green-200 rounded-full animate-pulse border-t-green-500"></div>
              <CheckCircle className="w-6 h-6 text-green-500 absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 animate-ping" />
            </div>
          </div>
        );
      default:
        return (
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 border-4 border-gray-200 rounded-full animate-spin border-t-gray-500"></div>
          </div>
        );
    }
  };

  return (
    <div className="flex flex-col items-center space-y-4 p-6">
      {getAnimation()}
      <div className="text-center">
        <p className="text-lg font-medium text-gray-800">{message}</p>
        <div className="flex justify-center mt-2">
          <div className="flex space-x-1">
            <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce"></div>
            <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
            <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoadingSpinner;