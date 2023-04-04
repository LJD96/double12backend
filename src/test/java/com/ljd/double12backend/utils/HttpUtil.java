package com.ljd.double12backend.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @author Liu JianDong
 * @create 2023-03-24-15:30
 **/
public class HttpUtil {

    public static String getCategory(Integer categoryId, String session) throws IOException {
        String url = "http://10.120.21.177:18080/dam/service/models/category/";
        url = url + categoryId;

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpGet = new HttpGet(url);

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build();
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("username", "admin");
        httpGet.setHeader("Referer", "http://10.120.21.177:18080/");
        httpGet.setHeader("Host", "10.120.21.177:18080");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36");
        httpGet.setHeader("Cookie", session);


        HttpResponse response = httpClient.execute(httpGet);

        String result = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (200 == statusCode) {
            result = EntityUtils.toString(response.getEntity());
        } else {
            // logger.info("请求第三方接口出现错误，状态码为:{}", statusCode);
            return null;
        }
//        6、释放资源
        httpGet.abort();
        httpClient.getConnectionManager().shutdown();
        return result;
    }

    public static String getColumn(String tableId, String session) throws IOException {
        String url = "http://10.120.21.177:18080/dam/service/entities/" + tableId +"/columns";
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpGet = new HttpGet(url);

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build();
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("username", "admin");
        httpGet.setHeader("Referer", "http://10.120.21.177:18080/");
        httpGet.setHeader("Host", "10.120.21.177:18080");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36");
        httpGet.setHeader("Cookie", session);

        HttpResponse response = httpClient.execute(httpGet);

        String result = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (200 == statusCode) {
            result = EntityUtils.toString(response.getEntity());
        } else {
            // logger.info("请求第三方接口出现错误，状态码为:{}", statusCode);
            return null;
        }
//        6、释放资源
        httpGet.abort();
        httpClient.getConnectionManager().shutdown();
        return result;
    }


    public static String postModel(String modelId, int page, String session) throws IOException {
        String url = "http://10.120.21.177:18080/dam/service/entities/search";

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(url);

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Connection", "keep-alive");
        httpPost.setHeader("username", "admin");
        httpPost.setHeader("Referer", "http://10.120.21.177:18080/");
        httpPost.setHeader("Host", "10.120.21.177:18080");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36");
        httpPost.setHeader("Cookie", session);
        httpPost.setHeader("Content-Type", "application/json");

        String playLoad = "{\"currentPage\":" + page +",\"keyword\":\"\",\"pageSize\":20,\"modelId\":"  + modelId + ",\"types\":[\"TABLE\",\"VIEW\",\"STORED_PROCEDURE\",\"FUNCTION\"],\"tagIds\":null,\"sortByName\":null,\"sortByCreateTime\":null}\n";
        httpPost.setEntity(new StringEntity(playLoad, "UTF-8"));
        HttpResponse response = httpClient.execute(httpPost);

        String result = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (200 == statusCode) {
            result = EntityUtils.toString(response.getEntity());
        } else {
            // logger.info("请求第三方接口出现错误，状态码为:{}", statusCode);
            return null;
        }
//        6、释放资源
        httpPost.abort();
        httpClient.getConnectionManager().shutdown();
        return result;
    }

    public static String postOrder(String url) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build();
        String playLoad = "{\n" +
                "  \"name\": \"张三\",\n" +
                "  \"phone\": \"17825421269\",\n" +
                "  \"orderInfo\":\n" +
                "  [\n" +
                "    {\n" +
                "      \"name\":\"卫生纸\",\n" +
                "      \"num\": 1,\n" +
                "      \"id\": 1\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\":\"垃圾袋\",\n" +
                "      \"num\": 1,\n" +
                "      \"id\": 2\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        httpPost.setEntity(new StringEntity(playLoad, "UTF-8"));
        HttpResponse response = httpClient.execute(httpPost);

        String result = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (200 == statusCode) {
            result = EntityUtils.toString(response.getEntity());
        } else {
            // logger.info("请求第三方接口出现错误，状态码为:{}", statusCode);
            return null;
        }
//        6、释放资源
        httpPost.abort();
        httpClient.getConnectionManager().shutdown();
        return result;
    }

}
