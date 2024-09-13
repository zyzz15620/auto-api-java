package api.test;

import api.model.login.LoginResponse;
import api.model.user.Address;
import api.model.user.CreateUserResponse;
import api.model.user.User;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static api.test.LoginApiTests.getStaffLoginResponse;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

public class CreateUserApiTests {
    private static final String CREATE_USER_PATH = "/api/user";
    private static final String DELETE_USER_PATH = "/api/user/{id}";
    private static final String GET_USER_PATH = "/api/user/{id}";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static List<String> createdUserIds = new ArrayList<>();
    private static String token;

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;

        Response actualResponse = getStaffLoginResponse("staff", "1234567890");
        assertThat(actualResponse.statusCode(), equalTo(200));  //We still need assertion here because if it fails, the other tests won't need to run which helps to save time
        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getToken(), not(blankString()));
        token = loginResponse.getToken(); // we need to save the token after assertion, if assertion failed then the token doesn't need to be saved
    }

    @Test
    public void verifyStaffCreateUserSuccessfully() {

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

        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer ".concat(token))
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
                .header(AUTHORIZATION_HEADER, "Bearer ".concat(token))
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

    }

    @AfterAll
    static void cleanData() {
        createdUserIds.forEach(id -> {
            RestAssured.given().log().all()
                    .pathParam("id", id)
                    .header(AUTHORIZATION_HEADER, "Bearer ".concat(token))
                    .delete(DELETE_USER_PATH);
        });
    }
}

