# Tribe Backend

Spring Boot + JPA application for social user profiles with REST APIs, Postgres persistence, and automated container delivery to Amazon ECR.

## Project layout

- `spring-webapi/` - Spring Boot source, tests, Maven build, and Dockerfile.
- `.github/workflows/deploy.yml` - GitHub Actions pipeline that builds/tests and pushes the container image to ECR.

## Local development

```bash
cd spring-webapi
mvn test               # run test suite
mvn spring-boot:run    # start the app on http://localhost:8080
```

Build the Docker image locally and run it:

```bash
cd spring-webapi
mvn -DskipTests package
docker build -t tribe-backend:dev .
docker run --rm -p 8080:8080 --name tribe-backend tribe-backend:dev
```

Set `DB_USERNAME` / `DB_PASSWORD` environment variables to point at your Postgres instance before starting.

## Continuous deployment to Amazon ECR

The workflow at `.github/workflows/deploy.yml` runs on pushes to `main` (and on manual triggers). It:

1. Checks out the repository and sets up Java 17.
2. Runs `mvn test` inside `spring-webapi`.
3. Authenticates to AWS using GitHub OIDC (`aws-actions/configure-aws-credentials`).
4. Builds and pushes the Docker image defined in `spring-webapi/Dockerfile` to your Amazon ECR repository, tagging it with the commit SHA and (for `main`) `latest`.

### Required AWS setup

1. Create an ECR repository named `tribe-backend` (or adjust `ECR_REPOSITORY` in the workflow).
2. Provision an IAM role that GitHub Actions can assume. Grant it permissions for ECR push operations (`ecr:GetAuthorizationToken`, `ecr:InitiateLayerUpload`, `ecr:UploadLayerPart`, `ecr:CompleteLayerUpload`, `ecr:PutImage`, and `sts:AssumeRole`).
3. Add these secrets to your GitHub repository:
   - `AWS_ROLE_TO_ASSUME` - ARN of the IAM role.
   - `AWS_REGION` - AWS region of your ECR repository (e.g., `us-east-1`).

> Tip: If you prefer long-lived access keys, replace the `role-to-assume` input with `aws-access-key-id` / `aws-secret-access-key`, but OIDC short-lived credentials are more secure.

## Git repository bootstrap

To publish this code to a remote repository named `tribe-backend`:

```bash
cd c:/Users/91751/Desktop/study_and_development/Tribe
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin git@github.com:<your-account>/tribe-backend.git
git push -u origin main
```

Replace `<your-account>` with your GitHub username or organization. After the initial push (and once secrets are configured), the CI/CD workflow will build and push the container image to ECR automatically.
