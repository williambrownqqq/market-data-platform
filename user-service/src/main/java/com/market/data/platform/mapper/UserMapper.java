package com.market.data.platform.mapper;

import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.market.data.platform.dto.response.UserDTO;
import com.market.data.platform.dto.response.UserResponseDTO;
import com.market.data.platform.model.User;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = BalanceTransactionMapper.class
)
public interface UserMapper {

    UserDTO toDto(User user);

    List<UserDTO> toDtoList(List<User> user);

    List<User> toEntityList(List<UserDTO> dtoList);

    User toEntity(UserDTO userDTO);
}