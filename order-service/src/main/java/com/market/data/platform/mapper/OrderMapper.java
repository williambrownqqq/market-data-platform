package com.market.data.platform.mapper;


import com.market.data.platform.dto.kafka.OrderEvent;
import com.market.data.platform.dto.request.OrderRequestDTO;
import com.market.data.platform.dto.response.OrderResponseDTO;
import com.market.data.platform.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderRequestDTO request);

    OrderResponseDTO toResponse(Order order);

    OrderEvent orderEventToEntity(Order requestDTO);

    List<OrderResponseDTO> toEntityList(List<Order> orders);
}