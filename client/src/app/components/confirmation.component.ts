import { Component, inject, OnInit } from '@angular/core';
import { RestaurantService } from '../restaurant.service';
import { OrderResponse } from '../models';
import { formatDate } from '@angular/common';

@Component({
  selector: 'app-confirmation',
  standalone: false,
  templateUrl: './confirmation.component.html',
  styleUrl: './confirmation.component.css'
})
export class ConfirmationComponent implements OnInit {

  // TODO: Task 5
  private restSvc = inject(RestaurantService)

  response!: OrderResponse
  date!: string

  ngOnInit(): void {
    this.response = this.restSvc.getOrderResponse()
    console.log('>>> Timestamp: ', this.response.timestamp)
    this.date = new Date(this.response.timestamp!).toLocaleDateString()
    // invalid date
  }

}
