# Phân tích Assignment 03 - FUMiniHotelSystem

## 1. Đề thực sự yêu cầu gì?

Đề có phần giới thiệu khá rộng (hóa đơn, housekeeping, tài chính...). Phần chức năng chính ở trang 5-6 chỉ yêu cầu quản lý khách hàng, phòng và đặt phòng.
Một số câu như "news data" hay "vehicles" là nội dung sót từ mẫu; đối tượng bài này vẫn là khách sạn.

| Yêu cầu bắt buộc | Cách làm trong dự án |
| --- | --- |
| Spring Boot 3 REST API | Spring Boot 3.5.16, Java 21, Maven |
| SQL Server, JPA tạo bảng | Profile `sqlserver`, các entity JPA và `ddl-auto=update` |
| 3-Layer + Repository | Controller → Service → Repository |
| JWT | Spring Security xác thực Bearer JWT ký HS256 |
| Staff trong application.properties | `app.staff.email`, `app.staff.password` |
| Khách xem phòng không đăng nhập | GET `/api/rooms`, `/api/rooms/{id}`, `/api/room-types` công khai |
| ReactJS + axios | Vite React, axios trong `services/api.js` |
| Kiểm tra dữ liệu | Bean Validation + kiểm tra nghiệp vụ trong service |
| Kiểm thử API bằng Postman | Collection trong `postman/` |
| Tên project/database theo mẫu | `A3StudentName_ClassCode`, cần đổi trước khi nộp |

## 2. Phân quyền

| Thao tác | Chưa đăng nhập | Customer | Staff |
| --- | --- | --- | --- |
| Xem/tìm phòng đang hoạt động | Có | Có | Có |
| Đăng ký/đăng nhập | Có | Có | Có |
| Đặt online một hoặc nhiều phòng | Không | Có, cho bản thân | Dùng tạo đơn tại quầy |
| Sửa hồ sơ | Không | Chỉ bản thân | Quản lý qua màn khách hàng |
| Xem lịch sử đặt | Không | Chỉ của mình | Tất cả |
| CRUD/tìm khách hàng | Không | Không | Có |
| CRUD/tìm phòng | Không | Không | Có |
| Quản lý đơn và chi tiết phòng | Không | Không | Có |

FE ẩn menu theo vai trò giúp người dùng sử dụng thuận tiện. BE vẫn kiểm tra quyền trên từng nhóm endpoint; không dựa vào FE để bảo vệ dữ liệu.
Customer ID của thao tác khách hàng lấy từ JWT, không tin ID gửi lên trong body.
API không trả entity Customer trực tiếp, vì entity chứa mật khẩu đã mã hóa.

## 3. Đọc sơ đồ dữ liệu trang 4

### Customer

`customerId`, `customerFullName`, `telephone`, `emailAddress`, `customerBirthday`, `customerStatus`, `password`.

Mỗi email là duy nhất. Password lưu BCrypt, không lưu mật khẩu gốc.

### RoomType

`roomTypeId`, `roomTypeName`, `typeDescription`, `typeNote`.

Loại phòng là dữ liệu danh mục. Đề không yêu cầu CRUD riêng cho RoomType, nên dùng 3 loại mẫu và API GET để chọn trong form.

### RoomInformation

`roomId`, `roomNumber`, `roomDetailDescription`, `roomMaxCapacity`, `roomType`, `roomStatus`, `roomPricePerDay`.

Mỗi phòng thuộc đúng một loại (`ManyToOne`). Số phòng duy nhất.

### BookingReservation

`bookingReservationId`, `bookingDate`, `totalPrice`, `customer`, `bookingStatus`.

Một khách có nhiều đơn (`Customer 1-n BookingReservation`). Mỗi đơn có ít nhất một chi tiết phòng.

### BookingDetail

Khóa chính ghép: **bookingReservationId + roomId**. Các trường còn lại: `startDate`, `endDate`, `actualPrice`.

Dùng `@Embeddable BookingDetailId`, `@EmbeddedId`, `@MapsId` để bám đúng sơ đồ, không tự thêm ID thứ ba.
BookingDetail là bảng nối giữa BookingReservation và RoomInformation, đồng thời lưu ngày và giá tại lúc đặt.

