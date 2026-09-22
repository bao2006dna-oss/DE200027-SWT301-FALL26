package DinhGiaBao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử dịch vụ AccountService")
class AccountServiceTest {

    private AccountService service;

    @BeforeEach
    void setUp() {
        // Arrange chung cho mọi test
        service = new AccountService();
    }

    @Nested
    @DisplayName("1. Kiểm thử phương thức isValidEmail")
    class IsValidEmailTest {

        @ParameterizedTest(name = "Email hợp lệ: {0}")
        @ValueSource(strings = {
                "john@example.com",
                "alice.b@mail.co.uk",
                "carol_99@domain.io"
        })
        @DisplayName("Trả về true với danh sách email đúng định dạng")
        void isValidEmail_ValidEmails_ReturnsTrue(String email) {
            // Act
            boolean result = service.isValidEmail(email);

            // Assert
            assertTrue(result, () -> "Email hợp lệ phải trả về true: " + email);
        }

        @ParameterizedTest(name = "Email sai định dạng: \"{0}\"")
        @CsvSource(value = {
                "bobmail.com",     // thiếu @
                "missing@dot",    // thiếu .domain
                "'@nodomain.com'",// thiếu local part
                "' '"             // chỉ chứa khoảng trắng
        })
        @DisplayName("Trả về false với email sai định dạng")
        void isValidEmail_InvalidEmails_ReturnsFalse(String email) {
            // Act
            boolean result = service.isValidEmail(email);

            // Assert
            assertFalse(result, () -> "Email sai định dạng phải trả về false: " + email);
        }

        @ParameterizedTest(name = "Email rỗng hoặc null: \"{0}\"")
        @NullAndEmptySource
        @DisplayName("Trả về false khi email null hoặc rỗng")
        void isValidEmail_NullOrEmpty_ReturnsFalse(String email) {
            // Act & Assert
            assertFalse(service.isValidEmail(email));
        }
    }

    @Nested
    @DisplayName("2. Kiểm thử phương thức registerAccount")
    class RegisterAccountTest {

        @ParameterizedTest(name = "Dòng {index}: ({0}, {1}, {2}) → {3}")
        @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
        @DisplayName("Đăng ký tài khoản với các bộ dữ liệu từ test-data.csv")
        void registerAccount_FromCsv(String username, String password, String email, boolean expected) {
            // Act
            boolean actual = service.registerAccount(username, password, email);

            // Assert
            assertEquals(expected, actual,
                    () -> String.format("Bộ dữ liệu (%s, %s, %s) phải trả về %s", username, password, email, expected));
        }

        @Test
        @DisplayName("Password đúng 6 ký tự (giá trị biên dưới) → false")
        void registerAccount_PasswordExactly6_ReturnsFalse() {
            // Arrange
            String username = "bob";
            String password = "abcdef"; // 6 ký tự
            String email = "bob@mail.com";

            // Act
            boolean actual = service.registerAccount(username, password, email);

            // Assert
            assertFalse(actual, "Password <= 6 ký tự phải không hợp lệ");
        }

        @Test
        @DisplayName("Password đúng 7 ký tự (giá trị biên trên) → true")
        void registerAccount_PasswordExactly7_ReturnsTrue() {
            // Arrange
            String username = "bob";
            String password = "abcdefg"; // 7 ký tự
            String email = "bob@mail.com";

            // Act
            boolean actual = service.registerAccount(username, password, email);

            // Assert
            assertTrue(actual, "Password > 6 ký tự (7 ký tự) phải hợp lệ");
        }

        @Test
        @DisplayName("Tất cả tham số truyền vào đều null → false")
        void registerAccount_AllNull_ReturnsFalse() {
            // Act & Assert
            assertFalse(service.registerAccount(null, null, null));
        }
    }
}