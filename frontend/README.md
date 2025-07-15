# CodeGuardian Frontend

A modern React dashboard for the CodeGuardian security analysis platform.

## 🚀 Features

- **Real-time Dashboard** - Live updates on code analysis results
- **Repository Management** - View and manage Git repositories
- **Security Analysis** - Visual display of security findings
- **System Health** - Monitor backend services status
- **Responsive Design** - Works on desktop and mobile devices

## 🛠️ Technology Stack

- **React 18** with Hooks
- **Vite** for fast development and building
- **TailwindCSS** for styling
- **Lucide React** for icons
- **Axios** for API calls
- **Socket.io** for real-time updates

## 📦 Quick Start

### Development Mode
```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Access at http://localhost:3000
```

### Production Build
```bash
# Build for production
npm run build

# Preview production build
npm run preview
```

### Docker Deployment
```bash
# Build Docker image
docker build -t codeguardian-frontend .

# Run container
docker run -p 3000:80 codeguardian-frontend
```

## 🔧 Configuration

The frontend connects to backend services via API proxying:

- **API Gateway**: `http://localhost:8080/api/*`
- **Git Processor**: `http://localhost:8081/webhooks/*`
- **Code Analyzer**: `http://localhost:8082/api/*`

## 📊 Dashboard Components

### Main Dashboard
- **Summary Cards** - Key metrics overview
- **Recent Analysis** - Latest security scan results
- **Repository List** - All connected repositories
- **System Health** - Backend services status
- **Real-time Updates** - Live activity feed

### Analysis Results
- **Security Findings** - Vulnerability details
- **Severity Levels** - Critical, High, Medium, Low
- **Code References** - File and line information
- **Status Tracking** - Analysis completion status

### Repository Management
- **Platform Support** - GitHub, GitLab, Bitbucket
- **Health Monitoring** - Repository activity status
- **Quick Actions** - Trigger analysis, view history

## 🔗 API Integration

The frontend communicates with backend services through:

```javascript
// Repository API
repositoryAPI.getAll()           // Get all repositories
repositoryAPI.getById(id)        // Get specific repository
repositoryAPI.getByPlatform()    // Filter by platform

// Analysis API  
analysisAPI.getByCommit(id)      // Get commit analysis
analysisAPI.getLatest()          // Recent results
analysisAPI.getSummary()         // Analysis statistics

// Webhook API
webhookAPI.getHealth()           // System health
webhookAPI.getConfig()           // Webhook configuration
```

## 🎨 Styling

The application uses TailwindCSS with custom design tokens:

```css
/* Color Palette */
--primary: #3b82f6    /* Blue */
--success: #22c55e    /* Green */  
--warning: #f59e0b    /* Amber */
--danger: #ef4444     /* Red */

/* Components */
.card                 /* Standard card layout */
.btn-primary         /* Primary button style */
.badge-*             /* Status badges */
.status-*            /* Health indicators */
```

## 🔄 Real-time Updates

WebSocket integration provides live updates:

```javascript
// Connection status
useWebSocket('connection', (status) => {
  console.log('WebSocket:', status);
});

// Analysis updates
useWebSocket('analysisUpdate', (data) => {
  updateDashboard(data);
});

// Repository updates
useWebSocket('repositoryUpdate', (data) => {
  refreshRepositories(data);
});
```

## 📱 Responsive Design

The dashboard adapts to different screen sizes:

- **Desktop** - Full dashboard with all components
- **Tablet** - Responsive grid layout
- **Mobile** - Stacked components, collapsible sidebar

## 🚀 Deployment

### With Docker Compose
```bash
# Start all services including frontend
docker-compose up -d

# Access dashboard at http://localhost:3000
```

### Production Deployment
```bash
# Build optimized production bundle
npm run build

# Deploy dist/ folder to web server
# or use the provided Dockerfile
```

## 🔧 Environment Variables

```bash
# API endpoints (configured in vite.config.js)
VITE_API_GATEWAY_URL=http://localhost:8080
VITE_GIT_PROCESSOR_URL=http://localhost:8081
VITE_CODE_ANALYZER_URL=http://localhost:8082

# WebSocket URL
VITE_WEBSOCKET_URL=ws://localhost:8080
```

## 🎯 Demo Mode

When backend services are unavailable, the frontend shows mock data:

- **Sample repositories** with realistic data
- **Mock analysis results** demonstrating features  
- **Simulated real-time updates** for demonstration
- **System health indicators** showing service status

This allows the frontend to be demonstrated standalone without requiring full backend deployment.

## 🔒 Security Features

- **XSS Protection** via React's built-in sanitization
- **CSRF Protection** through API token validation
- **Secure Headers** in nginx configuration
- **Input Validation** on all user inputs
- **Authentication Ready** for future JWT integration

## 📈 Performance

- **Code Splitting** for faster initial loads
- **Tree Shaking** to minimize bundle size
- **Gzip Compression** via nginx
- **Asset Caching** for static resources
- **Lazy Loading** for non-critical components

## 🐛 Troubleshooting

### Common Issues

**1. API Connection Errors**
```bash
# Check backend services are running
docker-compose ps

# Verify API endpoints in browser
curl http://localhost:8081/webhooks/health
```

**2. Build Failures**
```bash
# Clear npm cache
npm cache clean --force

# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

**3. Styling Issues**
```bash
# Rebuild Tailwind styles
npm run build

# Check for CSS conflicts in browser dev tools
```

---

## 🤝 Contributing

1. Follow React best practices
2. Use TypeScript for new components
3. Maintain responsive design principles
4. Add tests for critical functionality
5. Update documentation for new features

For more information, see the main project README.