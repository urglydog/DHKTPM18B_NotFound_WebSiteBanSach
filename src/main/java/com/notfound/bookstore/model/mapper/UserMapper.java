package com.notfound.bookstore.model.mapper;

import com.notfound.bookstore.model.dto.response.userresponse.UserResponse;
import com.notfound.bookstore.model.entity.User;
import com.notfound.bookstore.model.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "role", target = "role", qualifiedByName = "roleToString")
    @Mapping(source = "isEmailVerified", target = "emailVerified")
    @Mapping(source = "avatar_url", target = "avatarUrl")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "lastLogin", target = "lastLogin")
    @Mapping(source = "membershipTier", target = "membershipTier", qualifiedByName = "membershipTierToString")
    @Mapping(source = "points", target = "points")
    @Mapping(source = "providerId", target = "providerId")
    UserResponse toUserResponse(User user);

    @Named("roleToString")
    default String roleToString(Role role) {
        return role != null ? role.name() : null;
    }

    @Named("membershipTierToString")
    default String membershipTierToString(User.MembershipTier membershipTier) {
        return membershipTier != null ? membershipTier.name() : null;
    }
}