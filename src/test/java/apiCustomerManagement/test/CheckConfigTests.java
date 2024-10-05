package apiCustomerManagement.test;

import org.junit.jupiter.api.Test;
import static apiCustomerManagement.common.ConfigUtils.getDotenv;

public class CheckConfigTests {
    @Test
    void checkConfig() {
        System.out.println(getDotenv().get("host"));
    }
}
