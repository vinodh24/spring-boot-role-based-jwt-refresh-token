# 🚀 Spring Boot 3 – Role-Based Authentication & Authorization using JWT

This project demonstrates JWT-based authentication with role-based access control (RBAC) using Spring Boot 3, Spring Security 6 and Java 21. It now implements a safer refresh-token flow: short-lived access tokens (JWT) and long-lived refresh tokens persisted server-side and rotated on use.

---

## Quick Tech & Features

- Java 21, Spring Boot 3.1.6, Spring Security 6
- JWT access tokens (short-lived)
- Refresh tokens persisted and rotated (server-side)
- Roles: ADMIN, USER
- BCrypt password hashing
- Swagger UI

---

## ⚙️ Features

✅ User registration (`USER` role)  
✅ Admin auto-registration at startup  
✅ Login endpoint with JWT generation  
✅ Stateless JWT authentication  
✅ Role-based endpoint authorization  
✅ Swagger UI integration  
✅ BCrypt password encryption  
✅ Exception handling for invalid tokens

---

## 🗂️ Project Structure


---

## 🧠 How It Works

### 1️⃣ Admin Auto-Registration
When the application starts, it automatically creates an admin user:

```json
{
  "email": "admin@vcti.io",
  "password": "admin"
}
```
User Registration (I’m Vinodh)

Now I (Vinodh) want to register as a new user.

```Endpoint:
POST /api/auth/register

Request Body:

{
"firstName": "vinodh",
"lastName": "sangavaram",
"email": "vinodh.sangavaram@vcti.io",
"password": "vinodh"
}


Response (after successful registration):

{
"password": "$2a$10$tgvib9v.xV9oUCBsIVwRyu07PuvI6vAYafc852myuz0gD8foq36VO",
"enabled": true,
"username": "vinodh.sangavaram@vcti.io",
"authorities": [
{
"authority": "USER"
}
],
"accountNonLocked": true,
"credentialsNonExpired": true,
"accountNonExpired": true
}

```

This confirms that my user account was created successfully, and my password has been securely encrypted using BCrypt.

🔐 3️⃣ Login to Get JWT Token

Now that my account is registered, I’ll log in with my credentials to receive a JWT Token.

Endpoint:
POST /api/auth/login

Request Body:
```
{
"email": "vinodh.sangavaram@vcti.io",
"password": "vinodh"
}

Response (example):

{
"token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ2aW5vZGguc2FuZ2F2YXJhbUB2Y3RpLmlvIiwicm9sZXMiOlsiVVNFUiJdLCJpYXQiOjE3MzA4NjkzOTgsImV4cCI6MTczMDg3MTk5OH0.zT1WjF8jD2Y3vwRxT9Fr8LPt6m3W4kCCWfO_7vx4MIc"
}
```

Swagger UI

To test easily, open the built-in Swagger UI:

👉 http://localhost:8080/swagger-ui/index.html

## Usage: endpoints, requests and responses

Below are concise, copy-pastable examples that match the project's preferred flow. Preferred approach: store refresh token in a Secure, HttpOnly cookie. Alternative: accept refresh token in request body.

---

### 1) Register user
Endpoint:
POST /api/auth/register

Request:
```json
{
  "firstName": "vinodh",
  "lastName": "sangavaram",
  "email": "vinodh.sangavaram@vcti.io",
  "password": "vinodh"
}
```

Response (201 Created):
```json
{
  "username": "vinodh.sangavaram@vcti.io",
  "enabled": true,
  "authorities": [
    { "authority": "USER" }
  ],
  "accountNonLocked": true,
  "credentialsNonExpired": true,
  "accountNonExpired": true
}
```

---

### 2) Login (issue access + refresh tokens)
Endpoint:
POST /api/auth/login

Request:
```json
{
  "email": "vinodh.sangavaram@vcti.io",
  "password": "vinodh"
}
```

Preferred Response (HttpOnly cookie for refresh token):
- Status: 200 OK
- Body:
```json
{
  "accessToken": "<JWT_ACCESS_TOKEN>",
  "expiresIn": 900
}
```
- Set-Cookie header (example):
Set-Cookie: refreshToken=<REFRESH_TOKEN>; Path=/api/auth/refresh-token; HttpOnly; Secure; SameSite=Strict; Max-Age=604800

