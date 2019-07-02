package table;

import entity.Course;
import entity.Student;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MyThread3 extends Thread {
    private CourseTable table;
    private AtomicInteger i;

    public MyThread3(CourseTable courseTable)
    {
        this.table = courseTable;
        i = new AtomicInteger(0);
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+new Date().getTime()+"start");
        try {
            System.out.println(table.insertCourse(new Course(String.valueOf(i.addAndGet(1)),"111",20,20)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+new Date().getTime()+"end");
    }
}
