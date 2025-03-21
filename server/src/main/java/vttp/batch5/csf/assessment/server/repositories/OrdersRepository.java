package vttp.batch5.csf.assessment.server.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import vttp.batch5.csf.assessment.server.models.Item;

@Repository
public class OrdersRepository {

  // autowire mongotemplate
  @Autowired
  private MongoTemplate template;

  // collection name
  private static final String MENUS_COLLECTION = "menus";
  private static final String ORDERS_COLLECTION = "orders";

  // TODO: Task 2.2
  // You may change the method's signature
  // Write the native MongoDB query in the comment below
  /*
    db.menus.find()
      .sort( { name: 1 } )
  */
  // retrieve menu items from mongodb in ascending order
  // return List<Document>
  public List<Document> getMenu() {

    // new query
    Query query = new Query();
    
    // apply sorting - ascending by name
    query.with(Sort.by(Sort.Direction.ASC, "name"));

    // execute command
    return template.find(query, Document.class, MENUS_COLLECTION);
  }

  // TODO: Task 4
  // Write the native MongoDB query for your access methods in the comment below
  /*
    db.orders.insert({...}) 
  */
  // insert order details into mongodb after payment success
  public Document insertOrder(String orderId, String paymentId, String username, 
    double total, Date orderDate, JsonArray itemsArray) {

    // convert items array to list
    List<Item> items = new ArrayList<>();

    for (JsonValue v : itemsArray) {

      JsonObject itemJson = v.asJsonObject();

      Item item = new Item(
        itemJson.getString("id"), 
        itemJson.getJsonNumber("price").doubleValue(),
        itemJson.getInt("quantity"));

      items.add(item);

    }
      
    // build document
    Document doc = new Document()
      .append("_id", orderId)
      .append("order_id", orderId)
      .append("payment_id", paymentId)
      .append("username", username)
      .append("total", total)
      .append("timestamp", orderDate)
      .append("items", items);

    System.out.printf(">>> Inserting order into Mongo: %s", doc.toString());

    return template.insert(doc, ORDERS_COLLECTION);

  }
  
}
