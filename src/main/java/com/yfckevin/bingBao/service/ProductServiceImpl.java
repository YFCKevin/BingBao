package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Product;
import com.yfckevin.bingBao.repository.ProductRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService{
    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;

    public ProductServiceImpl(ProductRepository productRepository, MongoTemplate mongoTemplate) {
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> findAllByDeletionDateIsNullOrderByCreationDateDesc() {
        return productRepository.findAllByDeletionDateIsNullOrderByCreationDateDesc();
    }

    @Override
    public void deleteById(String id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> saveAll(List<Product> productList) {
        return productRepository.saveAll(productList);
    }

    @Override
    public List<Product> findByIdIn(List<String> productIds) {
        return productRepository.findByIdIn(productIds);
    }

    @Override
    public List<Product> searchProductByName(String keyword) {
        return productRepository.searchProductByName(keyword);
    }

    @Override
    public List<Product> searchProductByNameAndMainCategory(String keyword, String mainCategory) {
        List<Criteria> andCriterias = new ArrayList<>();

        Criteria criteria = Criteria.where("deletionDate").exists(false);

        if (StringUtils.isNotBlank(keyword)) {
            Criteria criteriaKeyword = Criteria.where("name").regex(keyword, "i");
            andCriterias.add(criteriaKeyword);
        }

        if (StringUtils.isNotBlank(mainCategory)) {
            Criteria criteriaMainCategory = Criteria.where("mainCategory").is(mainCategory);
            andCriterias.add(criteriaMainCategory);
        }

        if (!andCriterias.isEmpty()) {
            criteria = criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Product.class);
    }

    @Override
    public List<Product> searchProductByMainCategory(String mainCategory) {
        Criteria criteria = Criteria.where("deletionDate").exists(false);

        if (StringUtils.isNotBlank(mainCategory)) {
            Criteria criteriaMainCategory = Criteria.where("mainCategory").is(mainCategory);
            criteria = criteria.andOperator(criteria, criteriaMainCategory);
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Product.class);
    }

    @Override
    public List<Product> searchProductByNameAndMainCategoryAndSubCategory(String keyword, String mainCategory, String subCategory) {
        List<Criteria> andCriterias = new ArrayList<>();

        Criteria criteria = Criteria.where("deletionDate").exists(false);

        if (StringUtils.isNotBlank(keyword)) {
            Criteria criteriaKeyword = Criteria.where("name").regex(keyword, "i");
            andCriterias.add(criteriaKeyword);
        }

        if (StringUtils.isNotBlank(mainCategory)) {
            Criteria criteriaMainCategory = Criteria.where("mainCategory").is(mainCategory);
            andCriterias.add(criteriaMainCategory);
        }

        if (StringUtils.isNotBlank(subCategory)) {
            Criteria criteriaSubCategory = Criteria.where("subCategory").is(subCategory);
            andCriterias.add(criteriaSubCategory);
        }

        if (!andCriterias.isEmpty()) {
            criteria = criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Product.class);
    }

    @Override
    public List<Product> searchProductByMainCategoryAndSubCategory(String mainCategory, String subCategory) {
        List<Criteria> andCriterias = new ArrayList<>();

        Criteria criteria = Criteria.where("deletionDate").exists(false);

        if (StringUtils.isNotBlank(mainCategory)) {
            Criteria criteriaMainCategory = Criteria.where("mainCategory").is(mainCategory);
            andCriterias.add(criteriaMainCategory);
        }

        if (StringUtils.isNotBlank(subCategory)) {
            Criteria criteriaSubCategory = Criteria.where("subCategory").is(subCategory);
            andCriterias.add(criteriaSubCategory);
        }

        if (!andCriterias.isEmpty()) {
            criteria = criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Product.class);
    }

    @Override
    public long countByDeletionDateIsNull() {
        return productRepository.countByDeletionDateIsNull();
    }
}
