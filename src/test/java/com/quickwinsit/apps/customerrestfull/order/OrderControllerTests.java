/*
 * MIT License
 *
 * Copyright (c) [2016-2021] [Ivan Claudio Fernandes]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.quickwinsit.apps.customerrestfull.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickwinsit.apps.customerrestfull.controller.order.OrderController;
import com.quickwinsit.apps.customerrestfull.model.order.Order;
import com.quickwinsit.apps.customerrestfull.model.order.OrderModelAssembler;
import com.quickwinsit.apps.customerrestfull.model.order.OrderRepository;
import com.quickwinsit.apps.customerrestfull.model.order.Status;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@WebMvcTest(OrderController.class)
@Import(OrderModelAssembler.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper mapper;

    private List<Order> getOrderData() {
        return Arrays.asList(
                new Order(3L, "MacBook Pro", Status.COMPLETED),
                new Order(4L, "iPhone", Status.IN_PROGRESS),
                new Order(5L, "iPad Pro", Status.CANCELLED)
        );
    }

    private void checkAllOrderRecordsJson(final ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(jsonPath("$._embedded.orderList[0].id", is(3)))
                .andExpect(jsonPath("$._embedded.orderList[0].description", is("MacBook Pro")))
                .andExpect(jsonPath("$._embedded.orderList[0].status", is("COMPLETED")))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.self.href", is("http://localhost/orders/3")))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.orders.href", is("http://localhost/orders")))
                .andExpect(jsonPath("$._embedded.orderList[1].id", is(4)))
                .andExpect(jsonPath("$._embedded.orderList[1].description", is("iPhone")))
                .andExpect(jsonPath("$._embedded.orderList[1].status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$._embedded.orderList[1]._links.self.href", is("http://localhost/orders/4")))
                .andExpect(jsonPath("$._embedded.orderList[1]._links.orders.href", is("http://localhost/orders")))
                .andExpect(jsonPath("$._embedded.orderList[1]._links.cancel.href", is("http://localhost/orders/4/cancel")))
                .andExpect(jsonPath("$._embedded.orderList[1]._links.complete.href", is("http://localhost/orders/4/complete")))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/orders")))
                .andExpect(jsonPath("$._embedded.orderList[2].id", is(5)))
                .andExpect(jsonPath("$._embedded.orderList[2].description", is("iPad Pro")))
                .andExpect(jsonPath("$._embedded.orderList[2].status", is("CANCELLED")))
                .andExpect(jsonPath("$._embedded.orderList[2]._links.self.href", is("http://localhost/orders/5")))
                .andExpect(jsonPath("$._embedded.orderList[2]._links.orders.href", is("http://localhost/orders")));
    }

    private void checkSingleOrderJson(final ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(jsonPath("id", is(3)))
                .andExpect(jsonPath("description", is("MacBook Pro")))
                .andExpect(jsonPath("status", is("COMPLETED")))
                .andExpect(jsonPath("_links.self.href", is("http://localhost/orders/3")))
                .andExpect(jsonPath("_links.orders.href", is("http://localhost/orders")));
    }

    private void checkSingleOrderCancelled(final ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(jsonPath("id", is(4)))
                .andExpect(jsonPath("description", is("iPhone")))
                .andExpect(jsonPath("status", is("CANCELLED")))
                .andExpect(jsonPath("_links.self.href", is("http://localhost/orders/4")))
                .andExpect(jsonPath("_links.orders.href", is("http://localhost/orders")));
    }

    private void checkSingleOrderCompleted(final ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(jsonPath("id", is(4)))
                .andExpect(jsonPath("description", is("iPhone")))
                .andExpect(jsonPath("status", is("COMPLETED")))
                .andExpect(jsonPath("_links.self.href", is("http://localhost/orders/4")))
                .andExpect(jsonPath("_links.orders.href", is("http://localhost/orders")));
    }

    @Test
    void getAllRecordsSuccess() throws Exception {
        given(orderRepository.findAll()).willReturn(getOrderData());
        final ResultActions resultsActions = mockMvc.perform(get("/orders").accept(MediaType.APPLICATION_JSON));
        resultsActions.andExpect(status().isOk());
        checkAllOrderRecordsJson(resultsActions);
    }

    @Test
    void getSingleRecordSuccess() throws Exception {
        given(orderRepository.findById(3L)).willReturn(Optional.of(getOrderData().get(0)));
        final ResultActions resultsActions = mockMvc.perform(get("/orders/3").accept(MediaType.APPLICATION_JSON));
        resultsActions.andExpect(status().isOk());
        checkSingleOrderJson(resultsActions);
    }

    @Test
    void insertNewOrderSuccess() throws Exception {
        Order order = getOrderData().get(0);
        given(orderRepository.save(any())).willReturn(order);
        final ResultActions resultActions =
                mockMvc.perform(post("/orders")
                        .content(mapper.writeValueAsBytes(order))
                        .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isCreated());
        checkSingleOrderJson(resultActions);
    }

    @Test
    void changeOrderSuccess() throws Exception {
        Order order = getOrderData().get(0);
        given(orderRepository.save(any())).willReturn(order);
        final ResultActions resultActions =
                mockMvc.perform(put("/orders/3")
                        .content(mapper.writeValueAsBytes(order))
                        .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isCreated());
        checkSingleOrderJson(resultActions);
    }

    @Test
    void cancelOrderSuccess() throws Exception {
        given(orderRepository.findById(4L)).willReturn(Optional.of(getOrderData().get(1)));
        final ResultActions resultsActions =
                mockMvc.perform(delete("/orders/4/cancel"));
        resultsActions.andExpect(status().isOk());
        checkSingleOrderCancelled(resultsActions);

    }

    @Test
    void completeOrderSuccess() throws Exception {
        given(orderRepository.findById(4L)).willReturn(Optional.of(getOrderData().get(1)));
        final ResultActions resultsActions =
                mockMvc.perform(put("/orders/4/complete"));
        resultsActions.andExpect(status().isOk());
        checkSingleOrderCompleted(resultsActions);
    }

    @Test
    void getOrderThatDoesNotExistReturnsError() throws Exception {
        given(orderRepository.findById(3L)).willReturn(Optional.empty());
        final ResultActions resultActions = mockMvc.perform(get("/orders/10"));
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(content().string("Could not find Order: 10\n"));
    }

    @Test
    void tryChangeStatusOfACancelledOrderShouldBeFail() throws Exception {
        given(orderRepository.findById(5L)).willReturn(Optional.of(getOrderData().get(2)));
        final ResultActions resultsActions =
                mockMvc.perform(put("/orders/5/complete"));
        resultsActions.andExpect(status().isMethodNotAllowed());
    }

    @Test
    void tryChangeStatusOfACompletedOrderShouldBeFail() throws Exception {
        given(orderRepository.findById(3L)).willReturn(Optional.of(getOrderData().get(0)));
        final ResultActions resultsActions =
                mockMvc.perform(put("/orders/3/complete"));
        resultsActions.andExpect(status().isMethodNotAllowed());
    }
}
