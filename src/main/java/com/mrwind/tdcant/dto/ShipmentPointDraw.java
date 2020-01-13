package com.mrwind.tdcant.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ShipmentPointDraw {
    private String shipmentId;
    //1 司机 2是订单
    private Integer pointType;
    private String courier;
    private boolean start;
    private WindPoint point;
}
