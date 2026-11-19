import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Model {
    private static final String DB_URL = "jdbc:sqlite:banking.db";

    public Model() {
        initializeDatabase();
    }

    private Connection connect() throws SQLException {
        Connection con = DriverManager.getConnection(DB_URL);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return con;
    }

    private void initializeDatabase() {
        String customers = """
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    email TEXT,
                    address TEXT
                )
                """;

        String accounts = """
                CREATE TABLE IF NOT EXISTS accounts (
                    account_number TEXT PRIMARY KEY,
                    customer_id INTEGER NOT NULL,
                    account_type TEXT NOT NULL,
                    balance REAL NOT NULL CHECK(balance >= 0),
                    pin TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY(customer_id) REFERENCES customers(customer_id)
                )
                """;

        String transactions = """
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    account_number TEXT NOT NULL,
                    transaction_type TEXT NOT NULL,
                    amount REAL NOT NULL CHECK(amount > 0),
                    transaction_date TEXT NOT NULL,
                    details TEXT,
                    FOREIGN KEY(account_number) REFERENCES accounts(account_number)
                )
                """;

        try (Connection con = connect(); Statement st = con.createStatement()) {
            st.execute(customers);
            st.execute(accounts);
            st.execute(transactions);

            boolean pinExists = false;
            try (ResultSet rs = st.executeQuery("PRAGMA table_info(accounts)")) {
                while (rs.next()) {
                    if (rs.getString("name").equalsIgnoreCase("pin")) {
                        pinExists = true;
                        break;
                    }
                }
            }

            if (!pinExists) {
                st.execute("ALTER TABLE accounts ADD COLUMN pin TEXT NOT NULL DEFAULT ''");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database: " + e.getMessage(), e);
        }
    }

    public int registerCustomer(String name, String phone, String email, String address)
            throws SQLException {
        String sql = """
                INSERT INTO customers(name, phone, email, address)
                VALUES(?,?,?,?)
                """;

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.setString(4, address);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public String createAccount(int customerId, double openingBalance, String pin)
            throws SQLException {
        String accountNumber = String.valueOf(1000000000 + new Random().nextInt(9000));

        String sql = """
                INSERT INTO accounts
                (account_number, customer_id, account_type, balance, pin, created_at)
                VALUES(?,?,'SAVINGS',?,?,?)
                """;

        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement check = con.prepareStatement(
                        "SELECT customer_id FROM customers WHERE customer_id=?")) {
                    check.setInt(1, customerId);
                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Customer ID does not exist. Register the customer first.");
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, accountNumber);
                    ps.setInt(2, customerId);
                    ps.setDouble(3, openingBalance);
                    ps.setString(4, pin);
                    ps.setString(5, now());
                    ps.executeUpdate();
                }

                if (openingBalance > 0) {
                    insertTransaction(con, accountNumber, "DEPOSIT",
                            openingBalance, "Opening balance");
                }

                con.commit();
                return accountNumber;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private boolean verifyPin(Connection con, String accountNumber, String enteredPin)
            throws SQLException {
        String sql = "SELECT pin FROM accounts WHERE account_number=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getString("pin").equals(enteredPin);
            }
        }
    }

    public double processTransaction(String accountNumber, String pin,
                                     double amount, String type) throws SQLException {
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                double balance;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT balance, pin FROM accounts WHERE account_number=?")) {
                    ps.setString(1, accountNumber);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Account not found.");
                        }
                        if (!rs.getString("pin").equals(pin)) {
                            throw new IllegalArgumentException("Incorrect PIN.");
                        }
                        balance = rs.getDouble("balance");
                    }
                }

                if (type.equals("WITHDRAW") && amount > balance) {
                    throw new IllegalArgumentException(
                            "Insufficient balance. Available balance: " + balance);
                }

                double newBalance = type.equals("DEPOSIT")
                        ? balance + amount : balance - amount;

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET balance=? WHERE account_number=?")) {
                    ps.setDouble(1, newBalance);
                    ps.setString(2, accountNumber);
                    ps.executeUpdate();
                }

                insertTransaction(con, accountNumber, type, amount,
                        type.equals("DEPOSIT") ? "Cash deposit" : "Cash withdrawal");
                con.commit();
                return newBalance;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private void insertTransaction(Connection con, String accountNumber,
                                   String type, double amount, String details)
            throws SQLException {
        String sql = """
                INSERT INTO transactions
                (account_number, transaction_type, amount, transaction_date, details)
                VALUES(?,?,?,?,?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.setString(4, now());
            ps.setString(5, details);
            ps.executeUpdate();
        }
    }

    public String getBalanceDetails(String accountNumber, String pin) throws SQLException {
        String sql = """
                SELECT c.name, a.balance, a.account_type
                FROM accounts a
                JOIN customers c ON a.customer_id=c.customer_id
                WHERE a.account_number=? AND a.pin=?
                """;

        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, pin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name") + "\n"
                            + rs.getString("account_type") + "\n"
                            + String.format("%.2f", rs.getDouble("balance"));
                }
            }
        }
        return null;
    }

    public DefaultTableData getTransactionHistory(String accountNumber, String pin)
            throws SQLException {
        try (Connection con = connect()) {
            if (!verifyPin(con, accountNumber, pin)) {
                throw new IllegalArgumentException("Account not found or incorrect PIN.");
            }

            DefaultTableData data = new DefaultTableData();
            String sql = """
                    SELECT transaction_id, account_number, transaction_type,
                           amount, transaction_date, details
                    FROM transactions
                    WHERE account_number=?
                    ORDER BY transaction_id DESC
                    """;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, accountNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        data.addRow(new Object[]{
                                rs.getInt("transaction_id"),
                                rs.getString("account_number"),
                                rs.getString("transaction_type"),
                                String.format("%.2f", rs.getDouble("amount")),
                                rs.getString("transaction_date"),
                                rs.getString("details")
                        });
                    }
                }
            }
            return data;
        }
    }

    private String now() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Small data holder so the Model does not depend on Swing table classes.
    public static class DefaultTableData {
        private final List<Object[]> rows = new ArrayList<>();

        public void addRow(Object[] row) {
            rows.add(row);
        }

        public List<Object[]> getRows() {
            return rows;
        }

        public int getRowCount() {
            return rows.size();
        }
    }
}
