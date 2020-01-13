package com.mrwind.tdcant.windaco;

import com.mrwind.tdcant.common.Utils;
import com.mrwind.tdcant.dto.City;
import com.mrwind.tdcant.dto.CourierPoint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class Greedy {
    static int greedyTour(int[] tour,
                          int length,
                          double[][] distance,
                          List<City> shipmentCityList,
                          City endCity,
                          CourierPoint courierPoint) {

        for(int i = 1; i < shipmentCityList.size() ; i++) {
            for(int j = i+1; j < shipmentCityList.size() ; j++) {
                int[] tmpTour = new int[shipmentCityList.size()];
                System.arraycopy(tour, 0, tmpTour, 0, shipmentCityList.size());

                int beforeLen = getDistance(shipmentCityList, endCity, distance, tmpTour);
                reverseSubTour(tmpTour, i, j);
                int afterLen = getDistance(shipmentCityList, endCity, distance, tmpTour);
                if(afterLen < beforeLen) {
                    //
                    List<Integer> tourList = Arrays.stream(tmpTour).boxed().collect(Collectors.toList());
                    if(checkFirstPickSecondDelivery(tourList, shipmentCityList) == Boolean.FALSE ||
                            checkEffectiveness(tourList, shipmentCityList, courierPoint, distance)== Boolean.FALSE ||
                            checkEnd(courierPoint, tourList, endCity, shipmentCityList) == Boolean.FALSE ||
                            checkUnit(courierPoint, tourList, shipmentCityList) == Boolean.FALSE) {

                    } else {
                        reverseSubTour(tour, i, j);
                    }
                }
            }
        }
//        int last = tour[tour.length-1];
//        City city = getCity(new Integer(last), shipmentCityList);
//        double homelen = Utils.distanceSimplify(city.getLat().doubleValue(), city.getLng().doubleValue(), endCity.getLat().doubleValue(), endCity.getLng().doubleValue());
        List<Integer> tourListNew = Arrays.stream(tour).boxed().collect(Collectors.toList());
        if(checkFirstPickSecondDelivery(tourListNew, shipmentCityList) == Boolean.FALSE ||
                checkEffectiveness(tourListNew, shipmentCityList, courierPoint, distance)== Boolean.FALSE ||
                checkEnd(courierPoint, tourListNew, endCity, shipmentCityList) == Boolean.FALSE ||
                checkUnit(courierPoint, tourListNew, shipmentCityList) == Boolean.FALSE) {
            return Integer.MAX_VALUE;
        } else {
            return getDistance(shipmentCityList, endCity, distance, tour) ;
        }
    }
    private static double getDistance(Integer start, Integer end, double[][] distance, int[] tour) {
        double sum = 0;
        for(int i = start; i < end - start -1; i++) {
            sum += distance[tour[i]][tour[i+1]];
        }
        return sum;
    }
    private static double penaltyFunction(List<City> shipmentCityList, int start, int end, double[][] distance){
        if(shipmentCityList.get(start).getShipmentId() != null &&
                shipmentCityList.get(start).getShipmentId().equals(shipmentCityList.get(end).getShipmentId()) &&
                shipmentCityList.get(start).getUnit() < 0 &&
                shipmentCityList.get(end).getUnit() > 0
        ) {
            return distance[start][end] + 500*1000;
        } else {
            return distance[start][end];
        }

    }
    private static int getDistance(List<City> shipmentCityList, City endCity, double[][] distance, int[] tour) {
        int sum = 0;
        for(int i = 0; i < shipmentCityList.size() - 1; i++) {
            sum += penaltyFunction(shipmentCityList, tour[i], tour[i+1], distance);
        }
        City tmp = getCity(tour[shipmentCityList.size()-1], shipmentCityList);
//        City tmp = shipmentCityList.get(shipmentCityList.size() - 1);

        int homelen = (int)Utils.distanceSimplify(tmp.getLat().doubleValue(), tmp.getLng().doubleValue(), endCity.getLat().doubleValue(), endCity.getLng().doubleValue());

        return sum + homelen;
    }
    private static void reverseSubTour(int[] tour, int i, int j) {
        for (; i < j; ++i, --j) {
            tour[i] ^= tour[j];
            tour[j] ^= tour[i];
            tour[i] ^= tour[j];
        }
    }
    public static Boolean checkFirstPickSecondDelivery(List<Integer> tabu, List<City> shipmentCityList){
        Map<String, List<City>> singleMap = shipmentCityList.stream().filter(city -> city.getShipmentId() != null).collect(Collectors.groupingBy(City::getShipmentId));
        for(List<City> value : singleMap.values()){
            if(value.size() == 1) {

            } else if(value.size() == 2) {
                Boolean isStart = value.get(0).getUnit() > 0 ? Boolean.TRUE:Boolean.FALSE;
                Boolean vaild = Boolean.TRUE;
                if(isStart == Boolean.TRUE) {
                    vaild = checkFirstAndSecond(value.get(0).getId(), value.get(1).getId(), tabu);
                } else {
                    vaild = checkFirstAndSecond(value.get(1).getId(), value.get(0).getId(), tabu);
                }
                if(vaild == Boolean.FALSE) {
                    return Boolean.FALSE;
                }
            } else if(value.size() == 3) {

            } else {

            }
        }
        return Boolean.TRUE;
    }
    public static Boolean checkFirstAndSecond(Integer start, Integer end, List<Integer> tabu) {
        Integer startIndex = 0;
        Integer endIndex = 0;
        for(int i = 0 ; i < tabu.size(); i++) {
            if(tabu.get(i).equals(start)) {
                startIndex = i;
            }
            if(tabu.get(i).equals(end)) {
                endIndex = i;
            }
        }
        if(startIndex < endIndex){
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
    public static Boolean checkEnd(CourierPoint courierPoint, List<Integer> tabu, City endCity, List<City> shipmentCityList) {
        return Boolean.TRUE;
//        updateEndPlanTime(courierPoint, tabu, endCity, shipmentCityList);
//        if(endCity.getPlan().getTime() <= endCity.getEnd().getTime()) {
//            return Boolean.TRUE;
//        } else {
//            return Boolean.FALSE;
//        }
    }
    public static Boolean checkUnit(CourierPoint courierPoint, List<Integer> tabu, List<City> shipmentCityList){
        Integer unit = courierPoint.getUnitlimit();
//        for(Integer index: tabu) {
//            if(index != 0) { //过滤出发点
//                City city = getCity(index, shipmentCityList);
//                if(unit - city.getUnit() < 0) {
//                    return Boolean.FALSE;
//                }
//            }
//        }
        return Boolean.TRUE;
    }
    public static void updateEndPlanTime(CourierPoint courierPoint, List<Integer> tabu, City endCity, List<City> shipmentCityList) {
        City city = getCity(tabu.get(tabu.size()-1), shipmentCityList);
        double len = Utils.distanceSimplify(city.getLat().doubleValue(), city.getLng().doubleValue(), endCity.getLat().doubleValue(), endCity.getLng().doubleValue());
        long time = (long)(len / (courierPoint.getSpeed().divide(new BigDecimal(3.6), 2, RoundingMode.DOWN).longValue()));
        time = city.getPlan().getTime() + time * 1000;
        endCity.setPlan(new Date(time));
    }
    public static City getCity(Integer index, List<City> shipmentCityList) {
        for(City city : shipmentCityList){
            if(city.getId().equals(index)){
                return city;
            }
        }
        return null;
    }
    public static Boolean checkEffectiveness(List<Integer> tabu, List<City> shipmentCityList, CourierPoint courierPoint, double[][] distance) {
        updatePlanTime(tabu, courierPoint, distance, shipmentCityList);
        for(City city : shipmentCityList) {
            if(city.getId() == 0) {

            } else if(city.getStart().getTime() <= city.getPlan().getTime() &&
                    city.getPlan().getTime() <= city.getEnd().getTime()) {

            } else {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
    public static void updatePlanTime(List<Integer> tabu, CourierPoint courierPoint, double[][] distance, List<City> shipmentCityList) {

        long currentTime = 0;
        for(int i = 0; i < tabu.size(); i++){
            if(i == 0) {
                currentTime = courierPoint.getStart().getTime().getTime();

            } else {
                long time = (long)(distance[tabu.get(i-1)][tabu.get(i)] / (courierPoint.getSpeed().divide(new BigDecimal(3.6), 2, RoundingMode.DOWN).longValue()));

                City city = getCity(tabu.get(i), shipmentCityList);
                if(city == null || city.getUnit() == null) {
                    System.out.println("s");
                }
                Boolean isStart = city.getUnit() > 0 ? Boolean.TRUE:Boolean.FALSE;
                if (isStart) {
                    time = time + courierPoint.getGetOrdertime().longValue() * 60;
                } else {
                    time = time + courierPoint.getSendOrdertime().longValue() * 60;
                }
                time = currentTime + time * 1000;
                city.setPlan(new Date(time));
                currentTime = time;
            }
        }
    }
}
