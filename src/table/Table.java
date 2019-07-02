package table;

import entity.Choice;
import entity.Course;
import entity.Student;
import index.Index;
import index.IndexTree;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Table {
    protected String tableName;
    protected File folder;//表所在的文件夹
    protected File indexFile;//索引文件
    protected IndexTree indexTree;
    protected CopyOnWriteArraySet<File> dataFileSet;
    protected ReentrantLock indexLock;
    protected Map<Integer, ReentrantLock> fileLocks;
    protected AtomicInteger lineNum;

    protected static int lineNumConfine = 10;

    protected Table(String tableName) {

        this.tableName = tableName;
        this.folder = new File("dir" + "/" + tableName);
        this.indexFile = new File(folder, this.tableName + ".index");
        this.indexTree = new IndexTree();
        this.fileLocks = new ConcurrentHashMap<>();
        this.indexLock = new ReentrantLock(true);
        this.dataFileSet = new CopyOnWriteArraySet<>();
        this.lineNum = new AtomicInteger(0);
        init();
        System.out.println("The record is " + lineNum + " lines");
    }

    protected synchronized void init() {

        File[] dataFiles = new File(folder, "data").listFiles();
        if (null != dataFiles && 0 != dataFiles.length) {
            for (int i = 1; i <= dataFiles.length; i++) {
                File dataFile = new File(folder + "/data", i + ".data");
                dataFileSet.add(dataFile);
                fileLocks.put(i,new ReentrantLock(true));
                lineNum.addAndGet(fileLineNum(dataFile));
            }
        } else {
            lineNum.set(0);
        }

        if (indexFile.exists()) {
            readIndex();
            System.out.println(indexTree.toString());
        } else {
            buildIndex();
            writeIndex();
        }


    }


    /**
     * 将索引对象从索引文件读取
     */
    private void readIndex() {
        if (!indexFile.exists()) {
            return;
        }
        try (
                FileInputStream fis = new FileInputStream(indexFile);
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            indexTree = (IndexTree) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 插入数据到最后一个数据文件，如果数据行数超过限定值，写入下一个文件中
     *
     * @param data
     * @return
     */
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
            indexTree.putIndex(key, file.getAbsolutePath(), fileLineNum(file), myFile);
            writeIndex();

            return "sucess";
        } finally {
            fileLocks.get(myFile).unlock();
            indexLock.unlock();
        }
    }

    private void buildIndex() {
        indexTree = new IndexTree();
        File[] dataFiles = new File(folder, "data").listFiles();
        if (null == dataFiles || dataFiles.length == 0) {
            return;
        }

        for (File dataFile : dataFiles) {
            List<Map<String, String>> datas = readDatasAndLineNum(dataFile);

            int fileNum = Integer.valueOf(dataFile.getAbsolutePath().split("dir/"+tableName+"/data/")[1].split(".data")[0]);
            for (Map<String, String> data : datas) {
                int lineNum = Integer.valueOf(data.get("[lineNum]"));
                String priKey = data.get("[prikey]");
                if(tableName == "choice")
                {
                    priKey = data.get("[prikey]") + "-" +data.get("[seckey]");
                }
                System.out.println("key:" + priKey);
                indexTree.putIndex(priKey, dataFile.getAbsolutePath(), lineNum,fileNum);
            }
            System.out.println(fileNum+"----"+ dataFile.getAbsolutePath());
        }
    }

    public String insertData(File file, Table data) {
        return null;
    }

    protected int fileLineNum(File file) {
        if(!file.exists())
        {
            return 0;
        }
        int num = 0;
        try (
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr)
        ) {
            while (null != br.readLine()) {
                num++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return num;
    }

    /**
     * 读取指定文件的所有数据加行号
     *
     * @param dataFile 数据文件
     * @return 数据列表
     */
    private List<Map<String, String>> readDatasAndLineNum(File dataFile) {
        List<Map<String, String>> dataMapList = new ArrayList<>();

        try (
                FileReader fr = new FileReader(dataFile);
                BufferedReader br = new BufferedReader(fr)
        ) {

            String line = null;
            long lineNum = 1;
            while (null != (line = br.readLine())) {
                Map<String, String> dataMap = new LinkedHashMap<>();
                if(!line.contains("+")) {
                    String[] datas = line.replace("*", "").split("~");
                    //System.out.println("read datas-----------------"+Arrays.toString(datas));
                    dataMap.put("[prikey]", datas[0]);
                    dataMap.put("[seckey]", datas[1]);
                    dataMap.put("[lineNum]", String.valueOf(lineNum));
                    dataMapList.add(dataMap);
                }
                lineNum++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataMapList;
    }

    /**
     * 将索引对象写入索引文件
     */
    protected void writeIndex() {
        try (
                FileOutputStream fos = new FileOutputStream(indexFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(indexTree);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getRecordNums()
    {
        indexLock.lock();
        try {
            return indexTree.getTreeMap().size();
        }finally {
            indexLock.unlock();
        }
    }

    public List<String> getRecordByRange(int left ,int right)
    {
        if(left>right)
        {
            System.out.println("left > right");
            return null;
        }
        if(left<1)
        {
            System.out.println( " left < 1");
            return null;
        }
        List<String> data = new ArrayList<>();
        indexLock.lock();
        try {
            int line = 1;
            for(String key : indexTree.getTreeMap().keySet())
            {
                if(line>=left && line<=right)
                {
                    data.add(getRecord(key));
                }
                line++;
                if(line > right)
                {
                    break;
                }
            }
            return data;
        }finally {
            indexLock.unlock();
        }
    }

    public String getRecord(String key)
    {
        indexLock.lock();
        Index index = new Index();
        index = indexTree.getTreeMap().get(key);
        if(index == null)
        {
            indexLock.unlock();
            return null;
        }
        indexLock.unlock();

        ReentrantLock lock = fileLocks.get(index.getFileNum());
        if(lock == null)
        {
            return null;
        }

        lock.lock();
        indexLock.lock();
        index = indexTree.getTreeMap().get(key);
        if(index == null)
        {

            indexLock.unlock();
            lock.unlock();
            return null;
        }
        indexLock.unlock();

        if(fileLocks.get(index.getFileNum())== null)
        {
            lock.unlock();
            return null;
        }

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
            lock.unlock();
            System.out.println("file read exception");
            return null;
        }
        lock.unlock();
        if(srcData.contains("+"))
        {
            return null;
        }
        return srcData.replace("*","");
    }

    public static void main(String[] args) {
        StudentTable studentTable = new StudentTable();
        CourseTable courseTable = new CourseTable();
        ChoiceTable choiceTable = new ChoiceTable(studentTable, courseTable);
        System.out.println( choiceTable.getChoice("1","1"));

        System.out.println(courseTable.deleteCourse("2",choiceTable));
    }
}
