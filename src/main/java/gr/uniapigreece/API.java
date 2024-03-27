package gr.uniapigreece;
import gr.unistudents.services.student.StudentService;
import gr.unistudents.services.student.components.Options;
import gr.unistudents.services.student.components.ScraperOutput;
import gr.unistudents.services.student.components.StudentResponse;
import gr.unistudents.services.student.components.University;
import gr.unistudents.services.student.exceptions.NotAuthorizedException;
import gr.unistudents.services.student.exceptions.NotReachableException;
import gr.unistudents.services.student.exceptions.ParserException;
import gr.unistudents.services.student.exceptions.ScraperException;
import gr.unistudents.services.student.models.*;
import gr.unistudents.services.student.scrapers.UOPScraper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
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
    public JSONObject processInput(@RequestBody String input) throws NotAuthorizedException, IOException {
        JSONParser parser = new JSONParser();
        JSONObject output = new JSONObject();
        JSONObject json = null;
        try {json = (JSONObject) parser.parse(input);}
        catch (ParseException e) {return ParserExceptionJSON;}
        Options options = new Options();
        options.username = json.get("u").toString();
        options.password = json.get("p").toString();
        options.university = "uop.gr";
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
        int TotalCoursesNull = 0;
        int TotalCoursesFailed = 0;
        int TotalCoursesInSecretary = 0;
        double TotalCoursesECTSInSecretary = 0.0;
        JSONArray COURSE_MAIN_ARRAY = new JSONArray();
        ArrayList<Semester> s = sP.semesters;
        for (int i = 0; i < s.size(); i++) {
            Semester semester = s.get(i);
            double ECTS_TOTAL_PER_CLASS = 0.0;
            JSONObject SEM = new JSONObject();
            SEM.put("semesterECTS" , semester.ects);
            SEM.put("semesterTOTALCLASS" , semester.courses.size());
            SEM.put("semesterNum" , (semester.name == null ? i+1 : semester.name));
            SEM.put("semesterAvgGrade" , semester.averageGrade);
            JSONArray  CLASSES_PER_SEMESTER = new JSONArray();
            ArrayList<Course> classes = semester.courses;
            for (Course course : classes) {
            JSONObject CLASS = new JSONObject();
            CLASS.put("course_name" , course.name);
            CLASS.put("course_grade", (course.latestExamGrade == null ? "-" : course.latestExamGrade.grade));
            CLASS.put("course_exam_period" , (course.latestExamGrade == null ? "-" : course.latestExamGrade.displayPeriod));
            CLASS.put("course_code" , course.displayCode);
            CLASS.put("course_ects" , course.ects);
            CLASS.put("course_id" , (course.latestExamGrade == null ? null : course.latestExamGrade.externalId));
            CLASSES_PER_SEMESTER.add(CLASS);
            if(course.subCourses != null || course.subCourses.size() > 0){
                for(Course subCourse : course.subCourses){
                    JSONObject SCLASS = new JSONObject();
                    SCLASS.put("course_name" , subCourse.name);
                    SCLASS.put("course_grade", (subCourse.latestExamGrade == null ? "-" : subCourse.latestExamGrade.grade));
                    SCLASS.put("course_exam_period" , (subCourse.latestExamGrade == null ? "-" : subCourse.latestExamGrade.displayPeriod));
                    SCLASS.put("course_code" , subCourse.displayCode);
                    SCLASS.put("course_ects" , subCourse.ects);
                    SCLASS.put("SUBCOURSE" , true);
                    SCLASS.put("course_id" , (subCourse.latestExamGrade == null ? null : subCourse.latestExamGrade.externalId));
                    SCLASS.put("course_instructor" , subCourse.instructor);
                    CLASSES_PER_SEMESTER.add(SCLASS);
                    if(subCourse.latestExamGrade == null) TotalCoursesNull++;
                    if(subCourse.latestExamGrade != null)
                    if(subCourse.latestExamGrade.grade != null)
                    if(subCourse.latestExamGrade.grade < 5.0)
                    TotalCoursesFailed++;
                }
            }
            CLASS.put("course_instructor" , course.instructor);
            ECTS_TOTAL_PER_CLASS += course.ects;
            if(course.latestExamGrade == null) TotalCoursesNull++;
            if(course.latestExamGrade != null)
            if(course.latestExamGrade.grade != null)
            if(course.latestExamGrade.grade < 5.0)
            TotalCoursesFailed++;
            TotalCoursesECTSInSecretary += course.ects;
            }
            TotalCoursesInSecretary += semester.courses.size();
            SEM.put("ECTS_TOTAL_ALL_CLASS" , ECTS_TOTAL_PER_CLASS);
            SEM.put("courses", CLASSES_PER_SEMESTER);
            COURSE_MAIN_ARRAY.add(SEM);
        }
        output.put("totalECTS" , sP.ects);
        output.put("totalPassed", sP.passedCourses);
        output.put("totalFailed" , TotalCoursesFailed);
        output.put("totalNull" , TotalCoursesNull);
        output.put("avgGrade" , sP.averageGrade);
        output.put("weightedAverageGrade" , sP.weightedAverageGrade);
        output.put("success" , "200");
        output.put("fname"   ,  sinfo.firstName);
        output.put("lname"   ,  sinfo.lastName);
        output.put("aem"     ,  sinfo.aem);
        output.put("year"     , sinfo.registrationYear);
        output.put("currentSemester" , sinfo.currentSemester);
        output.put("department" , sinfo.departmentTitle);
        output.put("programmeTitle" , sinfo.programTitle);
        output.put("studentSemester" , sinfo.currentSemester);
        output.put("studentTotalClassesInSecretary" , TotalCoursesInSecretary);
        output.put("studentTotalECTSInSecretary" , TotalCoursesECTSInSecretary);
        output.put("coursesArray",COURSE_MAIN_ARRAY);
        output.put("bearer" , r.cookies.get("token"));
        ScrapeThesis thesis = new ScrapeThesis(r.cookies.get("token"));
        JSONArray THESIS = new JSONArray();
        JSONObject THESISOBJ = new JSONObject();
        THESISOBJ.put("hasThesis" , thesis.hasThesis);
        THESISOBJ.put("name" , thesis.thesis_name);
        THESISOBJ.put("instructor" , thesis.thesis_instructor);
        THESIS.add(THESISOBJ);
        output.put("thesis" , THESIS);
        ScrapeGradutionRules grad = new ScrapeGradutionRules(r.cookies.get("token"));
        output.put("graduation" , grad.grad_rules);
        return output;
    }
}
