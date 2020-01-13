package com.test.tps.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ShipmentDispatchAndETAOut {
    private String expressno;
    private String courier;
    private Date plangettime;
    private Date plansendtime;
    private Date planbacktime;
    private Date hometime;
    private List<ShipmentDispatchAndETA> oldshipmentout;
}
