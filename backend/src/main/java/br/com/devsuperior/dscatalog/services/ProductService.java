package br.com.devsuperior.dscatalog.services;

import br.com.devsuperior.dscatalog.dto.CategoryDTO;
import br.com.devsuperior.dscatalog.dto.ProductDTO;
import br.com.devsuperior.dscatalog.entities.Category;
import br.com.devsuperior.dscatalog.entities.Product;
import br.com.devsuperior.dscatalog.projections.ProductProjection;
import br.com.devsuperior.dscatalog.repositories.CategoryRepository;
import br.com.devsuperior.dscatalog.repositories.ProductRepository;
import br.com.devsuperior.dscatalog.services.exceptions.DataBaseException;
import br.com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository repository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(Pageable pageable){
        Page<Product> list = repository.findAll(pageable);
        return list.map(x -> new ProductDTO(x));

    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Optional<Product> obj = repository.findById(id);
        Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity nor found"));
        return new ProductDTO(entity, entity.getCategories());
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product entity = new Product();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);

        return new ProductDTO(entity);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {
            Product entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);
            return new ProductDTO(entity);
        }
        catch (EntityNotFoundException e){
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        if (!repository.existsById(id)){
            throw new ResourceNotFoundException("Recurso não encontrado");
        }
        try {
            repository.deleteById(id);
        }
        catch (DataIntegrityViolationException e){
            throw new DataBaseException("Falha de integridade referencial");
        }
    }

    private void copyDtoToEntity(ProductDTO dto, Product entity){
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDate(dto.getDate());
        entity.setImgUrl(dto.getImgUrl());
        entity.setPrice(dto.getPrice());

        entity.getCategories().clear();
        for(CategoryDTO catDto : dto.getCategories()){
            Category category = categoryRepository.getReferenceById(catDto.getId());
            entity.getCategories().add(category);
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(String name, String categoryId, Pageable pageable) {

        List<Long> categoryIds = Arrays.asList();
        if(!"0".equals(categoryId)){
            String[] vet = categoryId.split(",");
            categoryIds = Arrays.asList(vet).stream().map(x -> Long.parseLong(x)).toList();
        }

        Page<ProductProjection> page =  repository.searchProducts(categoryIds, name, pageable);
        List<Long> produtctIds = page.map(x -> x.getId()).toList();

        List<Product> entities = repository.searchProductsWithCategories(produtctIds);

        entities = (List<Product>) Utils.replace(page.getContent(), entities);

        List<ProductDTO> dtos = entities.stream().map(p -> new ProductDTO(p, p.getCategories())).toList();

        Page<ProductDTO> pageDto = new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
        return pageDto;
    }
}
