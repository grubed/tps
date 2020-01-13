package com.test.tps.common;

import com.google.common.collect.Lists;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AlgorithmUtil {

    /**
     * kruskal算法创建最小生成树
     */
    public static List<Edge> createMinSpanTreeKruskal(int edgeSize, List<Edge> edges) {
        Comparator c = new Comparator<Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                // TODO Auto-generated method stub
                if(o1.getWeight() > o2.getWeight()) {
                    return 1;
                } else if(o1.getWeight() == o2.getWeight()) {
                    return 0;
                } else {
                    return -1;
                }

                    //注意！！返回值必须是一对相反数，否则无效。jdk1.7以后就是这样。
                    //		else return 0; //无效
                // else return -1;
            }
        };
        edges.sort(c);

        List<Edge> result = new ArrayList<>();

        // 定义一个一维数组，下标为连线的起点，值为连线的终点
        Integer[] parent = new Integer[edgeSize + 1];
        for (int i = 0; i < edgeSize + 1; i++) {
            parent[i] = 0;
        }

        int sum = 0;
        for (Edge edge : edges) {

            // 找到起点和终点在临时连线数组中的最后连接点
            Integer start = find(parent, edge.getStart());
            Integer end = find(parent, edge.getEnd());

            // 通过起点和终点找到的最后连接点是否为同一个点，是则产生回环
            if (start.equals(end) == false) {
                result.add(edge);
                // 没有产生回环则将临时数组中，起点为下标，终点为值
                parent[start] = end;
//                System.out.println("访问到了节点：{" + start + "," + end + "}，权值：" + edge.getWeight());
                sum += edge.getWeight();
            }
        }
        System.out.println("最小生成树的权值总和：" + sum);
        return result;
    }
    /**
     * 按条件拆分为若干堆
     */
    public static Integer howMuchHeap(double dis, Integer courierCount) {

        return (int)dis / 150;
    }

    /**
     * 按条件拆分为若干堆
     */
    public static List<Edge> departIf(List<Edge> edges, Predicate<Edge> predicate) {
        return edges.stream().filter(predicate).collect(Collectors.toList());
    }
    /**
     * 拆分为n堆
     */
    public static List<Edge> departIf(List<Edge> edges, @Min(value=2) Integer num) {
        List<Edge> result = new ArrayList<>();
        result.addAll(edges);
        if(num < 2) {
            return result;
        }
        //n堆就是删掉最长的n-1条线
        num = num - 1;
//        //自定义Comparator对象，自定义排序
//        Comparator c = new Comparator<Edge>() {
//            @Override
//            public int compare(Edge o1, Edge o2) {
//                // TODO Auto-generated method stub
//                if(o1.getWeight() < o2.getWeight())
//                    return 1;
//                    //注意！！返回值必须是一对相反数，否则无效。jdk1.7以后就是这样。
//                    //		else return 0; //无效
//                else return -1;
//            }
//        };
//
//        edges.sort(c);
        result.sort(Comparator.comparingInt(Edge::getWeight).reversed());
        if (num > edges.size() - 1){
            num = edges.size();
        }
        result.removeAll(result.subList(0, num));
        return result;
    }
    /**
     * 拆分为n堆
     */
    public static List<List<Edge>> edgeDepartIf(List<Edge> edges,  Integer num) {
        List<List<Edge>> edgelistlist = new ArrayList<>();
        List<Edge> edgeList = departIf(edges, num);
        while (edgeList.size() > 0) {
            List<Edge> subEdgeList = findRelated(edgeList);
            if(subEdgeList.size() == 0) {
                break;
            }
            edgeList.removeAll(subEdgeList);
            edgelistlist.add(subEdgeList);
        }
        if(edgeList.size() > 0) {
            for (Edge edge: edgeList) {
                edgelistlist.add(Lists.newArrayList(edge));
            }
        }
        return edgelistlist;
    }

    public static List<Edge> findRelated(List<Edge> edgeList) {
        List<Edge> edgeList1 = findRelatedBefor(edgeList);
        List<Edge> edgeList2 = findRelatedAfter(edgeList);
        edgeList1.addAll(edgeList2);
        return edgeList1;
    }
    public static List<Edge> findRelatedBefor(List<Edge> edgeList){
        List<Edge> result = new ArrayList<>();
        result.add(edgeList.get(0));
        for(int i = 0; i < result.size(); i++) {
            for(Edge edge : edgeList) {
                if(result.get(i).getStart() == edge.getStart() && result.get(i).getEnd() == edge.getEnd()) {
                    continue;
                } else {
                    if(result.get(i).getEnd() == edge.getStart()) {
                        result.add(edge);
                    }
                }
            }
        }
        return result;
    }
    public static List<Edge> findRelatedAfter(List<Edge> edgeList){
        List<Edge> result = new ArrayList<>();
        result.add(edgeList.get(0));
        for(int i = 0; i < result.size(); i++) {
            for(Edge edge : edgeList) {
                if(result.get(i).getStart() == edge.getStart() && result.get(i).getEnd() == edge.getEnd()) {
                    continue;
                } else {
                    if(result.get(i).getStart() == edge.getEnd()) {
                        result.add(edge);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取集合的最后节点
     */
    private static Integer find(Integer parent[], Integer index) {
        while (parent[index] > 0) {
            index = parent[index];
        }
        return index;
    }


    public static List<MultiTree<Integer>> toTree(List<Edge> edgeList, int num) {
        List<MultiTree<Integer>> multiTreeList = new ArrayList<>();

        List<Integer> isolation = new ArrayList<>();
        for(int i = 0; i< num; i++    ){
            Boolean found = false;
            for(Edge edge : edgeList){
                if(edge.getStart() == i || edge.getEnd() == i){
                    found = true;
                    break;
                }
            }
            if(found == false){
                isolation.add(i);
            }
        }
        Integer len = num - edgeList.size();
        for(int i = 0; i < len; i++){
            MultiTree<Integer> tree = createTree(edgeList, isolation);
            multiTreeList.add(tree);
        }


        return multiTreeList;
    }
    public static MultiTree<Integer> createTree(List<Edge> edgeList, List<Integer> isolation) {
        if(edgeList.size() == 0) {
            Integer n = isolation.get(0);
            isolation.remove(0);
            return new MultiTree<Integer>(n);
        }
        MultiTree<Integer> multiTree = new MultiTree<Integer>(edgeList.get(0).getStart());
        Integer before = edgeList.size();
        while (true) {
            for(int i = edgeList.size() -1 ; i >=0  ; i--){
                multiTree = addTree(edgeList, edgeList.get(i), multiTree);
            }
            if(before == edgeList.size()){
                break;
            } else {
                before = edgeList.size();
            }
        }

        return multiTree;
    }
    public static MultiTree<Integer> addTree(List<Edge> edgeList, Edge edge, MultiTree<Integer> multiTree) {
        Optional<MultiTree.Node<Integer>> node = multiTree.getNode(edge.getStart());
        if(node.isPresent()){
            node.ifPresent(e -> multiTree.add(e, edge.getEnd()));
            edgeList.remove(edge);
        } else {
            Optional<MultiTree.Node<Integer>> nodeEnd = multiTree.getNode(edge.getEnd());
            if(nodeEnd.isPresent()){
                nodeEnd.ifPresent(e -> multiTree.add(e, edge.getStart()));
                edgeList.remove(edge);
            }
        }

        return multiTree;
    }

    public static List<MultiTree<Integer>> addTree(int num) {
        List<MultiTree<Integer>> multiTreeList = new ArrayList<>();

        for(int i = 0; i < num; i++) {
            MultiTree<Integer> mts = new MultiTree<>(i);
            multiTreeList.add(mts);
        }
        return multiTreeList;
    }
    public static List<MultiTree<Integer>> addTree(List<MultiTree<Integer>> multiTreeList, int num) {
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < num; i++){
            list.add(i);
        }
        for(MultiTree<Integer> mst : multiTreeList) {
            List<Integer> tmp = new ArrayList<>();
            mst.preTraversal(tmp::add);
            list.removeAll(list);
        }
        if(list != null && list.size() > 0) {
            for(Integer newi : list) {
                multiTreeList.add(new MultiTree<>(newi));
            }
        }
        return multiTreeList;
    }

    public static List<MultiTree<Integer>> addTree(Edge edge, List<MultiTree<Integer>> multiTreeList) {
        boolean found = false;
        for(MultiTree<Integer> multiTree : multiTreeList){
            Optional<MultiTree.Node<Integer>> node = multiTree.getNode(edge.getStart());
            if(node.isPresent()){
                node.ifPresent(e -> multiTree.add(e, edge.getEnd()));
                found = true;
            } else {

            }
            if(found == true) {
                break;
            }
        }
        if(found == false) {
            MultiTree<Integer> tree = new MultiTree<>(edge.getStart());
            tree.add(new MultiTree.Node(edge.getStart()), edge.getEnd());
            multiTreeList.add(tree);
        }
        return multiTreeList;
    }
}
