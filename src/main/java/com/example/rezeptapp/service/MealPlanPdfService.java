package com.example.rezeptapp.service;

import com.example.rezeptapp.model.MealPlan;
import com.example.rezeptapp.model.MealPlanEntry;
import com.example.rezeptapp.model.MealSlot;
import com.example.rezeptapp.model.Recipe;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class MealPlanPdfService {

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font H2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font TEXT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font MUTED = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(90, 90, 90));

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public byte[] createPlanPdf(MealPlan plan) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 42, 42, 46, 46);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Paragraph header = new Paragraph("RezeptApp • Wochenplan", MUTED);
            header.setAlignment(Element.ALIGN_RIGHT);
            doc.add(header);

            String title = (plan.getTitle() == null || plan.getTitle().isBlank())
                    ? "Wochenplan"
                    : plan.getTitle().trim();

            Paragraph pTitle = new Paragraph(title, TITLE);
            pTitle.setSpacingBefore(6);
            pTitle.setSpacingAfter(10);
            doc.add(pTitle);

            LocalDate monday = plan.getWeekStartMonday();
            LocalDate sunday = monday.plusDays(6);

            Paragraph range = new Paragraph("Woche: " + DF.format(monday) + " – " + DF.format(sunday), MUTED);
            range.setSpacingAfter(12);
            doc.add(range);

            doc.add(weekTable(plan, monday));

            doc.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Erstellt mit RezeptApp • PDF-Export", MUTED);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF konnte nicht erstellt werden: " + e.getMessage(), e);
        }
    }

    private Element weekTable(MealPlan plan, LocalDate monday) {
        PdfPTable table = new PdfPTable(new float[]{1.3f, 2.3f, 2.3f, 2.3f});
        table.setWidthPercentage(100);

        table.addCell(th("Tag"));
        table.addCell(th("Frühstück"));
        table.addCell(th("Mittag"));
        table.addCell(th("Abend"));

        List<MealPlanEntry> entries = plan.getEntries() == null ? List.of() : plan.getEntries();
        entries = entries.stream()
                .sorted(Comparator.comparing(MealPlanEntry::getDay).thenComparing(MealPlanEntry::getSlot))
                .toList();

        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            table.addCell(td(dayLabel(day)));

            table.addCell(td(recipeTitle(entries, day, MealSlot.BREAKFAST)));
            table.addCell(td(recipeTitle(entries, day, MealSlot.LUNCH)));
            table.addCell(td(recipeTitle(entries, day, MealSlot.DINNER)));
        }

        return table;
    }

    private String recipeTitle(List<MealPlanEntry> entries, LocalDate day, MealSlot slot) {
        for (MealPlanEntry e : entries) {
            if (day.equals(e.getDay()) && slot == e.getSlot()) {
                Recipe r = e.getRecipe();
                return r == null ? "—" : safe(r.getTitle(), "—");
            }
        }
        return "—";
    }

    private String dayLabel(LocalDate d) {
        DayOfWeek w = d.getDayOfWeek();
        String name = switch (w) {
            case MONDAY -> "Mo";
            case TUESDAY -> "Di";
            case WEDNESDAY -> "Mi";
            case THURSDAY -> "Do";
            case FRIDAY -> "Fr";
            case SATURDAY -> "Sa";
            case SUNDAY -> "So";
        };
        return name + " • " + DF.format(d);
    }

    private PdfPCell th(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        c.setBackgroundColor(new Color(45, 108, 223));
        c.setPadding(7);
        c.setBorderColor(new Color(45, 108, 223));
        return c;
    }

    private PdfPCell td(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, TEXT));
        c.setPadding(7);
        c.setBorderColor(new Color(235, 235, 235));
        return c;
    }

    private String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}