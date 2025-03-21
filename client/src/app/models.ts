// You may use this file to create any models
// TASK 2.2
// model for menu response from server
export interface MenuItem {
    id: string
    name: string
    description: string
    price: number
    quantity: number | 0
}

export interface Cart {
    menuItems: MenuItem[]
}

export interface OrderItem {
    id: string
    price: number
    quantity: number
}

export interface Order {
    username: string
    password: string
    items: OrderItem[]
}

export interface OrderResponse {
    message: string | null
    orderId: string | null
    paymentId: string | null
    total: number | null
    timestamp: number | null
}