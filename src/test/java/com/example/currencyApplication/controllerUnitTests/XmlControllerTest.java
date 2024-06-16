package com.example.currencyApplication.controllerUnitTests;

import com.example.currencyApplication.controller.XMLController;
import com.example.currencyApplication.service.CurrencyService;
import com.example.currencyApplication.service.MessagePublisher;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XmlControllerTest {

    @InjectMocks
    private XMLController xmlController;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private MessagePublisher messagePublisher;

    private final XmlMapper xmlMapper = new XmlMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleCommand_ShouldReturnBadRequest_WhenInvalidXML() {
        String invalidXml = "<invalid></invalid>";

        ResponseEntity<?> response = xmlController.handleCommand(invalidXml);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid XML format", response.getBody());
    }

    @Test
    void handleCommand_ShouldReturnBadRequest_WhenInvalidCommand() throws Exception {
        String invalidCommandXml = "<command id=\"1234\"></command>";

        ResponseEntity<?> response = xmlController.handleCommand(invalidCommandXml);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid XML format", response.getBody());
    }}