package tdd.vendingMachine.dto;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Agustin on 2/19/2017.
 * @since 1.0
 */
public class ProductImportTest {


    @Test
    public void should_validate_creation_productImport() {
        String importType = "testImport";
        int price = 0;
        int itemCount = 1;
        ProductImport productImport = new ProductImport(importType, price, itemCount);
        Assert.assertEquals(importType, productImport.getType());
        Assert.assertEquals(price, productImport.getPrice());
        Assert.assertEquals(itemCount, productImport.getItemCount());
    }
}
