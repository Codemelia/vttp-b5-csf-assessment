import { Component, EventEmitter, inject, OnDestroy, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RestaurantStore } from '../restaurant.store';
import { catchError, map, Observable, Subscription, tap, throwError } from 'rxjs';
import { MenuItem, Order, OrderItem, OrderResponse } from '../models';
import { Router } from '@angular/router';
import { RestaurantService } from '../restaurant.service';
import { sha224 } from 'js-sha256';

@Component({
  selector: 'app-place-order',
  standalone: false,
  templateUrl: './place-order.component.html',
  styleUrl: './place-order.component.css'
})
export class PlaceOrderComponent implements OnInit, OnDestroy {

  // TODO: Task 3

  // form
  private fb = inject(FormBuilder)
  protected form!: FormGroup

  // inject component store, service, router
  private restStore = inject(RestaurantStore)
  private router = inject(Router)
  private restSvc = inject(RestaurantService)

  // menu items from component store - pass to html in async pipe
  menuItems$!: Observable<MenuItem[]>

  // item index
  itemIndex: number = 0

  // total price of order
  totalPriceOfMenuItems$!: Observable<number>

  // order to send to server
  order!: Order
  orderItems: OrderItem[] = []

  // sub
  orderSub!: Subscription
  submitSub!: Subscription

  ngOnInit(): void {
    this.createForm() // create form on init
    this.menuItems$ = this.restStore.getMenuItems // get menu items on init
    this.totalPriceOfMenuItems$ = this.restStore.getTotalPriceOfMenuItems // get total price on init
  }

  // create form
  createForm() {
    this.form = this.fb.group({
      username: this.fb.control<string>('', 
        [ Validators.required, Validators.maxLength(64) ]
      ),
      password: this.fb.control<string>('', 
        [ Validators.required, Validators.maxLength(128) ]
      )
    })
  }

  // discard order on start over
  discardOrder() {
    console.log('>>> Deleting menu items')
    this.restStore.deleteAllMenuItems()
    this.router.navigate(['/']) // navigate back to view 1
  }

  // submit order to send to server
  submitOrder() {
    this.orderSub = this.restStore.getMenuItems
      .pipe(tap(
        (menuItems) => {
          menuItems.forEach(
            (mi) => {
              const oi = { id: mi.id, price: mi.price, quantity: mi.quantity }
              this.orderItems = [ ...this.orderItems, oi ] // add each item to order
            }
          )
          return this.orderItems
        }
      ), catchError(
        (error) => {
          throw error
        }
      )
    ).subscribe()

    console.log('>>> Order items: ', this.orderItems)

    let password = this.form.value.password
    // map form and items to order
    const order = { 
      username: this.form.value.username,
      password: sha224.update(password).hex(),
      items: this.orderItems
    }

    console.log('>>> Submitting order: ', order)

    this.submitSub = this.restSvc.submitOrder(order).pipe(
      map(
        (response: OrderResponse) => {
          console.log('>>> Order response: ', response)
          this.restSvc.saveOrderResponse(response) // save
          this.router.navigate(['/confirm'])// navigate to view 3
        }
      ),
      catchError(
        (error: OrderResponse) => {
          console.error('>>> Error after order submit: ', error)
          alert(error.message)
          return throwError(() => error)
        } 
      )
    ).subscribe()
  }

  ngOnDestroy(): void {
    this.orderSub?.unsubscribe()
    this.submitSub?.unsubscribe()
  }

}
