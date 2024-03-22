package gr.uniapigreece;


import gr.unistudents.services.student.components.University;
import gr.unistudents.services.student.exceptions.NotAuthorizedException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ScrapeExamStatistics {

    private JSONObject ParserExceptionJSON = new JSONObject();
    private JSONObject NotAuthorizedException = new JSONObject();
    private JSONObject ParserException = new JSONObject();
    ScrapeExamStatistics(){
        ParserExceptionJSON.put("exception" , "Πρόβλημα στον Docker\nParser Exception Json");
        NotAuthorizedException.put("exception" , "Λάθος Στοιχεία Σύνδεσης");
        ParserException.put("exception" , "Δεν υπάρχουν αποτελέσματα");
    }


    @PostMapping("/exam")
    public String getInfo(@RequestBody String input) throws NotAuthorizedException, IOException {

        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {json = (JSONObject) parser.parse(input);}
        catch (ParseException e) {return ParserExceptionJSON.toJSONString();}
        OkHttpClient clientWithRedirects = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://uniapi.uop.gr/api/students/me/exams/" + json.get("course_id") + "/statistics?$top=-1&$count=false")
                .header("Authorization", "Bearer " + json.get("bearer"))
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();
        try (Response response = clientWithRedirects.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String res = Jsoup.parse(response.body().string()).text();
                return res;
            } else if (response.code() == 401) {
                return NotAuthorizedException.toJSONString();
            } else {
                return ParserException.toJSONString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error executing HTTP request: " + e.getMessage(), e);
        }
    }
}

