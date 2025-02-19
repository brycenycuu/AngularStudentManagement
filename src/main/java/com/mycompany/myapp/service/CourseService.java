package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Course;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.UserCourse;
import com.mycompany.myapp.domain.dto.CourseDto;
import com.mycompany.myapp.domain.dto.CourseWithTNDto;
import com.mycompany.myapp.repository.CourseRepository;
import com.mycompany.myapp.repository.UserCourseRepository;
import com.mycompany.myapp.repository.UserRepository;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserCourseRepository userCourseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    List<CourseDto> courseDtos = new ArrayList<>();

    public List<CourseDto> findAllCourses() {

        //Cache
        if (courseDtos.isEmpty()) {
            List<Course> courses = courseRepository.findAll();

            for (Course c : courses) {
                courseDtos.add(new CourseDto(c.getCourseName(), c.getCourseLocation(), c.getCourseContent(), c.getTeacherId()));
            }

            return courseDtos;
        }

        return courseDtos;
    }

    public List<CourseDto> findAllCoursesLengthLargerThan10() {

        List<CourseDto> courseDtos = new ArrayList<>();
        List<Course> courses = courseRepository.findAll();

        for (Course c: courses) {
            if (c.getCourseName().length() > 10) {
                courseDtos.add(new CourseDto(c.getCourseName(), c.getCourseLocation(), c.getCourseContent(), c.getTeacherId()));
            }
        }

        return courseDtos;
    }


//    Next Lecture

    public List<CourseDto> findAllCoursesDtoFromDB(){
        return courseRepository.findAllCoursesDto();
    }

    public List<CourseWithTNDto> findAllRegisteredCourses(Long userId) {

        Optional<User> curUser = userRepository.findOneById(userId);
        List<CourseWithTNDto> courseTNDtos = new ArrayList<>();

        if (curUser.isPresent()) {
            User user = curUser.get();
            Optional<List<UserCourse>> userCourseOp = userCourseRepository.findAllByUser(user);
            if (userCourseOp.isPresent()) {
                List<UserCourse> userCourse = userCourseOp.get();
                for (UserCourse uc : userCourse) {
                    Course ucCourse = uc.getCourse();
                    Optional<User> teacher = userRepository.findOneById(ucCourse.getTeacherId());
                    if (teacher.isPresent()) {
                        User t = teacher.get();
                        courseTNDtos.add(new CourseWithTNDto(ucCourse.getCourseName(), ucCourse.getCourseLocation(), ucCourse.getCourseContent(), t.getLogin()));
                    }
                }
            }
        }
        return courseTNDtos;

    }


    public void registerCourse(String courseName) throws Exception{
        Optional<User> curUser = userService.getUserWithAuthorities();
        Optional<Course> curCourse = courseRepository.findCourseByCourseName(courseName);

        if (curUser.isPresent() && curCourse.isPresent()){
            userCourseRepository.save(UserCourse.builder()
                .user(curUser.get())
                .course(curCourse.get())
                .build());
        } else {
            throw new Exception("UnExpected Exception");
        }
    }

    public void addCourse(CourseDto course) throws Exception{
        Optional<Course> courseDto = courseRepository.findCourseByCourseName(course.getCourseName());

        if(courseDto.isPresent()){
            throw new Exception("Course is existing.");
        }

        Course courseBeingSaved = Course.builder()
            .courseName(course.getCourseName())
            .courseContent(course.getCourseContent())
            .courseLocation(course.getCourseLocation())
            .teacherId(course.getTeacherId())
            .build();

        System.out.print("in");

        try {
            courseRepository.saveAndFlush(courseBeingSaved);
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }

    }

    public void deleteCourse(String courseName) throws Exception{
        Optional<Course> OptionalExistingCourse = courseRepository.findCourseByCourseName(courseName);

        if(!OptionalExistingCourse.isPresent()){
            throw new Exception("Course is not exist.");
        }

        try {
            courseRepository.delete(OptionalExistingCourse.get());
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }


    public void updateCourse(CourseDto course) throws Exception{
        Optional<Course> OptionalExistingCourse = courseRepository.findCourseByCourseName(course.getCourseName());

        if(!OptionalExistingCourse.isPresent()){
            throw new Exception("Course is not exist.");
        }

        Course existingCourse = OptionalExistingCourse.get();
        existingCourse.setCourseContent(course.getCourseContent());
        existingCourse.setCourseLocation(course.getCourseLocation());
        existingCourse.setCourseName(course.getCourseName());
        existingCourse.setTeacherId(course.getTeacherId());

    }

//    public void addCourseToStudent(UserCourse userCourse) throws Exception {
//
//        Optional<User> curUser = userService.getUserWithAuthorities();
//        // 2 find course from course table
//
//        UserCourse t1 =  UserCourse.builder()
//            .course(c1)
//            .user(curUser)
//            .build();
//
//        try {
//            UserCourseRepository.saveAndFlush(t1);
//        } catch (Exception e){
//            throw new Exception(e.getMessage());
//        }
//    }

}
