# API và kiểm thử

Base URL: `http://localhost:8080/api`.
JSON dùng tên thuộc tính `camelCase`. Ngày: `yyyy-MM-dd`. Tiền: số VND.
Request cần đăng nhập phải có `Authorization: Bearer <token>`.

## 1. API công khai

| Method | Đường dẫn | Chức năng |
| --- | --- | --- |
| POST | `/auth/login` | Đăng nhập Staff/Customer, trả token và vai trò |
| POST | `/auth/register` | Đăng ký Customer |
| GET | `/rooms?keyword=101` | Tìm phòng đang hoạt động theo số phòng |
| GET | `/rooms?startDate=...&endDate=...` | Tìm phòng trống theo thời gian |
| GET | `/rooms/{id}` | Xem phòng đang hoạt động |
| GET | `/room-types` | Danh mục loại phòng |

Đăng nhập:

```json
{ "email": "staff@fuminihotel.com", "password": "Staff@123" }
```

Response:

```json
{ "token": "...", "role": "STAFF", "customerId": null, "fullName": "Staff" }
```

Đăng ký / thêm khách / sửa khách / sửa hồ sơ dùng:

```json
{
  "customerFullName": "Nguyễn Văn An",
  "telephone": "0901234567",
  "emailAddress": "an@example.com",
  "customerBirthday": "2000-01-01",
  "customerStatus": 1,
  "password": "Customer@123"
}
```

Khi sửa, password `""` hoặc null nghĩa là giữ nguyên. Khi đăng ký/thêm mới bắt buộc mật khẩu tối thiểu 8 ký tự, tối đa 72 byte.
Customer không thể thay đổi trạng thái của bản thân; đăng ký luôn tạo tài khoản hoạt động.

## 2. API Customer

| Method | Đường dẫn | Chức năng |
| --- | --- | --- |
| GET | `/customer/profile` | Lấy hồ sơ bản thân |
| PUT | `/customer/profile` | Sửa hồ sơ bản thân |
| GET | `/customer/bookings` | Lịch sử đơn của bản thân |
| GET | `/customer/bookings/{id}` | Chi tiết một đơn của bản thân |
| POST | `/customer/bookings` | Đặt một hoặc nhiều phòng |

Ví dụ đặt nhiều phòng (thay ngày bằng ngày tương lai):

```json
{
  "rooms": [
    { "roomId": 1, "startDate": "2027-01-10", "endDate": "2027-01-12" },
    { "roomId": 2, "startDate": "2027-01-10", "endDate": "2027-01-12" }
  ]
}
```

Không truyền `totalPrice`, `actualPrice` hay `bookingStatus`: server tự tính và gán.
`customerId` trong request Customer được bỏ qua và thay bằng ID trong JWT.

## 3. API Staff

| Method | Đường dẫn | Chức năng |
| --- | --- | --- |
| GET | `/staff/customers?keyword=an` | Danh sách, tìm tên/email |
| GET | `/staff/customers/{id}` | Chi tiết khách |
| POST | `/staff/customers` | Thêm khách |
| PUT | `/staff/customers/{id}` | Sửa khách |
| DELETE | `/staff/customers/{id}` | Xóa hoặc khóa khách có lịch sử |
| GET | `/staff/rooms?keyword=101` | Danh sách cả phòng ngừng hoạt động |
| POST | `/staff/rooms` | Thêm phòng |
| PUT | `/staff/rooms/{id}` | Sửa phòng |
| DELETE | `/staff/rooms/{id}` | Xóa thật nếu chưa có đơn; còn lại chuyển trạng thái |
| GET | `/staff/bookings` | Tất cả đơn, kèm chi tiết |
| GET | `/staff/bookings/{id}` | Chi tiết đơn |
| POST | `/staff/bookings` | Tạo đơn tại quầy |
| PUT | `/staff/bookings/{id}` | Sửa khách, phòng và ngày của đơn đang xác nhận |
| PATCH | `/staff/bookings/{id}/status` | Hủy/hoàn tất đơn |
| DELETE | `/staff/bookings/{id}` | Xóa đơn đã hủy và chi tiết của đơn |

Thêm/sửa phòng:

```json
{
  "roomNumber": "201",
  "roomDetailDescription": "Phòng có điều hòa",
  "roomMaxCapacity": 2,
  "roomTypeId": 2,
  "roomStatus": 1,
  "roomPricePerDay": 450000
}
```

Staff tạo/sửa đơn dùng body đặt phòng ở mục 2, thêm `"customerId": 1`.
Sửa đơn là thay thế toàn bộ danh sách chi tiết bằng danh sách gửi lên, ít nhất một phòng.
Giữ nguyên phòng thì giữ giá mỗi đêm đã chốt trước đó. Thêm phòng mới thì lấy giá hiện tại.

Đổi trạng thái:

```json
{ "bookingStatus": "CANCELLED" }
```

`COMPLETED` chỉ được dùng từ ngày trả phòng cuối cùng trở đi.
Không mở lại đơn đã hủy/hoàn tất. Đây là quy ước bổ sung của bài làm, xem tài liệu phân tích.

## 4. Validation và HTTP status

