package com.mrwind.tdcant.dto;

import lombok.Data;

import java.util.List;
@Data
public class Uds {
    private List<ShipmentPoint> shipment;
    private List<CourierPoint> courier;
}
