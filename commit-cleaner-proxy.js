const http = require('http');
const httpProxy = require('http-proxy-middleware');
const express = require('express');

const app = express();

// Create proxy middleware for the API Gateway
const apiProxy = httpProxy.createProxyMiddleware({
  target: 'http://localhost:8080',
  changeOrigin: true,
  onProxyRes: function (proxyRes, req, res) {
    // Intercept commits endpoints and clean the response
    if (req.url.includes('/commits')) {
      console.log(`🧹 Intercepting commits request: ${req.url}`);
      
      let body = '';
      proxyRes.on('data', function (chunk) {
        body += chunk;
      });
      
      proxyRes.on('end', function () {
        try {
          // Parse the response
          const data = JSON.parse(body);
          
          // If it's commits data, replace with empty array
          if (Array.isArray(data)) {
            console.log(`🚮 Cleaning ${data.length} mock commits, returning empty array`);
            res.setHeader('Content-Type', 'application/json');
            res.end('[]');
            return;
          }
        } catch (e) {
          console.log('⚠️ Error parsing commits response, passing through');
        }
        
        // Pass through other responses unchanged
        res.end(body);
      });
    }
  }
});

// Apply proxy to all requests
app.use('/', apiProxy);

// Start the proxy server on port 8081 (API Gateway proxy)
const PORT = 8081;
app.listen(PORT, () => {
  console.log(`🔄 Commit Cleaner Proxy running on port ${PORT}`);
  console.log(`📍 Proxying to API Gateway: http://localhost:8080`);
  console.log(`🧹 Cleaning commits endpoints automatically`);
  console.log(`\n💡 Update frontend to use: http://localhost:${PORT} instead of http://localhost:8080`);
});

// Handle graceful shutdown
process.on('SIGINT', () => {
  console.log('\n🛑 Shutting down Commit Cleaner Proxy...');
  process.exit(0);
});