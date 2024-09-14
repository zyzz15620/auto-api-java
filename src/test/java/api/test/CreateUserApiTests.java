package api.test;

import api.model.login.LoginResponse;
import api.model.user.Address;
import api.model.user.CreateUserResponse;
import api.model.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
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
import static org.hamcrest.Matchers.equalTo;
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

    @Test
    public void verifyStaffCreateUserSuccessfully() throws JsonProcessingException {
        Address address = new Address();
        address.setStreetNumber("123");
        address.setStreet("Main St");
        address.setWard("Ward 1");
        address.setDistrict("District 1");
        address.setCity("Thu Duc");
        address.setState("Ho Chi Minh");
        address.setZip("70000");
        address.setCountry("VN");

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setMiddleName("Smith");
        user.setBirthday("01-23-2000");
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis()); //set a name so that we can easy to query and delete the data
        user.setEmail(randomEmail);
        user.setPhone("0123456788");
        user.setAddresses(List.of(address));

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

        //kiểm tra tính đúng đắn
        String expectedTemplate = """
                {
                    "id": "%s",
                    "firstName": "John",
                    "lastName": "Doe",
                    "middleName": "Smith",
                    "birthday": "01-23-2000",
                    "phone": "0123456788",
                    "email": "%s",
                    "createdAt": "",
                    "updatedAt": "",
                    "addresses": [
                        {
                            "id": "",
                            "customerId": "%s",
                            "streetNumber": "123",
                            "street": "Main St",
                            "ward": "Ward 1",
                            "district": "District 1",
                            "city": "Thu Duc",
                            "state": "Ho Chi Minh",
                            "zip": "70000",
                            "country": "VN",
                            "createdAt": "",
                            "updatedAt": ""
                        }
                    ]
                }""";
        String expected =  String.format(expectedTemplate, actual.getId(), randomEmail, actual.getId());
        String actualResponseBody = getCreatedUserResponse.asString();
        assertThat(actualResponseBody, jsonEquals(expected).whenIgnoringPaths("createdAt"
                , "updatedAt"
                , "addresses[*].id"
                , "addresses[*].createdAt"
                , "addresses[*].updatedAt"));


        //Homework: verify the createdAt, updatedAt, addressId, addressCreatedAt, addressUpdatedAt
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNodeResponseOfUser = objectMapper.readTree(getCreatedUserResponse.getBody().asString());
        JsonNode jsonNodeResponseOfAddress = jsonNodeResponseOfUser.get("addresses").get(0);

        String addressesId = jsonNodeResponseOfAddress.get("id").asText();
        assertTrue(addressesId.matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");

        String userCreatedAt = jsonNodeResponseOfAddress.get("createdAt").asText();
        String userUpdatedAt = jsonNodeResponseOfAddress.get("updatedAt").asText();
        String addressCreatedAt = jsonNodeResponseOfAddress.get("createdAt").asText();
        String addressUpdatedAt = jsonNodeResponseOfAddress.get("updatedAt").asText();
        Instant userCreatedAtInstant = Instant.parse(userCreatedAt);
        Instant userUpdatedAtInstant = Instant.parse(userUpdatedAt);
        Instant addressCreatedAtInstant = Instant.parse(addressCreatedAt);
        Instant addressUpdatedAtInstant = Instant.parse(addressUpdatedAt);

        //Verify createdAt is after reference time
        assertTrue(userCreatedAtInstant.isAfter(referenceTime), "createdAt is too far in the past");
        assertTrue(addressCreatedAtInstant.isAfter(referenceTime), "createdAt is too far in the past");

        //Verify updatedAt is after or same at createdAt
        assertTrue(userCreatedAtInstant.isBefore(userUpdatedAtInstant) || userCreatedAtInstant.equals(userUpdatedAtInstant), "createdAt is after updatedAt");
        assertTrue(addressCreatedAtInstant.isBefore(addressUpdatedAtInstant) || addressCreatedAtInstant.equals(addressUpdatedAtInstant), "createdAt is after updatedAt");

        //Verify updatedAt is before current time
        assertTrue(userUpdatedAtInstant.isBefore(Instant.now()), "updatedAt is in the future");
        assertTrue(addressUpdatedAtInstant.isBefore(Instant.now()), "updatedAt is in the future");

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
}

