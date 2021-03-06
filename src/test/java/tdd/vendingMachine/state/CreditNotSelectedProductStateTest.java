package tdd.vendingMachine.state;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import tdd.vendingMachine.VendingMachine;
import tdd.vendingMachine.domain.Coin;
import tdd.vendingMachine.domain.Product;
import tdd.vendingMachine.domain.Shelf;
import tdd.vendingMachine.domain.VendingMachineConfiguration;
import tdd.vendingMachine.dto.ProductImport;
import tdd.vendingMachine.util.Constants;
import tdd.vendingMachine.util.TestUtils.TestUtils;
import tdd.vendingMachine.validation.VendingMachineValidator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Agustin Cabra on 2/21/2017.
 * @since 1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({VendingMachine.class, VendingMachineImpl.class, VendingMachineConfiguration.class,
    VendingMachineFactory.class, CreditNotSelectedProductState.class,
    State.class})
@PowerMockIgnore(value = {"javax.management.*"})
public class CreditNotSelectedProductStateTest implements StateTest {

    private Product COLA_199_025;
    CreditNotSelectedProductState creditNotSelectedProductState;

    /**
     * Creates a mock for the class VendingMachineConfiguration
     * @param coinShelfCapacity what to return when coinShelfCapacity is requested
     * @param productShelfCount what to return when productShelfCount is requested
     * @param productShelfCapacity what to return when productShelfCapacity is requested
     * @return new mock with the desired behaviour
     */
    private VendingMachineConfiguration getConfigMock(int coinShelfCapacity, int productShelfCount, int productShelfCapacity) {
        VendingMachineConfiguration vendingMachineConfigurationMock = Mockito.mock(VendingMachineConfiguration.class);
        Mockito.when(vendingMachineConfigurationMock.getCoinShelfCapacity()).thenReturn(coinShelfCapacity);
        Mockito.when(vendingMachineConfigurationMock.getProductShelfCount()).thenReturn(productShelfCount);
        Mockito.when(vendingMachineConfigurationMock.getProductShelfCapacity()).thenReturn(productShelfCapacity);
        return vendingMachineConfigurationMock;
    }

    /**
     * Verifies the calls to the methods on the mock for the VendingMachineConfiguration object
     * @param mockConfig the object to verify
     * @param coinShelfInvocations expected invocations for coinShelfCapacity
     * @param productShelfCountInvocations expected invocations for productShelfCount
     * @param productShelfCapacityInvocations expected invocations for productShelfCapacity
     */
    private void verifyConfigMock(VendingMachineConfiguration mockConfig, int coinShelfInvocations, int productShelfCountInvocations, int productShelfCapacityInvocations) {
        Mockito.verify(mockConfig, Mockito.times(coinShelfInvocations)).getCoinShelfCapacity();
        Mockito.verify(mockConfig, Mockito.times(productShelfCountInvocations)).getProductShelfCount();
        Mockito.verify(mockConfig, Mockito.times(productShelfCapacityInvocations)).getProductShelfCapacity();
    }

    @Override
    public CreditNotSelectedProductState transformToAndValidateInitialState(VendingMachine vendingMachine) {
        VendingMachineValidator.validateToReadyState(vendingMachine);
        Assert.assertTrue(vendingMachine.provideCurrentState() instanceof ReadyState);
        ReadyState initialState = (ReadyState) vendingMachine.provideCurrentState();

        //transform to desired state
        initialState.insertCoin(Coin.FIFTY_CENTS);

        //validate initial state
        Assert.assertEquals(Coin.FIFTY_CENTS.denomination, vendingMachine.provideCredit());
        Assert.assertTrue(vendingMachine.provideCurrentState() instanceof CreditNotSelectedProductState);
        return (CreditNotSelectedProductState) vendingMachine.provideCurrentState();
    }

    @Before @Override
    public void setup(){
        COLA_199_025 = new Product(190, "COLA_199_025");
    }

    @After @Override
    public void tearDown(){
        COLA_199_025 = null;
        creditNotSelectedProductState = null;
    }

