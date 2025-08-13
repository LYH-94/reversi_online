package com.lyh.reversi_online.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class ResponseHTML {
    @Autowired
    private ResourceLoader resourceLoader;

    public ResponseEntity<byte[]> getHTML(String html, HttpStatus httpStatus) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:/static/pages/" + html + ".html");

        // 使用 InputStream 讀取，避免打包成 JAR 後出錯
        byte[] content;
        try (InputStream inputStream = resource.getInputStream()) {
            content = inputStream.readAllBytes();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, httpStatus);
    }
}

// resource.getFile() 在打包成 jar 包後不適用，因為它假設資源是以檔案系統的形式存在，但打包成 JAR 包後，
// 資源實際上是壓縮包內的一段位元流。因此部屬於伺服器後，執行會報錯。
/*
@Component
public class ResponseHTML {
    @Autowired
    private ResourceLoader resourceLoader;

    public ResponseEntity<byte[]> getHTML(String html, HttpStatus httpStatus) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:/static/pages/" + html + ".html");
        byte[] content = Files.readAllBytes(resource.getFile().toPath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(Files.probeContentType(resource.getFile().toPath())));
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, httpStatus);
    }
}
*/