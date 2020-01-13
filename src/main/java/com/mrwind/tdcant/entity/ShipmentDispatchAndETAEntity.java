package com.mrwind.tdcant.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "ShipmentDispatchAndETAEntity")
public class ShipmentDispatchAndETAEntity {
    private String expressno;
    private String courier;
    private Date plangettime;
    private Date plansendtime;
    private Date planbacktime;
    private Integer type;
    @CreatedDate
    private Date create_time;
}
