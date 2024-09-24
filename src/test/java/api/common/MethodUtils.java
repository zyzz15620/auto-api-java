package api.common;

import api.model.user.Address;
import api.model.user.User;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.time.Instant;

import static api.common.ConstantUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MethodUtils {
    public static void datetimeVerifier(Instant timeBeforeExecution, Instant time) {
        assertThat(time.isAfter(timeBeforeExecution), equalTo(true));
        assertThat(time.isBefore(Instant.now()), equalTo(true));
    }

    public static String getRandomEmail(){
        return String.format("auto_api_%s@abc.com", System.currentTimeMillis());
    }

    public static Response createRequest(String TOKEN, User<Address> user){
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header("Authorization", TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %n%s", createUserResponse.asString());
        assertThat(createUserResponse.statusCode(), equalTo(200));
        return createUserResponse;
    }

    public static Response getRequest(String id, String TOKEN){
        Response getCreatedUserResponse = RestAssured.given().log().all()
                .pathParam("id", id)
                .header(AUTHORIZATION_HEADER, TOKEN)
                .get(GET_USER_PATH);
        System.out.printf("Get created user response: %n%s", getCreatedUserResponse.asString());
        assertThat(getCreatedUserResponse.statusCode(), equalTo(200));
        return getCreatedUserResponse;
    }

    public static Response updateRequest(String TOKEN, User<Address> user, String id){
        Response getUpdateUserResponse = RestAssured.given().log().all()
                .pathParam("id", id)
                .header("Content-Type", "application/json")
                .header("Authorization", TOKEN)
                .body(user)
                .put(UPDATE_USER_PATH);
        System.out.printf("Update user response: %n%s", getUpdateUserResponse.asString()); //to log
        assertThat(getUpdateUserResponse.statusCode(), equalTo(200));
        return getUpdateUserResponse;
    }

    public static void updateUser(String TOKEN, String id, User<Address> user){
        Response getUpdateUserResponse = RestAssured.given().log().all()
                .pathParam("id", id)
                .header("Content-Type", "application/json")
                .header("Authorization", TOKEN)
                .body(user)
                .put(UPDATE_USER_PATH);
        System.out.printf("Update user response: %n%s", getUpdateUserResponse.asString()); //to log
        assertThat(getUpdateUserResponse.statusCode(), equalTo(200));
    }
}
