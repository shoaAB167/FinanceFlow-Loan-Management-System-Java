# Kubernetes loan-app.yaml Explained Like You're 5 🧒

This guide explains **every single line** of the `k8s/loan-app.yaml` file. No prior Kubernetes knowledge needed!

---

## What is This File?

Think of Kubernetes as a **robot manager** that runs your apps. But this robot doesn't speak English - it reads YAML files.

The `loan-app.yaml` is an **instruction manual** that tells Kubernetes:

1. **What** to run (your Java app in a Docker container)
2. **How many** copies to run
3. **How** to keep it healthy
4. **How** users on the internet can access it

---

## The File Has Two Parts

```
┌─────────────────────────────────────┐
│  PART 1: Deployment                 │  ← "Run my app"
│  (Lines 4-61)                       │
├─────────────────────────────────────┤
│  ---                                │  ← Separator (like a page break)
├─────────────────────────────────────┤
│  PART 2: Service                    │  ← "Let people access my app"
│  (Lines 67-81)                      │
└─────────────────────────────────────┘
```

---

# PART 1: Deployment (The "Run My App" Instructions)

## Lines 4-5: What Type of Thing is This?

```yaml
apiVersion: apps/v1
kind: Deployment
```

| Line                  | What It Means                                                                                                            |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| `apiVersion: apps/v1` | "Hey Kubernetes, I'm using version 1 of your 'apps' instruction set" - like saying "I'm writing in English, not Spanish" |
| `kind: Deployment`    | "This instruction is about **running an application**" (not a database, not a network rule - an app)                     |

**Real-world analogy**: Like filling out a form - first you specify what TYPE of form it is.

---

## Lines 6-10: Identity Card (Metadata)

```yaml
metadata:
  name: loan-app
  namespace: loan-app
  labels:
    app: loan-app
```

| Line                  | What It Means                                                                           |
| --------------------- | --------------------------------------------------------------------------------------- |
| `metadata:`           | "Here comes the identity information"                                                   |
| `name: loan-app`      | **The name** - like naming your pet. Kubernetes will call this deployment "loan-app"    |
| `namespace: loan-app` | **The folder** - like putting files in a folder. Keeps our app separate from other apps |
| `labels:`             | **Tags** - like hashtags on Instagram                                                   |
| `app: loan-app`       | A tag that says "this belongs to loan-app". Used to find/filter things later            |

**Real-world analogy**: Like a person's ID card - name, department, and ID tags.

---

## Lines 11-15: The Specification (The Actual Instructions)

```yaml
spec:
  replicas: 1
  selector:
    matchLabels:
      app: loan-app
```

| Line            | What It Means                                                                                             |
| --------------- | --------------------------------------------------------------------------------------------------------- |
| `spec:`         | "Here are the actual instructions" (spec = specification)                                                 |
| `replicas: 1`   | **How many copies** to run. `1` means one copy. If this was `3`, Kubernetes would run 3 identical copies! |
| `selector:`     | "How do I identify which pods belong to this deployment?"                                                 |
| `matchLabels:`  | "Look for things with these tags"                                                                         |
| `app: loan-app` | "Find anything tagged with `app: loan-app`"                                                               |

**Real-world analogy**:

- `replicas: 1` = "Hire 1 employee for this job"
- `replicas: 3` = "Hire 3 employees for this job" (for more work capacity)

---

## Lines 16-27: The Pod Template (What Each Copy Looks Like)

```yaml
template:
  metadata:
    labels:
      app: loan-app
  spec:
    containers:
      - name: loan-app
        image: REPLACE_WITH_ECR_URL/loan-app:latest
        imagePullPolicy: Always
        ports:
          - containerPort: 8080
```

### The Template Section

| Line                              | What It Means                                              |
| --------------------------------- | ---------------------------------------------------------- |
| `template:`                       | "Here's the blueprint for each copy (pod)"                 |
| `metadata: labels: app: loan-app` | Each pod gets this tag (so the selector above can find it) |

### The Container Section

| Line                        | What It Means                                                     |
| --------------------------- | ----------------------------------------------------------------- |
| `spec:`                     | "Here are the container details"                                  |
| `containers:`               | "Here's the list of containers to run" (usually just 1)           |
| `- name: loan-app`          | The `-` means "list item". This container is named "loan-app"     |
| `image: ...loan-app:latest` | **The Docker image to use** - like saying "install this software" |
| `imagePullPolicy: Always`   | "Always download the latest version" (vs. using a cached old one) |
| `ports:`                    | "Which doors (ports) should be open?"                             |
| `containerPort: 8080`       | "Open door number 8080" - this is where Spring Boot listens       |

**Real-world analogy**:

- `image` = The software installation DVD
- `containerPort` = The door number of your office (so people know where to find you)

```
┌──────────────────────────────────┐
│         Pod (your copy)          │
│  ┌────────────────────────────┐  │
│  │      Container             │  │
│  │   ┌──────────────────┐     │  │
│  │   │   loan-app       │     │  │
│  │   │   (Java app)     │     │  │
│  │   └────────[8080]────┘     │  │  ← Door 8080 is open
│  └────────────────────────────┘  │
└──────────────────────────────────┘
```

