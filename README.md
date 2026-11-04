# FU Mini Hotel - Assignment 03 SBA301

Bài làm theo đề trong thư mục `docs`: Spring Boot 3 REST API + SQL Server + JWT + React/axios.
Java tổ chức theo 3 tầng; giao diện dùng bảng, form và CSS thuần để dễ học.

## 1. Đọc tài liệu theo thứ tự

1. [Phân tích đề và cách triển khai](docs/PHAN_TICH_DE.md).
2. Hướng dẫn chạy trong README này.
3. [Danh sách API và kịch bản kiểm thử](docs/API_VA_KIEM_THU.md).
4. Đọc code: `entity` → `repository` → `service` → `controller` → các trang React.

## 2. Công cụ cần có

- JDK 21. Kiểm tra: `java -version`. Đặt `JAVA_HOME` trỏ đến thư mục JDK nếu cần.
- Node.js 22.12+ hoặc 20.19+ và npm. Kiểm tra: `node -v`, `npm -v`.
- SQL Server + SQL Server Management Studio khi chạy bản nộp bài.
- IntelliJ IDEA để mở `backend/pom.xml`; VS Code hoặc IntelliJ để mở frontend.
- Maven Wrapper đã có sẵn; không cần cài Maven riêng. Lần đầu cần mạng để tải dependencies.

## 3. Chạy thử nhanh, chưa cần cấu hình SQL Server

Mở terminal thứ nhất tại thư mục gốc:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Mở terminal thứ hai tại thư mục gốc:

```powershell
cd frontend
npm ci
npm run dev
```

Mở **http://localhost:5173**. Backend chạy **http://localhost:8080**.
Không mở `index.html` trực tiếp. Vite chuyển tiếp `/api` sang backend.

`demo` dùng H2 trong RAM để thử chức năng. Tắt backend thì mất dữ liệu demo.
**Khi nộp bài phải dùng SQL Server theo mục 4.**

| Vai trò | Email | Mật khẩu mẫu |
| --- | --- | --- |
| Staff | staff@fuminihotel.com | Staff@123 |
| Customer | customer@example.com | Customer@123 |

Dữ liệu mẫu có 3 loại phòng và 3 phòng. Staff đọc từ `application.properties`; Customer lưu trong database với mật khẩu BCrypt.
`app.seed-data=false` để tắt tự tạo dữ liệu mẫu. Khi bật, dữ liệu thiếu sẽ được bổ sung khi khởi động.

## 4. Chạy đúng đề với SQL Server

### Cách nhanh trên máy Windows hiện tại

Đã xác nhận máy có SQL Server 2022 tại `localhost:1433` và tài khoản Windows hiện tại kết nối được.
Từ thư mục gốc, có thể chạy:

```powershell
.\run-sqlserver.ps1
```

Script dùng Windows Authentication, tạo database nếu chưa có, tải DLL xác thực chính thức của Microsoft vào `.tools/`, build và chạy backend tại cổng 8080.
Script cần Windows x64/JDK x64, `sqlcmd` và `curl`; không cần đặt mật khẩu SQL Server.
Nếu đổi tên database: `.\run-sqlserver.ps1 -Database A3NguyenVanAn_SE18XX`.
Dừng backend đang chạy trước khi dùng script. Frontend vẫn chạy `npm run dev` như mục 3.

### Cấu hình thủ công bằng SQL Server Authentication

1. Mở SSMS, chạy [database/create-database.sql](database/create-database.sql).
2. Bật TCP/IP cho SQL Server trong SQL Server Configuration Manager; kiểm tra đúng cổng.
   Nếu dùng named instance như `SQL2022`, instance có thể dùng cổng động, không mặc định là 1433.
3. Dùng SQL Server Authentication với tài khoản có quyền trên database bài tập.
   Nếu chỉ có Windows Authentication, bật Mixed Mode và cấu hình SQL login phù hợp trong SSMS.
4. Trong terminal backend, điền thông tin kết nối thật:

```powershell
$env:DB_URL='jdbc:sqlserver://localhost:1433;databaseName=A3StudentName_ClassCode;encrypt=true;trustServerCertificate=true'
$env:DB_USERNAME='sa'
$env:DB_PASSWORD='mat-khau-SQL-Server-cua-ban'
.\mvnw.cmd spring-boot:run
```

Profile mặc định là `sqlserver`. JPA tự tạo bảng và quan hệ; script SQL chỉ tạo database.
Không đặt `SPRING_PROFILES_ACTIVE=demo` trong Run Configuration khi chạy SQL Server.
`trustServerCertificate=true` dùng cho SQL Server cục bộ trong bài tập.

Cấu hình nằm ở:

- `backend/src/main/resources/application.properties`: Staff, JWT, cổng và dữ liệu mẫu.
- `backend/src/main/resources/application-sqlserver.properties`: SQL Server.
- `backend/src/main/resources/application-demo.properties`: bản chạy thử H2.

Có thể đổi Staff bằng `STAFF_EMAIL`, `STAFF_PASSWORD`; khóa ký token bằng `JWT_SECRET` (ít nhất 32 byte).
Frontend có `.env.example`; mặc định dùng `/api` qua Vite. Chỉ cần `VITE_API_URL` nếu đổi địa chỉ API.

