package com.mrwind.tdcant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Warehouse {
    private BigDecimal lat;
    private BigDecimal lng;
    private double baseServiceArea;
    private double maxServiceArea;
}
