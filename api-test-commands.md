# API Test Commands

## Authentication Endpoints

### 1. User Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "email": "testuser1@example.com",
    "password": "TestPassword123!"
  }'
```

### 2. User Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser1@example.com",
    "password": "TestPassword123!"
  }'
```

### 3. Refresh Token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

### 4. Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

### 5. Verify Email
```bash
curl -X GET "http://localhost:8080/api/auth/verify?token=YOUR_TOKEN_HERE"
```

### 6. Request Password Reset (Forgot Password)
```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser1@example.com"
  }'
```

### 7. Reset Password (with token from email)
```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_RESET_TOKEN_HERE",
    "newPassword": "NewPassword123!"
  }'
```

### 8. Change Password (Authenticated users only)
```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -d '{
    "oldPassword": "TestPassword123!",
    "newPassword": "TestPassword1234!"
  }'
```

### 9. Resend Verification Email
```bash
curl -X POST http://localhost:8080/api/auth/resend-verification \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser1@example.com"
  }'
```

## Test Different Scenarios

### Registration - Invalid Email
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "email": "invalid-email",
    "password": "TestPassword123!"
  }'
```

### Registration - Weak Password
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser3",
    "email": "testuser3@example.com",
    "password": "weak"
  }'
```

### Login - Wrong Password
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "WrongPassword123!"
  }'
```

### Login - Non-existent User
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@example.com",
    "password": "TestPassword123!"
  }'
```

### Change Password - Wrong Old Password
```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -d '{
    "oldPassword": "WrongOldPassword123!",
    "newPassword": "NewSecurePassword123!"
  }'
```

### Change Password - Without Authentication
```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "TestPassword123!",
    "newPassword": "NewSecurePassword123!"
  }'
```

## Tips
- Add `-v` flag for verbose output to see headers
- Add `-i` flag to include HTTP response headers in output
- Use `| jq` at the end to pretty-print JSON responses (requires jq installed)

Example with verbose and pretty print:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "TestPassword123!"
  }' -v | jq
```