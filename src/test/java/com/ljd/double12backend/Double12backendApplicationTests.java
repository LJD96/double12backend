package com.ljd.double12backend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ljd.double12backend.utils.HttpUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class Double12backendApplicationTests {

    @Test
    void contextLoads() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet sheet = workbook.createSheet("Sheet1");
        int rowNum = 0;
        XSSFRow header = sheet.createRow(rowNum++);
        header.createCell(0).setCellValue("系统名");
        header.createCell(1).setCellValue("数据库名");
        header.createCell(2).setCellValue("用户名");
        header.createCell(3).setCellValue("表名");
        header.createCell(4).setCellValue("字段名");
        header.createCell(5).setCellValue("字段注释");
        header.createCell(6).setCellValue("是否有中文注释");


        Map<String, Integer> target_category = new HashMap<>();
        target_category.put("钉钉服务平台(钉钉服务平台)", 55);
        target_category.put("员工成长关心卡(员工成长关心卡)", 53);
        target_category.put("财务共享平台(财务共享平台)", 45);
        target_category.put("财务基台(财务基台)", 18);

        String session = "SESSION=899faf37-44bb-4886-86ba-e45e62724658";

        for (String systemName : target_category.keySet()) {
            String category = HttpUtil.getCategory(target_category.get(systemName), session);
            //系统名

            JSONArray categoryList = JSON.parseArray(category);
            for (Object item : categoryList) {
                JSONObject categoryJSONObject = (JSONObject) item;
                //数据库名
                String categoryName = categoryJSONObject.getString("definition");

                String modelId = categoryJSONObject.getString("modelId");
                String tableListStr = HttpUtil.postModel(modelId, 1, session);
                JSONObject tableList = JSON.parseObject(tableListStr);
                Integer totalItems = tableList.getInteger("totalItems");
                if (totalItems <= 20) {
                    JSONArray content = tableList.getJSONArray("content");
                    for (Object t : content) {
                        JSONObject table = (JSONObject) t;
                        String tableId = table.getString("objectId");
                        //用户名
                        String schema = table.getString("schema");
                        //表名
                        String physicalTableName = table.getString("physicalName");
                        String column = HttpUtil.getColumn(tableId, session);
                        JSONArray columnArray = JSON.parseArray(column);
                        for (Object o : columnArray) {
                            JSONObject columnItem = (JSONObject) o;
                            String columnPhysicalName = columnItem.getString("physicalName");
                            String columnLogicalName = columnItem.getString("logicalName");

                            if (columnLogicalName != null) {
                                if (containsChinese(columnLogicalName)) {
                                    //nothing to do
                                }else{
                                    //没有中文，需要记录
                                    XSSFRow row = sheet.createRow(rowNum++);
                                    row.createCell(0).setCellValue(systemName);
                                    row.createCell(1).setCellValue(categoryName);
                                    row.createCell(2).setCellValue(schema);
                                    row.createCell(3).setCellValue(physicalTableName);
                                    row.createCell(4).setCellValue(columnPhysicalName);
                                    row.createCell(5).setCellValue(columnLogicalName);
                                    row.createCell(6).setCellValue("False");
                                }

                            }else{
                                //没有中文需要记录
                                XSSFRow row = sheet.createRow(rowNum++);
                                row.createCell(0).setCellValue(systemName);
                                row.createCell(1).setCellValue(categoryName);
                                row.createCell(2).setCellValue(schema);
                                row.createCell(3).setCellValue(physicalTableName);
                                row.createCell(4).setCellValue(columnPhysicalName);
                                row.createCell(5).setCellValue(columnLogicalName);
                                row.createCell(6).setCellValue("False");
                            }
                        }
                    }
                }else {
                    int totalPage;
                    if(totalItems % 20 == 0){
                        totalPage = totalItems / 20;
                    }else {
                        totalPage = totalItems /20;
                        totalPage++;
                    }
                    for (int i = 1; i < totalPage; i++) {
                        tableListStr = HttpUtil.postModel(modelId, i + 1, session);
                        JSONArray content = tableList.getJSONArray("content");
                        for (Object t : content) {
                            JSONObject table = (JSONObject) t;
                            String schema = table.getString("schema");
                            String tableId = table.getString("objectId");
                            String column = HttpUtil.getColumn(tableId, session);
                            String physicalTableName = table.getString("physicalName");
                            JSONArray columns = JSON.parseArray(column);
                            for (Object c : columns) {
                                JSONObject columnItem = (JSONObject) c;
                                String columnPhysicalName = columnItem.getString("physicalName");
                                String columnLogicalName = columnItem.getString("logicalName");
                                if (columnLogicalName != null) {
                                    if (containsChinese(columnLogicalName)) {
                                        //nothing to do
                                    }else{
                                        XSSFRow row = sheet.createRow(rowNum++);
                                        row.createCell(0).setCellValue(systemName);
                                        row.createCell(1).setCellValue(categoryName);
                                        row.createCell(2).setCellValue(schema);
                                        row.createCell(3).setCellValue(physicalTableName);
                                        row.createCell(4).setCellValue(columnPhysicalName);
                                        row.createCell(5).setCellValue(columnLogicalName);
                                        row.createCell(6).setCellValue("False");
                                    }

                                }else{
                                    XSSFRow row = sheet.createRow(rowNum++);
                                    row.createCell(0).setCellValue(systemName);
                                    row.createCell(1).setCellValue(categoryName);
                                    row.createCell(2).setCellValue(schema);
                                    row.createCell(3).setCellValue(physicalTableName);
                                    row.createCell(4).setCellValue(columnPhysicalName);
                                    row.createCell(5).setCellValue(columnLogicalName);
                                    row.createCell(6).setCellValue("False");
                                }
                            }
                        }
                    }
                }
            }

            FileOutputStream outputStream = new FileOutputStream("results.xlsx");
            workbook.write(outputStream);
            outputStream.close();

        }

    }

    public static boolean containsChinese(String s) {
        String regex = "[\u4e00-\u9fa5]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }

}
