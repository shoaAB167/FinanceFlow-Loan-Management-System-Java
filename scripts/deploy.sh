#!/bin/bash
# ============================================================================
# AWS EKS Deployment Script for Loan Application
# Run this script step-by-step (not all at once) to learn!
# ============================================================================

set -e  # Exit on any error

echo "=============================================="
echo "  AWS EKS Deployment - Loan Application"
echo "=============================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ============================================================================
# STEP 1: Initialize and Apply Terraform
# ============================================================================
echo -e "\n${YELLOW}STEP 1: Deploying AWS Infrastructure with Terraform${NC}"
echo "This will create: VPC, EKS Cluster, ECR Repositories"
echo "Estimated time: 15-20 minutes"
echo ""

cd terraform

echo "Initializing Terraform..."
terraform init

echo ""
echo "Planning infrastructure..."
terraform plan

echo ""
read -p "Review the plan above. Press Enter to apply or Ctrl+C to cancel..."
terraform apply -auto-approve

# Get outputs
ECR_LOAN_APP=$(terraform output -raw ecr_loan_app_url)
ECR_ML_SERVICE=$(terraform output -raw ecr_ml_service_url)
CLUSTER_NAME=$(terraform output -raw cluster_name)
REGION=$(terraform output -raw region)

echo -e "\n${GREEN}✓ Infrastructure deployed successfully!${NC}"
echo "ECR Loan App: $ECR_LOAN_APP"
echo "ECR ML Service: $ECR_ML_SERVICE"

cd ..

# ============================================================================
# STEP 2: Login to ECR
# ============================================================================
echo -e "\n${YELLOW}STEP 2: Logging into ECR${NC}"
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $(echo $ECR_LOAN_APP | cut -d'/' -f1)
echo -e "${GREEN}✓ Logged in to ECR${NC}"

# ============================================================================
# STEP 3: Build and Push Docker Images
# ============================================================================
echo -e "\n${YELLOW}STEP 3: Building and Pushing Docker Images${NC}"

echo "Building loan-app..."
docker build -t loan-app:latest .
docker tag loan-app:latest $ECR_LOAN_APP:latest
docker push $ECR_LOAN_APP:latest
echo -e "${GREEN}✓ loan-app pushed to ECR${NC}"

echo "Building ml-service..."
docker build -t ml-service:latest ./ml-service
docker tag ml-service:latest $ECR_ML_SERVICE:latest
docker push $ECR_ML_SERVICE:latest
echo -e "${GREEN}✓ ml-service pushed to ECR${NC}"

# ============================================================================
# STEP 4: Configure kubectl
# ============================================================================
echo -e "\n${YELLOW}STEP 4: Configuring kubectl for EKS${NC}"
aws eks update-kubeconfig --region $REGION --name $CLUSTER_NAME
echo -e "${GREEN}✓ kubectl configured${NC}"

# ============================================================================
# STEP 5: Update K8s manifests with ECR URLs
# ============================================================================
echo -e "\n${YELLOW}STEP 5: Updating K8s manifests with ECR URLs${NC}"
ECR_BASE=$(echo $ECR_LOAN_APP | sed 's|/loan-app||')

# Update the image URLs in manifests
sed -i "s|REPLACE_WITH_ECR_URL|$ECR_BASE|g" k8s/loan-app.yaml
sed -i "s|REPLACE_WITH_ECR_URL|$ECR_BASE|g" k8s/ml-service.yaml
echo -e "${GREEN}✓ K8s manifests updated${NC}"

# ============================================================================
# STEP 6: Deploy to Kubernetes
# ============================================================================
echo -e "\n${YELLOW}STEP 6: Deploying to Kubernetes${NC}"

echo "Creating namespace..."
kubectl apply -f k8s/namespace.yaml

echo "Deploying MySQL..."
kubectl apply -f k8s/mysql.yaml

echo "Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql -n loan-app --timeout=120s

echo "Deploying ML Service..."
kubectl apply -f k8s/ml-service.yaml

echo "Deploying Loan App..."
kubectl apply -f k8s/loan-app.yaml

echo -e "${GREEN}✓ All services deployed!${NC}"

# ============================================================================
# STEP 7: Get Service URL
# ============================================================================
echo -e "\n${YELLOW}STEP 7: Getting Service URL${NC}"
echo "Waiting for LoadBalancer to be assigned (this may take 2-3 minutes)..."

sleep 30

EXTERNAL_URL=$(kubectl get svc loan-app -n loan-app -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

echo ""
echo "=============================================="
echo -e "${GREEN}  DEPLOYMENT COMPLETE!${NC}"
echo "=============================================="
echo ""
echo "Application URL: http://$EXTERNAL_URL"
echo ""
echo "Useful commands:"
echo "  kubectl get pods -n loan-app          # Check pod status"
echo "  kubectl logs -f deployment/loan-app -n loan-app  # View logs"
echo "  kubectl get svc -n loan-app           # View services"
echo ""
echo -e "${YELLOW}IMPORTANT: Remember to run cleanup.sh when done to avoid charges!${NC}"
