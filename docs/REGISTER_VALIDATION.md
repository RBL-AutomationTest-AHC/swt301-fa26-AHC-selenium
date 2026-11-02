# Xác minh phần Nguyễn Văn Chương

Ngày chạy: 02/11/2026 (Asia/Bangkok). Automation code: `22eeeff`.
SUT: `F:/2026/Fa26/DTD/ancu-proptech`, commit `1e626fe`, working tree sạch tại
lúc ghi nhận. Frontend `http://localhost:3000`; backend `http://localhost:8080`;
SQL Server local theo cấu hình sẵn có. Không sửa source hoặc file secret SUT.

## Kết quả

| Bộ kiểm tra | Tests | Pass | Failure/Error/Skipped |
|---|---:|---:|---:|
| ConfigTest | 2 | 2 | 0/0/0 |
| ScreenshotExtensionTest | 1 | 1 | 0/0/0 |
| DataUtilitiesTest | 4 | 4 | 0/0/0 |
| RegisterDataTest | 3 | 3 | 0/0/0 |
| RegisterTest | 10 | 10 | 0/0/0 |
| Tổng lần xác minh cuối | 20 | 20 | 0/0/0 |

Register chạy thành công hai lần: REG-RUN-01 là 10/10, 57.682 giây;
REG-RUN-02 là 10/10, 46.421 giây. REG-RUN-01 dùng working tree sau đó được
commit nguyên phần code thành `22eeeff`; REG-RUN-02 chạy trên commit đó.
Đây là hai lần xác minh module Register, không phải hai regression toàn nhóm.

Lệnh cuối (PowerShell, cần cho phép Chrome chạy ngoài sandbox nếu bị chặn):

```powershell
mvn -o "-Dtest=ConfigTest,DataUtilitiesTest,ScreenshotExtensionTest,RegisterDataTest,RegisterTest" "-Dheadless=true" "-Dsut.env.file=F:/2026/Fa26/DTD/ancu-proptech/ancu-proptech-backend/.env.local" test
```

JUnit XML/text được giữ local ở `target/evidence/register-run-01/` và
`target/evidence/register-run-02/`. Các thư mục target không commit vào Git.
SHA-256 của Register XML REG-RUN-02:
`13f50c66d946b99c4545e69da78463bce79b1be5e29d3179674747ccff4ef165`.
Chi tiết theo TC: `Register_ExecutionResults.csv`; nhật ký module:
`BM06_RegisterExecutionLog.csv`. BM03 chỉ cập nhật 10 dòng Register;
không đổi Actual/Status của Login.

## Kiểm tra trình duyệt bổ sung

Playwright mở trang thật và kiểm tra desktop 1440×1000, mobile 390×844:
tên trống bị required validation; confirmation không khớp hiển thị đúng
`Mật khẩu xác nhận không khớp.`; cả hai ở lại `/register` và không tràn ngang.
Không mock API trong kiểm tra này.

Console/network được kiểm tra: `/auth/csrf` và `/auth/vneid-config` trả 200;
`/auth/refresh` trả 401 khi chưa có phiên đăng nhập. Không có request register
trong hai thao tác bị chặn ở client nêu trên. Playwright smoke này không thay
thế bộ 10 case Selenium đã chạy riêng với API/DB thật.

## Giới hạn và cảnh báo môi trường

Lần Selenium đầu trong sandbox không tạo được Chrome session
(`DevToolsActivePort` và cache access denied), nên không có test nghiệp vụ nào
được xác minh trong lần đó. Chạy lại ngoài sandbox đạt 10/10 rồi 20/20 ở lần
cuối. Đây là lỗi môi trường, không mở defect SUT.

Selenium 4.21.0 cảnh báo chưa có CDP implementation cho Chrome 156.0.8078.75.
Các thao tác WebDriver đang dùng và toàn bộ kiểm tra trên đều PASS; suite không
dùng API DevTools. Không nâng dependency ngoài phạm vi task.

