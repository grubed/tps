package com.test.tps.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;


import com.test.tps.common.Utils;
import com.test.tps.common.exception.InvalidTimeException;


import com.test.tps.dto.*;
import com.test.tps.entity.ShipmentDispatchAndETAEntity;
import com.test.tps.repository.ShipmentDispatchAndETAEntityRepository;
import com.test.tps.windaco.ACO;

import com.test.tps.common.AlgReUtil;
import com.test.tps.common.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;


@Service
public class DispatchService {
//    public Map<String, CityGraph2> getCityList(Uds uds) {
//        Map<String, CityGraph2> groupCity = new HashMap<>();
//
//        List<CourierPoint> courierPointList = uds.getCourier();
//        for(CourierPoint courierPoint : courierPointList){
//            CityGraph2 cityGraph2 = createCityGraph2(courierPoint, uds.getShipment());
//            groupCity.put(courierPoint.getId(), cityGraph2);
//        }
//        return groupCity;
//    }
    private Integer model = 0;
    private Integer count = 30;
    private Integer day = 3;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private HeapService heapService;

    @Autowired
    private ShipmentDispatchAndETAEntityRepository shipmentDispatchAndETAEntityRepository;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public void output(List<ShipmentDispatchAndETAEntity> data) {

    }
    public void setModel(Integer model) {
        this.model = model;
    }

    public List<CourierPoint> findCourier(Map<String, List<ShipmentPoint>> group, List<CourierPoint> courierPointList) {

        List<String> courierIdList = new ArrayList<>();
        for (Map.Entry<String, List<ShipmentPoint>> entry : group.entrySet()) {
            String mapKey = entry.getKey();
            courierIdList.add(mapKey);
        }
        for(int i = courierPointList.size() ; i >= 0 ; i--) {
            for(String  courierId : courierIdList) {
                if(courierPointList.get(i).getId().equals(courierId)) {
                    courierPointList.remove(i);
                }
            }

        }
        return courierPointList;
    }


