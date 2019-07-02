package table;

import entity.Choice;

import java.util.concurrent.atomic.AtomicInteger;

public class MyThread4 implements Runnable {
    private ChoiceTable table;
    private AtomicInteger i;

    public MyThread4(ChoiceTable student) {
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
            String k = String.valueOf(i.addAndGet(1));
            for (int j = 0; j < 10; j++) {
                System.out.println(table.insertChoice(new Choice(String.valueOf(k), "1", "3")));
            } //System.out.println(table.deleteChoice(new Choice("1","1")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}