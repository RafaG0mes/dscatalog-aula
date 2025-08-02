package br.com.devsuperior.dscatalog.services;

import br.com.devsuperior.dscatalog.dto.ProductDTO;
import br.com.devsuperior.dscatalog.entities.Category;
import br.com.devsuperior.dscatalog.entities.Product;
import br.com.devsuperior.dscatalog.repositories.CategoryRepository;
import br.com.devsuperior.dscatalog.repositories.ProductRepository;
import br.com.devsuperior.dscatalog.services.exceptions.DataBaseException;
import br.com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import br.com.devsuperior.dscatalog.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    private long existingId;
    private long nonExistsId;
    private long dependetId;
    private PageImpl<Product> page;
    private Product product;
    private ProductDTO productDTO;
    private Category category;

    @BeforeEach
    void setUp() throws Exception{
        existingId = 1L;
        nonExistsId = 2L;
        dependetId = 3L;
        category = Factory.createCategory();
        product = Factory.createProduct();
        productDTO = Factory.createProductDTO();
        page = new PageImpl<>(List.of(product));

        Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(repository.findById(nonExistsId)).thenReturn(Optional.empty());
        Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);

        Mockito.when(repository.getReferenceById(existingId)).thenReturn(product);

        Mockito.when(categoryRepository.getReferenceById(existingId)).thenReturn(category);

        Mockito.when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);
        Mockito.doNothing().when(repository).deleteById(existingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependetId);
        Mockito.when(repository.existsById(existingId)).thenReturn(true);
        Mockito.when(repository.existsById(nonExistsId)).thenReturn(false);
        Mockito.when(repository.existsById(dependetId)).thenReturn(true);
    }

    @Test
    public void updateShouldReturnProductDTOWhenExistsId(){
        ProductDTO result = service.update(existingId, productDTO);
        Assertions.assertNotNull(result);

        Mockito.verify(repository, Mockito.times(1)).save(product);

    }
    @Test
    public void findByIdShouldResourceNotFoundExceptionWhenIdNotExists(){
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.findById(nonExistsId));
    }
    @Test
    public void findByIdShouldReturnWhenIdExistis(){
        ProductDTO productDTO = service.findById(existingId);
        Assertions.assertNotNull(productDTO);
    }
    @Test
    public void findAllPagedShouldReturnPage(){
        Pageable pageable = PageRequest.of(0,10);
        Page<ProductDTO> result = service.findAllPaged(pageable);
        Assertions.assertNotNull(result);
        Mockito.verify(repository, Mockito.times(1)).findAll(pageable);
    }
    @Test
    public void deleteShouldDoNothingWhenIdExists(){

        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingId);
        });
    }

    @Test
    public void deleteShouldThrowDataBaseExceptionWhenDependentId(){
        Assertions.assertThrows(DataBaseException.class, () -> {
            service.delete(dependetId);
        });
    }
    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIddoesNotExists(){

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistsId);
        });
    }

}
