import java.sql.SQLException;

public class Controller {
    private final Model model;
    private final View view;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;

        view.addRegisterListener(e -> registerCustomer());
        view.addCreateAccountListener(e -> createAccount());
        view.addDepositListener(e -> processTransaction("DEPOSIT"));
        view.addWithdrawListener(e -> processTransaction("WITHDRAW"));
        view.addCheckBalanceListener(e -> checkBalance());
        view.addHistoryListener(e -> loadHistory());
    }

    private void registerCustomer() {
        String name = view.getNameText();
        String phone = view.getPhoneText();

        if (name.isEmpty() || phone.isEmpty()) {
            view.showError("Customer name and phone number are required.");
            return;
        }

        try {
            int id = model.registerCustomer(name, phone,
                    view.getEmailText(), view.getAddressText());
            if (id != -1) {
                view.setCustomerId(String.valueOf(id));
                view.showMessage("Customer registered successfully!\nCustomer ID: " + id);
            } else {
                view.showMessage("Customer registered successfully.");
            }
            view.clearCustomerFields();
        } catch (SQLException e) {
            view.showError("Customer registration failed: " + e.getMessage());
        }
    }

    private void createAccount() {
        try {
            int customerId = Integer.parseInt(view.getCustomerIdText());
            double openingBalance = Double.parseDouble(view.getOpeningBalanceText());
            String pin = view.getAccountPin();

            if (openingBalance < 0) {
                view.showError("Opening balance cannot be negative.");
                return;
            }
            if (!pin.matches("[0-9]{4,6}")) {
                view.showError("PIN must contain 4 to 6 digits.");
                return;
            }

            String accountNumber = model.createAccount(customerId, openingBalance, pin);
            view.showMessage("Account created successfully!\nAccount Number: " + accountNumber);
            view.clearAccountFields();
        } catch (NumberFormatException e) {
            view.showError("Enter a valid Customer ID and opening balance.");
        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        } catch (SQLException e) {
            view.showError("Account creation failed: " + e.getMessage());
        }
    }

    private void processTransaction(String type) {
        String accountNumber = view.getTransactionAccount();
        String pin = view.getTransactionPin();

        if (accountNumber.isEmpty() || pin.isEmpty()) {
            view.showError("Enter account number and PIN.");
            return;
        }

        try {
            double amount = Double.parseDouble(view.getTransactionAmount());
            if (amount <= 0) {
                view.showError("Amount must be greater than zero.");
                return;
            }

            double newBalance = model.processTransaction(accountNumber, pin, amount, type);
            view.showMessage(type + " successful!\nTransaction amount: " + amount
                    + "\nNew balance: " + newBalance);
            view.clearTransactionFields();
        } catch (NumberFormatException e) {
            view.showError("Enter a valid amount.");
        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        } catch (SQLException e) {
            view.showError("Transaction failed: " + e.getMessage());
        }
    }

    private void checkBalance() {
        String accountNumber = view.getEnquiryAccount();
        String pin = view.getEnquiryPin();

        if (accountNumber.isEmpty() || pin.isEmpty()) {
            view.showError("Enter account number and PIN.");
            return;
        }

        try {
            String details = model.getBalanceDetails(accountNumber, pin);
            if (details == null) {
                view.setBalanceMessage("Account not found or incorrect PIN.");
            } else {
                String[] parts = details.split("\n", -1);
                view.setBalanceDetails(parts[0], parts[1], parts[2]);
            }
            view.clearEnquiryPin();
        } catch (SQLException e) {
            view.showError("Balance enquiry failed: " + e.getMessage());
        }
    }

    private void loadHistory() {
        String accountNumber = view.getHistoryAccount();
        String pin = view.getHistoryPin();

        view.clearHistory();

        if (accountNumber.isEmpty() || pin.isEmpty()) {
            view.showError("Enter account number and PIN.");
            return;
        }

        try {
            Model.DefaultTableData data =
                    model.getTransactionHistory(accountNumber, pin);

            for (Object[] row : data.getRows()) {
                view.addHistoryRow(row);
            }

            view.clearHistoryPin();
            if (view.getHistoryRowCount() == 0) {
                view.showMessage("No transactions found for this account.");
            }
        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
            view.clearHistoryPin();
        } catch (SQLException e) {
            view.showError("Could not load transaction history: " + e.getMessage());
        }
    }
}
