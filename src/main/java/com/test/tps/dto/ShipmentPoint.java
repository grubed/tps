package com.test.tps.dto;

import lombok.Data;

@Data
public class ShipmentPoint {
    private String expressno;
    private Integer chargeunit;
    private WindPoint start;
    private WindPoint end;
    private String courier;
    private String status;

    public String getDoublePoint(){
        return start.getLat().toString()+
                start.getLng().toString()+
                end.getLat().toString()+
                end.getLng().toString();
    }
}
