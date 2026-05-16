package com.sky.test;


import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HttpClientTest {

    @Test
    public void testGET() throws Exception{
        //创建httpclient对象
        CloseableHttpClient httpClient=HttpClients.createDefault();
        //创建get请求对象
        HttpGet httpGet=new HttpGet("http://localhost:8080/user/shop/status");
        //发送请求
        CloseableHttpResponse response=httpClient.execute(httpGet);

        //获取服务端响应状态码
        int statusCode=response.getStatusLine().getStatusCode();
        System.out.println("响应状态码："+statusCode);

        HttpEntity entity=response.getEntity();
        String body= EntityUtils.toString(entity);
        System.out.println("响应体："+body);

        //关闭资源
        response.close();
        httpClient.close();
    }




    @Test
    public  void testPOST() throws Exception {
        {

            //创建httpclient对象
            CloseableHttpClient httpClient = HttpClients.createDefault();
            //创建post请求对象
            HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");


            JSONObject json = new JSONObject();
            json.put("username", "admin");
            json.put("password", "123456");

            StringEntity entity = new StringEntity(json.toString(), "utf-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

            //发送请求
            CloseableHttpResponse response = httpClient.execute(httpPost);
            //解析响应
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("响应状态码：" + statusCode);

            HttpEntity responseEntity = response.getEntity();
            String body = EntityUtils.toString(responseEntity);
            System.out.println("响应体：" + body);


            //关闭资源
            response.close();
            httpClient.close();

        }


    }


}
