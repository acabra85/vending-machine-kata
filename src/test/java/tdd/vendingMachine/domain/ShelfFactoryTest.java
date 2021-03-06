package tdd.vendingMachine.domain;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.InputMismatchException;

/**
 * Created by Agustin on 2/19/2017.
 */
public class ShelfFactoryTest {

    private String type;
    private int shelfId;
    private int capacity;
    private int itemCount;
    private Product product;
    private Product emptyProduct;

    @Before
    public void setup() {
        type = "type";
        product = new Product(10, type);
        emptyProduct = new Product(50, "");
        shelfId = 1;
        capacity = 10;
        itemCount = 5;
    }

    @After
    public void tearDown() {
        product = null;
        emptyProduct = null;
    }

    @Test
    public void should_build_empty_shelf_with_capacity() {
        Shelf<Product> shelfStub = ShelfFactory.<Product>buildShelf(shelfId, product, capacity);

        Assert.assertEquals(type, shelfStub.getType().getType());
        Assert.assertEquals(capacity, shelfStub.capacity);
        Assert.assertEquals(0, shelfStub.getItemCount());
        Assert.assertTrue(shelfStub.isEmpty());
    }

    @Test
    public void should_build_shelf_with_capacity_and_initial_itemCount() {
        Shelf<Product> shelfStub = ShelfFactory.buildShelf(shelfId, product, capacity, itemCount);

        Assert.assertEquals(type, shelfStub.getType().getType());
        Assert.assertEquals(capacity, shelfStub.capacity);
        Assert.assertEquals(itemCount, shelfStub.getItemCount());
        Assert.assertFalse(shelfStub.isEmpty());
    }

    @Test(expected = InputMismatchException.class)
    public void should_fail_building_shelf_id_empty(){
        ShelfFactory.buildShelf(0, emptyProduct, 10);
    }

    @Test(expected = InputMismatchException.class)
    public void should_fail_building_shelf_id_null(){
        ShelfFactory.buildShelf(0, emptyProduct, 10);
    }

    @Test(expected = InputMismatchException.class)
    public void should_fail_building_shelf_type_empty(){
        ShelfFactory.buildShelf(shelfId, emptyProduct, 10);
    }

    @Test(expected = NullPointerException.class)
    public void should_fail_building_shelf_product_null(){
        ShelfFactory.buildShelf(shelfId, null, 10);
    }

    @Test(expected = InputMismatchException.class)
    public void should_fail_itemCount_less_zero() {
        ShelfFactory.buildShelf(shelfId, product, capacity, -1);
    }

    @Test(expected = InputMismatchException.class)
    public void should_fail_capacity_less_zero() {
        ShelfFactory.buildShelf(shelfId, product, -1, itemCount);
    }
}
