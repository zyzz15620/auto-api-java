# API Automation Testing Framework - Learning Project

This project demonstrates how I developed an API automation framework for verifying simple APIs using RestAssured, testing dependent APIs with WireMock, and validating database consistency with Hibernate.

## Key Features
- **API Verification**: Automated functional testing of simple RESTful APIs using RestAssured.
- **Mock APIs**: Utilized WireMock to handle dependent APIs for more reliable testing scenarios.
- **Database Validation**: Integrated Hibernate to verify database changes after API requests, ensuring data integrity.
- **Environment Setup**: Learned how to set up `.env` files to manage environment variables efficiently.

## Discoveries and Lessons Learned
1. **Advanced JSON Comparison**:
    - Used **Junit** and **ObjectMapper** for absolute JSON comparison:
      ```java
      assertEquals(objectMapper.readTree(expectJsonString), objectMapper.readTree(actualJsonString));
      ```
    - Discovered that **Hamcrest** with **JsonUnit** allows for more flexible comparisons, such as ignoring array order:
      ```java
      assertThat(actualJsonString, jsonEquals(expectJsonString).when(Option.IGNORING_ARRAY_ORDER));
      ```
2. **URL Encoding**: Resolved issues with encoding special characters (like `==`) by using `.queryParam()` instead of `.pathParam()`.
3. **Singleton Pattern**: Applied the **Singleton design pattern** to manage multiple stub servers, preventing conflicts from using the same port:
    ```java
    private static WireMockServer wireMockServer;
    
    public static WireMockServer getInstance() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(8080);
        }
        return wireMockServer;
    }
    ```
4. **Lombok**: Adopted **Lombok** to simplify class constructors, especially when dealing with numerous variables:
    ```java
    @Data
    @AllArgsConstructor
    public class ApiResponse {
        private String status;
        private String message;
        private List<Object> data;
    }
    ```
5. **Java 17**: Discovered that Java 17 allows embedding JSON strings directly using triple quotes (`"""json"""`), eliminating the need for separate JSON files in expected data.
6. **Authorization Headers**: Realized that **Authorization** is part of the request header, unlike Postman where it's separated into a different tab.
7. **Date Handling**: Found **Instant** class to be superior to **DateTime** when dealing with ISO8601 date formats:
    ```java
    Instant timestamp = Instant.parse("2023-01-01T00:00:00Z");
    ```

## Future Enhancements
- **Improved Assertions**: Explore more features of Hamcrest for deeper API body assertions.
- **CI/CD Integration**: Set up continuous integration pipelines to automate tests on code changes.


