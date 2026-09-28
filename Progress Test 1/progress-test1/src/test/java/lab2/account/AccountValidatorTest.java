package lab2.account;


import fu.de200027.AccountValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AccountValidatorTest {

    // --- 1. USERNAME TESTS ---

    @ParameterizedTest(name = "[{index}] username hợp lệ: {0}")
    @ValueSource(strings = {"alice", "Alice_01", "Z____"})
    void isValidUsername_ValidInputs(String username) {
        assertTrue(AccountValidator.isValidUsername(username));
    }

    @ParameterizedTest(name = "[{index}] username không hợp lệ: {0}")
    @ValueSource(strings = {"ab_1", "1alice", "_alice", "ali ce", "alice!", "alice-01", " "})
    @NullAndEmptySource
    void isValidUsername_InvalidInputs(String username) {
        assertFalse(AccountValidator.isValidUsername(username));
    }

    @ParameterizedTest(name = "[{index}] username độ dài {0} -> expected={1}")
    @MethodSource("usernameLengths")
    void isValidUsername_BoundaryLengths(int length, boolean expected) {
        assertEquals(expected, AccountValidator.isValidUsername("a".repeat(length)));
    }

    static Stream<Arguments> usernameLengths() {
        return Stream.of(
                Arguments.of(4, false),
                Arguments.of(5, true),
                Arguments.of(6, true),
                Arguments.of(19, true),
                Arguments.of(20, true),
                Arguments.of(21, false)
        );
    }

    // --- 2. EMAIL TESTS ---

    @ParameterizedTest(name = "[{index}] email={0} -> expected={1}")
    @CsvSource({
            "user@domain.com, true",
            "user.name@sub.domain.org, true",
            "user+tag@domain.co.uk, true",
            "user@domain, false",
            "@domain.com, false",
            "user@, false",
            "user@.com, false",
            "user@domain..com, false"
    })
    void isValidEmail_Partitions(String email, boolean expected) {
        assertEquals(expected, AccountValidator.isValidEmail(email));
    }

    @ParameterizedTest(name = "[{index}] email null hoặc rỗng -> false")
    @NullAndEmptySource
    void isValidEmail_NullAndEmpty(String email) {
        assertFalse(AccountValidator.isValidEmail(email));
    }

    @ParameterizedTest(name = "[{index}] email độ dài {0} -> expected={1}")
    @MethodSource("emailLengths")
    void isValidEmail_BoundaryLengths(int length, boolean expected) {
        String prefix = "a@b.";
        String email = prefix + "c".repeat(length - prefix.length());
        assertEquals(expected, AccountValidator.isValidEmail(email));
    }

    static Stream<Arguments> emailLengths() {
        return Stream.of(
                Arguments.of(99, true),
                Arguments.of(100, true),
                Arguments.of(101, false)
        );
    }

    // --- 3. PASSWORD TESTS ---

    @ParameterizedTest(name = "[{index}] {3}")
    @CsvSource(delimiter = '|', value = {
            "Secret@123    | alice_01 | true  | hợp lệ",
            "secret@123    | alice_01 | false | thiếu chữ hoa",
            "SECRET@123    | alice_01 | false | thiếu chữ thường",
            "SecretPass    | alice_01 | false | thiếu chữ số và ký tự đặc biệt",
            "Secret1234    | alice_01 | false | thiếu ký tự đặc biệt",
            "'Secret @123' | alice_01 | false | chứa khoảng trắng",
            "Xalice_01@1   | alice_01 | false | chứa username",
            "Xalice_01@1   |          | true  | username null -> bỏ qua"
    })
    void isValidPassword_Partitions(String pw, String user, boolean expected, String desc) {
        assertEquals(expected, AccountValidator.isValidPassword(pw, user));
    }

    @ParameterizedTest(name = "[{index}] password độ dài {0} -> expected={1}")
    @MethodSource("passwordLengths")
    void isValidPassword_BoundaryLengths(int length, boolean expected) {
        String password = "A1@" + "a".repeat(Math.max(0, length - 3));
        assertEquals(expected, AccountValidator.isValidPassword(password, "alice_01"));
    }

    static Stream<Arguments> passwordLengths() {
        return Stream.of(
                Arguments.of(7, false),
                Arguments.of(8, true),
                Arguments.of(32, true),
                Arguments.of(33, false)
        );
    }

    // --- 4. PHONE TESTS ---

    @ParameterizedTest(name = "[{index}] phone hợp lệ: {0}")
    @ValueSource(strings = {"0312345678", "0512345678", "0712345678", "0812345678", "0912345678"})
    void isValidPhone_ValidInputs(String phone) {
        assertTrue(AccountValidator.isValidPhone(phone));
    }

    @ParameterizedTest(name = "[{index}] phone không hợp lệ: {0}")
    @ValueSource(strings = {"0123456789", "090123456", "09012345678", "abc1234567"})
    @NullAndEmptySource
    void isValidPhone_InvalidInputs(String phone) {
        assertFalse(AccountValidator.isValidPhone(phone));
    }

    // --- 5. AGE TESTS ---

    @ParameterizedTest(name = "[{index}] sinh {0}, hôm nay {1} -> {2} tuổi")
    @CsvSource({
            "2008-09-28, 2026-09-28, 18",
            "2008-09-29, 2026-09-28, 17",
            "2008-02-29, 2026-02-28, 17",
            "2008-02-29, 2026-03-01, 18"
    })
    void calculateAge_Boundaries(LocalDate dob, LocalDate today, int expected) {
        assertEquals(expected, AccountValidator.calculateAge(dob, today));
    }
}