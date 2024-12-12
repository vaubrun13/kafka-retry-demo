package com.acme.kafkaretrydemo.exceptions;

public class BusinessException extends RuntimeException {

  public BusinessException(String message) {
    super(message);
  }
}
