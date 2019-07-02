package index;

import java.io.Serializable;
import java.util.TreeMap;

public class IndexTree implements Serializable {
    private TreeMap<String, Index> treeMap;

    public IndexTree() {
        treeMap = new TreeMap<>();
    }

    public TreeMap<String, Index> getTreeMap() {
        return treeMap;
    }

    public void setTreeMap(TreeMap<String, Index> treeMap) {
        this.treeMap = treeMap;
    }

    public void putIndex(String indexKey, String filePath, int lineNum , int fileNum) {

        Index index = new Index(filePath, lineNum , fileNum);
        treeMap.put(indexKey,index);
    }
}
