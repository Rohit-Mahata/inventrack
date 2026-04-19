package com.inventory.ui.panels;

import com.inventory.dao.StockDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.model.StockMovement;
import com.inventory.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class StockPanel extends JPanel {

    private StockDAO stockDAO       = new StockDAO();
    private ProductDAO productDAO   = new ProductDAO();
    private JTable movementTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> productCombo;
    private JComboBox<String> typeCombo;
    private JTextField qtyField;
    private JTextField noteField;

    private static final Color BG        = new Color(26, 26, 46);
    private static final Color CARD_BG   = new Color(22, 33, 62);
    private static final Color BORDER    = new Color(42, 42, 74);
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color MUTED     = new Color(100, 116, 139);
    private static final Color INDIGO    = new Color(79, 70, 229);
    private static final Color GREEN     = new Color(34, 197, 94);
    private static final Color RED       = new Color(239, 68, 68);
    private static final Color TABLE_ALT = new Color(30, 41, 59);

    public StockPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BG);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Stock Management");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Track stock in and out movements");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(subtitle);

        // Top split panel
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        topPanel.setBackground(BG);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        topPanel.add(createStockForm());
        topPanel.add(createSummaryPanel());

        // Movement history table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_BG);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel tableTitle = new JLabel("Stock Movement History");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 14));
        tableTitle.setForeground(TEXT);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        String[] columns = {"ID", "Product", "Type", "Quantity", "Note", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        movementTable = new JTable(tableModel);
        movementTable.setBackground(CARD_BG);
        movementTable.setForeground(TEXT);
        movementTable.setFont(new Font("Arial", Font.PLAIN, 13));
        movementTable.setRowHeight(36);
        movementTable.setShowGrid(false);
        movementTable.setIntercellSpacing(new Dimension(0, 0));
        movementTable.getTableHeader().setBackground(new Color(30, 41, 59));
        movementTable.getTableHeader().setForeground(MUTED);
        movementTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        movementTable.setSelectionBackground(INDIGO);
        movementTable.setSelectionForeground(Color.WHITE);
        movementTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        movementTable.getColumnModel().getColumn(2).setPreferredWidth(60);

        movementTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? CARD_BG : TABLE_ALT);
                    // Color IN green, OUT red
                    if (col == 2) {
                        String type = val != null ? val.toString() : "";
                        setForeground(type.equals("IN") ? GREEN : RED);
                    } else {
                        setForeground(TEXT);
                    }
                } else {
                    setBackground(INDIGO);
                    setForeground(Color.WHITE);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(movementTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBackground(CARD_BG);

        tablePanel.add(tableTitle,  BorderLayout.NORTH);
        tablePanel.add(scroll,      BorderLayout.CENTER);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG);
        mainContent.add(titlePanel, BorderLayout.NORTH);
        mainContent.add(topPanel,   BorderLayout.CENTER);

        add(mainContent,  BorderLayout.NORTH);
        add(tablePanel,   BorderLayout.CENTER);

        loadMovements();
    }

    private JPanel createStockForm() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Add Stock Movement");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBackground(CARD_BG);

        // Product dropdown
        productCombo = new JComboBox<>();
        styleCombo(productCombo);
        loadProductsIntoCombo();

        // Type dropdown
        typeCombo = new JComboBox<>(new String[]{"IN", "OUT"});
        styleCombo(typeCombo);

        // Fields
        qtyField  = createFormField("");
        noteField = createFormField("");

        form.add(createFormLabel("Product:"));   form.add(productCombo);
        form.add(createFormLabel("Type:"));      form.add(typeCombo);
        form.add(createFormLabel("Quantity:"));  form.add(qtyField);
        form.add(createFormLabel("Note:"));      form.add(noteField);

        JButton saveBtn = createButton("Save Movement", INDIGO);
        saveBtn.addActionListener(e -> saveMovement());
        form.add(new JLabel());
        form.add(saveBtn);

        card.add(title, BorderLayout.NORTH);
        card.add(form,  BorderLayout.CENTER);

        return card;
    }

    private JPanel createSummaryPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Stock Summary");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel content = new JPanel(new GridLayout(3, 1, 0, 12));
        content.setBackground(CARD_BG);

        int totalProducts = productDAO.getTotalProducts();
        int lowStock      = productDAO.getLowStockProducts().size();
        List<StockMovement> movements = stockDAO.getAllMovements();
        int totalIn  = movements.stream().filter(m -> m.getType().equals("IN")).mapToInt(StockMovement::getQuantity).sum();
        int totalOut = movements.stream().filter(m -> m.getType().equals("OUT")).mapToInt(StockMovement::getQuantity).sum();

        content.add(createSummaryCard("Total Products",    String.valueOf(totalProducts), INDIGO));
        content.add(createSummaryCard("Total Stock IN",    String.valueOf(totalIn),       GREEN));
        content.add(createSummaryCard("Total Stock OUT",   String.valueOf(totalOut),      RED));

        card.add(title,   BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSummaryCard(String label, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, color),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.PLAIN, 12));
        labelComp.setForeground(MUTED);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.BOLD, 20));
        valueComp.setForeground(color);

        panel.add(labelComp, BorderLayout.NORTH);
        panel.add(valueComp, BorderLayout.CENTER);

        return panel;
    }

    private void saveMovement() {
        try {
            String productName = (String) productCombo.getSelectedItem();
            if (productName == null) {
                JOptionPane.showMessageDialog(this, "Please select a product.");
                return;
            }

            List<Product> products = productDAO.getAllProducts();
            Product selected = products.stream()
                .filter(p -> p.getName().equals(productName))
                .findFirst().orElse(null);

            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Product not found.");
                return;
            }

            int qty = Integer.parseInt(qtyField.getText().trim());
            String type = (String) typeCombo.getSelectedItem();
            String note = noteField.getText().trim();

            if (type.equals("OUT") && selected.getQuantity() < qty) {
                JOptionPane.showMessageDialog(this, "Not enough stock! Available: " + selected.getQuantity());
                return;
            }

            StockMovement movement = new StockMovement();
            movement.setProductId(selected.getId());
            movement.setProductName(selected.getName());
            movement.setType(type);
            movement.setQuantity(qty);
            movement.setNote(note);
            movement.setMoveDate(LocalDate.now().toString());

            if (stockDAO.addMovement(movement)) {
                // Update product quantity
                if (type.equals("IN"))  selected.setQuantity(selected.getQuantity() + qty);
                if (type.equals("OUT")) selected.setQuantity(selected.getQuantity() - qty);
                productDAO.updateProduct(selected);

                qtyField.setText("");
                noteField.setText("");
                loadMovements();
                JOptionPane.showMessageDialog(this, "Stock movement saved successfully!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.");
        }
    }

    private void loadMovements() {
        tableModel.setRowCount(0);
        List<StockMovement> movements = stockDAO.getAllMovements();
        for (StockMovement m : movements) {
            tableModel.addRow(new Object[]{
                m.getId(), m.getProductName(), m.getType(),
                m.getQuantity(), m.getNote(), m.getMoveDate()
            });
        }
    }

    private void loadProductsIntoCombo() {
        productCombo.removeAllItems();
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            productCombo.addItem(p.getName());
        }
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(new Color(15, 23, 42));
        combo.setForeground(TEXT);
        combo.setFont(new Font("Arial", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createLineBorder(BORDER, 1));
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