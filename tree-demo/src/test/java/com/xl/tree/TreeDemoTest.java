package com.xl.tree;

import com.xl.tree.entity.AdjacencyList;
import com.xl.tree.entity.ClosureTable;
import com.xl.tree.entity.NestedSet;
import com.xl.tree.service.impl.AdjacencyListServiceImpl;
import com.xl.tree.service.impl.ClosureTableServiceImpl;
import com.xl.tree.service.impl.NestedSetServiceImpl;
import com.xl.tree.service.impl.PathNodeServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.annotation.PrepareTestInstance;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TreeDemoTest {
    @Resource
    private PathNodeServiceImpl pathNodeService;
    @Resource
    private AdjacencyListServiceImpl adjacencyListService;
    @Resource
    private ClosureTableServiceImpl closureTableService;
    @Resource
    private NestedSetServiceImpl nestedSetService;

    static List<AdjacencyList> sampleData;

    static {
        sampleData = AdjacencyList.sampleData();
    }

    @Test
    public void testPath() {
        pathNodeService.insertNodes(sampleData);
    }

    @Test
    public void testAdjacencyList() {
        adjacencyListService.insertNodes(sampleData);
    }
    /**
     * *              0               10
     * *           1    2           1    6
     * *         3  4  5  6       3  4  7 8
     * *        6        7 8     6
     * *      7  8             7   8
     * *
     *
     * @return
     */
    @Test
    public void testClosureTable() {
        closureTableService.insertNodes(sampleData);
        List<ClosureTable> nestedSets = closureTableService.queryChildren(1L);
        System.out.println("查询1的直接子节点");
        nestedSets.forEach(System.out::println);
        List<ClosureTable> nestedSets1 = closureTableService.queryAllChildren(1L);
        System.out.println("查询1的所有子节点");
        nestedSets1.forEach(System.out::println);
        List<ClosureTable> parent1 = closureTableService.queryParents(6L);
        System.out.println("查询6的直接父节点");
        parent1.forEach(System.out::println);
        List<ClosureTable> parent2 = closureTableService.queryAllParents(6L);
        System.out.println("查询6的所有父节点");
        parent2.forEach(System.out::println);
//        closureTableService.removeNodes(AdjacencyList.build(0L, 0L, null));
//        closureTableService.removeNodes(AdjacencyList.build(10L, 10L, null));
    }

    /**
     * *              0               10
     * *           1    2           1    6
     * *         3  4  5  6       3  4  7 8
     * *        6        7 8     6
     * *      7  8             7   8
     * *
     *
     * @return
     */
    @Test
    public void testNestedSet() {
        nestedSetService.insertNodes(sampleData);
        List<NestedSet> nestedSets = nestedSetService.queryChildren(1L);
        System.out.println("查询1的直接子节点");
        nestedSets.forEach(System.out::println);
        List<NestedSet> nestedSets1 = nestedSetService.queryAllChildren(1L);
        System.out.println("查询1的所有子节点");
        nestedSets1.forEach(System.out::println);
        List<NestedSet> parent1 = nestedSetService.queryParents(6L);
        System.out.println("查询6的直接父节点");
        parent1.forEach(System.out::println);
        List<NestedSet> parent2 = nestedSetService.queryAllParents(6L);
        System.out.println("查询6的所有父节点");
        parent2.forEach(System.out::println);
        nestedSetService.removeNodes(AdjacencyList.build(0L, 0L, null));
        nestedSetService.removeNodes(AdjacencyList.build(10L, 10L, null));
    }
}
