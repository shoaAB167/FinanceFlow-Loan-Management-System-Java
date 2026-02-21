# DevOps Integration Guide: From Code to Cloud

This guide explains the DevOps layer for the Loan Management System, designed to help you deploy to **AWS EKS** using Docker, Kubernetes, and Terraform.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                          AWS Cloud                                   │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    VPC (10.0.0.0/16)                         │    │
│  │  ┌─────────────────┐    ┌─────────────────────────────────┐ │    │
│  │  │  Public Subnet  │    │       Private Subnet             │ │    │
│  │  │  ┌───────────┐  │    │  ┌─────────────────────────┐    │ │    │
│  │  │  │    ALB    │  │───▶│  │   EKS Cluster           │    │ │    │
│  │  │  └───────────┘  │    │  │  ┌─────────────────────┐│    │ │    │
│  │  │  ┌───────────┐  │    │  │  │ loan-app (Java)    ││    │ │    │
│  │  │  │    NAT    │──│────│  │  │ ml-service (Python)││    │ │    │
│  │  │  └───────────┘  │    │  │  │ mysql (database)   ││    │ │    │
│  │  └─────────────────┘    │  │  └─────────────────────┘│    │ │    │
│  │                         │  └─────────────────────────────┘ │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
│  ┌──────────────┐                                                   │
│  │     ECR      │  Docker Image Registry                            │
│  └──────────────┘                                                   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 1. Containerization (Docker)

**Goal:** Package applications into portable containers.

### Dockerfiles Created:

- **`Dockerfile`** (Java): Multi-stage build - compiles with Maven, runs with slim JDK
- **`ml-service/Dockerfile`** (Python): Flask API with scikit-learn

### Local Testing:

```bash
docker-compose up --build
```

---

## 2. Orchestration (Kubernetes)

**Goal:** Manage containers at scale with self-healing and load balancing.

### Manifests (`k8s/`):

| File              | Purpose                                    |
| ----------------- | ------------------------------------------ |
| `namespace.yaml`  | Isolated namespace for the app             |
| `loan-app.yaml`   | Java app deployment + LoadBalancer service |
| `ml-service.yaml` | Python ML service (internal only)          |
| `mysql.yaml`      | Database with persistent storage           |

---

## 3. Infrastructure as Code (Terraform)

**Goal:** Automate AWS resource creation.

### Resources Created (`terraform/`):

- **VPC** with public/private subnets
- **EKS Cluster** (Kubernetes control plane)
- **EC2 Node Group** (worker nodes)
- **ECR Repositories** (Docker image storage)

### Cost Optimization:

- Single NAT Gateway (~$0.045/hour saved)
- 1 EC2 node instead of 2 (~$0.02/hour saved)
- t3.small instance type

---

## 4. Quick Start Deployment

### Prerequisites:

```bash
# Verify all tools are installed
aws --version        # AWS CLI v2
terraform --version  # Terraform
kubectl version --client  # kubectl
docker --version     # Docker

# Configure AWS credentials
aws configure
```

### Deploy (Windows):

```cmd
cd scripts
deploy.bat
```

### Deploy (Linux/Mac):

```bash
cd scripts
chmod +x deploy.sh
./deploy.sh
```

### Manual Step-by-Step:

#### Step 1: Create Infrastructure

```bash
cd terraform
terraform init
terraform plan
terraform apply -auto-approve
```

#### Step 2: Push Docker Images to ECR

```bash
# Login to ECR (copy command from terraform output)
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account>.dkr.ecr.us-east-1.amazonaws.com

# Build and push loan-app
docker build -t loan-app .
docker tag loan-app:latest <ecr-url>/loan-app:latest
docker push <ecr-url>/loan-app:latest

# Build and push ml-service
docker build -t ml-service ./ml-service
docker tag ml-service:latest <ecr-url>/ml-service:latest
docker push <ecr-url>/ml-service:latest
```

#### Step 3: Configure kubectl

```bash
aws eks update-kubeconfig --region us-east-1 --name loan-app-cluster
```

#### Step 4: Deploy to Kubernetes

```bash
# Update k8s/loan-app.yaml and k8s/ml-service.yaml with ECR URLs first!

kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/ml-service.yaml
kubectl apply -f k8s/loan-app.yaml
```

#### Step 5: Verify Deployment

```bash
kubectl get pods -n loan-app
kubectl get svc -n loan-app
```

---

## 5. Useful Commands

```bash
# View pod status
kubectl get pods -n loan-app

# View logs
kubectl logs -f deployment/loan-app -n loan-app

# Describe pod (for debugging)
kubectl describe pod <pod-name> -n loan-app

# Port forward for local testing
kubectl port-forward svc/loan-app 8080:80 -n loan-app

# Scale deployment
kubectl scale deployment loan-app --replicas=2 -n loan-app
```

---

## 6. Cleanup (IMPORTANT!)

**Run this when done to avoid AWS charges:**

```bash
# Delete K8s resources
kubectl delete -f k8s/

# Destroy AWS infrastructure
cd terraform
terraform destroy -auto-approve
```

**Or use the cleanup script:**

```cmd
scripts\cleanup.bat  # Windows
./scripts/cleanup.sh # Linux/Mac
```

---

## Cost Summary

| Resource          | Cost/Hour       | Purpose                            |
| ----------------- | --------------- | ---------------------------------- |
| EKS Control Plane | $0.10           | Kubernetes API                     |
| t3.small EC2      | $0.02           | Worker node                        |
| NAT Gateway       | $0.045          | Internet access for private subnet |
| Load Balancer     | $0.02           | External access                    |
| **Total**         | **~$0.19/hour** |                                    |

> ⚠️ **For a 4-hour learning session: ~$0.76 total**
>
> Remember to run `terraform destroy` when done!
