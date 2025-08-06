#!/bin/bash

# Manual provisioning script for Ubuntu 20.04 with k3s and Argo CD
# Run this script if Vagrant provision fails

set -e

echo "🚀 Starting manual provisioning..."

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

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    print_error "Please run this script as vagrant user, not root"
    exit 1
fi

print_status "Step 1: Updating system packages..."
sudo apt-get update

print_status "Step 2: Installing Docker..."
sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=arm64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io

print_status "Step 3: Installing Docker Compose..."
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-linux-aarch64" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

print_status "Step 4: Adding vagrant user to docker group..."
sudo usermod -aG docker vagrant

print_status "Step 5: Installing Java 17 and Maven..."
sudo apt-get install -y openjdk-17-jdk maven

print_status "Step 6: Installing utilities..."
sudo apt-get install -y curl wget git vim htop

print_status "Step 7: Installing kubectl..."
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/arm64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

print_status "Step 8: Installing k3s..."
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--docker" sh -

print_status "Step 9: Waiting for k3s to start..."
sleep 30

print_status "Step 10: Setting up kubeconfig..."
mkdir -p /home/vagrant/.kube
sudo cp /etc/rancher/k3s/k3s.yaml /home/vagrant/.kube/config
sudo chown -R vagrant:vagrant /home/vagrant/.kube

# Fix kubeconfig server URL
sed -i 's|server: https://127.0.0.1:6443|server: https://localhost:6443|g' /home/vagrant/.kube/config

# Set KUBECONFIG environment variable
echo 'export KUBECONFIG=/home/vagrant/.kube/config' >> /home/vagrant/.bashrc

print_status "Step 11: Waiting for k3s API server..."
timeout=60
counter=0
while ! KUBECONFIG=/home/vagrant/.kube/config kubectl get nodes >/dev/null 2>&1 && [ $counter -lt $timeout ]; do
    print_status "Waiting for k3s API server... ($counter/$timeout)"
    sleep 2
    counter=$((counter + 2))
done

if [ $counter -eq $timeout ]; then
    print_error "k3s API server is not responding after $timeout seconds"
    sudo systemctl status k3s
    exit 1
fi

print_success "k3s is ready!"

print_status "Step 12: Installing Argo CD..."
KUBECONFIG=/home/vagrant/.kube/config kubectl create namespace argocd
KUBECONFIG=/home/vagrant/.kube/config kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

print_status "Step 13: Waiting for Argo CD..."
sleep 60

print_status "Step 14: Installing Argo CD CLI..."
sudo curl -sSL -o /usr/local/bin/argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-arm64
sudo chmod +x /usr/local/bin/argocd

print_status "Step 15: Creating wedding-app namespace..."
KUBECONFIG=/home/vagrant/.kube/config kubectl create namespace wedding-app

print_status "Step 16: Getting Argo CD password..."
KUBECONFIG=/home/vagrant/.kube/config kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d > /home/vagrant/argocd-password.txt

print_success "🎉 Provisioning completed successfully!"

echo ""
echo "📋 Access Information:"
echo "======================"
echo "Argo CD UI: http://localhost:30007"
echo "Username: admin"
echo "Password: $(cat /home/vagrant/argocd-password.txt)"
echo ""
echo "🔍 Test Commands:"
echo "================"
echo "kubectl get nodes"
echo "kubectl get pods -n argocd"
echo "kubectl get pods -n wedding-app"
echo ""
print_success "Setup complete! You can now run ./setup-argocd.sh to deploy the wedding app." 