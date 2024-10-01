package apiCustomerManagement.test;

import apiCustomerManagement.common.RestAssuredSetUp;
import apiCustomerManagement.model.login.LoginResponse;
import apiCustomerManagement.model.user.*;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static apiCustomerManagement.common.ConstantUtils.AUTHORIZATION_HEADER;
import static apiCustomerManagement.common.ConstantUtils.DELETE_USER_PATH;
import static apiCustomerManagement.common.MethodUtils.*;
import static apiCustomerManagement.common.StubServer.startStubServer;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CreateMembershipApiTests {
    private static final List<String> createdUserIds = new ArrayList<>();
    private static String TOKEN;
    private static long TIMEOUT = -1;
    private static long TIMEOUT_BEFORE_GET_TOKEN = -1;

    @BeforeAll
    static void setUp() {
        RestAssuredSetUp.setUp();
        startStubServer();
    }

    @BeforeEach
    void beforeEach() {
        if (TIMEOUT == -1 || (System.currentTimeMillis() - TIMEOUT_BEFORE_GET_TOKEN) > TIMEOUT * 0.8) {
            TIMEOUT_BEFORE_GET_TOKEN = System.currentTimeMillis();
            LoginResponse loginResponse = loginResponse();
            assertThat(loginResponse.getToken(), not(blankString()));
            TOKEN = "Bearer ".concat(loginResponse.getToken()); // we need to save the token after assertion, if assertion failed then the token doesn't need to be saved
            TIMEOUT = loginResponse.getTimeout();
        }
    }

    @Test
    public void verifyCardCreateResponseSchema() {
        String randomEmail = getRandomEmail();
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));

        //Create Request
        Response createUserResponse = createRequest(TOKEN, user);
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));

        Response createMembershipResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(new CardInput(actual.getId(), "SLIVER"))
                .post("/api/card");
        System.out.printf("Create membership response: %n%s", createMembershipResponse.asString());
        assertThat(createMembershipResponse.statusCode(), equalTo(200));
        assertThat(createMembershipResponse.asString(),matchesJsonSchemaInClasspath("json-schema/create-card-json-schema.json"));
    }

    private static Stream<Arguments> cardTypeProvider() {
        return Stream.of(
                Arguments.arguments("SLIVER", "1234567890123456"),
                Arguments.arguments("GOLD", "1234567800000000"),
                Arguments.arguments("PLATINUM", "111111112345678")
        );
    }

    @ParameterizedTest
    @MethodSource("cardTypeProvider")
    public void verifyCardCreateSuccessfully(String cardType, String cardId) {
        String randomEmail = getRandomEmail();
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));

        //Create Request
        Response createUserResponse = createRequest(TOKEN, user);
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));

        //Create Membership
        Response createMembershipResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(new CardInput(actual.getId(), cardType))
                .post("/api/card");
        System.out.printf("Create membership response: %n%s", createMembershipResponse.asString());
        assertThat(createMembershipResponse.statusCode(), equalTo(200));
        assertThat(createMembershipResponse.asString(), jsonEquals(new CardBuildOutput(cardId, "Doe John",  "01-23-2025")));
    }

    @AfterAll
    static void cleanData() {
        createdUserIds.forEach(id -> {
            RestAssured.given().log().all()
                    .pathParam("id", id)
                    .header(AUTHORIZATION_HEADER, TOKEN)
                    .delete(DELETE_USER_PATH);
        });

    }
}
