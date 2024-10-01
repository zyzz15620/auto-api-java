package apiCustomerManagement.test;

import apiCustomerManagement.common.DatabaseConnection;
import apiCustomerManagement.common.RestAssuredSetUp;
import apiCustomerManagement.model.login.LoginResponse;
import apiCustomerManagement.model.user.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static apiCustomerManagement.common.ConstantUtils.*;
import static apiCustomerManagement.common.MethodUtils.*;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserApiTests {

    private static final List<String> createdUserIds = new ArrayList<>();
    private static String TOKEN;
    private static long TIMEOUT = -1;
    private static long TIMEOUT_BEFORE_GET_TOKEN = -1;
    private static final SessionFactory sessionFactory = DatabaseConnection.getSessionFactory();

    @BeforeAll
    static void setUp() {
        RestAssuredSetUp.setUp();
    }

    @BeforeEach
    void beforeEach() {
        if (TIMEOUT == -1 || (System.currentTimeMillis() - TIMEOUT_BEFORE_GET_TOKEN) > TIMEOUT * 0.8) {
            TIMEOUT_BEFORE_GET_TOKEN = System.currentTimeMillis();
            Response actualResponse = getStaffLoginResponse();
            assertThat(actualResponse.statusCode(), equalTo(200));  //We still need assertion here because if it fails, the other tests won't need to run which helps to save time
            LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
            assertThat(loginResponse.getToken(), not(blankString()));
            TOKEN = "Bearer ".concat(loginResponse.getToken()); // we need to save the token after assertion, if assertion failed then the token doesn't need to be saved
            TIMEOUT = loginResponse.getTimeout();
        }
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

    @ParameterizedTest
    @MethodSource("apiCustomerManagement.data.InvalidUserData#validationUserProvider")
    public void verifyRequiredFieldWhenCreateUser(String testCase, User<Address> user, ValidationResponse expectedValidationResponse) {
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header("Authorization", TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %n%s", createUserResponse.asString());
        assertThat(createUserResponse.statusCode(), equalTo(400));
        assertThat(createUserResponse.as(ValidationResponse.class), samePropertyValuesAs(expectedValidationResponse));

        //add userID if the user is created successfully status 200
        if (createUserResponse.statusCode() == 200) {
            CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
            createdUserIds.add(actual.getId());
        }
    }


    @Test
    public void verifyStaffCreateUserSuccessfullyWithMultipleAddress() throws JsonProcessingException {
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis()); //set a good name so that we can easy to query and delete the data
        Address address = Address.getDefault();
        Address address2 = Address.getDefault();

        User<Address> user = User.getDefault();
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address, address2));

        //Create Request
        Instant referenceTime = Instant.now();
        Response createUserResponse = createRequest(TOKEN, user);
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created")); //In reality there could be a failure here due to typo in the message, so the cleaning data method wont be executed

        //Get Request
        Response getCreatedUserResponse = getRequest(actual.getId(), TOKEN);
        ObjectMapper objectMapper = new ObjectMapper();
        GetUserResponse<GetAddressResponse> expectedUser = objectMapper.convertValue(user, new TypeReference<>() {
        });
        expectedUser.setId(actual.getId());
        expectedUser.getAddresses().get(0).setCustomerId(actual.getId());
        expectedUser.getAddresses().get(1).setCustomerId(actual.getId());

        String actualResponseBody = getCreatedUserResponse.asString();
        assertThat(actualResponseBody, jsonEquals(expectedUser).whenIgnoringPaths(
                "createdAt"
                , "updatedAt"
                , "addresses[*].id"
                , "addresses[*].createdAt"
                , "addresses[*].updatedAt"));

        GetUserResponse<GetAddressResponse> actualGetUserResponse = getCreatedUserResponse.as(new TypeRef<>() {
        });
        datetimeVerifier(referenceTime, actualGetUserResponse.getCreatedAt());
        datetimeVerifier(referenceTime, actualGetUserResponse.getUpdatedAt());
        for (GetAddressResponse getAddressResponse : actualGetUserResponse.getAddresses()) {
            assertTrue(getAddressResponse.getId().matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");
            datetimeVerifier(referenceTime, getAddressResponse.getCreatedAt());
            datetimeVerifier(referenceTime, getAddressResponse.getUpdatedAt());
        }
    }


    @Test
    public void verifyStaffCreateUserSuccessfully() throws JsonProcessingException {
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis()); //set a good name so that we can easy to query and delete the data
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));

        //Create Request
        Instant referenceTime = Instant.now();
        Response createUserResponse = createRequest(TOKEN, user);
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created")); //In reality there could be a failure here due to typo in the message, so the cleaning data method wont be executed

        //Get Request
        Response getCreatedUserResponse = getRequest(actual.getId(), TOKEN);
        System.out.printf("Get created user response: %n%s", getCreatedUserResponse.asString()); //to log
        assertThat(getCreatedUserResponse.statusCode(), equalTo(200));

        ObjectMapper objectMapper = new ObjectMapper();
        GetUserResponse<GetAddressResponse> expectedUser = objectMapper.convertValue(user, new TypeReference<>() {
        });
        expectedUser.setId(actual.getId());
        expectedUser.getAddresses().get(0).setCustomerId(actual.getId());

        String actualResponseBody = getCreatedUserResponse.asString();
        assertThat(actualResponseBody, jsonEquals(expectedUser).whenIgnoringPaths(
                "createdAt"
                , "updatedAt"
                , "addresses[*].id"
                , "addresses[*].createdAt"
                , "addresses[*].updatedAt"));

        GetUserResponse<GetAddressResponse> actualGetUserResponse = getCreatedUserResponse.as(new TypeRef<>() {
        });
        datetimeVerifier(referenceTime, actualGetUserResponse.getCreatedAt());
        datetimeVerifier(referenceTime, actualGetUserResponse.getUpdatedAt());
        for (GetAddressResponse getAddressResponse : actualGetUserResponse.getAddresses()) {
            assertTrue(getAddressResponse.getId().matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");
            datetimeVerifier(referenceTime, getAddressResponse.getCreatedAt());
            datetimeVerifier(referenceTime, getAddressResponse.getUpdatedAt());
        }
    }


    @Test
    public void verifyStaffCreateUserSuccessfullyByDataBase() throws JsonProcessingException {
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis()); //set a good name so that we can easy to query and delete the data
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));

        //Create Request
        Instant referenceTime = Instant.now();
        Response createUserResponse = createRequest(TOKEN, user);
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created")); //In reality there could be a failure here due to typo in the message, so the cleaning data method wont be executed

        //Build expected user
        ObjectMapper objectMapper = new ObjectMapper();
        GetUserResponse<GetAddressResponse> expectedUser = objectMapper.convertValue(user, new TypeReference<>() {});
        expectedUser.setId(actual.getId());
        expectedUser.getAddresses().get(0).setCustomerId(actual.getId());

        sessionFactory.inTransaction(session -> {
            GetUserResponse<GetAddressResponse> actualUser = getUserFromDB(actual.getId(), TOKEN, objectMapper, session);

            assertThat(actualUser, jsonEquals(expectedUser).whenIgnoringPaths(
                    "createdAt"
                    , "updatedAt"
                    , "addresses[*].id"
                    , "addresses[*].createdAt"
                    , "addresses[*].updatedAt"));

            datetimeVerifier(referenceTime, actualUser.getCreatedAt());
            datetimeVerifier(referenceTime, actualUser.getCreatedAt());
            for (GetAddressResponse addressGetResponse : actualUser.getAddresses()) {
                assertTrue(addressGetResponse.getId().matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");
                datetimeVerifier(referenceTime, actualUser.getCreatedAt());
                datetimeVerifier(referenceTime, actualUser.getCreatedAt());
            }
        });
    }


}

