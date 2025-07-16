import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Dashboard from './components/Dashboard';
import RepositoryDetailsPage from './components/RepositoryDetailsPage';
import AnalysisResultsPage from './components/AnalysisResultsPage';
import './styles/index.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/repository/:id" element={<RepositoryDetailsPage />} />
          <Route path="/analysis/:id" element={<AnalysisResultsPage />} />
          {/* Add more routes as needed */}
        </Routes>
      </div>
    </Router>
  );
}

export default App;