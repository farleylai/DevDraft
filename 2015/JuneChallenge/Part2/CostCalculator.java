import java.util.*;
import java.io.*;

class CostCalculator {
    public static final boolean DEBUG = !true;
    public static void debug(String line) {
        if(DEBUG) System.err.println(line);
    }
    public static void debug(String[] lines) {
        if(DEBUG) 
            for(String line: lines)
                debug(line);
    }
    public static void debug(String fmt, Object... args) {
        if(DEBUG) System.err.printf(fmt, args);
    }

    /**
     * The Address class recognizes only the state and zip codes from the input address line for simplicity.
     */
    public static class Address {   
        private final String mState;
        private final int zipCode;
        public Address (String addressLine) {
            debug("address line: " + addressLine);
            String fields[] = addressLine.split(",");
            debug("total fields: ");
            debug(fields);
            fields = fields[fields.length - 1].trim().split(" ");
            debug("fields of interest: ");
            debug(fields);
            mState = fields[0];
            zipCode = Integer.parseInt(fields[1]);
        }

        public String state() { return mState; }

        public int zipCode() { return zipCode; }
    }

    /**
     * The shipping fees are computed according to the following business rules.
     * 1.) $10 for zip codes higher than 75000.
     * 2.) $20 for zip codes between 25000 and 75000 inclusive.
     * 3.) $30 for all the other zip codes.
     *
     * @param zipCode the zip code to look up
     * @return the shipping fees
     */
    private int calculateShipping(int zipCode) {
        if (zipCode > 75000) 
            return 10;
        else if (zipCode >= 25000) 
            return 20;
        return 30;
    }

    /**
     * The sales tax is computed according to the following business rules.
     * AZ => 5%,
     * WA => 9%,
     * CA => 6%,
     * DE => 0, and
     * 7% for all the other states.
     *
     * @param orderAmount the base retail price of the order in dollars
     * @param state the resident state
     * @return the sales tax
     */
    private int calculateTax(int orderAmount, String state) {
        final String AZ = "ARIZONA";
        final String WA = "WASHINGTON";
        final String CA = "CALIFORNIA";
        final String DE = "DELAWARE";
        state = state.toUpperCase();
        return state.equals(AZ) || state.equals("AZ")
            ? orderAmount * 5 / 100 
            : state.equals(WA) || state.equals("WA") 
                ? orderAmount * 9 / 100
                : state.equals(CA) || state.equals("CA")
                    ? orderAmount * 6 / 100
                    : state.equals(DE) || state.equals("DE")
                        ? 0
                        : orderAmount * 7 / 100;            
    }

    /**
     * The public interface to compute the cost of an order based on a retail price and the address.
     * Business logics remains hidden from clients.
     *
     * @param basePrice the base retail price in dollars
     * @param address the resident address
     * @return the total cost including the base price, tax and shpping fees
     */
    public int calculate(int basePrice, Address address) {
        int taxAmount = calculateTax(basePrice, address.state());
        int shippingAmount = calculateShipping(address.zipCode());
        return basePrice + taxAmount + shippingAmount;
    }
    
    public static void main(String[] args) {
        CostCalculator calculator = new CostCalculator();
        Scanner in = new Scanner(System.in).useDelimiter("\r?\n|\r");
        int numTestCases = in.nextInt();
        int[] totals = new int[numTestCases];
        for (int i = 0; i < numTestCases; i++) {            
            int basePrice = in.nextInt();
            Address addr = new Address(in.next());
            totals[i] = calculator.calculate(basePrice, addr);
        }
        if(DEBUG) {
            // local test mode
            for(int i = 0; i < totals.length; i++) {
                int solution = in.nextInt();
                if(solution != totals[i])
                    debug("Computed %d but expected %d\n", totals[i], solution);
                else
                    debug("%d is correct!\n", totals[i]);
            }
        } else {
            // output the totals
            for(int total: totals)
                System.out.println(total);
        }
        in.close();
    }
}
