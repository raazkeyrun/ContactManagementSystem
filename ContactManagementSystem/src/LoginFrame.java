/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Raaz
 */
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private Connection connection;

    public LoginFrame() {
        setTitle("Login Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null); // Center the frame

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);

        add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (validateUser(username, password)) {
                    openDashboard();
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private boolean validateUser(String username, String password) {
        String url = "jdbc:mysql://localhost:3306/sms";
        String user = "root";
        String pass = "";

        try {
            connection = DriverManager.getConnection(url, user, pass);
            String query = "SELECT * FROM pass WHERE username=? AND password=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void openDashboard() {
        DashboardFrame dashboard = new DashboardFrame(connection);
        dashboard.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}

class DashboardFrame extends JFrame {
    private Connection connection;
    private JTextField idField, nameField, addressField, phoneField;
    private JButton addButton, updateButton, deleteButton, searchButton;
    private JTable table;
    private DefaultTableModel tableModel;

    public DashboardFrame(Connection connection) {
        this.connection = connection;

        setTitle("Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new GridLayout(2, 6, 5, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel idLabel = new JLabel("ID:");
        idField = new JTextField();
        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();
        JLabel addressLabel = new JLabel("Address:");
        addressField = new JTextField();
        JLabel phoneLabel = new JLabel("Phone:");
        phoneField = new JTextField();

        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        searchButton = new JButton("Search");

        topPanel.add(idLabel);
        topPanel.add(idField);
        topPanel.add(nameLabel);
        topPanel.add(nameField);
        topPanel.add(addressLabel);
        topPanel.add(addressField);
        topPanel.add(phoneLabel);
        topPanel.add(phoneField);
        topPanel.add(addButton);
        topPanel.add(updateButton);
        topPanel.add(deleteButton);
        topPanel.add(searchButton);

        tableModel = new DefaultTableModel(new String[] { "ID", "Name", "Address", "Phone" }, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        createContactsTable(); // Ensure the contacts table exists in the database

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addCustomer();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateCustomer();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteCustomer();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchCustomer();
            }
        });

        fetchCustomers(); // Fetch existing customers from the database
    }

    private void createContactsTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS contacts (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "name VARCHAR(255) NOT NULL," +
                         "address VARCHAR(255)," +
                         "phone VARCHAR(20)" +
                         ")";
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void fetchCustomers() {
        try {
            String sql = "SELECT * FROM contacts";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String address = resultSet.getString("address");
                String phone = resultSet.getString("phone");

                Object[] row = { id, name, address, phone };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addCustomer() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String sql = "INSERT INTO contacts (name, address, phone) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setString(2, address);
            statement.setString(3, phone);
            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Customer added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                tableModel.setRowCount(0); // Clear table before reloading
                fetchCustomers(); // Reload data
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateCustomer() {
        String idText = idField.getText().trim();
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();

        if (idText.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID and Name are required for updating!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            String sql = "UPDATE contacts SET name=?, address=?, phone=? WHERE id=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setString(2, address);
            statement.setString(3, phone);
            statement.setInt(4, id);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Customer updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                tableModel.setRowCount(0); // Clear table before reloading
                fetchCustomers(); // Reload data
            } else {
                JOptionPane.showMessageDialog(this, "Customer not found with ID: " + id, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void deleteCustomer() {
        String idText = idField.getText().trim();

        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID is required for deletion!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            String sql = "DELETE FROM contacts WHERE id=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                tableModel.setRowCount(0); // Clear table before reloading
                fetchCustomers(); // Reload data
            } else {
                JOptionPane.showMessageDialog(this, "Customer not found with ID: " + id, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void searchCustomer() {
        String searchText = idField.getText().trim();
        if (searchText.isEmpty()) {
            searchText = nameField.getText().trim();
        }
        if (searchText.isEmpty()) {
            searchText = addressField.getText().trim();
        }
        if (searchText.isEmpty()) {
            searchText = phoneField.getText().trim();
        }

        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a value to search!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String sql = "SELECT * FROM contacts WHERE id=? OR name=? OR address=? OR phone=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, searchText);
            statement.setString(2, searchText);
            statement.setString(3, searchText);
            statement.setString(4, searchText);
            ResultSet resultSet = statement.executeQuery();

            tableModel.setRowCount(0); // Clear table before loading new data

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String address = resultSet.getString("address");
                String phone = resultSet.getString("phone");

                Object[] row = { id, name, address, phone };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        addressField.setText("");
        phoneField.setText("");
    }
}
