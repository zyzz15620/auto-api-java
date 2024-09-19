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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
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
        Address address = Address.getDefault();

        //FirstName
        user.setFirstName(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when firstName is null", user,
                new ValidationResponse("", "must have required property 'firstName'")));
        user = User.getDefaultWithEmail();
        user.setFirstName("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when firstName is empty", user,
                new ValidationResponse("/firstName", "must NOT have fewer than 1 characters")));
        user = User.getDefaultWithEmail();
        user.setFirstName("oeiqurkajdgfmncvbaisudfgoae8vb8wevhaiudgfkjahgefqe8ytf789adovgadvbakuygrfo8q7gefdagfkjagfkdjsgfuweiycbwyebv");
        argumentsList.add(Arguments.arguments("Verify API return 400 when firstName is more than 100 char", user,
                new ValidationResponse("/firstName", "must NOT have more than 100 characters")));

        //LastName
        user = User.getDefaultWithEmail();
        user.setLastName(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when lastName is null", user,
                new ValidationResponse("", "must have required property 'lastName'")));
        user = User.getDefaultWithEmail();
        user.setLastName("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when lastName is empty", user,
                new ValidationResponse("/lastName", "must NOT have fewer than 1 characters")));
        user = User.getDefaultWithEmail();
        user.setLastName("oeiqurkajdgfmncvbaisudfgoae8vb8wevhaiudgfkjahgefqe8ytf789adovgadvbakuygrfo8q7gefdagfkjagfkdjsgfuweiycbwyebv");
        argumentsList.add(Arguments.arguments("Verify API return 400 when lastName is more than 100 char", user,
                new ValidationResponse("/lastName", "must NOT have more than 100 characters")));

        //Birthday
        user = User.getDefaultWithEmail();
        user.setBirthday(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when birthday is null", user,
                new ValidationResponse("", "must have required property 'birthday'")));
        user = User.getDefaultWithEmail();
        user.setBirthday("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when birthday is empty", user,
                new ValidationResponse("/birthday", "must match pattern \"^\\d{2}-\\d{2}-\\d{4}$\"")));
        //--------------------------------------------------------------------------------
//        user = User.getDefaultWithEmail();
//        user.setBirthday("13-01-2000");
//        argumentsList.add(Arguments.arguments("Verify API return 400 when birthday is in dd-mm-yyyy pattern", user,
//                new ValidationResponse("/birthday", "must match pattern \"^\\d{2}-\\d{2}-\\d{4}$\"")));
//        user = User.getDefaultWithEmail();
//        user.setBirthday(LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
//        argumentsList.add(Arguments.arguments("Verify API return 400 when user under 18 years old", user,
//                new ValidationResponse("/birthday", "")));
//        user = User.getDefaultWithEmail();
//        user.setBirthday(LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
//        argumentsList.add(Arguments.arguments("Verify API return 400 when user is from future", user,
//                new ValidationResponse("/birthday", "")));

        //Email
        user = User.getDefaultWithEmail();
        user.setEmail(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when email is null", user,
                new ValidationResponse("", "must have required property 'email'")));
        user = User.getDefaultWithEmail();
        user.setEmail("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when email is empty", user,
                new ValidationResponse("/email", "must match format \"email\"")));
        user = User.getDefaultWithEmail();
        user.setEmail("meow.xyz.com");
        argumentsList.add(Arguments.arguments("Verify API return 400 when email is in wrong format", user,
                new ValidationResponse("/email", "must match format \"email\"")));
        user = User.getDefaultWithEmail();
        user.setEmail(".meow@gmail.com");
        argumentsList.add(Arguments.arguments("Verify API return 400 when email start with special symbol", user,
                new ValidationResponse("/email", "must match format \"email\"")));
        //--------------------------------------------------------------------------------
