#!/bin/bash
# ============================================================================
# Cleanup Script - IMPORTANT: Run this when done to avoid AWS charges!
# ============================================================================

set -e

echo "=============================================="
echo "  Cleaning up AWS Resources"
echo "=============================================="
echo ""
echo "This will delete:"
echo "  - All Kubernetes resources (pods, services)"
echo "  - EKS Cluster"
echo "  - ECR Repositories (and images)"
echo "  - VPC and all networking"
echo ""
read -p "Are you sure? Press Enter to continue or Ctrl+C to cancel..."

# Delete Kubernetes resources first
echo ""
echo "Deleting Kubernetes resources..."
kubectl delete -f k8s/ --ignore-not-found=true || true

# Destroy Terraform infrastructure
echo ""
echo "Destroying Terraform infrastructure..."
cd terraform
terraform destroy -auto-approve

echo ""
echo "=============================================="
echo "  CLEANUP COMPLETE!"
echo "=============================================="
echo ""
echo "All AWS resources have been deleted."
echo "No more charges will be incurred."
