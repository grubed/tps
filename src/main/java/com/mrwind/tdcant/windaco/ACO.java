package com.mrwind.tdcant.windaco;
/**
 * Created by houseyoung on 16/5/11 19:47.
 */
import com.mrwind.tdcant.common.Utils;
import com.mrwind.tdcant.dto.City;


import com.mrwind.tdcant.dto.CourierPoint;
import com.mrwind.tdcant.dto.ShipmentPoint;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Data
public class ACO {
    private Ant[] ants; // 蚂蚁
    private int cityNum; // 城市数量
    private List<City> shipmentCityList;
    private List<List<City>> dependentCity;
//    private City startCity;
    private City endCity;
    private CourierPoint courierPoint;
//    private double[] x; // X坐标矩阵
//    private double[] y; // Y坐标矩阵
    private double[][] distance; // 距离矩阵
    private double[][] pheromone; // 信息素矩阵
    private int oldLength; // 旧长度
    private int bestLength; // 最佳长度
    private int[] bestTour; // 最佳路径
    private Ant bestAnt; // 最佳路径
    private int antNum; // 蚂蚁数量
    private int generation; // 迭代次数
    private double alpha; // 信息素重要程度系数
    private double beta; // 城市间距离重要程度系数
    private double rho; // 信息素残留系数
    private int Q; // 蚂蚁循环一周在经过的路径上所释放的信息素总量
    private int deltaType; // 信息素更新方式模型，0: Ant-quantity; 1: Ant-density; 2: Ant-cycle

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 构造方法
     * @param cityNum
     * @param generation
     * @param alpha
     * @param beta
     * @param rho
     * @param Q
     */
    public ACO(int cityNum, int generation, double alpha, double beta, double rho, int Q, int deltaType) {
        this.cityNum = cityNum;
        this.antNum = (cityNum / 3) * 2;
        this.generation = generation;
        this.alpha = alpha;
        this.beta = beta;
        this.rho = rho;
        this.Q = Q;
        this.deltaType = deltaType;

        ants = new Ant[antNum];
    }

//    public List<City> shipmentCityList2CityList(List<List<City>> shipmentCityList) {
//        List<City> cityList = new ArrayList<>();
//        for(List<City> cityList1 : shipmentCityList){
//            cityList.addAll(cityList1);
//        }
//        return cityList;
//    }

//    /**
//     * 初始化
//     * @param cityList
//     */
//    public void init(City startCity, List<List<City>> cityList, City endCity, CourierPoint courierPoint) {
////        this.courierPoint = courierPoint;
////        this.startCity = startCity;
////        this.shipmentCityList = cityList;
////        this.endCity = endCity;
//        // 从文件中获取X坐标矩阵、Y坐标矩阵
////        x = ReadFile.getX(cityNum, filename);
////        y = ReadFile.getY(cityNum, filename);
////
////        // 计算距离矩阵
//        List<City> cities = new ArrayList<>();
//        cities.add(startCity);
//        cities.addAll(shipmentCityList2CityList(cityList));
//        getDistance(cities);
//
//        // 初始化信息素矩阵
//        pheromone = new double[cityNum][cityNum];
//        double start = 1.0 / ((cityNum - 1) * antNum); // 计算初始信息素数值
//        for (int i = 0; i < cityNum; i++) {
//            for (int j = 0; j < cityNum; j++) {
//                pheromone[i][j] = start;
//            }
//        }
//
//        // 初始化最佳长度及最佳路径
//        bestLength = Integer.MAX_VALUE;
//        bestTour = new int[cityNum + 1];
//
//        // 初始化antNum个蚂蚁
//        for (int i = 0; i < antNum; i++) {
//            ants[i] = new Ant(cityNum);
//            ants[i].init(clone(cityList), courierPoint, startCity, distance, alpha, beta);
//        }
//    }