```text
Customer       1 ─── n BookingReservation
BookingReservation 1 ─── n BookingDetail
RoomInformation    1 ─── n BookingDetail
RoomType       1 ─── n RoomInformation
```

Tên bảng/cột trong SQL dùng `snake_case` theo convention JPA; thuộc tính Java dùng `camelCase`.

## 4. Các quy ước bổ sung do đề chưa mô tả chi tiết

Các quyết định dưới đây là cách hoàn thiện nghiệp vụ của bài làm, không phải nội dung được đề quy định cụ thể:

- CustomerStatus/RoomStatus: `1 = hoạt động`, `0 = ngừng hoạt động`.
- BookingStatus: `CONFIRMED`, `COMPLETED`, `CANCELLED`.
- Tạo đơn thành công thì xác nhận ngay; chưa có thanh toán trực tuyến hay bước duyệt đơn.
- Đơn đang xác nhận được sửa khách, danh sách phòng và ngày. Chỉ Staff được sửa/hủy/hoàn tất.
- Không hoàn tất đơn trước ngày trả phòng cuối cùng; đơn đã hủy/hoàn tất không mở lại.
- Muốn xóa đơn phải hủy trước. Xóa đơn sẽ xóa các chi tiết của đơn đó.
- Khách có lịch sử đặt khi xóa sẽ khóa tài khoản để giữ dữ liệu liên kết; khách chưa có đơn có thể xóa thật.
- Một phòng không được lặp hai lần trong cùng một đơn vì khóa chính ghép không cho phép.
- Mỗi chi tiết có ngày riêng ở API; form FE cơ bản áp dụng cùng ngày nhận/trả cho tất cả phòng đã chọn.
- Ngày nhận từ hôm nay trở đi, ngày trả phải sau ngày nhận.
- Một lần đặt tối đa 20 phòng để giới hạn kích thước request.
- `actualPrice` là **giá một đêm lúc đặt**, dùng để giữ giá lịch sử. Không lấy giá từ FE.

### Tính tiền

```text
Số đêm của mỗi phòng = endDate - startDate
Tiền phòng = số đêm × actualPrice
TotalPrice = tổng tiền các phòng
```

Ví dụ: phòng 101 giá 300.000/đêm và phòng 102 giá 500.000/đêm, cùng 2 đêm:
`(300.000 × 2) + (500.000 × 2) = 1.600.000 VND`.

Dùng `BigDecimal` cho tiền. Khi sửa đơn, phòng được giữ lại dùng giá lịch sử; phòng mới thêm lấy giá hiện tại.

### Kiểm tra trùng lịch

Hai khoảng ngày trùng nhau khi:

```text
ngày nhận cũ < ngày trả mới AND ngày trả cũ > ngày nhận mới
```

Khoảng lưu trú là `[ngày nhận, ngày trả)`: khách trả ngày 10 thì khách khác có thể nhận ngày 10.
Đơn hủy không giữ chỗ. Availability và thao tác đặt đều kiểm tra cùng quy tắc.
Service khóa bản ghi phòng trong transaction khi đặt, tránh hai request cùng lấy một phòng trống.
Đặt nhiều phòng là một transaction: nếu một phòng lỗi, cả đơn không được lưu.

### Xóa phòng đúng yêu cầu đề

1. Chưa có bất kỳ BookingDetail nào: xóa thật.
2. Đã có BookingDetail: đặt RoomStatus = 0.
3. Phòng ngừng hoạt động không hiện trong danh sách công khai và không nhận đơn mới.
4. Lịch sử đơn cũ vẫn hiển thị phòng và giá đã đặt.

## 5. Tại sao chia Java theo cách này?

Ví dụ tạo phòng:

```text
StaffRoomsPage.jsx
  → axios POST /api/staff/rooms
  → Spring Security kiểm tra JWT và STAFF
  → StaffController nhận RoomRequest, chạy @Valid
  → RoomService kiểm tra số phòng, loại phòng, nghiệp vụ
  → RoomRepository.save(...)
  → SQL Server
  → RoomResponse JSON
  → React tải lại bảng
```