    public List<ShipmentPoint> clone(List<ShipmentPoint> src) {
        List<ShipmentPoint> ret = new ArrayList<>();
        for(ShipmentPoint source : src) {
            ShipmentPoint target = new ShipmentPoint();
            BeanUtils.copyProperties(source,target);
            ret.add(target);
        }
        return ret;
    }
    public Integer getNum(List<ShipmentPoint> shipment,
                          List<CourierPoint> courier){
        Integer num = ( shipment.size() + 29 ) / 30 ;
        Integer newnum = 0;
        if(shipment.size() > courier.size()){
            num = courier.size() > num ? courier.size() : num;
        } else {
            num = courier.size() < num ? courier.size() : num;
        }
        if(shipment.size() > 1) {
            List<Edge> edgeTreeList = heapService.getEdgeTreeList(shipment);
            while(num != newnum) {
                newnum = num;
                List<List<ShipmentPoint>> shipmentHeap = heapService.heapDepartTree(shipment, edgeTreeList, num);
                for(List<ShipmentPoint> shipmentPointList : shipmentHeap){
                    if(shipmentPointList.size() > 30) {
                        num++;
                    }
                }
            }
        }
        return newnum;
    }
    public void typeOutput(List<ShipmentPoint> shipment, String c) {

    }
    public void waitOutput(List<ShipmentPoint> shipment){
        typeOutput(shipment,"-1");
    }
    public void notConditionsOutput(List<ShipmentPoint> shipment, Integer type){
        typeOutput(shipment,"-2");
    }
    public Integer countdassigneShipmentList(List<ShipmentPoint> shipment, List<ShipmentDispatchAndETA> shipmentDispatchAndETAList){
        List<String> assigneShipmentIdList = shipmentDispatchAndETAList.stream().map(ShipmentDispatchAndETA::getExpressno).collect(Collectors.toList());
        List<String> allShipmentIdList = shipment.stream().map(ShipmentPoint::getExpressno).collect(Collectors.toList());
        allShipmentIdList.retainAll(assigneShipmentIdList);
        return allShipmentIdList.size();
    }
    public void  dispatchService(List<ShipmentPoint> shipment, Integer shipmentCount,
                    List<CourierPoint> courier,
                    List<ShipmentPoint> assignedShipmentList, AtomicInteger num) {
        logger.info("!!!!!!!!!!!!!!!!!!num="+num+" shipment="+shipment.size());
        List<List<ShipmentPointDraw>> shipmentListList = new ArrayList<>();
        while (num.get() <= shipment.size()) {
            List<List<ShipmentPoint>> shipmentHeap = heapService.heap(shipment, num.get());
            if(shipmentHeap.size() == 0) {
                break;
            }
            shipmentHeap.sort(new Comparator<List<ShipmentPoint>>() {
                public int compare(List<ShipmentPoint> o1, List<ShipmentPoint> o2) {
                    if(o1.size() > o2.size()) {
                        return -1;
                    }
                    if(o1.size() < o2.size()) {
                        return 1;
                    }
                    return 0;
                }
            });
//            Boolean fail = false;
            int number = courier.size() < shipmentHeap.size()? courier.size():shipmentHeap.size();
            for(int i = 0; i < number; i++ ) {
//            for(List<ShipmentPoint> shipmentPointList : shipmentHeap) {
                List<ShipmentPoint> shipmentPointList = clone(shipmentHeap.get(i));
//                shipmentPointList.addAll(assignedShipmentList);
                try {
                    AntResult antResult = getAntResult(shipmentPointList, assignedShipmentList, courier);
                    List<ShipmentDispatchAndETAEntity> shipmentDispatchAndETAEntityList = new ArrayList<>();
                    List<ShipmentDispatchAndETA> shipmentDispatchAndETAList = antResult.getShipmentDispatchAndETAS();
                    for(ShipmentDispatchAndETA shipmentDispatchAndETA : shipmentDispatchAndETAList) {
                        ShipmentDispatchAndETAEntity shipmentDispatchAndETAEntity = new ShipmentDispatchAndETAEntity();
                        BeanUtils.copyProperties(shipmentDispatchAndETA, shipmentDispatchAndETAEntity);

                        shipmentDispatchAndETAEntityList.add(shipmentDispatchAndETAEntity);
                    }
                    logger.info("路线总数"+shipmentDispatchAndETAEntityList.size()+"分配"+countdassigneShipmentList(shipment, shipmentDispatchAndETAList));
                    List<String> shipmentIds = shipmentDispatchAndETAList.stream().map(ShipmentDispatchAndETA::getExpressno).collect(Collectors.toList());
                    logger.info("分配出的订单号:"+JSONObject.toJSONString(shipmentIds));

                    output(shipmentDispatchAndETAEntityList);

                    CourierPoint courierPointTmp = null;
                    for(CourierPoint courierPoint : courier) {
                        if(courierPoint.getId().equals(antResult.getCourierId())) {
                            courierPointTmp = courierPoint;
                            break;
                        }
                    }

                    shipmentListList.add(antResult2ShipmentPointDraw(antResult, courierPointTmp));

                    for(CourierPoint courierPoint : courier) {
                        if(courierPoint.getId().equals(antResult.getCourierId())) {
                            courier.remove(courierPoint);
                            break;
                        }
                    }
                    shipment.removeAll(shipmentPointList);
                    if(courier.size() == 0 || shipment.size() == 0){
                        logger.info("Dispatch complete! left shipment ="+shipment.size());
                        // outputFile(shipmentListList, "a.json");

                        return;
                    }
                } catch (InvalidTimeException e) {
//                    fail = true;
                    logger.info("失败num="+num+"shipment size="+shipment.size());
                    if(num.get() == shipmentCount) {
                        continue;
                    } else {
                        num.getAndIncrement();
                        break;
                    }
                }
            }
            if(num.get() == shipmentCount) {
                num.getAndIncrement();
            }
//            if(fail == false) {
//                break;
//            }
        }
    }


