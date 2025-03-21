import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { MenuItem } from '../models';
import { RestaurantService } from '../restaurant.service';
import { catchError, firstValueFrom, Observable, of, Subscription, tap } from 'rxjs';
import { RestaurantStore } from '../restaurant.store';
import { Router } from '@angular/router';

@Component({
  selector: 'app-menu',
  standalone: false,
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent implements OnInit, OnDestroy {

  // inject service and component store
  private restSvc = inject(RestaurantService)
  private restStore = inject(RestaurantStore)
  private router = inject(Router)

  // sub
  restSub!: Subscription

  // observable holding menu items
  // async pipe in html
  menuItems$!: Observable<MenuItem[]>

  // total sum of items in cart - defaults to 0
  totalQtyOfItems$!: Observable<number>

  // total price of items in cart - defaults to 0.00
  totalPriceOfItems$!: Observable<number>

  ngOnInit(): void {
    
    // get menu items from server on init
    this.menuItems$ = this.restSvc.getMenuItems()

    // get num of menu items selected from component store
    this.totalQtyOfItems$ = this.restStore.getTotalQuantity

    // get total price of menu items selected from component store
    this.totalPriceOfItems$ = this.restStore.getTotalPriceOfMenuItems

  }

  // add item from selection to component store
  addItem(mi: MenuItem) {  
    if (mi.quantity == null) mi.quantity = 0 // assign 0 to mi quantity if null
    console.log('>>> Quantity for menu item: ', mi.quantity)
    this.restStore.addMenuItem(mi) // add menu item to component store
  }

  // remove item from selection
  removeItem(miName: string) {
    console.log('>>> Removing item: ', miName)
    this.restStore.removeMenuItem(miName) // evaluates if quantity < 1, deletes accordingly
  }

  // place order - route to place order page
  placeOrder() {
    console.log('>>> Navigating to place-order page')
    this.router.navigate(['/place-order'])
  }

  ngOnDestroy(): void {
    this.restSub?.unsubscribe() // unsub
  }

}
