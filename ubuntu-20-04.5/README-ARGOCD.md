# Wedding App with Argo CD GitOps

This guide shows how to run the Java wedding application using Argo CD for GitOps deployment in a Kubernetes cluster running on Ubuntu 20.04 Vagrant VM.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   GitHub Repo   │    │   Argo CD       │    │   Kubernetes    │
│   (GitOps)      │◄──►│   (Controller)  │◄──►│   (k3s)         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                                                       ▼
                                              ┌─────────────────┐
                                              │   Wedding App   │
                                              │   (Java/Spring) │
                                              └─────────────────┘
```

## 🚀 Quick Start

### 1. Start the Vagrant VM

```bash
cd ubuntu-20-04.5
vagrant up
```

The VM will automatically install:
- Docker & Docker Compose
- Kubernetes (k3s)
- Argo CD
- Java 17 & Maven
- kubectl & Argo CD CLI

### 2. Connect to the VM

```bash
vagrant ssh
```

### 3. Run the Setup Script

```bash
cd /vagrant
./setup-argocd.sh
```

This script will:
- Build the Java application with Maven
- Create a Docker image
- Deploy all Kubernetes resources
- Set up Argo CD application for GitOps

## 📋 Access Information

After setup, you can access:

| Service | URL | Credentials |
|---------|-----|-------------|
| **Argo CD UI** | http://localhost:30007 | admin / (see password below) |
| **Wedding API** | http://localhost:30081 | N/A |
| **Kubernetes API** | https://localhost:6443 | N/A |

### Get Argo CD Password

```bash
cat /home/vagrant/argocd-password.txt
```

## 🔧 Manual Setup (Alternative)

If you prefer to run commands manually:

### 1. Build the Application

```bash
cd backend
mvn clean package -DskipTests
docker build -t wedding-api:latest .
cd ..
```

### 2. Load Image into k3s

```bash
docker save wedding-api:latest | sudo k3s ctr images import -
```

### 3. Deploy Kubernetes Resources

```bash
# Apply all manifests
kubectl apply -f k8s/
```

### 4. Set up Argo CD Application

```bash
kubectl apply -f argocd/wedding-app.yaml
```

## 🎯 GitOps Workflow

### How it Works

1. **Source of Truth**: Your GitHub repository contains all Kubernetes manifests
2. **Argo CD Controller**: Continuously monitors the repository for changes
3. **Automatic Sync**: When changes are detected, Argo CD automatically applies them
4. **Self-Healing**: If the cluster state differs from the desired state, Argo CD corrects it

### Making Changes

1. **Edit manifests** in the `k8s/` directory
2. **Commit and push** to GitHub
3. **Argo CD automatically syncs** the changes (if auto-sync is enabled)
4. **Or manually sync**: `argocd app sync wedding-app`

### Manual Sync

```bash
# List applications
argocd app list

# Sync specific application
argocd app sync wedding-app

# Check application status
argocd app get wedding-app
```

## 🔍 Monitoring & Debugging

### Check Application Status

```bash
# Kubernetes resources
kubectl get pods -n wedding-app
kubectl get svc -n wedding-app
kubectl get ingress -n wedding-app

# Argo CD applications
argocd app list
argocd app get wedding-app
```

### View Logs

```bash
# Application logs
kubectl logs -f deployment/wedding-api -n wedding-app

# Database logs
kubectl logs -f deployment/wedding-postgres -n wedding-app

# Redis logs
kubectl logs -f deployment/wedding-redis -n wedding-app
```

### Test the API

```bash
# Health check
curl http://localhost:30081/actuator/health

# Create an event
curl -X POST http://localhost:30081/api/events \
  -H "Content-Type: application/json" \
  -d '{"name":"Wedding Ceremony","date":"2024-06-15T14:00:00Z","location":"Beach Resort"}'
```

## 🛠️ Configuration

### Environment Variables

The application uses ConfigMaps and Secrets for configuration:

- **ConfigMap**: `wedding-app-config` - Non-sensitive configuration
- **Secret**: `wedding-app-secret` - Sensitive data (passwords, API keys)

### Update Configuration

1. Edit the appropriate manifest in `k8s/`
2. Commit and push to GitHub
3. Argo CD will automatically apply the changes

### Datadog Integration

The setup includes Datadog APM monitoring:

- Datadog agent runs as a DaemonSet
- Java application includes Datadog Java agent
- Metrics and traces are sent to Datadog

## 🔄 Scaling

### Scale Application

```bash
# Scale wedding API
kubectl scale deployment wedding-api --replicas=3 -n wedding-app

# Scale database (not recommended for production)
kubectl scale deployment wedding-postgres --replicas=2 -n wedding-app
```

### Resource Limits

Current resource allocation:
- **Wedding API**: 512Mi-1Gi RAM, 250m-500m CPU
- **PostgreSQL**: Default limits
- **Redis**: Default limits
- **Datadog Agent**: 256Mi-512Mi RAM, 100m-200m CPU

## 🧹 Cleanup

### Remove Application

```bash
# Delete Argo CD application
kubectl delete -f argocd/wedding-app.yaml

# Delete all resources
kubectl delete namespace wedding-app
```

### Destroy VM

```bash
vagrant destroy -f
```

## 🐛 Troubleshooting

### Common Issues

1. **Argo CD not accessible**
   ```bash
   kubectl get pods -n argocd
   kubectl logs deployment/argocd-server -n argocd
   ```

2. **Application not syncing**
   ```bash
   argocd app get wedding-app
   argocd app sync wedding-app
   ```

3. **Pods not starting**
   ```bash
   kubectl describe pod <pod-name> -n wedding-app
   kubectl logs <pod-name> -n wedding-app
   ```

4. **Image not found**
   ```bash
   # Rebuild and load image
   docker build -t wedding-api:latest backend/
   docker save wedding-api:latest | sudo k3s ctr images import -
   ```

### Reset Everything

```bash
# Delete all resources
kubectl delete namespace wedding-app
kubectl delete namespace argocd

# Reinstall Argo CD
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Re-run setup
./setup-argocd.sh
```

## 📚 Additional Resources

- [Argo CD Documentation](https://argo-cd.readthedocs.io/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [k3s Documentation](https://k3s.io/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test with Argo CD
5. Submit a pull request

The GitOps workflow ensures that all changes are automatically deployed and tested in the Kubernetes environment. 