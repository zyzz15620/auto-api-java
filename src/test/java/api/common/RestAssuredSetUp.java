package api.common;

import io.restassured.RestAssured;

import static api.common.ConstantUtils.BASE_URL;
import static api.common.ConstantUtils.PORT;

public class RestAssuredSetUp {
    public static void setUp(){
        RestAssured.baseURI = BASE_URL;
        RestAssured.port = PORT;
    }
}