//        user = User.getDefaultWithEmail();
//        user.setEmail("meow$@gmail.com");
//        argumentsList.add(Arguments.arguments("Verify API return 400 when email contain a special symbol", user,
//                new ValidationResponse("/email", "must match format \"email\"")));

        //Phone
        user = User.getDefaultWithEmail();
        user.setPhone(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when phone is null", user,
                new ValidationResponse("", "must have required property 'phone'")));
        user = User.getDefaultWithEmail();
        user.setPhone("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when phone is empty", user,
                new ValidationResponse("/phone", "must match pattern \"^\\d{10,11}$\"")));
        user = User.getDefaultWithEmail();
        user.setPhone("0123K56789");
        argumentsList.add(Arguments.arguments("Verify API return 400 when phone contain character", user,
                new ValidationResponse("/phone", "must match pattern \"^\\d{10,11}$\"")));
        user.setPhone("0126789");
        argumentsList.add(Arguments.arguments("Verify API return 400 when phone is too short", user,
                new ValidationResponse("/phone", "must match pattern \"^\\d{10,11}$\"")));
        user.setPhone("01267890126789");
        argumentsList.add(Arguments.arguments("Verify API return 400 when phone is too long", user,
                new ValidationResponse("/phone", "must match pattern \"^\\d{10,11}$\"")));

        //StreetNumber
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setStreetNumber(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when streetNumber is null", user,
                new ValidationResponse("/addresses/0", "must have required property 'streetNumber'")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setStreetNumber("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when streetNumber is empty", user,
                new ValidationResponse("/addresses/0/streetNumber", "must NOT have fewer than 1 characters")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setStreetNumber("1234567890123");
        argumentsList.add(Arguments.arguments("Verify API return 400 when streetNumber is more than 12 char", user,
                new ValidationResponse("/addresses/0/streetNumber", "must NOT have more than 10 characters")));

        //Street, similar to StreetNumber
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setStreet(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when street is null", user,
                new ValidationResponse("/addresses/0", "must have required property 'street'")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setStreet("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when street is empty", user,
                new ValidationResponse("/addresses/0/street", "must NOT have fewer than 1 characters")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setStreet("12345678901234567890123456789012345678901234567890123456789012345678901456789012345678902345678901234567890");
        argumentsList.add(Arguments.arguments("Verify API return 400 when street is more than 100 char", user,
                new ValidationResponse("/addresses/0/street", "must NOT have more than 100 characters")));

        //Ward
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setWard(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when ward is null", user,
                new ValidationResponse("/addresses/0", "must have required property 'ward'")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setWard("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when ward is empty", user,
                new ValidationResponse("/addresses/0/ward", "must NOT have fewer than 1 characters")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setWard("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789045678901234567890");
        argumentsList.add(Arguments.arguments("Verify API return 400 when ward is more than 100 char", user,
                new ValidationResponse("/addresses/0/ward", "must NOT have more than 100 characters")));

        //District
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setDistrict(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when district is null", user,
                new ValidationResponse("/addresses/0", "must have required property 'district'")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setDistrict("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when district is empty", user,
                new ValidationResponse("/addresses/0/district", "must NOT have fewer than 1 characters")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setDistrict("12345678901234567890123456789012345678901234567890123456789012345674567890123456789089012345678901234567890");
        argumentsList.add(Arguments.arguments("Verify API return 400 when district is more than 100 char", user,
                new ValidationResponse("/addresses/0/district", "must NOT have more than 100 characters")));

        //City
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setCity(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when city is null", user,
                new ValidationResponse("/addresses/0", "must have required property 'city'")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setCity("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when city is empty", user,
                new ValidationResponse("/addresses/0/city", "must NOT have fewer than 1 characters")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setCity("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678904567890");
        argumentsList.add(Arguments.arguments("Verify API return 400 when city is more than 100 char", user,
                new ValidationResponse("/addresses/0/city", "must NOT have more than 100 characters")));

        //State
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setState(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when state is null", user,
                new ValidationResponse("/addresses/0", "must have required property 'state'")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setState("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when state is empty", user,
                new ValidationResponse("/addresses/0/state", "must NOT have fewer than 1 characters")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setState("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789045678901234567890");
        argumentsList.add(Arguments.arguments("Verify API return 400 when state is more than 100 char", user,
                new ValidationResponse("/addresses/0/state", "must NOT have more than 100 characters")));

        //Zip
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setZip(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when zip is null", user,
                new ValidationResponse("/addresses/0", "must have required property 'zip'")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setZip("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when zip is empty", user,
                new ValidationResponse("/addresses/0/zip", "must match pattern \"^\\d{5}(?:-\\d{4})?$\"")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setZip("70000-123");
        argumentsList.add(Arguments.arguments("Verify API return 400 when additional zip is in wrong format", user,
                new ValidationResponse("/addresses/0/zip", "must match pattern \"^\\d{5}(?:-\\d{4})?$\"")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setZip("7000000");
        argumentsList.add(Arguments.arguments("Verify API return 400 when zip is in wrong format", user,
                new ValidationResponse("/addresses/0/zip", "must match pattern \"^\\d{5}(?:-\\d{4})?$\"")));

        //Country pattern ^[A-Z]{2}$
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setCountry(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when country is null", user,
                new ValidationResponse("/addresses/0", "must have required property 'country'")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setCountry("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when country is empty", user,
                new ValidationResponse("/addresses/0/country", "must NOT have fewer than 2 characters")));
        user = User.getDefaultWithEmail();
        address = Address.getDefault();
        user.setAddresses(List.of(address));
        user.getAddresses().get(0).setCountry("VNN");
        argumentsList.add(Arguments.arguments("Verify API return 400 when country is not 2 char", user,
                new ValidationResponse("/addresses/0/country", "must NOT have more than 2 characters")));


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
    public void verifyRequiredFieldWhenCreateUser(String testCase, User<Address> user, ValidationResponse expectedValidationResponse){
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header("Authorization", TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        assertThat(createUserResponse.statusCode(), equalTo(400));
        assertThat(createUserResponse.as(ValidationResponse.class), samePropertyValuesAs(expectedValidationResponse));
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

