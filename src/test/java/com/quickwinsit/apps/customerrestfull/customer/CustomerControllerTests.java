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

package com.quickwinsit.apps.customerrestfull.customer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickwinsit.apps.customerrestfull.controller.customer.CustomerController;
import com.quickwinsit.apps.customerrestfull.model.customer.Customer;
import com.quickwinsit.apps.customerrestfull.model.customer.CustomerModelAssembler;
import com.quickwinsit.apps.customerrestfull.model.customer.CustomerRepository;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@WebMvcTest(CustomerController.class)
@Import({CustomerModelAssembler.class})
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper mapper;

    private List<Customer> getCustomerData() {
        return Arrays.asList(
                new Customer(
                        1L,
                        "Jose",
                        "Joaquim",
                        LocalDate.of(1746, 11, 12)
                ),
                new Customer(
                        2L,
                        "Silverio",
                        "Joaquim",
                        LocalDate.of(1756, 1, 1)
                )
        );
    }

    private void allRecordsCheckJson(final ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(jsonPath("$._embedded.customerList[0].id", is(1)))
                .andExpect(jsonPath("$._embedded.customerList[0].lastName", is("Jose")))
                .andExpect(jsonPath("$._embedded.customerList[0].firstName", is("Joaquim")))
                .andExpect(jsonPath("$._embedded.customerList[0].birthDate", is(LocalDate.of(1746, 11, 12).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))))
                .andExpect(jsonPath("$._embedded.customerList[0]._links.self.href", is("http://localhost/customers/1")))
                .andExpect(jsonPath("$._embedded.customerList[0]._links.customers.href", is("http://localhost/customers")))
                .andExpect(jsonPath("$._embedded.customerList[1].id", is(2)))
                .andExpect(jsonPath("$._embedded.customerList[1].lastName", is("Silverio")))
                .andExpect(jsonPath("$._embedded.customerList[1].firstName", is("Joaquim")))
                .andExpect(jsonPath("$._embedded.customerList[1].birthDate", is(LocalDate.of(1756, 1, 1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))))
                .andExpect(jsonPath("$._embedded.customerList[1]._links.self.href", is("http://localhost/customers/2")))
                .andExpect(jsonPath("$._embedded.customerList[1]._links.customers.href", is("http://localhost/customers")))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/customers")));
    }

    private void checkJson(final ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("lastName", is("Jose")))
                .andExpect(jsonPath("firstName", is("Joaquim")))
                .andExpect(jsonPath("birthDate", is(LocalDate.of(1746, 11, 12).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))))
                .andExpect(jsonPath("_links.self.href", is("http://localhost/customers/1")))
                .andExpect(jsonPath("_links.customers.href", is("http://localhost/customers")));
    }

    private void checkJsonChanged(final ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("lastName", is("Jose")))
                .andExpect(jsonPath("firstName", is("firstName_changed")))
                .andExpect(jsonPath("birthDate", is(LocalDate.of(1746, 11, 12).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))))
                .andExpect(jsonPath("_links.self.href", is("http://localhost/customers/1")))
                .andExpect(jsonPath("_links.customers.href", is("http://localhost/customers")));
    }

    @Test
    void getAllRecordsSuccess() throws Exception {
        given(customerRepository.findAll()).willReturn(getCustomerData());
        final ResultActions resultsActions = mockMvc.perform(get("/customers").accept(MediaTypes.HAL_JSON_VALUE));
        resultsActions.andExpect(status().isOk());
        allRecordsCheckJson(resultsActions);
    }

    @Test
    void getSingleRecordSuccess() throws Exception {
        given(customerRepository.findById(1L)).willReturn(Optional.of(getCustomerData().get(0)));
        final ResultActions resultActions = mockMvc.perform(get("/customers/1").accept(MediaTypes.HAL_JSON_VALUE));
        resultActions.andExpect(status().isOk());
        checkJson(resultActions);
    }

    @Test
    void insertNewCustomerSuccess() throws Exception {
        Customer customer = getCustomerData().get(0);
        given(customerRepository.save(any())).willReturn(customer);
        final ResultActions resultActions =
                mockMvc.perform(post("/customers")
                        .content(mapper.writeValueAsBytes(customer))
                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        resultActions.andExpect(status().isCreated());
        checkJson(resultActions);
    }

    @Test
    void changeCustomerSuccess() throws Exception {
        Customer customer = getCustomerData().get(0);
        customer.setFirstName("firstName_changed");
        given(customerRepository.save(any())).willReturn(customer);
        final ResultActions resultActions =
                mockMvc.perform(put("/customers/1")
                        .content(mapper.writeValueAsBytes(customer))
                        .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isCreated());
        checkJsonChanged(resultActions);
    }

    @Test
    void deleteCustomerSuccess() throws Exception {
        given(customerRepository.findById(1L)).willReturn(Optional.of(getCustomerData().get(0)));
        mockMvc.perform(delete("/customers/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void getCustomerThatDoesNotExistReturnsError() throws Exception {
        given(customerRepository.findById(1L)).willReturn(Optional.empty());
        final ResultActions resultActions = mockMvc.perform(get("/customers/10"));
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(content().string("Could not find Customer: 10\n"));
    }
}
