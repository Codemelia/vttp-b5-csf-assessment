import { HttpClient, HttpHeaders } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { MenuItem, Order, OrderResponse } from "./models";
import { Observable } from "rxjs";

@Injectable()
export class RestaurantService {

  // inject http
  private http = inject(HttpClient)

  orderResponse!: OrderResponse

  // TODO: Task 2.2
  // You change the method's signature but not the name
  // gets menu items from server
  // returns observable of menu items
  getMenuItems(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>('/api/menu')
  }

  // TODO: Task 3.2
  // send order to server as app json
  submitOrder(order: Order): Observable<OrderResponse> {

    const headers: HttpHeaders = new HttpHeaders({
      'Content-Type': 'application/json'
    })

    return this.http.post<OrderResponse>('/api/food_order', order,
    { headers: headers })
  }

  saveOrderResponse(response: OrderResponse) {
    this.orderResponse = response
  }

  getOrderResponse() {
    return this.orderResponse
  }

}
