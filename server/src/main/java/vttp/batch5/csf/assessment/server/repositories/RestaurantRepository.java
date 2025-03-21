package vttp.batch5.csf.assessment.server.repositories;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// Use the following class for MySQL database

@Repository
public class RestaurantRepository {

    // autowire jdbc template
    @Autowired
    private JdbcTemplate template;

    // query statements
    private final String CUSTOMER_CHECK = """
        SELECT EXISTS(SELECT 1 FROM customers WHERE username = ? AND password = ?);     
    """;

    private final String INSERT_ORDER = """
        INSERT INTO place_orders (order_id, payment_id, order_date, total, username)
            VALUES (?, ?, ?, ?, ?);      
    """;

    // check if user and password match and exist in mysql
    public int validateUser(String username, String password) {

        return template.queryForObject(
            CUSTOMER_CHECK, 
            Integer.class, 
            username, password);

    }

    // insert order into mysql
    // return num of rows inserted
    public int insertOrder(String orderId, String paymentId, Date orderDate,
        double total, String username) {
        
        return template.update(
            INSERT_ORDER,
            orderId,
            paymentId,
            orderDate,
            total,
            username);

    }

}
