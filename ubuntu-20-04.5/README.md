# 💕 Wedding Web Card with Datadog APM

A complete wedding web application built with **Java Spring Boot**, **PostgreSQL**, **Redis**, and **Docker** - designed specifically for **Datadog APM monitoring and host metrics collection**.

## 🏗️ Architecture Overview

```
Host (macOS) → Vagrant VM (Ubuntu 20.04) → Docker Containers
                                        ├── Datadog Agent (APM + Host Metrics)
                                        ├── Java Spring Boot API (Instrumented)
                                        ├── PostgreSQL Database
                                        ├── Redis Cache
                                        ├── Nginx Reverse Proxy
                                        └── Frontend Web App
```

<img width="2342" height="1945" alt="image" src="https://github.com/user-attachments/assets/f202d1bb-bd83-4fc6-9a6c-b3f24c3df124" />


## 🚀 Quick Start

### 1. Start Vagrant VM
```bash
vagrant up
vagrant ssh
```

### 2. Configure Datadog
```bash
cd /vagrant
cp env.example ../.env
# Edit ../.env and add your Datadog API key
nano ../.env
```

### 3. Run Setup Script
```bash
./setup.sh
```

### 4. Access Application
- **Web Interface**: http://localhost:8080
- **API Health**: http://localhost:8081/actuator/health
- **Datadog Dashboard**: https://app.datadoghq.com

## 📊 APM-Focused API Endpoints

The application includes comprehensive API endpoints designed to generate rich APM traces:

### **Guest Management APIs** (Database Heavy)
- `GET /api/guests` - Paginated guest listing with search
- `POST /api/guests` - Create new guest (validation traces)
- `GET /api/guests/{id}` - Single guest lookup
- `PUT /api/guests/{id}` - Update guest information  
- `DELETE /api/guests/{id}` - Remove guest

### **RSVP APIs** (Business Logic + Async)
- `POST /api/rsvp/submit` - Submit RSVP (complex business flow)
- `GET /api/rsvp/{guestId}` - Get RSVP status
- `GET /api/rsvp/stats` - Calculate RSVP statistics

### **Event APIs** (Caching + External Services)
- `GET /api/events/details` - Event info (Redis cached)
- `GET /api/weather/{date}` - Weather API integration
- `GET /api/venue/directions` - Maps API integration

### **Performance Testing APIs**
- `GET /api/performance/slow-query` - Intentionally slow DB query
- `GET /api/performance/cpu-intensive` - CPU intensive operation
- `GET /api/performance/memory-test` - Memory allocation test

### **Error Simulation APIs**
- `GET /api/errors/database-error` - Database connection failure
- `GET /api/errors/external-api-timeout` - External API timeout
- `GET /api/errors/validation-error` - Validation error scenarios

### **Analytics APIs** (Custom Metrics)
- `POST /api/analytics/page-view` - Record page views
- `POST /api/analytics/rsvp-funnel` - Track funnel steps
- `GET /api/dashboard/stats` - Dashboard metrics

## 🔍 Datadog Monitoring Features

### **APM Traces**
- **Service Maps**: Visualize service dependencies
- **Trace Analysis**: Deep dive into request flows
- **Performance Monitoring**: Identify bottlenecks
- **Error Tracking**: Monitor error rates and types

### **Custom Metrics**
```java
@Timed(value = "wedding.guests.list.time")
@Counted(value = "wedding.guests.list.count")
```
- Request duration timers
- Operation counters
- Business metrics (RSVP conversion rates)
- Performance metrics (CPU, memory usage)

### **Host Metrics**
- CPU usage and load average
- Memory utilization
- Disk I/O and space usage
- Network traffic
- Docker container metrics

## 🧪 Testing APM & Metrics

### **Generate Trace Data**
1. Open http://localhost:8080
2. Use the API testing dashboard
3. Click various API test buttons
4. Monitor traces in Datadog APM

### **Performance Testing**
```bash
# Generate load for APM analysis
curl -X GET "http://localhost:8080/api/performance/slow-query"
curl -X GET "http://localhost:8080/api/performance/cpu-intensive"
curl -X GET "http://localhost:8080/api/performance/memory-test?sizeMB=100"
```

### **Error Generation**
```bash
# Test error tracking
curl -X GET "http://localhost:8080/api/errors/database-error"
curl -X GET "http://localhost:8080/api/errors/external-api-timeout"
```

## 🐳 Docker Services

| Service | Container Name | Port | Purpose |
|---------|---------------|------|---------|
| Datadog Agent | `datadog-agent` | 8125, 8126 | APM & Host Metrics |
| Java API | `wedding-api` | 8081 | Spring Boot Application |
| PostgreSQL | `wedding-db` | 5432 | Primary Database |
| Redis | `wedding-cache` | 6379 | Caching Layer |
| Frontend | `wedding-frontend` | 3000 | Web Interface |
| Nginx | `wedding-nginx` | 80 | Reverse Proxy |

## 🛠️ Management Commands

```bash
# View logs
docker-compose logs -f wedding-api
docker-compose logs -f datadog-agent

# Restart services
docker-compose restart wedding-api
docker-compose restart datadog-agent

# Scale services
docker-compose up -d --scale wedding-api=2

# Stop/Start all services
docker-compose down
docker-compose up -d

# Check service health
docker-compose ps
curl http://localhost:8081/actuator/health
```

## 📈 Key Metrics to Monitor

### **Application Metrics**
- `wedding.guests.list.time` - Guest listing duration
- `wedding.rsvp.submit.count` - RSVP submission rate
- `wedding.performance.slow.time` - Slow query duration
- `wedding.analytics.pageview.count` - Page view tracking

### **Infrastructure Metrics**
- Container CPU/Memory usage
- Database connection pool utilization
- Redis cache hit/miss ratios
- HTTP response times and status codes

### **Business Metrics**
- RSVP conversion funnel
- Guest engagement rates
- API endpoint popularity
- Error rates by service

## 🔧 Troubleshooting

### **Common Issues**

**Datadog Agent Not Sending Data**
```bash
# Check agent logs
docker-compose logs datadog-agent

# Verify API key
grep DD_API_KEY ../.env

# Test agent connectivity
docker exec -it datadog-agent agent status
```

**Java Application Won't Start**
```bash
# Check Java application logs
docker-compose logs wedding-api

# Verify database connectivity
docker-compose logs wedding-db

# Check memory allocation
docker stats wedding-api
```

**Missing APM Traces**
- Verify Datadog Java agent is loaded
- Check `DD_AGENT_HOST` environment variable
- Ensure port 8126 is accessible
- Confirm service name configuration

## 📚 APM Best Practices

### **Trace Optimization**
- Use meaningful operation names
- Add custom tags for filtering
- Monitor trace sampling rates
- Set up service dependencies

### **Metric Collection**
- Create business-relevant metrics
- Use appropriate metric types (counters, gauges, histograms)
- Tag metrics with relevant dimensions
- Monitor metric cardinality

### **Performance Monitoring**
- Set up SLIs/SLOs for key endpoints
- Monitor P95/P99 latencies
- Track error rates and types
- Monitor resource utilization

## 🎯 Next Steps

1. **Customize Metrics**: Add business-specific metrics
2. **Create Dashboards**: Build custom Datadog dashboards
3. **Set Up Alerts**: Configure alerting rules
4. **Load Testing**: Use tools like Apache Bench or K6
5. **Service Maps**: Analyze service dependencies
6. **Distributed Tracing**: Track cross-service requests

---

**Happy Monitoring!** 🎉📊

This setup provides comprehensive APM data and host metrics perfect for learning Datadog's monitoring capabilities. 