| Dữ liệu | Kiểm tra |
| --- | --- |
| Họ tên | Không trống, tối đa 100 ký tự |
| Điện thoại | 8-20 ký tự: số, +, dấu cách, dấu ngoặc, gạch nối |
| Email | Đúng dạng, tối đa 100, không trùng, không dùng email Staff |
| Ngày sinh | Bắt buộc, trước hôm nay |
| Mật khẩu | Tối thiểu 8 ký tự, tối đa 72 byte UTF-8 |
| Số phòng | Không trống, tối đa 20, không trùng |
| Mô tả | Tối đa 500 ký tự |
| Sức chứa | Số nguyên 1-100 |
| Giá phòng | Dương, tối đa 16 chữ số phần nguyên và 2 số thập phân |
| Loại phòng | ID hợp lệ và tồn tại |
| Trạng thái Customer/Room | Số nguyên 0 hoặc 1 |
| Ngày nhận/trả | Ngày nhận không trong quá khứ; trả sau nhận |
| Danh sách phòng | 1-20 phòng, không lặp ID, đang hoạt động, không trùng lịch |

- `200`: đọc/sửa thành công.
- `201`: tạo thành công.
- `204`: xóa không có response body.
- `400`: sai dữ liệu, kiểu dữ liệu, ngày hoặc quy tắc nghiệp vụ.
- `401`: chưa có token, token sai/hết hạn hoặc tài khoản đã bị khóa.
- `403`: sai vai trò.
- `404`: không tìm thấy dữ liệu; gồm cả truy cập đơn của khách khác.
- `409`: email/số phòng trùng, phòng bị đặt trùng lịch hoặc chuyển trạng thái không hợp lệ.

## 5. Dùng Postman

Import [FUMiniHotel.postman_collection.json](../postman/FUMiniHotel.postman_collection.json).
Chạy backend demo mới khởi động rồi chạy các folder theo thứ tự trong Collection Runner.
Collection tự tạo ngày trong tương lai, tự lấy token, ID loại phòng và dữ liệu vừa tạo.
Các request có assertion HTTP status. Kiểm thử phân quyền có kết quả 401/403 là **đúng mong đợi**.

Collection sử dụng dữ liệu mới tạo và xóa lại cuối luồng CRUD; đơn khách tạo trong folder Customer được hủy/xóa trong folder Staff Bookings.
Collection chạy lại sẽ tạo email và số phòng mới bằng timestamp. Cần 2 phòng hoạt động từ seed để chạy kịch bản đặt nhiều phòng.
Nếu đã thay email/mật khẩu Staff, cập nhật collection variables `staffEmail`, `staffPassword`.

## 6. Kiểm thử tự động

File: `backend/src/test/java/com/fuminihotel/HotelApiIntegrationTest.java`.

| Bài kiểm thử | Điều chứng minh |
| --- | --- |
| Public rooms và role boundaries | Xem phòng không token; Staff/Customer không dùng lẫn quyền |
| Registration | Validate email, chặn trùng, lưu BCrypt, không trả password |
| Multi-room booking | Tính đúng tổng, lấy chủ đơn từ JWT, không xem đơn khách khác |
| Overlap và rollback | Không lưu nửa đơn; ngày trả phòng được dùng làm ngày nhận mới |
| Concurrent booking | Hai request cùng phòng: đúng một 201 và một 409 |
| Delete room | Soft delete phòng có lịch sử, hard delete phòng chưa dùng |
| Booking management | Thêm/xóa chi tiết, hủy, xóa đơn, chặn hoàn tất sớm |
| Disabled customer | Token cũ bị từ chối, lịch sử không mất |
| Profile ownership | Giữ trạng thái, không sửa khách khác, mật khẩu trống giữ nguyên |
| Bad dates / duplicate room | Từ chối dữ liệu lỗi, không có đơn thừa |
| Staff room CRUD | Tạo/sửa/tìm phòng, chặn sức chứa không hợp lệ |

Lệnh: `cd backend` rồi `.\mvnw.cmd clean verify`.
Các bài kiểm thử dùng H2, không kết nối SQL Server và không sửa database SQL Server thật.

Kết quả đã kiểm tra trong workspace:

- Maven `clean verify`: 11 tests, 0 failures, 0 errors.
- `npm run build`: thành công.
- Collection chạy qua Newman trên SQL Server 2022: 36 requests, 37 assertions, không có lỗi.
- Trình duyệt Edge: xem phòng, đăng ký, đăng nhập, đặt hai phòng, sửa hồ sơ, Staff sửa đơn, CRUD phòng và soft delete.
- Layout được xem ở desktop 1366px và mobile 390px; bảng trên mobile cho phép cuộn ngang.

## 7. Kịch bản demo giao diện

1. Chưa đăng nhập: mở `/`, tìm phòng, xem giá.
2. Đăng ký khách mới, đăng nhập.
3. Chọn khoảng ngày và hai phòng, kiểm tra tổng tiền, bấm đặt.
4. Mở lịch sử và hồ sơ; sửa số điện thoại.
5. Đăng xuất; đăng nhập Staff.
6. Thêm/sửa/tìm khách và phòng.
7. Xem đơn vừa đặt, sửa danh sách phòng hoặc ngày; thử hủy đơn.
8. Xóa phòng đã có đơn: nhận thông báo chuyển ngừng hoạt động.
9. Tạo phòng mới chưa có đơn rồi xóa: phòng biến mất khỏi danh sách Staff.
10. Chạy bản SQL Server, kiểm tra 5 bảng và dữ liệu bằng SSMS.
