package com.mrwind.tdcant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class City {
    private Integer id;
    private String shipmentId;
    private BigDecimal lat;
    private BigDecimal lng;
    private Date start;
    private Date end;
    private Date plan;
    private Integer unit;

    public Long getPlanLong() {
        return plan.getTime();
    }
}
