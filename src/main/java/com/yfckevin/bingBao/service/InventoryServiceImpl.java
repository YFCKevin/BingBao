package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.controller.LoginController;
import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.entity.Product;
import com.yfckevin.bingBao.repository.InventoryRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {
    Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private final InventoryRepository inventoryRepository;
    private final MongoTemplate mongoTemplate;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, MongoTemplate mongoTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Inventory> saveAll(List<Inventory> inventoryList) {
        return inventoryRepository.saveAll(inventoryList);
    }

    @Override
    public List<Inventory> findByIdIn(List<String> receiveItemIds) {
        return inventoryRepository.findByIdIn(receiveItemIds);
    }

    @Override
    public List<Inventory> findByStoreDateIsTodayAndNoUsedAndNoDeleteAndInValidPeriod() {
        LocalDate today = LocalDate.now();
        String startOfDay = today.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endOfDay = today.plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("usedDate").exists(false),
                Criteria.where("deletionDate").exists(false),
//                Criteria.where("expiryDate").gt(onlyDateEndOfDay),
                Criteria.where("storeDate").gt(startOfDay).lt(endOfDay)
                );
        Query query = new Query(criteria);
        final List<Inventory> inventoryList = mongoTemplate.find(query, Inventory.class);
        return inventoryList.stream().collect(Collectors.toMap(
                        Inventory::getReceiveItemId,
                        inventory -> inventory,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream().toList();
    }

    @Override
    public List<Inventory> findInventoryExpiringSoonAndNoUsedAndNoDeleteAndInValidPeriod() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String onlyDateEndOfDay = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("usedDate").exists(false),
                Criteria.where("deletionDate").exists(false),
                Criteria.where("expiryDate").gte(onlyDateEndOfDay)
        );
        Query query = new Query(criteria);
        List<Inventory> inventoryList = mongoTemplate.find(query, Inventory.class);

        final List<Inventory> tempInventoryList = inventoryList.stream().collect(Collectors.toMap(
                        Inventory::getReceiveItemId,
                        inventory -> inventory,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream().toList();

        List<Inventory> result = new ArrayList<>();
        for (Inventory inventory : tempInventoryList) {
            try {
                int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                    result.add(inventory);
                }
            } catch (NumberFormatException | DateTimeParseException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return result;
    }

    @Override
    public List<Inventory> findInventoryWithinValidityPeriodAndNoUsedAndNoDelete() {
        LocalDate today = LocalDate.now();
        String onlyDateEndOfDay = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("usedDate").exists(false),
                Criteria.where("deletionDate").exists(false),
                Criteria.where("expiryDate").gte(onlyDateEndOfDay)
        );
        Query query = new Query(criteria);
        final List<Inventory> inventoryList = mongoTemplate.find(query, Inventory.class);

        return inventoryList.stream().collect(Collectors.toMap(
                        Inventory::getReceiveItemId,
                        inventory -> inventory,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream().toList();
    }

    @Override
    public List<Inventory> searchByName(String keyword, String type) {
        LocalDate today = LocalDate.now();
        String startOfDay = today.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endOfDay = today.plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String onlyDateEndOfDay = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        AddFieldsOperation addFieldsOperation = AddFieldsOperation.addField("productId").withValue(ConvertOperators.ToObjectId.toObjectId("$productId")).build();

        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("product")
                .localField("productId")
                .foreignField("_id")
                .as("product");

        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        switch (type) {
            case "today" -> {
                final Criteria criteria_deletionDate = Criteria.where("deletionDate").exists(false);
                final Criteria criteria_storeDate = Criteria.where("storeDate").gt(startOfDay).lt(endOfDay);
                final Criteria criteria_usedDate = Criteria.where("usedDate").exists(false);
                andCriterias.add(criteria_storeDate);
                andCriterias.add(criteria_deletionDate);
                andCriterias.add(criteria_usedDate);
            }
            case "soon", "valid" -> {
                final Criteria criteria_usedDate = Criteria.where("usedDate").exists(false);
                final Criteria criteria_deletionDate = Criteria.where("deletionDate").exists(false);
                final Criteria criteria_expiryDate = Criteria.where("expiryDate").gte(onlyDateEndOfDay);
                andCriterias.add(criteria_usedDate);
                andCriterias.add(criteria_deletionDate);
                andCriterias.add(criteria_expiryDate);
            }
        }

        Criteria criteria_name = Criteria.where("product.name").regex(keyword, "i");
        orCriterias.add(criteria_name);

        Criteria criteria = new Criteria();
        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        MatchOperation matchOperation = Aggregation.match(criteria);
        // 將第一到第四步的Operation條件組到Aggregation
        Aggregation aggregation = Aggregation.newAggregation(
                addFieldsOperation,
                lookupOperation,
                matchOperation
        );

        // 用MongoTemplate執行之前的查詢條件，取得List<CountData>
        final AggregationResults<Inventory> results = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class);

        return results.getMappedResults().stream().collect(Collectors.toMap(
                        Inventory::getReceiveItemId,
                        inventory -> inventory,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream().toList();
    }

    @Override
    public List<Inventory> searchByNameAndMainCategory(String keyword, String mainCategory, String type) {
        LocalDate today = LocalDate.now();
        String startOfDay = today.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endOfDay = today.plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String onlyDateEndOfDay = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        AddFieldsOperation addFieldsOperation = AddFieldsOperation.addField("productId").withValue(ConvertOperators.ToObjectId.toObjectId("$productId")).build();

        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("product")
                .localField("productId")
                .foreignField("_id")
                .as("product");

        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        switch (type) {
            case "today" -> {
                final Criteria criteria_deletionDate = Criteria.where("deletionDate").exists(false);
                final Criteria criteria_storeDate = Criteria.where("storeDate").gt(startOfDay).lt(endOfDay);
                final Criteria criteria_usedDate = Criteria.where("usedDate").exists(false);
                andCriterias.add(criteria_storeDate);
                andCriterias.add(criteria_deletionDate);
                andCriterias.add(criteria_usedDate);
            }
            case "soon", "valid" -> {
                final Criteria criteria_usedDate = Criteria.where("usedDate").exists(false);
                final Criteria criteria_deletionDate = Criteria.where("deletionDate").exists(false);
                final Criteria criteria_expiryDate = Criteria.where("expiryDate").gte(onlyDateEndOfDay);
                andCriterias.add(criteria_usedDate);
                andCriterias.add(criteria_deletionDate);
                andCriterias.add(criteria_expiryDate);
            }
        }

        Criteria criteria_mainCategory = Criteria.where("product.mainCategory").is(mainCategory);
        Criteria criteria_name = Criteria.where("product.name").regex(keyword, "i");
        andCriterias.add(criteria_mainCategory);
        orCriterias.add(criteria_name);

        Criteria criteria = new Criteria();
        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        MatchOperation matchOperation = Aggregation.match(criteria);
        // 將第一到第四步的Operation條件組到Aggregation
        Aggregation aggregation = Aggregation.newAggregation(
                addFieldsOperation,
                lookupOperation,
                matchOperation
        );

        // 用MongoTemplate執行之前的查詢條件，取得List<CountData>
        final AggregationResults<Inventory> results = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class);

        return results.getMappedResults().stream().collect(Collectors.toMap(
                        Inventory::getReceiveItemId,
                        inventory -> inventory,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream().toList();
    }

    @Override
    public List<Inventory> searchByMainCategory(String mainCategory, String type) {
        LocalDate today = LocalDate.now();
        String startOfDay = today.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endOfDay = today.plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String onlyDateEndOfDay = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        AddFieldsOperation addFieldsOperation = AddFieldsOperation.addField("productId").withValue(ConvertOperators.ToObjectId.toObjectId("$productId")).build();

        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("product")
                .localField("productId")
                .foreignField("_id")
                .as("product");

        List<Criteria> andCriterias = new ArrayList<>();

        switch (type) {
            case "today" -> {
                final Criteria criteria_deletionDate = Criteria.where("deletionDate").exists(false);
                final Criteria criteria_storeDate = Criteria.where("storeDate").gt(startOfDay).lt(endOfDay);
                final Criteria criteria_usedDate = Criteria.where("usedDate").exists(false);
                andCriterias.add(criteria_storeDate);
                andCriterias.add(criteria_deletionDate);
                andCriterias.add(criteria_usedDate);
            }
            case "soon", "valid" -> {
                final Criteria criteria_usedDate = Criteria.where("usedDate").exists(false);
                final Criteria criteria_deletionDate = Criteria.where("deletionDate").exists(false);
                final Criteria criteria_expiryDate = Criteria.where("expiryDate").gte(onlyDateEndOfDay);
                andCriterias.add(criteria_usedDate);
                andCriterias.add(criteria_deletionDate);
                andCriterias.add(criteria_expiryDate);
            }
        }

        Criteria criteria_mainCategory = Criteria.where("product.mainCategory").is(mainCategory);
        andCriterias.add(criteria_mainCategory);

        Criteria criteria = new Criteria();
        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }

        MatchOperation matchOperation = Aggregation.match(criteria);
        // 將第一到第四步的Operation條件組到Aggregation
        Aggregation aggregation = Aggregation.newAggregation(
                addFieldsOperation,
                lookupOperation,
                matchOperation
        );

        // 用MongoTemplate執行之前的查詢條件，取得List<CountData>
        final AggregationResults<Inventory> results = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class);

        return results.getMappedResults().stream().collect(Collectors.toMap(
                        Inventory::getReceiveItemId,
                        inventory -> inventory,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream().toList();
    }

    @Override
    public List<Inventory> searchByNameAndMainCategoryAndSubCategory(String keyword, String mainCategory, String subCategory, String type) {
        LocalDate today = LocalDate.now();
        String startOfDay = today.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endOfDay = today.plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String onlyDateEndOfDay = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        AddFieldsOperation addFieldsOperation = AddFieldsOperation.addField("productId").withValue(ConvertOperators.ToObjectId.toObjectId("$productId")).build();

        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("product")
                .localField("productId")
                .foreignField("_id")
                .as("product");

        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        switch (type) {
            case "today" -> {
                final Criteria criteria_deletionDate = Criteria.where("deletionDate").exists(false);
                final Criteria criteria_storeDate = Criteria.where("storeDate").gt(startOfDay).lt(endOfDay);
                final Criteria criteria_usedDate = Criteria.where("usedDate").exists(false);
                andCriterias.add(criteria_storeDate);
                andCriterias.add(criteria_deletionDate);
                andCriterias.add(criteria_usedDate);
            }
            case "soon", "valid" -> {
                final Criteria criteria_usedDate = Criteria.where("usedDate").exists(false);
                final Criteria criteria_deletionDate = Criteria.where("deletionDate").exists(false);
                final Criteria criteria_expiryDate = Criteria.where("expiryDate").gte(onlyDateEndOfDay);
                andCriterias.add(criteria_usedDate);
                andCriterias.add(criteria_deletionDate);
                andCriterias.add(criteria_expiryDate);
            }
        }

        Criteria criteria_mainCategory = Criteria.where("product.mainCategory").is(mainCategory);
        Criteria criteria_subCategory = Criteria.where("product.subCategory").is(subCategory);
        Criteria criteria_name = Criteria.where("product.name").regex(keyword, "i");
        andCriterias.add(criteria_mainCategory);
        andCriterias.add(criteria_subCategory);
        orCriterias.add(criteria_name);

        Criteria criteria = new Criteria();
        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }
        if (!orCriterias.isEmpty()) {
            criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        MatchOperation matchOperation = Aggregation.match(criteria);
        // 將第一到第四步的Operation條件組到Aggregation
        Aggregation aggregation = Aggregation.newAggregation(
                addFieldsOperation,
                lookupOperation,
                matchOperation
        );

        // 用MongoTemplate執行之前的查詢條件，取得List<CountData>
        final AggregationResults<Inventory> results = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class);

        return results.getMappedResults().stream().collect(Collectors.toMap(
                        Inventory::getReceiveItemId,
                        inventory -> inventory,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream().toList();
    }

    @Override
    public List<Inventory> searchByMainCategoryAndSubCategory(String mainCategory, String subCategory, String type) {
        LocalDate today = LocalDate.now();
        String startOfDay = today.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endOfDay = today.plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String onlyDateEndOfDay = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        AddFieldsOperation addFieldsOperation = AddFieldsOperation.addField("productId").withValue(ConvertOperators.ToObjectId.toObjectId("$productId")).build();

        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("product")
                .localField("productId")
                .foreignField("_id")
                .as("product");

        List<Criteria> andCriterias = new ArrayList<>();

        switch (type) {
            case "today" -> {
                final Criteria criteria_deletionDate = Criteria.where("deletionDate").exists(false);
                final Criteria criteria_storeDate = Criteria.where("storeDate").gt(startOfDay).lt(endOfDay);
                final Criteria criteria_usedDate = Criteria.where("usedDate").exists(false);
                andCriterias.add(criteria_storeDate);
                andCriterias.add(criteria_deletionDate);
                andCriterias.add(criteria_usedDate);
            }
            case "soon", "valid" -> {
                final Criteria criteria_usedDate = Criteria.where("usedDate").exists(false);
                final Criteria criteria_deletionDate = Criteria.where("deletionDate").exists(false);
                final Criteria criteria_expiryDate = Criteria.where("expiryDate").gte(onlyDateEndOfDay);
                andCriterias.add(criteria_usedDate);
                andCriterias.add(criteria_deletionDate);
                andCriterias.add(criteria_expiryDate);
            }
        }

        Criteria criteria_mainCategory = Criteria.where("product.mainCategory").is(mainCategory);
        Criteria criteria_subCategory = Criteria.where("product.subCategory").is(subCategory);
        andCriterias.add(criteria_mainCategory);
        andCriterias.add(criteria_subCategory);

        Criteria criteria = new Criteria();
        if (!andCriterias.isEmpty()) {
            criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }

        MatchOperation matchOperation = Aggregation.match(criteria);
        // 將第一到第四步的Operation條件組到Aggregation
        Aggregation aggregation = Aggregation.newAggregation(
                addFieldsOperation,
                lookupOperation,
                matchOperation
        );

        // 用MongoTemplate執行之前的查詢條件，取得List<CountData>
        final AggregationResults<Inventory> results = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class);

        return results.getMappedResults().stream().collect(Collectors.toMap(
                        Inventory::getReceiveItemId,
                        inventory -> inventory,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream().toList();
    }

    @Override
    public List<Inventory> findByReceiveItemId(String receiveItemId) {
        return inventoryRepository.findByReceiveItemId(receiveItemId);
    }
}
