package apiCustomerManagement.common;

import apiCustomerManagement.model.login.LoginInput;
import apiCustomerManagement.model.login.LoginResponse;
import apiCustomerManagement.model.user.Address;
import apiCustomerManagement.model.user.GetAddressResponse;
import apiCustomerManagement.model.user.GetUserResponse;
import apiCustomerManagement.model.user.User;
import apiCustomerManagement.model.user.dto.DbAddress;
import apiCustomerManagement.model.user.dto.DbUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hibernate.Session;

import java.time.Instant;
import java.util.*;

import static apiCustomerManagement.common.ConstantUtils.*;
import static apiCustomerManagement.common.LoginUtils.login;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MethodUtils {
    public static void datetimeVerifier(Instant timeBeforeExecution, String time) {
        Instant timeInstant = Instant.parse(time);
        assertThat(timeInstant.isAfter(timeBeforeExecution), equalTo(true));
        assertThat(timeInstant.isBefore(Instant.now()), equalTo(true));
    }

    public static String getRandomEmail(){
        return String.format("auto_api_%s@abc.com", System.currentTimeMillis());
    }

    public static <T> boolean areListsDisjoint(List<T> list1, List<T> list2) {
        Set<T> set1 = new HashSet<>(list1);
        Set<T> set2 = new HashSet<>(list2);
        return Collections.disjoint(set1, set2);
    }

    public static Response getStaffLoginResponse(String username, String password) {
        LoginInput loginInput = new LoginInput(username, password);
        return RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(loginInput)
                .post(LOGIN_PATH);
    }

    public static Response getStaffLoginResponse() {
        LoginInput loginInput = new LoginInput();
        return RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(loginInput)
                .post(LOGIN_PATH);
    }

    public static LoginResponse loginResponse(){
        return login("staff", "1234567890");
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

    public static void deleteUser(String TOKEN, String id){
        Response deleteUserResponse = RestAssured.given().log().all()
                .pathParam("id", id)
                .header(AUTHORIZATION_HEADER, TOKEN)
                .delete(DELETE_USER_PATH);
        System.out.printf("Delete user response: %n%s", deleteUserResponse.asString());
        assertThat(deleteUserResponse.statusCode(), equalTo(200));
    }

    public static GetUserResponse<GetAddressResponse> getUserFromDB (String id, String TOKEN, ObjectMapper objectMapper, Session session){
        DbUser dbUser = session.createSelectionQuery("from DbUser where id=:id", DbUser.class)
                .setParameter("id", UUID.fromString(id))
                .getSingleResult();
        List<DbAddress> dbAddresses = session.createSelectionQuery("from DbAddress where customerId=:customerId", DbAddress.class)
                .setParameter("customerId", UUID.fromString(id))
                .getResultList();
        GetUserResponse<GetAddressResponse> actualUser = objectMapper.convertValue(dbUser, new TypeReference<>() {});
        List<GetAddressResponse> actualAddress = objectMapper.convertValue(dbAddresses, new TypeReference<>() {});
        actualUser.setAddresses(actualAddress);
        return actualUser;
    }


}
