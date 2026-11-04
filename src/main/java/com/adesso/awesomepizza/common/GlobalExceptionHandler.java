package com.adesso.awesomepizza.common;

import com.adesso.awesomepizza.auth.InvalidKitchenTokenException;
import com.adesso.awesomepizza.menu.PizzaNotAvailableException;
import com.adesso.awesomepizza.order.InvalidOrderTransitionException;
import com.adesso.awesomepizza.order.OrderLockedException;
import com.adesso.awesomepizza.order.OrderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidOrderTransitionException.class)
    public ProblemDetail handleInvalidOrderTransition(InvalidOrderTransitionException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OrderLockedException.class)
    public ProblemDetail handleOrderLocked(OrderLockedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFound(OrderNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PizzaNotAvailableException.class)
    public ProblemDetail handlePizzaNotAvailable(PizzaNotAvailableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidKitchenTokenException.class)
    public ProblemDetail handleInvalidKitchenToken(InvalidKitchenTokenException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error while handling the request", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new TreeMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "Request validation failed.");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, typeMismatchDetail(ex));
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    private String typeMismatchDetail(TypeMismatchException ex) {
        if (!(ex instanceof MethodArgumentTypeMismatchException argumentMismatch)) {
            return "Invalid value '" + ex.getValue() + "'.";
        }

        String detail = "Invalid value '" + ex.getValue() + "' for parameter '" + argumentMismatch.getName() + "'.";
        Class<?> requiredType = ex.getRequiredType() != null && Collection.class.isAssignableFrom(ex.getRequiredType())
                ? argumentMismatch.getParameter().nested().getNestedParameterType()
                : ex.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            detail += " Allowed values: " + Arrays.toString(requiredType.getEnumConstants());
        }
        return detail;
    }
}
