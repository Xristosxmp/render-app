package gr.uniapigreece;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class API {

    @PostMapping("/processInput")
    public JSONObject processInput(@RequestBody String userInput) {
        JSONObject output = new JSONObject();
        output.put("t" , "sss");
        return output;
    }
}
