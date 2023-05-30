package org.anyline.entity;

import org.anyline.util.NumberUtil;

import java.util.ArrayList;
import java.util.List;

public class Line {
    private List<Point> points = new ArrayList<>();
    public Line(byte[] bytes) {
        // MySQL Linestring 的格式是 WKB (Well-Known Binary) 格式
        // 在 WKB 格式中，前四个字节表示几何类型，接下来的字节表示坐标值

        // 检查字节数组是否为空
        if (bytes == null || bytes.length == 0) {
            return; // 返回空数组
        }

        // 检查几何类型是否为 Linestring
        int geometryType = bytes[0] & 0xFF;
        if (geometryType != 0x02) {
            throw new IllegalArgumentException("Not a Linestring");
        }

        // 提取坐标数量
        int numPoints = NumberUtil.byte2int(bytes, 1);

        // 检查字节数组长度是否与坐标数量匹配
        int expectedLength = 1 + 4 + (numPoints * 16); // 1 字节几何类型 + 4 字节坐标数量 + 每个坐标点 16 字节
        if (bytes.length != expectedLength) {
            throw new IllegalArgumentException("Invalid byte array length");
        }

        // 解析坐标值
        double[] coordinates = new double[numPoints * 2];
        int offset = 5; // 跳过几何类型和坐标数量的字节

        for (int i = 0; i < numPoints; i++) {
            double x = NumberUtil.byte2double(bytes, offset);
            double y = NumberUtil.byte2double(bytes, offset + 8);
            Point point = new Point(x, y);
            points.add(point);
            offset += 16; // 每个坐标点 16 字节
        }
    }
}

