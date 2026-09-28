package fu.de200027;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class AccountService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int PASSWORD_HISTORY_SIZE = 3;
    public static final int MIN_AGE = 18;

    private final Map<String, Account> accounts;
    private final Map<String, String> emailToUsernameMap;

    public AccountService() {
        this.accounts = new HashMap<>();
        this.emailToUsernameMap = new HashMap<>();
    }

    public ResultCode register(String username, String email, String password,
                               String confirmPassword, LocalDate dateOfBirth, String phone) {
        LocalDate today = LocalDate.now();

        // 1. REG-01: Parameter null, blank hoặc dateOfBirth ở tương lai
        if (isBlank(username) || isBlank(email) || isBlank(password) || isBlank(confirmPassword)
                || dateOfBirth == null || dateOfBirth.isAfter(today)) {
            return ResultCode.INVALID_INPUT;
        }

        // 2. REG-02: Format username không hợp lệ
        if (!AccountValidator.isValidUsername(username)) {
            return ResultCode.INVALID_USERNAME;
        }

        // 3. REG-04: Format email không hợp lệ
        if (!AccountValidator.isValidEmail(email)) {
            return ResultCode.INVALID_EMAIL;
        }

        // 4. REG-06: Mật khẩu yếu
        if (!AccountValidator.isValidPassword(password, username)) {
            return ResultCode.WEAK_PASSWORD;
        }

        // 5. REG-07: Xác nhận mật khẩu không khớp
        if (!password.equals(confirmPassword)) {
            return ResultCode.PASSWORD_MISMATCH;
        }

        // 6. REG-08: Chưa đủ 18 tuổi (dùng calculateAge)
        if (AccountValidator.calculateAge(dateOfBirth, today) < MIN_AGE) {
            return ResultCode.UNDERAGE;
        }

        // 7. REG-09: Số điện thoại (null hoặc "" chấp nhận; "   " là INVALID_PHONE)
        if (phone != null && !phone.isEmpty()) {
            if (!AccountValidator.isValidPhone(phone)) {
                return ResultCode.INVALID_PHONE;
            }
        }

        // 8. REG-03: Trùng username (so sánh bằng key lowercase)
        if (accounts.containsKey(key(username))) {
            return ResultCode.DUPLICATE_USERNAME;
        }

        // 9. REG-05: Trùng email (so sánh bằng key lowercase)
        if (emailToUsernameMap.containsKey(key(email))) {
            return ResultCode.DUPLICATE_EMAIL;
        }

        // 10. REG-10: Đăng ký thành công -> sinh salt, hash pass, lưu map
        String salt = PasswordHasher.generateSalt();
        String passwordHash = PasswordHasher.hash(salt, password);
        Account account = new Account(username, email.toLowerCase(Locale.ROOT), dateOfBirth, phone, salt, passwordHash);

        accounts.put(key(username), account);
        emailToUsernameMap.put(key(email), key(username));

        return ResultCode.SUCCESS;
    }

    public ResultCode login(String username, String password) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode disableAccount(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode unlockAccount(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<Account> findByUsername(String username) {
        if (isBlank(username)) return Optional.empty();
        return Optional.ofNullable(accounts.get(key(username)));
    }

    public boolean isLocked(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode changePassword(String username, String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("TODO");
    }

    public TokenResult requestPasswordReset(String email) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode resetPassword(String token, String newPassword) {
        throw new UnsupportedOperationException("TODO");
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String key(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}