package gr.uniapigreece;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class API {

    @PostMapping("/v1")
    public JSONObject processInput(@RequestBody String userInput) {
        JSONObject output = new JSONObject();
        output.put("message" , "this is a message from docker");
        return output;
    }
}
