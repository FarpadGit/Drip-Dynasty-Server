package com.farpad.dripServer.services;

import com.farpad.dripServer.models.Customer;
import com.farpad.dripServer.models.Product;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

@Service
public class EmailService {

    @Value("${drip-app.server-origin}")
    private String serverRootPath;
    @Value("${spring.mail.username}")
    private String emailSenderAddress;

    private final ProductService productService;
    private final CustomerService customerService;
    private final JavaMailSender mailSender;

    public EmailService(ProductService productService, CustomerService customerService, JavaMailSender mailSender) {
        this.productService = productService;
        this.customerService = customerService;
        this.mailSender = mailSender;
    }

    public void sendPurchaseSuccessEmail(String to, String orderId) {
        try {
            Product product = productService.getProductByOrder(orderId);
            if(product == null) return;

            MimeMessage message = mailSender.createMimeMessage();

            message.setFrom(new InternetAddress(emailSenderAddress));
            message.setRecipients(MimeMessage.RecipientType.TO, to);
            message.setSubject("Purchase successful - Drip Dynasty");

            NumberFormat nf = NumberFormat.getCurrencyInstance();
            nf.setCurrency(Currency.getInstance(Locale.forLanguageTag("hu-HU")));
            nf.setMaximumFractionDigits(0);

            // Read the HTML template into a String variable
            String htmlTemplate = readTemplateFile("src/main/resources/templates/emailPurchaseSuccessTemplate.html");

            // Replace placeholders in the HTML template with dynamic values
            htmlTemplate = htmlTemplate.replace("${orderId}", orderId);
            htmlTemplate = htmlTemplate.replace("${extra}", product.getExtra());
            htmlTemplate = htmlTemplate.replace("${productName}", product.getName());
            htmlTemplate = htmlTemplate.replace("${productPrice}", nf.format(product.getPrice()));
            htmlTemplate = htmlTemplate.replace("${productImage}", product.getPrefixedFileNames(serverRootPath).get(0));

            // Set the email's content to be the HTML template
            message.setContent(htmlTemplate, "text/html; charset=utf-8");

            mailSender.send(message);
        } catch (MessagingException | IOException | MailException ignored) {
        }
    }

    public void sendOrderHistoryEmail(String to) {
        try {
            Customer customer = customerService.getCustomerByEmail(to);
            if(customer == null) return;

            MimeMessage message = mailSender.createMimeMessage();

            message.setFrom(new InternetAddress(emailSenderAddress));
            message.setRecipients(MimeMessage.RecipientType.TO, to);
            message.setSubject("Order History - Drip Dynasty");

            // Read the HTML template into a String variable
            String htmlTemplate = readTemplateFile("src/main/resources/templates/emailOrderHistoryTemplate.html");
            StringBuilder htmlTableRows = new StringBuilder();

            NumberFormat nf = NumberFormat.getCurrencyInstance();
            nf.setCurrency(Currency.getInstance(Locale.forLanguageTag("hu-HU")));
            nf.setMaximumFractionDigits(0);

            List<String> orders = customer.getOrders();
            orders.forEach(o -> {
                Product p = productService.getProductByOrder(o);
                String tableRow = "<tr>" +
                        "<td>" + o + "</td>" +
                        "<td>" + p.getName() + "</td>" +
                        "<td>" + nf.format(p.getPrice()) + "</td>" +
                        "<td><img alt='' src='" + p.getPrefixedFileNames(serverRootPath).get(0) + "' width='120px' height='120px'/></td>" +
                        "<td>" + p.getExtra() + "</td>" +
                        "</tr>";
                htmlTableRows.append(tableRow);
            });

            // Replace placeholders in the HTML template with dynamic values
            htmlTemplate = htmlTemplate.replace("${tableRows}", htmlTableRows.toString());


            // Set the email's content to be the HTML template
            message.setContent(htmlTemplate, "text/html; charset=utf-8");

            mailSender.send(message);
        } catch (MessagingException | IOException ignored) {
        }
    }

    private String readTemplateFile(String fileName) throws IOException {
        StringBuilder html = new StringBuilder();

        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);

        String val;

        while ((val = br.readLine()) != null) {
            html.append(val);
        }

        br.close();

        return html.toString();
    }
}
