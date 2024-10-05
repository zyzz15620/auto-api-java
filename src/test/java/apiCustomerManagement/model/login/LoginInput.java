package apiCustomerManagement.model.login;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static apiCustomerManagement.common.ConfigUtils.getDotenv;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginInput {
    private String username = getDotenv().get("username");
    private String password = getDotenv().get("password");
}
