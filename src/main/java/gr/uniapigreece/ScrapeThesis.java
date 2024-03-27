package gr.uniapigreece;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;

import java.io.IOException;

public class ScrapeThesis {

    public boolean hasThesis = false;
    public String thesis_name;
    public String thesis_instructor;

    String token;
    ScrapeThesis(String token) throws IOException {
        this.token = token;
        SetThesis(getThesis());
    }

    public String getThesis() throws IOException {
        OkHttpClient clientWithRedirects = this.buildClient( true);
        Request request = (new Request.Builder()).url("https://uniapi.uop.gr/api/students/me/theses?$expand=results($orderby=index;$expand=instructor($select=InstructorSummary)),thesis($expand=instructor($select=InstructorSummary),locale)&$top=-1&$count=false").header("Authorization", "Bearer " + token).addHeader("User-Agent", "Mozilla/5.0").build();
        Response response = clientWithRedirects.newCall(request).execute();
        String res = Jsoup.parse(response.body().string()).text();
        if (!res.contains("Token was expired or is in invalid state") && !res.contains("E_TOKEN_EXPIRED") && !res.contains("Invalid token")) {
            return res;
        } else {
            return "Λάθος όνομα χρήστη ή κωδικός πρόσβασης.";
        }
    }

    private void SetThesis(String i) throws JsonProcessingException {
        JsonNode node = (new ObjectMapper()).readTree(i);
        if(!node.path("value").isEmpty()){
            hasThesis = true;
            thesis_name = node.at("/value/0/thesis/name").asText();
            thesis_instructor = node.at("/value/0/thesis/instructor/familyName").asText() + " " + node.at("/value/0/thesis/instructor/givenName").asText();
        }
    }

    private static OkHttpClient buildClient(boolean allowRedirects) {
        OkHttpClient.Builder builder = (new OkHttpClient.Builder()).followRedirects(allowRedirects).followSslRedirects(allowRedirects);
        return builder.build();
    }
}
