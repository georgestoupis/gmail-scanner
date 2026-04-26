package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import java.time.LocalDateTime;

public interface OrderParser {

  Order parseOrder(EmailData emailData, Source source, LocalDateTime orderDateTime);

}
