package br.com.devsuperior.dscatalog.repositories;

import br.com.devsuperior.dscatalog.entities.Product;
import br.com.devsuperior.dscatalog.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class ProdutctRepositoryTests {

    private long existingId;
    private long notExistingId;

    @Autowired
    private ProductRepository repository;
    private long countTotalProducts;

    @BeforeEach
    void setUp() throws Exception{
        existingId = 1L;
        notExistingId = 999L;
        countTotalProducts = 25;
    }

    @Test
    public void saveShouldPersistWithAutoIncrementWhenIdIsNull(){
        Product product = Factory.createProduct();
        product.setId(null);

        product = repository.save(product);
        Assertions.assertNotNull(product.getId());
        Assertions.assertEquals(countTotalProducts + 1, product.getId());
    }
    @Test
    public void deleteShouldDeleteObjectWhenIdExists(){
        repository.deleteById(existingId);
        Optional<Product>  result = repository.findById(existingId);

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    public void findShouldfindObjectWhenIdExists(){
        Optional<Product>  result = repository.findById(existingId);

        Assertions.assertTrue(result.isPresent());
    }

    @Test
    public void findShouldfindObjectWhenIdNotExists(){
        Optional<Product>  result = repository.findById(notExistingId);

        Assertions.assertTrue(result.isEmpty());
    }
}
