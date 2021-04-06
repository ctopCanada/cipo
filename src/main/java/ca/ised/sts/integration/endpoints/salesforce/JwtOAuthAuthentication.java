package ca.ised.sts.integration.endpoints.salesforce;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.ised.sts.integration.exception.EndpointException;
import ca.ised.sts.integration.model.AuthTokenResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.*; 
import java.text.MessageFormat;  

@Component
public class JwtOAuthAuthentication {
	
	@Value("${sf-sts-connected-app-consumer-key:3MVG9rIycKjurncY199H833ZFbkP90RR6NEU5kDjc2OlFDRjgGiiW7eM0RySsxtNKveAeMliUqoenzfA8LNHq}") // added default 
	private String SF_CONNECTED_APP_CONSUMER_KEY;
	
	@Value("${sf-sts-connected-app-username:mm.mmarshall.mm@ised.appy.scr1}") // added default 
	private String SF_CONNECTED_APP_USERNAME;
	
	@Value("${sf-login-url-key:https://test.salesforce.com}") // added default 
	private String SF_LOGIN_URL;
	
	@Value("${sf-sts-connected-app-certificate-name:ca.ised.sts.integration.salesforce}") // added default 
	private String SF_CONNECTED_APP_CERTIFICATE_NAME;

	@Value("${sf-sts-connected-app-certificate-password:SomePassword}") // added default 
	private String SF_CONNECTED_APP_CERTIFICATE_PASSWORD;
	
	@Value("${java-keystore-path:C:/Users/mmmma/ISED/STS_UBF/keystore.jks}") // added default 
	private String JAVA_KEYSTORE_PATH;

	@Value("${java-keystore-password:password}") // added default 
	private String JAVA_KEYSTORE_PASSWORD;
	
	@Value("${sf-sts-endpoint-auth-domain:ability-drive-3941-dev-ed.my.salesforce.com}") // added default 
	private String SF_ENDPOINT_AUTH_DOMAIN;
	
	@Value("${http.proxyHost:dhwg01.prod.prv}")
    private String ISED_PROXY_HOST;

    @Value("${http.proxyPort:80}")
    private Integer ISED_PROXY_PORT;
    
    @Value("${spring.aop.proxy-target-class:false}")
    private boolean HAS_PROXY;
	    
  	public String getJWTtoken() {
	  	String header = "{\"alg\":\"RS256\"}";
	    String claimTemplate = "'{'\"iss\": \"{0}\", \"sub\": \"{1}\", \"aud\": \"{2}\", \"exp\": \"{3}\"'}'";
	
	    StringBuffer token = new StringBuffer();
	    
	    try {
	
	      //Encode the JWT Header and add it to our string to sign
	      token.append(Base64.encodeBase64URLSafeString(header.getBytes("UTF-8")));
	
	      //Separate with a period
	      token.append(".");
	
	      //Create the JWT Claims Object
	      String[] claimArray = new String[4];
	      claimArray[0] = SF_CONNECTED_APP_CONSUMER_KEY;
	      claimArray[1] = SF_CONNECTED_APP_USERNAME;
	      claimArray[2] = SF_LOGIN_URL;
	      claimArray[3] = Long.toString( ( System.currentTimeMillis()/1000 ) + 3000);
	      
	      MessageFormat claims;
	      claims = new MessageFormat(claimTemplate);
	      String payload = claims.format(claimArray);
	
	      //Add the encoded claims object
	      token.append(Base64.encodeBase64URLSafeString(payload.getBytes("UTF-8")));
	
	      //Load the private key from a keystore 
	      KeyStore keystore = KeyStore.getInstance("JKS");
	      keystore.load(new FileInputStream(JAVA_KEYSTORE_PATH), JAVA_KEYSTORE_PASSWORD.toCharArray());
	      PrivateKey privateKey = (PrivateKey) keystore.getKey(SF_CONNECTED_APP_CERTIFICATE_NAME, SF_CONNECTED_APP_CERTIFICATE_PASSWORD.toCharArray());
	
	      //Sign the JWT Header + "." + JWT Claims Object
	      Signature signature = Signature.getInstance("SHA256withRSA");
	      signature.initSign(privateKey);
	      signature.update(token.toString().getBytes("UTF-8"));
	      String signedPayload = Base64.encodeBase64URLSafeString(signature.sign());
	
	      //Separate with a period
	      token.append(".");
	
	      //Add the encoded signature
	      token.append(signedPayload);
	
	    } catch (Exception e) {
	    	throw new EndpointException("JWT Token creation failed", e);
	    }
	    
	    return token.toString();
	}
  	
  	protected HttpURLConnection createConnection(URL url) throws IOException {
  		if(HAS_PROXY) {
  			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ISED_PROXY_HOST, ISED_PROXY_PORT));
  	        return (HttpURLConnection) url.openConnection(proxy);
  		}
  		return (HttpURLConnection) url.openConnection();
        
    }

	public String getAuthToken(String jwtToken) {
		String token = "";
		
		try {
	            
			URL url = new URL("https://" + SF_ENDPOINT_AUTH_DOMAIN + "/services/oauth2/token?grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=" + jwtToken);
			HttpURLConnection con = createConnection(url);
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
	
			// For POST only - START
			con.setDoOutput(true);
	
			int responseCode = con.getResponseCode();
	
			if (responseCode == HttpURLConnection.HTTP_OK) { //success
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
	
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
	
				ObjectMapper mapper = new ObjectMapper();
			    AuthTokenResponse result = mapper.readValue(response.toString(), AuthTokenResponse.class);
				token = result.getAccess_token();
	
			    
			} else {
				throw new EndpointException("Authentication failed - With response code: " + responseCode);
			}
		}catch(Exception ex) {
			throw new EndpointException("Authentication Token creation failed", ex);
		}
		
		return token;
	}
	
}