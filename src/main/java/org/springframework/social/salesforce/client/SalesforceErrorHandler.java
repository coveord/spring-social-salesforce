package org.springframework.social.salesforce.client;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.social.InvalidAuthorizationException;
import org.springframework.social.OperationNotPermittedException;
import org.springframework.social.RateLimitExceededException;
import org.springframework.web.client.DefaultResponseErrorHandler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Custom error handler for handling Salesforce API specific error responses.
 * Detailed exception is saved as a session attribute so deliberate obfuscation does not
 * prevent proper upstream error handling.
 *
 * @author Umut Utkan
 * @author Maxime Coulombe
 */
public class SalesforceErrorHandler extends DefaultResponseErrorHandler
{
    public static final String EXCEPTION_DETAIL_SESSION_ATTRIBUTE = "salesforceExceptionDetail";
    public static final String PROVIDER_ID = "Salesforce";

    @Override
    public void handleError(ClientHttpResponse response) throws IOException
    {
        if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            Map<String, String> error = extractErrorDetailsFromResponse(response);
            Throwable exception = null;

            if ("unsupported_response_type".equals(error.get("error"))) {
                exception = new OperationNotPermittedException(PROVIDER_ID, error.get("error_description"));
            } else if ("invalid_client_id".equals(error.get("error"))) {
                exception = new InvalidAuthorizationException(PROVIDER_ID, error.get("error_description"));
            } else if ("invalid_request".equals(error.get("error"))) {
                exception = new OperationNotPermittedException(PROVIDER_ID, error.get("error_description"));
            } else if ("invalid_client_credentials".equals(error.get("error"))) {
                exception = new InvalidAuthorizationException(PROVIDER_ID, error.get("error_description"));
            } else if ("invalid_grant".equals(error.get("error"))) {
                if ("invalid user credentials".equals(error.get("error_description"))) {
                    exception = new InvalidAuthorizationException(PROVIDER_ID, error.get("error_description"));
                } else if ("ip restricted".equals(error.get("error_description"))) {
                    exception = new OperationNotPermittedException(PROVIDER_ID, error.get("error_description"));
                }
            } else if ("inactive_user".equals(error.get("error"))) {
                exception = new OperationNotPermittedException(PROVIDER_ID, error.get("error_description"));
            } else if ("inactive_org".equals(error.get("error"))) {
                exception = new OperationNotPermittedException(PROVIDER_ID, error.get("error_description"));
            } else if ("rate_limit_exceeded".equals(error.get("error"))) {
                exception = new RateLimitExceededException(PROVIDER_ID);
            } else if ("invalid_scope".equals(error.get("error"))) {
                exception = new InvalidAuthorizationException(PROVIDER_ID, error.get("error_description"));
            }

            if (exception != null) {
                saveDetailedExceptionInSessionAttributes(exception);
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

    private void saveDetailedExceptionInSessionAttributes(Throwable exception)
    {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        servletRequestAttributes.getRequest().getSession().setAttribute(EXCEPTION_DETAIL_SESSION_ATTRIBUTE, exception);
    }
}
