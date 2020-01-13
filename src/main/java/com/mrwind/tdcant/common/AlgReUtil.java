package com.mrwind.tdcant.common;

import java.text.DecimalFormat;

public class AlgReUtil {
    /**
     * 地球半径
     */
    private static final double EARTH_RADIUS = 6378.137;

    /**
     * @param distance 单位：米
     */
    public static double getDegreeByDistance(double distance) {
        double r = 6371.004 * 1000;
        double degree = distance * 180 / (Math.PI * r);
        return degree;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 返回两点之间距离，单位：m,保留三维小数
     */
    public static double getDistance(double lat1, double lng1, double lat2,
                                     double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        Double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        DecimalFormat df = new DecimalFormat("0.000");
        s = Double.parseDouble(df.format((double) Math.round(s * 1000)));

        return s;
    }

    /**
     * 放大系数 1.414
     * 返回两点之间距离，单位：km,保留三维小数
     */
    public static double getKmDistance(double lat1, double lng1, double lat2, double lng2) {
        double distance = getDistance(lat1, lng1, lat2, lng2);
        DecimalFormat df = new DecimalFormat("0.000");
        return Double.parseDouble(df.format(1.414 * distance / 1000));
    }


}

