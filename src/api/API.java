package api;

import entity.Choice;
import entity.Course;
import entity.Student;
import table.ChoiceTable;
import table.CourseTable;
import table.StudentTable;

import java.util.Date;
import java.util.List;

public class API {
    private ChoiceTable choiceTable;
    private StudentTable studentTable;
    private CourseTable courseTable;

    public API() {
        this.studentTable = new StudentTable();
        this.courseTable = new CourseTable();
        this.choiceTable = new ChoiceTable(studentTable, courseTable);
    }

    public String insertStudent(Student student) {
        return studentTable.insertStudent(student);
    }

    public String deleteStudent(String id) {
        return studentTable.deleteStudent(id, choiceTable);
    }

    public String updateStudent(Student student) {
        return studentTable.updateStudent(student);
    }

    public Student getStudent(String id)
    {
        return studentTable.getStudent(id);
    }

    public List<Student> getStudentByRange(int left ,int right)
    {
        return studentTable.getStudentsByRange(left,right);
    }

    public String insertCourse(Course course)
    {
        return courseTable.insertCourse(course);
    }

    public String deleteCourse(String id)
    {
        return courseTable.deleteCourse(id , choiceTable);
    }

    public String updatCourse(Course course)
    {
        return courseTable.updateCourse(course);
    }

    public Course getCourse(String id)
    {
        return courseTable.getCourse(id);
    }

    public List<Course> getCourseByRange(int left ,int right)
    {
        return courseTable.getCoursesByRange(left, right);
    }

    public String insertChoice(String studentId, String courseId)
    {
        return choiceTable.insertChoice(new Choice(studentId, courseId , String.valueOf(new Date().getTime())));
    }

    public String deleteChoice(String studentId, String courseId)
    {
        return choiceTable.deleteChoice(new Choice(studentId, courseId));
    }

    public Choice getChoice(String studentId, String courseId)
    {
        return choiceTable.getChoice(studentId,courseId);
    }

    public List<Choice> getChoiceByRange(int left , int right)
    {
        return getChoiceByRange(left, right);
    }

}
