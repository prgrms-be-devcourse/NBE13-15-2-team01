package com.team01.ticketing.reservation;

import com.team01.ticketing.global.security.JwtService;
import com.team01.ticketing.member.entity.Role;
import com.team01.ticketing.reservation.entity.Reservation;
import com.team01.ticketing.reservation.repository.ReservationRepository;
import com.team01.ticketing.support.TestClocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/reservation-fixture.sql")
class ReservationControllerTest {

    // 현재 시각 12/1 12:00 → 12/8 회차들은 예매 오픈(12/1 00:00) 지남 + 시작 전
    // 회차 1(OPEN)은 예매 가능, 회차 2는 마감(CLOSED) 상태로만 막힘
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 12, 1, 12, 0);
    private static final Long MEMBER_ID = 1L;

    // 스프링이 만드는 예매 정책에 고정 시계가 들어가도록 Clock 빈을 교체 (이 클래스에만 적용, 다른 통합 테스트는 실제 시계 그대로)
    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock testClock() {
            return TestClocks.fixedAt(NOW);
        }
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    JwtService jwtService;
    @Autowired
    ReservationRepository reservationRepository;

    String accessToken;

    @BeforeEach
    void setUp() {
        // 진짜 로그인처럼 JWT 필터를 통과할 access 토큰 발급
        accessToken = jwtService.createAccess(MEMBER_ID, Role.USER);
    }

    @Test
    @DisplayName("예매에 성공하면 201과 좌석 ID 순 예매 정보를 돌려주고, 토큰의 회원으로 저장된다")
    void reserve() throws Exception {
        reserve("""
                {"scheduleId": 1, "seatIds": [2, 1]}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.seats[0].seatId").value(1))
                .andExpect(jsonPath("$.seats[1].seatId").value(2))
                .andExpect(jsonPath("$.totalPrice").value(260000));

        assertThat(reservationRepository.findAll())
                .extracting(Reservation::getMemberId)
                .containsExactly(MEMBER_ID);
    }

    @Test
    @DisplayName("이미 예매된 좌석이 하나라도 있으면 전체 실패하고 409 SEAT_ALREADY_RESERVED")
    void seatAlreadyReserved() throws Exception {
        reserve("""
                {"scheduleId": 1, "seatIds": [1]}
                """).andExpect(status().isCreated());

        reserve("""
                {"scheduleId": 1, "seatIds": [1, 2]}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SEAT_ALREADY_RESERVED"));
    }

    @Test
    @DisplayName("마감된 회차를 예매하면 409 SCHEDULE_NOT_BOOKABLE")
    void closedSchedule() throws Exception {
        reserve("""
                {"scheduleId": 2, "seatIds": [3]}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SCHEDULE_NOT_BOOKABLE"));
    }

    @Test
    @DisplayName("다른 회차의 좌석을 섞으면 404 SEAT_NOT_FOUND")
    void seatOfOtherSchedule() throws Exception {
        reserve("""
                {"scheduleId": 1, "seatIds": [1, 3]}
                """)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SEAT_NOT_FOUND"));
    }

    @Test
    @DisplayName("좌석을 선택하지 않으면 400 VALIDATION_ERROR와 seatIds")
    void emptySeats() throws Exception {
        reserve("""
                {"scheduleId": 1, "seatIds": []}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.seatIds").exists());
    }

    @Test
    @DisplayName("토큰 없이 요청하면 401 UNAUTHORIZED")
    void unauthenticated() throws Exception {
        mvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"scheduleId": 1, "seatIds": [1]}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    // 로그인한 회원(토큰)으로 예매 요청
    private ResultActions reserve(String json) throws Exception {
        return mvc.perform(post("/api/v1/reservations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
