package com.cmai.service;

import com.cmai.exception.ApiException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** Reads plain text out of an uploaded PDF, DOCX or DOC resume (in memory, nothing is written to disk). */
@Component
public class DocumentTextExtractor {

    private static final Logger log = LoggerFactory.getLogger(DocumentTextExtractor.class);

    public String extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(400, "No file uploaded");
        }

        String name = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().toLowerCase(Locale.ROOT);

        try {
            byte[] bytes = file.getBytes();
            String text;
            if (name.endsWith(".pdf")) {
                text = fromPdf(bytes);
            } else if (name.endsWith(".docx")) {
                text = fromDocx(bytes);
            } else if (name.endsWith(".doc")) {
                text = fromDoc(bytes);
            } else {
                throw new ApiException(400, "Only PDF and Word documents are allowed.");
            }

            String cleaned = text == null ? "" : text.replace("\u0000", "").trim();
            if (cleaned.isBlank()) {
                throw new ApiException(
                        422, "No readable text found in this file. If it is a scan, upload a text-based PDF.");
            }
            return cleaned;
        } catch (ApiException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            log.warn("Could not read uploaded document: {}", e.toString());
            throw new ApiException(422, "This file could not be read. It may be corrupted or password protected.");
        }
    }

    private static String fromPdf(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static String fromDocx(byte[] bytes) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes));
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private static String fromDoc(byte[] bytes) throws IOException {
        try (HWPFDocument document = new HWPFDocument(new ByteArrayInputStream(bytes));
                WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }
}
