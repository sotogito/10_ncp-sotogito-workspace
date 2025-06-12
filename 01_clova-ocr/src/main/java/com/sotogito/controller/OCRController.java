package com.sotogito.controller;

import com.sotogito.util.FileUtil;
import com.sotogito.util.OcrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequiredArgsConstructor
@Controller
public class OCRController {

    private final FileUtil fileUtil;
    private final OcrUtil ocrUtil;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(String type, MultipartFile file) {
        Map<String, String> map = fileUtil.fileupload("ocr", file);
        /// 저장된 파일의 path ; map.get("filepath") + "/" + mao.get("filesystemName")

        return ResponseEntity.ok(map);
    }
}
