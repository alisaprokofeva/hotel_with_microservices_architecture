package demo.web;

import java.time.LocalDateTime;

//Dto - data transfer object
public record ErrorResponceDto(
        String message,
        String detailedMessage,
        LocalDateTime errorTime
) {

}
