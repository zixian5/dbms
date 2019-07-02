package table;

import entity.Choice;

import java.util.concurrent.atomic.AtomicInteger;

public class MyThread2 implements Runnable {
    private ChoiceTable table;
    private AtomicInteger i;

    public MyThread2(ChoiceTable student)

    {
        this.table = student;
        i = new AtomicInteger(0);
    }

    @Override
    public void run() {
//        System.out.println(Thread.currentThread().getName()+new Date().getTime()+"start");
//        try {
//            System.out.println(table.insertCourse(new Course(Thread.currentThread().getName()+new Date().getTime(),"子贤",20,10)));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(Thread.currentThread().getName()+new Date().getTime()+"end");
        try {
          //  System.out.println(table.insertChoice(new Choice(String.valueOf(i.addAndGet(1)),"1","3")));
            System.out.println(table.deleteChoice(new Choice(String.valueOf(i.addAndGet(1)),"1")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
