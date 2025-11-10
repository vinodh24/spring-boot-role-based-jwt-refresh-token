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