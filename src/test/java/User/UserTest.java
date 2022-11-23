package User;

import Entites.User;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.basePath;
import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTest {

    private static User user;

    public static Faker faker;
    public static RequestSpecification request;

    @BeforeAll
    public static void setup(){
        RestAssured.baseURI = "http://petstore.swagger.io/v2";

        faker = new Faker();

        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8,10),
                faker.phoneNumber().toString());
    }
    @BeforeEach
        void setRequest(){
        request = given()
                .header("api-key", "special-key")
                .contentType(ContentType.JSON);
    }
    @Test
    @Order(1)
    public void CreateNewUser_withValidData_ReturnOK(){

        request
                .body(user)
                .when()
                .post("/user")
                .then()
                .assertThat().statusCode(200).and()
                .body("code", equalTo(200))
                .body("type", equalTo("unknown"))
                .body("message",isA(String.class))
                .body("size()", equalTo(3));
    }
    @Test
    @Order(2)
    public void GetLogin_ValidUser_ReturnOK{

        request
                .param("username", user.getUsername())
                .param("password", user.getPassword())
                .when()
                .get("/user/login")
                .then()
                .assertThat()
                .statusCode(200)
                .and().time(lessThan(2000L))
                .and().body(matchesJsonSchemaInClasspath("LoginResponseSchema.json"));
    }
    @Test
    @Order(3)
    public void GetUserByUsername_userIsValid_ReturnOK(){

        request
                .when()
                .get("/user/" + user.getUsername())
                .then()
                .assertThat().statusCode(200).and().time(lessThan(2000L))
                .and().body("firstName", equalTo(user.getFirstName()));
    }
    @Test
    @Order(4)
    public void DeleteUser_UserExists_ReturnOK(){

        request
                .when()
                .delete("/user" + user.getUsername())
                .then()
                .assertThat().statusCode(200).and().time(lessThan(2000L))
                .log();
    }

}
