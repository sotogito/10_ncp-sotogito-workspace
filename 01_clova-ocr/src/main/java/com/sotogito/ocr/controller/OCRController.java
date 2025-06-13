package com.sotogito.ocr.controller;

import com.sotogito.ocr.util.FileUtil;
import com.sotogito.ocr.util.OcrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequiredArgsConstructor
@Controller
public class OCRController {

    private final FileUtil fileUtil;
    private final OcrUtil ocrUtil;

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> upload(String type, MultipartFile file) {


        Map<String, String> map = fileUtil.fileupload("ocr", file);
        /// 저장된 파일의 path ; map.get("filepath") + "/" + mao.get("filesystemName")
        String response = ocrUtil.processOCR(type, map.get("filePath") + "/" + map.get("filesystemName"));

        Map<String, Object> responseMessage = Map.of(
                "message", file.getOriginalFilename(),
                "result", response
        );

        return ResponseEntity.ok().body(responseMessage);
    }
}
