package br.com.devsuperior.dscatalog.services;

import br.com.devsuperior.dscatalog.dto.ProductDTO;
import br.com.devsuperior.dscatalog.entities.Category;
import br.com.devsuperior.dscatalog.entities.Product;
import br.com.devsuperior.dscatalog.repositories.CategoryRepository;
import br.com.devsuperior.dscatalog.repositories.ProductRepository;
import br.com.devsuperior.dscatalog.services.exceptions.DataBaseException;
import br.com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import br.com.devsuperior.dscatalog.tests.Factory;
import jakarta.persistence.EntityNotFoundException;
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
        product.getCategories().add(category);
        productDTO = Factory.createProductDTO();
        page = new PageImpl<>(List.of(product));

        // Simula o comportamento do repositório quando um ID existe.
        // Retorna um Optional com o produto.
        Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));

        // Simula o comportamento do repositório quando um ID não existe.
        // Retorna um Optional vazio.
        Mockito.when(repository.findById(nonExistsId)).thenReturn(Optional.empty());

        // Simula o comportamento do método 'save' do repositório.
        // Para qualquer objeto Product que for passado, ele deve retornar o 'product' mockado.
        Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);

        // Simula o comportamento do método 'getReferenceById' do repositório
        // quando um ID existente é passado.
        Mockito.when(repository.getReferenceById(existingId)).thenReturn(product);

        // Simula o comportamento do método 'getReferenceById' do CategoryRepository.
        Mockito.when(categoryRepository.getReferenceById(existingId)).thenReturn(category);

        // agora lança a exceção correta (EntityNotFoundException) que o serviço espera.
        Mockito.doThrow(EntityNotFoundException.class).when(repository).getReferenceById(nonExistsId);

        // Simula o comportamento do método 'findAll' do repositório para paginação.
        // Para qualquer objeto Pageable, retorna o objeto 'page' mockado.
        Mockito.when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

        // Simula o comportamento do método 'deleteById' do repositório
        // quando um ID existente é passado, não fazendo nada.
        Mockito.doNothing().when(repository).deleteById(existingId);

        // Simula o comportamento do método 'deleteById' quando um ID dependente é passado,
        // lançando uma exceção de violação de integridade de dados.
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependetId);

        // Simula o comportamento do método 'existsById' para IDs que existem, não existem e são dependentes.
        Mockito.when(repository.existsById(existingId)).thenReturn(true);
        Mockito.when(repository.existsById(nonExistsId)).thenReturn(false);
        Mockito.when(repository.existsById(dependetId)).thenReturn(true);
    }

    @Test
    public void updateShouldReturnProductDTOWhenExistsId(){
        ProductDTO result = service.update(existingId, productDTO);
        Assertions.assertNotNull(result);

        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(Product.class));

    }

    @Test
    public void updateShouldResourceNotFoundExceptionWhenIdNotExists(){
        Assertions.assertThrows(ResourceNotFoundException.class, ()-> {
            service.update(nonExistsId, productDTO);
        });
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
