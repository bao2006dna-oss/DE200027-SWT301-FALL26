package fu.de200027;

import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import java.util.regex.Pattern;

public final class AccountValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{4,19}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[35789]\\d{8}$");
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=";

    private AccountValidator() {}

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.length() <= 100 && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password, String username) {
        if (password == null || password.length() < 8 || password.length() > 32) {
            return false;
        }

        boolean upper = false, lower = false, digit = false, special = false;
        for (char c : password.toCharArray()) {
            if (c >= 'A' && c <= 'Z') upper = true;
            else if (c >= 'a' && c <= 'z') lower = true;
            else if (c >= '0' && c <= '9') digit = true;
            else if (SPECIAL_CHARS.indexOf(c) >= 0) special = true;
            else return false; // Khoảng trắng hoặc ký tự lạ
        }

        // Bắt buộc đủ cả 4 nhóm ký tự
        if (!(upper && lower && digit && special)) {
            return false;
        }

        // Kiểm tra không chứa username (bỏ qua nếu username null hoặc blank)
        if (username != null && !username.isBlank()) {
            if (password.toLowerCase(Locale.ROOT).contains(username.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static int calculateAge(LocalDate dob, LocalDate today) {
        if (dob == null || today == null) {
            return 0;
        }
        return Period.between(dob, today).getYears();
    }
}