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

package com.quickwinsit.apps.customerrestfull.controller.customer;

import com.quickwinsit.apps.customerrestfull.configuration.DatabaseConfig;
import com.quickwinsit.apps.customerrestfull.exception.CustomerNotFoundException;
import com.quickwinsit.apps.customerrestfull.model.customer.Customer;
import com.quickwinsit.apps.customerrestfull.model.customer.CustomerDto;
import com.quickwinsit.apps.customerrestfull.model.customer.CustomerModelAssembler;
import com.quickwinsit.apps.customerrestfull.model.customer.CustomerRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class CustomerController {
    Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    private final CustomerRepository repository;
    private final CustomerModelAssembler assembler;

    private final ModelMapper modelMapper;

    @Autowired
    public CustomerController(CustomerRepository r, CustomerModelAssembler c, ModelMapper m) {
        this.repository = r;
        this.assembler = c;
        this.modelMapper = m;
    }

    @GetMapping("/customers")
    public CollectionModel<EntityModel<Customer>> allCustomers() {
        logger.info("Getting all customers from database");
        List<EntityModel<Customer>> customers = repository.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(customers,
                linkTo(methodOn(CustomerController.class).allCustomers()).withSelfRel());
    }

    @PostMapping("/customers")
    public ResponseEntity<EntityModel<Customer>> newCustomer(@RequestBody CustomerDto newCustomer) {
        logger.info("Creating a new customer on database");
        EntityModel<Customer> entityModel = assembler.toModel(repository.save(convertToEntity(newCustomer)));
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModel);
    }

    @GetMapping("/customers/{id}")
    public EntityModel<Customer> getSingleCustomer(@PathVariable Long id) {
        logger.info("Getting a single customer from database");
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return assembler.toModel(customer);
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<EntityModel<Customer>> replaceCustomers(@RequestBody CustomerDto newCustomerDto, @PathVariable Long id) {
        logger.info("Updating a single customer from database");
        Customer newCustomer = convertToEntity(newCustomerDto);
        Customer updatedCustomer = repository.findById(id)
                .map(
                        customer -> {
                            customer.setFirstName(newCustomer.getFirstName());
                            customer.setLastName(newCustomer.getLastName());
                            customer.setBirthDate(newCustomer.getBirthDate());
                            return repository.save(customer);
                        }).orElseGet(() -> {
                    newCustomer.setId(id);
                    return repository.save(newCustomer);
                });
        EntityModel<Customer> entityModel = assembler.toModel(updatedCustomer);
        return ResponseEntity //
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModel);
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<CustomerDto> deleteCustomer(@PathVariable Long id) {
        logger.info("Deleting a customer from database");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Customer convertToEntity(CustomerDto customerDTO) {
        return modelMapper.map(customerDTO, Customer.class);
    }
}
