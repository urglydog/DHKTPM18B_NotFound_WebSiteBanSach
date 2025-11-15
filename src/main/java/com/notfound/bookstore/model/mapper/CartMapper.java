package com.notfound.bookstore.model.mapper;

import com.notfound.bookstore.model.entity.Cart;
import com.notfound.bookstore.model.entity.CartItem;
import com.notfound.bookstore.model.dto.response.cartresponse.CartItemResponse;
import com.notfound.bookstore.model.dto.response.cartresponse.CartResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartItemResponse toCartItemResponse(CartItem cartItem) {
        return CartItemResponse.builder()
                .itemId(cartItem.getItemID())
                .bookId(cartItem.getBook().getId())
                .bookTitle(cartItem.getBook().getTitle())
                .bookIsbn(cartItem.getBook().getIsbn())
                .bookPrice(cartItem.getBook().getPrice())
                .bookDiscountPrice(cartItem.getBook().getDiscountPrice())
                .bookImageUrl(cartItem.getBook().getImages() != null && !cartItem.getBook().getImages().isEmpty()
                    ? cartItem.getBook().getImages().get(0).getUrl()
                    : null)
                .quantity(cartItem.getQuantity())
                .subTotal(cartItem.getSubTotal())
                .stockQuantity(cartItem.getBook().getStockQuantity())
                .build();
    }

    public CartResponse toCartResponse(Cart cart, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getCartID())
                .userId(cart.getUser().getId())
                .items(itemResponses)
                .itemCount((long) items.size())
                .totalPrice(items.stream().mapToDouble(CartItem::getSubTotal).sum())
                .build();
    }
}
