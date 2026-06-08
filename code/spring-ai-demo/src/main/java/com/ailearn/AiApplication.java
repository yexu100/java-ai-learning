package com.ailearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class AiApplication {

    public static void main(String[] args) {
        // 启动前先加载 .env 文件，确保 API Key 可用
        loadEnvFile();

        String key = System.getProperty("DASHSCOPE_API_KEY");
        if (key == null || key.isEmpty() || key.contains("把你的Key")) {
            System.err.println("================================================");
            System.err.println("  ERROR: DASHSCOPE_API_KEY 未设置或无效");
            System.err.println("  请编辑项目根目录的 .env 文件：");
            System.err.println("  DASHSCOPE_API_KEY=sk-你在阿里云百炼创建的Key");
            System.err.println("================================================");
            System.exit(1);
        }
        System.out.println("API Key 已加载: " + key.substring(0, 8) + "...");

        SpringApplication.run(AiApplication.class, args);
    }

    /** 从 .env 文件读取环境变量并设到系统属性 */
    private static void loadEnvFile() {
        String[] paths = {
                "../.env",
                "../../.env",
                ".env"
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                System.out.println("找到 .env 文件: " + f.getAbsolutePath());
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            String key = line.substring(0, eq).trim();
                            String val = line.substring(eq + 1).trim();
                            System.setProperty(key, val);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("读取 .env 失败: " + e.getMessage());
                }
                return;
            }
        }
        System.err.println("未找到 .env 文件");
    }
}
