import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.List;

public class GuiManager {
    private final FileDatabase db;
    private final DefaultTableModel tableModel;

    public GuiManager(FileDatabase db) {
        this.db = db;
        this.tableModel = new DefaultTableModel(new String[]{"ID", "Identifier", "Name", "Age", "Date of Birth"}, 0);
    }

    public void launch() {
        JFrame frame = new JFrame("File Database");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);

        JTable table = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);

        JButton addButton = new JButton("Add Record");
        JButton searchButton = new JButton("Search");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear DB");
        JButton openButton = new JButton("Open DB");
        JButton saveButton = new JButton("Save DB");

        JPanel panel = new JPanel();
        panel.add(addButton);
        panel.add(searchButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        panel.add(openButton);
        panel.add(saveButton);

        frame.getContentPane().add(BorderLayout.CENTER, tableScroll);
        frame.getContentPane().add(BorderLayout.SOUTH, panel);

        addButton.addActionListener(e -> addRecordDialog());
        searchButton.addActionListener(e -> searchDialog());
        deleteButton.addActionListener(e -> deleteRecordDialog());
        clearButton.addActionListener(e -> clearDatabase());
        openButton.addActionListener(e -> openDatabase());
        saveButton.addActionListener(e -> saveDatabase());

        loadAllRecords();
        frame.setVisible(true);
    }

    private void addRecordDialog() {
        JTextField identifierField = new JTextField(5);
        JTextField nameField = new JTextField(10);
        JTextField ageField = new JTextField(5);
        JTextField dobField = new JTextField(10);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Identifier:"));
        panel.add(identifierField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Age:"));
        panel.add(ageField);
        panel.add(new JLabel("Date of Birth (DD.MM.YYYY):"));
        panel.add(dobField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Record", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int identifier;
                try {
                    identifier = Integer.parseInt(identifierField.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Identifier must be an integer.");
                }

                if (identifier <= 0) throw new IllegalArgumentException("Identifier must be positive.");
                if (db.isIdentifierExists(identifier)) throw new IllegalArgumentException("Identifier must be unique.");

                String name = nameField.getText().trim();
                if (name.isEmpty()) throw new IllegalArgumentException("Name cannot be empty.");

                int age;
                try {
                    age = Integer.parseInt(ageField.getText().trim());
                    if (age <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Age must be a positive integer.");
                }

                String dob = dobField.getText().trim();
                if (!dob.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
                    throw new IllegalArgumentException("Date of Birth must be in format DD.MM.YYYY.");
                }

                int newId = db.getNextId();
                String record = String.join(",", String.valueOf(newId), String.valueOf(identifier), name, String.valueOf(age), dob);

                db.addRecord(String.valueOf(identifier), record);
                JOptionPane.showMessageDialog(null, "Record added!");
                loadAllRecords();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Unexpected error: " + ex.getMessage());
            }
        }
    }

    private void searchDialog() {
        String field = JOptionPane.showInputDialog("Enter field name (e.g., Name, Age, Identifier):");
        String value = JOptionPane.showInputDialog("Enter value to search:");
        if (field != null && value != null && !field.isEmpty() && !value.isEmpty()) {
            try {
                List<String> results = db.searchByField(field, value);
                if (results.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No records found.");
                } else {
                    StringBuilder resultString = new StringBuilder("Search results:\n");
                    for (String record : results) {
                        resultString.append(record).append("\n");
                    }
                    JOptionPane.showMessageDialog(null, resultString.toString());
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        }
    }

    private void deleteRecordDialog() {
        String field = JOptionPane.showInputDialog("Enter field name to delete by (e.g., Name, Age, Identifier):");
        String value = JOptionPane.showInputDialog("Enter value to delete:");
        if (field != null && value != null && !field.isEmpty() && !value.isEmpty()) {
            try {
                db.deleteByField(field, value);
                JOptionPane.showMessageDialog(null, "Record(s) deleted!");
                loadAllRecords();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        }
    }

    private void clearDatabase() {
        try {
            db.clearDatabase();
            JOptionPane.showMessageDialog(null, "Database cleared!");
            loadAllRecords();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    private void openDatabase() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                db.openDatabase(fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(null, "Database opened!");
                loadAllRecords();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    private void saveDatabase() {
        try {
            db.saveDatabase();
            JOptionPane.showMessageDialog(null, "Database saved!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    private void loadAllRecords() {
        try {
            tableModel.setRowCount(0);
            List<String> records = db.getAllRecords();
            for (String record : records) {
                tableModel.addRow(record.split(","));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading records: " + e.getMessage());
        }
    }
}
