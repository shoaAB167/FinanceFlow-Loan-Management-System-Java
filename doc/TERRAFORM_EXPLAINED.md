# Terraform main.tf Explained Like You're 5 🧒

This guide explains **every single line** of the `terraform/main.tf` file. By the end, you'll understand Infrastructure as Code!

---

## What is Terraform?

Imagine you want to build a house. You have two options:

| Option                   | How It Works                                                | Problems                                |
| ------------------------ | ----------------------------------------------------------- | --------------------------------------- |
| **Manual** (AWS Console) | Click buttons in a website to create each room              | Slow, error-prone, can't easily rebuild |
| **Terraform** (Code)     | Write a blueprint, Terraform builds the house automatically | Fast, repeatable, version-controlled    |

**Terraform reads your `.tf` files and creates the actual AWS resources.**

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│   main.tf       │      │    Terraform    │      │      AWS        │
│   variables.tf  │ ───▶ │   (the builder) │ ───▶│   (the cloud)   │
│   (blueprints)  │      │                 │      │                 │
└─────────────────┘      └─────────────────┘      └─────────────────┘
```

---

## The 3 Terraform Commands You'll Use

```bash
terraform init      # Download plugins (like installing tools)
terraform plan      # Preview what will be created (like a simulation)
terraform apply     # Actually create the resources (build it!)
terraform destroy   # Delete everything (demolish the house)
```

---

# File Structure

Our `main.tf` has **5 main sections**:

```
┌─────────────────────────────────────────┐
│  1. TERRAFORM SETTINGS (Lines 6-19)     │  ← "What tools do I need?"
├─────────────────────────────────────────┤
│  2. VPC MODULE (Lines 27-60)            │  ← "Build me a network"
├─────────────────────────────────────────┤
│  3. ECR REPOSITORIES (Lines 65-91)      │  ← "Create Docker storage"
├─────────────────────────────────────────┤
│  4. EKS MODULE (Lines 96-152)           │  ← "Create Kubernetes"
├─────────────────────────────────────────┤
│  5. OUTPUTS (Lines 158-191)             │  ← "Tell me what was created"
└─────────────────────────────────────────┘
```

---

# SECTION 1: Terraform Settings (Lines 6-22)

## Lines 6-15: Version Requirements

```hcl
terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}
```

| Line                          | What It Means                                      |
| ----------------------------- | -------------------------------------------------- |
| `terraform {`                 | "Here are settings for Terraform itself"           |
| `required_version = ">= 1.0"` | "You need Terraform version 1.0 or higher"         |
| `required_providers {`        | "Here are the plugins I need"                      |
| `aws = {`                     | "I need the AWS plugin"                            |
| `source = "hashicorp/aws"`    | "Download it from HashiCorp (the official source)" |
| `version = "~> 5.0"`          | "Use version 5.x (5.0, 5.1, 5.2, etc.)"            |

**The `~>` symbol**: "Compatible with" - allows minor updates (5.0 → 5.9) but not major (5.0 → 6.0)

**Real-world analogy**: Like saying "I need a hammer from the hardware store, version 5 or newer"

---

## Lines 17-19: Provider Configuration

```hcl
provider "aws" {
  region = var.region
}
```

| Line                  | What It Means                                        |
| --------------------- | ---------------------------------------------------- |
| `provider "aws" {`    | "Configure the AWS plugin"                           |
| `region = var.region` | "Use the region from the variables file" (us-east-1) |

**What's `var.region`?**

- `var.` means "get this value from variables.tf file"
- Like saying "look at the settings page for the region"

---

## Line 22: Data Source (Reading Existing Information)

```hcl
data "aws_caller_identity" "current" {}
```

| Part                    | What It Means                                             |
| ----------------------- | --------------------------------------------------------- |
| `data`                  | "I want to READ information, not CREATE something"        |
| `"aws_caller_identity"` | "Get info about who is logged in to AWS"                  |
| `"current"`             | The name we give this data (so we can reference it later) |
| `{}`                    | No special options needed                                 |

**Why do we need this?** To get your AWS Account ID (like `123456789012`) for building URLs later.

**Real-world analogy**: Like asking "Who am I logged in as?" before starting work.

---

# SECTION 2: VPC Module (Lines 27-60)

## What is a VPC?

**VPC = Virtual Private Cloud** - Your own private network inside AWS.

```
┌─────────────────── AWS Cloud ───────────────────┐
│                                                  │
│   ┌─────────── Your VPC ───────────┐            │
│   │     (Your private network)      │            │
│   │                                 │            │
│   │  ┌─────────┐    ┌─────────┐    │            │
│   │  │ Public  │    │ Private │    │            │
│   │  │ Subnet  │    │ Subnet  │    │            │
│   │  │(internet│    │(no direct│   │            │
│   │  │ access) │    │internet) │   │            │
│   │  └─────────┘    └─────────┘    │            │
│   └─────────────────────────────────┘            │
│                                                  │
│   Other people's VPCs (you can't see them)      │
└──────────────────────────────────────────────────┘
```

## Lines 27-32: Module Declaration

```hcl
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"

  name = "${var.cluster_name}-vpc"
  cidr = "10.0.0.0/16"
```

| Line                                       | What It Means                                                                |
| ------------------------------------------ | ---------------------------------------------------------------------------- |
| `module "vpc" {`                           | "Use a pre-built recipe called 'vpc'"                                        |
| `source = "terraform-aws-modules/vpc/aws"` | "Download this recipe from Terraform Registry" (like npm for infrastructure) |
| `version = "5.0.0"`                        | "Use version 5.0.0 of this recipe"                                           |
| `name = "${var.cluster_name}-vpc"`         | Name it "loan-app-cluster-vpc"                                               |
| `cidr = "10.0.0.0/16"`                     | "Use IP addresses from 10.0.0.0 to 10.0.255.255"                             |

### What is CIDR? (10.0.0.0/16)

```
10.0.0.0/16 means:
├── 10.0.    = Fixed (the neighborhood)
└── 0.0-255.255 = 65,536 available addresses

Like saying: "I want all house numbers on 10.0.x.x street"
```

---

## Lines 34-36: Availability Zones and Subnets

```hcl
  azs             = ["${var.region}a", "${var.region}b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]
```

| Line                                 | What It Means                                |
| ------------------------------------ | -------------------------------------------- |
| `azs = ["us-east-1a", "us-east-1b"]` | Use 2 data centers (for high availability)   |
| `private_subnets`                    | Networks that can't be reached from internet |
| `public_subnets`                     | Networks that CAN be reached from internet   |

### Why Two Data Centers?

```
┌─────── us-east-1a ───────┐  ┌─────── us-east-1b ───────┐
│  (Data Center 1)         │  │  (Data Center 2)         │
│  ┌─────────┐ ┌─────────┐ │  │  ┌─────────┐ ┌─────────┐ │
│  │Private  │ │ Public  │ │  │  │Private  │ │ Public  │ │
│  │10.0.1.x │ │10.0.101 │ │  │  │10.0.2.x │ │10.0.102 │ │
│  └─────────┘ └─────────┘ │  │  └─────────┘ └─────────┘ │
└──────────────────────────┘  └──────────────────────────┘
         │                              │
         └──────── If one fails, ───────┘
                   the other works!
```

---

## Lines 38-42: NAT Gateway

```hcl
  enable_nat_gateway   = true
  single_nat_gateway   = true
  enable_vpn_gateway   = false
  enable_dns_hostnames = true
```

### What is NAT Gateway?

**Problem**: Private subnets can't access the internet (by design). But how do they download updates?

**Solution**: NAT Gateway - a "translator" that lets private resources talk to the internet, but internet can't reach back.

```
┌─────── Private Subnet ───────┐      ┌─── NAT Gateway ───┐      ┌─── Internet ───┐
│                              │      │                   │      │               │
│   EKS Node: "I need to      │ ───▶ │  "I'll ask on    │ ───▶ │               │
│   download Docker images"    │      │   your behalf"    │      │               │
│                              │ ◀─── │  "Here's the     │ ◀─── │               │
│   EKS Node: "Thanks!"        │      │   response"       │      │               │
└──────────────────────────────┘      └───────────────────┘      └───────────────┘

Internet trying to reach EKS: ❌ BLOCKED! (Can't reach private subnet directly)
```

| Setting                       | What It Means                        |
| ----------------------------- | ------------------------------------ |
| `enable_nat_gateway = true`   | "Yes, create a NAT Gateway"          |
| `single_nat_gateway = true`   | "Only 1 (saves ~$32/month vs 2)"     |
| `enable_vpn_gateway = false`  | "No VPN needed"                      |
| `enable_dns_hostnames = true` | "Allow names like mysql.loan-app..." |

---

## Lines 44-53: Subnet Tags (Labels for Kubernetes)

```hcl
  public_subnet_tags = {
    "kubernetes.io/role/elb"                    = "1"
    "kubernetes.io/cluster/${var.cluster_name}" = "shared"
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb"           = "1"
    "kubernetes.io/cluster/${var.cluster_name}" = "shared"
  }
```

**Why these weird tags?**

Kubernetes needs to know:

- "Which subnets can I put **public** load balancers in?" → `kubernetes.io/role/elb = 1`
- "Which subnets can I put **internal** load balancers in?" → `kubernetes.io/role/internal-elb = 1`
- "Which cluster owns these subnets?" → `kubernetes.io/cluster/loan-app-cluster = shared`

**Real-world analogy**: Like putting signs on rooms - "Meeting Room", "Kitchen", "Server Room"

---

# SECTION 3: ECR Repositories (Lines 65-91)

## What is ECR?

**ECR = Elastic Container Registry** - A place to store your Docker images (like Docker Hub, but private and on AWS).

```
┌─────── Your Computer ───────┐      ┌─────── ECR ───────┐      ┌─────── EKS ───────┐
│                             │      │                    │      │                   │
│  docker build               │ ───▶ │  loan-app:latest  │ ───▶ │  Running pods     │
│  docker push                │      │  ml-service:latest│      │  pull images      │
│                             │      │                    │      │                   │
└─────────────────────────────┘      └────────────────────┘      └───────────────────┘
```

## Lines 65-77: Loan App Repository

```hcl
resource "aws_ecr_repository" "loan_app" {
  name                 = "loan-app"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = false
  }

  tags = {
    Project = "loan-app"
  }
}
```

| Line                               | What It Means                                           |
| ---------------------------------- | ------------------------------------------------------- |
| `resource`                         | "CREATE something new" (vs `data` which reads)          |
| `"aws_ecr_repository"`             | The type of resource (Docker image storage)             |
| `"loan_app"`                       | Our internal name (used to reference it later)          |
| `name = "loan-app"`                | The actual name in AWS                                  |
| `image_tag_mutability = "MUTABLE"` | "Allow overwriting tags" (push `latest` multiple times) |
| `force_delete = true`              | "Delete even if it has images" (for easy cleanup)       |
| `scan_on_push = false`             | "Don't scan for vulnerabilities" (saves money)          |

**Real-world analogy**: Creating a folder on Google Drive called "loan-app" to store your Docker images.

---

# SECTION 4: EKS Module (Lines 96-152)

## What is EKS?

**EKS = Elastic Kubernetes Service** - AWS manages Kubernetes for you.

Without EKS, you'd have to:

- Set up 3+ servers for Kubernetes control plane
- Handle updates, security, backups
- Figure out networking

With EKS: AWS does all that. You just use it!

```
┌───────────────────────────────────────────────────────────────┐
│                        EKS Cluster                            │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  Control Plane (AWS MANAGES THIS)                        │  │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐                    │  │
│  │  │   API   │ │  etcd   │ │Scheduler│                    │  │
│  │  │ Server  │ │(database)│ │         │                    │  │
│  │  └─────────┘ └─────────┘ └─────────┘                    │  │
│  │  Cost: $0.10/hour (fixed)                                │  │
│  └─────────────────────────────────────────────────────────┘  │
│                            │                                   │
│                            ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  Worker Nodes (YOUR EC2 INSTANCES)                       │  │
│  │  ┌─────────────────┐                                     │  │
│  │  │   t3.small      │  ← Your pods run here               │  │
│  │  │   (2GB RAM)     │  ← Cost: $0.02/hour                 │  │
│  │  └─────────────────┘                                     │  │
│  └─────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘
```

## Lines 96-107: Basic Cluster Settings

```hcl
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"

  cluster_name    = var.cluster_name
  cluster_version = "1.29"

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  cluster_endpoint_public_access = true
```

| Line                                       | What It Means                                  |
| ------------------------------------------ | ---------------------------------------------- |
| `module "eks"`                             | "Use the pre-built EKS recipe"                 |
| `source = "terraform-aws-modules/eks/aws"` | From Terraform Registry                        |
| `cluster_name = var.cluster_name`          | "loan-app-cluster"                             |
| `cluster_version = "1.29"`                 | Kubernetes version 1.29                        |
| `vpc_id = module.vpc.vpc_id`               | "Put this cluster in the VPC we created above" |
| `subnet_ids = module.vpc.private_subnets`  | "Run nodes in the private subnets"             |
| `cluster_endpoint_public_access = true`    | "Allow kubectl from anywhere"                  |

**The `module.` reference**: When we write `module.vpc.vpc_id`, we're saying "get the `vpc_id` output from the `vpc` module we created earlier." It's like referencing a cell in Excel!

---

## Lines 109-123: Cluster Addons

```hcl
  enable_irsa = true

  cluster_addons = {
    coredns = {
      most_recent = true
    }
    kube-proxy = {
      most_recent = true
    }
    vpc-cni = {
      most_recent = true
    }
  }
```

| Addon         | What It Does                                                       |
| ------------- | ------------------------------------------------------------------ |
| `enable_irsa` | IAM Roles for Service Accounts (secure way for pods to access AWS) |
| `coredns`     | DNS server inside cluster (so pods can find each other by name)    |
| `kube-proxy`  | Network routing inside cluster                                     |
| `vpc-cni`     | AWS networking plugin (connects pods to VPC)                       |

**Real-world analogy**: These are like utilities (electricity, water, internet) that every building needs.

---

## Lines 125-142: Worker Node Configuration

```hcl
  eks_managed_node_groups = {
    default = {
      name = var.node_group_name

      instance_types = var.instance_types
      capacity_type  = "ON_DEMAND"

      min_size     = 1
      max_size     = 2
      desired_size = var.desired_capacity

      iam_role_additional_policies = {
        AmazonEC2ContainerRegistryReadOnly = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
      }
    }
  }
```

| Line                                 | What It Means                                                    |
| ------------------------------------ | ---------------------------------------------------------------- |
| `eks_managed_node_groups`            | "Create EC2 instances that Kubernetes will use"                  |
| `default = {`                        | Name of this node group                                          |
| `instance_types = ["t3.small"]`      | Type of EC2 (2 vCPU, 2GB RAM)                                    |
| `capacity_type = "ON_DEMAND"`        | Regular pricing (vs SPOT which is cheaper but can be terminated) |
| `min_size = 1`                       | Never go below 1 node                                            |
| `max_size = 2`                       | Never go above 2 nodes                                           |
| `desired_size = 1`                   | Start with 1 node                                                |
| `AmazonEC2ContainerRegistryReadOnly` | Permission to pull images from ECR                               |

### Auto Scaling Explained

```
                        desired_size = 1
                              │
┌─────────────────────────────┼─────────────────────────────┐
│                             ▼                             │
│  min=1                 [Node 1]                 max=2     │
│    │                       │                       │      │
│    │         If traffic increases...              │      │
│    │                       │                       │      │
│    │              [Node 1] [Node 2]               │      │
│    │                       │                       │      │
│    │         If traffic decreases...              │      │
│    │                       │                       │      │
│    │                  [Node 1]                    │      │
│    │               (back to 1)                    │      │
└────┴───────────────────────────────────────────────┴──────┘
```

---

# SECTION 5: Outputs (Lines 158-191)

## What are Outputs?

After Terraform runs, you need to know:

- What's my cluster endpoint?
- What's my ECR URL?
- What command do I run next?

**Outputs print this information!**

```hcl
output "cluster_name" {
  description = "EKS Cluster Name"
  value       = module.eks.cluster_name
}

output "ecr_login_command" {
  description = "Command to login to ECR"
  value       = "aws ecr get-login-password --region ${var.region} | docker login..."
}
```

After running `terraform apply`, you'll see:

```
Apply complete! Resources: 54 added, 0 changed, 0 destroyed.

Outputs:

cluster_name = "loan-app-cluster"
cluster_endpoint = "https://ABC123.eks.amazonaws.com"
ecr_loan_app_url = "123456789.dkr.ecr.us-east-1.amazonaws.com/loan-app"
configure_kubectl = "aws eks update-kubeconfig --region us-east-1 --name loan-app-cluster"
ecr_login_command = "aws ecr get-login-password..."
```

**Real-world analogy**: Like a receipt after buying something - "Here's what was created and how to use it."

---

# Variables File (variables.tf)

The `variables.tf` file defines **configurable values**:

```hcl
variable "region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}
```

| Part                    | What It Means                         |
| ----------------------- | ------------------------------------- |
| `variable "region"`     | Define a variable called "region"     |
| `description`           | Help text (shown in `terraform plan`) |
| `type = string`         | Must be a text value                  |
| `default = "us-east-1"` | If not specified, use this            |

### How to Override Variables

```bash
# Use default values
terraform apply

# Override region
terraform apply -var="region=eu-west-1"

# Override multiple
terraform apply -var="region=eu-west-1" -var="desired_capacity=2"
```

---

# Complete Resource Flow 🎯

```
terraform apply
       │
       ▼
┌──────────────────────────────────────────────────────────────────────┐
│ 1. VPC CREATED                                                        │
│    └── 10.0.0.0/16 network                                           │
│        ├── Public Subnets (10.0.101.x, 10.0.102.x)                   │
│        ├── Private Subnets (10.0.1.x, 10.0.2.x)                      │
│        └── NAT Gateway (for private → internet)                       │
│                                                                       │
│ 2. ECR CREATED                                                        │
│    ├── loan-app repository                                           │
│    └── ml-service repository                                          │
│                                                                       │
│ 3. EKS CREATED                                                        │
│    ├── Control Plane (API server, scheduler, etcd)                   │
│    ├── 1x t3.small EC2 Node                                          │
│    └── Addons (CoreDNS, kube-proxy, VPC-CNI)                         │
│                                                                       │
│ 4. OUTPUTS DISPLAYED                                                  │
│    └── ECR URLs, kubectl command, etc.                               │
└──────────────────────────────────────────────────────────────────────┘
```

---

# Quick Reference Card

| Terraform Concept   | What It Does               | Real-World Analogy             |
| ------------------- | -------------------------- | ------------------------------ |
| `terraform {}`      | Configure Terraform itself | Set up your tools              |
| `provider`          | Which cloud to use         | Which contractor to hire       |
| `module`            | Pre-built recipes          | IKEA furniture (just assemble) |
| `resource`          | Create something new       | Build a custom thing           |
| `data`              | Read existing info         | Look up information            |
| `variable`          | Configurable values        | Form fields                    |
| `output`            | Print results              | Receipt                        |
| `${var.x}`          | Variable interpolation     | Fill in the blank              |
| `module.vpc.vpc_id` | Reference another resource | "See Room 101"                 |

---

# Terraform Lifecycle

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                      │
│  terraform init     → Download plugins (do once)                     │
│        │                                                             │
│        ▼                                                             │
│  terraform plan     → Preview changes (see what would happen)        │
│        │                                                             │
│        ▼                                                             │
│  terraform apply    → Create/update resources                        │
│        │                                                             │
│        │  (use your infrastructure...)                               │
│        │                                                             │
│        ▼                                                             │
│  terraform destroy  → Delete everything ($0 charges!)                │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 🎓 You Now Understand Terraform!

You can now:

- Read any Terraform file and understand what it creates
- Modify configurations (change instance types, add resources)
- Explain Infrastructure as Code in your LinkedIn post! 🚀

**Remember**: Unlike clicking in AWS Console, your infrastructure is now **version-controlled, repeatable, and documented!**
