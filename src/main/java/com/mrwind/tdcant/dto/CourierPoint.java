package com.mrwind.tdcant.dto;


import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.data.mongodb.core.geo.GeoJson;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

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