---

## Lines 30-38: Environment Variables (Settings)

```yaml
env:
  - name: SPRING_DATASOURCE_URL
    value: "jdbc:mysql://mysql.loan-app.svc.cluster.local:3306/loan_db"
  - name: SPRING_DATASOURCE_USERNAME
    value: "user"
  - name: SPRING_DATASOURCE_PASSWORD
    value: "password"
  - name: ML_SERVICE_URL
    value: "http://ml-service.loan-app.svc.cluster.local:5000/predict"
```

| Line                          | What It Means                                                     |
| ----------------------------- | ----------------------------------------------------------------- |
| `env:`                        | "Here are the environment variables" (settings passed to the app) |
| `name: SPRING_DATASOURCE_URL` | The name of the setting                                           |
| `value: "jdbc:mysql://..."`   | The value of that setting                                         |

### Breaking Down the Database URL:

```
jdbc:mysql://mysql.loan-app.svc.cluster.local:3306/loan_db
└────┬────┘ └──────────────┬─────────────────┘└─┬─┘ └──┬──┘
     │                     │                    │     │
  Protocol         Kubernetes DNS name        Port  Database
  (MySQL)     (how to find MySQL inside       (MySQL's   name
              the Kubernetes cluster)          door)
```

**The Magic DNS Name**: `mysql.loan-app.svc.cluster.local`

- `mysql` = the service name
- `loan-app` = the namespace
- `svc.cluster.local` = "this is inside the Kubernetes cluster"

**Real-world analogy**: Like giving someone an address - "Room MySQL, in Building LoanApp, Floor Services, City Cluster"

---

## Lines 41-47: Resource Limits (How Much Food to Give)

```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

| Line              | What It Means                                                |
| ----------------- | ------------------------------------------------------------ |
| `resources:`      | "How much computer power can this container use?"            |
| `requests:`       | **Minimum guaranteed** - "Reserve at least this much"        |
| `memory: "256Mi"` | 256 Megabytes of RAM (like guaranteeing 256MB of desk space) |
| `cpu: "250m"`     | 250 millicores = 0.25 CPU (25% of one CPU core)              |
| `limits:`         | **Maximum allowed** - "Never use more than this"             |
| `memory: "512Mi"` | Maximum 512MB RAM (if it tries to use more, it gets killed!) |
| `cpu: "500m"`     | Maximum 50% of one CPU core                                  |

**Why is this important?**

- Without limits, one hungry app could eat ALL the memory and crash the whole server
- Our EC2 (t3.small) has only 2GB RAM - we need to share!

```
┌─────────────────────────────────────┐
│  t3.small Server (2GB RAM)          │
│  ┌─────────┐ ┌─────────┐ ┌────────┐ │
│  │loan-app │ │ml-service│ │ mysql │ │
│  │ 512MB   │ │  256MB   │ │ 512MB │ │
│  │  max    │ │   max    │ │  max  │ │
│  └─────────┘ └─────────┘ └────────┘ │
│        ▲                            │
│        └── Everyone gets their fair │
│            share, no one is greedy! │
└─────────────────────────────────────┘
```

---

## Lines 50-61: Health Checks (Is the App Alive?)

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30
```

### Two Types of Health Checks:

| Check            | Question It Asks                    | What Happens If Failed                              |
| ---------------- | ----------------------------------- | --------------------------------------------------- |
| `readinessProbe` | "Are you READY to receive traffic?" | Stops sending new users to this pod                 |
| `livenessProbe`  | "Are you still ALIVE?"              | Restarts the pod (like rebooting a frozen computer) |

### Breaking Down the Probe:

