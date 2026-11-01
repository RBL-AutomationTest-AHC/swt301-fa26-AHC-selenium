## Mô tả
- Module: M_ / Issue: #__
- Thay đổi: …

Closes #

## Loại thay đổi
- [ ] feat (tính năng kiểm thử mới)
- [ ] fix (sửa script kiểm thử flaky/locator)
- [ ] refactor (tái cấu trúc code test/page)
- [ ] docs (tài liệu BM01-BM09, README)
- [ ] chore (cấu hình pom.xml, git, workflow)
- [ ] test (dữ liệu CSV, test case specification)

## TC liên quan
TC-XXX-001 … TC-XXX-00n

## Cách kiểm thử
1. Mở terminal, chạy lệnh `mvn clean test -Dtest=<TestClass>`
2. Kiểm tra kết quả trong console và file log

Kết quả mong đợi: Toàn bộ test cases Pass (hoặc Fail đúng do bug của SUT đã ghi nhận).

## Ảnh chụp màn hình / Bằng chứng
(Đính kèm ảnh chụp kết quả Surefire report hoặc ảnh lỗi nếu có)

## Checklist tác giả
- [ ] Tiêu đề PR theo chuẩn Conventional Commits (`type(scope): description`)
- [ ] Đã rebase / merge nhánh `main` mới nhất trước khi mở PR
- [ ] Đã chạy `mvn clean test` thành công trên máy local
- [ ] Không có `Thread.sleep()`, không có XPath tuyệt đối
- [ ] Không có assertion trong Page class; không có By locator trong Test class
- [ ] Dữ liệu test nằm hoàn toàn trong CSV, không hard-code trong code
- [ ] Đã cập nhật BM03 (cột Automation Method)
- [ ] Không commit file nhạy cảm, mật khẩu, target/, .idea/ hoặc code tạm

