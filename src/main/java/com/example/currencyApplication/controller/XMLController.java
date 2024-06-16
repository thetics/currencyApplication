package com.example.currencyApplication.controller;
import com.example.currencyApplication.command.Command;
import com.example.currencyApplication.service.CurrencyService;
import com.example.currencyApplication.service.MessagePublisher;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;



@RestController
@RequestMapping("/xml_api")
public class XMLController {

    private static final Logger logger = LoggerFactory.getLogger(XMLController.class);

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private MessagePublisher messagePublisher;

    private final XmlMapper xmlMapper = new XmlMapper();

    @PostMapping(value = "/command", consumes = "application/xml", produces = "application/xml")
    public ResponseEntity<?> handleCommand(@RequestBody String commandXml) {
        try {
            Command command = xmlMapper.readValue(commandXml, Command.class);
            if (command == null) {
                logger.warn("Invalid command format received: {}", commandXml);
                return ResponseEntity.badRequest().body("Invalid command format");
            }
            ResponseEntity<?> response = command.handle(currencyService);
            messagePublisher.sendMessage(commandXml);
            return response;
        } catch (IOException e) {
            logger.error("Failed to parse XML: {}", commandXml, e);
            return ResponseEntity.badRequest().body("Invalid XML format");
        } catch (IllegalArgumentException e) {
            logger.warn("Duplicate request detected: {}", commandXml, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error processing command: {}", commandXml, e);
            return ResponseEntity.badRequest().body("Unexpected error occurred");
        }
    }
}