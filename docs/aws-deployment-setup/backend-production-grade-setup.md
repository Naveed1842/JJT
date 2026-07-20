# AWS Deployment Playbook

## JJT Platform — Spring Boot + AWS ECS Fargate

### Version 1.0

**Author:** Muhammad Naveed
**Project:** JJT Platform
**Cloud Provider:** Amazon Web Services (AWS)
**Deployment Model:** Containerized Microservice using Amazon ECS Fargate

---

# Table of Contents

1. Introduction
2. Solution Architecture
3. AWS Services Used
4. Deployment Journey
5. Step-by-Step Deployment
6. Problems Faced & Solutions
7. Verification Checklist
8. Final Architecture
9. Production Readiness
10. Remaining Work
11. Lessons Learned
12. Interview Questions

---

# 1. Introduction

The objective of this deployment was to migrate the JJT Spring Boot backend from a local development environment to a production-ready AWS infrastructure.

The deployment was designed to achieve:

* Scalability
* High Availability
* Load Balancing
* Centralized Logging
* Managed Infrastructure
* Zero-Downtime Deployment
* Production Ready Architecture

---

# 2. Initial Architecture

```
Developer PC
        │
        ▼
 Spring Boot
        │
        ▼
 PostgreSQL
```

Problems

* Single machine deployment
* No scalability
* No monitoring
* No centralized logs
* Manual deployment
* Single point of failure

---

# 3. Target AWS Architecture

```
                 GitHub
                    │
                    ▼
             Docker Build
                    │
                    ▼
              Amazon ECR
                    │
                    ▼
          ECS Task Definition
                    │
                    ▼
             ECS Service
                    │
                    ▼
            AWS Fargate Task
                    │
                    ▼
      Application Load Balancer
                    │
                    ▼
          Spring Boot Backend
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
 Amazon RDS PostgreSQL   CloudWatch Logs
```

---

# 4. AWS Services Used

| Service                   | Purpose                      |
| ------------------------- | ---------------------------- |
| Amazon ECS                | Container orchestration      |
| AWS Fargate               | Serverless container runtime |
| Amazon ECR                | Docker image repository      |
| Amazon RDS                | PostgreSQL database          |
| Application Load Balancer | Traffic distribution         |
| CloudWatch Logs           | Centralized logging          |
| IAM                       | Permissions                  |
| Security Groups           | Firewall                     |
| VPC                       | Networking                   |
| Target Group              | Health monitoring            |

---

# 5. Deployment Journey

---

## Phase 1 — Prepare Spring Boot

Completed

* Dockerfile
* Environment Variables
* Port 8080
* Spring Boot Production Build

Output

```
Docker Image
```

---

## Phase 2 — Docker

Built image

```
docker build
```

Verified locally.

---

## Phase 3 — Amazon ECR

Created repository

```
jjt-backend
```

Logged into ECR

Tagged image

Pushed

```
1.0
```

Output

Docker image successfully stored in AWS.

---

## Phase 4 — Amazon RDS

Created PostgreSQL instance.

Configured

```
SPRING_DATASOURCE_URL

SPRING_DATASOURCE_USERNAME

SPRING_DATASOURCE_PASSWORD
```

Verified

Application connected successfully.

---

## Phase 5 — CloudWatch Logs

Created

```
jjt-backend
```

Configured ECS Task

```
awslogs
```

Verified

Application logs visible.

---

## Phase 6 — IAM

Created

```
ecsTaskExecutionRole
```

Permissions

* Pull image from ECR
* Push logs to CloudWatch

---

## Phase 7 — Task Definition

Created

```
jjt-backend:2
```

Configuration

* CPU
* Memory
* Environment Variables
* Port Mapping
* CloudWatch Logging

---

## Phase 8 — ECS Cluster

Created

```
jjt-cluster
```

Launch Type

```
FARGATE
```

---

## Phase 9 — ECS Service

Created

```
jjt-backend-service
```

Desired Tasks

```
1
```

Task started successfully.

---

## Phase 10 — Backend Verification

Verified CloudWatch logs.

Confirmed

```
Spring Boot Started
```

Confirmed

```
Database Connected
```

Confirmed

```
Flyway Migration Successful
```

---

## Phase 11 — Public IP Testing

Assigned Public IP.

Verified

```
curl http://PUBLIC-IP:8080
```

Response

```
401 Unauthorized
```

Meaning

Application running correctly.

---

## Phase 12 — Application Load Balancer

Created

```
jjt-alb
```

Created Security Group

Opened

```
80
443
```

---

## Phase 13 — Target Group

Created

```
jjt-backend-tg
```

Port

```
8080
```

Health Check

Initially

```
/actuator/health
```

---

## Phase 14 — First Issue

Problem

Target remained

```
Unhealthy
```

Health Check

```
503
```

Investigation

```
/actuator/health
```

returned

```
DOWN
```

while

```
/actuator/health/readiness
```

returned

```
UP
```

Solution

Changed Target Group Health Check

```
/actuator/health/readiness
```

Result

```
Healthy
```

---

## Phase 15 — Listener

Created HTTP Listener

Port

```
80
```

Forwarded traffic

