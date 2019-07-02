package table;

import entity.Course;
import index.Index;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class CourseTable extends Table {
    protected ConcurrentHashMap<String, ReentrantLock> keyLock;

    public CourseTable() {
        super("course");
        initLock();
        System.out.println("init finish");

    }

    private void initLock() {
        this.keyLock = new ConcurrentHashMap<>();
        for (String key : indexTree.getTreeMap().keySet()) {
            System.out.println("key:" + key);
            keyLock.put(key, new ReentrantLock(true));
        }
    }

    public String insertCourse(Course course)  {
        String data = course.getId() + "~" + course.getName() + "~" + course.getTotalSize() + "~" + course.getRemainSize();
        if (data.length() > 64) {
            return "too long";
        }
        int distance = 64 - data.getBytes().length;
        StringBuilder stringBuilder = new StringBuilder(data);
        for (int i = 0; i < distance; i++) {
            stringBuilder.append("*");
        }
        return insert(stringBuilder.toString(), course.getId());
    }


    protected String insert(String data, String key) {
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
        indexLock.lock();

        try {
            if (indexTree.getTreeMap().containsKey(key)) {
                return "key exesits";
            }

            File file = new File(folder + "/data", myFile + ".data");
            file.getParentFile().mkdirs();
            try (
                    FileWriter fw = new FileWriter(file, true);
                    PrintWriter pw = new PrintWriter(fw)
            ) {

                pw.println(data);
            } catch (IOException e) {
                e.printStackTrace();
                return "写入异常";
            }
            keyLock.put(key, new ReentrantLock(true));
            System.out.println("inputKey:" + key);
            indexTree.putIndex(key, file.getAbsolutePath(), fileLineNum(file), myNum);
            writeIndex();
            return "sucess";
        } finally {
            fileLocks.get(myFile).unlock();
            indexLock.unlock();
        }
    }

    public String addRemainSize(String id) {
        keyLock.get(id).lock();
        try {
            Index index = indexTree.getTreeMap().get(id);
            String srcData = "";
            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(index.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * (index.getLineNum() - 1));
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
            if (remainSize >= totalSize) {
                return "error:remainSize >= totalSize";
            }
            remainSize++;

            String data = id + "~" + name + "~" + totalSize + "~" + remainSize;
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
                return "file write exception";
            }
            return "success";
        } finally {
            keyLock.get(id).unlock();
        }
    }

    public String removeRemainSize(String id) {
        keyLock.get(id).lock();
        try {
            Index index = indexTree.getTreeMap().get(id);
            String srcData = "";
            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(index.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * (index.getLineNum() - 1));
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
                return "error:remainSize ==0";
            }
            remainSize--;

            String data = id + "~" + name + "~" + totalSize + "~" + remainSize;
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
                return "file write exception";
            }
            return "success";
        } finally {
            keyLock.get(id).unlock();
        }

    }

    public String getRecord(String key) {
        ReentrantLock lock = keyLock.get(key);
        if (lock == null) {
            return null;
        }
        lock.lock();
        if (keyLock.get(key) == null) {
            lock.unlock();
            return null;
        }
        Index index = indexTree.getTreeMap().get(key);
        String srcData = null;
        try (
                RandomAccessFile raf = new RandomAccessFile(new File(index.getFilePath()), "rw");
        ) {
            raf.skipBytes(65 * (index.getLineNum() - 1));
            byte[] b = new byte[64];
            raf.read(b);
            srcData = new String(b);
        } catch (IOException e) {
            e.printStackTrace();
            lock.unlock();
            System.out.println("file read exception");
            return null;
        }
        lock.unlock();
        if (srcData.contains("+")) {
            return null;
        }
        return srcData.replace("*", "");
    }

    public Course getCourse(String key) {
        String data = getRecord(key);
        if (data == null) {
            return null;
        }
        String[] datas = data.split("~");
        Course course = new Course(datas[0], datas[1], Integer.valueOf(datas[2]), Integer.valueOf(datas[3]));

        return course;
    }

    public List<Course> getCoursesByRange(int left, int right) {
        List<String> datas = getRecordByRange(left, right);
        if (datas == null) {
            return null;
        }
        if (datas.size() == 0) {
            return null;
        }

        List<Course> courses = new ArrayList<>();
        for (String data : datas) {
            if (data != null) {
                String[] srcDatas = data.split("~");
                Course course = new Course(srcDatas[0], srcDatas[1], Integer.valueOf(srcDatas[2]), Integer.valueOf(srcDatas[3]));
                courses.add(course);
            }
        }
        return courses;
    }

    public String updateCourse(Course course) {
        ReentrantLock lock = keyLock.get(course.getId());
        if (lock == null) {
            return "course not exesit";
        }
        lock.lock();
        try {
            if (keyLock.get(course.getId()) == null) {
                return "course not exesit";
            }
            indexLock.lock();
            Index index = indexTree.getTreeMap().get(course.getId());
            indexLock.unlock();

            String srcData = "";
            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(index.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * (index.getLineNum() - 1));
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

            if (course.getTotalSize() < (totalSize - remainSize)) {
                return "totalSize is too small";
            }

            course.setRemainSize(remainSize);

            String courseData = course.getId() + "~" + course.getName() + "~" + course.getTotalSize() + "~" + course.getRemainSize();
            System.out.println("courseData:" + courseData);
            int distance = 64 - courseData.getBytes().length;
            StringBuilder stringBuilder = new StringBuilder(courseData);
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

            return "success";

        } finally {
            lock.unlock();
        }
    }

    public String deleteCourse(String id, ChoiceTable choiceTable) {
        ReentrantLock lock = keyLock.get(id);
        if (lock == null) {
            return "course not exesit";
        }

        lock.lock();
        try {
            if (keyLock.get(id) == null) {
                return "course not exesit";
            }
            indexLock.lock();
            Index index = indexTree.getTreeMap().get(id);
            indexLock.unlock();

            choiceTable.indexLock.lock();
            for (String key : choiceTable.indexTree.getTreeMap().keySet()) {
                if (key.split("-")[1].equals(id)) {
                    choiceTable.indexLock.unlock();
                    return "The course has his choice";
                }
            }
            choiceTable.indexLock.unlock();

            String srcData = "";
            try (
                    RandomAccessFile raf = new RandomAccessFile(new File(index.getFilePath()), "rw");
            ) {
                raf.skipBytes(65 * (index.getLineNum() - 1));
                byte[] b = new byte[64];
                raf.read(b);
                srcData = new String(b);
            } catch (IOException e) {
                e.printStackTrace();
                return "file read exception";
            }

            srcData = srcData.replace("*", "") + "+";
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

        } finally {
            lock.unlock();
        }
    }


    public static void main(String[] args)  {
//        RandomAccessFile raf = new RandomAccessFile(new File("dir"+"/"+"course"+"/data/3.data"),"rw");
//        raf.skipBytes(0);
//        raf.write("子贤".getBytes());
//        System.out.println(raf.readLine());
//        raf.close();
//        String str = "1-2-3-4";
//        String res = String.format("%10s", str);
//        res = res.replaceAll("\\s", "*");
//        System.out.println(res);
        CourseTable courseTable = new CourseTable();
       // System.out.println(courseTable.addRemainSize("Thread-191561910818941"));
        MyThread3 myThread = new MyThread3(courseTable);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            threads.add(new Thread(myThread));
        }

        for(int i=0; i<30 ; i++)
        {
            threads.get(i).start();
        }
    }

}
