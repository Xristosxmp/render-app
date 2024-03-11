package gr.uniapigreece;

import gr.unistudents.services.student.StudentService;
import gr.unistudents.services.student.components.Options;
import gr.unistudents.services.student.components.StudentResponse;
import gr.unistudents.services.student.exceptions.NotAuthorizedException;
import gr.unistudents.services.student.exceptions.NotReachableException;
import gr.unistudents.services.student.exceptions.ParserException;
import gr.unistudents.services.student.exceptions.ScraperException;
import gr.unistudents.services.student.models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class API {

    private JSONObject ParserExceptionJSON = new JSONObject();
    private JSONObject StudentServiceExceptionJSON = new JSONObject();
    private JSONObject ScraperExceptionJSON = new JSONObject();
    private JSONObject NotReachableException = new JSONObject();
    private JSONObject NotAuthorizedException = new JSONObject();
    private JSONObject ParserException = new JSONObject();

    API(){
        ParserExceptionJSON.put("exception" , "Πρόβλημα στον Docker\nParser Exception Json");
        StudentServiceExceptionJSON.put("exception" , "Πρόβλημα στον StudentService");
        ScraperExceptionJSON.put("exception" , "Πρόβλημα στον ScraperOutput");
        NotReachableException.put("exception" , "Το σύστημα της σχολής δεν ανταποκρίνεται");
        NotAuthorizedException.put("exception" , "Λάθος Στοιχεία Σύνδεσης");
        ParserException.put("exception" , "Πρόβλημα με τον Parser");

    }

    @PostMapping("/v1")
    public JSONObject processInput(@RequestBody String input) {
        JSONParser parser = new JSONParser();
        JSONObject output = new JSONObject();
        JSONObject json = null;
        try {json = (JSONObject) parser.parse(input);}
        catch (ParseException e) {return ParserExceptionJSON;}


        Options options = new Options();
        options.username = json.get("u").toString();
        options.password = json.get("p").toString();
        options.university = json.get("i").toString();
        options.system = "";
        options.userAgent = "Mozilla/5.0";
        StudentService ss = null;
        StudentResponse r = null;

        try {ss = new StudentService(options);} catch (Exception e) {return StudentServiceExceptionJSON;}
        try {r = ss.getStudent();} catch (ScraperException e) {return ScraperExceptionJSON;}
        catch (NotReachableException e) {return NotReachableException;}
        catch (NotAuthorizedException e) {return NotAuthorizedException;}
        catch (ParserException e) {return ParserException;}

        Student student = r.getStudent();
        Progress sP     = r.getStudent().progress;
        Info sinfo = student.info;


        System.out.println(sinfo.firstName + " " + sinfo.lastName);
        System.out.println(sinfo.aem);
        System.out.println(sinfo.specialtyId);
        System.out.println(sinfo.specialtyTitle);
        System.out.println(sinfo.registrationYear);
        System.out.println(sinfo.currentSemester);



        JSONArray coursesJSON = new JSONArray();
        ArrayList<Semester> s = sP.semesters;
        for (int i = 0; i < s.size(); i++) {
            Semester semester = s.get(i);

            ArrayList<Course> classes = semester.courses;
            for (Course course : classes) {
                System.out.println(course.name);
            }

        }




        output.put("success" , "200");
        output.put("fname"   ,  sinfo.firstName);
        output.put("lname"   ,  sinfo.lastName);
        output.put("aem"     ,  sinfo.aem);
        output.put("year"     , sinfo.registrationYear);
        output.put("currentSemester" , sinfo.currentSemester);
        output.put("department" , sinfo.departmentTitle);

        return output;
    }
}
