package com.gabia.apigateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabia.apigateway.request.Raw;
import com.gabia.apigateway.request.RequestAlarmCommon;
import com.gabia.apigateway.response.APIResponse;
import com.gabia.apigateway.response.Conversation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;

class ApiGatewayTo3rdPartyServiceTests {

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

    @DisplayName("Alarm-Distribution-Service 로 정상 요청 성공 시 200 반환")
    @Test
    void Test_Request_API_Gateway_To_Send_Alarm_Distribution() throws JsonProcessingException {
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
                raw.setAddresses(Arrays.asList("U1234", "U4321"));
                add(raw);

                raw.setAppName("email");
                raw.setAddresses(Arrays.asList("test@gmail.com", "test@naver.com"));
                add(raw);

                raw.setAppName("sms");
                raw.setAddresses(Arrays.asList("01012341234", "01043214321"));
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

    @DisplayName("Alarm-Distribution-Service 로 잘못된 Request 로 요청시 400 반환")
    @Test
    void Test_Request_API_Gateway_To_Send_Wrong_Request_Alarm_Distribution() throws JsonProcessingException {
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
                raw.setAddresses(Arrays.asList("U1234", "U4321"));
                add(raw);

                raw.setAppName("email_ng");
                raw.setAddresses(Arrays.asList("test@gmail.com", "test@naver.com"));
                add(raw);

                raw.setAppName("sms_ng");
                raw.setAddresses(Arrays.asList("01012341234", "01043214321"));
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

    @DisplayName("Alarm-Distribution-Service 로 인증 없이 API Gateway 요청시 401 반환")
    @Test
    void Test_Request_API_Gateway_To_Send_Without_Authenticated_Alarm_Distribution() {
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

    @DisplayName("Alarm-Distribution-Service 로 잘못된 URL 로 요청시 404 반환")
    @Test
    void Test_Request_API_Gateway_To_Send_Wrong_URL_Alarm_Distribution() {
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





    // Email Endpoints
    /*
    - [POST] /verify-email/{email}
     */

    @DisplayName("Email-Service 로 정상 요청 성공 시 200 반환")
    @Test
    void Test_Request_API_Gateway_To_Verify_Email() throws JsonProcessingException {
        // Request Entity 생성
        String request = "gabia@gabia.com";

        // Response Entity 생성
        APIResponse apiResponse = new APIResponse("인증 메일 발송 완료", null);
        String response = new ObjectMapper().writeValueAsString(apiResponse);


        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo(String.format("/email-service/verify-email/%s", request)))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.OK)
                                .withBody(response))
        );

        client.post()
                .uri("/email-service/verify-email/gabia@gabia.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("인증 메일 발송 완료")
                .jsonPath("$.result").isEmpty();
    }





    // Slack Endpoints
    /*
    - [GET] /channels
     */

    @DisplayName("Slack-Service 로 정상 요청 성공 시 200 반환")
    @Test
    void Test_Request_API_Gateway_To_Get_Channels_Slack() throws JsonProcessingException {
        // Request Entity 생성
        String requestToken = "xoxb-2148325514801-2142207279172-ttsneJk3GUgXqkw3dtPPK5bS";

        // Response Entity 생성
        List<Conversation> conversations = Arrays.asList(
                new Conversation("C024C9KFKHP", "프로젝트 팀"),
                new Conversation("C024CIAO11N", "공지 전파 방")
        );

        APIResponse apiResponse = new APIResponse("채널목록을 조회 했습니다.", conversations);
        String response = new ObjectMapper().writeValueAsString(apiResponse);


        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/slack-service/channels"))
                        .withHeader("SLACK-TOKEN", equalTo(requestToken))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.OK)
                                .withBody(response))
        );

        client.get()
                .uri("/slack-service/channels")
                .header("SLACK-TOKEN", requestToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("채널목록을 조회 했습니다.")
                .jsonPath("$.result[0].id").isEqualTo("C024C9KFKHP")
                .jsonPath("$.result[0].name").isEqualTo("프로젝트 팀")
                .jsonPath("$.result[1].id").isEqualTo("C024CIAO11N")
                .jsonPath("$.result[1].name").isEqualTo("공지 전파 방");
    }

    @DisplayName("Slack-Service 로 잘못된 Token 으로 요청시 401 반환")
    @Test
    void Test_Request_API_Gateway_To_Send_Wrong_Request_Slack() {

        // Request Entity 생성
        String requestToken = "is-not-a-token";

        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/slack-service/channels"))
                        .withHeader("SLACK-TOKEN", equalTo(requestToken))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.UNAUTHORIZED)
                                .withBody("잘못된 요청 입니다."))
        );

        client.get()
                .uri("/slack-service/channels")
                .header("SLACK-TOKEN", requestToken)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(Void.class);
    }
}

