package vttp.batch5.csf.assessment.server.services;

import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import vttp.batch5.csf.assessment.server.repositories.OrdersRepository;
import vttp.batch5.csf.assessment.server.repositories.RestaurantRepository;

@Service
public class RestaurantService {

  // autowire repo
  @Autowired
  private OrdersRepository ordersRepo;

  @Autowired
  private RestaurantRepository restRepo;

  // rest template
  private final RestTemplate template = new RestTemplate();

  // payment gateway link
  private static final String PAYMENT_URL = "https://payment-service-production-a75a.up.railway.app/api/payment";

  // TODO: Task 2.2
  // You may change the method's signature
  // get menu items from mongodb to pass to client
  public JsonArray getMenu() {

    // retrieve menu items as docs from mongodb
    List<Document> menuItemsDocs = ordersRepo.getMenu();

    // new jsonarraybuilder to build
    JsonArrayBuilder jBuilder = Json.createArrayBuilder();
    
    for (Document doc : menuItemsDocs) {

      // create json object
      JsonObject menuItemObject = Json.createObjectBuilder()
        .add("id", doc.getString("_id"))
        .add("name", doc.getString("name"))
        .add("description", doc.getString("description"))
        .add("price", doc.getDouble("price"))
        .build();

      jBuilder.add(menuItemObject); // add each json object to json array

    }

    // build and return json array
    return jBuilder.build();

  }
  
  // TODO: Task 4
  
  // username password validation
  public boolean validateUser(String username, String password) {

    return restRepo.validateUser(username, password) > 0; // returns true if > 0 rows returned

  }

  // invoke payment gateway service
  public Map<String, Object> makePayment(String orderId, String payer, String payee, double payment, JsonArray itemsArray) {

    // build json object
    JsonObject paymentJson = Json.createObjectBuilder()
      .add("order_id", orderId)
      .add("payer", payer)
      .add("payee", payee)
      .add("payment", payment)
      .build();

    System.out.printf(">>> Payment JSON built: %s", paymentJson.toString());

    /* 
     // set with and content type
      HttpHeaders headers = new HttpHeaders();
      headers.add("X-Authenticate", payer); // authentication header
      headers.setContentType(MediaType.APPLICATION_JSON);
      List<MediaType> mediaTypes = new ArrayList<>();
      mediaTypes.add(MediaType.APPLICATION_JSON);
      headers.setAccept(mediaTypes);

      URI paymentUri = URI.create(PAYMENT_URL);

      RequestEntity<JsonObject> request = new RequestEntity<>(
      paymentJson, headers, 
      HttpMethod.POST, paymentUri);
    */

    RequestEntity<String> request = RequestEntity
      .post(PAYMENT_URL)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Accept", MediaType.APPLICATION_JSON_VALUE)
      .header("X-Authenticate", payer)
      .body(paymentJson.toString(), JsonObject.class);

    ResponseEntity<String> response = template.exchange(request, String.class);

    // read response
    String responseString = response.getBody();
    System.out.printf(">>> Payment gateway response: %s", responseString);
    JsonObject responseJson = Json.createReader(new StringReader(responseString))
      .readObject();

    // extract response details if successful
    if (responseJson.containsKey("payment_id")) {

      String paymentId = responseJson.getString("payment_id");
      String respOrderId = responseJson.getString("order_id");
      Long timestamp = responseJson.getJsonNumber("timestamp").longValue();
      Date orderDate = new Date(timestamp);

      // insert orderid, payment id, date, total, username into mysql
      int rowsInsertSQL = restRepo.insertOrder(respOrderId, paymentId, orderDate, payment, payer);

      if (rowsInsertSQL > 0) {
        // insert orderId, paymentId, username, date, items array into mongodb
        Document insertDoc = ordersRepo.insertOrder(orderId, paymentId, payer, payment, orderDate, itemsArray);

        if (insertDoc.containsKey("_id")) {
          System.out.println(">>> Mongo and SQL insert successful");
          Map<String, Object> feedback = new HashMap<>();
          feedback.put("paymentId", paymentId);
          feedback.put("timestamp", timestamp);
          return feedback;
        } else {
          System.out.println(">>> Mongo insert failed");
          return null;
        }

      } else {
        System.out.println(">>> SQL insert failed");
        return null;
      }
      
    }

    System.out.println(">>> Payment failed");
    return null;

  }

}
