package com.xl.tree.builddemo;


import com.xl.mphelper.util.SnowflakeIds;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 假设 3号件 1.1-2.1 2 号件 1.1-3.1
 *
 * @author tanjl11
 * @date 2022/01/12 11:44
 *       1
 *     2       3       4
 * 5 6 7    6 7 7   8 9 7
 * a b c  a b c
 */
public class DemoPathNodeTemplate extends PathNodeTemplate<DemoAdjacencyList, DemoPathNode> {
    static List<DemoAdjacencyList> dataBases = new ArrayList<>();

    static {
        String from1 = "2022-01-01";
        String from2 = "2022-01-30";
        String to3 = "2022-03-01";
        String end = "9999-12-31";
        DemoAdjacencyList.buildAndAdd("1", "2", "1", dataBases, from1, from2);
        DemoAdjacencyList.buildAndAdd("1", "3", "2", dataBases, from1, to3);
        DemoAdjacencyList.buildAndAdd("1", "4", "3", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("2", "5", "1", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("2", "6", "2", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("2", "7", "3", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("3", "6", "1", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("3", "7", "2", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("3", "7", "3", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("4", "8", "1", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("4", "9", "2", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("4", "7", "3", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("6", "a", "1", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("6", "b", "2", dataBases, from1, end);
        DemoAdjacencyList.buildAndAdd("6", "c", "3", dataBases, from1, end);
    }

    @Override
    protected List<DemoAdjacencyList> findChildrenByParent(Collection<DemoAdjacencyList> parent, Map<String, Object> auxMap) {
        //模拟查询数据库操作，其实这里in所有父类的code即可，即 where pid in (x,x,x)
        List<DemoAdjacencyList> res = new ArrayList<>();
        for (DemoAdjacencyList demoAdjacencyList : parent) {
            for (DemoAdjacencyList dataBase : dataBases) {
                if (dataBase.getPidCode().equals(demoAdjacencyList.getIdCode())) {
                    res.add(dataBase);
                }
            }
        }
        return res;
    }

    @Override
    protected void dealWithNewNodes(Collection<DemoPathNode> newNodes, Map<String, Object> auxMap) {
        System.out.println("new");
        System.out.println(newNodes);
    }

    @Override
    protected void dealWithEndNodes(Collection<DemoPathNode> updateNode, Map<String, Object> auxMap) {
        System.out.println("update");
        System.out.println(updateNode);
    }

    @Override
    protected DemoPathNode convertToPathNode(DemoAdjacencyList parent, DemoAdjacencyList cur, Map<String, Object> auxMap) {
        String fullPath = cur.getFullPath();
        Integer level = getBuildingLevel();
        //属性赋值
        DemoPathNode node = new DemoPathNode();
        node.setFullPath(fullPath);
        node.setLevel(level);
        node.setLineNo(cur.getLineNo());
        node.setParentMaterialCode(cur.getPidCode());
        node.setSelfMaterialCode(cur.getIdCode());
        long id = SnowflakeIds.generate();
        node.setSelfId(id);
        cur.setPathDataBaseId(id);
        node.setEffectedFrom(cur.getFrom());
        node.setEffectedTo(cur.getTo());
        node.setLeaf(false);
        return node;
    }

    @Override
    protected DemoPathNode handleEndNode(DemoPathNode endNode, Map<String, Object> auxMap) {
        //末尾节点，设置为true，作为递归构建树的终点
        endNode.setLeaf(true);
        return endNode;
    }

    public static void main(String[] args) throws ParseException {
        DemoAdjacencyList root = DemoAdjacencyList.build("1", "1", "0");
        DemoPathNodeTemplate demoPathNodeTemplate = new DemoPathNodeTemplate();
        Collection<DemoPathNode> demoPathNodes = demoPathNodeTemplate.doBuildBom(root);
        DemoPathNode pathRoot = new DemoPathNode();
        pathRoot.setSelfMaterialCode("1");
        pathRoot.setParentMaterialCode("1");
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = format.parse("2022-01-01");
        Date date2 = format.parse("2022-02-01");
        /**
         * 这里构建树如果把{@link PathNode.Together}里面的条件注释，会看到爷爷失效，爸爸生效，但是子类重复的情况，所以要加链路判断
         */
        DemoPathNode demoPathNode = demoPathNodeTemplate.baseQuery(pathRoot, demoPathNodes, date1);
        System.out.println("1月层序遍历");
        PathNode.foreachInBfs(System.out::println, demoPathNode);
        System.out.println("2月层序遍历");
        DemoPathNode demo2 = demoPathNodeTemplate.baseQuery(pathRoot, demoPathNodes, date2);
        PathNode.foreachInBfs(System.out::println, demo2);
    }
}
