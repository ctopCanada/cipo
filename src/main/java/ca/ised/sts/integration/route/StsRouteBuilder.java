package ca.ised.sts.integration.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ca.ised.sts.integration.exception.EndpointException;
import ca.ised.sts.integration.model.salesforce.Case;
import ca.ised.sts.integration.model.salesforce.QueryResponse;
import ca.ised.sts.integration.processor.salesforce.GetParameterProcessor;
import ca.ised.sts.integration.processor.salesforce.SalesforceAuthExceptionProcessor;
import ca.ised.sts.integration.processor.salesforce.SalesforceAuthenticationProcessor;

@Component
public class StsRouteBuilder extends RouteBuilder {
	
	@Value("${sf-sts-endpoint-domain:sandbox-ability-drive-3941-dev-ed-1786a3ebeae.cs99.force.com}") // added default 
	private String SF_ENDPOINT_DOMAIN;
	
	@Value("${sts-integration-endpoint-domain:localhost}") // added default 
	private String INTEGRATION_ENDPOINT_DOMAIN;
	
	@Value("${sts-integration-endpoint-port:8181}") // added default 
	private String INTEGRATION_ENDPOINT_PORT;
	
	@Autowired
	private GetParameterProcessor getParameterProcessor;
	
	@Autowired
	private SalesforceAuthenticationProcessor salesforceAuthenticationProcessor;
	
	@Autowired
    private SalesforceAuthExceptionProcessor sfAuthExceptionProcessor;
		
	JacksonDataFormat jsonDataFormatQueryResponse = new JacksonDataFormat(QueryResponse.class);
	JacksonDataFormat jsonDataFormatCase = new JacksonDataFormat(Case.class);

	@Override
	public void configure() throws Exception {

		// route for REST GET Call
		restConfiguration().component("restlet").host(INTEGRATION_ENDPOINT_DOMAIN).port(INTEGRATION_ENDPOINT_PORT);

		// get cases to test camel
		rest("cipo/postback").get().route()
			.process(getParameterProcessor)
			.process(salesforceAuthenticationProcessor)
			.setHeader(Exchange.HTTP_QUERY, simple("transaction=${header.transaction}"))
			.to("https://" + SF_ENDPOINT_DOMAIN + "/citm/services/apexrest/cart/api/postback")
			.onException(EndpointException.class).onRedelivery(sfAuthExceptionProcessor);				
		
	}

}