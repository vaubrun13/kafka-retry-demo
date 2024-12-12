package com.acme.kafkaretrydemo.models;

import java.util.UUID;

public record RequestObject(UUID id, String firstName, String lastName) {}
