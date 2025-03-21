import { Injectable } from "@angular/core";
import { ComponentStore } from "@ngrx/component-store";
import { Cart, MenuItem } from "./models";

const INIT_CART: Cart = {
    menuItems: []
}

@Injectable()
export class RestaurantStore extends ComponentStore<Cart> {
    
    constructor() { super(INIT_CART) }

    // add menu item to cart slice
    readonly addMenuItem = this.updater<MenuItem>(
        (slice: Cart, item: MenuItem) => {

            const itemToUpdate = slice.menuItems.find(mi => mi.name === item.name) // find item
            if (itemToUpdate) {

                itemToUpdate.quantity++ // update quantity if menu item exists
                const deletedMenuItems = slice.menuItems.filter(mi => mi.name !== item.name) // remove item from existing array
                const deletedSlice: Cart = { ...slice, menuItems: deletedMenuItems } // update slice
                return { menuItems: [ ...deletedSlice.menuItems, itemToUpdate ] }// add item to array with updated quantity

            } else {

                item.quantity++
                const newSlice: Cart = {
                    menuItems: [ ...slice.menuItems, item ] // if item doesnt exist in array, add it to array
                }
                return newSlice

            }
        }
    )

    // reduce quantity of menu item in cart slice
    readonly removeMenuItem = this.updater<string>(
        (slice: Cart, itemName: string) => {

            const itemToDelete = slice.menuItems.find(mi => mi.name === itemName)
            if (itemToDelete && itemToDelete.quantity > 1) {

                itemToDelete.quantity-- // update quantity if menu item exists
                const deletedMenuItems = slice.menuItems.filter(mi => mi.name !== itemName) // remove item from existing array
                const deletedSlice: Cart = { ...slice, menuItems: deletedMenuItems } // update slice
                return { menuItems: [ ...deletedSlice.menuItems, itemToDelete ] } // return new slice with updated item quantity

            } else {

                itemToDelete!.quantity = 0 // make item quantity 0
                const updatedMenuItems = slice.menuItems.filter(mi => mi.name !== itemName) // delete item from array
                return { ...slice, menuItems: updatedMenuItems } // return new slice with updated items

            }
        }
    )

    // get num of items in cart
    readonly getTotalQuantity = this.select<number>(
        (slice: Cart) => {
            let totalQty = 0
            slice.menuItems.forEach(
                (mi) => totalQty += mi.quantity
            )
            return totalQty
        }
    )

    // get total price of items in cart
    readonly getTotalPriceOfMenuItems = this.select<number>(
        (slice: Cart) => {
            let totalPrice = 0
            slice.menuItems.forEach(
                (mi) => {
                    totalPrice += (mi.quantity * mi.price) // add price of items to total price
                }
            )
            return totalPrice
        }
    )

    // get all items in cart
    readonly getMenuItems = this.select<MenuItem[]>(
        (slice: Cart) => {
            return slice.menuItems
        }
    )

    // delete all items in cart
    readonly deleteAllMenuItems = this.updater<void>(
        (slice: Cart) => {
            return INIT_CART // re-initialise cart
        }
    )

}