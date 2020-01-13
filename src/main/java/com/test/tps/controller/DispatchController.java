package com.test.tps.controller;


import com.alibaba.fastjson.JSON;

import com.test.tps.common.Result;

import com.test.tps.service.DispatchService;


import com.test.tps.dto.CourierPoint;
import com.test.tps.dto.ShipmentPoint;
import com.test.tps.dto.Uds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import java.util.List;

import java.util.stream.Collectors;

@RestController
public class DispatchController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DispatchService dispatchService;


    public List<CourierPoint> cloneCourier(List<CourierPoint> courierPointList) {
        List<CourierPoint> ret = new ArrayList<>();
        for(CourierPoint courierPoint : courierPointList) {
            CourierPoint det = new CourierPoint();
            BeanUtils.copyProperties(courierPoint, det);
            ret.add(det);
        }
        return ret;
    }



    @PostMapping("/almighty")
    public Result<String> almightyDispatch(@RequestBody Uds uds){
        logger.info(JSON.toJSONString(uds));


        List<ShipmentPoint> unassignedShipment = uds.getShipment().stream().filter(sp -> sp.getStatus().equals("unassigned")).collect(Collectors.toList());
        List<ShipmentPoint> assignedShipmentList =
                uds.getShipment().stream().filter(sp -> sp.getStatus().equals("assigned") || sp.getStatus().equals("got")).collect(Collectors.toList());

        logger.info("unassignedShipment size="+unassignedShipment.size()+" assignedShipmentList size="+assignedShipmentList.size());
        dispatchService.asyncPreferencesService(unassignedShipment, cloneCourier(uds.getCourier()), assignedShipmentList);

        return Result.getSuccess();
    }

}
