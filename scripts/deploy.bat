@echo off
REM ============================================================================
REM AWS EKS Deployment Script for Loan Application (Windows)
REM Run each step manually to learn!
REM ============================================================================

echo ==============================================
echo   AWS EKS Deployment - Loan Application
echo ==============================================
echo.

REM ============================================================================
REM STEP 1: Initialize and Apply Terraform
REM ============================================================================
echo STEP 1: Deploying AWS Infrastructure with Terraform
echo This will create: VPC, EKS Cluster, ECR Repositories
echo Estimated time: 15-20 minutes
echo.

cd terraform

echo Initializing Terraform...
terraform init

echo.
echo Planning infrastructure...
terraform plan

echo.
echo Review the plan above. 
pause

terraform apply -auto-approve

REM Get outputs
for /f "tokens=*" %%a in ('terraform output -raw ecr_loan_app_url') do set ECR_LOAN_APP=%%a
for /f "tokens=*" %%a in ('terraform output -raw ecr_ml_service_url') do set ECR_ML_SERVICE=%%a
for /f "tokens=*" %%a in ('terraform output -raw cluster_name') do set CLUSTER_NAME=%%a
for /f "tokens=*" %%a in ('terraform output -raw region') do set REGION=%%a

echo.
echo Infrastructure deployed successfully!
echo ECR Loan App: %ECR_LOAN_APP%
echo ECR ML Service: %ECR_ML_SERVICE%

cd ..

REM ============================================================================
REM STEP 2: Login to ECR
REM ============================================================================
echo.
echo STEP 2: Logging into ECR

REM Extract account ID from ECR URL
for /f "tokens=1 delims=." %%a in ("%ECR_LOAN_APP%") do set ACCOUNT_ID=%%a

aws ecr get-login-password --region %REGION% | docker login --username AWS --password-stdin %ACCOUNT_ID%.dkr.ecr.%REGION%.amazonaws.com
echo Logged in to ECR

REM ============================================================================
REM STEP 3: Build and Push Docker Images
REM ============================================================================
echo.
echo STEP 3: Building and Pushing Docker Images

echo Building loan-app...
docker build -t loan-app:latest .
docker tag loan-app:latest %ECR_LOAN_APP%:latest
docker push %ECR_LOAN_APP%:latest
echo loan-app pushed to ECR

echo Building ml-service...
docker build -t ml-service:latest ./ml-service
docker tag ml-service:latest %ECR_ML_SERVICE%:latest
docker push %ECR_ML_SERVICE%:latest
echo ml-service pushed to ECR

REM ============================================================================
REM STEP 4: Configure kubectl
REM ============================================================================
echo.
echo STEP 4: Configuring kubectl for EKS
aws eks update-kubeconfig --region %REGION% --name %CLUSTER_NAME%
echo kubectl configured

REM ============================================================================
REM STEP 5: Update K8s manifests with ECR URLs (Manual step)
REM ============================================================================
echo.
echo STEP 5: MANUAL STEP REQUIRED
echo.
echo Please update the following files manually:
echo   k8s\loan-app.yaml - Replace REPLACE_WITH_ECR_URL with:
for /f "tokens=1 delims=/" %%a in ("%ECR_LOAN_APP%") do echo     %%a
echo.
echo   k8s\ml-service.yaml - Same replacement
echo.
pause

REM ============================================================================
REM STEP 6: Deploy to Kubernetes
REM ============================================================================
echo.
echo STEP 6: Deploying to Kubernetes

echo Creating namespace...
kubectl apply -f k8s\namespace.yaml

echo Deploying MySQL...
kubectl apply -f k8s\mysql.yaml

echo Waiting for MySQL to be ready (30 seconds)...
timeout /t 30

echo Deploying ML Service...
kubectl apply -f k8s\ml-service.yaml

echo Deploying Loan App...
kubectl apply -f k8s\loan-app.yaml

echo All services deployed!

REM ============================================================================
REM STEP 7: Get Service URL
REM ============================================================================
echo.
echo STEP 7: Getting Service URL
echo Waiting for LoadBalancer (30 seconds)...
timeout /t 30

kubectl get svc loan-app -n loan-app

echo.
echo ==============================================
echo   DEPLOYMENT COMPLETE!
echo ==============================================
echo.
echo Useful commands:
echo   kubectl get pods -n loan-app
echo   kubectl logs -f deployment/loan-app -n loan-app
echo   kubectl get svc -n loan-app
echo.
echo IMPORTANT: Run cleanup.bat when done to avoid charges!
