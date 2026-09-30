package fu.de200027;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử AccountService")
class AccountServiceTest {

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }

    @Nested
    @DisplayName("Kiểm thử chức năng Register (BR-REG)")
    class Register {

        private static final String VALID_USER = "alice_01";
        private static final String VALID_EMAIL = "alice@example.com";
        private static final String VALID_PASS = "Secret@123";
        private static final LocalDate VALID_DOB = LocalDate.now().minusYears(20);
        private static final String VALID_PHONE = "0987654321";

        @Test
        @DisplayName("1. Đăng ký thành công -> SUCCESS")
        void register_Success() {
            ResultCode result = service.register(VALID_USER, VALID_EMAIL, VALID_PASS, VALID_PASS, VALID_DOB, VALID_PHONE);
            assertEquals(ResultCode.SUCCESS, result);
            assertTrue(service.findByUsername(VALID_USER).isPresent());
        }

        @Test
        @DisplayName("2. REG-01: Tham số rỗng/null hoặc DOB ở tương lai -> INVALID_INPUT")
        void register_REG01_InvalidInput() {
            // Null parameters
            assertEquals(ResultCode.INVALID_INPUT, service.register(null, VALID_EMAIL, VALID_PASS, VALID_PASS, VALID_DOB, VALID_PHONE));
            assertEquals(ResultCode.INVALID_INPUT, service.register(VALID_USER, null, VALID_PASS, VALID_PASS, VALID_DOB, VALID_PHONE));
            assertEquals(ResultCode.INVALID_INPUT, service.register(VALID_USER, VALID_EMAIL, null, VALID_PASS, VALID_DOB, VALID_PHONE));
            assertEquals(ResultCode.INVALID_INPUT, service.register(VALID_USER, VALID_EMAIL, VALID_PASS, null, VALID_DOB, VALID_PHONE));
            assertEquals(ResultCode.INVALID_INPUT, service.register(VALID_USER, VALID_EMAIL, VALID_PASS, VALID_PASS, null, VALID_PHONE));

            // Date of birth ở tương lai
            assertEquals(ResultCode.INVALID_INPUT, service.register(VALID_USER, VALID_EMAIL, VALID_PASS, VALID_PASS, LocalDate.now().plusDays(1), VALID_PHONE));
        }

        @Test
        @DisplayName("3. REG-02: Username không hợp lệ -> INVALID_USERNAME")
        void register_REG02_InvalidUsername() {
            ResultCode result = service.register("123user", VALID_EMAIL, VALID_PASS, VALID_PASS, VALID_DOB, VALID_PHONE);
            assertEquals(ResultCode.INVALID_USERNAME, result);
        }

        @Test
        @DisplayName("4. REG-04: Email không hợp lệ -> INVALID_EMAIL")
        void register_REG04_InvalidEmail() {
            ResultCode result = service.register(VALID_USER, "invalid-email", VALID_PASS, VALID_PASS, VALID_DOB, VALID_PHONE);
            assertEquals(ResultCode.INVALID_EMAIL, result);
        }

        @Test
        @DisplayName("5. REG-06: Mật khẩu yếu -> WEAK_PASSWORD")
        void register_REG06_WeakPassword() {
            ResultCode result = service.register(VALID_USER, VALID_EMAIL, "12345", "12345", VALID_DOB, VALID_PHONE);
            assertEquals(ResultCode.WEAK_PASSWORD, result);
        }

        @Test
        @DisplayName("6. REG-07: Xác nhận mật khẩu không khớp -> PASSWORD_MISMATCH")
        void register_REG07_PasswordMismatch() {
            ResultCode result = service.register(VALID_USER, VALID_EMAIL, VALID_PASS, "WrongConfirm@123", VALID_DOB, VALID_PHONE);
            assertEquals(ResultCode.PASSWORD_MISMATCH, result);
        }

        @Test
        @DisplayName("7. REG-08: Chưa đủ 18 tuổi -> UNDERAGE")
        void register_REG08_Underage() {
            ResultCode result = service.register(VALID_USER, VALID_EMAIL, VALID_PASS, VALID_PASS, LocalDate.now().minusYears(17), VALID_PHONE);
            assertEquals(ResultCode.UNDERAGE, result);
        }

        @Test
        @DisplayName("8. REG-09: Phone không hợp lệ -> INVALID_PHONE")
        void register_REG09_InvalidPhone() {
            ResultCode result = service.register(VALID_USER, VALID_EMAIL, VALID_PASS, VALID_PASS, VALID_DOB, "012345");
            assertEquals(ResultCode.INVALID_PHONE, result);
        }

        @Test
        @DisplayName("9. REG-09: Phone null hoặc rỗng -> Vẫn chấp nhận SUCCESS")
        void register_REG09_NullOrEmptyPhone_Success() {
            assertEquals(ResultCode.SUCCESS, service.register("user_p1", "p1@example.com", VALID_PASS, VALID_PASS, VALID_DOB, null));
            assertEquals(ResultCode.SUCCESS, service.register("user_p2", "p2@example.com", VALID_PASS, VALID_PASS, VALID_DOB, ""));
        }

        @Test
        @DisplayName("10. REG-03: Trùng Username (không phân biệt hoa/thường) -> DUPLICATE_USERNAME")
        void register_REG03_DuplicateUsername() {
            service.register(VALID_USER, VALID_EMAIL, VALID_PASS, VALID_PASS, VALID_DOB, VALID_PHONE);
            ResultCode result = service.register("ALICE_01", "other@example.com", VALID_PASS, VALID_PASS, VALID_DOB, VALID_PHONE);
            assertEquals(ResultCode.DUPLICATE_USERNAME, result);
        }

        @Test
        @DisplayName("11. REG-05: Trùng Email (không phân biệt hoa/thường) -> DUPLICATE_EMAIL")
        void register_REG05_DuplicateEmail() {
            service.register(VALID_USER, VALID_EMAIL, VALID_PASS, VALID_PASS, VALID_DOB, VALID_PHONE);
            ResultCode result = service.register("bob_01", "ALICE@EXAMPLE.COM", VALID_PASS, VALID_PASS, VALID_DOB, VALID_PHONE);
            assertEquals(ResultCode.DUPLICATE_EMAIL, result);
        }
    }

    @Nested
    @DisplayName("Kiểm thử chức năng Login (BR-LOG) & Admin Management")
    class Login {

        private static final String USER = "alice_01";
        private static final String EMAIL = "alice@example.com";
        private static final String PASS = "Secret@123";
        private static final String WRONG = "WrongPass@123";
        private static final LocalDate DOB = LocalDate.now().minusYears(20);

        @BeforeEach
        void registerDefaultAccount() {
            service.register(USER, EMAIL, PASS, PASS, DOB, "0987654321");
        }

        private void failLogin(int times) {
            for (int i = 0; i < times; i++) {
                service.login(USER, WRONG);
            }
        }

        // Rule 6: Đăng nhập thành công
        @Test
        @DisplayName("Rule 6: Đăng nhập đúng username và password -> SUCCESS")
        void login_Success() {
            ResultCode result = service.login(USER, PASS);
            assertEquals(ResultCode.SUCCESS, result);
            Account acc = service.findByUsername(USER).orElseThrow();
            assertEquals(0, acc.getFailedAttempts());
        }

        // Rule 1: User không tồn tại
        @Test
        @DisplayName("Rule 1: Username không tồn tại -> INVALID_CREDENTIALS")
        void login_NonExistentUser() {
            ResultCode result = service.login("non_existent", PASS);
            assertEquals(ResultCode.INVALID_CREDENTIALS, result);
        }

        // Rule 2: Tài khoản bị DISABLED
        @ParameterizedTest(name = "[{index}] Mật khẩu = ''{0}''")
        @ValueSource(strings = {PASS, WRONG})
        @DisplayName("Rule 2: Tài khoản bị DISABLED -> ACCOUNT_DISABLED")
        void login_DisabledAccount(String inputPassword) {
            service.disableAccount(USER);
            ResultCode result = service.login(USER, inputPassword);
            assertEquals(ResultCode.ACCOUNT_DISABLED, result);
        }

        // Rule 4 & 5: Kiểm tra số lần sai liên tiếp và ngưỡng khóa 5 lần (BVA 4/5)
        @ParameterizedTest(name = "[{index}] Nhập sai {0} lần -> Kết quả: {1}, Đã bị khóa: {2}")
        @CsvSource({
                "1, INVALID_CREDENTIALS, false",
                "2, INVALID_CREDENTIALS, false",
                "3, INVALID_CREDENTIALS, false",
                "4, INVALID_CREDENTIALS, false", // Biên dưới ngưỡng
                "5, ACCOUNT_LOCKED, true"         // Đúng ngưỡng khóa
        })
        @DisplayName("Rule 4 & 5: Kiểm tra bộ đếm nhập sai liên tiếp")
        void login_FailedAttemptsBoundary(int failTimes, ResultCode expectedResult, boolean expectedLocked) {
            failLogin(failTimes - 1);
            ResultCode lastResult = service.login(USER, WRONG);

            assertEquals(expectedResult, lastResult);
            assertEquals(expectedLocked, service.isLocked(USER));

            Account acc = service.findByUsername(USER).orElseThrow();
            assertEquals(failTimes, acc.getFailedAttempts());
        }

        // Rule 3: Đang bị khóa thì không tăng thêm bộ đếm
        @Test
        @DisplayName("Rule 3: Tài khoản đang khóa -> ACCOUNT_LOCKED và bộ đếm không đổi")
        void login_LockedAccount_CounterDoesNotIncrease() {
            failLogin(5); // Khóa tài khoản
            Account acc = service.findByUsername(USER).orElseThrow();
            assertEquals(5, acc.getFailedAttempts());

            assertEquals(ResultCode.ACCOUNT_LOCKED, service.login(USER, PASS));
            assertEquals(5, acc.getFailedAttempts());

            assertEquals(ResultCode.ACCOUNT_LOCKED, service.login(USER, WRONG));
            assertEquals(5, acc.getFailedAttempts());
        }

        // Biên: Nhập đúng mật khẩu sau khi sai N lần
        @ParameterizedTest(name = "[{index}] Sai {0} lần rồi đúng -> Kết quả: {1}, IsLocked: {2}")
        @CsvSource({
                "4, SUCCESS, false",        // Sai 4 lần rồi đúng -> SUCCESS, reset bộ đếm về 0
                "5, ACCOUNT_LOCKED, true"   // Sai 5 lần (đã khóa) rồi đúng -> ACCOUNT_LOCKED
        })
        @DisplayName("Kiểm thử nhập đúng mật khẩu sau N lần thất bại")
        void login_CorrectPasswordAfterNFailures(int failures, ResultCode expectedResult, boolean isLocked) {
            failLogin(failures);
            ResultCode result = service.login(USER, PASS);

            assertEquals(expectedResult, result);
            assertEquals(isLocked, service.isLocked(USER));

            if (expectedResult == ResultCode.SUCCESS) {
                Account acc = service.findByUsername(USER).orElseThrow();
                assertEquals(0, acc.getFailedAttempts());
            }
        }

        // Mở khóa Admin
        @Test
        @DisplayName("Admin unlock -> Reset bộ đếm và cho phép đăng nhập lại")
        void login_AfterAdminUnlock_CounterRestarts() {
            failLogin(5);
            assertTrue(service.isLocked(USER));

            assertEquals(ResultCode.SUCCESS, service.unlockAccount(USER));
            assertFalse(service.isLocked(USER));

            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login(USER, WRONG));
            Account acc = service.findByUsername(USER).orElseThrow();
            assertEquals(1, acc.getFailedAttempts());

            assertEquals(ResultCode.SUCCESS, service.login(USER, PASS));
        }

        // Case Sensitivity
        @Test
        @DisplayName("Username không phân biệt hoa/thường, Password CÓ phân biệt hoa/thường")
        void login_CaseSensitivity() {
            assertEquals(ResultCode.SUCCESS, service.login("ALICE_01", PASS));
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login(USER, "secret@123"));
        }

        // Parameter Null / Empty / Blank
        @ParameterizedTest(name = "[{index}] Username = ''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Kiểm tra username null hoặc blank")
        void login_InvalidUsernameInput(String inputUsername) {
            assertEquals(ResultCode.INVALID_INPUT, service.login(inputUsername, PASS));
        }

        @ParameterizedTest(name = "[{index}] Password = ''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Kiểm tra password null hoặc blank")
        void login_InvalidPasswordInput(String inputPassword) {
            assertEquals(ResultCode.INVALID_INPUT, service.login(USER, inputPassword));
        }

        // Phủ các nhánh Admin (disableAccount, unlockAccount) với username không hợp lệ
        @Test
        @DisplayName("disableAccount và unlockAccount với username null/blank/không tồn tại -> USER_NOT_FOUND")
        void admin_InvalidUsernameBranches() {
            assertEquals(ResultCode.USER_NOT_FOUND, service.disableAccount(null));
            assertEquals(ResultCode.USER_NOT_FOUND, service.disableAccount(""));
            assertEquals(ResultCode.USER_NOT_FOUND, service.disableAccount("   "));
            assertEquals(ResultCode.USER_NOT_FOUND, service.disableAccount("not_exist"));

            assertEquals(ResultCode.USER_NOT_FOUND, service.unlockAccount(null));
            assertEquals(ResultCode.USER_NOT_FOUND, service.unlockAccount(""));
            assertEquals(ResultCode.USER_NOT_FOUND, service.unlockAccount("   "));
            assertEquals(ResultCode.USER_NOT_FOUND, service.unlockAccount("not_exist"));
        }

        // Phủ các nhánh helper (findByUsername, isLocked) với username null/blank
        @Test
        @DisplayName("findByUsername và isLocked với username null hoặc blank")
        void helper_NullOrBlankUsername() {
            assertTrue(service.findByUsername(null).isEmpty());
            assertTrue(service.findByUsername("").isEmpty());
            assertTrue(service.findByUsername("   ").isEmpty());

            assertFalse(service.isLocked(null));
            assertFalse(service.isLocked(""));
            assertFalse(service.isLocked("   "));
        }
    }
}