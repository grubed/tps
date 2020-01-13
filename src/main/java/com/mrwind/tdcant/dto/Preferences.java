package com.mrwind.tdcant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Preferences {
    private String type;
    private List<BigDecimal> coordinates;
    private BigDecimal distance;
}
