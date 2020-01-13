package com.test.tps.dto;

import lombok.Data;

import java.util.List;

@Data
public class AntResult {
    private int bestLength; // 最佳长度
    private int[] bestTour; // 最佳路径
    private String courierId;
    private List<ShipmentPoint> shipmentPointList;
    private List<ShipmentDispatchAndETA> shipmentDispatchAndETAS;
}
