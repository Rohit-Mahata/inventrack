package com.inventory.ui.panels;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import com.inventory.util.PDFExporter;
import com.inventory.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class ProductPanel extends JPanel {

    private ProductDAO productDAO = new ProductDAO();
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private static final Color BG        = new Color(26, 26, 46);
    private static final Color CARD_BG   = new Color(22, 33, 62);
    private static final Color BORDER    = new Color(42, 42, 74);
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color MUTED     = new Color(100, 116, 139);
    private static final Color INDIGO    = new Color(79, 70, 229);
    private static final Color GREEN     = new Color(34, 197, 94);
    private static final Color RED       = new Color(239, 68, 68);
    private static final Color TABLE_ALT = new Color(30, 41, 59);

    public ProductPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Top section
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(BG);
        topSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BG);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Products");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Manage your product inventory");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        // Search + Add button
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(BG);

        searchField = new JTextField(20);
        searchField.setBackground(CARD_BG);
        searchField.setForeground(TEXT);
        searchField.setCaretColor(TEXT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));

        JButton searchBtn = createButton("Search", INDIGO);
        JButton addBtn    = createButton("+ Add Product", GREEN);

        searchBtn.addActionListener(e -> searchProducts());
        addBtn.addActionListener(e -> showProductDialog(null));

        actionPanel.add(new JLabel("") {{
            setForeground(MUTED);
            setFont(new Font("Arial", Font.PLAIN, 13));
            setText("Search: ");
        }});
        actionPanel.add(searchField);
        actionPanel.add(searchBtn);
        actionPanel.add(addBtn);

        topSection.add(titlePanel, BorderLayout.WEST);
        topSection.add(actionPanel, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Name", "Category", "Quantity", "Price (₹)", "Low Stock Limit", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        productTable = new JTable(tableModel);
        productTable.setBackground(CARD_BG);
        productTable.setForeground(TEXT);
        productTable.setFont(new Font("Arial", Font.PLAIN, 13));
        productTable.setRowHeight(38);
        productTable.setShowGrid(false);
        productTable.setIntercellSpacing(new Dimension(0, 0));
        productTable.getTableHeader().setBackground(new Color(30, 41, 59));
        productTable.getTableHeader().setForeground(MUTED);
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        productTable.setSelectionBackground(INDIGO);
        productTable.setSelectionForeground(Color.WHITE);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        productTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? INDIGO : (row % 2 == 0 ? CARD_BG : TABLE_ALT));
                setForeground(sel ? Color.WHITE : TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        // Double click to edit
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = productTable.getSelectedRow();
                    if (row >= 0) {
                        int id = (int) tableModel.getValueAt(row, 0);
                        Product p = productDAO.getProductById(id);
                        showProductDialog(p);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBackground(CARD_BG);

        // Bottom action bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setBackground(BG);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton editBtn   = createButton("Edit Selected", INDIGO);
        JButton deleteBtn = createButton("Delete Selected", RED);
        JButton refreshBtn = createButton("Refresh", MUTED);
        JButton exportBtn = new JButton("Export PDF");
exportBtn.setBackground(new Color(220, 38, 38)); // red
exportBtn.setForeground(Color.WHITE);
exportBtn.addActionListener(e -> PDFExporter.exportProducts());
bottomBar.add(exportBtn);
        
// Role based access — only admin can edit and delete
if (!SessionManager.isAdmin()) {
    editBtn.setEnabled(false);
    editBtn.setToolTipText("Admin only");
    deleteBtn.setEnabled(false);
    deleteBtn.setToolTipText("Admin only");
}

        editBtn.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                Product p = productDAO.getProductById(id);
                showProductDialog(p);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to edit.");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                String name = (String) tableModel.getValueAt(row, 1);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete product: " + name + "?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    productDAO.deleteProduct(id);
                    loadProducts();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            }
        });

        refreshBtn.addActionListener(e -> loadProducts());

        bottomBar.add(refreshBtn);
        bottomBar.add(editBtn);
        bottomBar.add(deleteBtn);

        add(topSection,  BorderLayout.NORTH);
        add(scroll,      BorderLayout.CENTER);
        add(bottomBar,   BorderLayout.SOUTH);

        loadProducts();
    }

    private void showProductDialog(Product existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Product" : "Add Product", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(CARD_BG);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setBackground(CARD_BG);
        form.setLayout(new GridLayout(6, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JTextField nameField     = createFormField(isEdit ? existing.getName() : "");
        JTextField categoryField = createFormField(isEdit ? existing.getCategory() : "");
        JTextField qtyField      = createFormField(isEdit ? String.valueOf(existing.getQuantity()) : "");
        JTextField priceField    = createFormField(isEdit ? String.valueOf(existing.getPrice()) : "");
        JTextField limitField    = createFormField(isEdit ? String.valueOf(existing.getLowStockLimit()) : "10");

        form.add(createFormLabel("Name:"));        form.add(nameField);
        form.add(createFormLabel("Category:"));    form.add(categoryField);
        form.add(createFormLabel("Quantity:"));    form.add(qtyField);
        form.add(createFormLabel("Price (₹):"));   form.add(priceField);
        form.add(createFormLabel("Low Stock Limit:")); form.add(limitField);

        JButton saveBtn = createButton(isEdit ? "Update" : "Save", INDIGO);
        saveBtn.addActionListener(e -> {
            try {
                Product p = new Product();
                if (isEdit) p.setId(existing.getId());
                p.setName(nameField.getText().trim());
                p.setCategory(categoryField.getText().trim());
                p.setQuantity(Integer.parseInt(qtyField.getText().trim()));
                p.setPrice(Double.parseDouble(priceField.getText().trim()));
                p.setLowStockLimit(Integer.parseInt(limitField.getText().trim()));

                if (isEdit) productDAO.updateProduct(p);
                else        productDAO.addProduct(p);

                loadProducts();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for quantity, price and limit.");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(CARD_BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 16, 24));
        btnPanel.add(saveBtn);

        form.add(new JLabel());
        dialog.add(form,     BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getId(), p.getName(), p.getCategory(),
                p.getQuantity(), String.format("₹%.2f", p.getPrice()),
                p.getLowStockLimit(), "Edit | Delete"
            });
        }
    }

    private void searchProducts() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Product> products = keyword.isEmpty()
            ? productDAO.getAllProducts()
            : productDAO.searchProducts(keyword);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getId(), p.getName(), p.getCategory(),
                p.getQuantity(), String.format("₹%.2f", p.getPrice()),
                p.getLowStockLimit(), "Edit | Delete"
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