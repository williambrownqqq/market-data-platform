package com.market.data.platform.mapper;

import com.market.data.platform.model.BalanceTransaction;
import com.market.data.platform.dto.response.BalanceTransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface BalanceTransactionMapper {

    @Mapping(source = "user.id", target = "userId")
    BalanceTransactionDTO toDto(BalanceTransaction entity);

    @Mapping(source = "userId", target = "user.id")
    BalanceTransaction toEntity(BalanceTransactionDTO dto);
}
