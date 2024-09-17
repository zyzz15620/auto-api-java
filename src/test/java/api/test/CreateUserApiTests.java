package api.test;

import api.model.login.LoginResponse;
import api.model.user.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static api.test.LoginApiTests.getStaffLoginResponse;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserApiTests {
    private static final String CREATE_USER_PATH = "/api/user";
    private static final String DELETE_USER_PATH = "/api/user/{id}";
    private static final String GET_USER_PATH = "/api/user/{id}";
    private static final String UPDATE_USER_PATH = "/api/user/{id}";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final List<String> createdUserIds = new ArrayList<>();
    private static String TOKEN;
    private static long TIMEOUT = -1;
    private static long TIMEOUT_BEFORE_GET_TOKEN = -1;

    static Stream<Arguments> validationUserProvider() throws JsonProcessingException {
        List<Arguments> argumentsList = new ArrayList<>();
        User<Address> user = User.getDefaultWithEmail();
        user.setFirstName(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when firstName is null", user,
                new ValidationResponse("", "must have required property 'firstName'")));

        user = User.getDefaultWithEmail();
        user.setFirstName("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when firstName is empty", user,
                new ValidationResponse("/firstName", "must NOT have fewer than 1 characters")));

        return argumentsList.stream();
    }

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @BeforeEach
    void beforeEach(){
        if(TIMEOUT== -1 || (System.currentTimeMillis() - TIMEOUT_BEFORE_GET_TOKEN) > TIMEOUT*0.8){
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

    @ParameterizedTest
    @MethodSource("validationUserProvider")
    public void verifyRequiredFieldWhenCreateUser(String testCase, User<Address> user, ValidationResponse expectedValidationResponse) throws JsonProcessingException {
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header("Authorization", TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        assertThat(createUserResponse.statusCode(), equalTo(400));
        //equal object
        assertThat(createUserResponse.as(ValidationResponse.class), samePropertyValuesAs(expectedValidationResponse));
        //equal object using Junit
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
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header("Authorization", TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %n%s", createUserResponse.asString()); //to log
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created")); //In reality there could be a failure here due to typo in the message, so the cleaning data method wont be executed

        //Get Request
        Response getCreatedUserResponse = RestAssured.given().log().all()
                .pathParam("id", actual.getId())
                .header(AUTHORIZATION_HEADER, TOKEN)
                .get(GET_USER_PATH);
        System.out.printf("Get created user response: %n%s", getCreatedUserResponse.asString()); //to log
        assertThat(getCreatedUserResponse.statusCode(), equalTo(200));

        ObjectMapper objectMapper = new ObjectMapper();
        GetUserResponse<AddressGetResponse> expectedUser = objectMapper.convertValue(user, new TypeReference<>() {
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

        GetUserResponse<AddressGetResponse> actualGetUserResponse = getCreatedUserResponse.as(new TypeRef<>() {
        });
        Instant userCreatedAtInstant = Instant.parse(actualGetUserResponse.getCreatedAt());
        Instant userUpdatedAtInstant = Instant.parse(actualGetUserResponse.getUpdatedAt());
        datetimeVerifier(referenceTime, userCreatedAtInstant);
        datetimeVerifier(referenceTime, userUpdatedAtInstant);
        for (AddressGetResponse addressGetResponse : actualGetUserResponse.getAddresses()) {
            assertTrue(addressGetResponse.getId().matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");
            Instant addressCreatedAtInstant = Instant.parse(addressGetResponse.getCreatedAt());
            Instant addressUpdatedAtInstant = Instant.parse(addressGetResponse.getUpdatedAt());
            datetimeVerifier(referenceTime, addressCreatedAtInstant);
            datetimeVerifier(referenceTime, addressUpdatedAtInstant);
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
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header("Authorization", TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %n%s", createUserResponse.asString()); //to log
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created")); //In reality there could be a failure here due to typo in the message, so the cleaning data method wont be executed

        //Get Request
        Response getCreatedUserResponse = RestAssured.given().log().all()
                .pathParam("id", actual.getId())
                .header(AUTHORIZATION_HEADER, TOKEN)
                .get(GET_USER_PATH);
        System.out.printf("Get created user response: %n%s", getCreatedUserResponse.asString()); //to log
        assertThat(getCreatedUserResponse.statusCode(), equalTo(200));

        ObjectMapper objectMapper = new ObjectMapper();
        GetUserResponse<AddressGetResponse> expectedUser = objectMapper.convertValue(user, new TypeReference<>() {
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

        GetUserResponse<AddressGetResponse> actualGetUserResponse = getCreatedUserResponse.as(new TypeRef<>() {});
        Instant userCreatedAtInstant = Instant.parse(actualGetUserResponse.getCreatedAt());
        Instant userUpdatedAtInstant = Instant.parse(actualGetUserResponse.getUpdatedAt());
        datetimeVerifier(referenceTime, userCreatedAtInstant);
        datetimeVerifier(referenceTime, userUpdatedAtInstant);
        for(AddressGetResponse addressGetResponse : actualGetUserResponse.getAddresses()){
            assertTrue(addressGetResponse.getId().matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");
            Instant addressCreatedAtInstant = Instant.parse(addressGetResponse.getCreatedAt());
            Instant addressUpdatedAtInstant = Instant.parse(addressGetResponse.getUpdatedAt());
            datetimeVerifier(referenceTime, addressCreatedAtInstant);
            datetimeVerifier(referenceTime, addressUpdatedAtInstant);
        }

        //Update Request
        user.setFirstName("Anh Duc");
        user.setLastName("Pham");
        user.getAddresses().get(0).setCity("HCM");
        user.getAddresses().get(0).setCountry("VN");

        GetUserResponse<AddressGetResponse> expectedUpdatedUser = objectMapper.convertValue(user, new TypeReference<>() {});
        expectedUpdatedUser.setId(actual.getId());
        expectedUpdatedUser.getAddresses().get(0).setCustomerId(actual.getId());

        Response getUpdateUserResponse = RestAssured.given().log().all()
                .pathParam("id", actual.getId())
                .header("Content-Type", "application/json")
                .header("Authorization", TOKEN)
                .body(user)
                .put(UPDATE_USER_PATH);
        System.out.printf("Update user response: %n%s", getUpdateUserResponse.asString()); //to log
        assertThat(getUpdateUserResponse.statusCode(), equalTo(200));

        //Get Request
        Response getUpdatedUserResponse = RestAssured.given().log().all()
                .pathParam("id", actual.getId())
                .header(AUTHORIZATION_HEADER, TOKEN)
                .get(GET_USER_PATH);
        System.out.printf("Get updated user response: %n%s", getUpdatedUserResponse.asString()); //to log
        assertThat(getUpdatedUserResponse.statusCode(), equalTo(200));

        String actualUpdatedResponseBody = getUpdatedUserResponse.asString();
        assertThat(actualUpdatedResponseBody, jsonEquals(expectedUpdatedUser).whenIgnoringPaths(
            "createdAt"
                , "updatedAt"
                , "addresses[*].id"
                , "addresses[*].createdAt"
                , "addresses[*].updatedAt"));


    }

    private void datetimeVerifier(Instant timeBeforeExecution, Instant time) {
        assertThat(time.isAfter(timeBeforeExecution), equalTo(true));
        assertThat(time.isBefore(Instant.now()), equalTo(true));
    }
}

