package api.test;

import api.model.login.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static api.test.LoginApiTests.getStaffLoginResponse;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GetUserApiTests {
    private static final String GET_USER_PATH = "/api/user/{id}";

    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port =  3000;
    }

    @Test
    public void verifyGetUserApiResponseSchema(){
        Response actualResponse = getStaffLoginResponse("staff", "1234567890");
        assertThat(actualResponse.statusCode(), equalTo(200));
        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getToken(), not(blankString()));

        RestAssured.given().log().all()
                .pathParam("id", "b25634e5-d354-4a05-8bbd-37aee0ee6bfb")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer ".concat(loginResponse.getToken()))
                .get(GET_USER_PATH)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-user-response-json-schema.json"));
    }
}
