package core.controllers;

import core.entities.Course;
import core.entities.Lesson;
import core.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CourseWebAndApiController {

    private final CourseRepository courseRepo;

    @Autowired
    public CourseWebAndApiController(CourseRepository courseRepo) {
        this.courseRepo = courseRepo;
    }

    // ------------------ WEB ------------------

    @GetMapping("/courses")
    public String showCoursesList(Model model) {
        model.addAttribute("courses", courseRepo.findAll());
        return "courses";
    }

    @GetMapping("/courses/new")
    public String showNewCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "course-form";
    }

    @PostMapping("/courses")
    public String createCourse(@ModelAttribute Course course) {
        courseRepo.save(course);
        return "redirect:/courses";
    }

    @GetMapping("/courses/{id}/lessons/new")
    public String showNewLessonForm(@PathVariable Long id, Model model) {
        model.addAttribute("lesson", new Lesson());
        model.addAttribute("courseId", id);
        return "lesson-form";
    }

    @PostMapping("/courses/{id}/lessons")
    public String addLessonToCourse(@PathVariable Long id,
                                    @ModelAttribute Lesson lesson) {
        courseRepo.findById(id).ifPresent(course -> {
            lesson.setCourse(course);
            course.getLessons().add(lesson);
            courseRepo.save(course);
        });
        return "redirect:/courses";
    }

    // ------------------ API ------------------

    @RestController
    @RequestMapping("/api/courses")
    static class CourseApi {

        private final CourseRepository repo;

        @Autowired
        public CourseApi(CourseRepository repo) {
            this.repo = repo;
        }

        @GetMapping
        public List<Course> listAll() {
            return repo.findAll();
        }

        @GetMapping("/{id}")
        public ResponseEntity<Course> getOne(@PathVariable Long id) {
            return repo.findById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }

        @PostMapping
        public Course create(@RequestBody Course c) {
            return repo.save(c);
        }

        @PostMapping("/{courseId}/lessons")
        public ResponseEntity<String> addLesson(@PathVariable Long courseId,
                                                @RequestBody Lesson lesson) {
            return repo.findById(courseId)
                    .map(course -> {
                        lesson.setCourse(course);
                        course.getLessons().add(lesson);
                        repo.save(course);
                        return ResponseEntity.ok("Урок добавлен");
                    })
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
    }
}