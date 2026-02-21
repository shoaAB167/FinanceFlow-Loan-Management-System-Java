@echo off
REM ============================================================================
REM Cleanup Script - IMPORTANT: Run when done to avoid AWS charges!
REM ============================================================================

echo ==============================================
echo   Cleaning up AWS Resources
echo ==============================================
echo.
echo This will delete:
echo   - All Kubernetes resources
echo   - EKS Cluster
echo   - ECR Repositories
echo   - VPC and networking
echo.
echo Press any key to continue or Ctrl+C to cancel...
pause > nul

echo.
echo Deleting Kubernetes resources...
kubectl delete -f k8s\ --ignore-not-found=true

echo.
echo Destroying Terraform infrastructure...
cd terraform
terraform destroy -auto-approve

echo.
echo ==============================================
echo   CLEANUP COMPLETE!
echo ==============================================
echo.
echo All AWS resources have been deleted.
echo No more charges will be incurred.
