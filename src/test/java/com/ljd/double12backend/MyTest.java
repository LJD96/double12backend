package com.ljd.double12backend;

import com.alibaba.fastjson.JSONArray;
import com.ljd.double12backend.utils.HttpUtil;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Liu JianDong
 * @create 2023-03-24-16:53
 **/
public class MyTest {
    @Test
    public void test01(){
        // System.out.println(1 % 20);
        System.out.println( new Random().nextDouble() > 0.5);
    }


    @Test
    public void test02() throws ExecutionException, InterruptedException {
        String url = "http://localhost:9091/order/add/row/lock";
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            results.add(executor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return HttpUtil.postOrder(url);
                }
            }));
        }
        for (Future<String> result : results) {
            result.get();
        }
    }

    @Test
    public void test03() throws ExecutionException, InterruptedException {
        // String url1 = "http://localhost:9091/order/add/row/lock";
        // String url2 = "http://localhost:9092/order/add/row/lock";

        String url1 = "http://localhost:9091/order/add";
        String url2 = "http://localhost:9092/order/add";

        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            results.add(executor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String url;
                    url = new Random().nextDouble() > 0.5 ? url1 : url2;
                    return HttpUtil.postOrder(url);
                }
            }));
        }

        for (Future<String> result : results) {
            result.get();
        }
    }

    @Test
    public void test04(){
        String positionStr = "$.contactList[125]";
        System.out.println(positionStr.substring(positionStr.indexOf('[') + 1, positionStr.indexOf(']')));
    }

    @Test
    public void test05() throws ExecutionException, InterruptedException {
        String url1 = "http://localhost:9091/order/add/redis/lock";
        String url2 = "http://localhost:9092/order/add/redis/lock";

        // String url1 = "http://localhost:9091/order/add";
        // String url2 = "http://localhost:9092/order/add";

        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            results.add(executor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String url;
                    url = new Random().nextDouble() > 0.5 ? url1 : url2;
                    return HttpUtil.postOrder(url);
                }
            }));
        }

        for (Future<String> result : results) {
            result.get();
        }
    }

    @Test
    public void testReadJson() throws FileNotFoundException {
        String path = "E:\\Note System\\obsidian\\主仓库\\项目\\斑马阶段\\数据治理\\fromre.json";
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        String jsonString = stringBuilder.toString();
        if (jsonString != null) {
            JSONArray jsonArray = JSONArray.parseArray(jsonString);
            int totalItems = jsonArray.size();
            System.out.println("Total items in the JSON array: " + totalItems);
        } else {
            System.out.println("Could not read the JSON file.");
        }
    }
}