Alternative Response (if you return refresh token in body — not recommended):
```json
{
  "accessToken": "<JWT_ACCESS_TOKEN>",
  "expiresIn": 900,
  "refreshToken": "<LONG_LIVED_TOKEN>"
}
```

Notes:
- accessToken is a JWT intended for Authorization: Bearer <token>
- expiresIn is seconds until access token expiry (recommend 5–15 minutes)
- refreshToken is a long random token (UUID stored server-side) — rotate on use

---

### 3) Refresh access token
Endpoint:
POST /api/auth/refresh-token

Preferred: send refresh token via HttpOnly cookie. Server reads cookie, validates, rotates token and returns new access token and a new refresh cookie.

Request (cookie):
Cookie: refreshToken=<OLD_REFRESH_TOKEN>

Request (alternative body):
```json
{
  "refreshToken": "<OLD_REFRESH_TOKEN>"
}
```

Response (200 OK):
- Body:
```json
{
  "accessToken": "<NEW_JWT_ACCESS_TOKEN>",
  "expiresIn": 900
}
```
- Set-Cookie:
Set-Cookie: refreshToken=<ROTATED_REFRESH_TOKEN>; Path=/api/auth/refresh-token; HttpOnly; Secure; SameSite=Strict; Max-Age=604800

Error cases:
- 401 Unauthorized — refresh token invalid or expired
- 403 Forbidden — token reuse detected (optional server policy)

---

### 4) Logout (revoke refresh tokens)
Endpoint:
POST /api/auth/logout

Request:
- Prefer authenticated request (Authorization: Bearer <accessToken>) OR read user id from session context
- Optional: include refresh token cookie or body to target a specific token

Response (200 OK):
- Body:
```json
{ "message": "Logged out successfully" }
```
- Set-Cookie to clear:
Set-Cookie: refreshToken=; Path=/api/auth/refresh-token; HttpOnly; Secure; Max-Age=0

Behavior:
- Server deletes refresh token(s) for the user (or the specific token)
- Access tokens (JWT) remain valid until expiry — rely on short lifetime

---

## Minimal developer guidance — what to change in code

1. AuthController
- Login:
  - Authenticate user.
  - Create access token via JwtService.
  - Create refresh token via RefreshTokenService.createRefreshToken(userId).
  - Return access token in JSON and set refresh token as HttpOnly cookie (preferred).
- Refresh-token endpoint (POST /api/auth/refresh-token):
  - Read refresh token from cookie (preferred) or body.
  - Call RefreshTokenService.verifyAndRotate(oldToken) -> returns new token or throws.
  - Issue new access token and set rotated refresh token cookie.
- Logout:
  - Call RefreshTokenService.deleteByUserId(userId) or delete specific token.
  - Clear refresh cookie.

2. RefreshTokenService (new)
- createRefreshToken(userId): persist token string + expiry.
- verifyAndRotate(token): validate, remove old, create new token (return new token string).
- deleteByUserId(userId): remove tokens when user logs out.

3. RefreshToken entity / DB
- Table: refresh_token
  - id BIGINT PK
  - token VARCHAR UNIQUE
  - user_id FK
  - expiry_date TIMESTAMP
  - created_date TIMESTAMP

4. JwtService
- Continue issuing short-lived JWT access tokens.
- Use existing claims (subject = email, roles).
- Do not use JWT for refresh tokens (prefer opaque random tokens stored server-side).

5. SecurityConfig
- Permit endpoints:
  - /api/auth/login
  - /api/auth/register
  - /api/auth/refresh-token
- Keep stateless session policy.

6. DTOs
- LoginResponse: { accessToken, expiresIn }
- Avoid returning refreshToken in JSON when you set it as HttpOnly cookie.

---

## Checklist
- [ ] Add RefreshToken entity + repo
- [ ] Implement RefreshTokenService (rotation + deletion)
- [ ] Add refresh-token endpoint and update AuthController
- [ ] Issue refresh token as HttpOnly cookie on login and rotate on refresh
- [ ] Revoke refresh tokens on logout
- [ ] Update DB schema/migration

---

## Security best practices (summary)
- Use HttpOnly + Secure cookies for refresh tokens when possible.
- Rotate refresh tokens on each use.
- Log suspicious refresh-token activity and revoke tokens when reuse detected.
- Keep access tokens short-lived.
