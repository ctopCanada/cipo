package ca.ised.sts.integration.processor.salesforce;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import ca.ised.sts.integration.model.salesforce.Case;

@Component
public class GetParameterProcessor implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		String transaction = (String)exchange.getIn().getHeader("transaction");
		exchange.getOut().setHeader("transaction", transaction);
		exchange.getOut().setBody(exchange.getIn().getBody());
		exchange.getIn().setHeader(Exchange.HTTP_QUERY, "transaction="+transaction);
	}

}
