package com.expensemanager.util;

import com.expensemanager.model.Transaction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFGenerator {

    // Helper: safely strip control characters and non-Latin-1 glyphs.
    // PDFBox's built-in Type1 fonts (Helvetica, etc.) use WinAnsiEncoding which:
    //   • Only supports codepoints 0x20–0xFF  (printable Latin-1)
    //   • Will throw on ANY control character, including U+000A (LF), U+000D (CR), U+0009 (TAB)
    //   • Will throw on non-Latin-1 Unicode (e.g. Bengali ৳ U+09F3)
    private static String latin1Safe(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            // Skip ALL control characters (0x00–0x1F and 0x7F–0x9F)
            if (c < 0x20 || (c >= 0x7F && c <= 0x9F)) {
                // Replace common whitespace control chars with a space instead of dropping
                if (c == '\t' || c == '\n' || c == '\r') {
                    sb.append(' ');
                }
                // All other control chars are silently dropped
                continue;
            }
            if (c <= 0xFF) {
                sb.append(c);
            } else {
                // Replace Bengali taka sign or other non-Latin-1 Unicode
                if (c == '\u09F3') {   // ৳
                    sb.append("BDT");
                } else {
                    sb.append('?');
                }
            }
        }
        return sb.toString();
    }

    // Wrapper that applies latin1Safe to ANY string going into showText()
    private static String safeText(String text) {
        return latin1Safe(text);
    }

    public static void generateReport(String filePath, List<Transaction> transactions) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(document, page);

            float pageWidth  = page.getMediaBox().getWidth();   // ~595
            float pageHeight = page.getMediaBox().getHeight();  // ~842
            float margin = 50;
            float y = pageHeight - 60;

            // ── Header background bar ──────────────────────────────────────
            cs.setNonStrokingColor(new PDColor(new float[]{0.31f, 0.27f, 0.90f}, PDDeviceRGB.INSTANCE));
            cs.addRect(0, pageHeight - 90, pageWidth, 90);
            cs.fill();

            // Title (white on purple bar)
            cs.setNonStrokingColor(new PDColor(new float[]{1f, 1f, 1f}, PDDeviceRGB.INSTANCE));
            cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText(safeText("Smart Expense Manager - Financial Report"));
            cs.endText();

            y -= 22;
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText(safeText("Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))));
            cs.endText();

            y = pageHeight - 110;

            // ── Summary totals ─────────────────────────────────────────────
            double totalIncome  = transactions.stream()
                    .filter(t -> "INCOME".equals(t.getType()))
                    .mapToDouble(Transaction::getAmount).sum();
            double totalExpense = transactions.stream()
                    .filter(t -> "EXPENSE".equals(t.getType()))
                    .mapToDouble(Transaction::getAmount).sum();
            double balance = totalIncome - totalExpense;

            // Summary box background
            cs.setNonStrokingColor(new PDColor(new float[]{0.97f, 0.97f, 1.0f}, PDDeviceRGB.INSTANCE));
            cs.addRect(margin, y - 60, pageWidth - 2 * margin, 65);
            cs.fill();

            // Summary box border
            cs.setStrokingColor(new PDColor(new float[]{0.80f, 0.80f, 0.95f}, PDDeviceRGB.INSTANCE));
            cs.setLineWidth(0.8f);
            cs.addRect(margin, y - 60, pageWidth - 2 * margin, 65);
            cs.stroke();

            // Summary labels
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            float col1 = margin + 14, col2 = margin + 170, col3 = margin + 340;

            // Income
            cs.setNonStrokingColor(new PDColor(new float[]{0.13f, 0.65f, 0.30f}, PDDeviceRGB.INSTANCE));
            cs.beginText(); cs.newLineAtOffset(col1, y - 20);
            cs.showText(safeText("Total Income")); cs.endText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
            cs.beginText(); cs.newLineAtOffset(col1, y - 40);
            cs.showText(safeText(String.format("BDT %.2f", totalIncome))); cs.endText();

            // Expense
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.setNonStrokingColor(new PDColor(new float[]{0.87f, 0.26f, 0.26f}, PDDeviceRGB.INSTANCE));
            cs.beginText(); cs.newLineAtOffset(col2, y - 20);
            cs.showText(safeText("Total Expense")); cs.endText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
            cs.beginText(); cs.newLineAtOffset(col2, y - 40);
            cs.showText(safeText(String.format("BDT %.2f", totalExpense))); cs.endText();

            // Balance
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            float[] balColor = balance >= 0
                    ? new float[]{0.31f, 0.27f, 0.90f}
                    : new float[]{0.87f, 0.26f, 0.26f};
            cs.setNonStrokingColor(new PDColor(balColor, PDDeviceRGB.INSTANCE));
            cs.beginText(); cs.newLineAtOffset(col3, y - 20);
            cs.showText(safeText("Net Balance")); cs.endText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
            cs.beginText(); cs.newLineAtOffset(col3, y - 40);
            cs.showText(safeText(String.format("BDT %.2f", balance))); cs.endText();

            y -= 80;

            // ── Table header ───────────────────────────────────────────────
            float[] colX = {margin, margin + 75, margin + 150, margin + 255, margin + 350};
            String[] headers = {"Date", "Type", "Category", "Amount", "Note"};

            cs.setNonStrokingColor(new PDColor(new float[]{0.31f, 0.27f, 0.90f}, PDDeviceRGB.INSTANCE));
            cs.addRect(margin, y - 16, pageWidth - 2 * margin, 22);
            cs.fill();

            cs.setNonStrokingColor(new PDColor(new float[]{1f, 1f, 1f}, PDDeviceRGB.INSTANCE));
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            for (int i = 0; i < headers.length; i++) {
                cs.beginText();
                cs.newLineAtOffset(colX[i] + 4, y - 8);
                cs.showText(safeText(headers[i]));
                cs.endText();
            }
            y -= 26;

            // ── Table rows ─────────────────────────────────────────────────
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            boolean odd = true;

            for (Transaction t : transactions) {
                if (y < 60) {
                    // ── New page ──
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    cs = new PDPageContentStream(document, page);
                    y = pageHeight - 50;

                    // Repeat table header on new page
                    cs.setNonStrokingColor(new PDColor(new float[]{0.31f, 0.27f, 0.90f}, PDDeviceRGB.INSTANCE));
                    cs.addRect(margin, y - 16, pageWidth - 2 * margin, 22);
                    cs.fill();
                    cs.setNonStrokingColor(new PDColor(new float[]{1f, 1f, 1f}, PDDeviceRGB.INSTANCE));
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    for (int i = 0; i < headers.length; i++) {
                        cs.beginText(); cs.newLineAtOffset(colX[i] + 4, y - 8); cs.showText(safeText(headers[i])); cs.endText();
                    }
                    y -= 26;
                    odd = true;
                }

                // Row background
                if (odd) {
                    cs.setNonStrokingColor(new PDColor(new float[]{0.98f, 0.98f, 1.0f}, PDDeviceRGB.INSTANCE));
                    cs.addRect(margin, y - 12, pageWidth - 2 * margin, 17);
                    cs.fill();
                }
                odd = !odd;

                boolean isIncome = "INCOME".equals(t.getType());
                String amountStr = String.format("BDT %.2f", t.getAmount());

                String[] row = {
                    t.getDate() != null ? t.getDate().format(fmt) : "",
                    t.getType() != null ? t.getType() : "",
                    latin1Safe(t.getCategoryName() != null ? t.getCategoryName() : "Uncategorized"),
                    amountStr,
                    latin1Safe(t.getNote() != null ? truncate(t.getNote(), 28) : "")
                };

                // Amount color: green for income, red for expense
                for (int i = 0; i < row.length; i++) {
                    if (i == 3) {
                        cs.setNonStrokingColor(isIncome
                            ? new PDColor(new float[]{0.13f, 0.65f, 0.30f}, PDDeviceRGB.INSTANCE)
                            : new PDColor(new float[]{0.87f, 0.26f, 0.26f}, PDDeviceRGB.INSTANCE));
                        cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
                    } else {
                        cs.setNonStrokingColor(new PDColor(new float[]{0.12f, 0.18f, 0.24f}, PDDeviceRGB.INSTANCE));
                        cs.setFont(PDType1Font.HELVETICA, 9);
                    }
                    cs.beginText();
                    cs.newLineAtOffset(colX[i] + 4, y - 4);
                    cs.showText(safeText(row[i]));
                    cs.endText();
                }

                // Row bottom line
                cs.setStrokingColor(new PDColor(new float[]{0.90f, 0.90f, 0.95f}, PDDeviceRGB.INSTANCE));
                cs.setLineWidth(0.4f);
                cs.moveTo(margin, y - 13);
                cs.lineTo(pageWidth - margin, y - 13);
                cs.stroke();

                y -= 17;
            }

            // ── Footer ─────────────────────────────────────────────────────
            cs.setFont(PDType1Font.HELVETICA, 8);
            cs.setNonStrokingColor(new PDColor(new float[]{0.6f, 0.6f, 0.6f}, PDDeviceRGB.INSTANCE));
            cs.beginText();
            cs.newLineAtOffset(margin, 30);
            cs.showText(safeText("Smart Expense Manager  |  " + transactions.size() + " transaction(s)  |  " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))));
            cs.endText();

            cs.close();
            document.save(filePath);
            System.out.println("PDF saved: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    private static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
