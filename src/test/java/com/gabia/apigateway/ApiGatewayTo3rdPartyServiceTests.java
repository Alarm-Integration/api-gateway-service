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

    @Test
    void Alarm_Distribution_Service_로_정상_요청_성공_시_200_반환() throws JsonProcessingException {
        // given
        List<String> slackAddress = Arrays.asList("T13DA561", "U13DA561", "C13DA561");
        List<String> smsAddress = Arrays.asList("01012344321", "01037826481", "01027594837");
        List<String> emailAddress = Arrays.asList("test@gmail.com", "tes1@naver.com", "tes1@naver.com");

        Raw slackRaw = Raw.createRaw("slack", slackAddress);
        Raw emailRaw = Raw.createRaw("email", emailAddress);
        Raw smsRaw = Raw.createRaw("sms", smsAddress);

        List<Raw> raws = Arrays.asList(slackRaw, smsRaw, emailRaw);

        RequestAlarmCommon requestAlarmCommon = RequestAlarmCommon.createRequestAlarm(
                1L,
                "알림 제목",
                "알림 내용",
                Arrays.asList(1, 2, 3),
                raws
        );

        String request = new ObjectMapper().writeValueAsString(requestAlarmCommon);

        APIResponse apiResponse = new APIResponse("알람 전송 성공", null);
        String response = new ObjectMapper().writeValueAsString(apiResponse);

        // when
        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/alarm-distribution-service"))
                        .withRequestBody(equalToJson(request))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.OK)
                                .withBody(response))
        );

        // then
        client.post()
                .uri("/alarm-distribution-service")
                .body(Mono.just(request), String.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("알람 전송 성공")
                .jsonPath("$.result").isEmpty();
    }

    @Test
    void Alarm_Distribution_Service_로_잘못된_Request_로_요청시_400_반환() throws JsonProcessingException {
        // given
        List<String> slackAddress = Arrays.asList("T13DA561", "U13DA561", "C13DA561");
        List<String> smsAddress = Arrays.asList("01012344321", "01037826481", "01027594837");
        List<String> emailAddress = Arrays.asList("test@gmail.com", "tes1@naver.com", "tes1@naver.com");

        Raw slackRaw = Raw.createRaw("slack_ng", slackAddress);
        Raw emailRaw = Raw.createRaw("email_ng", emailAddress);
        Raw smsRaw = Raw.createRaw("sms_ng", smsAddress);

        List<Raw> raws = Arrays.asList(slackRaw, smsRaw, emailRaw);

        RequestAlarmCommon requestAlarmCommon = RequestAlarmCommon.createRequestAlarm(
                1L,
                "알림 제목",
                "알림 내용",
                Arrays.asList(1, 2, 3),
                raws
        );

        String request = new ObjectMapper().writeValueAsString(requestAlarmCommon);

        // when
        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/alarm-distribution-service"))
                        .withRequestBody(equalToJson(request))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.BAD_REQUEST))
        );

        // then
        client.post()
                .uri("/alarm-distribution-service")
                .body(Mono.just(request), String.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Void.class);
    }

    @Test
    void Alarm_Distribution_Service_로_인증_없이_API_Gateway_요청시_401_반환() {
        // when
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

        // then
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

    @Test
    void Alarm_Distribution_Service_로_잘못된_URL_로_요청시_404_반환() {
        // when
        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/alarm-distribution-service_ng"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.NOT_FOUND))
        );

        // then
        client.post()
                .uri("/alarm-distribution-service_ng")
                .exchange()
                .expectStatus().isNotFound();
    }


    // Email Endpoints
    /*
    - [POST] /verify-email/{email}
     */

    @Test
    void Email_Service_로_인증메일_발송_정상_요청_성공시_200_반환() throws JsonProcessingException {
        // given
        String request = "gabia@gabia.com";

        APIResponse apiResponse = new APIResponse("인증 메일 발송 완료", null);
        String response = new ObjectMapper().writeValueAsString(apiResponse);

        // when
        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo(String.format("/email-service/verify-email/%s", request)))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.OK)
                                .withBody(response))
        );

        // then
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

    @Test
    void Slack_Service_로_채널받아오기_정상_요청_성공시_200_반환() throws JsonProcessingException {
        // given
        String requestToken = "xoxb-2148325514801-2142207279172-ttsneJk3GUgXqkw3dtPPK5bS";

        List<Conversation> conversations = Arrays.asList(
                new Conversation("C024C9KFKHP", "프로젝트 팀"),
                new Conversation("C024CIAO11N", "공지 전파 방")
        );

        APIResponse apiResponse = new APIResponse("채널목록을 조회 했습니다.", conversations);
        String response = new ObjectMapper().writeValueAsString(apiResponse);

        // when
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/slack-service/channels"))
                        .withHeader("SLACK-TOKEN", equalTo(requestToken))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.OK)
                                .withBody(response))
        );

        // then
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

    @Test
    void Slack_Service_로_잘못된_Token_으로_요청시_401_반환() {
        // given
        String requestToken = "is-not-a-token";

        // when
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/slack-service/channels"))
                        .withHeader("SLACK-TOKEN", equalTo(requestToken))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.UNAUTHORIZED)
                                .withBody("잘못된 요청 입니다."))
        );

        // then
        client.get()
                .uri("/slack-service/channels")
                .header("SLACK-TOKEN", requestToken)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(Void.class);
    }
}