    public void init(City startCity, List<City> cityList, City endCity, CourierPoint courierPoint, List<List<City>> shipmentCityList) {
        this.dependentCity = shipmentCityList;
        List<City> cities = new ArrayList<>();
        cities.add(startCity);
        cities.addAll(cityList);
        this.shipmentCityList = clone(cities);
        this.endCity = endCity;
        this.courierPoint = courierPoint;
        getDistance(cities);

        // 初始化信息素矩阵
        pheromone = new double[cityNum][cityNum];
        double start = 1.0 / ((cityNum - 1) * antNum); // 计算初始信息素数值
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                pheromone[i][j] = start;
            }
        }

        // 初始化最佳长度及最佳路径
        bestLength = Integer.MAX_VALUE;
        bestTour = new int[cityNum];

        // 初始化antNum个蚂蚁
        for (int i = 0; i < antNum; i++) {
            ants[i] = new Ant(cityNum);
            ants[i].init(clone(this.shipmentCityList), courierPoint, endCity, this.dependentCity, distance, alpha, beta);
        }
    }

//    public List<List<City>> clone(List<List<City>> shipmentCityList) {
//        List<List<City>> ret = new ArrayList<>();
//        for(List<City> cities : shipmentCityList) {
//            List<City> cities1 = new ArrayList<>();
//            for(City city : cities){
//                City city1 = new City();
//                try {
//                    BeanUtils.copyProperties(city1, city);
//                    cities1.add(city1);
//                } catch (Exception e) {
//
//                }
//            }
//            ret.add(cities1);
//        }
//        return ret;
//    }
    public List<City> clone(List<City> shipmentCityList) {
        List<City> ret = new ArrayList<>();

        for(City city : shipmentCityList){
            City city1 = new City();
            try {
                BeanUtils.copyProperties(city1, city);
                ret.add(city1);
            } catch (Exception e) {

            }
        }

        return ret;
    }
    public Ant cloneAnt(Ant ant) {
        Ant ret = new Ant(ant.getCityNum());
        try {
            BeanUtils.copyProperties(ret, ant);
        } catch (Exception e) {

        }
        return ret;
    }
    /**
     * 计算距离矩阵
     * @param cityList
     */
    private void getDistance (List<City> cityList) {

        // 计算距离矩阵
        this.cityNum = cityList.size();
        distance = new double[cityNum][cityNum];
        for (int i = 0; i < cityNum - 1; i++) {
            distance[i][i] = 0; // 对角线为0
            for (int j = i + 1; j < cityNum; j++) {
                double len = Utils.distanceSimplify(cityList.get(i).getLat().doubleValue(), cityList.get(i).getLng().doubleValue(), cityList.get(j).getLat().doubleValue(), cityList.get(j).getLng().doubleValue());
                //距离误差100米，小于100米都是同一个点，distance不能为0
                if(len < 100) {
                    distance[i][j] = 100;
                    distance[j][i] = distance[i][j];
                } else {
                    distance[i][j] = len;
                    distance[j][i] = distance[i][j];
                }

            }
        }
        distance[cityNum - 1][cityNum - 1] = 0;
    }
