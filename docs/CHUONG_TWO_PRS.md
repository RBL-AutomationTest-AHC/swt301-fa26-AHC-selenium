# Hai pull request của Chương

Các nhánh cũ được giữ nguyên. Hai nhánh dưới được tạo riêng từ các thay đổi
đã hoàn thiện để review theo đúng hai đơn vị công việc, không tạo thêm Issue #3.

| Công việc theo tài liệu phân công | Head branch | Base PR trước khi merge framework |
|---|---|---|
| Issue #8 Framework và DynamicData | `chore/framework-pr` | `main` |
| Issue #4 RegisterPage và RegisterTest | `feature/register-pr` | `chore/framework-pr` |

Hai nhánh xếp nối tiếp. Merge framework trước, sau đó đổi base PR Register
thành main. Nếu dùng squash merge và GitHub vẫn hiển thị commit framework
trong PR Register, fetch rồi merge main mới vào nhánh Register trước khi đổi
base; không cần viết lại lịch sử hoặc force-push. Các nhánh này hiện chỉ được
chuẩn bị local.

Số #4/#8 ở trên là số trong tài liệu. Phải thay bằng số issue GitHub thực tế
khi liên kết PR; repository đã có các issue/PR của thành viên khác mang số này.

## PR Framework

Title: `chore(framework): complete local configuration and dynamic data support`

Mô tả: Hoàn thiện cấu hình local `.env.local`, hỗ trợ Run trực tiếp trong IDE,
token email fixture uppercase và cơ chế screenshot giữ nguyên lỗi gốc. Tái sử
dụng base/config/data có trên main. Có kiểm tra CSV UTF-8, dữ liệu biên 73 byte,
context độc lập và fixture precedence. Không chứa Page/Test/CSV Register.

Validation: clean build và ConfigTest/DataUtilitiesTest/ScreenshotExtensionTest
đạt 8/8 PASS, không cần SUT.

## PR Register

Title: `feat(register): automate ten registration cases using CSV and page objects`

Mô tả: Thêm RegisterPage và 10 RegisterTest, CSV/BM02/BM03 liên quan, test hợp
đồng dữ liệu, shared IntelliJ run configurations và evidence. CSV được đưa vào
PR này như dữ liệu cần để chạy suite, không tách thành issue/PR dữ liệu thứ ba.
Fixture thật trong `.env.local` và `config.local.properties` được Git ignore.

Validation: rerun ngày 02/11/2026 lúc 11:13:48 trên framework `f0c5cc9` và
working tree Register đã tách: 21/21 PASS (10 Register + 11 framework/data),
không skipped; Register mất khoảng 49.45 giây. Source/data khớp bản hoàn thiện
ở nhánh `feature/chuong-register-tests`. Evidence local:
`target/evidence/register-split-pr/`. Các lần chạy trước ở `REGISTER_VALIDATION.md`.
Reviewer theo phân công: Nguyễn Thị Ngọc Ánh. Chưa tạo approval hoặc review
trên GitHub thay cho reviewer.
