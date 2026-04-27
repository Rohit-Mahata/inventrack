package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import dao.ProductDAO;
import dao.SaleDAO;
import model.Product;
import model.Sale;

import javax.swing.*;
import java.awt.Color;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporter {

    // ─── Fonts ───────────────────────────────────────────────────────────────
    private static final Font TITLE_FONT   = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   new BaseColor(79, 70, 229));  // indigo
    private static final Font HEADER_FONT  = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   BaseColor.WHITE);
    private static final Font CELL_FONT    = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final Font LABEL_FONT   = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   BaseColor.DARK_GRAY);
    private static final Font SUBTITLE_FONT= new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.GRAY);

    private static final BaseColor HEADER_BG    = new BaseColor(79, 70, 229);   // indigo
    private static final BaseColor ROW_ALT_BG   = new BaseColor(245, 245, 255); // light indigo tint
    private static final BaseColor BORDER_COLOR  = new BaseColor(200, 200, 220);

    // ─── File chooser helper ─────────────────────────────────────────────────
    private static String chooseSavePath(String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save PDF");
        chooser.setSelectedFile(new java.io.File(defaultName));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";
            return path;
        }
        return null;
    }

    // ─── Shared: add header block ────────────────────────────────────────────
    private static void addHeader(Document doc, String title, String subtitle) throws DocumentException {
        // App name
        Paragraph appName = new Paragraph("InvenTrack", TITLE_FONT);
        appName.setAlignment(Element.ALIGN_LEFT);
        doc.add(appName);

        // Title
        Font tf = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph t = new Paragraph(title, tf);
        t.setAlignment(Element.ALIGN_LEFT);
        doc.add(t);

        // Subtitle / timestamp
        String ts = "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        Paragraph sub = new Paragraph(subtitle + "   |   " + ts, SUBTITLE_FONT);
        sub.setAlignment(Element.ALIGN_LEFT);
        sub.setSpacingAfter(12);
        doc.add(sub);

        // Divider line
        LineSeparator ls = new LineSeparator(1f, 100f, HEADER_BG, Element.ALIGN_CENTER, -2);
        doc.add(new Chunk(ls));
        doc.add(Chunk.NEWLINE);
    }

    // ─── Shared: styled table header row ────────────────────────────────────
    private static void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(8);
            cell.setBorderColor(BORDER_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    // ─── Shared: data cell ───────────────────────────────────────────────────
    private static PdfPCell dataCell(String text, boolean altRow) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "-" : text, CELL_FONT));
        cell.setPadding(7);
        cell.setBorderColor(BORDER_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (altRow) cell.setBackgroundColor(ROW_ALT_BG);
        return cell;
    }

    // =========================================================================
    // 1. EXPORT PRODUCTS LIST
    // =========================================================================
    public static void exportProducts() {
        String path = chooseSavePath("InvenTrack_Products.pdf");
        if (path == null) return;

        try {
            List<Product> products = new ProductDAO().getAllProducts();

            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            addHeader(doc, "Products List", "Total Products: " + products.size());

            // Summary cards row
            long lowStock = products.stream().filter(p -> p.getQuantity() <= p.getMinStockLevel()).count();
            addSummaryRow(doc, new String[]{"Total Products", "Low Stock Items"},
                                new String[]{String.valueOf(products.size()), String.valueOf(lowStock)});

            // Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 2.5f, 1.5f, 1f, 1f, 1f, 1.5f});
            table.setSpacingBefore(10);

            addTableHeader(table, "#", "Product Name", "Category", "Price (₹)", "Quantity", "Min Stock", "Status");

            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                boolean alt = i % 2 != 0;
                boolean isLow = p.getQuantity() <= p.getMinStockLevel();

                table.addCell(dataCell(String.valueOf(i + 1), alt));
                table.addCell(dataCell(p.getName(), alt));
                table.addCell(dataCell(p.getCategory(), alt));
                table.addCell(dataCell(String.format("%.2f", p.getPrice()), alt));
                table.addCell(dataCell(String.valueOf(p.getQuantity()), alt));
                table.addCell(dataCell(String.valueOf(p.getMinStockLevel()), alt));

                // Status cell colored
                PdfPCell statusCell = new PdfPCell(new Phrase(
                    isLow ? "LOW STOCK" : "OK",
                    new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD,
                        isLow ? new BaseColor(220, 38, 38) : new BaseColor(22, 163, 74))
                ));
                statusCell.setPadding(7);
                statusCell.setBorderColor(BORDER_COLOR);
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (alt) statusCell.setBackgroundColor(ROW_ALT_BG);
                table.addCell(statusCell);
            }

            doc.add(table);
            addFooter(doc);
            doc.close();

            JOptionPane.showMessageDialog(null, "✅ Products PDF exported successfully!\n" + path,
                "Export Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Export failed: " + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // 2. EXPORT SALES HISTORY
    // =========================================================================
    public static void exportSales() {
        String path = chooseSavePath("InvenTrack_Sales.pdf");
        if (path == null) return;

        try {
            List<Sale> sales = new SaleDAO().getAllSales();

            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            addHeader(doc, "Sales History", "Total Records: " + sales.size());

            // Summary
            double total = sales.stream().mapToDouble(Sale::getTotalPrice).sum();
            double todayTotal = new SaleDAO().getTodaySalesTotal();
            addSummaryRow(doc,
                new String[]{"Total Sales", "Total Revenue", "Today's Sales"},
                new String[]{String.valueOf(sales.size()),
                             String.format("₹ %.2f", total),
                             String.format("₹ %.2f", todayTotal)});

            // Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 2.5f, 1f, 1f, 1.5f, 2f});
            table.setSpacingBefore(10);

            addTableHeader(table, "#", "Product", "Qty", "Unit Price (₹)", "Total (₹)", "Date & Time");

            for (int i = 0; i < sales.size(); i++) {
                Sale s = sales.get(i);
                boolean alt = i % 2 != 0;
                table.addCell(dataCell(String.valueOf(i + 1), alt));
                table.addCell(dataCell(s.getProductName(), alt));
                table.addCell(dataCell(String.valueOf(s.getQuantity()), alt));
                table.addCell(dataCell(String.format("%.2f", s.getUnitPrice()), alt));
                table.addCell(dataCell(String.format("%.2f", s.getTotalPrice()), alt));
                table.addCell(dataCell(s.getSaleDate(), alt));
            }

            doc.add(table);
            addFooter(doc);
            doc.close();

            JOptionPane.showMessageDialog(null, "✅ Sales PDF exported successfully!\n" + path,
                "Export Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Export failed: " + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // 3. EXPORT REPORTS SUMMARY
    // =========================================================================
    public static void exportReports() {
        String path = chooseSavePath("InvenTrack_Report.pdf");
        if (path == null) return;

        try {
            SaleDAO saleDAO = new SaleDAO();
            ProductDAO productDAO = new ProductDAO();

            double todaySales   = saleDAO.getTodaySalesTotal();
            double monthlySales = saleDAO.getMonthlySalesTotal();
            int totalProducts   = productDAO.getTotalProductCount();
            int lowStockCount   = productDAO.getLowStockProducts().size();

            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            addHeader(doc, "Business Report", "Monthly Overview");

            // KPI Summary
            addSummaryRow(doc,
                new String[]{"Total Products", "Low Stock", "Today's Sales", "Monthly Revenue"},
                new String[]{String.valueOf(totalProducts),
                             String.valueOf(lowStockCount),
                             String.format("₹ %.2f", todaySales),
                             String.format("₹ %.2f", monthlySales)});

            // Top Selling Products table
            doc.add(new Paragraph("\nTop Selling Products", 
                new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(79, 70, 229))));
            doc.add(Chunk.NEWLINE);

            List<Sale> allSales = saleDAO.getAllSales();
            java.util.Map<String, Double> productRevenue = new java.util.LinkedHashMap<>();
            java.util.Map<String, Integer> productQty    = new java.util.LinkedHashMap<>();
            for (Sale s : allSales) {
                productRevenue.merge(s.getProductName(), s.getTotalPrice(), Double::sum);
                productQty.merge(s.getProductName(), s.getQuantity(), Integer::sum);
            }

            PdfPTable topTable = new PdfPTable(4);
            topTable.setWidthPercentage(100);
            topTable.setWidths(new float[]{0.5f, 3f, 1.5f, 2f});
            addTableHeader(topTable, "#", "Product", "Units Sold", "Revenue (₹)");

            List<java.util.Map.Entry<String, Double>> sorted = new java.util.ArrayList<>(productRevenue.entrySet());
            sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            int rank = 1;
            for (java.util.Map.Entry<String, Double> entry : sorted) {
                boolean alt = rank % 2 == 0;
                topTable.addCell(dataCell(String.valueOf(rank), alt));
                topTable.addCell(dataCell(entry.getKey(), alt));
                topTable.addCell(dataCell(String.valueOf(productQty.get(entry.getKey())), alt));
                topTable.addCell(dataCell(String.format("%.2f", entry.getValue()), alt));
                rank++;
                if (rank > 10) break; // Top 10 only
            }
            doc.add(topTable);

            // Low Stock Alert table
            doc.add(new Paragraph("\nLow Stock Alerts",
                new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(220, 38, 38))));
            doc.add(Chunk.NEWLINE);

            List<Product> lowStock = productDAO.getLowStockProducts();
            PdfPTable lowTable = new PdfPTable(4);
            lowTable.setWidthPercentage(100);
            lowTable.setWidths(new float[]{0.5f, 3f, 1.5f, 1.5f});
            addTableHeader(lowTable, "#", "Product", "Current Qty", "Min Required");

            for (int i = 0; i < lowStock.size(); i++) {
                Product p = lowStock.get(i);
                boolean alt = i % 2 != 0;
                lowTable.addCell(dataCell(String.valueOf(i + 1), alt));
                lowTable.addCell(dataCell(p.getName(), alt));

                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(p.getQuantity()),
                    new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(220, 38, 38))));
                qtyCell.setPadding(7);
                qtyCell.setBorderColor(BORDER_COLOR);
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (alt) qtyCell.setBackgroundColor(ROW_ALT_BG);
                lowTable.addCell(qtyCell);

                lowTable.addCell(dataCell(String.valueOf(p.getMinStockLevel()), alt));
            }
            doc.add(lowTable);

            addFooter(doc);
            doc.close();

            JOptionPane.showMessageDialog(null, "✅ Report PDF exported successfully!\n" + path,
                "Export Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Export failed: " + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Summary cards row ───────────────────────────────────────────────────
    private static void addSummaryRow(Document doc, String[] labels, String[] values) throws DocumentException {
        PdfPTable summary = new PdfPTable(labels.length);
        summary.setWidthPercentage(100);
        summary.setSpacingBefore(8);
        summary.setSpacingAfter(8);

        for (int i = 0; i < labels.length; i++) {
            PdfPCell cell = new PdfPCell();
            cell.setPadding(10);
            cell.setBorderColor(BORDER_COLOR);
            cell.setBackgroundColor(new BaseColor(238, 238, 255));

            Paragraph p = new Paragraph();
            p.add(new Chunk(values[i] + "\n",
                new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(79, 70, 229))));
            p.add(new Chunk(labels[i],
                new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.GRAY)));
            p.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(p);
            summary.addCell(cell);
        }
        doc.add(summary);
    }

    // ─── Footer ──────────────────────────────────────────────────────────────
    private static void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        LineSeparator ls = new LineSeparator(0.5f, 100f, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2);
        doc.add(new Chunk(ls));
        Paragraph footer = new Paragraph(
            "Generated by InvenTrack  •  " +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(4);
        doc.add(footer);
    }
}