#!/bin/bash

echo "🎉 Setting up Wedding Web Card Application with Datadog APM 🎉"
echo "================================================================"

# Check if running in Vagrant
if [ "$USER" != "vagrant" ]; then
    echo "⚠️  This script is designed to run inside the Vagrant VM"
    echo "Please run: vagrant ssh"
    exit 1
fi

# Check for Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please provision the Vagrant VM first."
    exit 1
fi

echo "📋 Step 1: Setting up environment..."

# Copy environment file if it doesn't exist
if [ ! -f "../.env" ]; then
    if [ -f "env.example" ]; then
        cp env.example ../.env
        echo "📝 Created .env file from template in root directory"
        echo "⚠️  Please edit ../.env file and add your Datadog API key!"
        echo "   You can get your API key from: https://app.datadoghq.com/organization-settings/api-keys"
    else
        echo "❌ env.example file not found"
        exit 1
    fi
fi

# Create necessary directories
echo "📁 Step 2: Creating directories..."
mkdir -p database
mkdir -p nginx/logs
mkdir -p backend/logs
mkdir -p frontend

echo "📊 Step 3: Creating database initialization..."
cat > database/init.sql << 'EOF'
-- Wedding Database Schema
CREATE TABLE IF NOT EXISTS guests (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    plus_one BOOLEAN DEFAULT FALSE,
    dietary_restrictions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rsvps (
    id SERIAL PRIMARY KEY,
    guest_id INTEGER REFERENCES guests(id) ON DELETE CASCADE,
    status VARCHAR(20) CHECK (status IN ('attending', 'not_attending', 'maybe')) NOT NULL,
    plus_one_attending BOOLEAN DEFAULT FALSE,
    message TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS wedding_events (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    event_date TIMESTAMP NOT NULL,
    venue_name VARCHAR(200),
    venue_address TEXT,
    dress_code VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data for testing
INSERT INTO guests (first_name, last_name, email, phone, plus_one) VALUES
('John', 'Smith', 'john.smith@email.com', '+1-555-0101', true),
('Jane', 'Doe', 'jane.doe@email.com', '+1-555-0102', false),
('Bob', 'Johnson', 'bob.johnson@email.com', '+1-555-0103', true),
('Alice', 'Brown', 'alice.brown@email.com', '+1-555-0104', false),
('Charlie', 'Wilson', 'charlie.wilson@email.com', '+1-555-0105', true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO wedding_events (name, description, event_date, venue_name, venue_address, dress_code) VALUES
('Wedding Ceremony', 'The main wedding ceremony', '2024-06-15 16:00:00', 'Beautiful Gardens', '123 Garden Lane, City, State', 'Formal'),
('Reception', 'Wedding reception and dinner', '2024-06-15 18:00:00', 'Grand Ballroom', '456 Celebration Ave, City, State', 'Cocktail')
ON CONFLICT DO NOTHING;
EOF

echo "🌐 Step 4: Creating Nginx configuration..."
mkdir -p nginx
cat > nginx/nginx.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    upstream wedding_api {
        server wedding-api:8080;
    }

    upstream wedding_frontend {
        server wedding-frontend:3000;
    }

    server {
        listen 80;
        server_name localhost;

        # Enable nginx status for monitoring
        location /nginx_status {
            stub_status on;
            access_log off;
            allow 172.0.0.0/8;
            allow 127.0.0.1;
            deny all;
        }

        # API routes
        location /api/ {
            proxy_pass http://wedding_api;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Health check
        location /actuator/ {
            proxy_pass http://wedding_api;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }

        # Frontend
        location / {
            proxy_pass http://wedding_frontend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
EOF

echo "🎨 Step 5: Creating simple frontend..."
mkdir -p frontend
cat > frontend/Dockerfile << 'EOF'
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm", "start"]
EOF

cat > frontend/package.json << 'EOF'
{
  "name": "wedding-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "start": "node server.js"
  },
  "dependencies": {
    "express": "^4.18.2"
  }
}
EOF

cat > frontend/server.js << 'EOF'
const express = require('express');
const path = require('path');
const app = express();
const port = 3000;

app.use(express.static('public'));

app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(port, '0.0.0.0', () => {
  console.log(`Wedding frontend running on port ${port}`);
});
EOF

mkdir -p frontend/public
cat > frontend/public/index.html << 'EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>💕 Emma & James Wedding 💕</title>
    <style>
        body { font-family: 'Georgia', serif; background: linear-gradient(135deg, #ffeef8, #f0f8ff); margin: 0; padding: 20px; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 40px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
        h1 { text-align: center; color: #8b4b7a; font-size: 2.5em; margin-bottom: 10px; }
        .date { text-align: center; font-size: 1.2em; color: #666; margin-bottom: 30px; }
        .section { margin: 30px 0; }
        .api-test { background: #f8f9fa; padding: 20px; border-radius: 10px; margin: 20px 0; }
        button { background: #8b4b7a; color: white; border: none; padding: 12px 24px; border-radius: 25px; cursor: pointer; margin: 5px; font-size: 16px; }
        button:hover { background: #6b3b5a; }
        .response { background: #e8f5e8; border: 1px solid #4caf50; padding: 15px; border-radius: 5px; margin: 10px 0; max-height: 200px; overflow-y: auto; }
        .error { background: #ffe8e8; border: 1px solid #f44336; }
        .loading { color: #666; font-style: italic; }
        .metrics { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px; }
        .metric-card { background: #f8f9fa; padding: 15px; border-radius: 8px; text-align: center; }
    </style>
</head>
<body>
    <div class="container">
        <h1>💕 Emma & James 💕</h1>
        <p class="date">June 15th, 2024 • Beautiful Gardens</p>
        
        <div class="section">
            <h2>🎉 You're Invited to Our Wedding!</h2>
            <p>We're excited to celebrate our special day with you. This application demonstrates a complete wedding website with Java backend API and Datadog APM monitoring.</p>
        </div>

        <div class="section">
            <h2>📊 API Testing Dashboard</h2>
            <p>Test the various API endpoints to generate APM traces and metrics:</p>
            
            <div class="api-test">
                <h3>Guest Management</h3>
                <button onclick="testAPI('/api/guests', 'GET')">📋 List Guests</button>
                <button onclick="testAPI('/api/guests/1', 'GET')">👤 Get Guest #1</button>
                <button onclick="createGuest()">➕ Create Guest</button>
            </div>

            <div class="api-test">
                <h3>RSVP System</h3>
                <button onclick="submitRSVP()">💌 Submit RSVP</button>
                <button onclick="testAPI('/api/rsvp/stats', 'GET')">📈 RSVP Stats</button>
                <button onclick="testAPI('/api/rsvp/1', 'GET')">🎯 Get RSVP #1</button>
            </div>

            <div class="api-test">
                <h3>Wedding Events</h3>
                <button onclick="testAPI('/api/events/details', 'GET')">🎪 Event Details (Cached)</button>
                <button onclick="testAPI('/api/weather/2024-06-15', 'GET')">🌤️ Weather API</button>
                <button onclick="testAPI('/api/venue/directions?from=Downtown', 'GET')">🗺️ Directions</button>
            </div>

            <div class="api-test">
                <h3>Performance Testing</h3>
                <button onclick="testAPI('/api/performance/slow-query', 'GET')">🐌 Slow Database Query</button>
                <button onclick="testAPI('/api/performance/cpu-intensive', 'GET')">💻 CPU Intensive Task</button>
                <button onclick="testAPI('/api/performance/memory-test?sizeMB=50', 'GET')">🧠 Memory Test</button>
            </div>

            <div class="api-test">
                <h3>Error Simulation</h3>
                <button onclick="testAPI('/api/errors/database-error', 'GET')">💥 Database Error</button>
                <button onclick="testAPI('/api/errors/external-api-timeout', 'GET')">⏰ API Timeout</button>
                <button onclick="testAPI('/api/errors/validation-error', 'GET')">❌ Validation Error</button>
            </div>

            <div class="api-test">
                <h3>Analytics</h3>
                <button onclick="recordPageView()">👁️ Record Page View</button>
                <button onclick="recordFunnelStep()">📊 Record Funnel Step</button>
                <button onclick="testAPI('/api/dashboard/stats', 'GET')">📈 Dashboard Stats</button>
            </div>

            <div id="response-container" style="display: none;">
                <h3>API Response:</h3>
                <div id="api-response" class="response"></div>
            </div>
        </div>

        <div class="section">
            <h2>🔍 Monitoring Instructions</h2>
            <ol>
                <li>Set up your Datadog API key in the <code>.env</code> file (located in root directory)</li>
                <li>Access your Datadog dashboard to see APM traces</li>
                <li>Click the API test buttons above to generate traces</li>
                <li>Monitor custom metrics and performance data</li>
                <li>Check service maps and dependency graphs</li>
            </ol>
        </div>
    </div>

    <script>
        let requestCount = 0;

        async function testAPI(endpoint, method = 'GET', body = null) {
            const responseDiv = document.getElementById('api-response');
            const container = document.getElementById('response-container');
            
            container.style.display = 'block';
            responseDiv.className = 'response loading';
            responseDiv.textContent = 'Loading...';
            
            requestCount++;
            
            try {
                const options = {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json',
                    }
                };
                
                if (body) {
                    options.body = JSON.stringify(body);
                }
                
                const response = await fetch(endpoint, options);
                const data = await response.json();
                
                responseDiv.className = response.ok ? 'response' : 'response error';
                responseDiv.innerHTML = `
                    <strong>Status:</strong> ${response.status} ${response.statusText}<br>
                    <strong>Endpoint:</strong> ${method} ${endpoint}<br>
                    <strong>Response:</strong><br>
                    <pre>${JSON.stringify(data, null, 2)}</pre>
                `;
                
                // Record analytics
                await recordPageView();
                
            } catch (error) {
                responseDiv.className = 'response error';
                responseDiv.innerHTML = `
                    <strong>Error:</strong> ${error.message}<br>
                    <strong>Endpoint:</strong> ${method} ${endpoint}
                `;
            }
        }

        async function createGuest() {
            const guestData = {
                firstName: 'Test',
                lastName: 'Guest',
                email: `test.guest.${Date.now()}@example.com`,
                phone: '+1-555-0199',
                plusOne: true,
                dietaryRestrictions: 'Vegetarian'
            };
            
            await testAPI('/api/guests', 'POST', guestData);
        }

        async function submitRSVP() {
            const rsvpData = {
                guestId: 1,
                status: 'attending',
                plusOneAttending: true,
                message: 'Looking forward to celebrating with you!'
            };
            
            await testAPI('/api/rsvp/submit', 'POST', rsvpData);
        }

        async function recordPageView() {
            const pageData = {
                page: window.location.pathname,
                userAgent: navigator.userAgent,
                timestamp: new Date().toISOString()
            };
            
            try {
                await fetch('/api/analytics/page-view', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(pageData)
                });
            } catch (error) {
                console.log('Analytics recording failed:', error.message);
            }
        }

        async function recordFunnelStep() {
            const funnelData = {
                step: 'api_testing',
                guestId: '1',
                timestamp: new Date().toISOString()
            };
            
            await testAPI('/api/analytics/rsvp-funnel', 'POST', funnelData);
        }

        // Auto-record page view on load
        window.addEventListener('load', recordPageView);
    </script>
</body>
</html>
EOF

echo "🚀 Step 6: Building and starting the application..."

# Check if .env file has been configured
if grep -q "your_datadog_api_key_here" ../.env 2>/dev/null; then
    echo "⚠️  WARNING: Please configure your Datadog API key in ../.env file"
    echo "   Edit ../.env and replace 'your_datadog_api_key_here' with your actual API key"
    echo "   You can continue without it, but APM data won't be sent to Datadog"
    echo ""
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Exiting... Please configure ../.env first"
        exit 1
    fi
fi

# Start the application
echo "🐳 Starting Docker containers..."
docker-compose up -d

echo "⏳ Waiting for services to start..."
sleep 15

echo "🏥 Checking service health..."
docker-compose ps

echo "✅ Wedding Web Application Setup Complete!"
echo "================================================================"
echo "🌐 Application URL: http://localhost:8080"
echo "🔗 API Health Check: http://localhost:8081/actuator/health"
echo "📊 API Documentation: Use the web interface to test APIs"
echo ""
echo "📈 Datadog Monitoring:"
echo "   - APM traces will appear in your Datadog dashboard"
echo "   - Custom metrics are being collected"
echo "   - Host metrics are being monitored"
echo ""
echo "🧪 To generate test data:"
echo "   1. Open http://localhost:8080 in your browser"
echo "   2. Click the API test buttons to generate traces"
echo "   3. Check your Datadog dashboard for APM data"
echo ""
echo "🛠️  Useful commands:"
echo "   docker-compose logs -f wedding-api    # View API logs"
echo "   docker-compose logs -f datadog-agent  # View Datadog logs"
echo "   docker-compose down                   # Stop all services"
echo "   docker-compose up -d                  # Start all services"
echo ""
echo "Happy monitoring! 🎉" 