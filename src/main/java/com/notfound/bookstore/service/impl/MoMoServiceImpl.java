package com.notfound.bookstore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.bookstore.config.MoMoConfig;
import com.notfound.bookstore.exception.AppException;
import com.notfound.bookstore.exception.ErrorCode;
import com.notfound.bookstore.model.dto.request.paymentrequest.MoMoCallbackRequest;
import com.notfound.bookstore.model.dto.request.paymentrequest.PaymentRequest;
import com.notfound.bookstore.model.dto.response.paymentresponse.CreatePaymentResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.PaymentResponse;
import com.notfound.bookstore.model.entity.Order;
import com.notfound.bookstore.model.entity.Payment;
import com.notfound.bookstore.model.enums.PaymentMethod;
import com.notfound.bookstore.model.enums.PaymentStatus;
import com.notfound.bookstore.model.mapper.PaymentMapper;
import com.notfound.bookstore.repository.OrderRepository;
import com.notfound.bookstore.repository.PaymentRepository;
import com.notfound.bookstore.util.MoMoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoMoServiceImpl {

    private final MoMoConfig moMoConfig;
    private final MoMoUtil moMoUtil;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Transactional(propagation = Propagation.REQUIRED)
    public CreatePaymentResponse createMoMoPayment(PaymentRequest request) {
        try {
            // 1. Validate Order
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));



            // 2. Check existing pending payment
            Optional<Payment> existingPayment = paymentRepository
                    .findByOrderAndStatus(order, PaymentStatus.PENDING);

            if (existingPayment.isPresent()) {
                Payment payment = existingPayment.get();
                LocalDateTime expiryTime = payment.getDate().plusMinutes(15);

                if (LocalDateTime.now().isAfter(expiryTime)) {
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                } else {
                    String transactionId = payment.getTransactionId();
                    if (transactionId == null || transactionId.isEmpty()) {
                        transactionId = generateTransactionId();
                        payment.setTransactionId(transactionId);
                        payment = paymentRepository.save(payment);
                    }

                    // Generate MoMo payment URL with existing transaction
                    String paymentUrl = createMoMoPaymentUrl(transactionId, payment.getAmount());
                    return paymentMapper.toSuccessResponse(payment, paymentUrl);
                }
            }

            // 3. Generate transaction ID
            String transactionId = generateTransactionId();

            // 4. Create new payment
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(request.getAmount())
                    .transactionId(transactionId)
                    .paymentMethod(String.valueOf(PaymentMethod.MoMo))
                    .status(PaymentStatus.PENDING)
                    .build();

            payment = paymentRepository.save(payment);

            // 5. Generate MoMo payment URL
            String paymentUrl = createMoMoPaymentUrl(transactionId, payment.getAmount());
            return paymentMapper.toSuccessResponse(payment, paymentUrl);

        } catch (AppException e) {
            return paymentMapper.toErrorResponse(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating MoMo payment: {}", e.getMessage(), e);
            return paymentMapper.toErrorResponse("Failed to create MoMo payment: " + e.getMessage());
        }
    }

    private String createMoMoPaymentUrl(String transactionId, Long amount) {
        try {
            String requestId = transactionId;
            String orderId = transactionId;
            String orderInfo = "Thanh toán đơn hàng " + orderId;

            // Build raw signature data (theo thứ tự của MoMo)
            String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + moMoConfig.getExtraData() +
                    "&ipnUrl=" + moMoConfig.getNotifyUrl() +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + moMoConfig.getPartnerCode() +
                    "&redirectUrl=" + moMoConfig.getReturnUrl() +
                    "&requestId=" + requestId +
                    "&requestType=" + moMoConfig.getRequestType();

            log.debug("MoMo raw signature: {}", rawSignature);

            String signature = moMoUtil.generateSignature(rawSignature);

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", moMoConfig.getPartnerCode());
            requestBody.put("accessKey", moMoConfig.getAccessKey());
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", moMoConfig.getReturnUrl());
            requestBody.put("ipnUrl", moMoConfig.getNotifyUrl());
            requestBody.put("extraData", moMoConfig.getExtraData());
            requestBody.put("requestType", moMoConfig.getRequestType());
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");

            // Send request to MoMo
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.debug("Sending MoMo request: {}", objectMapper.writeValueAsString(requestBody));

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    moMoConfig.getApiEndpoint(),
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Integer resultCode = (Integer) responseBody.get("resultCode");

                if (resultCode != null && resultCode == 0) {
                    String payUrl = (String) responseBody.get("payUrl");
                    log.info("MoMo payment URL created successfully: {}", payUrl);
                    return payUrl;
                } else {
                    String message = (String) responseBody.get("message");
                    log.error("MoMo returned error: {} - {}", resultCode, message);
                    throw new RuntimeException("MoMo error: " + message);
                }
            } else {
                throw new RuntimeException("Failed to get response from MoMo");
            }

        } catch (Exception e) {
            log.error("Error creating MoMo payment URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create MoMo payment URL", e);
        }
    }

    @Transactional
    public PaymentResponse handleMoMoCallback(MoMoCallbackRequest callback) {
        try {
            // 1. Verify signature
            String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                    "&amount=" + callback.getAmount() +
                    "&extraData=" + callback.getExtraData() +
                    "&message=" + callback.getMessage() +
                    "&orderId=" + callback.getOrderId() +
                    "&orderInfo=" + callback.getOrderInfo() +
                    "&orderType=" + callback.getOrderType() +
                    "&partnerCode=" + callback.getPartnerCode() +
                    "&payType=" + callback.getPayType() +
                    "&requestId=" + callback.getRequestId() +
                    "&responseTime=" + callback.getResponseTime() +
                    "&resultCode=" + callback.getResultCode() +
                    "&transId=" + callback.getTransId();

            boolean isValid = moMoUtil.verifySignature(rawSignature, callback.getSignature());

            if (!isValid) {
                log.error("Invalid MoMo signature");
                throw new AppException(ErrorCode.INVALID_PAYMENT_SIGNATURE);
            }

            // 2. Find payment by transaction ID
            String transactionId = callback.getOrderId();
            Payment payment = paymentRepository.findPaymentByTransactionId(transactionId)
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

            // 3. Check if already processed
            if (payment.getStatus() != PaymentStatus.PENDING) {
                if (payment.getStatus() == PaymentStatus.COMPLETED) {
                    return paymentMapper.toPaymentResponse(payment);
                } else {
                    throw new AppException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
                }
            }

            // 4. Update payment status
            if (callback.getResultCode() == 0) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setDate(LocalDateTime.now());
                payment.setPaymentMethod(String.valueOf(PaymentMethod.MoMo));
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                log.warn("MoMo payment failed: {} - {}", callback.getResultCode(), callback.getMessage());
            }

            paymentRepository.save(payment);
            return paymentMapper.toPaymentResponse(payment);

        } catch (Exception e) {
            log.error("Error handling MoMo callback: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to handle MoMo callback", e);
        }
    }

    private String generateTransactionId() {
        StringBuilder randomPart = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(ALPHANUMERIC.length());
            randomPart.append(ALPHANUMERIC.charAt(index));
        }
        long timestamp = System.currentTimeMillis();
        return String.format("PAY_%s_%d", randomPart.toString(), timestamp);
    }
}
