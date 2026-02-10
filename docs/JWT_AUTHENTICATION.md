# JWT Authentication & Authorization Implementation

This document describes the JWT-based authentication and authorization system implemented in the JJT Platform.

## Overview

The system uses **JWT (JSON Web Tokens)** for stateless authentication with **Spring Security** on the backend and **Angular Auth Guards** on the frontend.

## Features Implemented

### 1. Backend (Spring Boot)

#### Dependencies Added
- `spring-boot-starter-security` - Spring Security framework
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` - JWT token generation and validation
- `springdoc-openapi-starter-webmvc-ui` - Swagger/OpenAPI documentation

#### Database Schema
- **users** table with fields:
  - `id` (UUID) - Primary key
  - `username` (VARCHAR) - Unique username
  - `password` (VARCHAR) - BCrypt hashed password
  - `email` (VARCHAR) - Unique email
  - `role` (VARCHAR) - User role (ADMIN, SPONSOR)
  - `sponsor_id` (UUID) - Foreign key to sponsors table (for SPONSOR role)
  - `enabled` (BOOLEAN) - Account status
  - `created_at`, `updated_at` (TIMESTAMP) - Audit fields

#### Security Components

**1. JwtTokenProvider** (`config/security/JwtTokenProvider.java`)
- Generates JWT tokens with username, role, and sponsorId
- Validates and parses JWT tokens
- Extracts claims from tokens
- Configurable secret key and expiration time

**2. CustomUserDetailsService** (`config/security/CustomUserDetailsService.java`)
- Implements Spring Security's `UserDetailsService`
- Loads user details from database
- Integrates with Spring Security authentication

**3. JwtAuthenticationFilter** (`config/security/JwtAuthenticationFilter.java`)
- Intercepts all HTTP requests
- Extracts JWT token from Authorization header
- Validates token and sets Security Context
- Supports both custom SecurityContext (for existing AccessGuard) and Spring Security context

**4. SecurityConfig** (`config/security/SecurityConfig.java`)
- Configures Spring Security with JWT authentication
- Defines endpoint access rules:
  - `/api/auth/**` - Public (login, register)
  - `/api/public/**` - Public (sponsorship commitments)
  - `/swagger-ui/**`, `/v3/api-docs/**` - Public (API documentation)
  - `/api/admin/**` - Requires ADMIN, JJT_ADMIN, or ORG_ADMIN role
  - `/api/sponsor/**` - Requires SPONSOR role
- Enables CORS with Angular frontend
- Disables CSRF for stateless API
- Uses BCrypt for password encoding

**5. AuthController** (`api/auth/AuthController.java`)
- `POST /api/auth/login` - User login, returns JWT token
- `POST /api/auth/register` - User registration (admin only)
- `GET /api/auth/me` - Get current user info

**6. UserEntity & Repository**
- JPA entity for user table
- Repository with username and email lookups

#### Swagger/OpenAPI Configuration

**SwaggerConfig** (`config/SwaggerConfig.java`)
- Configures OpenAPI 3.0 documentation
- Adds JWT bearer authentication scheme
- Available at: `http://localhost:8080/swagger-ui.html`

### 2. Frontend (Angular)

#### Services

**AuthService** (`services/auth.service.ts`)
- `login(credentials)` - Authenticate user and store JWT token
- `logout()` - Clear token and user data
- `isAuthenticated()` - Check if user is logged in
- `hasRole(roles)` - Check user roles
- `isAdmin()`, `isSponsor()` - Role helpers
- `currentUser$` - Observable for user state

#### Guards

**AuthGuard** (`guards/auth.guard.ts`)
- Protects routes requiring authentication
- Redirects unauthenticated users to login
- Supports role-based route protection via route data

#### Interceptors

**RoleInterceptor** (`interceptors/role.interceptor.ts`)
- Automatically adds JWT token to Authorization header
- Skips token for public endpoints (`/api/public/`, `/api/auth/`)

#### Components

**LoginComponent** (`pages/login/`)
- Login form with username and password
- Error handling and loading states
- Redirect to return URL after login
- Test credentials display

**SiteHeaderComponent** (updated)
- Shows current user info (username, role)
- Logout button
- Login button for unauthenticated users
- Admin menu item (role-based)

#### Routes (updated)
- `/login` - Login page (public)
- `/children` - Protected with authGuard
- `/children/:childId` - Protected with authGuard
- `/admin` - Protected with authGuard + role check (ADMIN only)

## Configuration

### Backend Configuration (application.yml)

```yaml
jwt:
  secret: ${JWT_SECRET:jjt-platform-secret-key-for-jwt-token-generation-minimum-256-bits-long-for-security}
  expiration: ${JWT_EXPIRATION:86400000}  # 24 hours in milliseconds

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

### Frontend Configuration (environment.ts)

## User Roles

### ADMIN (or JJT_ADMIN, ORG_ADMIN)
- Full access to all admin endpoints
- Can create children, sponsors, sponsorships
- Can record education support and progress updates
- Can view all data

### SPONSOR
- Read-only access to assigned children
- Can view child profiles, ledger entries, progress updates
- Cannot create or modify data

## Default Test Users (Dev/Local Only)

Test users are created automatically via `DataInitializationRunner` when running in dev or local profiles:

### Admin User
- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@jjt.org`
- **Role**: JJT_ADMIN
- Linked to system administration

### Sponsor User
- **Username**: `sponsor`
- **Password**: `sponsor123`
- **Email**: `sponsor@example.org`
- **Role**: SPONSOR
- **Linked to**: First seeded sponsor (ID: `50000000-0000-0000-0000-000000000001`)

### Org Admin User
- **Username**: `orgadmin`
- **Password**: `orgadmin123`
- **Email**: `orgadmin@jjt.org`
- **Role**: ORG_ADMIN

**Note**: These default users are only created in dev/local environments. They will not be created in production.

## API Endpoints

### Authentication Endpoints

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ADMIN",
  "sponsorId": null
}
```

#### Register
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "email": "user@example.com",
  "role": "SPONSOR"
}
```

#### Get Current User
```
GET /api/auth/me
Authorization: Bearer <token>

Response:
{
  "token": "...",
  "username": "admin",
  "role": "ADMIN",
  "sponsorId": null
}
```

### Using JWT Token in API Calls

All protected endpoints require JWT token in Authorization header:

```
GET /api/admin/children
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Swagger Documentation

Access interactive API documentation at:
- **Local**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Using Swagger with JWT

1. Click "Authorize" button (lock icon) at top right
2. Enter: `Bearer <your-jwt-token>`
3. Click "Authorize"
4. All API calls will now include the token

## Security Best Practices Implemented

1. **Password Hashing**: BCrypt with salt
2. **Token Expiration**: 24-hour JWT expiration
3. **Stateless Authentication**: No server-side sessions
4. **CORS Configuration**: Restricted to Angular frontend
5. **Role-Based Access Control**: Method-level security annotations
6. **Input Validation**: Spring validation on DTOs
7. **Error Handling**: Custom authentication entry point
8. **Secure Headers**: Spring Security default headers

## Migration from Header-Based Auth

The old `SimpleSecurityFilter` (header-based) has been replaced with JWT authentication. The custom `SecurityContext` and `AccessGuard` classes are still used for backward compatibility with existing code.

### Changes Required in Existing Code

**None!** The new JWT system maintains compatibility with existing `AccessGuard.requireRole()` calls.

## Testing

### Backend Tests
```bash
mvn test
```

### Frontend Tests
```bash
cd jjt-angular
npm test
```

### Manual Testing

1. **Start Backend**:
   ```bash
   mvn spring-boot:run
   ```

2. **Start Frontend**:
   ```bash
   cd jjt-angular
   npm start
   ```

3. **Login**:
   - Navigate to http://localhost:4200/login
   - Use test credentials (admin/admin123 or sponsor/sponsor123)

4. **Test Protected Routes**:
   - Try accessing /children, /admin
   - Verify role-based access

5. **Test Swagger**:
   - Navigate to http://localhost:8080/swagger-ui.html
   - Login to get token
   - Authorize with token
   - Test API endpoints

## Production Deployment

### Environment Variables

Set these environment variables in production:

```bash
# Backend
JWT_SECRET=<strong-random-secret-at-least-256-bits>
JWT_EXPIRATION=86400000

# Frontend
NG_APP_API_URL=https://your-api-domain.com
```

### Heroku Deployment

```bash
# Set JWT secret
heroku config:set JWT_SECRET="your-production-secret-key" --app jjt-platform

# Deploy
git push heroku main
```

### Firebase Deployment (Frontend)

```bash
cd jjt-angular
npm run build
firebase deploy
```

## Troubleshooting

### Token Expired
- Error: "JWT expired"
- Solution: Login again to get new token

### Unauthorized Access
- Error: 401 Unauthorized
- Solution: Check token is included in Authorization header

### Forbidden
- Error: 403 Forbidden
- Solution: User doesn't have required role for endpoint

### CORS Errors
- Error: CORS policy blocked
- Solution: Update `SecurityConfig.corsConfigurationSource()` with frontend URL

## Future Enhancements

1. **Refresh Tokens**: Implement refresh token rotation
2. **Password Reset**: Email-based password reset flow
3. **Two-Factor Authentication**: Add 2FA support
4. **Account Lockout**: Lock account after failed login attempts
5. **Audit Logging**: Log all authentication events
6. **Social Login**: OAuth2 integration (Google, Facebook)
7. **Role Management UI**: Admin interface for user/role management

## Files Created/Modified

### Backend
- `V8__create_users_table.sql` - Database migration
- `UserEntity.java` - JPA entity
- `UserJpaRepository.java` - Repository
- `JwtTokenProvider.java` - JWT utility
- `CustomUserDetailsService.java` - User details service
- `JwtAuthenticationFilter.java` - JWT filter
- `JwtAuthenticationEntryPoint.java` - Auth entry point
- `SecurityConfig.java` - Security configuration (updated)
- `SwaggerConfig.java` - Swagger configuration
- `AuthController.java` - Authentication endpoints
- `LoginRequest.java`, `LoginResponse.java`, `RegisterRequest.java` - DTOs
- `application.yml` - Added JWT and Swagger config

### Frontend
- `auth.service.ts` - Authentication service
- `auth.guard.ts` - Route guard
- `role.interceptor.ts` - HTTP interceptor (updated)
- `login.component.ts` - Login page
- `login.component.html` - Login template
- `login.component.css` - Login styles
- `site-header.component.ts` - Header with auth UI (updated)
- `app.routes.ts` - Routes with guards (updated)
- `environment.ts`, `environment.prod.ts` - API URL config (updated)

## Support

For issues or questions, contact the JJT Platform team at support@jjt.org
