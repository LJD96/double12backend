package com.ljd.kafka;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Liu JianDong
 * @create 2023-02-28-13:31
 **/
public class Extract10000 {
    private static final String SOURCE_PATH = "E:\\myResource\\斑马项目\\淘宝双十二大促用户购物行为数据可视化分析作业\\user_action.csv";
    private static final String SINK_PATH = "E:\\myResource\\斑马项目\\淘宝双十二大促用户购物行为数据可视化分析作业\\user_action_10000.csv";

    private static final int NUM = 10000;

    public static void main(String[] args) {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(SOURCE_PATH))));BufferedWriter bw = new BufferedWriter(new FileWriter(SINK_PATH));){
            int count = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                if (count > NUM) {
                    break;
                } else if (count == 0) {
                    count++;
                    continue;
                }
                count++;
                bw.write(line + "\r\n");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
