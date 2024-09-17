package api.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User<T> {
    private String firstName;
    private String lastName;
    private String middleName;
    private String birthday;
    private String email;
    private String phone;
    private List<T> addresses;

    public static User<Address> getDefault() {
        User<Address> user = new User<Address>();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setMiddleName("Smith");
        user.setBirthday("01-23-2000");
        user.setPhone("0123456788");
        return user;
    }
    public static User<Address> getDefaultWithEmail(){
        User<Address> user = getDefault();
        user.setEmail(String.format("auto_api_%s@abc.com", System.currentTimeMillis()));
        return user;
    }

    @Override
    public String toString() {
        return email;
    }

}

