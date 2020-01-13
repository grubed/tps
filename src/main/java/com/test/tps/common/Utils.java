package com.test.tps.common;

import java.io.*;
import java.util.List;

import static java.lang.Math.toRadians;

public class Utils {
    public static double distanceSimplify(double lat1, double lng1, double lat2, double lng2) {
        double dx = lng1 - lng2; // 经度差值
        double dy = lat1 - lat2; // 纬度差值
        double b = (lat1 + lat2) / 2.0; // 平均纬度
        double Lx = toRadians(dx) * 6367000.0* Math.cos(toRadians(b)); // 东西距离
        double Ly = 6367000.0 * toRadians(dy); // 南北距离
        return Math.sqrt(Lx * Lx + Ly * Ly);  // 用平面的矩形对角距离公式计算总距离
    }
    public static <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        List<T> dest = (List<T>) in.readObject();
        return dest;
    }
}
