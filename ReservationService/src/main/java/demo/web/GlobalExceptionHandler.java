package demo.web;


import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponceDto> handleGenericException(
        Exception e
    ){
        var errorDto = new ErrorResponceDto(
                "Internal server error",
                e.getMessage(),
                LocalDateTime.now()
        );
        log.error("Handle exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponceDto> handleEntityNotFoundException(
            EntityNotFoundException e
    ){
        var errorDto = new ErrorResponceDto(
                "Entity not found",
                e.getMessage(),
                LocalDateTime.now()
        );
        log.error("Handle entityNotFoundException", e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(exception = {IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorResponceDto> handleBadRequest(
           Exception e
    ){
        var errorDto = new ErrorResponceDto(
                "Bad request",
                e.getMessage(),
                LocalDateTime.now()
        );
        log.error("Handle badRequestException", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }
}
