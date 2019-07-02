package table;

import entity.Student;
import index.Index;

import java.awt.image.ImagingOpException;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class StudentTable extends Table {

    public StudentTable() {
        super("student");
        System.out.println("init finish");
    }


    public String insertStudent(Student student) {

        String data = student.getId() + "~"+ student.getClassroom() + "~" + student.getName() + "~"+student.getSex();

        if(data.getBytes().length>64)
        {
            return "too long";
        }
        int distance = 64 - data.getBytes().length;
        StringBuilder stringBuilder = new StringBuilder(data);
        for( int i =0 ; i<distance ; i++)
        {
            stringBuilder.append("*");
        }

        return insert(stringBuilder.toString(),student.getId());
    }

    public Student getStudent(String key)
    {
        String data = getRecord(key);
        if(data == null)
        {
            return null;
        }
        String[] datas = data.split("~");
        Student student = new Student(datas[0],datas[1],datas[2],datas[3]);

        return student;
    }

    public List<Student> getStudentsByRange(int left, int right)
    {
        List<String> datas = getRecordByRange(left, right);
        if(datas == null)
        {
            return null;
        }
        if(datas.size()==0)
        {
            return null;
        }

        List<Student> students = new ArrayList<>();
        for(String data : datas)
        {
            if(data != null)
            {
                String[] srcDatas = data.split("~");
                Student student = new Student(srcDatas[0], srcDatas[1], srcDatas[2] ,srcDatas[3]);
                students.add(student);
            }
        }

        return students;
    }

    public String updateStudent(Student student)
    {
        indexLock.lock();
        Index index = indexTree.getTreeMap().get(student.getId());
        if(index == null)
        {
            indexLock.unlock();
            return "student not exesit";
        }
        indexLock.unlock();

        ReentrantLock lock = fileLocks.get(index.getFileNum());
        lock.lock();
        if(indexTree.getTreeMap().get(student.getId()) == null)
        {
            lock.unlock();
            return "student not exesit";
        }

        String data = student.getId()+ "~" + student.getClassroom() + "~" + student.getName() + "~" +student.getSex();
        int distance = 64 - data.getBytes().length;
        StringBuilder stringBuilder = new StringBuilder(data);
        for (int i = 0; i < distance; i++) {
            stringBuilder.append("*");
        }

        try (
                RandomAccessFile raf = new RandomAccessFile(new File(index.getFilePath()), "rw");
        ) {
            raf.skipBytes(65 * (index.getLineNum() - 1));
            raf.write(stringBuilder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            lock.unlock();
            return "file write exception";
        }

        lock.unlock();
        return "success";

    }

    public String deleteStudent (String id , ChoiceTable choiceTable)
    {

        indexLock.lock();
        Index index = indexTree.getTreeMap().get(id);
        if(index == null)
        {
            indexLock.unlock();
            return "student not exesit!";
        }
        indexLock.unlock();

        ReentrantLock lock = fileLocks.get(index.getFileNum());
        lock.lock();
        if(indexTree.getTreeMap().get(id) == null)
        {
            lock.unlock();
            return "student not exesit!!";
        }

        lock.lock();
        try{
            if(indexTree.getTreeMap().get(id) == null)
            {
                lock.unlock();
                return "student not exesit!!!";
            }
            choiceTable.indexLock.lock();
            for(String key : choiceTable.indexTree.getTreeMap().keySet())
            {
                if(key.split("-")[0].equals(id))
                {
                    choiceTable.indexLock.unlock();
                    return "The student has his choice";
                }
            }
            choiceTable.indexLock.unlock();

            String srcData = "";
            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(index.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * ( index.getLineNum() - 1));
                byte[] b = new byte[64];
                raf.read(b);
                srcData = new String(b);
            } catch (IOException e) {
                e.printStackTrace();
                return "file read exception";
            }

            srcData = srcData.replace("*","")+"+";
            int distance = 64 - srcData.getBytes().length;
            StringBuilder stringBuilder = new StringBuilder(srcData);
            for (int i = 0; i < distance; i++) {
                stringBuilder.append("*");
            }
            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(index.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * (index.getLineNum() - 1));
                raf.write(stringBuilder.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                return "file write exception";
            }

            indexLock.lock();
            indexTree.getTreeMap().remove(id);
            writeIndex();
            indexLock.unlock();

            return "success";

        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        //CourseTable table = new CourseTable();
        StudentTable studentTable = new StudentTable();
//        MyThread myThread = new MyThread(studentTable);
//        List<Thread> threads = new ArrayList<>();
//        for (int i = 0; i < 30; i++) {
//            threads.add(new Thread(myThread));
//        }
//
//        for(int i=0; i<30 ; i++)
//        {
//            threads.get(i).start();
//        }
        studentTable.insertStudent(new Student("123","子贤","12","男"));

    }


}
