// a way for the customer to pay. pay() collects the payment and returns how much was paid
interface PaymentMethod {
    int pay(int cost);
}
