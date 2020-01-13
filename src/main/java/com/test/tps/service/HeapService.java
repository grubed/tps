package com.test.tps.service;

import com.test.tps.common.AlgReUtil;
import com.test.tps.common.AlgorithmUtil;
import com.test.tps.common.Edge;
import com.test.tps.common.MultiTree;
import com.test.tps.dto.ShipmentPoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service
public class HeapService {

    public List<Edge> ShipmentPointListToEdges(List<ShipmentPoint> shipmentInfoList) {
        List<Edge> edgeList = new ArrayList<>();
        for (int i = 0; i < shipmentInfoList.size(); i++) {
            for (int j = i+1; j < shipmentInfoList.size(); j++) {
                double distance = getDistance(shipmentInfoList.get(i), shipmentInfoList.get(j));
                Edge edge = new Edge(i,j,(int)distance);
                edgeList.add(edge);
            }
        }
        return edgeList;
    }
    private double getDistance(ShipmentPoint start, ShipmentPoint end) {
        return AlgReUtil.getDistance(start.getEnd().getLat().doubleValue(), start.getEnd().getLng().doubleValue(), end.getEnd().getLat().doubleValue(),
                end.getEnd().getLng().doubleValue());
    }
    public List<Edge> getEdgeTreeList(List<ShipmentPoint> shipment
                                      ){
        List<Edge> allEdgeList = ShipmentPointListToEdges(shipment);
        return AlgorithmUtil.createMinSpanTreeKruskal(shipment.size(),
                allEdgeList);
    }
    public List<List<ShipmentPoint>> heapDepartTree(List<ShipmentPoint> shipment, List<Edge> edgeTreeList, Integer num){
        List<Edge> multiEdgeTreeList = AlgorithmUtil.departIf(edgeTreeList, num);
        List<MultiTree<Integer>> multiTreeList = AlgorithmUtil.toTree(multiEdgeTreeList, shipment.size());
        return departTree(multiTreeList, shipment);
    }
    public List<List<ShipmentPoint>> heapDepartTreeByDistance(List<ShipmentPoint> shipment, List<Edge> edgeTreeList, Integer dis){
        Predicate<Edge> p1 = i -> i.getWeight() < dis;
        List<Edge> multiEdgeTreeList = AlgorithmUtil.departIf(edgeTreeList, p1);
        List<MultiTree<Integer>> multiTreeList = AlgorithmUtil.toTree(multiEdgeTreeList, shipment.size());
        return departTree(multiTreeList, shipment);
    }
    public List<List<ShipmentPoint>> heapByDistance(  List<ShipmentPoint> shipment,
                                            Integer dis){
        List<Edge> edgeTreeList = getEdgeTreeList(shipment);
        return heapDepartTreeByDistance(shipment, edgeTreeList, dis);
    }
    public List<List<ShipmentPoint>> heap(  List<ShipmentPoint> shipment,
                                            Integer num){
        if(num.equals(1)) {
            List<List<ShipmentPoint>> ret = new ArrayList<>();
            ret.add(shipment);
            return ret;
        }
        List<Edge> edgeTreeList = getEdgeTreeList(shipment);
        return heapDepartTree(shipment, edgeTreeList, num);
    }

    public List<List<ShipmentPoint>> departTree(List<MultiTree<Integer>> multiTreeList, List<ShipmentPoint> shipmentInfoList){
        List<List<ShipmentPoint>> ShipmentInfoListList = new ArrayList<>();
        for(MultiTree<Integer> tree : multiTreeList) {
            List<ShipmentPoint> ShipmentInfoList2 = new ArrayList<>();
            List<Integer> list = new ArrayList<>();
            tree.preTraversal(list::add);
            for(Integer i : list) {
                ShipmentInfoList2.add(shipmentInfoList.get(i));
            }
            ShipmentInfoListList.add(ShipmentInfoList2);
        }
        return ShipmentInfoListList;
    }

}
