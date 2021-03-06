package tdd.vendingMachine.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import tdd.vendingMachine.dto.CashImport;
import tdd.vendingMachine.dto.ProductImport;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * @author Agustin on 2/19/2017.
 * @since 1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FileReaderHelper.class})
@PowerMockIgnore(value = {"javax.management.*"})
public class FileReaderHelperTest {

    @Before
    public void setup() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void should_parse_list_productImports() {
        String file = "products_test.csv";
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(file);
        Optional<List<ProductImport>> productImportsOpt = FileReaderHelper.retrieveProductsImportFromFileStream(resourceAsStream);
        Assert.assertTrue(productImportsOpt.isPresent());

        List<ProductImport> productImports = productImportsOpt.get();

        Assert.assertEquals(2, productImports.size());
        ProductImport productImport = productImports.get(0);
        Assert.assertEquals(10, productImport.getItemCount());
        Assert.assertEquals(90, productImport.getPrice());
        Assert.assertEquals("product1", productImport.getType());
    }

    @Test
    public void should_retrieve_empty_since_invalid_productImport_file_selected() {
        String file = "products_test.csv1";
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(file);
        Optional<List<ProductImport>> productImports = FileReaderHelper.retrieveProductsImportFromFileStream(resourceAsStream);
        Assert.assertFalse(productImports.isPresent());
    }


    @Test
    public void should_parse_list_cashImports() {
        String file = "cash_test.csv";
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(file);
        Optional<List<CashImport>> cashImportsList = FileReaderHelper.retrieveCashImportFromFileStream(resourceAsStream);

        Assert.assertTrue(cashImportsList.isPresent());

        List<CashImport> cashImportOpt = cashImportsList.get();

        Assert.assertEquals(2, cashImportOpt.size());
        CashImport cashImport = cashImportOpt.get(0);
        Assert.assertEquals("0.1$", cashImport.getLabel());
        Assert.assertEquals(10, cashImport.getAmount());
    }

    @Test
    public void should_retrieve_empty_since_invalid_cashImports_file_selected() {
        String file = "cash_test.csv1";
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(file);
        Optional<List<CashImport>> productImports = FileReaderHelper.retrieveCashImportFromFileStream(resourceAsStream);
        Assert.assertFalse(productImports.isPresent());
    }
}
