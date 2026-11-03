import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// takes orders, keeps track of supplies and earnings, and tells the staff when supplies run low
class CoffeeMachine {
    private Scanner scanner;
    private boolean isOn = true;
    private Map<String, PaymentMethod> paymentMethods = new HashMap<>();

    // water and milk are in ml, coffee is in grams
    private int water = 500;
    private int milk = 400;
    private int coffee = 200;
    // syrup is in pumps, whipped cream is in servings
    private int syrup = 20;
    private int whippedCream = 15;
    private int profit = 0;

    CoffeeMachine(Scanner scanner) {
        this.scanner = scanner;
        paymentMethods.put("coins", new CoinPayment(scanner));
        paymentMethods.put("gcash", new GCashPayment(scanner));
    }

    public void run() {
        System.out.println("Welcome to JavaBean: The Object-Oriented Coffee Machine!");
        System.out.println("(Type 'report' to see the supplies, or 'off' to turn the machine off.)");
        while (isOn) {
            System.out.print("\nWhat would you like? (espresso/americano/latte/cappuccino) >> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String choice = scanner.nextLine().trim().toLowerCase();
            handleCommand(choice);
        }
    }

    private void handleCommand(String choice) {
        if (choice.equals("off")) {
            isOn = false;
            System.out.println("Turning off. Goodbye!");
        } else if (choice.equals("report")) {
            printReport();
        } else if (choice.equals("espresso") || choice.equals("americano") || choice.equals("latte") || choice.equals("cappuccino")) {
            makeDrink(choice);
        } else {
            System.out.println("Sorry, we don't have '" + choice + "'.");
        }
    }

    // returns (price in Php, water in ml, milk in ml, coffee in g), or null for an unknown drink.
    private int[] getRecipe(String drink) {
        if (drink.equals("espresso")) {
            return new int[] {90, 60, 0, 18};
        } else if (drink.equals("americano")) {
            return new int[] {100, 200, 0, 18};
        } else if (drink.equals("latte")) {
            return new int[] {130, 60, 150, 18};
        } else if (drink.equals("cappuccino")) {
            return new int[] {150, 60, 100, 18};
        }
        return null;
    }

    private void makeDrink(String drink) {
        int[] recipe = getRecipe(drink);
        int cost = recipe[0];
        int waterNeeded = recipe[1];
        int milkNeeded = recipe[2];
        int coffeeNeeded = recipe[3];

        System.out.print("What size? (small/regular/large) >> ");
        String size = scanner.nextLine().trim().toLowerCase();

        // the recipe above is for a regular cup
        if (size.equals("small")) {
            cost = cost - 10;
            waterNeeded = waterNeeded * 3 / 4;
            milkNeeded = milkNeeded * 3 / 4;
        } else if (size.equals("large")) {
            cost = cost + 20;
            waterNeeded = waterNeeded * 3 / 2;
            milkNeeded = milkNeeded * 3 / 2;
            coffeeNeeded = coffeeNeeded * 3 / 2;
        } else if (!size.equals("regular")) {
            System.out.println("Sorry, we don't have a '" + size + "' cup.");
            return;
        }

        int syrupNeeded = 0;
        int creamNeeded = 0;
        String order = size + " " + drink;

        while (true) {
            System.out.print("Add an extra? (extra shot/syrup/whipped cream/done) >> ");
            String extra = scanner.nextLine().trim().toLowerCase();
            if (extra.equals("done")) {
                break;
            } else if (extra.equals("extra shot")) {
                cost = cost + 20;
                waterNeeded = waterNeeded + 60;
                coffeeNeeded = coffeeNeeded + 18;
                order = order + " + extra shot";
            } else if (extra.equals("syrup")) {
                cost = cost + 15;
                syrupNeeded = syrupNeeded + 1;
                order = order + " + syrup";
            } else if (extra.equals("whipped cream")) {
                cost = cost + 15;
                creamNeeded = creamNeeded + 1;
                order = order + " + whipped cream";
            } else {
                System.out.println("Sorry, we don't have '" + extra + "'.");
            }
        }

        System.out.println("A " + order + " costs Php " + cost + ".");
        if (!hasEnoughResources(waterNeeded, milkNeeded, coffeeNeeded, syrupNeeded, creamNeeded)) {
            return;
        }

        System.out.print("Pay with coins or GCash? (coins/gcash) >> ");
        String method = scanner.nextLine().trim().toLowerCase();
        PaymentMethod payment = paymentMethods.get(method);
        if (payment == null) {
            System.out.println("Sorry, we don't accept '" + method + "'.");
            return;
        }
        int paid = payment.pay(cost);

        if (paid < cost) {
            System.out.println("Sorry, that's not enough money. Money refunded.");
            return;
        }
        if (paid > cost) {
            System.out.println("Here is Php " + (paid - cost) + " in change.");
        }
        profit += cost;

        water -= waterNeeded;
        milk -= milkNeeded;
        coffee -= coffeeNeeded;
        syrup -= syrupNeeded;
        whippedCream -= creamNeeded;
        System.out.println("Here is your " + order + ". Enjoy!");
        checkLowSupplies();
    }

    private boolean hasEnoughResources(int waterNeeded, int milkNeeded, int coffeeNeeded, int syrupNeeded, int creamNeeded) {
        boolean enough = true;
        if (water < waterNeeded) {
            System.out.println("Sorry, there is not enough water.");
            SmsSender sms = new SmsSender();
            sms.send("0917-555-0100", "Coffee machine is out of water!");
            enough = false;
        }
        if (milk < milkNeeded) {
            System.out.println("Sorry, there is not enough milk.");
            SmsSender sms = new SmsSender();
            sms.send("0917-555-0100", "Coffee machine is out of milk!");
            enough = false;
        }
        if (coffee < coffeeNeeded) {
            System.out.println("Sorry, there is not enough coffee.");
            SmsSender sms = new SmsSender();
            sms.send("0917-555-0100", "Coffee machine is out of coffee!");
            enough = false;
        }
        if (syrup < syrupNeeded) {
            System.out.println("Sorry, there is not enough syrup.");
            SmsSender sms = new SmsSender();
            sms.send("0917-555-0100", "Coffee machine is out of syrup!");
            enough = false;
        }
        if (whippedCream < creamNeeded) {
            System.out.println("Sorry, there is not enough whipped cream.");
            SmsSender sms = new SmsSender();
            sms.send("0917-555-0100", "Coffee machine is out of whipped cream!");
            enough = false;
        }
        return enough;
    }

    private void checkLowSupplies() {
        if (water < 100 || milk < 50 || coffee < 30 || syrup < 3 || whippedCream < 3) {
            System.out.println("Warning: supplies are running low.");
            SmsSender sms = new SmsSender();
            sms.send("0917-555-0100", "Low supplies: water " + water + "ml, milk " + milk + "ml, coffee " + coffee + "g, syrup "
                    + syrup + " pumps, whipped cream " + whippedCream + " servings");
        }
    }

    private void printReport() {
        System.out.println("Water: " + water + "ml");
        System.out.println("Milk: " + milk + "ml");
        System.out.println("Coffee: " + coffee + "g");
        System.out.println("Syrup: " + syrup + " pumps");
        System.out.println("Whipped cream: " + whippedCream + " servings");
        System.out.println("Money: Php " + profit);
    }
}
