package com.microfocus.example.web.controllers;

import com.microfocus.example.service.StorageService;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    @Test
    public void serveUnverifiedFileEncodesUnsafeFilenameForContentDisposition() {
        StorageService storageService = mock(StorageService.class);
        UserController controller = new UserController();
        String filename = "report.txt\r\nX-Injected: true";

        when(storageService.loadAsResource(eq("requested-file"), eq(true)))
                .thenReturn(new ByteArrayResource(new byte[0]) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                });
        ReflectionTestUtils.setField(controller, "storageService", storageService);

        ResponseEntity<?> response = controller.serveUnverifiedFile("requested-file");
        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);

        assertFalse(contentDisposition.contains("\r"));
        assertFalse(contentDisposition.contains("\n"));
        assertEquals("attachment", contentDisposition.substring(0, "attachment".length()));
    }

    @Test
    public void serveUnverifiedFilePreservesAsciiFilename() {
        StorageService storageService = mock(StorageService.class);
        UserController controller = new UserController();

        when(storageService.loadAsResource(eq("requested-file"), eq(true)))
                .thenReturn(new ByteArrayResource(new byte[0]) {
                    @Override
                    public String getFilename() {
                        return "report.txt";
                    }
                });
        ReflectionTestUtils.setField(controller, "storageService", storageService);

        ResponseEntity<?> response = controller.serveUnverifiedFile("requested-file");

        assertEquals("attachment; filename*=UTF-8''report.txt",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }
}