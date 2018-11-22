package org.springframework.social.salesforce.client;

import java.io.IOException;
import java.util.Map;

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
    @Override
    public void handleError(ClientHttpResponse response) throws IOException
    {
        if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            Map<String, String> error = extractErrorDetailsFromResponse(response);

            if ("invalid_grant".equals(error.get("error")) && "ip restricted".equals(error.get("error_description"))) {
                throw new SalesforceIpRestrictedException(HttpStatus.FORBIDDEN);
            }
        }

        super.handleError(response);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractErrorDetailsFromResponse(ClientHttpResponse response) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        try {
            return mapper.readValue(response.getBody(), Map.class);
        } catch (JsonParseException e) {
            return null;
        }
    }
}