    @Test
    public void should_insert_credit_remain_same_state() throws Exception {
        int initialCoinsOnShelf = 10;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);

        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);

        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(TestUtils.buildShelvesWithItems(COLA_199_025, 1),
            TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 5));
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        Coin tenCents = Coin.TEN_CENTS;
        int previousStackCreditSize = creditNotSelectedProductState.vendingMachine.getCreditStackSize();
        int previousCredit = creditNotSelectedProductState.vendingMachine.provideCredit();

        creditNotSelectedProductState.insertCoin(tenCents);

        Assert.assertEquals(previousStackCreditSize + 1, creditNotSelectedProductState.vendingMachine.getCreditStackSize());
        Assert.assertEquals(tenCents.denomination + previousCredit, creditNotSelectedProductState.vendingMachine.provideCredit());
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof CreditNotSelectedProductState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_skip_insert_coins_after_dispensers_capacity_reached() throws Exception {
        int initialCoinsOnShelf = 10;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);
        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(TestUtils.buildShelvesWithItems(COLA_199_025, 1),
            TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 5));
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        Coin tenCents = Coin.TEN_CENTS;
        int previousStackCreditSize = creditNotSelectedProductState.vendingMachine.getCreditStackSize();
        int previousCredit = creditNotSelectedProductState.vendingMachine.provideCredit();
        int insertsToFillCoinShelf = 5;

        //fill ten cents dispenser shelf
        for (int i = 0; i < insertsToFillCoinShelf; i++) {
            creditNotSelectedProductState.insertCoin(tenCents);
        }

        Assert.assertEquals(previousStackCreditSize + insertsToFillCoinShelf, creditNotSelectedProductState.vendingMachine.getCreditStackSize());
        Assert.assertEquals(tenCents.denomination * insertsToFillCoinShelf + previousCredit, creditNotSelectedProductState.vendingMachine.provideCredit());
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof CreditNotSelectedProductState);

        //insert coins will no take any effect
        creditNotSelectedProductState.insertCoin(tenCents);
        creditNotSelectedProductState.insertCoin(tenCents);
        creditNotSelectedProductState.insertCoin(tenCents);
        creditNotSelectedProductState.insertCoin(tenCents);
        creditNotSelectedProductState.insertCoin(tenCents);

        Assert.assertEquals(previousStackCreditSize + insertsToFillCoinShelf, creditNotSelectedProductState.vendingMachine.getCreditStackSize());
        Assert.assertEquals(tenCents.denomination * insertsToFillCoinShelf + previousCredit, creditNotSelectedProductState.vendingMachine.provideCredit());
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof CreditNotSelectedProductState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);

    }

    @Test
    public void should_select_valid_shelfNumber_and_change_state_to_InsufficientCreditState() throws Exception {
        int initialCoinsOnShelf = 10;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);
        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(TestUtils.buildShelvesWithItems(COLA_199_025, 1),
            TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 5));
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        creditNotSelectedProductState.selectShelfNumber(0);
        Assert.assertNotNull(creditNotSelectedProductState.vendingMachine.provideSelectedProduct());
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof InsufficientCreditState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_select_valid_shelfNumber_and_successfully_sell_product_and_change_state_to_ReadyState() throws Exception {
        int initialCoinsOnShelf = 10;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);
        Map<Integer, Shelf<Product>> productShelves = TestUtils.buildShelvesWithItems(COLA_199_025, 2);
        Map<Coin, Shelf<Coin>> coinShelves = TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 5);
        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(productShelves, coinShelves);
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        creditNotSelectedProductState.insertCoin(Coin.ONE);
        creditNotSelectedProductState.insertCoin(Coin.TWENTY_CENTS);
        creditNotSelectedProductState.insertCoin(Coin.FIFTY_CENTS);
        creditNotSelectedProductState.selectShelfNumber(0);


        VendingMachineValidator.validateToReadyState(creditNotSelectedProductState.vendingMachine);
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof ReadyState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_select_valid_shelfNumber_and_successfully_sell_product_and_change_state_to_SoldOutState() throws Exception {
        int initialCoinsOnShelf = 10;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);
        Map<Integer, Shelf<Product>> productShelves = TestUtils.buildShelvesWithItems(COLA_199_025, 1);
        Map<Coin, Shelf<Coin>> coinShelves = TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 5);
        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(productShelves, coinShelves);
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        creditNotSelectedProductState.insertCoin(Coin.ONE);
        creditNotSelectedProductState.insertCoin(Coin.TWENTY_CENTS);
        creditNotSelectedProductState.insertCoin(Coin.FIFTY_CENTS);
        creditNotSelectedProductState.selectShelfNumber(0);

        Assert.assertNull(creditNotSelectedProductState.vendingMachine.provideSelectedProduct());
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.getCreditStackSize() == 0);
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCredit() == 0);
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof SoldOutState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_select_invalid_shelfNumber_and_remain_state_to_CreditNoSelectedProductState() throws Exception {
        int initialCoinsOnShelf = 10;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);
        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(TestUtils.buildShelvesWithItems(COLA_199_025, 1),
            TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 5));
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        creditNotSelectedProductState.selectShelfNumber(5454);
        Assert.assertNull(creditNotSelectedProductState.vendingMachine.provideSelectedProduct());
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof CreditNotSelectedProductState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_fail_when_selecting_empty_shelf() throws Exception {
        int initialCoinsOnShelf = 10;
        int emptyShelfId = 0;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);

        Map<Integer, Shelf<Product>> productShelves = TestUtils.buildShelfStubFromProductImports(
            Arrays.asList(new ProductImport("p1", 100, 0), new ProductImport("p2", 100, 1)), 10);
        Map<Coin, Shelf<Coin>> coinShelves = TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 5);
        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(productShelves, coinShelves);
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        creditNotSelectedProductState.selectShelfNumber(emptyShelfId);
        Assert.assertNull(creditNotSelectedProductState.vendingMachine.provideSelectedProduct());
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof CreditNotSelectedProductState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_not_add_credit_return_all_inserted_credit_after_cancel_and_change_to_noCreditState() throws Exception {
        int initialCoinsOnShelf = 10;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);
        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(TestUtils.buildShelvesWithItems(COLA_199_025, 1),
            TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 5));
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        creditNotSelectedProductState.cancel();

        VendingMachineValidator.validateToReadyState(creditNotSelectedProductState.vendingMachine);
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof ReadyState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_add_credit_return_all_inserted_credit_after_cancel_and_change_to_noCreditState() throws Exception {
        int initialCoinsOnShelf = 10;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);
        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(TestUtils.buildShelvesWithItems(COLA_199_025, 1),
            TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 5));
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        List<Coin> coinsToInsert = Arrays.asList(Coin.FIFTY_CENTS, Coin.TEN_CENTS, Coin.TWENTY_CENTS, Coin.TWO, Coin.ONE, Coin.FIVE);

        int previousStackCreditSize = creditNotSelectedProductState.vendingMachine.getCreditStackSize();
        int previousCredit = creditNotSelectedProductState.vendingMachine.provideCredit();

        coinsToInsert.forEach(creditNotSelectedProductState::insertCoin);

        Assert.assertEquals(previousStackCreditSize + coinsToInsert.size(), creditNotSelectedProductState.vendingMachine.getCreditStackSize());
        int total = coinsToInsert.stream()
            .mapToInt(coin -> coin.denomination)
            .reduce(Constants.SUM_INT_IDENTITY, Constants.SUM_INT_BINARY_OPERATOR);
        Assert.assertEquals(total + previousCredit, creditNotSelectedProductState.vendingMachine.provideCredit());

        creditNotSelectedProductState.cancel();

        VendingMachineValidator.validateToReadyState(creditNotSelectedProductState.vendingMachine);
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof ReadyState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_select_valid_shelfNumber_and_not_sell_product_unable_to_provide_change() throws Exception {
        int initialCoinsOnShelf = 10;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);
        Map<Integer, Shelf<Product>> productShelves = TestUtils.buildShelvesWithItems(COLA_199_025, 1);
        Map<Coin, Shelf<Coin>> coinShelves = TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 0);
        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(productShelves, coinShelves);
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        creditNotSelectedProductState.insertCoin(Coin.FIVE);
        creditNotSelectedProductState.selectShelfNumber(0);

        VendingMachineValidator.validateToReadyState(creditNotSelectedProductState.vendingMachine);
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof ReadyState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_select_valid_shelfNumber_and_sell_product_exact_amount_given_stateToReady() throws Exception {
        int initialCoinsOnShelf = 10;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);
        Map<Integer, Shelf<Product>> productShelves = TestUtils.buildShelvesWithItems(COLA_199_025, 2);
        Map<Coin, Shelf<Coin>> coinShelves = TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 5);
        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(productShelves,
            coinShelves);
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        creditNotSelectedProductState.insertCoin(Coin.ONE);
        creditNotSelectedProductState.insertCoin(Coin.TWENTY_CENTS);
        creditNotSelectedProductState.insertCoin(Coin.TWENTY_CENTS);
        creditNotSelectedProductState.selectShelfNumber(0);

        VendingMachineValidator.validateToReadyState(creditNotSelectedProductState.vendingMachine);
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof ReadyState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_select_valid_shelfNumber_and_sell_product_exact_amount_given_state_to_soldOut() throws Exception {
        int initialCoinsOnShelf = 10;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);
        Map<Integer, Shelf<Product>> productShelves = TestUtils.buildShelvesWithItems(COLA_199_025, 1);
        Map<Coin, Shelf<Coin>> coinShelves = TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(initialCoinsOnShelf, 5);
        VendingMachine vendingMachine = VendingMachineFactory.customVendingMachineForTesting(productShelves,
            coinShelves);
        creditNotSelectedProductState = transformToAndValidateInitialState(vendingMachine);

        creditNotSelectedProductState.insertCoin(Coin.ONE);
        creditNotSelectedProductState.insertCoin(Coin.TWENTY_CENTS);
        creditNotSelectedProductState.insertCoin(Coin.TWENTY_CENTS);
        creditNotSelectedProductState.selectShelfNumber(0);

        Assert.assertNull(creditNotSelectedProductState.vendingMachine.provideSelectedProduct());
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.getCreditStackSize() == 0);
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCredit() == 0);
        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof SoldOutState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_send_machine_to_technical_error_state_fail_adding_coins() throws Exception {
        Coin fiftyCents = Coin.FIFTY_CENTS;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);

        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);

        VendingMachine spied = PowerMockito.spy(VendingMachineFactory.customVendingMachineForTesting(TestUtils.buildShelvesWithItems(COLA_199_025, 1),
            TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(10, 5)));
        PowerMockito.doThrow(new RuntimeException("fail to error")).when(spied, "addCoinToCredit", fiftyCents);

        creditNotSelectedProductState = new CreditNotSelectedProductState((VendingMachineImpl) spied);

        creditNotSelectedProductState.insertCoin(fiftyCents);

        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof TechnicalErrorState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        Mockito.verify(spied, Mockito.times(1)).addCoinToCredit(fiftyCents);
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_send_machine_to_technical_error_state_fail_selecting_shelf() throws Exception {
        int shelfNumber = 0;
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);

        VendingMachine spied = PowerMockito.spy(VendingMachineFactory.customVendingMachineForTesting(TestUtils.buildShelvesWithItems(COLA_199_025, 1),
            TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(10, 5)));
        PowerMockito.doThrow(new RuntimeException("fail to error")).when(spied, "selectProductGivenShelfNumber", shelfNumber);

        creditNotSelectedProductState = new CreditNotSelectedProductState((VendingMachineImpl)spied);

        creditNotSelectedProductState.selectShelfNumber(shelfNumber);

        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof TechnicalErrorState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        Mockito.verify(spied, Mockito.times(1)).selectProductGivenShelfNumber(shelfNumber);
        verifyConfigMock(configMock, 1, 1, 1);
    }

    @Test
    public void should_send_machine_to_technical_error_state_fail_on_cancel() throws Exception {
        VendingMachineConfiguration configMock = getConfigMock(10, 10, 10);


        PowerMockito.spy(VendingMachineFactory.class);
        PowerMockito.when(VendingMachineFactory.getConfig()).thenReturn(configMock);

        VendingMachine spied = PowerMockito.spy(VendingMachineFactory.customVendingMachineForTesting(TestUtils.buildShelvesWithItems(COLA_199_025, 1),
            TestUtils.buildStubCoinDispenserWithGivenItemsPerShelf(10, 5)));
        PowerMockito.doThrow(new RuntimeException("fail to error")).when(spied, "returnAllCreditToBucket");

        creditNotSelectedProductState = new CreditNotSelectedProductState((VendingMachineImpl)spied);

        creditNotSelectedProductState.cancel();

        Assert.assertTrue(creditNotSelectedProductState.vendingMachine.provideCurrentState() instanceof TechnicalErrorState);

        PowerMockito.verifyStatic(Mockito.times(1));
        VendingMachineFactory.getConfig();
        Mockito.verify(spied, Mockito.times(1)).returnAllCreditToBucket();
        verifyConfigMock(configMock, 1, 1, 1);
    }
}
