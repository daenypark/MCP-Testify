#!/bin/bash

# Script to sync changes between GitHub and GitLab repositories
# Usage: ./sync-repos.sh [github|gitlab|both]

set -e

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

# Check if we're in a git repository
if [ ! -d ".git" ]; then
    print_error "Not in a git repository. Please run this script from the project root."
    exit 1
fi

# Check if remotes are configured
if ! git remote get-url origin >/dev/null 2>&1; then
    print_error "GitHub remote (origin) not configured"
    exit 1
fi

if ! git remote get-url gitlab >/dev/null 2>&1; then
    print_error "GitLab remote not configured. Run: git remote add gitlab https://gitlab.com/daenypark/MCP-Testify.git"
    exit 1
fi

# Function to sync to GitHub
sync_github() {
    print_status "Syncing to GitHub..."
    git push origin main
    print_success "Successfully pushed to GitHub"
}

# Function to sync to GitLab
sync_gitlab() {
    print_status "Syncing to GitLab..."
    git push gitlab main
    print_success "Successfully pushed to GitLab"
}

# Function to pull from both remotes
pull_from_both() {
    print_status "Pulling latest changes from both remotes..."
    
    # Fetch from both remotes
    git fetch origin
    git fetch gitlab
    
    # Check if we need to merge
    LOCAL=$(git rev-parse @)
    GITHUB=$(git rev-parse origin/main)
    GITLAB=$(git rev-parse gitlab/main)
    
    if [ "$LOCAL" != "$GITHUB" ] || [ "$LOCAL" != "$GITLAB" ]; then
        print_warning "Local branch is behind remote. Consider pulling first."
        echo "Local: $LOCAL"
        echo "GitHub: $GITHUB"
        echo "GitLab: $GITLAB"
    else
        print_success "Local branch is up to date with both remotes"
    fi
}

# Main logic
case "${1:-both}" in
    "github")
        sync_github
        ;;
    "gitlab")
        sync_gitlab
        ;;
    "both")
        pull_from_both
        sync_github
        sync_gitlab
        ;;
    "pull")
        pull_from_both
        ;;
    *)
        print_error "Usage: $0 [github|gitlab|both|pull]"
        echo ""
        echo "Options:"
        echo "  github  - Push to GitHub only"
        echo "  gitlab  - Push to GitLab only"
        echo "  both    - Push to both repositories (default)"
        echo "  pull    - Pull latest changes from both remotes"
        exit 1
        ;;
esac

print_success "Repository sync completed!" 