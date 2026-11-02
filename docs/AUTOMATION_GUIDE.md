# Hướng dẫn automation An Cư PropTech

Owner: Nguyễn Văn Chương — Framework Owner và Data Owner.

Theo bảng biểu mẫu của hướng dẫn RBL, BM05 là test data CSV trong
`src/test/resources/testdata/`. Đây là tài liệu giải thích framework bổ sung;
BM05 của Register là `register_data.csv`.

## Kiến trúc và luồng chạy

`TestData.resolvedRow(resource, tcId)` đọc CSV UTF-8 qua `CsvData` và tạo một
`DynamicData.Context` riêng cho mỗi dòng được lấy. Tất cả token `${UNIQUE}` trong
dòng dùng chung timestamp và UUID; các lần lấy dòng độc lập sinh giá trị riêng.
Token fixture được lấy qua `Config`; password fixture không nằm trong CSV/Git.
`PASSWORD_73_BYTES` sinh 37 ký tự nhưng đúng 73 byte UTF-8 để kiểm tra giới hạn
byte độc lập với `maxlength` của trình duyệt. Phone lấy trực tiếp từ CSV.

Test kế thừa `BaseTest`, điều phối hành động và assertion. Page kế thừa
`BasePage`, giữ locator và explicit wait. Không đặt assertion trong Page hoặc
locator trong Test; không dùng `Thread.sleep`. `BaseTest` tạo trình duyệt riêng
cho từng test và đóng trong `@AfterEach`.

`ScreenshotExtension` dùng `AfterTestExecutionCallback`, chụp lỗi trước khi
`@AfterEach` đóng trình duyệt. PNG có TC ID và timestamp; không tạo bằng chứng
fail cho test PASS hoặc aborted. Nếu chụp lỗi, giữ exception gốc và đính kèm lỗi
chụp bằng suppressed exception.

## Cấu hình và thực thi

Xem README và `.env.example`. Cần frontend, API, DB và fixture của môi trường
test đã sẵn sàng. Có thể truyền `-Dsut.env.file=...` hoặc `SUT_ENV_FILE`; không
phụ thuộc đường dẫn của một thành viên. Không đưa file chứa secret vào Git.
Chạy kiểm tra framework riêng trước, sau đó chạy từng suite UI và regression.

## Ghi nhận bằng chứng

Surefire ở `target/surefire-reports`, ảnh fail ở `target/screenshots`. Ghi commit,
môi trường, ngày chạy, Pass/Fail/Blocked vào BM06 và đồng bộ BM03 Actual/Status.
Phân biệt lỗi SUT với lỗi script, dữ liệu và môi trường trước khi tạo BM07.
Không suy ra PASS chỉ vì biên dịch thành công; không tạo ảnh/defect giả.