Các case thành công tạo tài khoản dữ liệu giả với prefix `rbl-` và email thuộc
`example.invalid`. Không xóa dữ liệu DB hoặc reset fixture có sẵn. OTP/inbox,
phân quyền Customer và trạng thái lưu DB chưa được khẳng định bởi suite UI này.
Phone có khoảng trắng được chấp nhận qua luồng UI; không đọc DB để khẳng định
giá trị đã lưu chuẩn hóa. Các assertion negative xác nhận lỗi và ở lại form,
không khẳng định đã truy vấn DB để chứng minh không có bản ghi mới.

Không sinh screenshot cho hai lần Register PASS. Cơ chế screenshot được kiểm
tra bằng framework test với driver giả: chỉ capture exception của test body,
bỏ qua PASS/aborted, giữ exception gốc nếu chụp lỗi. Chưa tạo failure có chủ ý
trên SUT để lấy ảnh hoặc defect giả.

Chưa chạy lại Login/Search hoặc regression toàn nhóm trong task này. Cần
Integration Owner hợp nhất các nhánh, fixture và tài liệu còn lại rồi chạy
regression toàn dự án trước khi chốt BM08 hoặc tag v1.0.

## Rerun REG-004 sau lỗi cấu hình IntelliJ

Ngày 02/11/2026, lần chạy IntelliJ của người dùng báo
`Thiếu fixture: EXISTING_EMAIL`. Run configuration tạm `RegisterTest` không
truyền `sut.env.file` hoặc `SUT_ENV_FILE`; case đọc data trước khi mở form nên
ảnh fail lúc đó trắng. File ảnh local được giữ nguyên tại
`target/screenshots/TC-REG-004__FAIL__20261102-104859-773.png`.

Bổ sung hai cấu hình shared `.run`: `RegisterTest (An Cu local)` và
`REG-004 (An Cu local)`, trỏ file fixture bằng `$PROJECT_DIR$` theo layout SWT/DTD.
Kiểm tra XML và đường dẫn thành công. Chưa bấm Run trực tiếp trong giao diện
IntelliJ; đã chạy riêng `RegisterTest#duplicateEmail` bằng Maven với cùng file
fixture lúc 10:52:48: 1 test, 0 failure/error/skipped, 4.569 giây, BUILD SUCCESS.
ConfigTest và RegisterDataTest cũng đạt 5/5. Warning CDP 156 vẫn xuất hiện nhưng
không ngăn REG-004 PASS. Evidence rerun local ở
`target/evidence/register-004-ide-fixture-fix/`.

Lỗi thiếu fixture còn tái diễn khi người dùng tiếp tục chạy cấu hình gutter
không có VM option. Bổ sung hỗ trợ file `config.local.properties` tại working
directory của project và tạo file local ignored trên máy này để trỏ backend
`.env.local`. Cấu hình này không chứa credential trong source và không cần
chọn cấu hình IntelliJ đặc biệt. `-Dsut.env.file`/`SUT_ENV_FILE` vẫn ưu tiên.
ConfigTest mới kiểm tra tự nạp và khả năng override trong JVM riêng, thư mục
tạm riêng. Tổng ConfigTest/RegisterDataTest đạt 6/6.

Rerun lúc 10:57:21 bằng `mvn -o "-Dtest=RegisterTest#duplicateEmail"
"-Dheadless=true" test` không truyền `sut.env.file`: REG-004 PASS, 4.714 giây.
Evidence ở `target/evidence/register-004-local-config/`. Chưa chạy nút Run
trong IntelliJ; IntelliJ cần working directory là thư mục gốc RBL như Maven.

Theo yêu cầu dùng file môi trường riêng của project test, đã tạo `.env.local`
ngay tại RBL với các khóa runtime và `EXISTING_EMAIL`, không sao chép password
backend. `config.local.properties` và hai cấu hình `.run` hiện trỏ vào file
này. Cả hai file local được Git ignore. Rerun riêng REG-004 lúc 11:01:52 bằng
lệnh không truyền `sut.env.file` đạt PASS, 4.668 giây, 0 failure/error/skipped.
