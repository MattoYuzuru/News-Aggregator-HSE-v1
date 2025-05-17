package com.news.aianalysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.ConfigLoader;
import okhttp3.OkHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class HuggingFaceClient {
    private static final String API_URL = "https://api-inference.huggingface.co/models/";
    private final OkHttpClient client;
    private final String repoId;
    private final String apiKey;
    private final Double temperature;
    private final int maxLength;
    private final int maxRetries;
    private final long retryDelay;

    public String summarize(String text) {
        try {
            String model = "facebook/bart-large-cnn";

            // Create a proper JSON request with parameters
            JSONObject requestObj = new JSONObject();
            requestObj.put("inputs", text);

            // Add these parameters to handle token limits
            JSONObject parameters = new JSONObject();
            parameters.put("max_length", 2000);     // Maximum tokens in output
            parameters.put("min_length", 30);      // Minimum tokens in output
            parameters.put("do_sample", false);    // Deterministic generation
            parameters.put("truncation", true);    // Enable input truncation
            requestObj.put("parameters", parameters);

            String requestBody = requestObj.toString();
            String response = post(model, requestBody);

            if (!response.isEmpty()) {
                try {
                    if (response.startsWith("[")) {
                        JSONArray jsonArray = new JSONArray(response);
                        if (!jsonArray.isEmpty()) {
                            return jsonArray.getJSONObject(0).getString("summary_text");
                        }
                    } else {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("summary_text")) {
                            return jsonObject.getString("summary_text");
                        }
                    }
                } catch (JSONException e) {
                    System.err.println("JSON parsing error: " + e.getMessage());
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

            JSONArray labelsArray = new JSONArray();
            List<String> commonPlaces = ConfigLoader.countries();
            for (String place : commonPlaces) {
                labelsArray.put(place);
            }

            JSONObject requestObject = new JSONObject();
            requestObject.put("inputs", text);
            JSONObject parameters = new JSONObject();
            parameters.put("candidate_labels", labelsArray);
            parameters.put("truncation", true);
            parameters.put("max_length", 2048);
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
            parameters.put("truncation", true);
            parameters.put("max_length", 512);
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
            conn.setRequestProperty("Authorization", "Bearer " + );
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