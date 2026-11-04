/**
 * Author: HoangTLT
 */
package com.fuminihotel;

import com.fasterxml.jackson.databind.*;
import com.fuminihotel.entity.*;
import com.fuminihotel.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Real Spring Security/JWT, controllers, services and JPA. H2 is used only for automated tests.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class HotelApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CustomerRepository customers;
    @Autowired RoomRepository rooms;
    @Autowired RoomTypeRepository types;
    @Autowired BookingRepository bookings;
    @Autowired PasswordEncoder encoder;

    Customer alice;
    Customer bob;
    RoomInformation room1;
    RoomInformation room2;
    String staffToken;
    String aliceToken;
    String bobToken;
    LocalDate start;
    LocalDate end;

    @BeforeEach
    void setup() throws Exception {
        bookings.deleteAll();
        rooms.deleteAll();
        customers.deleteAll();
        alice = customer("alice@example.com");
        bob = customer("bob@example.com");
        room1 = room("201", "300000");
        room2 = room("202", "500000");
        start = LocalDate.now().plusDays(2);
        end = start.plusDays(2);
        staffToken = login("staff@fuminihotel.com", "Staff@123");
        aliceToken = login("alice@example.com", "Customer@123");
        bobToken = login("bob@example.com", "Customer@123");
    }

    @Test
    void publicRoomsAndRoleBoundaries() throws Exception {
        mvc.perform(get("/api/rooms")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/staff/customers")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/staff/customers").header("Authorization", aliceToken)).andExpect(status().isForbidden());
        mvc.perform(get("/api/customer/profile").header("Authorization", staffToken)).andExpect(status().isForbidden());
        mvc.perform(get("/api/customer/profile").header("Authorization", "Bearer broken-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void registrationValidatesAndHashesPassword() throws Exception {
        Map<String, Object> body = customerBody("new@example.com");
        body.put("customerStatus", 0);
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.customerStatus").value(1))
                .andExpect(jsonPath("$.password").doesNotExist());
        Customer saved = customers.findByEmailAddressIgnoreCase("new@example.com").orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("Customer@123");
        assertThat(encoder.matches("Customer@123", saved.getPassword())).isTrue();
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isConflict());
        body.put("emailAddress", "invalid");
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@example.com\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized());
    }

    @Test
    void multiRoomBookingUsesServerPricesAndJwtOwner() throws Exception {
        Map<String, Object> body = bookingBody(room1.getRoomId(), room2.getRoomId());
        body.put("customerId", bob.getCustomerId());
        MvcResult result = mvc.perform(post("/api/customer/bookings").header("Authorization", aliceToken)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.totalPrice").value(1600000))
                .andExpect(jsonPath("$.customerId").value(alice.getCustomerId().intValue()))
                .andExpect(jsonPath("$.bookingDetails.length()").value(2)).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).get("bookingReservationId").asLong();
        mvc.perform(get("/api/customer/bookings").header("Authorization", bobToken)).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/customer/bookings/" + id).header("Authorization", bobToken)).andExpect(status().isNotFound());
    }

    @Test
    void overlappingBookingRollsBackAndCheckoutDayIsReusable() throws Exception {
        createBooking(aliceToken, bookingBody(room2.getRoomId()));
        mvc.perform(post("/api/customer/bookings").header("Authorization", bobToken).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(bookingBody(room1.getRoomId(), room2.getRoomId())))).andExpect(status().isConflict());
        assertThat(bookings.count()).isEqualTo(1);
        mvc.perform(get("/api/rooms").param("startDate", start.toString()).param("endDate", end.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        start = end; end = end.plusDays(1);
        createBooking(bobToken, bookingBody(room2.getRoomId()));
        assertThat(bookings.count()).isEqualTo(2);
    }

    @Test
    void concurrentBookingAllowsOnlyOneReservation() throws Exception {
        String body = json.writeValueAsString(bookingBody(room1.getRoomId()));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        try {
            Callable<Integer> task = () -> {
                ready.countDown(); go.await(5, TimeUnit.SECONDS);
                return mvc.perform(post("/api/customer/bookings").header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse().getStatus();
            };
            Future<Integer> first = executor.submit(task);
            Future<Integer> second = executor.submit(task);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue(); go.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
            assertThat(bookings.count()).isEqualTo(1);
        } finally { executor.shutdownNow(); }
    }

    @Test
    void deletingRoomPreservesBookedRoomAndRemovesUnusedRoom() throws Exception {
        createBooking(aliceToken, bookingBody(room1.getRoomId()));
        mvc.perform(delete("/api/staff/rooms/" + room1.getRoomId()).header("Authorization", staffToken)).andExpect(status().isOk());
        assertThat(rooms.findById(room1.getRoomId()).orElseThrow().getRoomStatus()).isZero();
        mvc.perform(get("/api/rooms/" + room1.getRoomId())).andExpect(status().isNotFound());
        mvc.perform(delete("/api/staff/rooms/" + room2.getRoomId()).header("Authorization", staffToken)).andExpect(status().isOk());
        assertThat(rooms.existsById(room2.getRoomId())).isFalse();
        mvc.perform(get("/api/customer/bookings").header("Authorization", aliceToken)).andExpect(jsonPath("$[0].bookingDetails.length()").value(1));
    }

    @Test
    void staffCanEditDetailsCancelAndDeleteBooking() throws Exception {
        long id = createBooking(aliceToken, bookingBody(room1.getRoomId()));
        Map<String, Object> updated = bookingBody(room1.getRoomId(), room2.getRoomId());
        updated.put("customerId", alice.getCustomerId());
        mvc.perform(put("/api/staff/bookings/" + id).header("Authorization", staffToken).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(updated))).andExpect(status().isOk()).andExpect(jsonPath("$.totalPrice").value(1600000));
        updated = bookingBody(room2.getRoomId()); updated.put("customerId", alice.getCustomerId());
        mvc.perform(put("/api/staff/bookings/" + id).header("Authorization", staffToken).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(updated))).andExpect(status().isOk()).andExpect(jsonPath("$.bookingDetails.length()").value(1));
        mvc.perform(delete("/api/staff/bookings/" + id).header("Authorization", staffToken)).andExpect(status().isConflict());
        mvc.perform(patch("/api/staff/bookings/" + id + "/status").header("Authorization", staffToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"bookingStatus\":\"COMPLETED\"}")).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/staff/bookings/" + id + "/status").header("Authorization", staffToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"bookingStatus\":\"CANCELLED\"}")).andExpect(status().isOk());
        createBooking(bobToken, bookingBody(room2.getRoomId()));
        mvc.perform(delete("/api/staff/bookings/" + id).header("Authorization", staffToken)).andExpect(status().isNoContent());
        assertThat(bookings.existsById(id)).isFalse();
    }

    @Test
    void disabledCustomerTokenIsRejectedAndHistoryPreserved() throws Exception {
        createBooking(aliceToken, bookingBody(room1.getRoomId()));
        mvc.perform(delete("/api/staff/customers/" + alice.getCustomerId()).header("Authorization", staffToken)).andExpect(status().isNoContent());
        assertThat(customers.findById(alice.getCustomerId()).orElseThrow().getCustomerStatus()).isZero();
        mvc.perform(get("/api/customer/profile").header("Authorization", aliceToken)).andExpect(status().isUnauthorized());
        assertThat(bookings.count()).isEqualTo(1);
    }

    @Test
    void profileCannotChangeOwnStatusOrOtherCustomers() throws Exception {
        Map<String, Object> body = customerBody("alice-new@example.com");
        body.put("customerStatus", 0); body.put("password", "");
        mvc.perform(put("/api/customer/profile").header("Authorization", aliceToken).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body))).andExpect(status().isOk()).andExpect(jsonPath("$.customerStatus").value(1));
        assertThat(customers.findById(bob.getCustomerId()).orElseThrow().getEmailAddress()).isEqualTo("bob@example.com");
        assertThat(login("alice-new@example.com", "Customer@123")).startsWith("Bearer ");
    }

    @Test
    void duplicateRoomsAndInvalidDatesAreRejected() throws Exception {
        mvc.perform(post("/api/customer/bookings").header("Authorization", aliceToken).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(bookingBody(room1.getRoomId(), room1.getRoomId())))).andExpect(status().isBadRequest());
        end = start;
        mvc.perform(post("/api/customer/bookings").header("Authorization", aliceToken).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(bookingBody(room1.getRoomId())))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/rooms").param("startDate", start.toString())).andExpect(status().isBadRequest());
        assertThat(bookings.count()).isZero();
    }

    @Test
    void staffRoomCrudAndInputValidation() throws Exception {
        Map<String, Object> body = new HashMap<>(Map.of("roomNumber", "301", "roomDetailDescription", "Basic room",
                "roomMaxCapacity", 2, "roomTypeId", types.findAll().getFirst().getRoomTypeId(),
                "roomStatus", 1, "roomPricePerDay", 400000));
        MvcResult result = mvc.perform(post("/api/staff/rooms").header("Authorization", staffToken)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body))).andExpect(status().isCreated()).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).get("roomId").asLong();
        body.put("roomPricePerDay", 450000);
        mvc.perform(put("/api/staff/rooms/" + id).header("Authorization", staffToken).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body))).andExpect(status().isOk()).andExpect(jsonPath("$.roomPricePerDay").value(450000));
        body.put("roomMaxCapacity", 0);
        mvc.perform(put("/api/staff/rooms/" + id).header("Authorization", staffToken).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/staff/rooms").header("Authorization", staffToken).param("keyword", "301"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    private Customer customer(String email) {
        Customer c = new Customer(); c.setCustomerFullName(email); c.setEmailAddress(email); c.setTelephone("0901234567");
        c.setCustomerBirthday(LocalDate.of(2000, 1, 1)); c.setCustomerStatus(1); c.setPassword(encoder.encode("Customer@123"));
        return customers.save(c);
    }

    private RoomInformation room(String number, String price) {
        RoomInformation r = new RoomInformation(); r.setRoomNumber(number); r.setRoomMaxCapacity(2);
        r.setRoomType(types.findAll().getFirst()); r.setRoomStatus(1); r.setRoomPricePerDay(new BigDecimal(price));
        return rooms.save(r);
    }

    private Map<String, Object> customerBody(String email) {
        return new HashMap<>(Map.of("customerFullName", "Test Customer", "telephone", "0901234567", "emailAddress", email,
                "customerBirthday", "2000-01-01", "customerStatus", 1, "password", "Customer@123"));
    }

    private Map<String, Object> bookingBody(Long... roomIds) {
        return new HashMap<>(Map.of("rooms", Arrays.stream(roomIds).map(id ->
                Map.of("roomId", id, "startDate", start.toString(), "endDate", end.toString())).toList()));
    }

    private long createBooking(String token, Map<String, Object> body) throws Exception {
        MvcResult result = mvc.perform(post("/api/customer/bookings").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body))).andExpect(status().isCreated()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("bookingReservationId").asLong();
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "password", password)))).andExpect(status().isOk()).andReturn();
        return "Bearer " + json.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
