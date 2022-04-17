package com.gstoupis.gps.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.gstoupis.gps.google.GoogleServiceFactory;
import com.gstoupis.gps.google.GoogleServiceType;
import com.gstoupis.gps.service.model.Prescription;
import com.gstoupis.gps.service.parser.HtmlParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrescriptionService {

  private static final Logger LOG = LoggerFactory.getLogger(PrescriptionService.class);

  private final Gmail gmail;
  private final Calendar calendar;

  public PrescriptionService() throws IOException, GeneralSecurityException {
    this.gmail = (Gmail) GoogleServiceFactory.getService(GoogleServiceType.GMAIL);
    this.calendar = (Calendar) GoogleServiceFactory.getService(GoogleServiceType.CALENDAR);
  }

  public List<Prescription> getActivePrescriptions() throws IOException {

    //TODO: parameterize the below
    String user = "me";
    String query = "from:Hs-no.reply@e-prescription.gr AND subject:Έκδοση";
    Long maxResults = 100L;

    // Read messages
    ListMessagesResponse listMessagesResponse = gmail.users().messages().list(user).setQ(query).setMaxResults(maxResults).execute();
    List<Message> messages = listMessagesResponse.getMessages();
    LOG.info("Got {} prescription emails", messages.size());

    HtmlParser htmlParser = new HtmlParser();
    List<Prescription> prescriptions = new ArrayList<>();
    for (Message message : messages) {
      Message detailedMessage = gmail.users().messages().get(user, message.getId()).execute();
      byte[] data = detailedMessage.getPayload().getBody().decodeData();
      String dataString = new String(data, StandardCharsets.UTF_8);
      Prescription prescription = htmlParser.parsePrescriptionEmailHtml(dataString);

      //Non-expired prescriptions only
      //TODO: Add check for successful prescription claim - do not include already claimed prescriptions
      if (prescription.isValid() && prescription.isActive() && prescription.isUnclaimed()) {
        prescriptions.add(prescription);
      }
    }

    return prescriptions;
  }

  public void createEventsForPrescriptions(List<Prescription> prescriptions) {

    List<Event> events = new ArrayList<>();

    prescriptions.forEach(prescription -> events.add(this.translatePrescriptionToEvent(prescription)));

    for (Event event : events) {
      LOG.info("Creating event: {} ({})", event.getSummary(), event.getStart().getDate());
//      calendar.events().insert("primary", event).execute();
    }

  }

  private Event translatePrescriptionToEvent(Prescription prescription) {
    Event event = new Event();

    event.setSummary("Prescription: " + prescription.id());

    EventDateTime start = new EventDateTime();
    start.setDate(new DateTime(prescription.from().format(DateTimeFormatter.ISO_DATE)));
    event.setStart(start);

    EventDateTime end = new EventDateTime();
    end.setDate(new DateTime(prescription.to().format(DateTimeFormatter.ISO_DATE)));
    event.setEnd(end);

    return event;
  }

}
