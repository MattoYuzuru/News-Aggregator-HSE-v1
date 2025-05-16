package com.news.aianalysis;

import com.news.ConfigLoader;
import org.json.JSONArray;
import org.json.JSONObject;

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
        try {
            String model = "facebook/bart-large-cnn";
            String requestBody = "{\"inputs\": " + quote(text) + "}";
            String response = post(model, requestBody);

            if (response.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(response);
                if (jsonArray.length() > 0) {
                    return jsonArray.getJSONObject(0).getString("summary_text");
                }
            } else if (response.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("summary_text")) {
                    return jsonObject.getString("summary_text");
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error in summarize: " + e.getMessage());
            return null;
        }
    }

    public String classifyRegion(String text) {
        try {
            String model = "facebook/bart-large-mnli";
            List<String> labels = ConfigLoader.countries();
            JSONArray labelsArray = new JSONArray();
            for (String label : labels) {
                labelsArray.put(label);
            }

            JSONObject requestObject = new JSONObject();
            requestObject.put("inputs", text);
            JSONObject parameters = new JSONObject();
            parameters.put("candidate_labels", labelsArray);
            requestObject.put("parameters", parameters);

            String response = post(model, requestObject.toString());
            if (!response.isEmpty()) {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray labelsResponse = jsonResponse.getJSONArray("labels");
                if (!labelsResponse.isEmpty()) {
                    return labelsResponse.getString(0);
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error in classifyRegion: " + e.getMessage());
            return null;
        }
    }

    public List<String> generateTags(String text) {
        try {
            String model = "facebook/bart-large-mnli";
            List<String> labels = List.of("Politics", "Economy", "Japan", "Technology", "AI");
            JSONArray labelsArray = new JSONArray();
            for (String label : labels) {
                labelsArray.put(label);
            }

            JSONObject requestObject = new JSONObject();
            requestObject.put("inputs", text);
            JSONObject parameters = new JSONObject();
            parameters.put("candidate_labels", labelsArray);
            requestObject.put("parameters", parameters);

            String response = post(model, requestObject.toString());
            if (!response.isEmpty()) {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray labelsResponse = jsonResponse.getJSONArray("labels");
                JSONArray scoresResponse = jsonResponse.getJSONArray("scores");

                List<String> result = new ArrayList<>();
                for (int i = 0; i < labelsResponse.length(); i++) {
                    if (scoresResponse.getDouble(i) >= 0.5) {
                        result.add(labelsResponse.getString(i));
                    }
                }
                return result;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error in generateTags: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String post(String model, String body) {
        try {
            URL url = new URL("https://api-inference.huggingface.co/models/" + model);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);  // 30 seconds timeout
            conn.setReadTimeout(30000);     // 30 seconds timeout

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
                os.flush();
            }

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

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                return sb.toString();
            }
        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
            return "";
        }
    }

    private String quote(String str) {
        return "\"" + str.replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}