## 5. Đổi tên trước khi nộp

Đề chưa cung cấp tên sinh viên và mã lớp. `A3StudentName_ClassCode` hiện là tên tạm.

Ví dụ đổi thành `A3NguyenVanAn_SE18XX`:

1. Đổi `artifactId`, `name` trong `backend/pom.xml` và `spring.application.name` trong `application.properties`.
2. Đổi tên database trong `database/create-database.sql`, `application-sqlserver.properties` hoặc `DB_URL`.
3. Frontend: đổi `name` trong `frontend/package.json` thành dạng hợp lệ npm, ví dụ `a3nguyenvanan-se18xx`, rồi chạy `npm install` để cập nhật lockfile.
4. Có thể đổi tên hai thư mục dự án khi đóng gói nộp bài: `backend/A3NguyenVanAn_SE18XX` và `frontend/A3NguyenVanAn_SE18XX`; khi đó mở terminal tại thư mục chứa `pom.xml`/`package.json` tương ứng.

## 6. Cấu trúc

```text
Ass3/
├── backend/
│   ├── pom.xml
│   ├── mvnw, mvnw.cmd, .mvn/       Maven Wrapper
│   └── src/
│       ├── main/java/com/fuminihotel/
│       │   ├── controller/         Nhận HTTP request, gọi service
│       │   ├── service/            Kiểm tra và xử lý nghiệp vụ
│       │   ├── repository/         Truy cập database bằng Spring Data JPA
│       │   ├── entity/             Các bảng và quan hệ JPA
│       │   ├── dto/                Dữ liệu request/response, validation
│       │   ├── config/             Security, CORS, dữ liệu mẫu
│       │   └── exception/          Trả lỗi API
│       ├── main/resources/         Cấu hình profiles
│       └── test/                   Kiểm thử tích hợp
├── frontend/
│   ├── package.json
│   └── src/
│       ├── pages/                  Các màn hình
│       ├── components/             Form, bảng, kiểm tra vai trò dùng chung
│       ├── services/api.js         axios và token
│       ├── utils/format.js         Định dạng tiền, ngày, trạng thái
│       ├── App.jsx                 Menu và routes
│       ├── main.jsx                Điểm khởi động React
│       └── styles.css              CSS cơ bản
├── database/create-database.sql
├── postman/FUMiniHotel.postman_collection.json
└── docs/
```

Không dùng Lombok, Redux, Tailwind, service interface hoặc generic base controller.
DTO dùng `record` của Java: phù hợp dữ liệu truyền nhận, tự có constructor và accessor.
Entity dùng class, constructor rỗng, getter/setter thông thường.

## 7. Build và kiểm thử

```powershell
cd backend
.\mvnw.cmd clean verify
```

Kiểm thử tự động chạy với profile H2 `demo`, độc lập với database SQL Server của bạn.
Có 11 bài kiểm thử tích hợp, gồm cả hai request đặt cùng phòng đồng thời.
Ngoài ra, collection Postman đã được chạy qua Newman trên SQL Server thật và các luồng chính đã được kiểm tra bằng trình duyệt ở kích thước desktop/mobile.

```powershell
cd frontend
npm ci
npm run build
```

File JAR ở `backend/target/`; frontend build ở `frontend/dist/`.
Để chạy JAR thử nhanh:

```powershell
java -jar backend/target/A3StudentName_ClassCode-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

`npm run dev` dùng proxy như trên. `npm run preview` không có proxy `/api` của dev server;
nếu dùng preview, build frontend với `VITE_API_URL=http://localhost:8080/api` và đặt `FRONTEND_URL` cho backend đúng origin preview.

## 8. Lỗi thường gặp

| Lỗi | Cách xử lý |
| --- | --- |
| `JAVA_HOME` chưa đúng | Trỏ đến thư mục JDK 21, mở terminal mới |
| Cổng 8080 hoặc 5173 bị chiếm | Dừng ứng dụng đang dùng cổng hoặc đổi cổng cùng proxy/CORS |
| Connection refused / TCP/IP connection failed | Kiểm tra SQL Server đang chạy, TCP/IP và cổng thật của instance |
| Login failed for user | Kiểm tra SQL login, mật khẩu, Mixed Mode và quyền database |
| Cannot open database | Chạy script tạo database; kiểm tra tên trong `DB_URL` |
| Không kết nối được máy chủ trên FE | Chạy backend trước, kiểm tra cổng 8080 và Vite proxy |
| 401/403 | Đăng nhập lại hoặc dùng tài khoản đúng vai trò |
| Không đặt được phòng | Kiểm tra ngày, trạng thái phòng và đơn trùng lịch |

## 9. Nguồn đối chiếu kỹ thuật

- [Spring Boot 3.5: Java và Maven được hỗ trợ](https://docs.spring.io/spring-boot/3.5/system-requirements.html).
- [Spring Security 6.5: xác thực Bearer JWT](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html).
- [Microsoft JDBC: Windows Authentication và DLL xác thực](https://learn.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url).

Phân tích trong `docs/PHAN_TICH_DE.md` phân biệt yêu cầu của đề và các quy ước nghiệp vụ tự chọn.
