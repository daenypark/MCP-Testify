#!/bin/bash

# Script to fix k3s kubeconfig issues
set -e

echo "🔧 Fixing k3s kubeconfig configuration..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Check if k3s is running
if ! systemctl is-active --quiet k3s; then
    print_error "k3s is not running. Starting k3s..."
    sudo systemctl start k3s
    sleep 10
fi

# Wait for k3s to be ready
print_status "Waiting for k3s to be ready..."
timeout=60
counter=0
while ! kubectl get nodes >/dev/null 2>&1 && [ $counter -lt $timeout ]; do
    print_status "Waiting for k3s API server... ($counter/$timeout)"
    sleep 2
    counter=$((counter + 2))
done

if [ $counter -eq $timeout ]; then
    print_error "k3s API server is not responding after $timeout seconds"
    print_status "Checking k3s status..."
    sudo systemctl status k3s
    exit 1
fi

print_success "k3s API server is ready!"

# Fix kubeconfig for vagrant user
print_status "Fixing kubeconfig for vagrant user..."

# Create .kube directory if it doesn't exist
mkdir -p /home/vagrant/.kube

# Copy k3s config
sudo cp /etc/rancher/k3s/k3s.yaml /home/vagrant/.kube/config

# Fix permissions
sudo chown -R vagrant:vagrant /home/vagrant/.kube

# Update server URL to use localhost instead of 127.0.0.1
print_status "Updating kubeconfig server URL..."
sed -i 's|server: https://127.0.0.1:6443|server: https://localhost:6443|g' /home/vagrant/.kube/config

# Test kubectl
print_status "Testing kubectl configuration..."
if kubectl get nodes >/dev/null 2>&1; then
    print_success "kubectl is working correctly!"
    kubectl get nodes
else
    print_error "kubectl is still not working"
    print_status "Current kubeconfig:"
    cat /home/vagrant/.kube/config
    exit 1
fi

# Set KUBECONFIG environment variable
echo 'export KUBECONFIG=/home/vagrant/.kube/config' >> /home/vagrant/.bashrc

print_success "k3s kubeconfig has been fixed!"
print_status "You can now use kubectl commands."
print_status "To test: kubectl get nodes" 