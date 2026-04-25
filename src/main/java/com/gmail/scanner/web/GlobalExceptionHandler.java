package com.gmail.scanner.web;

import com.gmail.scanner.exception.InsufficientScopeException;
import com.gmail.scanner.exception.UnsupportedGroupException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({UnsupportedGroupException.class})
  public ResponseEntity<Object> handleUnsupportedGroupException(Exception exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
  }

  @ExceptionHandler({InsufficientScopeException.class})
  public ResponseEntity<Object> handleInsufficientScopeException(Exception exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "insufficient_scope"));
  }
}
