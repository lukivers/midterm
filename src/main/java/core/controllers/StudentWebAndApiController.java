package core.controllers;

import core.entities.Student;
import core.entities.StudentProfile;
import core.repositories.CourseRepository;
import core.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class StudentWebAndApiController {

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;

    @Autowired
    public StudentWebAndApiController(StudentRepository studentRepo, CourseRepository courseRepo) {
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
    }

    @GetMapping("/students")
    public String showAllStudents(Model model) {
        model.addAttribute("students", studentRepo.findAll());
        return "students";
    }

    @GetMapping("/students/new")
    public String showNewStudentForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("profile", new StudentProfile());
        return "student-form";
    }

    @PostMapping("/students")
    public String createStudentFromForm(@ModelAttribute Student student,
                                        @RequestParam String address,
                                        @RequestParam String phone,
                                        @RequestParam String birthDate,
                                        Model model) {

        Optional<Student> existing = studentRepo.findByEmail(student.getEmail());
        if (existing.isPresent()) {
            model.addAttribute("error", "Email уже используется другим студентом");
            model.addAttribute("student", student);
            model.addAttribute("profile", new StudentProfile());
            return "student-form";
        }

        StudentProfile profile = new StudentProfile();
        profile.setAddress(address);
        profile.setPhone(phone);
        profile.setBirthDate(LocalDate.parse(birthDate));
        student.setProfile(profile);

        studentRepo.save(student);
        return "redirect:/students";
    }

    @DeleteMapping("/students/{id}")
    public String removeStudentFromWeb(@PathVariable Long id) {
        studentRepo.deleteById(id);
        return "redirect:/students";
    }


    @RestController
    @RequestMapping("/api/students")
    static class StudentApi {

        private final StudentRepository repo;
        private final CourseRepository courseRepo;

        @Autowired
        public StudentApi(StudentRepository repo, CourseRepository courseRepo) {
            this.repo = repo;
            this.courseRepo = courseRepo;
        }

        @GetMapping
        public List<Student> getAll() {
            return repo.findAll();
        }

        @GetMapping("/{id}")
        public ResponseEntity<Student> getOne(@PathVariable Long id) {
            return repo.findById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }

        @PostMapping
        public Student addStudent(@RequestBody Student s) {
            return repo.save(s);
        }

        @PostMapping("/{sid}/courses/{cid}")
        public ResponseEntity<String> linkCourse(@PathVariable Long sid, @PathVariable Long cid) {
            Student student = repo.findById(sid).orElse(null);
            if (student == null) return ResponseEntity.notFound().build();

            courseRepo.findById(cid).ifPresent(course -> {
                student.getCourses().add(course);
                repo.save(student);
            });

            return ResponseEntity.ok("Курс назначен");
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<String> remove(@PathVariable Long id) {
            if (!repo.existsById(id)) return ResponseEntity.notFound().build();
            repo.deleteById(id);
            return ResponseEntity.ok("Студент удалён");
        }
    }
}