package api.test;

import api.common.DatabaseConnection;
import api.common.RestAssuredSetUp;
import api.model.login.LoginResponse;
import api.model.user.*;
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

import static api.common.ConstantUtils.*;
import static api.common.MethodUtils.*;
import static api.test.LoginApiTests.getStaffLoginResponse;
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
        String randomEmail = getRandomEmail();
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
        assertThat(actual.getMessage(), equalTo("Customer created"));

        //Get Request
        Response getCreatedUserResponse = getRequest(actual.getId(), TOKEN);

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
        Instant userCreatedAtInstant = Instant.parse(actualGetUserResponse.getCreatedAt());
        Instant userUpdatedAtInstant = Instant.parse(actualGetUserResponse.getUpdatedAt());
        datetimeVerifier(referenceTime, userCreatedAtInstant);
        datetimeVerifier(referenceTime, userUpdatedAtInstant);
        for (GetAddressResponse getAddressResponse : actualGetUserResponse.getAddresses()) {
            assertTrue(getAddressResponse.getId().matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), "ID format is invalid");
            Instant addressCreatedAtInstant = Instant.parse(getAddressResponse.getCreatedAt());
            Instant addressUpdatedAtInstant = Instant.parse(getAddressResponse.getUpdatedAt());
            datetimeVerifier(referenceTime, addressCreatedAtInstant);
            datetimeVerifier(referenceTime, addressUpdatedAtInstant);
        }

        //Update Request
        user.setFirstName("Anh Duc");
        user.setLastName("Pham");
        user.getAddresses().get(0).setCity("HCM");
        user.getAddresses().get(0).setCountry("VN");
        GetUserResponse<GetAddressResponse> expectedUpdatedUser = objectMapper.convertValue(user, new TypeReference<>() {});
        expectedUpdatedUser.setId(actual.getId());
        expectedUpdatedUser.getAddresses().get(0).setCustomerId(actual.getId());
        updateUser(TOKEN, actual.getId(), user);

        //Get Request
        Response getUpdatedUserResponse = getRequest(actual.getId(), TOKEN);
        String actualUpdatedResponseBody = getUpdatedUserResponse.asString();
        assertThat(actualUpdatedResponseBody, jsonEquals(expectedUpdatedUser).whenIgnoringPaths(
                "createdAt"
                , "updatedAt"
                , "addresses[*].id"
                , "addresses[*].createdAt"
                , "addresses[*].updatedAt"));
    }
}
