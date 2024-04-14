package com.samistax.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.samistax.webhook.dto.BaseEvent;
import com.samistax.webhook.dto.EventsNotFoundException;
import com.samistax.webhook.dto.PrinterEvent;
import com.samistax.webhook.dto.ShipmentEvent;
import com.samistax.webhook.dto.Shipment;
import com.samistax.webhook.service.AstraStreamingService;
import com.samistax.webhook.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@Controller
@Configuration
@EnableScheduling
public class WebhookController {

    //private static boolean USE_PULSAR_PRODUCER = false;
    private static int lastActivityId = 0;

    protected Logger logger = Logger.getLogger(WebhookController.class.getName());
    private List<BaseEvent> events = new ArrayList<>();

    @Autowired
    private AstraStreamingService astraStreamingService;

    @Autowired
    private final WebhookService webhookService;

    @Value("${app.pulsar_producer_enabled:true}")
    private boolean PULSAR_PRODUCER_MODE;

    public WebhookController(AstraStreamingService astraStreamingService, WebhookService webhookService) {
        this.astraStreamingService = astraStreamingService;
        this.webhookService = webhookService;
    }

    @Scheduled(fixedRate = 1000) // delay in millisecond for generating new event
    public void simulatedEventStream() {
        // Simulate event generation background task
        BaseEvent event = generateEvents(1).get(0);
        this.events.add(event);

        System.out.println("Event: " + lastActivityId);

        JsonMapper mapper = new JsonMapper();
        // Convert the POJO to a JSON string
        try {
            String jsonPayload = mapper.writeValueAsString(event);
            if (! PULSAR_PRODUCER_MODE ) {
                // Option #1 invoke Pulsar function using endpoint registered to webhook service.
                webhookService.triggerEvent(jsonPayload);
            } else {
                // Option #2 send Pulsar message to Astra Streaming using message Producer
                astraStreamingService.sendAsynchPulsarMessage(jsonPayload);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/subscribe")
    @ResponseBody
    public ResponseEntity<String> handleSubscribeEvent(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth, @RequestBody String payload) {
        // Process the subscription event
        System.out.println("Received subscription event: " + payload);

        byte[] decodedBytes = Base64.getDecoder().decode(auth.substring(auth.lastIndexOf("Basic ")+6));
        String decodedCredentials = new String(decodedBytes);

        StringTokenizer st = new StringTokenizer(decodedCredentials, ":");
        String userName = st.nextToken();
        String userPwd = st.nextToken();

        if ( userName.equals("user") && userPwd.equals("pass") ) {
            // Initiate asynchronous processing
            try {
                new URL(payload).toURI();
                webhookService.registerWebhook(payload);
                return new ResponseEntity<>("Webhook registered successfully", HttpStatus.OK);
            } catch (MalformedURLException e) {
                return new ResponseEntity<>("Webhook threw MalformedURLException: " + e, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (URISyntaxException e) {
                return new ResponseEntity<>("Webhook threw URISyntaxException: " + e, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Unauthorized access. Check your username and password", HttpStatus.UNAUTHORIZED);
        }
    }
    @PostMapping("/unsubscribe")
    @ResponseBody
    public ResponseEntity<String> handleUnsubscribeEvent(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth, @RequestBody String payload) {
        // Process the subscription event
        System.out.println("Received unsubscribe event: " + payload);

        byte[] decodedBytes = Base64.getDecoder().decode(auth.substring(auth.lastIndexOf("Basic ")+6));
        String decodedCredentials = new String(decodedBytes);

        StringTokenizer st = new StringTokenizer(decodedCredentials, ":");
        String userName = st.nextToken();
        String userPwd = st.nextToken();

        if ( userName.equals("user") && userPwd.equals("pass") ) {
            // Initiate asynchronous processing
            try {
                webhookService.unregisterWebhook(payload);
                return new ResponseEntity<>("Webhook unregistered successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Something went wrong. Exception thrown: " + e, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Unauthorized access. Check your username and password", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerEvent(@RequestBody String jsonPayload) {
        webhookService.triggerEvent(jsonPayload);
        return new ResponseEntity<>("Event triggered successfully.", HttpStatus.OK);
    }

    @RequestMapping("/generateEvents/{eventCount}")
    public List<BaseEvent> generate(@PathVariable("eventCount") String eventCount) {
        logger.info("events-service byTypeId() invoked. TypeId = " + eventCount);
        int count = 0;
        try {
            count = Integer.parseInt(eventCount);
        } catch (NumberFormatException nfe) {}

        List<BaseEvent> generatedEvents = generateEvents(count);
        // Add events to the in memoory events list ofr other methods to be retrieved.
        events.addAll(generatedEvents );
        logger.info("events-service generated: " + generatedEvents.size() + " events" );
        return generatedEvents;

    }

    /**
     * Fetch events with the specified name. A partial case-insensitive match
     * is supported. So <code>http://.../events/owner/a</code> will find any
     * events with upper or lower case 'a' in their name.
     *
     * @param id
     * @return A non-null, non-empty set of events.
     * @throws EventsNotFoundException
     *             If there are no matches at all.
     */
    @RequestMapping("/events/byEventId/{id}")
    @GetMapping(produces = "application/json")
    //public List<BaseEvent> byEventId(@PathVariable("id") String id) {
    public ResponseEntity<String> byEventId(@PathVariable("id") String id) {
        logger.info("events-service byEventId() invoked. TypeId = " + id);

        String jsonString = "";

        JsonMapper mapper = new JsonMapper();
        if (events == null || events.size() == 0)
            throw new EventsNotFoundException(id);
        else {
            if ( id != null && id.strip().length() > 0) {
                int filterId = 0;
                try {
                    filterId = Integer.parseInt(id);
                } catch (NumberFormatException nfe) {}

                List<BaseEvent> filteredEvents = new ArrayList<>();
                //List<ShipmentEvent> filteredEvents = events.stream().filter(e -> String.valueOf(e.getEventId()) == id).toList();
                int finalFilterId = filterId;
                events.forEach(event -> {
                    if (event.getEventId() == finalFilterId) {
                        filteredEvents.add(event);
                    }
                });
                logger.info("events-service byOwner() found: " + filteredEvents.size() + " items. Events" + filteredEvents);
                //return filteredEvents;
                try {
                    jsonString = mapper.writeValueAsString(filteredEvents);
                    System.out.println(jsonString);
                } catch (JsonProcessingException jpe) { }
            }
            //return events;
        }
        return new ResponseEntity<String>(jsonString, HttpStatus.OK);
    }


    @RequestMapping("/events/eventCount")
    public ResponseEntity<Integer> eventCount() {
        System.out.println("Event count -> " + events.size());
        logger.info("events-service eventCount() invoked. Events = " + events.size());
        return new ResponseEntity<Integer>(events.size(), HttpStatus.OK);

    }
    @RequestMapping("/events/clearEvents")
    public ResponseEntity<Integer> clearEvents() {
        logger.info("events-service clearEvents() invoked. Removing " + events.size() +" events.");
        events.clear();
        return new ResponseEntity<Integer>(events.size(), HttpStatus.OK);

    }
    @RequestMapping("/events/all")
    public List<BaseEvent> getAllEvents() {
        logger.info("events-service getAllEvents() invoked. Events = " + events);
        return events;

    }
    // TODO: Add dynamic way of defining evnt types viw json file imports, or list of provided POJO classes, or some other way
    public static BaseEvent generateEvent() {

        BaseEvent returnEvent = new BaseEvent();
        Random random = new Random();
        // Dummy data ganeration variables
        int eventId = random.nextInt(0,3);
        int userId = random.nextInt(0,20);
        int equipmentId = random.nextInt(0,1000);
        // Use one unique id to ensure upserts and inserts are creating new rows in DB.
        lastActivityId += 1;

        if ( eventId == 1 ) {
            int shipmentId = random.nextInt(0,1000);
            int boxType = random.nextInt(0,5);
            int pickListID = random.nextInt(0,100);

            ShipmentEvent event = new ShipmentEvent();
            event.setEventId(eventId); // Assuming eventId starts from 1
            event.setActivityId(lastActivityId); // Assuming activityId starts from 1000
            event.setTaskTypeCode("task-" + event.getEventId()); // Assigning taskTypeCode based on index
            event.setActivated(LocalDateTime.now().toString());
            event.setUserCode("user-" + userId); // Assigning a unique userCode for each event
            event.setEquipmentId("" + equipmentId); // Assigning a unique equipmentId for each event
            event.setPrinterName("<printer not found>");

            // Generating shipments for each event
            List<Shipment> shipments = new ArrayList<>();
            Shipment shipment = new Shipment();
            shipment.setShipmentid(shipmentId);
            shipment.setPicklistid("PL-" + pickListID); // Assuming picklistId starts from 1
            shipment.setDeliverylocation("AST1-01-01-01");
            shipment.setBoxtype("Box" + boxType); // Assigning boxType based on index
            shipment.setOwnercode("");
            shipments.add(shipment);
            event.setShipments(shipments);
            returnEvent = event;
        } else if ( eventId == 2 ){
            PrinterEvent event = new PrinterEvent();
            event.setEventId(eventId); // Assuming eventId starts from 1
            event.setActivityId(lastActivityId); // Assuming activityId starts from 1000
            event.setTaskTypeCode("Task type " + event.getEventId()); // Assigning taskTypeCode based on index
            event.setActivated(LocalDateTime.now().toString());
            event.setUserCode("user" + userId); // Assigning a unique userCode for each event
            event.setEquipmentId("equipment" + equipmentId); // Assigning a unique equipmentId for each event
            event.setPrinterName("<printer not found>");
            returnEvent = event;
        } else {
            BaseEvent event = new BaseEvent();
            event.setEventId(eventId); // Assuming eventId starts from 1
            event.setActivityId(lastActivityId); // Assuming activityId starts from 1000
            event.setTaskTypeCode("Task type " + event.getEventId()); // Assigning taskTypeCode based on index
            event.setActivated(LocalDateTime.now().toString());
            event.setUserCode("JohnDoe");
            returnEvent = event;
        }
         return returnEvent;
    }


    public static List<BaseEvent> generateEvents(int eventCount) {
        List<BaseEvent> events = new ArrayList<>();
        for (int i = 0; i < eventCount; i++) {
            events.add(WebhookController.generateEvent());
        }
        return events;
    }
}