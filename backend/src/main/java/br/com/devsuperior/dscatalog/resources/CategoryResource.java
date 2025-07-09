package br.com.devsuperior.dscatalog.resources;

import br.com.devsuperior.dscatalog.dto.CategoryDTO;
import br.com.devsuperior.dscatalog.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/categories")
public class CategoryResource {

    @Autowired
    private CategoryService service;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> findall(){
        List<CategoryDTO> list = service.findAll();
        return ResponseEntity.ok().body(list);
    }
}
