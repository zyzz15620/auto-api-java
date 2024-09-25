package apiCustomerManagement.test;

import apiCustomerManagement.common.RestAssuredSetUp;
import apiCustomerManagement.model.login.LoginInput;
import apiCustomerManagement.model.login.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static apiCustomerManagement.common.ConstantUtils.LOGIN_PATH;
import static apiCustomerManagement.common.MethodUtils.getStaffLoginResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LoginApiTests {
    @BeforeAll
    static void setUp() {
        RestAssuredSetUp.setUp();
    }

    private static Stream<Arguments> invalidStaffAccounts(){
        return Stream.of(
                Arguments.arguments("valid-invalid", "staff", "1234"),
                Arguments.arguments("Invalid-Valid", "staff1", "1234567890"),
                Arguments.arguments("Empty-Valid", "", "1234567890"),
                Arguments.arguments("Valid-Empty", "staff", ""),
                Arguments.arguments("Null-Valid", null, "1234567890"),
                Arguments.arguments("Valid-Null", "staff", null)
        );
    }

    @Test
    public void verifyStaffLoginValidInput() {
        Response actualResponse = getStaffLoginResponse();
        assertThat(actualResponse.statusCode(), equalTo(200));
        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getToken(), not(blankString()));
        assertThat(loginResponse.getTimeout(), equalTo(120000));
    }

    @ParameterizedTest
    @MethodSource("invalidStaffAccounts")
    public void verifyStaffLoginInvalidInput(String testCase, String userName, String password) {
        Response actualResponse = getStaffLoginResponse(userName, password);
        assertThat(actualResponse.statusCode(), equalTo(401));
        assertThat(actualResponse.asString(), equalTo("{\"message\":\"Invalid credentials\"}"));
    }
}
