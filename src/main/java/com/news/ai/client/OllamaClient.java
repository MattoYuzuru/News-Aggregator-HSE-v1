package com.news.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.model.RequestModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static com.news.storage.impl.JdbcTagRepository.parseTags;

public class OllamaClient implements AIClient {
    private static final String API_URL = "http://localhost:11434/api/generate";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    private final String modelName;

    public OllamaClient(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public String summarize(String articleContent) throws IOException, InterruptedException {
        String prompt = "Read the following article and generate a concise summary in the same language as the article." +
                " Focus on the main events, people, or topics mentioned.\n" +
                "Return ONLY the summary, without additional comments or formatting.\n" +
                "\n" + articleContent;

        JsonNode jsonNode = generateResponse(prompt);
        return jsonNode.has("response") ? cleanResponse(jsonNode.get("response").asText()) : "No 'response' field in response.";
    }

    @Override
    public String classifyRegion(String articleContent) throws IOException, InterruptedException {
        String shortenedContent = shortenArticleByHalf(articleContent);
        String prompt = "Given the following news article excerpt, identify the geographic region that best represents the main location(s) where the events occur." +
                " If the events are localized to a specific city or prefecture, etc (e.g., Tokyo or Osaka), return the city or prefecture name." +
                " If the events span multiple countries or locations, return a broader region name (e.g., \"East Asia\" for events across Japan and South Korea).\n" +
                "Return ONLY the most appropriate location name without any explanation.\n\n" +
                shortenedContent;

        JsonNode jsonNode = generateResponse(prompt);
        return jsonNode.has("response") ? cleanResponse(jsonNode.get("response").asText()) : "No 'response' field in response.";
    }

    @Override
    public List<String> generateTags(String articleContent) throws IOException, InterruptedException {
        String shortenedContent = shortenArticleByHalf(articleContent);
        String prompt = "Read the article below and generate 1 to 4 relevant tags that best capture the key themes or topics of the article." +
                " Tags can include significant people, events, organizations, locations, or other important concepts mentioned.\n" +
                "Return ONLY the tags as a comma-separated list (like: tag1, tag2), without explanations.\n" +
                "\n" +
                shortenedContent;

        JsonNode jsonNode = generateResponse(prompt);
        return jsonNode.has("response") ? parseTags(cleanResponse(jsonNode.get("response").asText())) : List.of();
    }

    @Override
    public Integer evaluateArticle(String articleTitle) throws IOException, InterruptedException {
        String prompt = "Title: " + articleTitle + "\n" +
                "Target Audience: University students interested in technologies, IT, world news, travelling, advances in science (health, tech), gaming, and developments that may affect the stock market.\n" +
                "Task: Rate how appealing or relevant this article is to the target audience on a scale from 1 to 100, where 1 is very poor and 100 is excellent.\n" +
                "Consider factors like: factual accuracy, writing quality, completeness of information, and overall value to readers.\n" +
                "Output ONLY the number, without explanation.";

        JsonNode jsonNode = generateResponse(prompt);
        if (jsonNode.has("response")) {
            try {
                String response = cleanResponse(jsonNode.get("response").asText());
                return Integer.parseInt(response.trim());
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse rating from response: " + jsonNode.get("response").asText());
                return null;
            }
        }
        return null;
    }

    private JsonNode generateResponse(String prompt) throws IOException, InterruptedException {
        String jsonRequest = objectMapper.writeValueAsString(new RequestModel(modelName, prompt, false));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    private static String shortenArticleByHalf(String article) {
        if (article == null || article.isEmpty()) return "";
        int halfLength = article.length() / 2;
        return article.substring(0, halfLength).trim();
    }

    private static String cleanResponse(String responseText) {
        String endTag = "</think>";
        int end = responseText.indexOf(endTag);
        if (end == -1 || end + endTag.length() >= responseText.length()) {
            return responseText.trim();
        }
        return responseText.substring(end + endTag.length()).trim();
    }
}