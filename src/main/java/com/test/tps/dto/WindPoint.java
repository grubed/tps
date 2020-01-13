package com.test.tps.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class WindPoint {
    private BigDecimal lat;
    private BigDecimal lng;
    private Date time;
}
