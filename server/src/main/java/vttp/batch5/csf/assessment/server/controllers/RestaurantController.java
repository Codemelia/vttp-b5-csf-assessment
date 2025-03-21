package vttp.batch5.csf.assessment.server.controllers;

import java.io.StringReader;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import vttp.batch5.csf.assessment.server.services.RestaurantService;

// CONFIGURED PROXY.CONF.JSON

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class RestaurantController {

  // autowire service
  @Autowired
  private RestaurantService restSvc;

  // my official name
  private static final String MY_OFFICIAL_NAME = "Wong Su Hui, Amelia";

  // TODO: Task 2.2
  // You may change the method's signature
  // handle request for menu items from client - no params
  // returns array of menu items in json format
  @GetMapping(path = "/menu")
  public ResponseEntity<String> getMenus() {

    JsonArray menuItems = restSvc.getMenu();
    // System.out.printf(">>> Built JsonArray of menu items: %s", menuItems.toString());
    return ResponseEntity.ok(menuItems.toString());

  }

  // TODO: Task 4
  // Do not change the method's signature
  @PostMapping(path = "/food_order", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> postFoodOrder(@RequestBody String payload) {

    System.out.printf(">>> Received order: %s", payload);

    // read payload as json array
    JsonObject orderJson = Json.createReader(new StringReader(payload))
      .readObject();

    // get username and password
    String username = orderJson.getString("username");
    String password = orderJson.getString("password");

    // validate user
    boolean userValid = restSvc.validateUser(username, password);

    // if username and password don't match/dont exist in mysql, return error message with 401 status
    if (!userValid) {
      JsonObject errorJson = Json.createObjectBuilder()
        .add("message", "Invalid username and/or password")
        .build();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorJson.toString());
    }

    // if valid, generate uuid with 8 char for order
    String orderId = UUID.randomUUID().toString().substring(0, 8);

    // get total price of order items
    double totalOrderPrice = 0.0;
    JsonArray itemsArray = orderJson.getJsonArray("items");
    for (JsonValue v : itemsArray) {
      JsonObject itemJson = v.asJsonObject();
      double itemSumPrice = 
        itemJson.getJsonNumber("price").doubleValue() * itemJson.getInt("quantity"); // get sum price for each item
      totalOrderPrice += itemSumPrice; // add sum price to total order price
    }
    System.out.printf(">>> Order total: %f", totalOrderPrice);

    // invoke payment gateway service
    try {

      Map<String, Object> feedback = 
        restSvc.makePayment(orderId, username, MY_OFFICIAL_NAME, totalOrderPrice, itemsArray);
      
      JsonObject successJson = Json.createObjectBuilder()
        .add("orderId", orderId)
        .add("paymentId", (String) feedback.get("paymentId"))
        .add("total", totalOrderPrice)
        .add("timestamp", (Long) feedback.get("timestamp"))
        .build();
      return ResponseEntity.ok(successJson.toString());

    } catch (Exception e) {

      System.out.printf(">>> Error occurred while saving: %s", e.getMessage());
      JsonObject errorJson = Json.createObjectBuilder()
        .add("message", e.getMessage())
        .build();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(errorJson.toString());

    }

  }
}
