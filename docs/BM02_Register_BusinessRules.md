# BM02 Business Rules Register

Owner: Nguyễn Văn Chương. Scope: US-01 `/register`.

| BR ID | Quy tắc kiểm thử | TC |
|---|---|---|
| BR-REG-01 | Đăng ký hợp lệ khi không nhập phone; tạo Customer pending verification và chuyển sang bước xác minh email. | 001, 006, 009 |
| BR-REG-02 | Họ tên bắt buộc; để trống bị browser validation chặn. | 002 |
| BR-REG-03 | Email phải đúng định dạng. | 003 |
| BR-REG-04 | Email đã tồn tại bị từ chối, không phân biệt hoa thường. | 004 |
| BR-REG-05 | Password tối thiểu 12 ký tự; kiểm tra 11 và 12. | 005, 006 |
| BR-REG-06 | Password tối đa 72 byte UTF-8; 73 byte bị chặn dù số ký tự nhỏ hơn maxlength. | 007 |
| BR-REG-07 | Password và confirmation phải khớp hoàn toàn. | 008 |
| BR-REG-08 | Phone tùy chọn; số Việt Nam có khoảng trắng được chấp nhận, ký tự chữ bị từ chối. | 009, 010 |

Basis: `RegisterPage.tsx`, `RegistrationRequest.java`, `RegistrationService.java`
và `AccountService.normalizePhone()` của source An Cư được đọc ngày 02/11/2026.
Các expected cần được đối chiếu lại với phiên bản SUT thực sự triển khai.

Case thành công chỉ kiểm tra kết quả UI và bước xác minh; không đọc inbox OTP.
UI có nhánh fallback `/login` nếu account đã tạo nhưng đăng nhập tự động thất
bại; chỉ chấp nhận fallback khi có đúng notice xác nhận tài khoản đã tạo.
Vai trò Customer, trạng thái pending, phone lưu chuẩn hóa và việc không tạo bản
ghi cho negative case cần bằng chứng backend/DB bổ sung để khẳng định đầy đủ.

Fixture `EXISTING_EMAIL` phải trỏ tới email đã tồn tại trong DB test riêng.
Các case còn lại sinh email độc lập bằng `${UNIQUE}`; không chạy theo thứ tự để
chuẩn bị case trùng email. Các password literal là dữ liệu giả, không phải secret.
