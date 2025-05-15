package com.news.aianalysis;

import com.news.ConfigLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class HuggingFaceClient {
    private final String apiKey;

    public HuggingFaceClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public String summarize(String text) {
        String model = "facebook/bart-large-cnn";
        String requestBody = "{\"inputs\": " + quote(text) + "}";
        String response = post(model, requestBody);
        return extractField(response, "summary_text");
    }

    public String classifyRegion(String text) {
        String model = "facebook/bart-large-mnli";
        List<String> labels = ConfigLoader.countries();  // Fix?
        String jsonLabels = "[\"" + String.join("\", \"", labels) + "\"]";

        String requestBody = String.format(
                "{\"inputs\": %s, \"parameters\": {\"candidate_labels\": %s}}",
                quote(text), jsonLabels
        );

        String response = post(model, requestBody);
        return extractFirstLabel(response);
    }

    public List<String> generateTags(String text) {
        String model = "facebook/bart-large-mnli";
        List<String> labels = List.of("Politics", "Economy", "Japan", "Technology", "AI");
        String jsonLabels = "[\"" + String.join("\", \"", labels) + "\"]";

        String requestBody = String.format(
                "{\"inputs\": %s, \"parameters\": {\"candidate_labels\": %s}}",
                quote(text), jsonLabels
        );

        String response = post(model, requestBody);
        return extractTopLabels(response, 0.5);
    }

    private String post(String model, String body) {
        try {
            URL url = new URL("https://api-inference.huggingface.co/models/" + model);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // First send the request body
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
                os.flush();
            }

            // Then check the response code
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("HF error: " + responseCode);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    System.err.println("Error response: " + sb.toString());
                }
                return "";
            }

            // Read the successful response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String extractField(String json, String fieldName) {
        int index = json.indexOf(fieldName);
        if (index == -1) return null;

        int start = json.indexOf(":", index) + 1;
        int quote1 = json.indexOf('"', start);
        int quote2 = json.indexOf('"', quote1 + 1);

        return quote1 != -1 && quote2 != -1 ? json.substring(quote1 + 1, quote2) : null;
    }

    private String extractFirstLabel(String json) {
        int i = json.indexOf("\"labels\"");
        if (i == -1) return null;
        int open = json.indexOf("[", i);
        int quote1 = json.indexOf('"', open);
        int quote2 = json.indexOf('"', quote1 + 1);
        return json.substring(quote1 + 1, quote2);
    }

    private List<String> extractTopLabels(String json, double threshold) {
        List<String> labels = new ArrayList<>();
        int i = json.indexOf("\"labels\"");
        int j = json.indexOf("\"scores\"");

        if (i == -1 || j == -1) return labels;

        List<String> labelList = extractJsonArray(json.substring(i));
        List<String> scoreList = extractJsonArray(json.substring(j));

        for (int k = 0; k < Math.min(labelList.size(), scoreList.size()); k++) {
            double score = Double.parseDouble(scoreList.get(k));
            if (score >= threshold) {
                labels.add(labelList.get(k));
            }
        }

        return labels;
    }

    private List<String> extractJsonArray(String json) {
        List<String> result = new ArrayList<>();
        int open = json.indexOf("[");
        int close = json.indexOf("]");
        if (open == -1 || close == -1) return result;

        String content = json.substring(open + 1, close);
        for (String part : content.split(",")) {
            String trimmed = part.trim().replace("\"", "");
            if (!trimmed.isEmpty()) result.add(trimmed);
        }

        return result;
    }

    private String quote(String str) {
        return "\"" + str.replace("\"", "\\\"") + "\"";
    }
}