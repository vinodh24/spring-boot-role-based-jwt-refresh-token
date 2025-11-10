# 🚀 Spring Boot 3 – Role-Based Authentication & Authorization using JWT

This project demonstrates **JWT-based authentication** and **role-based access control (RBAC)** using **Spring Boot 3**, **Spring Security 6**, and **Java 21**.  
It supports two roles: `ADMIN` and `USER`, and secures REST APIs accordingly.

---

## 🧩 Tech Stack

- **Java 21**
- **Spring Boot 3.1.6**
- **Spring Security 6**
- **Spring Data JPA (Hibernate)**
- **MySQL Database**
- **JWT (io.jsonwebtoken 0.11.5)**
- **SpringDoc OpenAPI (Swagger UI)**
- **Maven**

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

## 🔁 Updated Refresh-Token Flow (important)

This project now follows a safer refresh-token approach. Key points:

- We issue two tokens on successful authentication:
  - Access Token: short-lived JWT (used for Authorization header).
  - Refresh Token: long-lived token persisted in the database and tied to a user.
- Refresh tokens are rotated: when a refresh is used, issue a new refresh token and invalidate the previous one.
- Refresh tokens should be stored securely (recommendation: HttpOnly, Secure cookie). If you accept them in the JSON body, validate and rotate them the same way.
- Provide explicit logout endpoint to delete the refresh token from the database.

Why the change?
- Prevents long-lived JWT misuse.
- Enables server-side revocation.
- Supports refresh token rotation to mitigate stolen token replay.

### Endpoints (recommended)

- POST /api/auth/login
  - Request: { "email": "...", "password": "..." }
  - Response (recommendation):
    - Body: { "accessToken": "<jwt>", "expiresIn": 900 }  // short expiry in seconds
    - HttpOnly cookie: refreshToken=<long-lived-token>; Path=/api/auth/refresh-token; Secure; HttpOnly; SameSite=Strict

- POST /api/auth/refresh-token
  - Accepts refresh token from HttpOnly cookie or in request body: { "refreshToken": "..." }
  - Validates server-side, rotates refresh token, returns:
    - Body: { "accessToken": "<new-jwt>", "expiresIn": 900 }
    - Set-Cookie: new refresh token (HttpOnly, Secure)

- POST /api/auth/logout
  - Invalidates/deletes refresh token for the user (server-side).
  - Clears the refresh-token cookie.

### Example: Refresh request (preferred via cookie)
Request:
- POST /api/auth/refresh-token
- Cookie: refreshToken=<token>

Response:
- 200 OK
- JSON: { "accessToken": "<new-access-token>", "expiresIn": 900 }
- Cookie: refreshToken=<rotated-token>; HttpOnly; Secure

### Developer notes — minimal controller/service edits

To implement this with minimal invasive changes, edit the following components:

1. AuthController (or equivalent)
   - Login:
     - After successful authentication, call RefreshTokenService.createRefreshToken(userId) and set refresh token either in an HttpOnly cookie or return it in the response body (HttpOnly cookie preferred).
     - Return access token in the response body.
   - Refresh-token endpoint:
     - New endpoint: POST /api/auth/refresh-token
     - Read refresh token (cookie preferred). If in body, accept { "refreshToken": "..." }.
     - Call RefreshTokenService.verifyAndRotate(refreshToken) which:
       - Finds refresh token entity, verifies not expired, deletes old token, creates and returns a new refresh token.
     - Generate a new access token (JwtService.createAccessToken(user)).
     - Return new access token and set rotated refresh token cookie.
   - Logout:
     - Delete refresh tokens for the user with RefreshTokenService.deleteByUserId(userId).
     - Clear cookie.

2. RefreshTokenService (new/modify)
   - Methods:
     - createRefreshToken(userId): persist token with expiry and return token string.
     - verifyAndRotate(token): validate, remove old, create new (returns new token).
     - deleteByUserId(userId): remove tokens on logout.
   - Store tokens in a refresh_token table with columns: id, user_id, token, expiry_date, created_date.

3. JwtService / Token Utility
   - Keep generating short-lived access tokens.
   - You may reuse existing JWT methods; refresh tokens SHOULD be random UUIDs stored server-side (not necessarily JWTs).

4. Security configuration
   - Permit /api/auth/refresh-token and /api/auth/login and /api/auth/register.
   - Keep stateless session management (SessionCreationPolicy.STATELESS).
   - No need to change access token filters; they remain the same.

5. DB migration
   - Add table refresh_token (id BIGINT PK, token VARCHAR unique, user_id FK, expiry TIMESTAMP).

6. DTOs
   - Adjust LoginResponse to include accessToken and expiresIn (and do not expose refresh token in JSON when you set it as HttpOnly cookie).

### Checklist before merging
- [ ] Implement RefreshToken entity + repository.
- [ ] Implement RefreshTokenService with rotation and deletion.
- [ ] Add refresh-token endpoint to AuthController.
- [ ] Update login flow to issue refresh token cookie.
- [ ] Update logout to delete server-side refresh tokens.
- [ ] Update SecurityConfig to allow refresh endpoint.
- [ ] Add DB migration or JPA entity for refresh_token.
- [ ] Update README examples/Swagger docs if you expose refresh-token endpoints.

### Security best practices
- Use HttpOnly, Secure cookies for refresh tokens when possible.
- Rotate refresh tokens on use.
- Keep access token lifetime short (e.g., 5–15 minutes).
- Keep refresh token lifetime longer but limited (e.g., days/weeks).
- Log and monitor refresh-token usage; revoke suspicious tokens.