- **Controller**: URL, HTTP method, request và response. Không viết SQL hay công thức tiền ở đây.
- **Service**: nghiệp vụ, quyền sở hữu dữ liệu, transaction, tính tiền, quy tắc xóa.
- **Repository**: truy vấn JPA. Không chứa xử lý giao diện hay HTTP.
- **Entity**: mô tả bảng, khóa chính và quan hệ.
- **DTO**: dữ liệu được phép nhận/trả; đặt validation ở request.
- **Config**: cấu hình hạ tầng; không phải một tầng nghiệp vụ thứ tư.

Không tách `Service`/`ServiceImpl` khi chỉ có một cách triển khai. Đề yêu cầu 3 tầng, không yêu cầu interface cho từng service.
Không viết BaseEntity, generic CRUD service hoặc mapper framework để giảm khái niệm người mới phải học.

## 6. JWT trong bài này

1. FE gửi email/password đến `/api/auth/login`.
2. Backend kiểm tra tài khoản Staff trong cấu hình hoặc Customer trong database.
3. Backend phát JWT chứa `sub`, `role`, `iss`, `iat`, `exp`.
4. FE giữ session trong `sessionStorage`, axios gắn `Authorization: Bearer <token>`.
5. Spring Security kiểm tra chữ ký, thời hạn, issuer và vai trò.
6. Với Customer, kiểm tra thêm tài khoản còn hoạt động; khóa tài khoản sẽ vô hiệu token cũ.
7. FE đăng xuất bằng cách xóa session. JWT có hạn 2 giờ; chưa làm refresh token hoặc blacklist đăng xuất phía server.

Spring Security đã có xử lý Bearer JWT nên không cần tự viết bộ lọc JWT.
Các DTO không chứa vai trò để khách không tự đăng ký thành Staff.

## 7. Cấu trúc FE dành cho người mới

- `App.jsx`: BrowserRouter routes, menu, trạng thái đăng nhập.
- `pages`: mỗi màn hình một file, gồm state, sự kiện, gọi API và JSX của màn đó.
- `components/CustomerForm`: dùng chung cho đăng ký, hồ sơ, quản lý khách.
- `components/BookingTable`: bảng lịch sử cho khách và Staff.
- `components/ProtectedRoute`: chuyển trang khi chưa đăng nhập/sai vai trò.
- `services/api.js`: một axios instance, token và thông báo lỗi.
- `styles.css`: CSS thuần, không dùng thư viện giao diện.

Chỉ dùng `useState`, `useEffect`, props và React Router. Không cần Redux, Context hoặc custom hook cho phạm vi bài này.
Form dùng input/select HTML; sửa bằng form ngay trên bảng, không dùng modal phức tạp.

## 8. Trình tự học và trình bày bài

1. Chạy demo; xem phòng khi chưa đăng nhập.
2. Đăng ký một khách mới, đăng nhập và đặt hai phòng.
3. Giải thích luồng React → Controller → Service → Repository → DB.
4. Mở lịch sử và hồ sơ để chứng minh quyền Customer.
5. Đăng nhập Staff, CRUD khách/phòng và quản lý đơn.
6. Xóa một phòng đã có đơn để chứng minh soft delete; so với xóa phòng chưa có đơn.
7. Dùng Postman thử gọi API Staff bằng token Customer, kiểm tra 403.
8. Chuyển sang SQL Server, xem 5 bảng và khóa ngoại do JPA tạo.

## 9. Phạm vi và điều cần cấu hình thật

Bài này không mở rộng sang thanh toán, email, hóa đơn, housekeeping, upload ảnh hay dashboard thống kê vì phần Main Functions không yêu cầu.
Tên sinh viên/lớp chưa có trong đầu vào; cần đổi tên theo README trước khi nộp.
Đã chạy bản SQL Server bằng Windows Authentication trên máy hiện tại; có script `run-sqlserver.ps1` để chạy lại.
Kiểm thử tự động dùng H2; collection Postman được kiểm tra riêng trên SQL Server thật.

Tài liệu kỹ thuật đối chiếu:

- [Spring Boot 3.5 system requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html).
- [Spring Security 6.5 JWT resource server](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html).
