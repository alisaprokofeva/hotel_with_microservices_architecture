package demo.reservation.mapper;

import demo.reservation.model.entity.UserEntity;
import demo.reservation.model.UserRequestDto;
import demo.reservation.model.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserEntity toUserEntity(UserRequestDto requestDto) {
        return UserEntity.builder()
                .name(requestDto.name())
                .email(requestDto.email())
                .password(requestDto.password())
                .build();
    }

    public UserResponseDto toUserResponseDto(UserEntity entity) {
        return new UserResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getRole()
        );
    }
}
