/**
 * Prompt Engineering demo - Compare 3 prompt styles via Aliyun Bailian API.
 *
 * Prerequisites:
 *   1. Register at https://bailian.console.aliyun.com/
 *   2. Create an API Key
 *   3. Set env var DASHSCOPE_API_KEY
 *
 * Compile and run:
 *   cd code\prompt-engineering
 *   javac -encoding utf-8 PromptDemo.java
 *   java PromptDemo
 *
 * API: https://help.aliyun.com/zh/model-studio/use-api-to-apply-model
 */

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Compares 3 prompting techniques:
 * 1. Zero-shot: ask directly
 * 2. Few-shot: give 2 examples first
 * 3. Chain-of-Thought: guide step-by-step reasoning
 */
public class PromptDemo {

    private static final String API_URL =
        "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String MODEL = "qwen-plus";

    // ---- 1. Zero-shot ----
    private static String buildZeroShotBody() {
        return "{"
            + "  \"model\": \"" + MODEL + "\","
            + "  \"messages\": ["
            + "    {"
            + "      \"role\": \"user\","
            + "      \"content\": \"Explain the JVM memory model, including heap, stack, method area, and their relationships.\""
            + "    }"
            + "  ]"
            + "}";
    }

    // ---- 2. Few-shot (give 2 examples) ----
    private static String buildFewShotBody() {
        return "{"
            + "  \"model\": \"" + MODEL + "\","
            + "  \"messages\": ["
            + "    {"
            + "      \"role\": \"user\","
            + "      \"content\": \"Example 1:\\nQ: What is HashMap?\\nA: HashMap is a hash-table-based Map implementation. It uses hashCode() to determine storage location, allows null keys/values, and is not thread-safe. Default capacity 16, load factor 0.75.\""
            + "    },"
            + "    {"
            + "      \"role\": \"assistant\","
            + "      \"content\": \"I understand the format: definition + key features + details.\""
            + "    },"
            + "    {"
            + "      \"role\": \"user\","
            + "      \"content\": \"Example 2:\\nQ: What is a thread pool?\\nA: A thread pool reuses pre-created threads to execute tasks from a work queue. Key params: corePoolSize, maxPoolSize, workQueue. Benefits: less overhead, faster response.\""
            + "    },"
            + "    {"
            + "      \"role\": \"assistant\","
            + "      \"content\": \"Got it: core definition + key features + important params.\""
            + "    },"
            + "    {"
            + "      \"role\": \"user\","
            + "      \"content\": \"Now follow the style above.\\nQ: What is the JVM memory model?\\nA:\""
            + "    }"
            + "  ]"
            + "}";
    }

    // ---- 3. Chain-of-Thought ----
    private static String buildCoTBody() {
        return "{"
            + "  \"model\": \"" + MODEL + "\","
            + "  \"messages\": ["
            + "    {"
            + "      \"role\": \"user\","
            + "      \"content\": \"Let's think step by step. First list reasoning steps, then give final answer.\\n\\nExplain the JVM memory model from:\\n1. Overall architecture\\n2. Heap division and purpose\\n3. Stack structure and characteristics\\n4. Method Area contents\\n5. How they work together\""
            + "    }"
            + "  ]"
            + "}";
    }

    // ---- HTTP helper ----
    private static String callApi(String jsonBody) throws IOException {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException(
                "Please set DASHSCOPE_API_KEY first.\n"
                + "In PowerShell: $env:DASHSCOPE_API_KEY = \"sk-your-key\"");
        }

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

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

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

            if (statusCode != 200) {
                throw new IOException("API error " + statusCode + ": " + response);
            }
            return response.toString();

        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // ---- Extract content from JSON response (simple, no JSON lib) ----
    private static String extractContent(String json) {
        String marker = "\"content\":\"";
        int start = json.indexOf(marker);
        if (start == -1) {
            marker = "\"content\" : \"";
            start = json.indexOf(marker);
        }
        if (start == -1) {
            return "[Cannot parse response - no content field found]";
        }
        start += marker.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "[Parse error]";

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < end) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case 'n': sb.append('\n'); i++; break;
                    case 't': sb.append('\t'); i++; break;
                    case '"': sb.append('"'); i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    default: sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---- Main ----
    public static void main(String[] args) throws IOException {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("==================================================");
            System.out.println("  ERROR: DASHSCOPE_API_KEY not set");
            System.out.println();
            System.out.println("  1. Register at https://bailian.console.aliyun.com/");
            System.out.println("  2. Create an API Key");
            System.out.println("  3. Set env var:");
            System.out.println("     PowerShell: $env:DASHSCOPE_API_KEY = \"sk-xxx\"");
            System.out.println("==================================================");
            return;
        }

        System.out.println("==================================================");
        System.out.println("  Prompt Engineering Comparison");
        System.out.println("  Model: " + MODEL);
        System.out.println("==================================================");
        System.out.println();

        // Test 1: Zero-shot
        System.out.println("[Test 1] Zero-shot");
        System.out.println("------------------------------");
        System.out.println("Sending...");
        String resp1 = callApi(buildZeroShotBody());
        System.out.println(extractContent(resp1));
        System.out.println();

        // Test 2: Few-shot
        System.out.println("[Test 2] Few-shot (2 examples)");
        System.out.println("------------------------------");
        System.out.println("Sending...");
        String resp2 = callApi(buildFewShotBody());
        System.out.println(extractContent(resp2));
        System.out.println();

        // Test 3: Chain-of-Thought
        System.out.println("[Test 3] Chain-of-Thought");
        System.out.println("------------------------------");
        System.out.println("Sending...");
        String resp3 = callApi(buildCoTBody());
        System.out.println(extractContent(resp3));
        System.out.println();

        // Summary
        System.out.println("==================================================");
        System.out.println("  Summary");
        System.out.println("==================================================");
        System.out.println("Zero-shot length: " + extractContent(resp1).length() + " chars");
        System.out.println("Few-shot  length: " + extractContent(resp2).length() + " chars");
        System.out.println("CoT       length: " + extractContent(resp3).length() + " chars");
        System.out.println();
        System.out.println("Compare the quality of the 3 responses.");
        System.out.println("==================================================");
    }
}
