# Troubleshooting Guide

This guide helps resolve common issues when setting up the Ubuntu 20.04 Vagrant environment with k3s and Argo CD.

## 🔧 Common Issues and Solutions

### 1. k3s Connection Refused Error

**Error:**
```
The connection to the server localhost:8080 was refused - did you specify the right host or port?
```

**Solution:**
```bash
# SSH into the VM
vagrant ssh

# Run the fix script
cd /vagrant
chmod +x fix-k3s.sh
./fix-k3s.sh
```

**Manual Fix:**
```bash
# Check if k3s is running
sudo systemctl status k3s

# Start k3s if not running
sudo systemctl start k3s

# Wait for it to be ready
sleep 30

# Fix kubeconfig
sudo cp /etc/rancher/k3s/k3s.yaml /home/vagrant/.kube/config
sudo chown -R vagrant:vagrant /home/vagrant/.kube
sed -i 's|server: https://127.0.0.1:6443|server: https://localhost:6443|g' /home/vagrant/.kube/config

# Test
kubectl get nodes
```

### 2. Docker Permission Issues

**Error:**
```
Got permission denied while trying to connect to the Docker daemon socket
```

**Solution:**
```bash
# Add user to docker group
sudo usermod -aG docker vagrant

# Reload the VM to apply group changes
exit
vagrant reload
vagrant ssh
```

### 3. Argo CD Not Accessible

**Error:**
```
Connection refused on port 30007
```

**Solution:**
```bash
# Check if Argo CD is running
kubectl get pods -n argocd

# Check Argo CD service
kubectl get svc -n argocd

# Restart Argo CD if needed
kubectl delete pods -n argocd --all
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### 4. Memory/CPU Issues

**Error:**
```
Out of memory or CPU throttling
```

**Solution:**
- Increase VM resources in Vagrantfile:
  ```ruby
  v.vmx["memsize"] = "8192"  # 8GB RAM
  v.vmx["numvcpus"] = "4"    # 4 CPUs
  ```
- Rebuild the VM:
  ```bash
  vagrant destroy -f
  vagrant up
  ```

### 5. Port Forwarding Issues

**Error:**
```
Port already in use
```

**Solution:**
- Check what's using the port:
  ```bash
  lsof -i :30007  # For Argo CD
  lsof -i :6443   # For Kubernetes API
  ```
- Change ports in Vagrantfile if needed:
  ```ruby
  config.vm.network "forwarded_port", guest: 30007, host: 30008
  ```

### 6. Git Repository Issues

**Error:**
```
Repository not found or access denied
```

**Solution:**
- Update Argo CD application to use correct repository URL:
  ```yaml
  spec:
    source:
      repoURL: https://github.com/daenypark/MCP-Testify.git
      # or
      repoURL: https://gitlab.com/daenypark/MCP-Testify.git
  ```

## 🔍 Diagnostic Commands

### Check System Status
```bash
# VM status
vagrant status

# k3s status
sudo systemctl status k3s

# Docker status
sudo systemctl status docker

# Kubernetes nodes
kubectl get nodes

# All pods
kubectl get pods --all-namespaces
```

### Check Logs
```bash
# k3s logs
sudo journalctl -u k3s -f

# Docker logs
sudo journalctl -u docker -f

# Argo CD logs
kubectl logs -n argocd deployment/argocd-server

# Application logs
kubectl logs -n wedding-app deployment/wedding-api
```

### Check Network
```bash
# Port forwarding
netstat -tlnp | grep :30007
netstat -tlnp | grep :6443

# Connectivity
curl -k https://localhost:6443/healthz
curl http://localhost:30007
```

## 🚀 Quick Recovery Steps

### Complete Reset
```bash
# Destroy and recreate VM
vagrant destroy -f
vagrant up

# SSH and run setup
vagrant ssh
cd /vagrant
./setup-argocd.sh
```

### Partial Reset
```bash
# Reset just Kubernetes
vagrant ssh
sudo systemctl restart k3s
./fix-k3s.sh

# Reset just Argo CD
kubectl delete namespace argocd
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

## 📋 Health Check Commands

Run these commands to verify everything is working:

```bash
# 1. Check k3s
kubectl get nodes

# 2. Check Argo CD
kubectl get pods -n argocd

# 3. Check application
kubectl get pods -n wedding-app

# 4. Check services
kubectl get svc -n wedding-app

# 5. Test API
curl http://localhost:30081/actuator/health

# 6. Test Argo CD UI
curl http://localhost:30007
```

## 🆘 Getting Help

If you're still experiencing issues:

1. **Check the logs** using the diagnostic commands above
2. **Run the fix script**: `./fix-k3s.sh`
3. **Check this troubleshooting guide** for your specific error
4. **Reset the environment** using the quick recovery steps
5. **Check system resources** (memory, CPU, disk space)

## 📞 Support Information

- **k3s Documentation**: https://k3s.io/
- **Argo CD Documentation**: https://argo-cd.readthedocs.io/
- **Vagrant Documentation**: https://www.vagrantup.com/docs
- **Kubernetes Documentation**: https://kubernetes.io/docs/ 