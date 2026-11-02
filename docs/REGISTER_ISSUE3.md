# Issue 3 Thiết kế test case và CSV Register

Nhánh: `test/register-csv`. Base PR: `chore/framework-pr`.
Phạm vi theo Issue #3 trong tài liệu phân công; số issue GitHub thực tế cần
được chọn khi tạo PR, không mặc định dùng số #3 của repository.

## Sản phẩm

- `BM02_Register_BusinessRules.md`: 8 Business Rule và liên kết test case.
- `BM03_TestCases.csv`: 10 dòng TC-REG-001 đến TC-REG-010 thuộc module Register;
  giữ nguyên các dòng Login có trên base.
- `src/test/resources/testdata/register_data.csv`: 10 dòng UTF-8 có header,
  `tcId`, input và expected; không chứa credential thật.
- `RegisterDataTest`: kiểm tra hợp đồng CSV, dữ liệu biên và fixture email.

BM03 trên nhánh này ghi `Automated=No`, `Status=NOT RUN`, Actual để trống.
`Automation_Method` là tên method dự kiến dùng khi thực hiện Issue #4.
Nhánh này không có RegisterPage hoặc RegisterTest và không khai báo đã chạy
10 test UI. Việc kiểm tra data bằng JUnit không thay thế thực thi nghiệp vụ.

## Tiêu chí chấp nhận

| Yêu cầu | TC | Cách thiết kế |
|---|---|---|
| Phone tùy chọn; họ tên bắt buộc | 001, 002 | Decision Table trường bắt buộc/tùy chọn |
| Email sai định dạng | 003 | Equivalence Partitioning |
| Email đã có, không phân biệt hoa thường | 004 | Fixture `${EXISTING_EMAIL_UPPER}` |
| Password 11 bị chặn, 12 được chấp nhận | 005, 006 | Boundary Value Analysis |
| Password 73 byte UTF-8 bị chặn | 007 | `${PASSWORD_73_BYTES}` có số ký tự nhỏ hơn HTML maxlength |
| Confirmation không khớp | 008 | Negative partition |
| Phone VN có khoảng trắng được chấp nhận | 009 | Valid partition |
| Phone chứa chữ bị chặn | 010 | Invalid partition |

Các email đăng ký mới dùng `${UNIQUE}` nên độc lập giữa các lần chạy.
TC-REG-004 cần `EXISTING_EMAIL` là một email có sẵn trong DB test, không dựa
vào một test khác vừa tạo account. Password trong CSV là dữ liệu giả.

## Kiểm tra

```powershell
mvn -o "-Dtest=RegisterDataTest" test
```

Reviewer theo phân công: Nguyễn Thị Ngọc Ánh. Sau khi nhánh data được merge,
đồng bộ nhánh automation và đổi base PR phù hợp; kết quả UI và metadata PASS
được cập nhật trong PR automation, không đưa ngược vào thiết kế ban đầu.
