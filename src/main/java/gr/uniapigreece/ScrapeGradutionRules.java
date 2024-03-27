package gr.uniapigreece;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import java.io.IOException;
import java.util.HashMap;

public class ScrapeGradutionRules {
    public JSONArray grad_rules;
    public HashMap<String,String> map;
    private String token;
    ScrapeGradutionRules(String token) throws IOException {
        this.token = token;
        map = new HashMap<>();
        setGrad(getGrad());
    }

    public String getGrad() throws IOException {
        OkHttpClient clientWithRedirects = this.buildClient( true);
        Request request = (new Request.Builder()).url("https://uniapi.uop.gr/api/students/me/graduationRules?$expand=validationResult&$top=-1&$count=false").header("Authorization", "Bearer " + token).addHeader("User-Agent", "Mozilla/5.0").build();
        Response response = clientWithRedirects.newCall(request).execute();
        String res = Jsoup.parse(response.body().string()).text();
        if (!res.contains("Token was expired or is in invalid state") && !res.contains("E_TOKEN_EXPIRED") && !res.contains("Invalid token")) {
            return res;
        } else {
            return "Κάτι πήγε Λάθος! Προσπαθήστε ξανά αργότερα.";
        }
    }

    private void setGrad(String i) throws JsonProcessingException {
        try {
            grad_rules = new JSONArray();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(i);
            JsonNode graduationRulesNode = rootNode.path("graduationRules");
            for (JsonNode ruleNode : graduationRulesNode) {
                JsonNode validationResultNode = ruleNode.path("validationResult");
                int result = validationResultNode.path("data").path("result").asInt();
                String max = validationResultNode.path("data").path("value1").asText();
                String message = validationResultNode.path("message").asText();
                JSONObject OBJ = new JSONObject();
                OBJ.put("message" , message);
                OBJ.put("result" , result);
                OBJ.put("max" , max);
                grad_rules.add(OBJ);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static OkHttpClient buildClient(boolean allowRedirects) {
        OkHttpClient.Builder builder = (new OkHttpClient.Builder()).followRedirects(allowRedirects).followSslRedirects(allowRedirects);
        return builder.build();
    }
}
