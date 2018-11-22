package org.springframework.social.salesforce.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Exception thrown when receiving an ip restriction error from Salesforce.
 *
 * @author Maxime Coulombe
 */
public class SalesforceIpRestrictedException extends HttpClientErrorException
{
    private static final long serialVersionUID = 5177015431887513952L;

    public SalesforceIpRestrictedException(HttpStatus statusCode) {
        super(statusCode);
    }
}