| Line                      | What It Means                                                               |
| ------------------------- | --------------------------------------------------------------------------- |
| `httpGet:`                | "Check by making an HTTP request"                                           |
| `path: /actuator/health`  | "Go to this URL" (Spring Boot's built-in health page)                       |
| `port: 8080`              | "On port 8080"                                                              |
| `initialDelaySeconds: 30` | "Wait 30 seconds after starting before first check" (give app time to boot) |
| `periodSeconds: 10`       | "Then check every 10 seconds"                                               |

**Real-world analogy**:

- `readinessProbe` = A waiter asking "Are you ready to take orders?" before seating customers
- `livenessProbe` = A doctor checking "Are you breathing?" - if not, time for CPR (restart)!

```
          Kubernetes
              │
              ▼
    Every 10s: "GET /actuator/health"
              │
              ▼
┌─────────────────────────┐
│       loan-app          │
│                         │
│  "Yes, I'm healthy!"    │ ──► ✅ Keep running
│         OR              │
│  "..." (no response)    │ ──► ❌ Restart me!
└─────────────────────────┘
```

---

## Line 63: The Separator

```yaml
---
```

This is just a **page break**. It says "end of first document, start of second document."

YAML allows multiple documents in one file, separated by `---`.

---

# PART 2: Service (The "Let People Access My App" Instructions)

## Lines 67-73: What is a Service?

```yaml
apiVersion: v1
kind: Service
metadata:
  name: loan-app
  namespace: loan-app
  labels:
    app: loan-app
```

**Problem**: Pods can die and restart with new IP addresses. How do users find your app?

**Solution**: A **Service** - like a receptionist who always knows where everyone sits!

| Line                                | What It Means                           |
| ----------------------------------- | --------------------------------------- |
| `kind: Service`                     | "This is a networking rule, not an app" |
| `name: loan-app`                    | This service is called "loan-app"       |
| Same namespace and labels as before |                                         |

---

## Lines 74-81: The Service Spec

```yaml
spec:
  selector:
    app: loan-app
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: LoadBalancer
```

### The Selector (Who Does This Apply To?)

| Line            | What It Means                                          |
| --------------- | ------------------------------------------------------ |
| `selector:`     | "Which pods should receive traffic from this service?" |
| `app: loan-app` | "Any pod with the label `app: loan-app`"               |

### The Ports (Door Translation)

| Line               | What It Means                                                            |
| ------------------ | ------------------------------------------------------------------------ |
| `ports:`           | "Here are the port mappings"                                             |
| `protocol: TCP`    | Use TCP (standard internet protocol)                                     |
| `port: 80`         | **External port** - what users type (http://... uses port 80 by default) |
| `targetPort: 8080` | **Internal port** - where the container is actually listening            |

**Translation happening**:

```
User types: http://your-app.com      (port 80)
                    │
                    ▼
            ┌───────────────┐
            │   Service     │
            │   (port 80)   │
            └───────┬───────┘
                    │ translates to
                    ▼
            ┌───────────────┐
            │   Pod         │
            │   (port 8080) │
            └───────────────┘
```

### The Type (How to Expose It)

| Type           | What It Does                                                   |
| -------------- | -------------------------------------------------------------- |
| `ClusterIP`    | Internal only (other pods can access, not internet)            |
| `NodePort`     | Opens a port on each server                                    |
| `LoadBalancer` | **Creates a cloud load balancer** (AWS ELB) with a public URL! |

```yaml
type: LoadBalancer
```

This tells AWS: "Please create a Load Balancer and give me a public URL!"

**Result**: You get something like `a1b2c3d4.us-east-1.elb.amazonaws.com`

```
┌─────── Internet ───────┐
│                        │
│    User's Browser      │
│          │             │
└──────────┼─────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│  AWS Load Balancer                  │
│  (a1b2c3d4.us-east-1.elb...)       │
│          │                          │
└──────────┼──────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│  Kubernetes Service (port 80)       │
│          │                          │
│          ▼                          │
│  ┌─────────────┐                    │
│  │   Pod       │                    │
│  │  loan-app   │                    │
│  │  port 8080  │                    │
│  └─────────────┘                    │
└─────────────────────────────────────┘
```

---

# Complete Flow Summary 🎯

When a user visits your app:

```
1. User types URL in browser
         │
         ▼
2. AWS Load Balancer receives request (created by type: LoadBalancer)
         │
         ▼
3. Kubernetes Service routes to a pod (using selector: app: loan-app)
         │
         ▼
4. Service translates port 80 → 8080 (port: 80, targetPort: 8080)
         │
         ▼
5. Container receives request and responds
         │
         ▼
6. Kubernetes checks health periodically (readinessProbe)
         │
         ▼
7. If pod dies, Kubernetes restarts it (livenessProbe)
         │
         ▼
8. If traffic increases, increase replicas! (replicas: 1 → 3)
```

---

# Quick Reference Card

| Concept          | What It Does           | Real-World Analogy                         |
| ---------------- | ---------------------- | ------------------------------------------ |
| `Deployment`     | Runs your app          | Hiring an employee                         |
| `replicas`       | Number of copies       | Number of employees doing same job         |
| `Pod`            | One running instance   | One employee                               |
| `Container`      | The app inside the pod | The skills/software the employee has       |
| `image`          | Docker image to run    | The training/software DVD                  |
| `env`            | Settings for the app   | Instructions given to employee             |
| `resources`      | CPU/Memory limits      | Desk space and computer power allocated    |
| `readinessProbe` | "Ready for work?"      | "Can you take customers now?"              |
| `livenessProbe`  | "Still alive?"         | "Are you breathing?"                       |
| `Service`        | Network routing        | Receptionist who knows where everyone sits |
| `selector`       | Which pods to route to | "Find employees with this badge"           |
| `port`           | External port          | Front door number                          |
| `targetPort`     | Internal port          | Office room number                         |
| `LoadBalancer`   | Public URL creation    | Installing a front door to the street      |

---

## 🎓 You Now Understand Kubernetes Deployments!

This knowledge applies to ANY Kubernetes deployment, not just this app. Now you can:

- Read any K8s YAML file and understand it
- Debug deployment issues
- Explain Kubernetes in your LinkedIn post! 🚀
