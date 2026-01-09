package ELEC5619_Practical2_Group_5.bookstore.dto.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderRequestTest {

    @Test
    void testShippingInfoDTO_GettersAndSetters() {
        OrderRequest.ShippingInfoDTO shippingInfo = new OrderRequest.ShippingInfoDTO();

        // Set values
        shippingInfo.setName("John Doe");
        shippingInfo.setAddress("123 Main St");
        shippingInfo.setCity("Sydney");
        shippingInfo.setPostcode("2000");
        shippingInfo.setCountry("Australia");

        // Get values and assert
        assertEquals("John Doe", shippingInfo.getName());
        assertEquals("123 Main St", shippingInfo.getAddress());
        assertEquals("Sydney", shippingInfo.getCity());
        assertEquals("2000", shippingInfo.getPostcode());
        assertEquals("Australia", shippingInfo.getCountry());
    }

    @Test
    void testOrderItemDTO_GettersAndSetters() {
        OrderRequest.OrderItemDTO item = new OrderRequest.OrderItemDTO();
        item.setBookFormatId(1L);
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(19.99));

        assertEquals(1L, item.getBookFormatId());
        assertEquals(2, item.getQuantity());
        assertEquals(BigDecimal.valueOf(19.99), item.getPrice());
    }

    @Test
    void testOrderRequest_GettersAndSetters() {
        OrderRequest request = new OrderRequest();
        OrderRequest.ShippingInfoDTO shippingInfo = new OrderRequest.ShippingInfoDTO();
        shippingInfo.setName("Alice");

        request.setShippingInfo(shippingInfo);
        request.setPaypalOrderId("PAYPAL123");
        request.setTotalAmount(BigDecimal.valueOf(50.0));

        assertEquals("PAYPAL123", request.getPaypalOrderId());
        assertEquals(BigDecimal.valueOf(50.0), request.getTotalAmount());
        assertEquals("Alice", request.getShippingInfo().getName());
    }
}
