package com.mrwind.tdcant.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class BatchShipmentDispatchAndETAOut {
    private Date hometime;
    private List<ShipmentDispatchAndETA> shipmentout;
    private List<ShipmentDispatchAndETA> oldshipmentout;
}
