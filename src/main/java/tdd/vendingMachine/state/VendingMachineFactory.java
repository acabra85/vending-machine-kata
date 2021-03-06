package tdd.vendingMachine.state;

import lombok.NonNull;
import org.apache.log4j.Logger;
import tdd.vendingMachine.VendingMachine;
import tdd.vendingMachine.domain.*;
import tdd.vendingMachine.dto.CashImport;
import tdd.vendingMachine.dto.ProductImport;
import tdd.vendingMachine.util.FileReaderHelper;
import tdd.vendingMachine.validation.VendingMachineValidator;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Agustin on 2/25/2017.
 * @since 1.0
 * Utility class to build customized types of vending machines
 */
public class VendingMachineFactory {

    private static final Logger logger = Logger.getLogger(VendingMachineFactory.class);
    private static final VendingMachineConfiguration vendingMachineConfiguration = new VendingMachineConfiguration();

    private VendingMachineFactory() {
        throw new AssertionError("VendingMachineFactory should not be instantiated");
    }

    private static Map<Integer, Shelf<Product>> buildProductShelf(@NonNull Collection<Product> products, final int amount) {
        AtomicInteger id = new AtomicInteger(0);
        Map<Integer, Shelf<Product>> productShelf = new HashMap<>();
        for (Product product: products) {
            Shelf<Product> singleShelf = ShelfFactory.buildShelf(id.getAndIncrement(), product,
                getConfig().getProductShelfCapacity(), amount);
            productShelf.put(singleShelf.id, singleShelf);
        }
        return productShelf;
    }

    static VendingMachineConfiguration getConfig() {
        return vendingMachineConfiguration;
    }

    /**
     * Builds as vending machine with single empty product shelf of given product
     * @param product the product to build a shelf from
     * @return a vending machine with a single
     */
    public static VendingMachine buildSoldOutVendingMachineNoCash(@NonNull Product product) {
        return buildSoldOutVendingMachineNoCash(Collections.singletonList(product));
    }

    /**
     * Builds a vending machine with with empty shelves of the given products and no cash on the dispenser
     * @param products the list of products to include shelves from
     * @return a vending machine
     */
    public static VendingMachine buildSoldOutVendingMachineNoCash(@NonNull List<Product> products) {
        Map<Integer, Shelf<Product>> productShelves = buildProductShelf(products, 0);
        VendingMachineConfiguration config = getConfig();
        Map<Coin, Shelf<Coin>> coinShelves = CoinDispenserFactory.buildShelfWithGivenCoinItemCount(config, 0);
        VendingMachineValidator.validateNewVendingMachineParameters(config, productShelves, coinShelves);
        return new VendingMachineImpl(productShelves, coinShelves);
    }

    /**
     * Builds a vending machine with productsShelf containing the given list of products and the productItemCount for each product
     * And a coinDispenser with coinItemCount per coin denominations
     * @param products the products available for the vending machine
     * @param productItemCount the amount of each product
     * @param coinItemCount the amount of coins per denomination in the cashDispenser
     * @return a vending machine
     */
    public static VendingMachine buildVendingMachineGivenProductsAndInitialShelfItemCounts(@NonNull Collection<Product> products, int productItemCount, int coinItemCount) {
        if(productItemCount < 0) throw new InputMismatchException("Product amount must be non-negative");
        if(coinItemCount < 0) throw new InputMismatchException("Coin amount must be non-negative");
        Map<Integer, Shelf<Product>> productShelves = buildProductShelf(products, productItemCount);
        VendingMachineConfiguration config = getConfig();
        Map<Coin, Shelf<Coin>> coinShelves = CoinDispenserFactory.buildShelfWithGivenCoinItemCount(config, coinItemCount);
        VendingMachineValidator.validateNewVendingMachineParameters(config, productShelves, coinShelves);
        return new VendingMachineImpl(productShelves, coinShelves);
    }

    /**
     * Method mean to be used by unit testing to allow flexible creation of productShelves
     * @param productShelves the productShelves
     * @param coinShelves the coinShelves
     * @return a vending machine with given shelves
     */
    public static VendingMachine customVendingMachineForTesting(Map<Integer, Shelf<Product>> productShelves, Map<Coin, Shelf<Coin>> coinShelves) {
        VendingMachineValidator.validateNewVendingMachineParameters(getConfig(), productShelves, coinShelves);
        return new VendingMachineImpl(productShelves, coinShelves);
    }

    /**
     * Given a collection of ProductImport objects builds a productShelves object for the vending machine
     * @param productImports the product imports defining the products to load.
     * @return map containing the product shelves.
     */
    static Map<Integer, Shelf<Product>> buildProductShelfFromCashImports(Collection<ProductImport> productImports) {
        int idShelfCounter = 0;
        Map<Integer, Shelf<Product>> productShelves = new HashMap<>();
        int productShelfCapacity = getConfig().getProductShelfCapacity();
        for (ProductImport productImport: productImports) {
            Shelf<Product> productShelf = ShelfFactory.buildShelf(idShelfCounter++,
                new Product(productImport.getPrice(), productImport.getType()), productShelfCapacity, productImport.getItemCount());
            productShelves.put(productShelf.id, productShelf);
            logger.info(String.format("Shelf id: %d has %d items of product [%s] ", idShelfCounter - 1,
                productImport.getItemCount(), productImport.getType()));
        }
        return productShelves;
    }

    /**
     * Builds a vending machine with the inventory provided on the files cash.csv for coinDispenser and
     * products.csv for productShelves
     * @return a vending machine.
     */
    public static VendingMachine buildVendingMachineFromResourceFiles() {
        InputStream streamCashImports = VendingMachineFactory.class.getClassLoader().getResourceAsStream("cash.csv");
        List<CashImport> cashImports = FileReaderHelper.retrieveCashImportFromFileStream(streamCashImports).orElse(Collections.emptyList());
        InputStream streamProducts = VendingMachineFactory.class.getClassLoader().getResourceAsStream("products.csv");
        List<ProductImport> productImports = FileReaderHelper.retrieveProductsImportFromFileStream(streamProducts).orElse(Collections.emptyList());
        VendingMachineConfiguration config = getConfig();
        Map<Coin, Shelf<Coin>> cashDispenser = CoinDispenserFactory.buildShelf(config, cashImports);
        Map<Integer, Shelf<Product>> productShelves = VendingMachineFactory.buildProductShelfFromCashImports(productImports);
        VendingMachineValidator.validateNewVendingMachineParameters(config, productShelves, cashDispenser);
        return new VendingMachineImpl(productShelves, cashDispenser);
    }
}
