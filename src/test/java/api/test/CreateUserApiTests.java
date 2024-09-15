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
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static api.test.LoginApiTests.getStaffLoginResponse;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserApiTests {
    private static final String CREATE_USER_PATH = "/api/user";
    private static final String DELETE_USER_PATH = "/api/user/{id}";
    private static final String GET_USER_PATH = "/api/user/{id}";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final List<String> createdUserIds = new ArrayList<>();
    private static String token;

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;

        Response actualResponse = getStaffLoginResponse("staff", "1234567890");
        assertThat(actualResponse.statusCode(), equalTo(200));  //We still need assertion here because if it fails, the other tests won't need to run which helps to save time
        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getToken(), not(blankString()));
        token = "Bearer ".concat(loginResponse.getToken()); // we need to save the token after assertion, if assertion failed then the token doesn't need to be saved
    }

    @AfterAll
    static void cleanData() {
        createdUserIds.forEach(id -> {
            RestAssured.given().log().all()
                    .pathParam("id", id)
                    .header(AUTHORIZATION_HEADER, token)
                    .delete(DELETE_USER_PATH);
        });
    }

    @Test
    public void verifyStaffCreateUserSuccessfully() throws JsonProcessingException {
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis()); //set a name so that we can easy to query and delete the data
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));
        //Store the moment before execution
        Instant referenceTime = Instant.now();

        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header("Authorization", token)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %n%s", createUserResponse.asString()); //to log
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created")); //In reality there could be a failure here due to typo in the message, so the cleaning data method wont be executed

        Response getCreatedUserResponse = RestAssured.given().log().all()
                .pathParam("id", actual.getId())
                .header(AUTHORIZATION_HEADER, token)
                .get(GET_USER_PATH);
        System.out.printf("Get created user response: %n%s", getCreatedUserResponse.asString()); //to log
        assertThat(getCreatedUserResponse.statusCode(), equalTo(200));
        //verify the response schema, homework

        ObjectMapper objectMapper = new ObjectMapper();
        GetUserResponse<AddressGetResponse> expectedUser = objectMapper.convertValue(user, new TypeReference<>() {
        });
        expectedUser.setId(actual.getId());
        expectedUser.getAddresses().get(0).setCustomerId(actual.getId());

        String actualResponseBody = getCreatedUserResponse.asString();
        assertThat(actualResponseBody, jsonEquals(expectedUser).whenIgnoringPaths("createdAt"
                , "updatedAt"
                , "addresses[*].id"
                , "addresses[*].createdAt"
                , "addresses[*].updatedAt"));

        GetUserResponse<AddressGetResponse> actualGetUserResponse = getCreatedUserResponse.as(new TypeRef<GetUserResponse<AddressGetResponse>>() {
        });

        String addressId = actualGetUserResponse.getAddresses().get(0).getId();
        assertTrue(addressId.matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");

        Instant userCreatedAtInstant = Instant.parse(actualGetUserResponse.getCreatedAt());
        Instant userUpdatedAtInstant = Instant.parse(actualGetUserResponse.getUpdatedAt());
        datetimeVerifier(referenceTime, userCreatedAtInstant);
        datetimeVerifier(referenceTime, userUpdatedAtInstant);

        //good practice is using for each, in case there might be more than 1 in the array
        Instant addressCreatedAtInstant = Instant.parse(actualGetUserResponse.getAddresses().get(0).getCreatedAt());
        Instant addressUpdatedAtInstant = Instant.parse(actualGetUserResponse.getAddresses().get(0).getUpdatedAt());
        datetimeVerifier(referenceTime, addressCreatedAtInstant);
        datetimeVerifier(referenceTime, addressUpdatedAtInstant);
    }

    private void datetimeVerifier(Instant timeBeforeExecution, Instant time) {
        assertThat(time.isAfter(timeBeforeExecution), equalTo(true));
        assertThat(time.isBefore(Instant.now()), equalTo(true));
    }
}

