package entity;

public class Course {
    private String id;
    private String name;
    private int totalSize;
    private int remainSize;

    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", totalSize=" + totalSize +
                ", remainSize=" + remainSize +
                '}';
    }

    public Course(String id, String name, int totalSize, int remainSize) {
        this.id = id;
        this.name = name;
        this.totalSize = totalSize;
        this.remainSize = remainSize;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public int getRemainSize() {
        return remainSize;
    }

    public void setRemainSize(int remainSize) {
        this.remainSize = remainSize;
    }
}
