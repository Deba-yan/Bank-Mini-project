import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class View extends JFrame {
    private final JTextField nameField = new JTextField(18);
    private final JTextField phoneField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField addressField = new JTextField(18);

    private final JPasswordField accountPinField = new JPasswordField(18);
    private final JPasswordField transactionPinField = new JPasswordField(18);
    private final JPasswordField enquiryPinField = new JPasswordField(18);
    private final JPasswordField historyPinField = new JPasswordField(18);

    private final JTextField customerIdField = new JTextField(18);
    private final JTextField openingBalanceField = new JTextField(18);
    private final JTextField transactionAccountField = new JTextField(18);
    private final JTextField transactionAmountField = new JTextField(18);
    private final JTextField enquiryAccountField = new JTextField(18);
    private final JTextField historyAccountField = new JTextField(18);

    private final JLabel balanceLabel =
            new JLabel("Enter an account number and click Check Balance.");

    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"Transaction ID", "Account No.", "Type",
                    "Amount", "Date & Time", "Details"}, 0) {
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JButton registerButton = new JButton("Register Customer");
    private final JButton createAccountButton = new JButton("Create Savings Account");
    private final JButton depositButton = new JButton("Deposit");
    private final JButton withdrawButton = new JButton("Withdraw");
    private final JButton checkBalanceButton = new JButton("Check Balance");
    private final JButton historyButton = new JButton("Show History");

    public View() {
        setTitle("Banking Management System");
        setSize(900, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customer Registration", createCustomerPanel());
        tabs.addTab("Create Account", createAccountPanel());
        tabs.addTab("Deposit / Withdraw", createTransactionPanel());
        tabs.addTab("Balance Enquiry", createBalancePanel());
        tabs.addTab("Transaction History", createHistoryPanel());
        add(tabs);
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = constraints();

        addField(panel, g, 0, "Customer Name:", nameField);
        addField(panel, g, 1, "Phone Number:", phoneField);
        addField(panel, g, 2, "Email:", emailField);
        addField(panel, g, 3, "Address:", addressField);

        g.gridx = 1;
        g.gridy = 4;
        g.anchor = GridBagConstraints.WEST;
        panel.add(registerButton, g);
        return panel;
    }

    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = constraints();

        addField(panel, g, 0, "Customer ID:", customerIdField);
        addField(panel, g, 1, "Opening Balance:", openingBalanceField);
        addField(panel, g, 2, "Create PIN:", accountPinField);

        g.gridx = 1;
        g.gridy = 3;
        g.anchor = GridBagConstraints.WEST;
        panel.add(createAccountButton, g);

        JLabel note = new JLabel(
                "Register the customer first. The Customer ID is shown after registration.");
        g.gridx = 0;
        g.gridy = 4;
        g.gridwidth = 2;
        panel.add(note, g);
        return panel;
    }

    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = constraints();

        addField(panel, g, 0, "Account Number:", transactionAccountField);
        addField(panel, g, 1, "PIN:", transactionPinField);
        addField(panel, g, 2, "Amount:", transactionAmountField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(depositButton);
        buttons.add(withdrawButton);

        g.gridx = 1;
        g.gridy = 3;
        g.anchor = GridBagConstraints.WEST;
        panel.add(buttons, g);
        return panel;
    }

    private JPanel createBalancePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = constraints();

        addField(panel, g, 0, "Account Number:", enquiryAccountField);
        addField(panel, g, 1, "PIN:", enquiryPinField);

        g.gridx = 1;
        g.gridy = 2;
        g.anchor = GridBagConstraints.WEST;
        panel.add(checkBalanceButton, g);

        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        panel.add(balanceLabel, g);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        top.add(new JLabel("Account Number:"));
        top.add(historyAccountField);
        top.add(new JLabel("PIN:"));
        top.add(historyPinField);
        top.add(historyButton);

        JTable table = new JTable(historyModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private GridBagConstraints constraints() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        return g;
    }

    private void addField(JPanel panel, GridBagConstraints g,
                          int row, String label, JTextField field) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        g.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), g);

        g.gridx = 1;
        g.anchor = GridBagConstraints.WEST;
        panel.add(field, g);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Banking System", JOptionPane.ERROR_MESSAGE);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public String getNameText() { return nameField.getText().trim(); }
    public String getPhoneText() { return phoneField.getText().trim(); }
    public String getEmailText() { return emailField.getText().trim(); }
    public String getAddressText() { return addressField.getText().trim(); }
    public String getCustomerIdText() { return customerIdField.getText().trim(); }
    public String getOpeningBalanceText() { return openingBalanceField.getText().trim(); }
    public String getAccountPin() { return new String(accountPinField.getPassword()); }
    public String getTransactionAccount() { return transactionAccountField.getText().trim(); }
    public String getTransactionPin() { return new String(transactionPinField.getPassword()); }
    public String getTransactionAmount() { return transactionAmountField.getText().trim(); }
    public String getEnquiryAccount() { return enquiryAccountField.getText().trim(); }
    public String getEnquiryPin() { return new String(enquiryPinField.getPassword()); }
    public String getHistoryAccount() { return historyAccountField.getText().trim(); }
    public String getHistoryPin() { return new String(historyPinField.getPassword()); }

    public void setCustomerId(String id) { customerIdField.setText(id); }
    public void clearCustomerFields() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
    }
    public void clearAccountFields() {
        openingBalanceField.setText("");
        accountPinField.setText("");
    }
    public void clearTransactionFields() {
        transactionAmountField.setText("");
        transactionPinField.setText("");
    }
    public void clearEnquiryPin() { enquiryPinField.setText(""); }
    public void clearHistoryPin() { historyPinField.setText(""); }

    public void setBalanceDetails(String name, String accountType, String balance) {
        balanceLabel.setText("<html>Customer: " + escapeHtml(name)
                + "<br>Account Type: " + escapeHtml(accountType)
                + "<br>Available Balance: ₹" + balance + "</html>");
    }

    public void setBalanceMessage(String message) {
        balanceLabel.setText(message);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public void clearHistory() {
        historyModel.setRowCount(0);
    }

    public void addHistoryRow(Object[] row) {
        historyModel.addRow(row);
    }

    public int getHistoryRowCount() {
        return historyModel.getRowCount();
    }

    public void addRegisterListener(java.awt.event.ActionListener listener) {
        registerButton.addActionListener(listener);
    }
    public void addCreateAccountListener(java.awt.event.ActionListener listener) {
        createAccountButton.addActionListener(listener);
    }
    public void addDepositListener(java.awt.event.ActionListener listener) {
        depositButton.addActionListener(listener);
    }
    public void addWithdrawListener(java.awt.event.ActionListener listener) {
        withdrawButton.addActionListener(listener);
    }
    public void addCheckBalanceListener(java.awt.event.ActionListener listener) {
        checkBalanceButton.addActionListener(listener);
    }
    public void addHistoryListener(java.awt.event.ActionListener listener) {
        historyButton.addActionListener(listener);
    }
}
