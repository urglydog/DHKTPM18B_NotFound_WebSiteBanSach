package com.notfound.bookstore.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.bookstore.config.ZaloPayConfig;
import com.notfound.bookstore.exception.AppException;
import com.notfound.bookstore.exception.ErrorCode;
import com.notfound.bookstore.model.dto.request.paymentrequest.PaymentRequest;
import com.notfound.bookstore.model.dto.request.paymentrequest.ZaloPayCallbackRequest;
import com.notfound.bookstore.model.dto.response.paymentresponse.CreatePaymentResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.PaymentResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.ZaloPayCallBackResponseDTO;
import com.notfound.bookstore.model.entity.Order;
import com.notfound.bookstore.model.entity.Payment;
import com.notfound.bookstore.model.enums.OrderStatus;
import com.notfound.bookstore.model.enums.PaymentMethod;
import com.notfound.bookstore.model.enums.PaymentStatus;
import com.notfound.bookstore.model.mapper.PaymentMapper;
import com.notfound.bookstore.repository.OrderRepository;
import com.notfound.bookstore.repository.PaymentRepository;
import com.notfound.bookstore.util.HMACUtil;
import com.notfound.bookstore.util.ZaloPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZaloPayServiceImpl {

    private final ZaloPayUtil zaloPay;
    private final ObjectMapper objectMapper;
    private final ZaloPayConfig properties;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public CreatePaymentResponse createOrderTransaction(PaymentRequest body) {
        Order order = orderRepository.findById(body.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        try {
            String appTransId = ZaloPayUtil.getCurrentTimeString("yyMMdd") + "_" + new Date().getTime();

            log.info("Creating ZaloPay payment - Order ID: {}, Amount: {}, AppTransId: {}",
                    body.getOrderId(), body.getAmount(), appTransId);

            Map<String, Object> orderResponse = zaloPay.createOrderZaloPay(
                    appTransId,
                    "bookstore",
                    body.getAmount()
            );

            log.info("ZaloPay full response: {}", orderResponse);

            // ZaloPay v001 API trả về key không có underscore
            Integer returnCode = (Integer) orderResponse.get("returncode");
            String returnMessage = (String) orderResponse.get("returnmessage");
            String orderUrl = (String) orderResponse.get("orderurl");
            String zpTransToken = (String) orderResponse.get("zptranstoken");

            log.info("returncode: {}", returnCode);
            log.info("returnmessage: {}", returnMessage);
            log.info("orderurl: {}", orderUrl);
            log.info("zptranstoken: {}", zpTransToken);

            if (returnCode == null || returnCode != 1) {
                String errorMsg = returnMessage != null && !returnMessage.isEmpty()
                        ? returnMessage
                        : "Unknown error (returncode: " + returnCode + ")";

                log.error("ZaloPay payment failed - returncode: {}, returnmessage: {}",
                        returnCode, errorMsg);

                throw new RuntimeException("ZaloPay error [" + returnCode + "]: " + errorMsg);
            }

            // Update order
            order.setPaymentMethod(PaymentMethod.ZALOPAY.name());
            orderRepository.save(order);

            // Create payment record
            Payment payment = Payment.builder()
                    .transactionId(appTransId)
                    .paymentMethod(PaymentMethod.ZALOPAY.name())
                    .status(PaymentStatus.PENDING)
                    .amount(body.getAmount())
                    .order(order)
                    .build();

            paymentRepository.save(payment);

            log.info("ZaloPay payment created successfully - Transaction ID: {}", appTransId);

            return paymentMapper.toSuccessResponse(payment, orderUrl);

        } catch (Exception e) {
            log.error("Failed to create ZaloPay payment for order {}: {}",
                    body.getOrderId(), e.getMessage(), e);
            return paymentMapper.toErrorResponse("Failed to create payment: " + e.getMessage());
        }
    }

    public ZaloPayCallBackResponseDTO processCallback(ZaloPayCallbackRequest body) {
        String reqMac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, properties.getZap_Key2(), body.getData());
        if (reqMac != null && reqMac.equals(body.getMac())) {
            try {

                Map<String, Object> callbackData = objectMapper.readValue(
                        body.getData(),
                        new TypeReference<Map<String, Object>>() {
                        }
                );

                String appTransId = (String) callbackData.get("app_trans_id");
                String zpTransId = (String) callbackData.get("zp_trans_id");
                Long amount = ((Number) callbackData.get("amount")).longValue();

                Payment payment = paymentRepository.findPaymentByTransactionId(appTransId)
                        .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setDate(LocalDateTime.now());
                paymentRepository.save(payment);

                Order order = payment.getOrder();
                order.setStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);

                log.info("Payment updated: {} - ZP Trans: {}", appTransId, zpTransId);

                return ZaloPayCallBackResponseDTO.builder()
                        .returnCode(1)
                        .returnMessage("Thành công")
                        .build();

            } catch (Exception e) {
                throw new AppException(ErrorCode.PAYMENT_NOT_FOUND);
            }
        } else {
            return ZaloPayCallBackResponseDTO.builder()
                    .returnCode(-1)
                    .returnMessage("Thất bại")
                    .build();
        }
    }

//    public GetOrderZaloPayResponseDTO getOrderTransaction(String appTransId) {
//        Payment payment = paymentRepository.findByCodeAndDeleted(appTransId, false).orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng"));
//        try {
//            Map<String, Object> orderResponse = zaloPay.getOrderZaloPay(appTransId);
//            int returnCode = (int) orderResponse.get("return_code");
//            String message = (String) orderResponse.get("return_message");
//            String zpTransId = orderResponse.get("zp_trans_id").toString();
//
//            GetOrderZaloPayResponseDTO.Status status;
//            if (returnCode == 1) {
//                status = GetOrderZaloPayResponseDTO.Status.SUCCESS;
//                payment.setStatus(PaymentStatus.PAID);
//                payment.setTransactionId(zpTransId);
//                paymentRepository.save(payment);
//            } else if (returnCode == 2) {
//                status = GetOrderZaloPayResponseDTO.Status.FAILED;
//            } else {
//                status = GetOrderZaloPayResponseDTO.Status.PENDING;
//            }
//
//            return GetOrderZaloPayResponseDTO.builder()
//                    .status(status)
//                    .message(message)
//                    .zpTransId(orderResponse.get("zp_trans_id").toString())
//                    .build();
//        } catch (IOException | URISyntaxException e) {
//            throw new BadRequestException("Error when get order transaction");
//        }
//    }

}
