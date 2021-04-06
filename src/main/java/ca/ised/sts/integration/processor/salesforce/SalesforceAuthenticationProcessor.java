package ca.ised.sts.integration.processor.salesforce;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.ised.sts.integration.endpoints.salesforce.JwtOAuthAuthentication;

/**
 * Contains logic for authenticating against Salesforce.
 * 
 * @author mmmma
 *
 */
@Component
public class SalesforceAuthenticationProcessor implements Processor {
		
	@Autowired
	private JwtOAuthAuthentication jwtOAuthAuthentication;
	
	private static String AUTH_TOKEN;
	
	@Override
	public void process(Exchange exchange) throws Exception {
	    String authToken = getAuthToken();
	    exchange.getOut().getHeaders().putAll(exchange.getIn().getHeaders()); // keep existing headers
		exchange.getOut().getHeaders().put("Authorization", "Bearer " + authToken); // add auth token to 
		exchange.getOut().setBody(exchange.getIn().getBody());
	}
	
	public void populateAuthToken() {
        String jwtToken = jwtOAuthAuthentication.getJWTtoken();
        AUTH_TOKEN = jwtOAuthAuthentication.getAuthToken(jwtToken);
    }

    public synchronized String getAuthToken() {

        // If Auth token hasn't been created yet - Do it now
        if (AUTH_TOKEN == null || AUTH_TOKEN.isEmpty()) {
            populateAuthToken();
        } 

        return AUTH_TOKEN;
    }
}

