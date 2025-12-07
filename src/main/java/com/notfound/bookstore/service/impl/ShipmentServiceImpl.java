package com.notfound.bookstore.service.impl;

import com.notfound.bookstore.model.dto.request.shipmentrequest.ShippingFeeRequest;
import com.notfound.bookstore.model.dto.response.shipmentresponse.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShipmentServiceImpl {

    @Value("${shipment.ghn.url}")
    private String ghnApiUrl;

    @Value("${shipment.ghn.apiToken}")
    private String ghnToken;

    @Value("${shipment.ghn.shopId}")
    private String shopId;

    @Value("${shipment.ghn.address.fromDistrictId}")
    private Integer fromDistrictId;

    @Value("${shipment.ghn.address.fromWardCode}")
    private String fromWardCode;

    @Value("${shipment.ghn.defaultBook.length}")
    private Integer defaultBookLength;

    @Value("${shipment.ghn.defaultBook.width}")
    private Integer defaultBookWidth;

    @Value("${shipment.ghn.defaultBook.height}")
    private Integer defaultBookHeight;

    @Value("${shipment.ghn.defaultBook.weight}")
    private Integer defaultBookWeight;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<CustomerProvinceResponse> getProvinces() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = ghnApiUrl + "/master-data/province";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

            return data.stream()
                    .filter(province -> (Integer) province.get("Status") == 1)
                    .map(province -> CustomerProvinceResponse.builder()
                            .provinceId((Integer) province.get("ProvinceID"))
                            .provinceName((String) province.get("ProvinceName"))
                            .code((String) province.get("Code"))
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting provinces from GHN", e);
            throw new RuntimeException("Failed to get provinces", e);
        }
    }

    public List<CustomerDistrictResponse> getDistricts(Integer provinceId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            Map<String, Object> requestBody = Map.of("province_id", provinceId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = ghnApiUrl + "/master-data/district";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

            return data.stream()
                    .filter(district -> (Integer) district.get("Status") == 1)
                    .map(district -> CustomerDistrictResponse.builder()
                            .districtId((Integer) district.get("DistrictID"))
                            .provinceId((Integer) district.get("ProvinceID"))
                            .districtName((String) district.get("DistrictName"))
                            .supportType((Integer) district.get("SupportType"))
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting districts from GHN for provinceId: {}", provinceId, e);
            throw new RuntimeException("Failed to get districts", e);
        }
    }

    public List<CustomerWardResponse> getWards(Integer districtId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            Map<String, Object> requestBody = Map.of("district_id", districtId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = ghnApiUrl + "/master-data/ward";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

            return data.stream()
                    .filter(ward -> (Integer) ward.get("Status") == 1)
                    .map(ward -> CustomerWardResponse.builder()
                            .wardCode(String.valueOf(ward.get("WardCode")))
                            .districtId((Integer) ward.get("DistrictID"))
                            .wardName((String) ward.get("WardName"))
                            .supportType((Integer) ward.get("SupportType"))
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting wards from GHN for districtId: {}", districtId, e);
            throw new RuntimeException("Failed to get wards", e);
        }
    }

    public ShippingCalculationResponse calculateShipping(ShippingFeeRequest request) {
        try {
            // Lấy phí vận chuyển
            Map<String, Object> feeData = getShippingFee(request);

            // Lấy thời gian giao hàng dự kiến
            Map<String, Object> leadTimeResponse = getLeadTime(request.getToDistrictId(), request.getToWardCode());
            Map<String, Object> leadTimeData = (Map<String, Object>) leadTimeResponse.get("data");

            // Lấy thông tin leadtime_order
            Map<String, Object> leadtimeOrder = (Map<String, Object>) leadTimeData.get("leadtime_order");

            LocalDateTime estimatedDeliveryTime = null;
            Integer deliveryDays = null;

            if (leadtimeOrder != null) {
                String toEstimateDate = (String) leadtimeOrder.get("to_estimate_date");
                if (toEstimateDate != null) {
                    estimatedDeliveryTime = LocalDateTime.parse(toEstimateDate,
                            java.time.format.DateTimeFormatter.ISO_DATE_TIME);

                    // Tính số ngày từ hiện tại đến ngày giao
                    deliveryDays = (int) java.time.temporal.ChronoUnit.DAYS
                            .between(LocalDateTime.now(), estimatedDeliveryTime);
                }
            }

            return ShippingCalculationResponse.builder()
                    .totalFee(((Number) feeData.get("total")).intValue())
                    .serviceFee(((Number) feeData.get("service_fee")).intValue())
                    .insuranceFee(((Number) feeData.get("insurance_fee")).intValue())
                    .estimatedDeliveryTime(estimatedDeliveryTime)
                    .deliveryDays(deliveryDays)
                    .build();

        } catch (Exception e) {
            log.error("Error calculating shipping: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate shipping", e);
        }
    }

    private Map<String, Object> getShippingFee(ShippingFeeRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnToken);
        headers.set("ShopId", String.valueOf(shopId));

        Map<String, Object> requestBody = Map.of(
                "service_type_id", 2,
                "from_district_id", fromDistrictId,
                "from_ward_code", fromWardCode,
                "to_district_id", request.getToDistrictId(),
                "to_ward_code", request.getToWardCode(),
                "length", request.getLength() != null ? request.getLength() : defaultBookLength,
                "width", request.getWidth() != null ? request.getWidth() : defaultBookWidth,
                "height", request.getHeight() != null ? request.getHeight() : defaultBookHeight,
                "weight", request.getWeight() != null ? request.getWeight() : defaultBookWeight,
                "insurance_value", request.getInsuranceValue() != null ? request.getInsuranceValue() : 0
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String url = ghnApiUrl + "/v2/shipping-order/fee";

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return (Map<String, Object>) response.getBody().get("data");
    }

    private Map<String, Object> getLeadTime(Integer toDistrictId, String toWardCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnToken);

        Map<String, Object> requestBody = Map.of(
                "from_district_id", fromDistrictId,
                "from_ward_code", fromWardCode,
                "to_district_id", toDistrictId,
                "to_ward_code", toWardCode,
                "service_id", 53320
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String url = ghnApiUrl + "/v2/shipping-order/leadtime";

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }
}