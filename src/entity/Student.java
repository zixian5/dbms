package entity;

public class Student {
    private String id;//16个字符
    private String classroom;//8个字符
    private String name;//4个字符
    private String sex;//1个字符

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", classroom='" + classroom + '\'' +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                '}';
    }

    public Student(String id, String classroom, String name, String sex) {
        this.id = id;
        this.classroom = classroom;
        this.name = name;
        this.sex = sex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) throws Exception {

        this.id = id;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
