# JWT Authentication - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Step 1: Start the Backend (1 min)

```bash
cd C:\Users\muhassan\springprojects\JJT
mvn spring-boot:run
```

Wait for the message: `Started JjtPlatformApplication in X seconds`

### Step 2: Verify Swagger (30 sec)

Open your browser: http://localhost:8080/swagger-ui.html

You should see the API documentation with:
- Authentication section
- Admin section
- Public section

### Step 3: Test Login API (1 min)

In Swagger:
1. Go to **Authentication** → **POST /api/auth/login**
2. Click "Try it out"
3. Enter:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
4. Click "Execute"
5. Copy the `token` from the response

### Step 4: Authorize Swagger (30 sec)

1. Click the **Authorize** button (🔒 icon) at the top
2. Enter: `Bearer YOUR_TOKEN_HERE`
3. Click "Authorize"
4. Click "Close"

Now all API calls will include your JWT token!

### Step 5: Test Protected Endpoint (1 min)

1. Go to **Admin** → **GET /api/admin/sponsorships**
2. Click "Try it out"
3. Click "Execute"
4. You should see successful response (200 OK)

### Step 6: Start Frontend (1 min)

Open a new terminal:
```bash
cd C:\Users\muhassan\springprojects\JJT\jjt-angular
npm start
```

Wait for: `Compiled successfully`

### Step 7: Test Frontend Login (30 sec)

1. Open browser: http://localhost:4200/login
2. Login with:
   - **Username**: admin
   - **Password**: admin123
3. You should be redirected to home page
4. Header should show: "admin (ADMIN)" with a Logout button

## ✅ Success Checklist

- [ ] Backend running on port 8080
- [ ] Swagger UI accessible
- [ ] Login API returns JWT token
- [ ] Authorized requests work in Swagger
- [ ] Frontend running on port 4200
- [ ] Login redirects to home page
- [ ] User info shown in header

## 🎯 Test Both User Roles

### Admin User
- Username: `admin`
- Password: `admin123`
- Can access: All endpoints, /admin pages

### Sponsor User
- Username: `sponsor`
- Password: `sponsor123`
- Can access: Read-only sponsor data

## 📖 Full Documentation

See `IMPLEMENTATION_SUMMARY.md` and `docs/JWT_AUTHENTICATION.md` for complete details.

## 🆘 Common Issues

### Port 8080 already in use
```bash
# Windows: Find and kill process
netstat -ano | findstr :8080
taskkill /PID <PID_NUMBER> /F
```

### Backend won't start
```bash
# Clean and rebuild
mvn clean install
mvn spring-boot:run
```

### Frontend errors
```bash
# Reinstall dependencies
cd jjt-angular
rm -rf node_modules package-lock.json
npm install
npm start
```

### Token not working
- Make sure you included "Bearer " prefix
- Check token hasn't expired (24 hours)
- Verify you're logged in correctly

## 🎉 You're Ready!

Your JJT Platform now has:
✅ JWT authentication
✅ Role-based authorization
✅ Swagger API documentation
✅ Beautiful login UI
✅ Secure token management

Happy coding! 🚀
