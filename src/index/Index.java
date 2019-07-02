package index;

import java.io.Serializable;

public class Index implements Serializable{
    private String filePath;
    private int lineNum;
    private int fileNum;

    public Index()
    {

    }

    public Index(String filePath, int lineNum, int fileNum) {
        this.filePath = filePath;
        this.lineNum = lineNum;
        this.fileNum = fileNum;
    }

    public int getFileNum() {
        return fileNum;
    }

    public String getFilePath() {
        return filePath;
    }


    public int getLineNum() {
        return lineNum;
    }

}
