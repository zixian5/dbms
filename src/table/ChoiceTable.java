package table;

import entity.Choice;
import entity.Student;
import index.Index;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ChoiceTable extends Table {
    private StudentTable studentTable;
    private CourseTable courseTable;

    public ChoiceTable(StudentTable studentTable, CourseTable courseTable) {
        super("choice");
        this.studentTable = studentTable;
        this.courseTable = courseTable;
    }

    public String insertChoice(Choice choice) {

        String data = choice.getStudent_id() + "~" + choice.getCourse_id() + "~" + choice.getTime();

        if (data.getBytes().length > 64) {
            return "too long";
        }

        indexLock.lock();
        if(indexTree.getTreeMap().containsKey(choice.getStudent_id() + "-" + choice.getCourse_id()))
        {
            indexLock.unlock();
            return "choice has exestied";
        }
        indexLock.unlock();

        studentTable.indexLock.lock();
        Index studentIndex = new Index();
        studentIndex = studentTable.indexTree.getTreeMap().get(choice.getStudent_id());
        if (studentIndex == null) {
            studentTable.indexLock.unlock();
            return "Student not exesit ";
        }
        studentTable.indexLock.unlock();

        ReentrantLock courseLock = courseTable.keyLock.get(choice.getCourse_id());
        if (courseLock == null) {
            return "course not exesit";
        }

        ReentrantLock fileLock = studentTable.fileLocks.get(studentIndex.getFileNum());
        fileLock.lock();
        courseLock.lock();
        try {
            studentIndex = studentTable.indexTree.getTreeMap().get(choice.getStudent_id());
            if (studentIndex == null) {
                return "Student not exesit ";
            }
            if (courseTable.keyLock.get(choice.getCourse_id()) == null) {
                return "course not exesit";
            }

            indexLock.lock();
            if (indexTree.getTreeMap().containsKey(choice.getStudent_id() + "-" + choice.getCourse_id())) {
                indexLock.unlock();
                return "choice has exesited";
            }
            indexLock.unlock();

            courseTable.indexLock.lock();
            Index courseIndex = courseTable.indexTree.getTreeMap().get(choice.getCourse_id());
            courseTable.indexLock.unlock();
            String srcData = "";
            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(courseIndex.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * (courseIndex.getLineNum() - 1));
                byte[] b = new byte[64];
                raf.read(b);
                srcData = new String(b);
            } catch (IOException e) {
                e.printStackTrace();
                return "file read exception";
            }
            String[] datas = srcData.replace("*", "").split("~");
            String name = datas[1];
            int totalSize = Integer.valueOf(datas[2]);
            int remainSize = Integer.valueOf(datas[3]);
            if (remainSize == 0) {
                return "remain size is 0";
            }

            remainSize--;
            String courseData = choice.getCourse_id() + "~" + name + "~" + totalSize + "~" + remainSize;
            System.out.println("courseData:" +courseData);
            int distance = 64 - courseData.getBytes().length;
            StringBuilder stringBuilder = new StringBuilder(courseData);
            for (int i = 0; i < distance; i++) {
                stringBuilder.append("*");
            }

            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(courseIndex.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * (courseIndex.getLineNum() - 1));
                raf.write(stringBuilder.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                return "file write exception";
            }

            int myNum = lineNum.addAndGet(1);
            int myFile = myNum / lineNumConfine + 1;

            if (!fileLocks.containsKey(myFile)) {
                synchronized (fileLocks.getClass()) {
                    if (!fileLocks.containsKey(myFile)) {
                        fileLocks.put(myFile, new ReentrantLock(true));
                    }
                }
            }

            fileLocks.get(myFile).lock();

            distance = 64 - data.getBytes().length;
            stringBuilder = new StringBuilder(data);
            for (int i = 0; i < distance; i++) {
                stringBuilder.append("*");
            }

            File file = new File(folder + "/data", myFile + ".data");
            file.getParentFile().mkdirs();
            try (
                    FileWriter fw = new FileWriter(file, true);
                    PrintWriter pw = new PrintWriter(fw)
            ) {

                pw.println(stringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
                fileLocks.get(myFile).unlock();
                return "写入异常";
            }
            indexLock.lock();
            indexTree.putIndex(choice.getStudent_id() + "-" + choice.getCourse_id(), file.getAbsolutePath(), fileLineNum(file), myFile);
            writeIndex();
            indexLock.unlock();
            fileLocks.get(myFile).unlock();

            return "选课成功";

        } finally {
            courseLock.unlock();
            fileLock.unlock();
        }

    }

    public String deleteChoice(Choice choice)
    {
        indexLock.lock();
        Index index = new Index();
        index = indexTree.getTreeMap().get(choice.getStudent_id() + "-" + choice.getCourse_id());
        if(index == null)
        {
            indexLock.unlock();
            return "Choice not exesit";
        }
        indexLock.unlock();

        ReentrantLock courseLock = courseTable.keyLock.get(choice.getCourse_id());
        ReentrantLock fileLock = fileLocks.get(index.getFileNum());
        if(courseLock == null)
        {
            return "course not exesit";
        }

        courseLock.lock();
        fileLock.lock();
        try {
            indexLock.lock();
            if (!indexTree.getTreeMap().containsKey(choice.getStudent_id() + "-" + choice.getCourse_id())) {
                indexLock.unlock();
                return "choice not exesites";
            }
            indexLock.unlock();

            if(!courseTable.keyLock.containsKey(choice.getCourse_id()))
            {
                return "course not exesit!";
            }

            courseTable.indexLock.lock();
            Index courseIndex = courseTable.indexTree.getTreeMap().get(choice.getCourse_id());
            courseTable.indexLock.unlock();
            String srcData = "";
            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(courseIndex.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * (courseIndex.getLineNum() - 1));
                byte[] b = new byte[64];
                raf.read(b);
                srcData = new String(b);
            } catch (IOException e) {
                e.printStackTrace();
                return "file read exception";
            }
            String[] datas = srcData.replace("*", "").split("~");
            String name = datas[1];
            int totalSize = Integer.valueOf(datas[2]);
            int remainSize = Integer.valueOf(datas[3]);
            remainSize++;

            String courseData = choice.getCourse_id() + "~" + name + "~" + totalSize + "~" + remainSize;
            System.out.println("courseData:" +courseData);
            int distance = 64 - courseData.getBytes().length;
            StringBuilder stringBuilder = new StringBuilder(courseData);
            for (int i = 0; i < distance; i++) {
                stringBuilder.append("*");
            }

            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(courseIndex.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * (courseIndex.getLineNum() - 1));
                raf.write(stringBuilder.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                return "file write exception";
            }

            indexLock.lock();
            Index fileIndex = indexTree.getTreeMap().get(choice.getStudent_id() + "-" + choice.getCourse_id());
            indexLock.unlock();

            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(fileIndex.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * (fileIndex.getLineNum() - 1));
                byte[] b = new byte[64];
                raf.read(b);
                srcData = new String(b);
            } catch (IOException e) {
                e.printStackTrace();
                return "file read exception";
            }

            srcData = srcData.replace("*","")+"+";
            distance = 64 - srcData.getBytes().length;
            stringBuilder = new StringBuilder(srcData);
            for (int i = 0; i < distance; i++) {
                stringBuilder.append("*");
            }
            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(fileIndex.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * (fileIndex.getLineNum() - 1));
                raf.write(stringBuilder.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                return "file write exception";
            }

            indexLock.lock();
            indexTree.getTreeMap().remove(choice.getStudent_id() + "-" + choice.getCourse_id());
            writeIndex();
            indexLock.unlock();

            return "退课成功";

        }finally {
            fileLock.unlock();
            courseLock.unlock();
        }
    }

    public Choice getChoice(String studentId, String courseId)
    {
        String data = getRecord(studentId+"-"+courseId);
        if(data == null)
        {
            return null;
        }
        String[] datas = data.split("~");
        Choice choice = new Choice(datas[0], datas[1] , datas[2]);

        return choice;
    }

    public List<Choice> getChoicesByRange(int left ,int right)
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

        List<Choice> choices = new ArrayList<>();
        for(String data : datas)
        {
            if(data !=null)
            {
                String[] srcDatas = data.split("~");
                Choice choice = new Choice(srcDatas[0],srcDatas[1],srcDatas[2]);
            }
        }

        return choices;
    }

    public static void main(String[] args) {
        StudentTable studentTable = new StudentTable();
        CourseTable courseTable = new CourseTable();
//        ChoiceTable table = new ChoiceTable(studentTable,courseTable);
//        System.out.println(table.insertChoice(new Choice("Thread-01561971925369","Thread-11561971729722","3")));
        ChoiceTable choiceTable = new ChoiceTable(studentTable,courseTable);

        Choice choice = new Choice("2","2");
//        System.out.println(choiceTable.deleteChoice(choice));

        MyThread2 myThread = new MyThread2(choiceTable);
        MyThread4 myThread4 = new MyThread4(choiceTable);
        List<Thread> threads = new ArrayList<>();
        for ( int i=0; i<30;i++)
        {
            threads.add(new Thread(myThread));
        }

        for(int i=0; i< 30;i++)
        {
            threads.get(i).start();
        }

        List<Thread> thread4s = new ArrayList<>();
        for ( int i=0; i<30;i++)
        {
            thread4s.add(new Thread(myThread4));
        }

        for(int i=0; i< 30;i++)
        {
            thread4s.get(i).start();
        }

    }
}
