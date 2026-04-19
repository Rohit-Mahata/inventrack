package com.inventory.ui.panels;

import com.inventory.dao.SaleDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.model.Sale;
import com.inventory.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class SalesPanel extends JPanel {

    private SaleDAO saleDAO         = new SaleDAO();
    private ProductDAO productDAO   = new ProductDAO();
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> productCombo;
    private JTextField qtyField;
    private JTextField totalField;
    private JLabel stockAvailableLabel;

    private static final Color BG        = new Color(26, 26, 46);
    private static final Color CARD_BG   = new Color(22, 33, 62);
    private static final Color BORDER    = new Color(42, 42, 74);
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color MUTED     = new Color(100, 116, 139);
    private static final Color INDIGO    = new Color(79, 70, 229);
    private static final Color GREEN     = new Color(34, 197, 94);
    private static final Color RED       = new Color(239, 68, 68);
    private static final Color TABLE_ALT = new Color(30, 41, 59);

    public SalesPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BG);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Sales");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Record and manage your sales");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(subtitle);

        // Top split panel
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        topPanel.setBackground(BG);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        topPanel.add(createSaleForm());
        topPanel.add(createSalesSummary());

        // Sales history table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_BG);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(CARD_BG);
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel tableTitle = new JLabel("Sales History");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 14));
        tableTitle.setForeground(TEXT);

        JButton deleteBtn = createButton("Delete Selected", RED);
        deleteBtn.addActionListener(e -> deleteSelectedSale());

        tableHeader.add(tableTitle,  BorderLayout.WEST);
        tableHeader.add(deleteBtn,   BorderLayout.EAST);

        String[] columns = {"ID", "Product", "Quantity", "Total (₹)", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        salesTable = new JTable(tableModel);
        salesTable.setBackground(CARD_BG);
        salesTable.setForeground(TEXT);
        salesTable.setFont(new Font("Arial", Font.PLAIN, 13));
        salesTable.setRowHeight(36);
        salesTable.setShowGrid(false);
        salesTable.setIntercellSpacing(new Dimension(0, 0));
        salesTable.getTableHeader().setBackground(new Color(30, 41, 59));
        salesTable.getTableHeader().setForeground(MUTED);
        salesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        salesTable.setSelectionBackground(INDIGO);
        salesTable.setSelectionForeground(Color.WHITE);
        salesTable.getColumnModel().getColumn(0).setPreferredWidth(40);

        salesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? INDIGO : (row % 2 == 0 ? CARD_BG : TABLE_ALT));
                setForeground(sel ? Color.WHITE : TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(salesTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBackground(CARD_BG);

        tablePanel.add(tableHeader, BorderLayout.NORTH);
        tablePanel.add(scroll,      BorderLayout.CENTER);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG);
        mainContent.add(titlePanel, BorderLayout.NORTH);
        mainContent.add(topPanel,   BorderLayout.CENTER);

        add(mainContent, BorderLayout.NORTH);
        add(tablePanel,  BorderLayout.CENTER);

        loadSales();
    }

    private JPanel createSaleForm() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("New Sale");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBackground(CARD_BG);

        // Product dropdown
        productCombo = new JComboBox<>();
        productCombo.setBackground(new Color(15, 23, 42));
        productCombo.setForeground(TEXT);
        productCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        loadProductsIntoCombo();

        // Stock available label
        stockAvailableLabel = new JLabel("Available: -");
        stockAvailableLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        stockAvailableLabel.setForeground(GREEN);

        // Update stock label when product changes
        productCombo.addActionListener(e -> updateStockLabel());

        // Quantity field
        qtyField = createFormField("");
        qtyField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { calculateTotal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { calculateTotal(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculateTotal(); }
        });

        // Total field (auto calculated)
        totalField = createFormField("0.00");
        totalField.setEditable(false);
        totalField.setForeground(GREEN);

        form.add(createFormLabel("Product:"));          form.add(productCombo);
        form.add(createFormLabel("Stock Available:"));  form.add(stockAvailableLabel);
        form.add(createFormLabel("Quantity:"));         form.add(qtyField);
        form.add(createFormLabel("Total (₹):"));        form.add(totalField);

        JButton saveBtn = createButton("Record Sale", INDIGO);
        saveBtn.addActionListener(e -> saveSale());
        form.add(new JLabel());
        form.add(saveBtn);

        card.add(title, BorderLayout.NORTH);
        card.add(form,  BorderLayout.CENTER);

        updateStockLabel();

        return card;
    }

    private JPanel createSalesSummary() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Sales Summary");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel content = new JPanel(new GridLayout(3, 1, 0, 12));
        content.setBackground(CARD_BG);

        double todayTotal   = saleDAO.getTodaySalesTotal();
        double monthlyTotal = saleDAO.getMonthlySalesTotal();
        int totalSales      = saleDAO.getAllSales().size();

        content.add(createSummaryCard("Today's Revenue",  String.format("₹%.0f", todayTotal),   GREEN));
        content.add(createSummaryCard("Monthly Revenue",  String.format("₹%.0f", monthlyTotal), INDIGO));
        content.add(createSummaryCard("Total Sales Made", String.valueOf(totalSales),            MUTED));

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

    private void saveSale() {
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

            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                return;
            }

            if (selected.getQuantity() < qty) {
                JOptionPane.showMessageDialog(this, "Not enough stock! Available: " + selected.getQuantity());
                return;
            }

            double total = selected.getPrice() * qty;

            Sale sale = new Sale();
            sale.setProductId(selected.getId());
            sale.setProductName(selected.getName());
            sale.setQuantity(qty);
            sale.setTotal(total);
            sale.setSaleDate(LocalDate.now().toString());

            if (saleDAO.addSale(sale)) {
                // Reduce product stock
                selected.setQuantity(selected.getQuantity() - qty);
                productDAO.updateProduct(selected);

                qtyField.setText("");
                totalField.setText("0.00");
                loadSales();
                updateStockLabel();
                JOptionPane.showMessageDialog(this,
                    "Sale recorded!\nProduct: " + selected.getName() +
                    "\nQty: " + qty +
                    "\nTotal: ₹" + String.format("%.2f", total));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.");
        }
    }

    private void deleteSelectedSale() {
        int row = salesTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this sale record?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                saleDAO.deleteSale(id);
                loadSales();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a sale to delete.");
        }
    }

    private void loadSales() {
        tableModel.setRowCount(0);
        List<Sale> sales = saleDAO.getAllSales();
        for (Sale s : sales) {
            tableModel.addRow(new Object[]{
                s.getId(),
                s.getProductName(),
                s.getQuantity(),
                String.format("₹%.2f", s.getTotal()),
                s.getSaleDate()
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

    private void updateStockLabel() {
        String productName = (String) productCombo.getSelectedItem();
        if (productName != null) {
            List<Product> products = productDAO.getAllProducts();
            products.stream()
                .filter(p -> p.getName().equals(productName))
                .findFirst()
                .ifPresent(p -> {
                    stockAvailableLabel.setText("Available: " + p.getQuantity());
                    stockAvailableLabel.setForeground(p.getQuantity() <= p.getLowStockLimit() ? RED : GREEN);
                });
        }
    }

    private void calculateTotal() {
        try {
            String productName = (String) productCombo.getSelectedItem();
            if (productName == null) return;
            List<Product> products = productDAO.getAllProducts();
            products.stream()
                .filter(p -> p.getName().equals(productName))
                .findFirst()
                .ifPresent(p -> {
                    int qty = Integer.parseInt(qtyField.getText().trim());
                    double total = p.getPrice() * qty;
                    totalField.setText(String.format("%.2f", total));
                });
        } catch (NumberFormatException e) {
            totalField.setText("0.00");
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