```
Target Group
```

---

## Phase 16 — Backend Verification Through ALB

Verified

```
http://ALB-DNS/
```

Returned

```
401 Unauthorized
```

Expected.

Verified

```
http://ALB-DNS/actuator/health/readiness
```

Returned

```
UP
```

Backend successfully reachable through ALB.

---

## Phase 17 — ECS Service Integration

Initially

Target was manually registered.

Updated ECS Service

```
update-service
```

Now ECS automatically

* Registers tasks
* Deregisters tasks
* Handles deployments
* Performs health checks

Deployment completed.

---

# 6. Problems Faced

## Git Bash Path Conversion

Problem

```
/actuator/health
```

became

```
C:\Users...
```

Solution

```
MSYS_NO_PATHCONV=1
```

---

## CloudWatch Logs Missing

Cause

Incorrect Task Definition.

Solution

Configured

```
awslogs
```

---

## Health Check Failure

Problem

```
503
```

Root Cause

Wrong endpoint.

Solution

```
/actuator/health/readiness
```

---

## Security Groups

Forgot Region parameter.

Resolved.

---

## Public IP

Initially unavailable.

Enabled

```
assignPublicIp
```

---

# 7. Deployment Verification Checklist

| Item            | Status |
| --------------- | ------ |
| Docker Build    | ✅      |
| Image Push      | ✅      |
| ECR             | ✅      |
| Task Definition | ✅      |
| ECS Cluster     | ✅      |
| ECS Service     | ✅      |
| CloudWatch      | ✅      |
| IAM             | ✅      |
| PostgreSQL      | ✅      |
| Spring Boot     | ✅      |
| Public IP       | ✅      |
| Load Balancer   | ✅      |
| Listener        | ✅      |
| Target Group    | ✅      |
| Health Check    | ✅      |
| Backend API     | ✅      |

---

# 8. Final Production Architecture

```
                    GitHub
                       │
                       ▼
               Docker Image
                       │
                       ▼
                 Amazon ECR
                       │
                       ▼
             ECS Task Definition
                       │
                       ▼
                 ECS Service
                       │
                       ▼
              AWS Fargate Task
                       │
                       ▼
         Application Load Balancer
                       │
                       ▼
              Spring Boot Backend
                 │             │
                 ▼             ▼
          Amazon RDS      CloudWatch
           PostgreSQL        Logs
```

---

# 9. Production Readiness Score

| Feature                  | Status |
| ------------------------ | ------ |
| Containerized            | ✅      |
| Load Balanced            | ✅      |
| Managed Database         | ✅      |
| Central Logs             | ✅      |
| Rolling Deployment       | ✅      |
| Health Checks            | ✅      |
| High Availability        | ✅      |
| Auto Registration        | ✅      |
| Zero Downtime Deployment | ✅      |

Overall

```
95% Production Ready
```

---

# 10. Remaining Work (Phase 2)

## Security

Move secrets into

```
AWS Secrets Manager
```

Remove passwords from ECS Task Definition.

---

## HTTPS

* AWS ACM Certificate
* HTTPS Listener
* HTTP Redirect

---

## Route53

Configure

```
api.jjt.org
```

---

## CI/CD

GitHub Actions

Pipeline

```
GitHub

↓

Build

↓

Docker

↓

ECR

↓

ECS Deployment
```

---

## Monitoring

CloudWatch Alarms

SNS Notifications

---

## Auto Scaling

Scale ECS Tasks automatically.

---

# 11. Lessons Learned

* Docker image lifecycle.
* ECS vs Fargate.
* Task Definition vs Service.
* ALB Health Checks.
* Spring Boot Readiness Probe.
* CloudWatch logging.
* IAM roles.
* Security Groups.
* Rolling deployments.
* Git Bash path conversion issues.
* Production deployment best practices.

---

# 12. Interview Questions

### Why ECS instead of EC2?

Fargate removes server management and provides serverless container execution.

---

### Why use ALB?

To distribute traffic, perform health checks, and enable zero-downtime deployments.

---

### Difference between Health and Readiness?

* **Health (`/actuator/health`)**: Overall application health.
* **Readiness (`/actuator/health/readiness`)**: Whether the application is ready to receive traffic. This is the recommended endpoint for load balancers.

---

### Why ECR?

Secure private Docker registry tightly integrated with ECS.

---

### Why CloudWatch?

Centralized logs, monitoring, and troubleshooting.

---

### Why Fargate?

No server management, automatic scaling, pay-per-use, and improved security.

---

### Why RDS?

Managed PostgreSQL with automated backups, high availability, and operational simplicity.

---

# Conclusion

The JJT backend has been successfully deployed on AWS using a modern containerized architecture based on **Amazon ECS Fargate**. The deployment includes centralized logging, load balancing, managed database connectivity, health monitoring, and rolling deployment support.

The infrastructure is **production-ready** for backend services. The remaining work focuses on operational excellence and security, including HTTPS, Secrets Manager, CI/CD automation, monitoring, and custom domain configuration.

This playbook can serve as a reusable reference for future Spring Boot deployments and as supporting material for technical interviews involving AWS, Docker, ECS, and cloud-native application deployment.
