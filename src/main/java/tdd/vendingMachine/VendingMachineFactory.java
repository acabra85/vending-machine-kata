package tdd.vendingMachine;

import lombok.NonNull;
import tdd.vendingMachine.domain.*;
import tdd.vendingMachine.domain.exception.InvalidShelfSizeException;
import tdd.vendingMachine.view.VendingMachineMessages;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Agustin on 2/25/2017.
 * @since 1.0
 * Utility class to build customized types of vending machines
 */
public class VendingMachineFactory {

    private final VendingMachineConfiguration vendingMachineConfiguration;
    private final CoinDispenserBuilder coinDispenserBuilder;

    public VendingMachineFactory() {
        this.vendingMachineConfiguration = new VendingMachineConfiguration();
        this.coinDispenserBuilder = new CoinDispenserBuilder(vendingMachineConfiguration);
    }

    private Map<Coin, Shelf<Coin>> buildEmptyCoinShelf() {
        return coinDispenserBuilder.buildShelf();
    }

    private Map<Integer, Shelf<Product>> buildProductShelf(@NonNull Collection<Product> products, final int amount) {
        AtomicInteger id = new AtomicInteger(0);
        Map<Integer, Shelf<Product>> productShelf = new HashMap<>();
        for (Product product: products) {
            Shelf<Product> singleShelf = ShelfFactory.buildShelf(id.getAndIncrement(), product,
                vendingMachineConfiguration.getProductShelfCapacity(), amount);
            productShelf.put(singleShelf.id, singleShelf);
        }
        return productShelf;
    }

    /**
     * Builds as vending machine with single empty product shelf of given product
     * @param product the product to build a shelf from
     * @return a vending machine with a single
     */
    public VendingMachine buildSoldOutVendingMachineNoCash(@NonNull Product product) {
        return buildSoldOutVendingMachineNoCash(Collections.singletonList(product));
    }

    /**
     * Builds a vending machine with with empty shelves of the given products and no cash on the dispenser
     * @param products the list of products to include shelves from
     * @return a vending machine
     */
    public VendingMachine buildSoldOutVendingMachineNoCash(@NonNull List<Product> products) {
        return new VendingMachine(buildProductShelf(products, 0), buildEmptyCoinShelf());
    }

    /**
     * Builds a vending machine with productsShelf containing the given list of products and the productItemCount for each product
     * And a coinDispenser with coinItemCount per coin denominations
     * @param products the products available for the vending machine
     * @param productItemCount the amount of each product
     * @param coinItemCount the amount of coins per denomination in the cashDispenser
     * @return a vending machine
     */
    public VendingMachine buildVendingMachineGivenProductsAndInitialShelfItemCounts(@NonNull Collection<Product> products, int productItemCount, int coinItemCount) {
        if(productItemCount < 0) throw new InputMismatchException("Product amount must be non-negative");
        if(coinItemCount < 0) throw new InputMismatchException("Coin amount must be non-negative");
        Map<Integer, Shelf<Product>> productShelves = buildProductShelf(products, productItemCount);
        Map<Coin, Shelf<Coin>> coinShelves = coinDispenserBuilder.buildShelfWithGivenCoinItemCount(coinItemCount);
        return new VendingMachine(productShelves, coinShelves);
    }


    /**
     * Builds a vending machine with productsShelf containing the given product and the productItemCount for the product
     * And a coinDispenser with coinItemCount per coin denominations
     * @param product the product available for the vending machine
     * @param productItemCount the amount of each product
     * @param coinItemCount the amount of coins per denomination in the cashDispenser
     * @return a vending machine
     */
    public VendingMachine buildVendingMachineGivenProductsAndInitialShelfItemCounts(Product product, int productItemCount, int coinItemCount) {
        return buildVendingMachineGivenProductsAndInitialShelfItemCounts(Collections.singletonList(product), productItemCount, coinItemCount);
    }

    /**
     * Method mean to be used by unit testing to allow flexible creation of productShelves
     * @param productShelves the productShelves
     * @param coinShelves the coinShelves
     * @return a vending machine with given shelves
     */
    public VendingMachine customVendingMachineForTesting(@NonNull Map<Integer, Shelf<Product>> productShelves, @NonNull Map<Coin, Shelf<Coin>> coinShelves) {
        if(productShelves.size() > vendingMachineConfiguration.getProductShelfCount()) {
            throw new InvalidShelfSizeException(VendingMachineMessages.PRODUCT_SHELF_SIZE_EXCEEDS_MAX.label,
                vendingMachineConfiguration.getProductShelfCapacity(), productShelves.size());
        }
        int maxCoinShelvesSize = Coin.values().length;
        if(coinShelves.size() != maxCoinShelvesSize) {
            throw new InvalidShelfSizeException(VendingMachineMessages.COIN_SHELF_SIZE_EXCEEDS_MAX.label,
                maxCoinShelvesSize, coinShelves.size());
        }
        return new VendingMachine(productShelves, coinShelves);
    }

    public VendingMachineConfiguration getVendingMachineConfiguration() {
        return vendingMachineConfiguration;
    }
}