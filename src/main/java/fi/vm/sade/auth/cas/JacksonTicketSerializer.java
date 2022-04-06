package fi.vm.sade.auth.cas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.apereo.cas.ticket.Ticket;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JacksonTicketSerializer implements TicketSerializer {

    private final ObjectMapper objectMapper;

    public JacksonTicketSerializer() {
        this(new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .addHandler(new OldTicketPackageNameDeserializationProblemHandler()));
    }

    protected JacksonTicketSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String toJson(Ticket ticket) {
        try {
            return objectMapper.writeValueAsString(ticket);
        } catch (JsonProcessingException e) {
            throw new TicketJsonpSerializingProblem(e);
        }
    }

    @Override
    public Ticket fromJson(String ticketJson, String ticketGrantingTicketJson) {
        try {
            if (ticketGrantingTicketJson != null) {
                Ticket ticket = objectMapper.readValue(ticketJson, Ticket.class);
                ObjectReader objectReader = objectMapper.readerForUpdating(ticket);
                JsonNode ticketGrantingTicketNode = objectMapper.createObjectNode()
                        .set("ticketGrantingTicket", objectMapper.readTree(ticketGrantingTicketJson));
                return objectReader.readValue(ticketGrantingTicketNode);
            }
            return objectMapper.readValue(ticketJson, Ticket.class);
        } catch (IOException e) {
            throw new TicketJsonpSerializingProblem(e);
        }
    }

    private static class TicketJsonpSerializingProblem extends RuntimeException {
        TicketJsonpSerializingProblem(Exception cause) {
            super(cause);
        }
    }

}
