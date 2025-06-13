package com.sotogito.ocr.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Component
public class OcrUtil {

    @Value("${ncp.clova-ocr.general.url}")
    private String GENERAL_OCR_URL;
    @Value("${ncp.clova-ocr.general.secretKey}")
    private String GENERAL_OCR_SECRET_KEY;

    @Value("${ncp.clova-ocr.template.url}")
    private String TEMPLATE_OCR_URL;
    @Value("${ncp.clova-ocr.template.secretKey}")
    private String TEMPLATE_OCR_SECRET_KEY;

    /**
     * https://api.ncloud-docs.com/docs/ai-application-service-ocr-example01
     * <p>
     * NCP Clova OCR API 호출 후 응답 결과 반환용 메서드
     *
     * @param type      :   generaltemplate
     * @param imagePath :   OCR할 파일 경로
     * @return :   응답 결과
     */
    public String processOCR(String type, String imagePath) {
        String OCR_URL = "general".equals(type) ? GENERAL_OCR_URL : TEMPLATE_OCR_URL;
        String SECRET_KEY = "general".equals(type) ? GENERAL_OCR_SECRET_KEY : TEMPLATE_OCR_SECRET_KEY;

        try {
            URL url = new URL(OCR_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setReadTimeout(30000);
            con.setRequestMethod("POST");
            String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
            /// 아래 2개는 NCP요청 필수 설정
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("X-OCR-SECRET", SECRET_KEY); ///X-OCR-SECURET :HTTP 표준 아니고 자체적으로 정해둠

            // JSON 방식으로 JSON 문자열화 시키는거 => Jackson 방식으로
            Map<String, Object> jsonMap = new HashMap<>(); //JSONObject json = new JSONObject();
            jsonMap.put("version", "V2");
            jsonMap.put("requestId", UUID.randomUUID().toString());
            jsonMap.put("timestamp", System.currentTimeMillis());

            Map<String, Object> imageMap = new HashMap<>(); //JSONObject image = new JSONObject();
            imageMap.put("format", "jpg");
            imageMap.put("name", "demo");

            List<Map<String, Object>> imagesList = new ArrayList<>(); //  JSONArray images = new JSONArray();
            imagesList.add(imageMap);

            jsonMap.put("images", imagesList);

            ObjectMapper objectMapper = new ObjectMapper();
            String postParams = objectMapper.writeValueAsString(jsonMap); //java Object -> Jackson(ObjectMapper)이 json 문자열화


            con.connect();
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            long start = System.currentTimeMillis();
            File file = new File(imagePath);                  ///ocr할 이미지가 있는 파일의 경로
            writeMultiPart(wr, postParams, file, boundary);
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream())); /// conn 입력 스트림으로부터 응답 데이터 읽기 위한 입력용 스트림 생성
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) { /// 한 줄 단위로 응답데이터 읽기
                response.append(inputLine);
            }
            br.close();

            return response.toString(); ///JSON 문자열로 반환
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void writeMultiPart(OutputStream out, String jsonMessage, File file, String boundary) throws
            IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");                            ///경계1
        sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n");
        sb.append(jsonMessage);
        sb.append("\r\n");

        out.write(sb.toString().getBytes("UTF-8"));
        out.flush();

        if (file != null && file.isFile()) {
            out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));    ///경계2 = 파일명, 파일이진데이터
            StringBuilder fileString = new StringBuilder();
            fileString.append("Content-Disposition:form-data; name=\"file\"; filename=");
            fileString.append("\"" + file.getName() + "\"\r\n");
            fileString.append("Content-Type: application/octet-stream\r\n\r\n");
            out.write(fileString.toString().getBytes("UTF-8"));
            out.flush();

            try (FileInputStream fis = new FileInputStream(file)) { //파일 이진 처리
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.write("\r\n".getBytes());
            }

            out.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));  ///경계3
        }
        out.flush();
    }

}
