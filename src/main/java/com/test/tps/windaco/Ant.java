package com.test.tps.windaco; /**
 * Created by houseyoung on 16/5/11 19:47.
 */

import com.test.tps.common.Utils;
import com.test.tps.dto.City;
import com.test.tps.dto.CourierPoint;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class Ant {
    private Boolean isBatchDispatch;
    private CourierPoint courierPoint;
    private List<City> shipmentCityList;
    private List<List<City>> dependentCity;
    private City endCity;
    private ArrayList<Integer> tabu; // 禁忌表
    private List<Integer> allowedCities; // 下一步允许选择的城市
    private double[][] delta; // 信息素增量矩阵
    private double[][] distance; // 距离矩阵
    private double[][] eta; // 能见度矩阵

    private double alpha; // 信息素重要程度系数
    private double beta; // 城市间距离重要程度系数

    private int tourLength = 0; // tabu路径长度
    private int cityNum; // 城市数量
    private int firstCity; // 起始城市
    private int currentCity; // 当前城市

    /**
     * 构造方法
     * @param cityNum
     */
    public Ant(int cityNum) {
        this.cityNum = cityNum;
        tourLength = 0;
    }

//    /**
//     * 初始化蚂蚁，随机挑选一个城市作为起始位置
//     * @param distance
//     * @param alpha
//     * @param beta
//     */
//    public void init(List<List<City>> shipmentCityList, CourierPoint courierPoint, City firstCity, double[][] distance, double alpha, double beta) {
////        this.shipmentCityList = shipmentCityList;
//        this.alpha = alpha;
//        this.beta = beta;
//        this.distance = distance;
////        this.courierPoint = courierPoint;
//        // 初始化禁忌表为空
//        tabu = new ArrayList<Integer>();
//
//        // 初始化信息素增量矩阵为0
//        // 初始化下一步允许选择的城市为所有城市
//        delta = new double[cityNum][cityNum];
////        allowedCities = new ArrayList<Integer>();
//        allowedCities = getAllowedCityIndex(courierPoint, firstCity);
//        for (int i = 0; i < cityNum; i++) {
////            allowedCities.add(i);
//            for (int j = 0; j < cityNum; j++) {
//                delta[i][j] = 0.0;
//            }
//        }
//
//        // 随机挑选一个城市作为蚂蚁的起始城市
//        Random random = new Random();
////        this.current = firstCity;
//        this.firstCity = firstCity.getId(); // random.nextInt(cityNum);
//
//        // 从未访问过的城市集合中移除起始城市
////        for (Integer integer : allowedCities) {
////            if (integer.intValue() == firstCity) {
////                allowedCities.remove(integer);
////                break;
////            }
////        }
//
//        // 已访问过的城市集合中添加起始城市
//        tabu.add(this.firstCity);
//
//        // 将当前城市设为起始城市
//        currentCity = this.firstCity;
//
//        // 根据距离矩阵计算能见度矩阵
//        eta = new double[cityNum][cityNum];
//        for (int i = 0; i < cityNum - 1; i++) {
//            eta[i][i] = 0; // 对角线为0
//            for (int j = i + 1; j < cityNum; j++) {
//                eta[i][j] = 1.0 / distance[i][j];
//                eta[j][i] = eta[i][j];
//            }
//        }
//        eta[cityNum - 1][cityNum - 1] = 0;
//    }
    /**
     * 初始化蚂蚁，随机挑选一个城市作为起始位置
     * @param distance
     * @param alpha
     * @param beta
     */
    public void init(List<City> shipmentCityList, CourierPoint courierPoint, City endCity, List<List<City>> dependentCity,
                     double[][] distance, double alpha, double beta) {
        this.dependentCity = dependentCity;
        this.shipmentCityList = shipmentCityList;
        this.courierPoint = courierPoint;
        this.endCity = endCity;

        this.alpha = alpha;
        this.beta = beta;
        this.distance = distance;

        // 初始化禁忌表为空
        tabu = new ArrayList<Integer>();

        // 初始化信息素增量矩阵为0
        // 初始化下一步允许选择的城市为所有城市
        delta = new double[cityNum][cityNum];
        allowedCities = new ArrayList<Integer>();
        for (int i = 0; i < cityNum; i++) {
            if(i != 0) {// 从未访问过的城市集合中移除起始城市
                allowedCities.add(i);
            }
            for (int j = 0; j < cityNum; j++) {
                delta[i][j] = 0.0;
            }
        }

        // 随机挑选一个城市作为蚂蚁的起始城市
        Random random = new Random();
        firstCity = 0; // random.nextInt(cityNum);

        // 从未访问过的城市集合中移除起始城市
//        for (Integer integer : allowedCities) {
//            if (integer.intValue() == firstCity) {
//                allowedCities.remove(integer);
//                break;
//            }
//        }

        // 已访问过的城市集合中添加起始城市
        tabu.add(firstCity);

        // 将当前城市设为起始城市
        currentCity = firstCity;

        // 根据距离矩阵计算能见度矩阵
        eta = new double[cityNum][cityNum];
        for (int i = 0; i < cityNum - 1; i++) {
            eta[i][i] = 0; // 对角线为0
            for (int j = i + 1; j < cityNum; j++) {
                eta[i][j] = 1.0 / distance[i][j];
                eta[j][i] = eta[i][j];
            }
        }
        eta[cityNum - 1][cityNum - 1] = 0;
    }

//    /**
//     * 选择下一个城市
//     * @param pheromone
//     */
//    public void selectNextCity(double[][] pheromone) {
//        double[] probability = new double[cityNum]; // 转移概率矩阵
//        double sum = 0;
//
//        // 计算分母
//        for (int i : allowedCities) {
//            sum += Math.pow(pheromone[currentCity][i], alpha) * Math.pow(eta[currentCity][i], beta);
//        }
//
//        // 计算概率矩阵
//        for (int i = 0; i < cityNum; i++) {
//            if (allowedCities.contains(i)) {
//                probability[i] = (Math.pow(pheromone[currentCity][i], alpha) * Math.pow(eta[currentCity][i], beta)) / sum;
//            } else {
//                probability[i] = 0;
//            }
//        }
//
//        // 选择下一个城市(权重随机数算法/轮盘赌)
//        int selectCity = 0;
//
//        Random random = new Random();
//        double rand = random.nextDouble();
//        double sumPs = 0.0;
//        for (int i = 0; i < cityNum; i++) {
//            sumPs += probability[i];
//            if (sumPs >= rand) {
//                selectCity = i;
//                break;
//            }
//        }
//        if(currentCity != 0) {
//            this.current = findCity(currentCity);
//        }
//        // 从允许选择的城市中去掉选中的下一个城市
////        for (Integer i : allowedCities) {
////            if (i.intValue() == selectCity) {
////                allowedCities.remove(i);
////                break;
////            }
////        }
//        City current = updateAllowedCityList(selectCity);
//        allowedCities = getAllowedCityIndex(courierPoint, current);
//
//        // 在禁忌表中添加选中的下一个城市
//        tabu.add(selectCity);
//
//        // 将当前城市改为选中的下一个城市
//        currentCity = selectCity;
//    }
    List<List<City>> cloneCityList(List<List<City>> source) {
        List<List<City>> cloneCity = new ArrayList<>();
        for(List<City> cityList : source) {
            List<City> list = new ArrayList<>();
            for(City city : cityList) {
                City city1 = new City();
                try {
                    BeanUtils.copyProperties(city1, city);
                    list.add(city1);
                } catch (Exception e){

                }
            }
            cloneCity.add(list);
        }
        return cloneCity;
    }
    public List<Integer> getNextDependent(){
        List<List<City>> cloneCity = cloneCityList(this.dependentCity);
        for(Integer index : this.tabu) {
            for(int i = cloneCity.size() - 1; i >= 0; i--){
                if(cloneCity.get(i).size() > 0){
                    if(cloneCity.get(i).get(0).getId().equals(index)){
                        cloneCity.get(i).remove(0);
                        if(cloneCity.get(i).size() == 0){
                            cloneCity.remove(i);
                        }
                        break;
                    }
                }
            }
//            for(List<City> cityList : cloneCity){
//                if(cityList.size() > 0){
//                    if(cityList.get(0).getId().equals(index)){
//                        cityList.remove(0);
//                        break;
//                    }
//                }
//            }
        }
//        for(int i = cloneCity.size() - 1; i >= 0; i--){
//            if(cloneCity.get(i).size() == 0){
//                cloneCity.remove(i);
//            }
//        }
        List<Integer> ret = new ArrayList<>();
        for(List<City> cityList : cloneCity){
            ret.add(cityList.get(0).getId());
        }
        return ret;
    }

    public void selectNextCity(double[][] pheromone) {
        double[] probability = new double[cityNum]; // 转移概率矩阵
        double sum = 0;

        // 计算分母
        for (int i : allowedCities) {
            sum += Math.pow(pheromone[currentCity][i], alpha) * Math.pow(eta[currentCity][i], beta);
        }
        List<Integer> nextDependent = getNextDependent();
        //&& nextDependent.contains(i)
        //nextDependent.get(0)
        // 计算概率矩阵
        for (int i = 0; i < cityNum; i++) {
            if (allowedCities.contains(i) && nextDependent.contains(i)) {
                probability[i] = (Math.pow(pheromone[currentCity][i], alpha) * Math.pow(eta[currentCity][i], beta)) / sum;
            } else {
                probability[i] = 0;
            }
        }

        // 选择下一个城市(权重随机数算法/轮盘赌)
        int selectCity = nextDependent.get(0);

             Random random = new Random();
        double rand = random.nextDouble();
        double sumPs = 0.0;
        for (int i = 0; i < cityNum; i++) {
            sumPs += probability[i];
            if (sumPs >= rand) {
                if(nextDependent.contains(i)) {
                    selectCity = i;
                    break;
                }
            }
        }
        allowedCities.remove(new Integer(selectCity));
//        allowedCities.remove(selectCity);
        // 从允许选择的城市中去掉选中的下一个城市
//        for (Integer i : allowedCities) {
//            if (i.intValue() == selectCity) {
//                allowedCities.remove(i);
//                break;
//            }
//        }

        // 在禁忌表中添加选中的下一个城市
        tabu.add(selectCity);

        // 将当前城市改为选中的下一个城市
        currentCity = selectCity;
    }
    public Boolean checkFirstAndSecond(Integer start, Integer end, List<Integer> tabu) {
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
    public Boolean checkFirstPickSecondDelivery(List<Integer> tabu){
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
    public City getCity(Integer index) {
        for(City city : this.shipmentCityList){
            if(city.getId().equals(index)){
                return city;
            }
        }
        return null;
    }
    public void updatePlanTime(List<Integer> tabu) {
        long currentTime = 0;
        for(int i = 0; i < tabu.size(); i++){
            if(i == 0) {
                currentTime = this.courierPoint.getStart().getTime().getTime();

            } else {
                long time = (long)(distance[tabu.get(i-1)][tabu.get(i)] / (courierPoint.getSpeed().divide(new BigDecimal(3.6), 2, RoundingMode.DOWN).longValue()));

                City city = getCity(tabu.get(i));
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
    public void updatePlanTime() {
        long currentTime = 0;
        for(int i = 0; i < tabu.size(); i++){
            if(i == 0) {
                currentTime = this.courierPoint.getStart().getTime().getTime();

            } else {
                long time = (long)(distance[tabu.get(i-1)][tabu.get(i)] / (courierPoint.getSpeed().divide(new BigDecimal(3.6), 2, RoundingMode.DOWN).longValue()));

                City city = getCity(tabu.get(i));
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
        updateEndPlanTime();
    }
    public Boolean checkEffectiveness(List<Integer> tabu) {
        updatePlanTime(tabu);
        for(City city : this.shipmentCityList) {
            if(city.getId() == 0) {

            } else if(city.getStart().getTime() <= city.getPlan().getTime() &&
               city.getPlan().getTime() <= city.getEnd().getTime()) {

            } else {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
    public void updateEndPlanTime() {
        City city = getCity(tabu.get(tabu.size()-1));
        double len = Utils.distanceSimplify(city.getLat().doubleValue(), city.getLng().doubleValue(), endCity.getLat().doubleValue(), endCity.getLng().doubleValue());
        long time = (long)(len / (courierPoint.getSpeed().divide(new BigDecimal(3.6), 2, RoundingMode.DOWN).longValue()));
        time = city.getPlan().getTime() + time * 1000;
        endCity.setPlan(new Date(time));
    }
    public Boolean checkEnd() {
        return Boolean.TRUE;
//        updateEndPlanTime();
//        if(endCity.getPlan().getTime() <= endCity.getEnd().getTime()) {
//            return Boolean.TRUE;
//        } else {
//            return Boolean.FALSE;
//        }
    }
    public Boolean checkUnit(){
        Integer unit = this.courierPoint.getUnitlimit();
//        for(Integer index: tabu) {
//            if(index != 0) { //过滤出发点
//                City city = getCity(index);
//                if(unit - city.getUnit() < 0) {
//                    return Boolean.FALSE;
//                }
//            }
//        }
        return Boolean.TRUE;
    }
    /**
     * 计算路径长度
     * @return
     */
    public int calculateTourLength() {
        int length = 0;
        if(checkFirstPickSecondDelivery(tabu) == Boolean.FALSE ||
           checkEffectiveness(tabu)== Boolean.FALSE ||
           checkEnd() == Boolean.FALSE ||
           checkUnit() == Boolean.FALSE) {
            length = Integer.MAX_VALUE;
        } else {
            for (int i = 0; i < cityNum - 1; i++) {
                length += distance[tabu.get(i)][tabu.get(i + 1)];
            }

            City city = getCity(tabu.get(tabu.size()-1));

            int homelen = (int) Utils.distanceSimplify(city.getLat().doubleValue(), city.getLng().doubleValue(), endCity.getLat().doubleValue(), endCity.getLng().doubleValue());

//            System.out.println("homelen ="+homelen);
            length += homelen;
        }
        return length;
    }
    /**
     * 计算路径长度
     * @return
     */
    private int calculateBatchTourLength() {
        int length = 0;
        if(checkFirstPickSecondDelivery(tabu) == Boolean.FALSE ||
                checkEffectiveness(tabu)== Boolean.FALSE ||
                checkEnd() == Boolean.FALSE ||
                checkUnit() == Boolean.FALSE) {
            length += 100 * 1000;
        } else {
            for (int i = 0; i < cityNum - 1; i++) {
                length += distance[tabu.get(i)][tabu.get(i + 1)];
            }
            City city = getCity(tabu.get(tabu.size()-1));
            length += Utils.distanceSimplify(city.getLat().doubleValue(), city.getLng().doubleValue(), endCity.getLat().doubleValue(), endCity.getLng().doubleValue());

        }
        return length;
    }

    public List<Integer> getAllowedCities() {
        return allowedCities;
    }

    public void setAllowedCities(List<Integer> allowedCities) {
        this.allowedCities = allowedCities;
    }

    public int getTourLength() {
        if(isBatchDispatch == Boolean.TRUE) {
            tourLength = calculateBatchTourLength();
        } else {
            tourLength = calculateTourLength();
        }

        return tourLength;
    }

    public void setTourLength(int tourLength) {
        this.tourLength = tourLength;
    }

    public int getCityNum() {
        return cityNum;
    }

    public void setCityNum(int cityNum) {
        this.cityNum = cityNum;
    }

    public ArrayList<Integer> getTabu() {
        return tabu;
    }

    public void setTabu(ArrayList<Integer> tabu) {
        this.tabu = tabu;
    }

    public double[][] getDelta() {
        return delta;
    }

    public void setDelta(double[][] delta) {
        this.delta = delta;
    }

    public int getFirstCity() {
        return firstCity;
    }

    public void setFirstCity(int firstCity) {
        this.firstCity = firstCity;
    }

//    public Boolean checkEffectiveness(CourierPoint courierPoint, City current, City next) {
//        if(current==null) {
//            return Boolean.TRUE;
//        }
//        double len = Utils.distanceSimplify(current.getLat().doubleValue(), current.getLng().doubleValue(), next.getLat().doubleValue(), next.getLng().doubleValue());
//        BigDecimal time = new BigDecimal(len).divide(courierPoint.getSpeed().divide(new BigDecimal(3.6),2, BigDecimal.ROUND_DOWN), 2, BigDecimal.ROUND_DOWN);
//        if(current.getPlan() == null){
//            System.out.println("1");
//        }
//        Long nextPlanTime = current.getPlan().getTime() +  time.longValue();
//        if(next.getStart().getTime() < nextPlanTime && nextPlanTime < next.getEnd().getTime()) {
//            return Boolean.TRUE;
//        } else {
//            return Boolean.FALSE;
//        }
//    }
//    public void updatePlan(CourierPoint courierPoint, City current, City next) {
//        if(current == null) {
//            next.setPlan(courierPoint.getStart().getTime());
//            return;
//        }
//        double len = Utils.distanceSimplify(current.getLat().doubleValue(), current.getLng().doubleValue(), next.getLat().doubleValue(), next.getLng().doubleValue());
//        BigDecimal time = new BigDecimal(len).divide(courierPoint.getSpeed(),2, BigDecimal.ROUND_DOWN);
//        Long nextPlanTime = current.getPlan().getTime() +  time.longValue();
//        next.setPlan(new Date(nextPlanTime));
//    }
//    public List<Integer> getAllowedCityIndex(CourierPoint courierPoint, City current) {
//        List<Integer> cityIndexList = new ArrayList<>();
//        for(List<City> cities:shipmentCityList){
//            if(cities.size() > 0) {
//                if(checkEffectiveness(courierPoint, current, cities.get(0))) {
//                    cityIndexList.add(cities.get(0).getId());
//                }
//            }
//        }
//        return cityIndexList;
//    }
//    public City findCity(int cityIndex){
//        for(List<City> cities:shipmentCityList){
//            if(cities.size() > 0) {
//                if(cities.get(0).getId().equals(cityIndex)){
//                    return cities.get(0);
//                }
//            }
//        }
//        return null;
//    }
//    public City updateAllowedCityList(int selectCity) {
//        City ret = null;
//        if(shipmentCityList.size() > 0) {
//            for(List<City> cities:shipmentCityList){
//                if(cities.size() > 0) {
//                    if(cities.get(0).getId().equals(selectCity)){
//                        ret = cities.get(0);
//                        updatePlan(this.courierPoint, this.current, ret);
//                        cities.remove(0);
//                    }
//                }
//                if(cities.size() == 0) {
//                    shipmentCityList.remove(cities);
//                    break;
//                }
//            }
//        }
//        return ret;
//    }
}
