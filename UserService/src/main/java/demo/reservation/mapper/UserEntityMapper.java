package demo.reservation.mapper;

import demo.reservation.model.entity.UserEntity;
import demo.reservation.model.UserRequestDto;
import demo.reservation.model.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

//@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
//        componentModel = MappingConstants.ComponentModel.SPRING)
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserEntityMapper {
    UserEntity toUserEntity(UserRequestDto requestDto);
    UserResponseDto toUserResponseDto(UserEntity entity);

}
