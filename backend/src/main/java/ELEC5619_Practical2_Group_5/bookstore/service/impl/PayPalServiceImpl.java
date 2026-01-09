package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.pay.PayPalOrderRequest;
import ELEC5619_Practical2_Group_5.bookstore.service.PayPalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class PayPalServiceImpl implements PayPalService {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.base-url:sandbox}")
    private String baseUrl;

    // ---------- get Access Token ----------
    private String getAccessToken() throws Exception {
        String url = baseUrl.equals("live") ?
                "https://api.paypal.com/v1/oauth2/token" :
                "https://api.sandbox.paypal.com/v1/oauth2/token";

        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");

        Map<String, Object> response = webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("access_token")) {
            throw new Exception("Failed to get PayPal access token: " + response);
        }

        return (String) response.get("access_token");
    }

    // ---------- create order from cart ----------
    public Map<String, Object> createOrderFromCart(Map<String, Object> body) throws Exception {
        List<Map<String, Object>> cart = (List<Map<String, Object>>) body.get("cart");

        // calculate total amount
        double totalAmount = cart.stream()
                .mapToDouble(item -> {
                    double price = ((Number) item.get("price")).doubleValue();
                    int quantity = ((Number) item.get("quantity")).intValue();
                    return price * quantity;
                }).sum();

        // build PayPal purchase_units
        Map<String, Object> purchaseUnit = Map.of(
                "reference_id", "ORDER_" + System.currentTimeMillis(),
                "description", "Bookstore Order",
                "amount", Map.of(
                        "currency_code", "AUD",
                        "value", String.format("%.2f", totalAmount),
                        "breakdown", Map.of(
                                "item_total", Map.of(
                                        "currency_code", "AUD",
                                        "value", String.format("%.2f", totalAmount)
                                )
                        )
                ),
                "items", cart.stream().map(item -> Map.of(
                        "name", item.get("title"),
                        "unit_amount", Map.of(
                                "currency_code", "AUD",
                                "value", String.format("%.2f", ((Number) item.get("price")).doubleValue())
                        ),
                        "quantity", item.get("quantity").toString()
                )).toList()
        );

        Map<String, Object> orderRequest = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(purchaseUnit)
        );

        return createOrderFromRaw(orderRequest);
    }

    // ---------- create order（send purchase_units directly） ----------
    public Map<String, Object> createOrderFromRaw(Map<String, Object> orderRequest) throws Exception {
        String token = getAccessToken();

        String url = baseUrl.equals("live") ?
                "https://api.paypal.com/v2/checkout/orders" :
                "https://api.sandbox.paypal.com/v2/checkout/orders";

        System.out.println("Order request: " + orderRequest);
        Map<String, Object> response = webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        System.out.println("PayPal response: " + response);
        return response;

//        return webClient.post()
//                .uri(url)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(orderRequest)
//                .retrieve()
//                .bodyToMono(Map.class)
//                .block();
    }

    // ---------- capture order ----------
    public Map<String, Object> captureOrder(String orderId) throws Exception {
        String token = getAccessToken();

        String url = baseUrl.equals("live") ?
                "https://api.paypal.com/v2/checkout/orders/" + orderId + "/capture" :
                "https://api.sandbox.paypal.com/v2/checkout/orders/" + orderId + "/capture";

//        return webClient.post()
//                .uri(url)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                .retrieve()
//                .bodyToMono(Map.class)
//                .block();
        return webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of())  // 空 JSON
                .retrieve()
                .bodyToMono(Map.class)
                .block();

    }

    @Override
    public Map<String, Object> refundPayment(String captureId, BigDecimal amount) throws Exception {
        String token = getAccessToken();

        // Select correct API base
        String url = baseUrl.equals("live")
                ? "https://api.paypal.com/v2/payments/captures/" + captureId + "/refund"
                : "https://api.sandbox.paypal.com/v2/payments/captures/" + captureId + "/refund";

        // Build refund request payload
        Map<String, Object> refundRequest = Map.of(
                "amount", Map.of(
                        "value", String.format("%.2f", amount),
                        "currency_code", "AUD"
                )
        );

        System.out.println("Refund request: " + refundRequest);

        // Call PayPal Refund API
        Map<String, Object> response = webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refundRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        System.out.println("PayPal refund response: " + response);
        return response;
    }

}
