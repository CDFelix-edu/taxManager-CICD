package it.unimol.taxManager.util;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class FlyingSaucerService {

    public byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html, getClass().getClassLoader().getResource("/templates/").toExternalForm());
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        } catch (IOException | com.lowagie.text.DocumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore nella generazione del PDF: "+e.getMessage());
        }
    }
}