    @Async
    public void asyncPreferencesService(List<ShipmentPoint> shipments,
                                     List<CourierPoint> couriers,
                                     List<ShipmentPoint> assignedShipmentList){

        if(shipments.size() == 0) {
            return;
        }
        if(couriers.size() > 0 && shipments.size() > 0) {
            logger.info("start normal shipment size="+shipments.size());
            asyncDispatchService(shipments, couriers, assignedShipmentList);
        } else if (shipments.size() > 0) {
            waitOutputPrint(shipments);
        }

    }
//    @Async
    public List<ShipmentPoint> asyncDispatchService(List<ShipmentPoint> shipment,
                                     List<CourierPoint> courier,
                                     List<ShipmentPoint> assignedShipmentList){
//        List<List<ShipmentPointDraw>> shipmentListList = new ArrayList<>();
        if(courier.size() == 0){
            return shipment;
        }
        int shipmentCount = shipment.size();
        logger.info("Dispatch action shipment ="+shipmentCount+" assignedShipmentList="+assignedShipmentList.size());
        long startTime = System.currentTimeMillis();
        Integer num = ( shipmentCount + 29 ) / 30 ;
        if(this.model.equals(0)) {
            num = getNum(shipment, courier);
        } else if (this.model.equals(1)) {
            num = ( shipmentCount + 29 ) / 30 ;
        } else {
        }
        AtomicInteger n = new AtomicInteger(num);
        dispatchService(shipment, shipmentCount, courier, assignedShipmentList, n);

        if(n.get() > shipmentCount) {
            failAssignedPrint(n.get(), shipment);
        } else {
            if(shipment.size() > 0) {
                waitOutputPrint(shipment);
            }
        }
//        logger.info("num="+n.get()+"shipment size="+shipment.size());
        long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) / 1000F + "秒");
        return shipment;
    }
    public void failAssignedPrint(int n, List<ShipmentPoint> shipment){
        failAssigned(n, shipment);
        logger.info("failAssigned shipment ="+shipment.size());
        List<String> shipmentIds = shipment.stream().map(ShipmentPoint::getExpressno).collect(Collectors.toList());
        logger.info("failAssigned订单号:"+JSONObject.toJSONString(shipmentIds));
    }
    public void waitOutputPrint(List<ShipmentPoint> shipment) {
        waitOutput(shipment);
        logger.info("waitOutput shipment ="+shipment.size());
        List<String> shipmentIds = shipment.stream().map(ShipmentPoint::getExpressno).collect(Collectors.toList());
        logger.info("waitOutput订单号:"+JSONObject.toJSONString(shipmentIds));
    }
    private void failAssigned(Integer num, List<ShipmentPoint> shipment){
        logger.info("fail num="+num+"shipment size="+shipment.size());
    }

    private void outputFile(List<List<ShipmentPointDraw>> response, String fileName) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", response);

        File file = new File("/tps/src/main/resources/output/" + fileName);
        try {
            FileUtils.writeStringToFile(file, JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect), "utf8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ShipmentPoint findShipmentPoint(int tourIndex, List<ShipmentPoint> shipmentPointList, AtomicInteger startEnd){
        int index = 1;
        for(int i = 0 ; i < shipmentPointList.size() ; i++) {
            if(shipmentPointList.get(i).getStatus().equals("got")) {
                if(index == tourIndex) {
                    startEnd.set(1);
                    return shipmentPointList.get(i);
                } else {
                    index++;
                }
            } else {
                if(index == tourIndex) {
                    startEnd.set(0);
                    return shipmentPointList.get(i);
                }
                index++;
                if(index == tourIndex) {
                    startEnd.set(1);
                    return shipmentPointList.get(i);
                } else {
                    index++;
                }
            }
        }
        return null;
    }
    public List<ShipmentPointDraw> antResult2ShipmentPointDraw(AntResult antResult, CourierPoint courierPoint){
        List<ShipmentPointDraw> shipmentPointDrawList = new ArrayList<>();
        ShipmentPointDraw shipmentPointDraw = new ShipmentPointDraw();
        shipmentPointDraw.setCourier(courierPoint.getId());
        WindPoint startPoint = new WindPoint();
        startPoint.setLat(courierPoint.getStart().getLat());
        startPoint.setLng(courierPoint.getStart().getLng());
        startPoint.setTime(courierPoint.getStart().getTime());
        shipmentPointDraw.setPoint(startPoint);
        shipmentPointDraw.setPointType(1);
        shipmentPointDraw.setStart(true);
        shipmentPointDrawList.add(shipmentPointDraw);
        for(int i = 1; i < antResult.getBestTour().length; i++) {
            int index = antResult.getBestTour()[i];
//            int shipmentIndex = (index+1) / 2 - 1;
//            int shipmentStart = index % 2;
//            ShipmentPoint shipmentPoint = antResult.getShipmentPointList().get(shipmentIndex);
            AtomicInteger startEnd = new AtomicInteger();
            ShipmentPoint shipmentPoint = findShipmentPoint(index, antResult.getShipmentPointList(), startEnd);

            ShipmentPointDraw shipmentPointDraw1 = new ShipmentPointDraw();
            shipmentPointDraw1.setShipmentId(shipmentPoint.getExpressno());
            shipmentPointDraw1.setCourier(courierPoint.getId());
            shipmentPointDraw1.setPointType(2);
            if(startEnd.get() == 0) {
                shipmentPointDraw1.setStart(true);
                WindPoint startPoint1 = new WindPoint();
                startPoint1.setLat(shipmentPoint.getStart().getLat());
                startPoint1.setLng(shipmentPoint.getStart().getLng());
                shipmentPointDraw1.setPoint(startPoint1);
            } else {
                shipmentPointDraw1.setStart(false);
                WindPoint startPoint1 = new WindPoint();
                startPoint1.setLat(shipmentPoint.getEnd().getLat());
                startPoint1.setLng(shipmentPoint.getEnd().getLng());
                shipmentPointDraw1.setPoint(startPoint1);
            }
            shipmentPointDrawList.add(shipmentPointDraw1);
        }
        return shipmentPointDrawList;
    }
    public List<City> shipmentDispatchAndETAEntityList2CityList(List<ShipmentDispatchAndETAEntity> shipmentDispatchAndETAEntityList,
                                                                List<ShipmentPoint> assignedShipmentList) {

        Map<String, ShipmentPoint> shipmentPointMap = assignedShipmentList.stream().collect(
                Collectors.toMap(ShipmentPoint::getExpressno, d->d, (oldValue, newValue)->newValue));

        List<City> cityList = new ArrayList<>();
        Integer index = 1;
        for(ShipmentDispatchAndETAEntity shipmentDispatchAndETAEntity : shipmentDispatchAndETAEntityList) {
            City city1 = new City();
            city1.setId(index);
            index++;
            city1.setShipmentId(shipmentDispatchAndETAEntity.getExpressno());
            city1.setPlan(shipmentDispatchAndETAEntity.getPlangettime());
            city1.setLat(shipmentPointMap.get(shipmentDispatchAndETAEntity.getExpressno()).getStart().getLat());
            city1.setLng(shipmentPointMap.get(shipmentDispatchAndETAEntity.getExpressno()).getStart().getLng());
            cityList.add(city1);
            City city2 = new City();
            city2.setId(index);
            index++;
            city2.setShipmentId(shipmentDispatchAndETAEntity.getExpressno());
            city2.setPlan(shipmentDispatchAndETAEntity.getPlansendtime());
            city2.setLat(shipmentPointMap.get(shipmentDispatchAndETAEntity.getExpressno()).getEnd().getLat());
            city2.setLng(shipmentPointMap.get(shipmentDispatchAndETAEntity.getExpressno()).getEnd().getLng());
            cityList.add(city2);
        }
        return cityList;
    }
    public int getPathLength(CourierPoint courier, List<ShipmentPoint> assignedShipmentList){
        List<String> expressnoList =
                assignedShipmentList.stream().filter(s -> s.getCourier().equals(courier.getId())).map(ShipmentPoint::getExpressno).collect(Collectors.toList());
        List<ShipmentDispatchAndETAEntity> shipmentDispatchAndETAEntityList = shipmentDispatchAndETAEntityRepository.findByExpressnoList(expressnoList);
        City start = new City();
        start.setLat(courier.getStart().getLat());
        start.setLng(courier.getStart().getLng());
        start.setPlan(courier.getStart().getTime());
        List<City> cityPath = new ArrayList<>();
        cityPath.add(start);

        List<City> cityList = shipmentDispatchAndETAEntityList2CityList(shipmentDispatchAndETAEntityList, assignedShipmentList);
        cityList = cityList.stream().sorted(Comparator.comparing(City::getPlanLong)).collect(Collectors.toList());
        cityPath.addAll(cityList);

        City end = new City();
        end.setLat(courier.getEnd().getLat());
        end.setLng(courier.getEnd().getLng());
        cityPath.add(end);

        int len = 0;
        for(int i = 0; i < cityPath.size() -1 ; i++){
            len += (int) AlgReUtil.getDistance(cityPath.get(i).getLat().doubleValue(), cityPath.get(i).getLng().doubleValue(),
                    cityPath.get(i+1).getLat().doubleValue(), cityPath.get(i+1).getLng().doubleValue());
        }
        return len;
    }
    public AntResult getAntResult(List<ShipmentPoint> shipment, List<ShipmentPoint> assignedShipmentList,
                            List<CourierPoint> courier) throws InvalidTimeException {
        List<ACO> acoList = new ArrayList<>();

        for(CourierPoint courierPoint : courier) {
            List<ShipmentPoint> shipmentIn = clone(shipment);
            List<ShipmentPoint> courierAssignedShipmentList =
                    assignedShipmentList.stream().filter(s -> s.getCourier().equals(courierPoint.getId())).collect(Collectors.toList());
            shipmentIn.addAll(courierAssignedShipmentList);
            List<List<City>> shipmentCityList = createShipmentCityList(courierPoint, shipmentIn);
            List<City> cityList = shipmentCityList2CityList(shipmentCityList);
            List<City> startEndCityList = createStartEndCity(courierPoint);
            Integer generation = 5;
//            if(cityList.size() < 16) {
//                generation = 16;
//            } else if(cityList.size() > 16 && cityList.size() < 100 ){
//                generation = cityList.size();
//            } else {
//                generation = 100;
//            }
//            generation = 5;
            long startTime = System.currentTimeMillis();
            int len = getPathLength(courierPoint, courierAssignedShipmentList);
            ACO aco = new ACO(cityList.size()+1,  generation, 1.0, 5.0, 0.5, 10, 2);
            aco.init(startEndCityList.get(0), cityList, startEndCityList.get(1), courierPoint, shipmentCityList);
            aco.iterator();
            long endTime = System.currentTimeMillis();
            System.out.println((endTime - startTime) / 1000F + "秒");
            aco.greedyTour();
            aco.getBestAnt().updatePlanTime();
            aco.getBestAnt().updateEndPlanTime();
            aco.setOldLength(len);
            acoList.add(aco);
        }
        AntResult antResult = new AntResult();
        if(acoList.size() > 0) {
            ACO bastAco = acoList.get(0);
            for(int i = 1; i < acoList.size(); i++) {
                if(bastAco.getBestLength() - bastAco.getOldLength() > acoList.get(i).getBestLength() - bastAco.getOldLength() ) {
                    bastAco = acoList.get(i);
                }
            }
            if(bastAco.getBestLength() < Integer.MAX_VALUE) {
                antResult.setBestLength(bastAco.getBestLength());
                antResult.setBestTour(bastAco.getBestTour());
                antResult.setCourierId(bastAco.getCourierPoint().getId());
                shipment.addAll(assignedShipmentList);
                antResult.setShipmentPointList(shipment);
                antResult.setShipmentDispatchAndETAS(cityListToShipmentDispatchAndETA(bastAco.getShipmentCityList(), bastAco.getCourierPoint().getId()));
                return antResult;
            } else {
                throw new InvalidTimeException();
            }
        } else {
            throw new InvalidTimeException();
        }
    }

    public List<ShipmentDispatchAndETA> cityListToShipmentDispatchAndETA(List<City> cityList, String courierId){
        List<ShipmentDispatchAndETA> shipmentDispatchAndETAS = new ArrayList<>();
        Map<String, List<City>> shipment = cityList.stream().filter(city -> city.getShipmentId() != null)
                .collect(Collectors.groupingBy(City::getShipmentId));
        for(Map.Entry<String, List<City>> entry : shipment.entrySet()){
            String shipmentId = entry.getKey();
            List<City> mapValue = entry.getValue();
            ShipmentDispatchAndETA shipmentDispatchAndETA = new ShipmentDispatchAndETA();
            shipmentDispatchAndETA.setExpressno(shipmentId);
            shipmentDispatchAndETA.setCourier(courierId);
            long plangettime = 0;
            long plansendtime = 0;
            for (City city : mapValue) {
                if (city.getUnit() > 0) {
                    plangettime = city.getPlan().getTime();
                } else {
                    plansendtime = city.getPlan().getTime();
                }
            }
            shipmentDispatchAndETA.setPlangettime(new Date(plangettime));
            shipmentDispatchAndETA.setPlansendtime(new Date(plansendtime));

            shipmentDispatchAndETAS.add(shipmentDispatchAndETA);
        }
        return shipmentDispatchAndETAS;
    }

    public void initCourierPoint(CourierPoint courierPoint) {
        if(courierPoint.getSpeed() == null) {
            courierPoint.setSpeed(new BigDecimal(30));
        }
        if(courierPoint.getSendOrdertime() == null) {
            courierPoint.setSendOrdertime(new BigDecimal(10));
        }
        if(courierPoint.getGetOrdertime() == null){
            courierPoint.setGetOrdertime(new BigDecimal(10));
        }
        if(courierPoint.getSamePoint() == null) {
            courierPoint.setSamePoint(new BigDecimal(2));
        }
    }
    public List<City> createStartEndCity(CourierPoint courierPoint) {
        Integer index = 0;
        List<City> cityList = new ArrayList<>();
        City start = new City();
        start.setId(index);
        start.setLng(courierPoint.getStart().getLng());
        start.setLat(courierPoint.getStart().getLat());
        start.setStart(courierPoint.getStart().getTime());
        start.setPlan(courierPoint.getStart().getTime());
        cityList.add(start);

        City end = new City();
        end.setId(index);
        end.setLng(courierPoint.getEnd().getLng());
        end.setLat(courierPoint.getEnd().getLat());
        end.setEnd(courierPoint.getEnd().getTime());
        cityList.add(end);
        return cityList;
    }
    public List<City> createCityList(CourierPoint courierPoint, List<ShipmentPoint> shipment){

        Integer index = 0;

        List<City> cityList = new ArrayList<>();
        City start = new City();
        start.setId(index);
        start.setLng(courierPoint.getStart().getLng());
        start.setLat(courierPoint.getStart().getLat());
        start.setStart(courierPoint.getStart().getTime());
        start.setPlan(courierPoint.getStart().getTime());
        cityList.add(start);

        List<List<City>> shipmentCityList = createShipmentCityList(courierPoint, shipment);
        return shipmentCityList2CityList(shipmentCityList);
    }
    public List<City> shipmentCityList2CityList(List<List<City>> shipmentCityList) {
        List<City> cityList = new ArrayList<>();
        for(List<City> cityList1 : shipmentCityList){
            cityList.addAll(cityList1);
        }
        return cityList;
    }
    public List<List<City>> createShipmentCityList(CourierPoint courierPoint, List<ShipmentPoint> shipment){
        initCourierPoint(courierPoint);
        Integer index = 0;
        List<List<City>> cityList = new ArrayList<>();
        for(ShipmentPoint shipmentPoint : shipment) {
            if(courierPoint.getId().equals(shipmentPoint.getCourier())) {
                List<City> cities = new ArrayList<>();
                index++;
                City city1 = new City();
                city1.setId(index);
                city1.setShipmentId(shipmentPoint.getExpressno());
                city1.setLat(shipmentPoint.getStart().getLat());
                city1.setLng(shipmentPoint.getStart().getLng());
                city1.setStart(shipmentPoint.getStart().getTime());
                city1.setEnd(getEndTime(shipmentPoint.getStart(), shipmentPoint.getEnd(), courierPoint.getSpeed()));
                city1.setUnit(shipmentPoint.getChargeunit());
                if(shipmentPoint.getStatus().equals("got")) {
                    city1.setPlan(shipmentPoint.getEnd().getTime());
                } else {
                    city1.setPlan(shipmentPoint.getStart().getTime());
                }
                cities.add(city1);
                if(!shipmentPoint.getStatus().equals("got")) {
                    index++;
                    City city2 = new City();
                    city2.setId(index);
                    city2.setShipmentId(shipmentPoint.getExpressno());
                    city2.setLat(shipmentPoint.getEnd().getLat());
                    city2.setLng(shipmentPoint.getEnd().getLng());
                    city2.setStart(getStartTime(shipmentPoint.getStart(), shipmentPoint.getEnd(), courierPoint.getSpeed()));
                    city2.setEnd(shipmentPoint.getEnd().getTime());
                    city2.setUnit(0 - shipmentPoint.getChargeunit());
                    city2.setPlan(shipmentPoint.getEnd().getTime());
                    cities.add(city2);
                }
                cityList.add(cities);
            }
            if(shipmentPoint.getCourier() == null){
                List<City> cities = new ArrayList<>();
                index++;
                City city1 = new City();
                city1.setId(index);
                city1.setShipmentId(shipmentPoint.getExpressno());
                city1.setLat(shipmentPoint.getStart().getLat());
                city1.setLng(shipmentPoint.getStart().getLng());
                city1.setStart(shipmentPoint.getStart().getTime());
                city1.setEnd(getEndTime(shipmentPoint.getStart(), shipmentPoint.getEnd(), courierPoint.getSpeed()));
                city1.setUnit(shipmentPoint.getChargeunit());
                cities.add(city1);
                index++;

                City city2 = new City();
                city2.setId(index);
                city2.setShipmentId(shipmentPoint.getExpressno());
                city2.setLat(shipmentPoint.getEnd().getLat());
                city2.setLng(shipmentPoint.getEnd().getLng());
                city2.setStart(getStartTime(shipmentPoint.getStart(), shipmentPoint.getEnd(), courierPoint.getSpeed()));
                city2.setEnd(shipmentPoint.getEnd().getTime());
                city2.setUnit(0-shipmentPoint.getChargeunit());
                cities.add(city2);

                cityList.add(cities);
            }
        }
        return cityList;
    }
