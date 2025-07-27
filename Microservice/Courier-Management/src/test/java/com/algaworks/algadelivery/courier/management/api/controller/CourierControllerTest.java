package com.algaworks.algadelivery.courier.management.api.controller;

import com.algaworks.algadelivery.courier.management.domain.model.Courier;
import com.algaworks.algadelivery.courier.management.domain.repository.CourierRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CourierControllerTest {

    @Autowired
    CourierRepository courierRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/couriers";
    }

    @Test
    public void shouldCreateCourier() {
        String requestBody = """ 
                 {
                     "name": "John Doe",
                     "phone": "81999999999"
                 }
                """;
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("John Doe"))
                .body("phone", Matchers.equalTo("81999999999"));
    }

    @Test
    void shouldReturn200() {
        UUID courierId = courierRepository.saveAndFlush(Courier.brandNew("Maria Silva", "81977777777")).getId();

        RestAssured
                .given()
                .pathParam("courierId", courierId)
                .accept(ContentType.JSON)
                .when()
                .get("/{courierId}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", Matchers.equalTo(courierId.toString()))
                .body("name", Matchers.equalTo("Maria Silva"))
                .body("phone", Matchers.equalTo("81977777777"));
    }

    @Test
    void shouldReturn404WhenCourierDoesNotExist() {
        UUID randomId = UUID.randomUUID();

        RestAssured
                .given()
                .pathParam("courierId", randomId)
                .accept(ContentType.JSON)
                .when()
                .get("/{courierId}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldUpdateCourierSuccessfully() {
        UUID courierId = courierRepository.saveAndFlush(Courier.brandNew("Ana Paula", "81988888888")).getId();

        String requestBody = """
                 {
                     "name": "Ana Paula Atualizada",
                     "phone": "81988888888"
                 }
                """;

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .pathParam("courierId", courierId)
                .when()
                .put("/{courierId}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", Matchers.equalTo(courierId.toString()))
                .body("name", Matchers.equalTo("Ana Paula Atualizada"))
                .body("phone", Matchers.equalTo("81988888888"));
    }

    @Test
    void shouldReturnPagedCouriers() {
        courierRepository.saveAndFlush(Courier.brandNew("Carlos", "81966666666"));
        courierRepository.saveAndFlush(Courier.brandNew("Fernanda", "81955555555"));

        RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.size()", Matchers.greaterThanOrEqualTo(2));
    }

    @Test
    void shouldCalculatePayoutSuccessfully() {
        String requestBody = """
                {
                    "distanceInKm": 12.5
                }
                """;

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/payout-calculation")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("payoutFee", Matchers.notNullValue());
    }

}