package api.data;

import api.model.user.Address;
import api.model.user.User;
import api.model.user.ValidationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UserData {
    public static Stream<Arguments> validationUserProvider() throws JsonProcessingException {
        List<Arguments> argumentsList = new ArrayList<>();
        User<Address> user = User.getDefaultWithEmail();
        Address address;  //tại sao nó hiểu mặc định là dùng getDefault()?

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
}
