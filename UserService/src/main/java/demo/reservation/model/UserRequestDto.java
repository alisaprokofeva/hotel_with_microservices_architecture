package demo.reservation.model;

public record UserRequestDto (
        String name,
        String email,
        String password
){
}