//    private void getDistance (double[] x, double[] y) throws IOException {
//        // 计算距离矩阵
//        distance = new double[cityNum][cityNum];
//        for (int i = 0; i < cityNum - 1; i++) {
//            distance[i][i] = 0; // 对角线为0
//            for (int j = i + 1; j < cityNum; j++) {
//                distance[i][j] = Math.sqrt(((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j]) * (y[i] - y[j])) / 10.0);
//                distance[j][i] = distance[i][j];
//            }
//        }
//        distance[cityNum - 1][cityNum - 1] = 0;
//    }
    public void greedyTour() {
        bestLength = Greedy.greedyTour(bestTour, bestLength, distance, shipmentCityList, endCity, courierPoint);
        System.out.println("最佳长度: " + bestLength);
        System.out.print("最佳路径: ");
        for (int i = 0; i < cityNum - 1; i++) {
            System.out.print(bestTour[i] + 1 + "-");
        }
        System.out.println(bestTour[cityNum - 1] + 1);
    }
    public void iterator() {
        for (int g = 0; g < generation; g++) {//一次迭代即更新了一次解空间
            System.out.println("第" + g + "次迭代：");
            // 重新初始化蚂蚁
            for (int i = 0; i < antNum; i++) {
                ants[i].init(clone(this.shipmentCityList), this.courierPoint, endCity, this.dependentCity,distance, alpha, beta);
            }
            solve();
//            findBestRoad();
        }
    }
    public void findBestRoad(){
        int bestAntIndex = -1;
        for (int ant = 0; ant < antNum; ant++) {
            if (ants[ant].getTourLength() <= this.bestLength) {
                this.bestLength = ants[ant].getTourLength();
                bestAnt = cloneAnt(ants[ant]);
                bestAntIndex = ant;
            }
        }
        if(bestAntIndex != -1) {
            for (int k = 0; k < cityNum; k++) {
                bestTour[k] = ants[bestAntIndex].getTabu().get(k).intValue();
            }
        }
    }

    /**
     * 解决TSP问题
     */
    public void solve() {
        // 迭代generation次
//        for (int g = 0; g < generation; g++) {

            // 对antNum只蚂蚁分别进行操作
            long startTime = System.currentTimeMillis();
            for (int ant = 0; ant < antNum; ant++) {
                // 为每只蚂蚁分别选择一条路径
                for (int i = 1; i < cityNum; i++) {
                    ants[ant].selectNextCity(pheromone);
                }

                // 把这只蚂蚁起始城市再次加入其禁忌表中，使禁忌表中的城市最终形成一个循环
                //ants[ant].getTabu().add(ants[ant].getFirstCity());

                // 若这只蚂蚁走过所有路径的距离比当前的最佳距离小，则覆盖最佳距离及最佳路径
                if (ants[ant].getTourLength() <= bestLength) {
                    bestLength = ants[ant].getTourLength();
                    bestAnt = cloneAnt(ants[ant]);
                    bestTour = ants[ant].getTabu().stream().mapToInt(Integer::valueOf).toArray();
//                    for (int k = 0; k < cityNum; k++) {
//                        bestTour[k] = ants[ant].getTabu().get(k).intValue();
//
//                    }
                }

                // 更新这只蚂蚁信息素增量矩阵
                double[][] delta = ants[ant].getDelta();
                for(int i=0;i<ants[ant].getTabu().size()-1;i++){
                    if (deltaType == 0) {
                        delta[i][i+1] = Q; // Ant-quantity System
                    }
                    if (deltaType == 1) {
                        delta[i][i+1] = Q / distance[i][i+1]; // Ant-density System
                    }
                    if (deltaType == 2) {
                        delta[i][i+1] = Q / ants[ant].getTourLength(); // Ant-cycle System
                    }
                }
//
//                for (int i = 0; i < cityNum; i++) {
//
//                    for (int j : ants[ant].getTabu()) {
//                        if (deltaType == 0) {
//                            delta[i][j] = Q; // Ant-quantity System
//                        }
//                        if (deltaType == 1) {
//                            delta[i][j] = Q / distance[i][j]; // Ant-density System
//                        }
//                        if (deltaType == 2) {
//                            delta[i][j] = Q / ants[ant].getTourLength(); // Ant-cycle System
//                        }
//                    }
//                }
                ants[ant].setDelta(delta);
            }
            long endTime = System.currentTimeMillis();
            System.out.println((endTime - startTime) / 1000F + "秒");
            // 更新信息素
            updatePheromone();

//            // 重新初始化蚂蚁
//            for (int i = 0; i < antNum; i++) {
//                ants[i].init(clone(this.shipmentCityList), this.courierPoint, endCity, this.dependentCity,distance, alpha, beta);
//            }
//        }

        // 打印最佳结果
        print();
    }

    /**
     * 更新信息素
     */
    private void updatePheromone() {
        // 按照rho系数保留原有信息素
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                pheromone[i][j] = pheromone[i][j] * rho;
            }
        }

        // 按照蚂蚁留下的信息素增量矩阵更新信息素
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                for (int ant = 0; ant < antNum; ant++) {
                    pheromone[i][j] += ants[ant].getDelta()[i][j];
                }
            }
        }
    }

    /**
     * 在控制台中输出最佳长度及最佳路径
     */
    private void print() {
        System.out.println("最佳长度: " + bestLength);
        System.out.print("最佳路径: ");
        for (int i = 0; i < cityNum - 1; i++) {
            System.out.print(bestTour[i] + 1 + "-");
        }
        System.out.println(bestTour[cityNum - 1] + 1);
    }

    /**
     * 输出最佳路径
     * @return
     */
    public int[] getBestTour() {
        return bestTour;
    }

    /**
     * 输出最佳长度
     * @return
     */
    public int getBestLength() {
        return bestLength;
    }

//    /**
//     * 输出X坐标矩阵
//     * @return
//     */
//    public double[] getX() {
//        return x;
//    }
//
//    /**
//     * 输出Y坐标矩阵
//     * @return
//     */
//    public double[] getY() {
//        return y;
//    }
}
