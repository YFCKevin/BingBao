package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.ShoppingItem;
import com.yfckevin.bingBao.entity.Supplier;
import com.yfckevin.bingBao.repository.ShoppingRepository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShoppingServiceImpl implements ShoppingService{
    private final ShoppingRepository shoppingRepository;
    private final MongoTemplate mongoTemplate;

    public ShoppingServiceImpl(ShoppingRepository shoppingRepository, MongoTemplate mongoTemplate) {
        this.shoppingRepository = shoppingRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<ShoppingItem> saveAll(List<ShoppingItem> shoppingItemList) {
        return shoppingRepository.saveAll(shoppingItemList);
    }

    @Override
    public List<ShoppingItem> searchByName(String keyword) {
        List<Supplier> suppliers = mongoTemplate.find(
                Query.query(Criteria.where("name").regex(keyword, "i")),
                Supplier.class
        );

        List<ObjectId> supplierIds = suppliers.stream()
                .map(supplier -> new ObjectId(supplier.getId()))
                .collect(Collectors.toList());

        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        andCriterias.add(Criteria.where("deletionDate").exists(false));
        Criteria criteria_name = Criteria.where("name").regex(keyword, "i");
        Criteria criteria_supplier = Criteria.where("supplier.$id").in(supplierIds);
        orCriterias.add(criteria_name);
        orCriterias.add(criteria_supplier);

        Criteria criteria = new Criteria();

        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, ShoppingItem.class);
    }

    @Override
    public List<ShoppingItem> searchByNameAndMainCategory(String keyword, String mainCategory) {
        List<Supplier> suppliers = mongoTemplate.find(
                Query.query(Criteria.where("name").regex(keyword, "i")),
                Supplier.class
        );
        List<ObjectId> supplierIds = suppliers.stream()
                .map(supplier -> new ObjectId(supplier.getId()))
                .collect(Collectors.toList());

        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        andCriterias.add(Criteria.where("deletionDate").exists(false));
        andCriterias.add(Criteria.where("mainCategory").is(mainCategory));
        Criteria criteria_name = Criteria.where("name").regex(keyword, "i");
        Criteria criteria_supplier = Criteria.where("supplier.$id").in(supplierIds);
        orCriterias.add(criteria_name);
        orCriterias.add(criteria_supplier);

        Criteria criteria = new Criteria();

        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, ShoppingItem.class);
    }

    @Override
    public List<ShoppingItem> searchByMainCategory(String mainCategory) {
        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        andCriterias.add(Criteria.where("deletionDate").exists(false));
        andCriterias.add(Criteria.where("mainCategory").is(mainCategory));

        Criteria criteria = new Criteria();

        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, ShoppingItem.class);
    }

    @Override
    public List<ShoppingItem> searchByNameAndMainCategoryAndSubCategory(String keyword, String mainCategory, String subCategory) {
        List<Supplier> suppliers = mongoTemplate.find(
                Query.query(Criteria.where("name").regex(keyword, "i")),
                Supplier.class
        );
        List<ObjectId> supplierIds = suppliers.stream()
                .map(supplier -> new ObjectId(supplier.getId()))
                .collect(Collectors.toList());

        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        andCriterias.add(Criteria.where("deletionDate").exists(false));
        andCriterias.add(Criteria.where("mainCategory").is(mainCategory));
        andCriterias.add(Criteria.where("subCategory").is(subCategory));
        Criteria criteria_name = Criteria.where("name").regex(keyword, "i");
        Criteria criteria_supplier = Criteria.where("supplier.$id").in(supplierIds);
        orCriterias.add(criteria_name);
        orCriterias.add(criteria_supplier);

        Criteria criteria = new Criteria();

        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, ShoppingItem.class);
    }

    @Override
    public List<ShoppingItem> searchByMainCategoryAndSubCategory(String mainCategory, String subCategory) {
        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        andCriterias.add(Criteria.where("deletionDate").exists(false));
        andCriterias.add(Criteria.where("mainCategory").is(mainCategory));
        andCriterias.add(Criteria.where("subCategory").is(subCategory));

        Criteria criteria = new Criteria();

        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, ShoppingItem.class);
    }

    @Override
    public List<ShoppingItem> searchByNameAndPriority(String keyword, String priority) {
        List<Supplier> suppliers = mongoTemplate.find(
                Query.query(Criteria.where("name").regex(keyword, "i")),
                Supplier.class
        );

        List<ObjectId> supplierIds = suppliers.stream()
                .map(supplier -> new ObjectId(supplier.getId()))
                .collect(Collectors.toList());

        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        andCriterias.add(Criteria.where("deletionDate").exists(false));
        andCriterias.add(Criteria.where("priorityType").is(priority));
        Criteria criteria_name = Criteria.where("name").regex(keyword, "i");
        Criteria criteria_supplier = Criteria.where("supplier.$id").in(supplierIds);
        orCriterias.add(criteria_name);
        orCriterias.add(criteria_supplier);

        Criteria criteria = new Criteria();

        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, ShoppingItem.class);
    }

    @Override
    public List<ShoppingItem> searchByNameAndMainCategoryAndPriority(String keyword, String mainCategory, String priority) {
        List<Supplier> suppliers = mongoTemplate.find(
                Query.query(Criteria.where("name").regex(keyword, "i")),
                Supplier.class
        );
        List<ObjectId> supplierIds = suppliers.stream()
                .map(supplier -> new ObjectId(supplier.getId()))
                .collect(Collectors.toList());

        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        andCriterias.add(Criteria.where("deletionDate").exists(false));
        andCriterias.add(Criteria.where("mainCategory").is(mainCategory));
        andCriterias.add(Criteria.where("priorityType").is(priority));
        Criteria criteria_name = Criteria.where("name").regex(keyword, "i");
        Criteria criteria_supplier = Criteria.where("supplier.$id").in(supplierIds);
        orCriterias.add(criteria_name);
        orCriterias.add(criteria_supplier);

        Criteria criteria = new Criteria();

        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, ShoppingItem.class);
    }

    @Override
    public List<ShoppingItem> searchByMainCategoryAndPriority(String mainCategory, String priority) {
        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        andCriterias.add(Criteria.where("deletionDate").exists(false));
        andCriterias.add(Criteria.where("mainCategory").is(mainCategory));
        andCriterias.add(Criteria.where("priorityType").is(priority));

        Criteria criteria = new Criteria();

        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, ShoppingItem.class);
    }

    @Override
    public List<ShoppingItem> searchByNameAndMainCategoryAndSubCategoryAndPriority(String keyword, String mainCategory, String subCategory, String priority) {
        List<Supplier> suppliers = mongoTemplate.find(
                Query.query(Criteria.where("name").regex(keyword, "i")),
                Supplier.class
        );
        List<ObjectId> supplierIds = suppliers.stream()
                .map(supplier -> new ObjectId(supplier.getId()))
                .collect(Collectors.toList());

        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        andCriterias.add(Criteria.where("deletionDate").exists(false));
        andCriterias.add(Criteria.where("mainCategory").is(mainCategory));
        andCriterias.add(Criteria.where("subCategory").is(subCategory));
        andCriterias.add(Criteria.where("priorityType").is(priority));
        Criteria criteria_name = Criteria.where("name").regex(keyword, "i");
        Criteria criteria_supplier = Criteria.where("supplier.$id").in(supplierIds);
        orCriterias.add(criteria_name);
        orCriterias.add(criteria_supplier);

        Criteria criteria = new Criteria();

        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, ShoppingItem.class);
    }

    @Override
    public List<ShoppingItem> searchByMainCategoryAndSubCategoryAndPriority(String mainCategory, String subCategory, String priority) {
        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        andCriterias.add(Criteria.where("deletionDate").exists(false));
        andCriterias.add(Criteria.where("mainCategory").is(mainCategory));
        andCriterias.add(Criteria.where("subCategory").is(subCategory));
        andCriterias.add(Criteria.where("priorityType").is(priority));

        Criteria criteria = new Criteria();

        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, ShoppingItem.class);
    }

    @Override
    public List<ShoppingItem> searchByPriority(String priority) {
        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        andCriterias.add(Criteria.where("deletionDate").exists(false));
        andCriterias.add(Criteria.where("priorityType").is(priority));

        Criteria criteria = new Criteria();

        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, ShoppingItem.class);
    }

    @Override
    public Optional<ShoppingItem> findById(String id) {
        return shoppingRepository.findById(id);
    }

    @Override
    public ShoppingItem save(ShoppingItem shoppingItem) {
        return shoppingRepository.save(shoppingItem);
    }

    @Override
    public long countByDeletionDateIsNull() {
        return shoppingRepository.countByDeletionDateIsNull();
    }

    @Override
    public long countByDeletionDateIsNullAndPurchasedIsFalse() {
        return shoppingRepository.countByDeletionDateIsNullAndPurchasedIsFalse();
    }
}
