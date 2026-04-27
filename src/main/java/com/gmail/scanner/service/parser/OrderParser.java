package com.gmail.scanner.service.parser;

import java.util.Optional;

public interface OrderParser {
  Optional<String> parseOrderPrice(EmailData emailData);
}
