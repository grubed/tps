package com.test.tps.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourierPoint {
    private String id;
    private Integer unitlimit;
    private WindPoint start;
    private WindPoint end;
    private BigDecimal speed;
    private BigDecimal getOrdertime;
    private BigDecimal sendOrdertime;
    private BigDecimal samePoint;
}
