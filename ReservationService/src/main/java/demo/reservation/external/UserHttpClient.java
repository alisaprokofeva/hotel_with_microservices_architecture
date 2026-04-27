package demo.reservation.external;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@HttpExchange(
        accept = "application/json",
        url = "/api/v1/users"
)
public interface UserHttpClient {

    @GetExchange("/me")
    UserProfileResponseDto getCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader
    );

    @GetExchange("/{id}/exists")
    void checkUserExists(@PathVariable("id") Long id);
}
