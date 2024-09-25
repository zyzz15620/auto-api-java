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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static apiCustomerManagement.common.ConstantUtils.*;
import static apiCustomerManagement.common.MethodUtils.*;
import static apiCustomerManagement.test.LoginApiTests.getStaffLoginResponse;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateUserApiTests
{
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
            Response actualResponse = getStaffLoginResponse("staff", "1234567890");
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

    @Test
    public void verifyUpdateUserSuccessfully() throws JsonProcessingException {
        //Create User
        String randomEmail = getRandomEmail();
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));

        //Create-Request
        Instant referenceTime = Instant.now();
        Response createUserResponse = createRequest(TOKEN, user);
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));

        //Create expectedGetUserResponse
        ObjectMapper objectMapper = new ObjectMapper();
        GetUserResponse<GetAddressResponse> expectedGetUserResponse = objectMapper.convertValue(user, new TypeReference<>() {});
        expectedGetUserResponse.setId(actual.getId());
        expectedGetUserResponse.getAddresses().get(0).setCustomerId(actual.getId());

        //Get-Request & Assert
        Response getCreatedUserResponse = getRequest(actual.getId(), TOKEN);
        GetUserResponse<GetAddressResponse> actualGetUserResponse = getCreatedUserResponse.as(new TypeRef<>() {});
        assertThat(actualGetUserResponse, jsonEquals(expectedGetUserResponse).whenIgnoringPaths(
                "createdAt"
                , "updatedAt"
                , "addresses[*].id"
                , "addresses[*].createdAt"
                , "addresses[*].updatedAt"));

        datetimeVerifier(referenceTime, actualGetUserResponse.getCreatedAt());
        datetimeVerifier(referenceTime, actualGetUserResponse.getUpdatedAt());
        for (GetAddressResponse getAddressResponse : actualGetUserResponse.getAddresses()) {
            assertTrue(getAddressResponse.getId().matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");
            datetimeVerifier(referenceTime, getAddressResponse.getCreatedAt());
            datetimeVerifier(referenceTime, getAddressResponse.getUpdatedAt());
        }

        //Update-Request, also update expectedGetUserResponse
        user.setFirstName("Anh Duc");
        user.setLastName("Pham");
        user.getAddresses().get(0).setCity("HCM");
        user.getAddresses().get(0).setCountry("VN");

        expectedGetUserResponse = objectMapper.convertValue(user, new TypeReference<>() {});
        expectedGetUserResponse.setId(actual.getId());
        expectedGetUserResponse.getAddresses().get(0).setCustomerId(actual.getId());
        updateUser(TOKEN, actual.getId(), user);

        //Get-Request & Assert
        Response getUpdatedUserResponse = getRequest(actual.getId(), TOKEN);
        GetUserResponse<GetAddressResponse> actualUpdatedResponseBody = getUpdatedUserResponse.as(new TypeRef<>() {});
        assertThat(actualUpdatedResponseBody, jsonEquals(expectedGetUserResponse).whenIgnoringPaths(
                "createdAt"
                , "updatedAt"
                , "addresses[*].id"
                , "addresses[*].createdAt"
                , "addresses[*].updatedAt"));

        datetimeVerifier(referenceTime, actualUpdatedResponseBody.getCreatedAt());
        datetimeVerifier(referenceTime, actualUpdatedResponseBody.getUpdatedAt());
        for (GetAddressResponse getAddressResponse : actualUpdatedResponseBody.getAddresses()) {
            assertTrue(getAddressResponse.getId().matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");
            datetimeVerifier(referenceTime, getAddressResponse.getCreatedAt());
            datetimeVerifier(referenceTime, getAddressResponse.getUpdatedAt());
        }
    }

    @Test
    public void verifyUpdateUserSuccessfullyByDB() throws JsonProcessingException {
        String randomEmail = getRandomEmail();
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));

        //Create-Request
        Instant referenceTime = Instant.now();
        Response createUserResponse = createRequest(TOKEN, user);
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));

        //Create expectedGetUserResponse
        ObjectMapper objectMapper = new ObjectMapper();
        final GetUserResponse<GetAddressResponse> expectedGetUserResponse = objectMapper.convertValue(user, new TypeReference<>() {
        });
        expectedGetUserResponse.setId(actual.getId());
        expectedGetUserResponse.getAddresses().get(0).setCustomerId(actual.getId());

        //GetUserFromDB & Assert
        sessionFactory.inTransaction(session -> {
            GetUserResponse<GetAddressResponse> actualGetUserResponse = getUserFromDB(actual.getId(), TOKEN, objectMapper, session);
            assertThat(actualGetUserResponse, jsonEquals(expectedGetUserResponse).whenIgnoringPaths(
                    "createdAt"
                    , "updatedAt"
                    , "addresses[*].id"
                    , "addresses[*].createdAt"
                    , "addresses[*].updatedAt"));

            datetimeVerifier(referenceTime, actualGetUserResponse.getCreatedAt());
            datetimeVerifier(referenceTime, actualGetUserResponse.getUpdatedAt());
            for (GetAddressResponse getAddressResponse : actualGetUserResponse.getAddresses()) {
                assertTrue(getAddressResponse.getId().matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");
                datetimeVerifier(referenceTime, getAddressResponse.getCreatedAt());
                datetimeVerifier(referenceTime, getAddressResponse.getUpdatedAt());
            }});

            //Update-Request, and create expectedGetUpdatedUserResponse
            user.setFirstName("Anh Duc");
            user.setLastName("Pham");
            user.getAddresses().get(0).setCity("HCM");
            user.getAddresses().get(0).setCountry("VN");
            final GetUserResponse<GetAddressResponse> expectedGetUpdatedUserResponse = objectMapper.convertValue(user, new TypeReference<>() {});
            expectedGetUpdatedUserResponse.setId(actual.getId());
            expectedGetUpdatedUserResponse.getAddresses().get(0).setCustomerId(actual.getId());
            updateUser(TOKEN, actual.getId(), user);

            //Get-Request & Assert
        sessionFactory.inTransaction(session -> {
            Response getUpdatedUserResponse = getRequest(actual.getId(), TOKEN);
            GetUserResponse<GetAddressResponse> actualUpdatedResponseBody = getUpdatedUserResponse.as(new TypeRef<>() {
            });
            assertThat(actualUpdatedResponseBody, jsonEquals(expectedGetUpdatedUserResponse).whenIgnoringPaths(
                    "createdAt"
                    , "updatedAt"
                    , "addresses[*].id"
                    , "addresses[*].createdAt"
                    , "addresses[*].updatedAt"));

            datetimeVerifier(referenceTime, actualUpdatedResponseBody.getCreatedAt());
            datetimeVerifier(referenceTime, actualUpdatedResponseBody.getUpdatedAt());
            for (GetAddressResponse getAddressResponse : actualUpdatedResponseBody.getAddresses()) {
                assertTrue(getAddressResponse.getId().matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");
                datetimeVerifier(referenceTime, getAddressResponse.getCreatedAt());
                datetimeVerifier(referenceTime, getAddressResponse.getUpdatedAt());
            }
        });
    }
}
