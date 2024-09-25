package apiCustomerManagement.common;

import io.restassured.RestAssured;

import static apiCustomerManagement.common.ConstantUtils.BASE_URL;
import static apiCustomerManagement.common.ConstantUtils.PORT;

public class RestAssuredSetUp {
    public static void setUp(){
        RestAssured.baseURI = BASE_URL;
        RestAssured.port = PORT;
    }
}
