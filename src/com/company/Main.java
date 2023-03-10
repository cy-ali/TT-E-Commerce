package com.company;

import com.company.balance.Balance;
import com.company.balance.CustomerBalance;
import com.company.balance.GiftCardBalance;
import com.company.category.Category;
import com.company.discount.Discount;
import com.company.order.Order;
import com.company.order.OrderService;
import com.company.order.OrderServiceImpl;

import java.util.*;

import static com.company.StaticConstants.DISCOUNT_LIST;
import static com.company.StaticConstants.ORDER_LIST;

public class Main {

    public static void main(String[] args) {

        DataGenerator.createCustomer();
        DataGenerator.createCategory();
        DataGenerator.createProduct();
        DataGenerator.createBalance();
        DataGenerator.createDiscount();
    //to confirm address
      //  printAddressByCustomerId(StaticConstants.CUSTOMER_LIST.get(0));
        //Address{StreetNumber='7925', StreetName='Jones Branch Dr', ZipCode='22102', State='VA'}
        //Address{StreetNumber='825', StreetName='GeorgeTown Pky', ZipCode='22036', State='VA'}
    //   System.out.println( findCustomerBalance(StaticConstants.CUSTOMER_LIST.get(0).getId()));

        Scanner scanner = new Scanner(System.in);

        System.out.println("Select Customer:");
        Customer customer = null;
        for (int i = 0; i < StaticConstants.CUSTOMER_LIST.size(); i++) {
            System.out.println(
                    "Type " + i + " for customer:" + StaticConstants.CUSTOMER_LIST.get(i).getUserName());

        }

        int selectedIndex = -1; // Assign to an invalid value
        do {
            try {
                selectedIndex = scanner.nextInt();
                if (selectedIndex < 0 || selectedIndex >= StaticConstants.CUSTOMER_LIST.size()) {
                    System.out.println("Invalid input. Please enter a number between 0 and " + (StaticConstants.CUSTOMER_LIST.size() - 1) + ".");
                    selectedIndex = -1; // Reset again
                }
            } catch (RuntimeException e) {
                System.out.println("Invalid input. Please enter a number.");
          //      scanner.nextLine(); // Clear the input//scanner memory
            }
        } while (selectedIndex == -1);

        customer = StaticConstants.CUSTOMER_LIST.get(selectedIndex);

        Cart cart = new Cart(customer);

        while (true) {

            System.out.println("What would you like to do? Just type id for selection");

            for (int i = 0; i < prepareMenuOptions().length; i++) {
                System.out.println(i + "-" + prepareMenuOptions()[i]);
            }

            int menuSelection = scanner.nextInt();

            switch (menuSelection) {
                case 0: //list categories
                    for (Category category : StaticConstants.CATEGORY_LIST) {
                        System.out.println(
                                "Category Code:" + category.generateCategoryCode() + " category name:"
                                        + category.getName());
                    }
                    break;
                case 1: //list products  //product name, product category name
                    try {
                        for (Product product : StaticConstants.PRODUCT_LIST) {
                            System.out.println("Product Name:" + product.getName() + "Product Category Name:"
                                    + product.getCategoryName());
                        }
                    } catch (Exception e) {
                        System.out.println(
                                "Product could not printed because category not found for product name:"
                                        + e.getMessage().split(",")[1]);
                    }
                    break;
                case 2: //list discounts
                    for (Discount discount : StaticConstants.DISCOUNT_LIST) {
                        System.out.println(
                                "Discount Name: " + discount.getName() + "discount threshold amount: "
                                        + discount.getThresholdAmount());
                    }
                    break;
                case 3://see balance
                    seeBalance(customer);
                    break;
                case 4://add balance
                addBalance(customer);
                    break;
                case 5://place an order
                    Map<Product, Integer> map = new HashMap<>();
                    cart.setProductMap(map);
                    while (true) {
                        System.out.println(
                                "Which product you want to add to your cart. For exit product selection Type : exit");
                        for (Product product : StaticConstants.PRODUCT_LIST) {
                            try {
                                System.out.println(
                                        "id:" + product.getId() + "price:" + product.getPrice() +
                                                "product category" + product.getCategoryName() +
                                                "stock:" + product.getRemainingStock() +
                                                "product delivery due:" + product.getDeliveryDueDate());
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        String productId = scanner.next();

                        try {
                            Product product = findProductById(productId);
                            if (!putItemToCartIfStockAvailable(cart, product)) {
                                System.out.println("Stock is insufficient. Please try again");
                                continue;
                            }
                        } catch (Exception e) {
                            System.out.println("Product does not exist. please try again");
                            continue;
                        }

                        System.out.println(
                                "Do you want to add more product. Type Y for adding more, N for exit");
                        String decision = scanner.next();
                        if (!decision.equals("Y")) {
                            break;
                        }
                    }

                    System.out.println(
                            "seems there are discount options. Do you want to see and apply to your cart if it is applicable. For no discount type no");
                    for (Discount discount : DISCOUNT_LIST) {
                        System.out.println(
                                "discount id " + discount.getId() + " discount name: " + discount.getName());
                    }
                    String discountId = scanner.next();
                    if (!discountId.equals("no")) {
                        try {
                            Discount discount = findDiscountById(discountId);
                            if (discount.decideDiscountIsApplicableToCart(cart)) {
                                cart.setDiscountId(discount.getId());
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }

                    }

                    OrderService orderService = new OrderServiceImpl();
                    String result = orderService.placeOrder(cart);
                    if (result.equals("Order has been placed successfully")) {
                        System.out.println("Order is successful");
                        updateProductStock(cart.getProductMap());
                        cart.setProductMap(new HashMap<>());
                        cart.setDiscountId(null);
                    } else {
                        System.out.println(result);
                    }
                    break;
                case 6://See cart
                    System.out.println("Your Cart");
                    try {
                        if (!cart.getProductMap().keySet().isEmpty()) {
                            for (Product product : cart.getProductMap().keySet()) {
                                System.out.println(
                                        "product name: " + product.getName() + " count: " + cart.getProductMap()
                                                .get(product));
                            }
                        }
                    } catch (RuntimeException e) {
                        System.err.println("There is no product added cart yet!");
                    }
                    break;
                case 7://see order details
                    printOrdersByCustomerId(customer.getId());
                    break;
                case 8://see your address
                    printAddressByCustomerId(customer);
                    break;
                case 9:
                    System.exit(1);
                    break;
            }


        }

    }
private static void addBalance(Customer customer){
    Scanner scanner = new Scanner(System.in);
    CustomerBalance customerBalance = findCustomerBalance(customer.getId());
    GiftCardBalance giftCardBalance = findGiftCardBalance(customer.getId());
    System.out.println("Which Account would you like to add?");
    System.out.println("Type 1 for Customer Balance:" + customerBalance.getBalance());
    System.out.println("Type 2 for Gift Card Balance:" + giftCardBalance.getBalance());
    int balanceAccountSelection = scanner.nextInt();
    System.out.println("How much you would like to add?");
    double additionalAmount = scanner.nextInt();

    switch (balanceAccountSelection) {
        case 1:
            customerBalance.addBalance(additionalAmount);
            System.out.println("New Customer Balance:" + customerBalance.getBalance());
            break;
        case 2:
            giftCardBalance.addBalance(additionalAmount);
            System.out.println("New Gift Card Balance:" + giftCardBalance.getBalance());
            break;
    }




}

    private static Discount findDiscountById(String discountId) throws Exception {
        return StaticConstants.DISCOUNT_LIST.stream()
                .filter(discount -> discount.getId().toString().equals(discountId))
                .findFirst()
                .orElseThrow(() -> new Exception("Discount couldn't applied because couldn't found"));

    }

    private static void updateProductStock(Map<Product, Integer> map) {
                 map.keySet().stream()
                .forEach(product -> product.setRemainingStock(product.getRemainingStock()-map.get(product)));


        }


    private static void printOrdersByCustomerId(UUID customerId) {
        for (Order order : ORDER_LIST) {
            if (order.getCustomerId().toString().equals(customerId.toString())) {
                System.out.println(
                        "Order status: " + order.getOrderStatus() + " order amount " + order.getPaidAmount()
                                + " order date " + order.getOrderDate());
            }
        }
    }

    private static void printAddressByCustomerId(Customer customer) {
        customer.getAddress().stream()
           //     .map(Objects::toString)
                .forEach(System.out::println);




     /*
        for (Address address : customer.getAddress()) {
            System.out.println(" Street Name: " + address.getStreetName() +
                    " Street Number: " + address.getStreetNumber() + "ZipCode:  "
                    + address.getZipCode() + " State: " + address.getState());
        }

      */
    }

    private static boolean putItemToCartIfStockAvailable(Cart cart, Product product) {

        System.out.println("Please provide product count:");
        Scanner scanner = new Scanner(System.in);
        int count = scanner.nextInt();

        Integer cartCount = cart.getProductMap().get(product);

        if (cartCount != null && product.getRemainingStock() > cartCount + count) {
            cart.getProductMap().put(product, cartCount + count);
            return true;

        } else if (product.getRemainingStock() >= count) {
            cart.getProductMap().put(product, count);
            return true;
        }
        return false; //BREAK TILL 3:10 PM

    }

    private static Product findProductById(String productId) throws Exception {
        return StaticConstants.PRODUCT_LIST.stream()
                .filter(product -> product.getId().toString().equals(productId))
                .findFirst()
                .orElseThrow(() -> new Exception("Product not found"));
    }


    private static CustomerBalance findCustomerBalance(UUID customerId) {
        if (StaticConstants.CUSTOMER_BALANCE_LIST.stream()
                .anyMatch(balance -> balance.getCustomerId().toString().equals(customerId.toString()))) {
            return (CustomerBalance) StaticConstants.CUSTOMER_BALANCE_LIST.stream()
                   .filter(balance -> balance.getCustomerId().toString().equals(customerId.toString()))
                    .findFirst()
                    .orElseThrow();
        } else {
            CustomerBalance c = new CustomerBalance(customerId, 0d);
            StaticConstants.CUSTOMER_BALANCE_LIST.add(c);
            return c;
        }




    }

    private static GiftCardBalance findGiftCardBalance(UUID customerId) {
        if( StaticConstants.GIFT_CARD_BALANCE_LIST.stream()
                .anyMatch((balance-> balance.getCustomerId().equals(customerId)))){

            return (GiftCardBalance) StaticConstants.GIFT_CARD_BALANCE_LIST.stream()
                    .filter(balance-> balance.getCustomerId().equals(customerId))
                    .findFirst()
                    .orElseThrow();
        }

        GiftCardBalance giftCarBalance = new GiftCardBalance(customerId, 0d);
        StaticConstants.GIFT_CARD_BALANCE_LIST.add(giftCarBalance);

        return giftCarBalance;



    }


    private static String[] prepareMenuOptions() {
        return new String[]{"List Categories", "List Products", "List Discount", "See Balance",
                "Add Balance",
                "Place an order", "See Cart", "See order details", "See your address", "Close App"};
    }


    private static void seeBalance(Customer customer){
        CustomerBalance cBalance = findCustomerBalance(customer.getId());
        GiftCardBalance gBalance = findGiftCardBalance(customer.getId());
        double totalBalance = cBalance.getBalance() + gBalance.getBalance();
        System.out.println("Total Balance:" + totalBalance);
        System.out.println("Customer Balance:" + cBalance.getBalance());
        System.out.println("Gift Card Balance:" + gBalance.getBalance());
    }

}
