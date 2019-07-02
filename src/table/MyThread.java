package table;

import entity.Course;
import entity.Student;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MyThread implements Runnable {
    private StudentTable table;
    private AtomicInteger i;
    //private CourseTable table;

    public MyThread(StudentTable student)

    {
        this.table = student;
        i= new AtomicInteger(0);
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+new Date().getTime()+"start");
        try {
            System.out.println(table.insertStudent(new Student(String.valueOf(i.addAndGet(1)),"111","222","1")));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+new Date().getTime()+"end");
    //    System.out.println(table.removeRemainSize("Thread-291561911264968"));
    }
}
