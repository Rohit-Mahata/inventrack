package com.inventory.ui.panels;

import com.inventory.dao.UserDAO;
import com.inventory.dao.SecurityQuestionDAO;
import com.inventory.model.User;
import com.inventory.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class UserPanel extends JPanel {

    private UserDAO userDAO = new UserDAO();
    private SecurityQuestionDAO securityDAO = new SecurityQuestionDAO();
    private JTable userTable;
    private DefaultTableModel tableModel;

    private static final Color BG        = new Color(26, 26, 46);
    private static final Color CARD_BG   = new Color(22, 33, 62);
    private static final Color BORDER    = new Color(42, 42, 74);
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color MUTED     = new Color(100, 116, 139);
    private static final Color INDIGO    = new Color(79, 70, 229);
    private static final Color GREEN     = new Color(34, 197, 94);
    private static final Color RED       = new Color(239, 68, 68);
    private static final Color TABLE_ALT = new Color(30, 41, 59);

    public UserPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BG);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("User Management");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Admin only — manage user accounts and roles");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(subtitle);

        // Action bar
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBar.setBackground(BG);
        actionBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JButton addBtn     = createButton("+ Add User", GREEN);
        JButton editBtn    = createButton("Edit Selected", INDIGO);
        JButton deleteBtn  = createButton("Delete Selected", RED);
        JButton refreshBtn = createButton("Refresh", MUTED);

        addBtn.addActionListener(e -> showUserDialog(null));
        editBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                List<User> users = userDAO.getAllUsers();
                users.stream().filter(u -> u.getId() == id)
                    .findFirst().ifPresent(u -> showUserDialog(u));
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user to edit.");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                int id       = (int) tableModel.getValueAt(row, 0);
                String uname = (String) tableModel.getValueAt(row, 1);

                if (uname.equals("admin")) {
                    JOptionPane.showMessageDialog(this, "Cannot delete the main admin account!");
                    return;
                }
                if (id == SessionManager.getCurrentUser().getId()) {
                    JOptionPane.showMessageDialog(this, "Cannot delete your own account!");
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete user: " + uname + "?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    userDAO.deleteUser(id);
                    loadUsers();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            }
        });

        refreshBtn.addActionListener(e -> loadUsers());

        actionBar.add(refreshBtn);
        actionBar.add(editBtn);
        actionBar.add(deleteBtn);
        actionBar.add(addBtn);

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG);
        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(actionBar,  BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Username", "Role", "Sync Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        userTable = new JTable(tableModel);
        userTable.setBackground(CARD_BG);
        userTable.setForeground(TEXT);
        userTable.setFont(new Font("Arial", Font.PLAIN, 13));
        userTable.setRowHeight(38);
        userTable.setShowGrid(false);
        userTable.setIntercellSpacing(new Dimension(0, 0));
        userTable.getTableHeader().setBackground(new Color(30, 41, 59));
        userTable.getTableHeader().setForeground(MUTED);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        userTable.setSelectionBackground(INDIGO);
        userTable.setSelectionForeground(Color.WHITE);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(40);

        userTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? INDIGO : (row % 2 == 0 ? CARD_BG : TABLE_ALT));
                if (!sel && col == 2) {
                    String role = val != null ? val.toString() : "";
                    setForeground(role.equals("admin") ? INDIGO : GREEN);
                } else {
                    setForeground(sel ? Color.WHITE : TEXT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(userTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBackground(CARD_BG);

        add(topPanel, BorderLayout.NORTH);
        add(scroll,   BorderLayout.CENTER);

        loadUsers();
    }

    private void showUserDialog(User existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit User" : "Add User", true);
        dialog.setSize(500, isEdit ? 300 : 560);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(CARD_BG);
        dialog.setLayout(new BorderLayout());

        // Scrollable form panel
        JPanel form = new JPanel();
        form.setBackground(CARD_BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));

        // Username
        form.add(createFormLabel("Username:"));
        form.add(Box.createVerticalStrut(4));
        JTextField usernameField = createFormField(isEdit ? existing.getUsername() : "");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(12));

        // Password
        form.add(createFormLabel("Password:"));
        form.add(Box.createVerticalStrut(4));
        JPasswordField passwordField = new JPasswordField(isEdit ? existing.getPassword() : "");
        passwordField.setBackground(new Color(15, 23, 42));
        passwordField.setForeground(TEXT);
        passwordField.setCaretColor(TEXT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 13));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(12));

        // Role
        form.add(createFormLabel("Role:"));
        form.add(Box.createVerticalStrut(4));
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"staff", "admin"});
        roleCombo.setBackground(new Color(15, 23, 42));
        roleCombo.setForeground(TEXT);
        roleCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (isEdit) roleCombo.setSelectedItem(existing.getRole());
        form.add(roleCombo);

        // Security questions (for new users and edit)
        JTextField[] answerFields = new JTextField[SecurityQuestionDAO.QUESTIONS.length];

        form.add(Box.createVerticalStrut(20));
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        form.add(sep);
        form.add(Box.createVerticalStrut(12));

        JLabel sqTitle = new JLabel("Security Questions (for password recovery)");
        sqTitle.setFont(new Font("Arial", Font.BOLD, 13));
        sqTitle.setForeground(new Color(245, 158, 11));
        sqTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sqTitle);
        form.add(Box.createVerticalStrut(8));

        // Load existing answers if editing
        java.util.Map<String, String> existingQA = null;
        if (isEdit) {
            existingQA = securityDAO.getQuestionsByUserId(existing.getId());
        }

        for (int i = 0; i < SecurityQuestionDAO.QUESTIONS.length; i++) {
            JLabel qLabel = createFormLabel((i + 1) + ". " + SecurityQuestionDAO.QUESTIONS[i]);
            qLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(qLabel);
            form.add(Box.createVerticalStrut(4));

            String existingAnswer = "";
            if (existingQA != null) {
                existingAnswer = existingQA.getOrDefault(SecurityQuestionDAO.QUESTIONS[i], "");
            }
            answerFields[i] = createFormField(existingAnswer);
            answerFields[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            answerFields[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(answerFields[i]);
            form.add(Box.createVerticalStrut(10));
        }

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.getViewport().setBackground(CARD_BG);
        formScroll.setBackground(CARD_BG);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        JButton saveBtn = createButton(isEdit ? "Update" : "Save", INDIGO);
        saveBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role     = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and password cannot be empty.");
                return;
            }

            if (!isEdit && userDAO.usernameExists(username)) {
                JOptionPane.showMessageDialog(dialog, "Username already exists!");
                return;
            }

            // Validate security questions — all must be answered
            for (int i = 0; i < answerFields.length; i++) {
                if (answerFields[i].getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "Please answer all security questions.\nMissing: Q" + (i + 1));
                    answerFields[i].requestFocus();
                    return;
                }
            }

            User user = new User();
            if (isEdit) user.setId(existing.getId());
            user.setUsername(username);
            user.setPassword(password);
            user.setRole(role);

            boolean success;
            if (isEdit) {
                success = userDAO.updateUser(user);
            } else {
                success = userDAO.addUser(user);
                if (success) {
                    // Need to get the user ID for the newly created user
                    User created = userDAO.getUserByUsername(username);
                    if (created != null) user.setId(created.getId());
                }
            }

            if (success) {
                // Save security questions
                java.util.LinkedHashMap<String, String> qa = new java.util.LinkedHashMap<>();
                for (int i = 0; i < SecurityQuestionDAO.QUESTIONS.length; i++) {
                    qa.put(SecurityQuestionDAO.QUESTIONS[i], answerFields[i].getText().trim());
                }
                securityDAO.saveQuestions(user.getId(), qa);
            }

            loadUsers();
            dialog.dispose();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(CARD_BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 16, 24));
        btnPanel.add(saveBtn);

        dialog.add(formScroll, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            tableModel.addRow(new Object[]{
                u.getId(), u.getUsername(),
                u.getRole(), u.getSyncStatus()
            });
        }
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private JTextField createFormField(String value) {
        JTextField field = new JTextField(value);
        field.setBackground(new Color(15, 23, 42));
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        return field;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        return label;
    }
}