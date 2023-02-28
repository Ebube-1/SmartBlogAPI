package com.springboot.blog.service.impl;

import com.springboot.blog.dto.EmailDto;
import com.springboot.blog.exception.ServerException;
import com.springboot.blog.service.EmailService;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.email.service-type", havingValue = "app", matchIfMissing = true)
public class EmailServiceImpl implements EmailService {

    @Value("${app.email.providerUrl}")
    private String providerUrl;

    @Value("${app.email.sender}")
    private String sender;

    @Value("${app.email.username}")
    private String username;

    @Value("${app.email.password}")
    private String password;

    @Override
    public void sendEmail(EmailDto emailDto) {
        log.info("email: " + emailDto);

        Client client = Client.create();
        WebResource webResource = client.resource(providerUrl);
        client.addFilter(new HTTPBasicAuthFilter(username, password));

        MultivaluedMapImpl formData = new MultivaluedMapImpl();
        formData.add("from", sender);
        formData.add("to", emailDto.getRecipientEmail());
        formData.add("subject", emailDto.getSubject());
        formData.add("text", emailDto.getMessage());

        ClientResponse clientResponse = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).
                post(ClientResponse.class, formData);

        boolean isSuccessful = Response.Status.Family.SUCCESSFUL.equals(clientResponse.getStatusInfo().getFamily());
        String statusMsg = clientResponse.getStatusInfo().getReasonPhrase();
        if (isSuccessful) {
            log.info("Email sent successfully. Status: " + statusMsg);
        } else {
            String errorMsg = "Error sending email. Status: " + statusMsg;
            throw new ServerException(errorMsg);
        }
    }
}
