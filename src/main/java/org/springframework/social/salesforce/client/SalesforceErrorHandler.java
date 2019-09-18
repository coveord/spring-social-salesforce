package org.springframework.social.salesforce.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom error handler for handling Salesforce API specific error responses.
 *
 * @author Umut Utkan
 * @author Maxime Coulombe
 */
public class SalesforceErrorHandler extends DefaultResponseErrorHandler
{
    private static final Logger logger = LoggerFactory.getLogger(SalesforceErrorHandler.class);

    private ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException
    {
        boolean hasError = super.hasError(response);
        if (hasError) {
            logger.warn("Salesforce error received : '{}'.",
                        IOUtils.toString(response.getBody(), StandardCharsets.UTF_8));
        }
        return hasError;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException
    {
        if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            Map<String, String> error = extractErrorDetailsFromResponse(response);

            if (error != null && "invalid_grant".equals(error.get("error"))
                    && "ip restricted".equals(error.get("error_description"))) {
                throw new SalesforceIpRestrictedException();
            }
        }

        super.handleError(response);
    }

    private Map<String, String> extractErrorDetailsFromResponse(ClientHttpResponse response) throws IOException
    {
        try {
            return objectMapper.readValue(response.getBody(), Map.class);
        } catch (JsonParseException e) {
            return null;
        }
    }
}
