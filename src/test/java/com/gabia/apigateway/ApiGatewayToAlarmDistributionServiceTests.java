package com.gabia.apigateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabia.apigateway.request.Raw;
import com.gabia.apigateway.request.RequestAlarmCommon;
import com.gabia.apigateway.response.APIResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;

class ApiGatewayToAlarmDistributionServiceTests {

    private static final WireMockServer wireMockServer = new WireMockServer(WireMockSpring.options().port(80));

    private WebTestClient client;

    @BeforeAll
    static void beforeAll() {
        wireMockServer.start();
    }

    @BeforeEach
    void beforeEach() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:80").build();
    }

    @AfterEach
    void afterEach() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.shutdown();
    }

    @DisplayName("정상 요청 성공 시 200 반환")
    @Test
    void Test_Request_API_Gateway_To_Send() throws JsonProcessingException {
        // Request Entity 생성
        RequestAlarmCommon requestAlarmCommon = new RequestAlarmCommon();
        requestAlarmCommon.setGroupId(1L);
        requestAlarmCommon.setTitle("알림 제목");
        requestAlarmCommon.setContent("알림 내용");
        requestAlarmCommon.setBookmarks(Arrays.asList(1,2,3));
        requestAlarmCommon.setRaws(new ArrayList<>() {
            {
                Raw raw = new Raw();
                raw.setAppName("slack");
                raw.setAddress(Arrays.asList("U1234", "U4321"));
                add(raw);

                raw.setAppName("email");
                raw.setAddress(Arrays.asList("test@gmail.com", "test@naver.com"));
                add(raw);

                raw.setAppName("sms");
                raw.setAddress(Arrays.asList("01012341234", "01043214321"));
                add(raw);
            }
        });

        String request = new ObjectMapper().writeValueAsString(requestAlarmCommon);

        // Response Entity 생성
        APIResponse apiResponse = new APIResponse("알람 전송 성공", null);
        String response = new ObjectMapper().writeValueAsString(apiResponse);


        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/alarm-distribution-service"))
                        .withRequestBody(equalToJson(request))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.OK)
                                .withBody(response))
        );

        client.post()
                .uri("/alarm-distribution-service")
                .body(Mono.just(request), String.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("알람 전송 성공")
                .jsonPath("$.result").isEmpty();
    }

    @DisplayName("잘못된 Request 로 요청시 400 반환")
    @Test
    void Test_Request_API_Gateway_To_Send_Wrong_Request() throws JsonProcessingException {
        // Request Entity 생성
        RequestAlarmCommon requestAlarmCommon = new RequestAlarmCommon();
        requestAlarmCommon.setGroupId(0L);
        requestAlarmCommon.setTitle("알림 제목");
        requestAlarmCommon.setContent("알림 내용");
        requestAlarmCommon.setBookmarks(Arrays.asList(1,2,3));
        requestAlarmCommon.setRaws(new ArrayList<>() {
            {
                Raw raw = new Raw();
                raw.setAppName("slack_ng");
                raw.setAddress(Arrays.asList("U1234", "U4321"));
                add(raw);

                raw.setAppName("email_ng");
                raw.setAddress(Arrays.asList("test@gmail.com", "test@naver.com"));
                add(raw);

                raw.setAppName("sms_ng");
                raw.setAddress(Arrays.asList("01012341234", "01043214321"));
                add(raw);
            }
        });

        String request = new ObjectMapper().writeValueAsString(requestAlarmCommon);

        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/alarm-distribution-service"))
                        .withRequestBody(equalToJson(request))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.BAD_REQUEST))
        );

        client.post()
                .uri("/alarm-distribution-service")
                .body(Mono.just(request), String.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Void.class);
    }

    @DisplayName("인증 없이 API Gateway 요청시 401 반환")
    @Test
    void Test_Request_API_Gateway_To_Send_Without_Authenticated() {
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/alarm-distribution-service"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.UNAUTHORIZED))
        );
        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/alarm-distribution-service"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.UNAUTHORIZED))
        );
        wireMockServer.stubFor(
                WireMock.put(WireMock.urlEqualTo("/alarm-distribution-service"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.UNAUTHORIZED))
        );
        wireMockServer.stubFor(
                WireMock.delete(WireMock.urlEqualTo("/alarm-distribution-service"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.UNAUTHORIZED))
        );


        client.get()
                .uri("/alarm-distribution-service")
                .exchange()
                .expectStatus().isUnauthorized();
        client.post()
                .uri("/alarm-distribution-service")
                .exchange()
                .expectStatus().isUnauthorized();
        client.put()
                .uri("/alarm-distribution-service")
                .exchange()
                .expectStatus().isUnauthorized();
        client.delete()
                .uri("/alarm-distribution-service")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @DisplayName("잘못된 URL 로 요청시 404 반환")
    @Test
    void Test_Request_API_Gateway_To_Send_Wrong_URL() {
        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/alarm-distribution-service_ng"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.NOT_FOUND))
        );
        client.post()
                .uri("/alarm-distribution-service_ng")
                .exchange()
                .expectStatus().isNotFound();
    }
}

