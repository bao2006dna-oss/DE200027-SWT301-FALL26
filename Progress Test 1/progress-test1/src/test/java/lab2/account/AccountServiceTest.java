package lab2.account;

import fu.de200027.Account;
import fu.de200027.AccountService;
import fu.de200027.AccountStatus;
import fu.de200027.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    static final String USER = "alice_01";
    static final String EMAIL = "alice@example.com";
    static final String PASS = "Secret@123";
    static final String PHONE = "0901234567";
    static final LocalDate DOB = LocalDate.now().minusYears(20);

    AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(); // Mỗi test dùng 1 service mới -> đảm bảo tính độc lập
    }

    @Nested
    @DisplayName("Module Đăng ký (Register)")
    class Register {

        @Test
        @DisplayName("Đăng ký thành công - Kiểm tra toàn bộ trạng thái tài khoản")
        void register_Success_VerifyState() {
            ResultCode result = service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);

            assertEquals(ResultCode.SUCCESS, result);

            Optional<Account> accOpt = service.findByUsername(USER);
            assertTrue(accOpt.isPresent());

            Account acc = accOpt.get();
            assertEquals(AccountStatus.ACTIVE, acc.getStatus());
            assertEquals(0, acc.getFailedAttempts());
            assertFalse(acc.isLocked());
            assertNotEquals(PASS, acc.getCurrentPasswordHash()); // Hash không trùng mật khẩu rõ
            assertEquals(EMAIL.toLowerCase(), acc.getEmail());   // Email được lưu chữ thường
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("lab2.account.AccountServiceTest#invalidRegisterInputs")
        void register_InvalidInputs_ReturnsExpectedCode(String desc, String u, String e, String p, String c,
                                                        LocalDate dob, String phone, ResultCode expected) {
            assertEquals(expected, service.register(u, e, p, c, dob, phone));
            assertTrue(service.findByUsername(u).isEmpty()); // Đảm bảo không tạo tài khoản
        }

        @ParameterizedTest(name = "[{index}] Null/Empty Username -> INVALID_INPUT")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void register_NullAndEmptyUsername(String username) {
            assertEquals(ResultCode.INVALID_INPUT, service.register(username, EMAIL, PASS, PASS, DOB, PHONE));
        }

        @ParameterizedTest(name = "[{index}] Null/Empty Email -> INVALID_INPUT")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void register_NullAndEmptyEmail(String email) {
            assertEquals(ResultCode.INVALID_INPUT, service.register(USER, email, PASS, PASS, DOB, PHONE));
        }

        @ParameterizedTest(name = "[{index}] Null/Empty Password -> INVALID_INPUT")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void register_NullAndEmptyPassword(String pass) {
            assertEquals(ResultCode.INVALID_INPUT, service.register(USER, EMAIL, pass, PASS, DOB, PHONE));
        }

        @ParameterizedTest(name = "[{index}] Trùng username: {0}")
        @ValueSource(strings = {"alice_01", "ALICE_01", "Alice_01"})
        void register_DuplicateUsername_CaseInsensitive(String duplicateUser) {
            service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);
            assertEquals(ResultCode.DUPLICATE_USERNAME, service.register(duplicateUser, "other@example.com", PASS, PASS, DOB, PHONE));
        }

        @ParameterizedTest(name = "[{index}] Trùng email: {0}")
        @ValueSource(strings = {"alice@example.com", "ALICE@EXAMPLE.COM", "Alice@Example.Com"})
        void register_DuplicateEmail_CaseInsensitive(String duplicateEmail) {
            service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);
            assertEquals(ResultCode.DUPLICATE_EMAIL, service.register("bob_0123", duplicateEmail, PASS, PASS, DOB, PHONE));
        }

        @ParameterizedTest(name = "[{index}] today - {0} năm + {1} ngày -> {2}")
        @CsvSource({
                "18, 0, SUCCESS",
                "18, 1, UNDERAGE",
                "0, 1, INVALID_INPUT"
        })
        void register_AgeBoundary(int yearsAgo, int plusDays, ResultCode expected) {
            LocalDate dob = LocalDate.now().minusYears(yearsAgo).plusDays(plusDays);
            assertEquals(expected, service.register("user_test", "test@domain.com", PASS, PASS, dob, null));
        }
    }

    static Stream<Arguments> invalidRegisterInputs() {
        return Stream.of(
                // Mỗi BR-REG một dòng
                Arguments.of("REG-01: Ngày sinh ở tương lai", USER, EMAIL, PASS, PASS, LocalDate.now().plusDays(1), PHONE, ResultCode.INVALID_INPUT),
                Arguments.of("REG-02: Username không hợp lệ", "1alice", EMAIL, PASS, PASS, DOB, PHONE, ResultCode.INVALID_USERNAME),
                Arguments.of("REG-04: Email không hợp lệ", USER, "bad_email", PASS, PASS, DOB, PHONE, ResultCode.INVALID_EMAIL),
                Arguments.of("REG-06: Mật khẩu yếu", USER, EMAIL, "weak", "weak", DOB, PHONE, ResultCode.WEAK_PASSWORD),
                Arguments.of("REG-07: Confirm password không khớp", USER, EMAIL, PASS, "Different@123", DOB, PHONE, ResultCode.PASSWORD_MISMATCH),
                Arguments.of("REG-08: Chưa đủ 18 tuổi", USER, EMAIL, PASS, PASS, LocalDate.now().minusYears(17), PHONE, ResultCode.UNDERAGE),
                Arguments.of("REG-09: Phone không hợp lệ", USER, EMAIL, PASS, PASS, DOB, "   ", ResultCode.INVALID_PHONE),

                // 3 test kiểm tra thứ tự ưu tiên (Priority Order) khi vi phạm nhiều quy tắc cùng lúc
                Arguments.of("Priority: Username sai + Email sai -> REG-02", "1alice", "bad_email", PASS, PASS, DOB, PHONE, ResultCode.INVALID_USERNAME),
                Arguments.of("Priority: Email sai + MK yếu -> REG-04", USER, "bad_email", "weak", "weak", DOB, PHONE, ResultCode.INVALID_EMAIL),
                Arguments.of("Priority: MK yếu + Confirm lệch -> REG-06", USER, EMAIL, "weak", "Different@123", DOB, PHONE, ResultCode.WEAK_PASSWORD)
        );
    }
}