package api.test;

import api.model.login.LoginInput;
import api.model.login.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LoginApiTests {
    private static final String LOGIN_PATH = "/api/login";

    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port =  3000;
    }

    @Test
    public void verifyStaffLoginValidInput(){
        Response actualResponse = getStaffLoginResponse("staff", "1234567890");
        assertThat(actualResponse.statusCode(), equalTo(200));
        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getToken(), not(blankString()));
        assertThat(loginResponse.getTimeout(), equalTo(120000));
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    public void verifyStaffLoginInvalidInput(List<String> InvalidInputs){
        Response actualResponse = getStaffLoginResponse(InvalidInputs.get(1), InvalidInputs.get(2));
        assertThat(actualResponse.statusCode(), equalTo(401));
        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getMessage(), equalTo("Invalid credentials"));
    }

    static Stream<List<String>> invalidInputs(){
        return Stream.of(
                Arrays.asList("Valid-Invalid", "staff", "123456780"),
                Arrays.asList("Invalid-Valid", "staff1", "1234567890"),
                Arrays.asList("Empty-Valid", "", "1234567890"),
                Arrays.asList("Valid-Empty", "staff", ""),
                Arrays.asList("Null-Valid", null, "1234567890"),
                Arrays.asList("Valid-Null", "staff", null)
        );
    }

    public static Response getStaffLoginResponse(String username, String password){
        LoginInput loginInput = new LoginInput(username, password);
        return RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(loginInput)
                .post(LOGIN_PATH);
    }
}
