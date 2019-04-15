package org.apereo.cas.services.util;

import org.apache.commons.lang3.NotImplementedException;
import org.apereo.cas.services.RegisteredService;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * Default implementation breaks creating service ticket (ticket is not in response). Fixed in cas 6.1.
 */
public class RegisteredServiceYamlHttpMessageConverter<T> extends AbstractHttpMessageConverter<T> {
    public RegisteredServiceYamlHttpMessageConverter() {
        super(new MediaType("application", "vnd.cas.services+yaml"));
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    @Override
    protected T readInternal(final Class<? extends T> clazz, final HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        throw new NotImplementedException("read() operation is not implemented");
    }

    @Override
    protected void writeInternal(final T t, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try (OutputStreamWriter writer = new OutputStreamWriter(outputMessage.getBody(), StandardCharsets.UTF_8)) {
            RegisteredServiceYamlSerializer serializer = new RegisteredServiceYamlSerializer();
            if (t instanceof Collection) {
                Collection.class.cast(t)
                        .stream()
                        .filter(object -> object instanceof RegisteredService)
                        .forEach(service -> serializer.to(writer, RegisteredService.class.cast(service)));
            }
        }
    }
}
