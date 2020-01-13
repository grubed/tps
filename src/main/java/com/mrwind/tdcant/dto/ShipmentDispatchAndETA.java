package com.mrwind.tdcant.dto;

import lombok.Data;

import java.util.Date;
@Data
public class ShipmentDispatchAndETA {
    private String expressno;
    private String courier;
    private Date plangettime;
    private Date plansendtime;
    private Date planbacktime;
}
