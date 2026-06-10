import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PromptDemo {
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String MODEL = "qwen-plus";

    public static void main(String[] args) throws IOException {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("请先设置 DASHSCOPE_API_KEY");
            return;
        }
        System.out.println("=== Prompt Engineering 对比 ===");
        System.out.println();

        // Test 1: Zero-shot
        System.out.println("[Test 1] Zero-shot");
        String resp1 = callApi(buildZeroShotBody());
        System.out.println(resp1);


    }

    private static String buildZeroShotBody() {
        return "{"
                + "  \"model\": \"" + MODEL + "\","
                + "  \"messages\": ["
                + "    {"
                + "      \"role\": \"system\","
                + "      \"content\": \"You are a Java tutor.\""
                + "    },"
                + "    {"
                + "      \"role\": \"user\","
                + "      \"content\": \"什么是 JVM 内存模型？\""
                + "    }"
                + "  ]"
                + "}";
    }

    private static String callApi(String jsonBody) throws IOException {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        HttpURLConnection conn = null;
        try {

            URL url = new URL(API_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            // 发送请求体
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 读取响应
            int statusCode = conn.getResponseCode();
            InputStream inputStream = (statusCode >= 200 && statusCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            return response.toString();

        } finally {
            if (conn != null) conn.disconnect();
        }

    }


}