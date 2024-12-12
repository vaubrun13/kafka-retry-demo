package com.acme.kafkaretrydemo.exceptions;

public class MyFatalException extends RuntimeException {

  public MyFatalException(String message) {
    super(message);
  }
}
