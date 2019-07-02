package entity;

import table.Table;

public class Choice {
    private String student_id;
    private String course_id;
    private String time ;

    public Choice(String student_id, String course_id, String time) {
        this.student_id = student_id;
        this.course_id = course_id;
        this.time = time;
    }

    public Choice(String student_id, String course_id) {
        this.student_id = student_id;
        this.course_id = course_id;
    }

    @Override
    public String toString() {
        return "Choice{" +
                "student_id='" + student_id + '\'' +
                ", course_id='" + course_id + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getCourse_id() {
        return course_id;
    }

    public void setCourse_id(String course_id) {
        this.course_id = course_id;
    }
}
