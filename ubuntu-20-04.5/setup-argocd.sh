#!/bin/bash

set -e

echo "🚀 Setting up Wedding App with Argo CD in Kubernetes..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "backend/pom.xml" ]; then
    print_error "Please run this script from the ubuntu-20-04.5 directory"
    exit 1
fi

print_status "Step 1: Building the Java application..."

# Build the Java application
cd backend
print_status "Building with Maven..."
mvn clean package -DskipTests

# Build Docker image
print_status "Building Docker image..."
docker build -t wedding-api:latest .

cd ..

print_status "Step 2: Loading image into k3s..."

# Load the Docker image into k3s
docker save wedding-api:latest | sudo k3s ctr images import -

print_status "Step 3: Applying Kubernetes manifests..."

# Apply all Kubernetes manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/datadog-agent.yaml
kubectl apply -f k8s/wedding-api.yaml
kubectl apply -f k8s/ingress.yaml

print_status "Step 4: Setting up Argo CD application..."

# Wait for Argo CD to be ready
print_status "Waiting for Argo CD to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/argocd-server -n argocd

# Apply Argo CD application
kubectl apply -f argocd/wedding-app.yaml

print_status "Step 5: Waiting for all pods to be ready..."

# Wait for all pods to be ready
kubectl wait --for=condition=ready --timeout=300s pod -l app=wedding-postgres -n wedding-app
kubectl wait --for=condition=ready --timeout=300s pod -l app=wedding-redis -n wedding-app
kubectl wait --for=condition=ready --timeout=300s pod -l app=wedding-api -n wedding-app

print_success "🎉 Wedding App deployed successfully!"

echo ""
echo "📋 Access Information:"
echo "======================"
echo "Argo CD UI: http://localhost:30007"
echo "Username: admin"
echo "Password: $(cat /home/vagrant/argocd-password.txt)"
echo ""
echo "Wedding API: http://localhost:30081"
echo "Kubernetes API: https://localhost:6443"
echo ""
echo "🔍 Useful Commands:"
echo "=================="
echo "kubectl get pods -n wedding-app"
echo "kubectl logs -f deployment/wedding-api -n wedding-app"
echo "kubectl get svc -n wedding-app"
echo "argocd app list"
echo "argocd app sync wedding-app"
echo ""
print_success "Setup complete! Your Java app is now running with Argo CD GitOps!" 