//    public CityGraph2 createCityGraph2(CourierPoint courierPoint, List<ShipmentPoint> shipment){
//        initCourierPoint(courierPoint);
//        Integer index = 0;
//        CityGraph2 cityGraph2 = new CityGraph2();
//        List<List<City>> cityList = new ArrayList<>();
//        City start = new City();
//        start.setId(index);
//        start.setLng(courierPoint.getStart().getLng());
//        start.setLat(courierPoint.getStart().getLat());
//        start.setStart(courierPoint.getStart().getTime());
//        start.setPlan(courierPoint.getStart().getTime());
//        cityGraph2.setStart(start);
//
//        for(ShipmentPoint shipmentPoint : shipment) {
//            if(courierPoint.getId().equals(shipmentPoint.getCourier())) {
//                List<City> cities = new ArrayList<>();
//                index++;
//                City city1 = new City();
//                city1.setId(index);
//                city1.setShipmentId(shipmentPoint.getExpressno());
//                city1.setLat(shipmentPoint.getStart().getLat());
//                city1.setLng(shipmentPoint.getStart().getLng());
//                city1.setStart(shipmentPoint.getStart().getTime());
//                city1.setEnd(getEndTime(shipmentPoint.getStart(), shipmentPoint.getEnd(), courierPoint.getSpeed()));
//                city1.setUnit(shipmentPoint.getChargeunit());
//                cities.add(city1);
//                index++;
//
//                City city2 = new City();
//                city2.setId(index);
//                city2.setShipmentId(shipmentPoint.getExpressno());
//                city2.setLat(shipmentPoint.getEnd().getLat());
//                city2.setLng(shipmentPoint.getEnd().getLng());
//                city2.setStart(getStartTime(shipmentPoint.getStart(), shipmentPoint.getEnd(), courierPoint.getSpeed()));
//                city2.setEnd(shipmentPoint.getEnd().getTime());
//                city2.setUnit(0-shipmentPoint.getChargeunit());
//                cities.add(city2);
//
//                cityList.add(cities);
//            }
//        }
//        index++;
//        City end = new City();
//        end.setId(index);
//        end.setLng(courierPoint.getEnd().getLng());
//        end.setLat(courierPoint.getEnd().getLat());
//        end.setEnd(courierPoint.getEnd().getTime());
//        cityGraph2.setEnd(end);
//
//        cityGraph2.setShipment(cityList);
//        cityGraph2.initPheronome();
//        return cityGraph2;
//    }

    public Date getEndTime(WindPoint start, WindPoint end, BigDecimal speed) {
        double distance = Utils.distanceSimplify(start.getLat().doubleValue(), start.getLng().doubleValue(), end.getLat().doubleValue(), end.getLng().doubleValue());
        BigDecimal bspeed = speed.divide(new BigDecimal(3.6), 2);
        double time = (distance / bspeed.doubleValue());
        return new Date(end.getTime().getTime()-(long)time);
    }

    public Date getStartTime(WindPoint start, WindPoint end, BigDecimal speed) {
        double distance = Utils.distanceSimplify(start.getLat().doubleValue(), start.getLng().doubleValue(), end.getLat().doubleValue(), end.getLng().doubleValue());
        Long time = (long)(distance / (speed.divide(new BigDecimal(3.6), 2)).doubleValue());
        return new Date(start.getTime().getTime()+time);
    }
}
