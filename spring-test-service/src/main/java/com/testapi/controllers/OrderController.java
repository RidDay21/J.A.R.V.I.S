package com.testapi.controllers;

import com.testapi.dto.OrderRequest;
import com.testapi.dto.OrderResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // ВАЖНО: Разрешаем запросы из браузера (из нашего сгенерированного HTML)
public class OrderController {

    // 1. POST - создание (тестируем @RequestBody и @RequestHeader)
    @PostMapping
    public OrderResponse createOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody OrderRequest request) {

        System.out.println("Получен токен: " + token);
        System.out.println("Заказ на товар ID: " + request.itemId + " в город " + request.deliveryAddress.city);

        return new OrderResponse("ORD-123", "CREATED", "Заказ успешно создан");
    }

    // 2. GET - получение (тестируем @PathVariable)
    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable("id") String orderId) {
        return new OrderResponse(orderId, "DELIVERED", "Заказ доставлен");
    }

    // 3. PUT - обновление (тестируем @PathVariable и @RequestParam)
    @PutMapping("/{id}")
    public OrderResponse updateOrderStatus(
            @PathVariable("id") String orderId,
            @RequestParam("status") String newStatus) {

        return new OrderResponse(orderId, newStatus, "Статус успешно обновлен");
    }

    // 4. DELETE - удаление
    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable("id") String orderId) {
        return "{\"message\": \"Заказ " + orderId + " удален\"}";
    }
}