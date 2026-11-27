package com.team01.ticketing.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PerformanceApiTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("INSERT INTO performance (id, title) VALUES (101, 'Test performance'), (102, 'No schedules')");
        jdbc.update("""
                INSERT INTO schedule (id, performance_id, start_at, status) VALUES
                (201, 101, '2026-12-10 19:00:00', 'OPEN'),
                (202, 101, '2026-12-10 14:00:00', 'OPEN')
                """);
        jdbc.update("""
                INSERT INTO seat (id, schedule_id, seat_name, grade, price) VALUES
                (301, 201, 'A1', 'VIP', 100000),
                (302, 202, 'A1', 'VIP', 100000)
                """);
        jdbc.update("""
                INSERT INTO member (id, email, password, name, role)
                VALUES (401, 'fixture@example.test', 'unused-placeholder', 'Test member', 'USER')
                """);
    }

    @Test
    void readsPerformancesAndReturnsNotFoundForMissingId() throws Exception {
        mvc.perform(get("/api/performances"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(101));
        mvc.perform(get("/api/v1/performances"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(101));
        mvc.perform(get("/api/v1/performances/101"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Test performance"));
        mvc.perform(get("/api/v1/performances/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletesPerformanceWithoutSchedules() throws Exception {
        mvc.perform(delete("/api/v1/performances/102")).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/performances/102")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/performances/102")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blocksDeletionWhenPerformanceHasSchedules() throws Exception {
        mvc.perform(delete("/api/v1/performances/101"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PERFORMANCE_HAS_SCHEDULES"));
        mvc.perform(get("/api/v1/performances/101/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void refusesDeletionForRegularMembers() throws Exception {
        mvc.perform(delete("/api/v1/performances/102")).andExpect(status().isForbidden());
    }

    @Test
    void listsSchedulesInTimeOrderAndDistinguishesAnEmptyList() throws Exception {
        mvc.perform(get("/api/v1/performances/101/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(202))
                .andExpect(jsonPath("$[1].id").value(201));
        mvc.perform(get("/api/v1/performances/102/schedules"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(get("/api/v1/performances/999999/schedules"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registersPerformancesAndSchedulesAndValidatesInputs() throws Exception {
        mvc.perform(post("/api/v1/performances").contentType("application/json").content("""
                {"id":101,"kopisId":"PF132236","title":"New performance","genre":"Play"}
                """)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.kopisId").doesNotExist())
                .andExpect(jsonPath("$.title").value("New performance"));
        mvc.perform(get("/api/v1/performances/101"))
                .andExpect(jsonPath("$.title").value("Test performance"));
        mvc.perform(post("/api/v1/performances").contentType("application/json").content("""
                {"title":""}
                """)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/performances").contentType("application/json").content("""
                {"title":"New performance","startDate":"2026-12-12","endDate":"2026-12-10"}
                """)).andExpect(status().isBadRequest());

        String schedule = """
                {"id":201,"performanceId":102,"startAt":"2026-12-12T19:00:00","status":"OPEN"}
                """;
        mvc.perform(post("/api/v1/performances/101/schedules").contentType("application/json").content(schedule))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.performanceId").value(101));
        mvc.perform(post("/api/v1/performances/999999/schedules").contentType("application/json").content(schedule))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/performances/102/schedules"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(post("/api/v1/performances/101/schedules").contentType("application/json").content("""
                {"status":"OPEN"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void anonymousUsersCannotRegisterPerformancesOrSchedules() throws Exception {
        mvc.perform(post("/api/v1/performances")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/performances/101/schedules")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void regularUsersCannotRegisterPerformancesOrSchedules() throws Exception {
        mvc.perform(post("/api/v1/performances")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/performances/101/schedules")).andExpect(status().isForbidden());
    }

    @Test
    void readsSeatAvailabilitySeparatelyForEachSchedule() throws Exception {
        String afternoon = "/api/v1/schedules/202/seats";
        String evening = "/api/v1/schedules/201/seats";

        mvc.perform(get(afternoon)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(302))
                .andExpect(jsonPath("$[0].seatName").value("A1"))
                .andExpect(jsonPath("$[0].price").value(100000))
                .andExpect(jsonPath("$[0].available").value(true));

        jdbc.update("""
                INSERT INTO reservation (id, member_id, schedule_id, status, reserved_at)
                VALUES (501, 401, 202, 'CONFIRMED', '2026-12-01 12:00:00')
                """);
        jdbc.update("""
                INSERT INTO reservation_seat (reservation_id, member_id, schedule_id, seat_id, active_seat_id, status)
                VALUES (501, 401, 202, 302, 302, 'CONFIRMED')
                """);
        mvc.perform(get(afternoon)).andExpect(jsonPath("$[0].available").value(false));
        mvc.perform(get(evening)).andExpect(jsonPath("$[0].seatName").value("A1"))
                .andExpect(jsonPath("$[0].available").value(true));

        jdbc.update("UPDATE reservation_seat SET active_seat_id = NULL, status = 'CANCELED' WHERE reservation_id = 501");
        mvc.perform(get(afternoon)).andExpect(jsonPath("$[0].available").value(true));
        mvc.perform(get("/api/v1/schedules/999999/seats")).andExpect(status().isNotFound());
    }

    @Test
    void removedRoutesReturnNotFound() throws Exception {
        mvc.perform(post("/api/v1/admin/performances")).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/admin/performances/kopis/PF132236")).andExpect(status().isNotFound());
    }
}
