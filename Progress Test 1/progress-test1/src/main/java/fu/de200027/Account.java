package fu.de200027;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private final String username;
    private final String email;
    private final LocalDate dateOfBirth;
    private final String phone;
    private final String salt;
    private AccountStatus status;
    private int failedAttempts;
    private boolean locked;
    private final List<String> passwordHistory;

    public Account(String username, String email, LocalDate dateOfBirth, String phone, String salt, String initialPasswordHash) {
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.salt = salt;
        this.status = AccountStatus.ACTIVE;
        this.failedAttempts = 0;
        this.locked = false;
        this.passwordHistory = new ArrayList<>();
        this.passwordHistory.add(initialPasswordHash);
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; }
    public String getSalt() { return salt; }
    public AccountStatus getStatus() { return status; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return locked; }

    // Các hàm thay đổi trạng thái để package-private theo đúng yêu cầu
    void setStatus(AccountStatus status) { this.status = status; }
    void incrementFailedAttempts() { this.failedAttempts++; }
    void resetFailedAttempts() { this.failedAttempts = 0; }
    void lock() { this.locked = true; }
    void unlock() {
        this.locked = false;
        this.failedAttempts = 0;
    }

    public String getCurrentPasswordHash() {
        return passwordHistory.get(passwordHistory.size() - 1);
    }

    public List<String> getPasswordHistory() {
        return List.copyOf(passwordHistory);
    }
}