import React from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import Dashboard from './components/Dashboard';
import RepositoryDetailsPage from './components/RepositoryDetailsPage';
import RepositoryDetailsPageSimple from './components/RepositoryDetailsPageSimple';
import AnalysisResultsPage from './components/AnalysisResultsPage';
import RiskDashboard from './components/RiskDashboard';
import './styles/index.css';

// Debug component to log route changes
function RouteDebugger() {
  const location = useLocation();
  
  React.useEffect(() => {
    console.log('🛤️ Route changed to:', location.pathname);
    console.log('🔍 Full location:', location);
  }, [location]);
  
  return null;
}

function App() {
  console.log('🚀 App component rendered');
  
  return (
    <Router>
      <div className="App">
        <RouteDebugger />
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/risk-dashboard" element={<RiskDashboard />} />
          <Route path="/repository/:id" element={<RepositoryDetailsPage />} />
          <Route path="/analysis/:id" element={<AnalysisResultsPage />} />
          {/* Add more routes as needed */}
        </Routes>
      </div>
    </Router>
  );
}

export default App;