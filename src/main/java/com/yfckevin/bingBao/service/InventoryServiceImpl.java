package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.entity.Product;
import com.yfckevin.bingBao.enums.PackageForm;
import com.yfckevin.bingBao.enums.StorePlace;
import com.yfckevin.bingBao.repository.InventoryRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {
    Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private final InventoryRepository inventoryRepository;
    private final ProductService productService;
    private final MongoTemplate mongoTemplate;
    static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public InventoryServiceImpl(InventoryRepository inventoryRepository, ProductService productService, MongoTemplate mongoTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.productService = productService;
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
    public Map<String, Map<Long, List<Inventory>>> findByStoreDateIsTodayAndNoUsedAndNoDeleteAndInValidPeriod() {
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

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> findInventoryExpiringSoonAndNoUsedAndNoDeleteAndInValidPeriod() {
        LocalDate today = LocalDate.now();
        String onlyDateEndOfDay = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("usedDate").exists(false),
                Criteria.where("deletionDate").exists(false),
                Criteria.where("expiryDate").gte(onlyDateEndOfDay)
        );
        Query query = new Query(criteria);
        List<Inventory> inventoriesFromMongo = mongoTemplate.find(query, Inventory.class);

        List<Inventory> inventoryList = new ArrayList<>();
        for (Inventory inventory : inventoriesFromMongo) {
            try {
                int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                    inventoryList.add(inventory);
                }
            } catch (NumberFormatException | DateTimeParseException e) {
                logger.error(e.getMessage(), e);
            }
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> findInventoryWithinValidityPeriodAndNoUsedAndNoDelete() {
        LocalDate today = LocalDate.now();
        String onlyDateEndOfDay = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("usedDate").exists(false),
                Criteria.where("deletionDate").exists(false),
                Criteria.where("expiryDate").gte(onlyDateEndOfDay)
        );
        Query query = new Query(criteria);
        final List<Inventory> inventoryList = mongoTemplate.find(query, Inventory.class);

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));
        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));
        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> searchByName(String keyword, String type) {
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

        // 用MongoTemplate執行之前的查詢條件
        List<Inventory> inventoriesFromMongo = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class).getMappedResults();
        List<Inventory> inventoryList = new ArrayList<>();

        if ("soon".equals(type)) {

            for (Inventory inventory : inventoriesFromMongo) {
                try {
                    int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                    LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                    LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                    if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                        inventoryList.add(inventory);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            inventoryList = inventoriesFromMongo;
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategory(String keyword, String mainCategory, String type) {
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

        // 用MongoTemplate執行之前的查詢條件
        List<Inventory> inventoriesFromMongo = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class).getMappedResults();
        List<Inventory> inventoryList = new ArrayList<>();

        if ("soon".equals(type)) {

            for (Inventory inventory : inventoriesFromMongo) {
                try {
                    int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                    LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                    LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                    if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                        inventoryList.add(inventory);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            inventoryList = inventoriesFromMongo;
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> searchByMainCategory(String mainCategory, String type) {
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

        // 用MongoTemplate執行之前的查詢條件
        List<Inventory> inventoriesFromMongo = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class).getMappedResults();
        List<Inventory> inventoryList = new ArrayList<>();

        if ("soon".equals(type)) {

            for (Inventory inventory : inventoriesFromMongo) {
                try {
                    int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                    LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                    LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                    if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                        inventoryList.add(inventory);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            inventoryList = inventoriesFromMongo;
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategoryAndSubCategory(String keyword, String mainCategory, String subCategory, String type) {
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

        // 用MongoTemplate執行之前的查詢條件
        List<Inventory> inventoriesFromMongo = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class).getMappedResults();
        List<Inventory> inventoryList = new ArrayList<>();

        if ("soon".equals(type)) {

            for (Inventory inventory : inventoriesFromMongo) {
                try {
                    int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                    LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                    LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                    if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                        inventoryList.add(inventory);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            inventoryList = inventoriesFromMongo;
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> searchByMainCategoryAndSubCategory(String mainCategory, String subCategory, String type) {
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

        // 用MongoTemplate執行之前的查詢條件
        List<Inventory> inventoriesFromMongo = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class).getMappedResults();
        List<Inventory> inventoryList = new ArrayList<>();

        if ("soon".equals(type)) {

            for (Inventory inventory : inventoriesFromMongo) {
                try {
                    int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                    LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                    LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                    if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                        inventoryList.add(inventory);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            inventoryList = inventoriesFromMongo;
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public List<Inventory> findByReceiveItemId(String receiveItemId) {
        return inventoryRepository.findByReceiveItemId(receiveItemId);
    }

    @Override
    public List<Inventory> findInventoryNoticeDateIsBeforeExpiryDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String onlyDateEndOfDay = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("usedDate").exists(false),
                Criteria.where("deletionDate").exists(false),
                Criteria.where("expiryDate").gte(onlyDateEndOfDay)
        );
        Query query = new Query(criteria);
        List<Inventory> inventoriesFromMongo = mongoTemplate.find(query, Inventory.class);

        List<Inventory> inventoryList = new ArrayList<>();
        for (Inventory inventory : inventoriesFromMongo) {
            try {
                int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                if (expiryDate.isEqual(noticeDate)) {
                    inventoryList.add(inventory);
                }
            } catch (NumberFormatException | DateTimeParseException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return inventoryList;
    }

    @Override
    public Optional<Inventory> findById(String id) {
        return inventoryRepository.findById(id);
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> searchByNameAndStorePlace(String keyword, String type, String storePlace) {
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
        Criteria criteria_storePlace;
        if ("HUALIEN_ALL".equals(storePlace)) {
            criteria_storePlace = new Criteria().orOperator(
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_LEFT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_1F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_OTHER.name())
            );
        } else {
            criteria_storePlace = Criteria.where("storePlace").is(storePlace);
        }
        orCriterias.add(criteria_name);
        andCriterias.add(criteria_storePlace);

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

        // 用MongoTemplate執行之前的查詢條件
        List<Inventory> inventoriesFromMongo = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class).getMappedResults();
        List<Inventory> inventoryList = new ArrayList<>();

        if ("soon".equals(type)) {

            for (Inventory inventory : inventoriesFromMongo) {
                try {
                    int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                    LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                    LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                    if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                        inventoryList.add(inventory);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            inventoryList = inventoriesFromMongo;
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategoryAndStorePlace(String keyword, String mainCategory, String type, String storePlace) {
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
        Criteria criteria_storePlace;
        if ("HUALIEN_ALL".equals(storePlace)) {
            criteria_storePlace = new Criteria().orOperator(
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_LEFT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_1F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_OTHER.name())
            );
        } else {
            criteria_storePlace = Criteria.where("storePlace").is(storePlace);
        }
        Criteria criteria_mainCategory = Criteria.where("product.mainCategory").is(mainCategory);
        orCriterias.add(criteria_name);
        andCriterias.add(criteria_storePlace);
        andCriterias.add(criteria_mainCategory);

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

        // 用MongoTemplate執行之前的查詢條件
        List<Inventory> inventoriesFromMongo = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class).getMappedResults();
        List<Inventory> inventoryList = new ArrayList<>();

        if ("soon".equals(type)) {

            for (Inventory inventory : inventoriesFromMongo) {
                try {
                    int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                    LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                    LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                    if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                        inventoryList.add(inventory);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            inventoryList = inventoriesFromMongo;
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> searchByMainCategoryAndStorePlace(String mainCategory, String type, String storePlace) {
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

        Criteria criteria_storePlace;
        if ("HUALIEN_ALL".equals(storePlace)) {
            criteria_storePlace = new Criteria().orOperator(
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_LEFT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_1F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_OTHER.name())
            );
        } else {
            criteria_storePlace = Criteria.where("storePlace").is(storePlace);
        }
        Criteria criteria_mainCategory = Criteria.where("product.mainCategory").is(mainCategory);
        andCriterias.add(criteria_storePlace);
        andCriterias.add(criteria_mainCategory);

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

        // 用MongoTemplate執行之前的查詢條件
        List<Inventory> inventoriesFromMongo = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class).getMappedResults();
        List<Inventory> inventoryList = new ArrayList<>();

        if ("soon".equals(type)) {

            for (Inventory inventory : inventoriesFromMongo) {
                try {
                    int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                    LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                    LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                    if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                        inventoryList.add(inventory);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            inventoryList = inventoriesFromMongo;
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> searchByNameAndMainCategoryAndSubCategoryAndStorePlace(String keyword, String mainCategory, String subCategory, String type, String storePlace) {
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
        Criteria criteria_storePlace;
        if ("HUALIEN_ALL".equals(storePlace)) {
            criteria_storePlace = new Criteria().orOperator(
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_LEFT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_1F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_OTHER.name())
            );
        } else {
            criteria_storePlace = Criteria.where("storePlace").is(storePlace);
        }
        Criteria criteria_mainCategory = Criteria.where("product.mainCategory").is(mainCategory);
        Criteria criteria_subCategory = Criteria.where("product.subCategory").is(subCategory);
        orCriterias.add(criteria_name);
        andCriterias.add(criteria_storePlace);
        andCriterias.add(criteria_mainCategory);
        andCriterias.add(criteria_subCategory);

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

        // 用MongoTemplate執行之前的查詢條件
        List<Inventory> inventoriesFromMongo = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class).getMappedResults();
        List<Inventory> inventoryList = new ArrayList<>();

        if ("soon".equals(type)) {

            for (Inventory inventory : inventoriesFromMongo) {
                try {
                    int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                    LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                    LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                    if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                        inventoryList.add(inventory);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            inventoryList = inventoriesFromMongo;
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> searchByMainCategoryAndSubCategoryAndStorePlace(String mainCategory, String subCategory, String type, String storePlace) {
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

        Criteria criteria_storePlace;
        if ("HUALIEN_ALL".equals(storePlace)) {
            criteria_storePlace = new Criteria().orOperator(
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_LEFT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_1F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_OTHER.name())
            );
        } else {
            criteria_storePlace = Criteria.where("storePlace").is(storePlace);
        }
        Criteria criteria_mainCategory = Criteria.where("product.mainCategory").is(mainCategory);
        Criteria criteria_subCategory = Criteria.where("product.subCategory").is(subCategory);
        andCriterias.add(criteria_storePlace);
        andCriterias.add(criteria_mainCategory);
        andCriterias.add(criteria_subCategory);

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

        // 用MongoTemplate執行之前的查詢條件
        List<Inventory> inventoriesFromMongo = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class).getMappedResults();
        List<Inventory> inventoryList = new ArrayList<>();

        if ("soon".equals(type)) {

            for (Inventory inventory : inventoriesFromMongo) {
                try {
                    int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                    LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                    LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                    if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                        inventoryList.add(inventory);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            inventoryList = inventoriesFromMongo;
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> searchByStorePlace(String type, String storePlace) {
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

        Criteria criteria_storePlace;
        if ("HUALIEN_ALL".equals(storePlace)) {
            criteria_storePlace = new Criteria().orOperator(
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_LEFT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_2F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_REF_1F_RIGHT.name()),
                    Criteria.where("storePlace").is(StorePlace.HUALIEN_OTHER.name())
            );
        } else {
            criteria_storePlace = Criteria.where("storePlace").is(storePlace);
        }
        andCriterias.add(criteria_storePlace);

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

        // 用MongoTemplate執行之前的查詢條件
        List<Inventory> inventoriesFromMongo = mongoTemplate.aggregate(aggregation, "inventory", Inventory.class).getMappedResults();
        List<Inventory> inventoryList = new ArrayList<>();

        if ("soon".equals(type)) {

            for (Inventory inventory : inventoriesFromMongo) {
                try {
                    int overdueNoticeDays = Integer.parseInt(inventory.getOverdueNotice());
                    LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);
                    LocalDate noticeDate = today.plusDays(overdueNoticeDays);

                    if (expiryDate.isBefore(noticeDate) || expiryDate.isEqual(noticeDate)) {
                        inventoryList.add(inventory);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            inventoryList = inventoriesFromMongo;
        }

        Map<String, Long> itemCountMap = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        Map<String, Map<Long, List<Inventory>>> result = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));

        return result;
    }

    @Override
    public List<Inventory> findInventoryWithExpiryDateInSevenDays() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String onlyDateEndOfDay = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 設定查詢條件
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("usedDate").exists(false),
                Criteria.where("deletionDate").exists(false),
                Criteria.where("expiryDate").gte(onlyDateEndOfDay)
        );
        Query query = new Query(criteria);
        List<Inventory> inventoriesFromMongo = mongoTemplate.find(query, Inventory.class);

        List<Inventory> inventoryList = new ArrayList<>();
        LocalDate noticeDate = today.plusDays(7); // 計算今天加7天的日期

        for (Inventory inventory : inventoriesFromMongo) {
            try {
                LocalDate expiryDate = LocalDate.parse(inventory.getExpiryDate(), dateFormatter);

                // 如果expiryDate等於今天加7天的日期，則加入清單
                if (expiryDate.isEqual(noticeDate)) {
                    inventoryList.add(inventory);
                }
            } catch (DateTimeParseException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return inventoryList;
    }

    @Override
    public Map<String, Map<Long, List<Inventory>>> findAllAndDeletionDateIsNull() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("usedDate").exists(false),
                Criteria.where("deletionDate").exists(false),
                Criteria.where("expiryDate").gte(today)
        );
        Query query = new Query(criteria);
        List<Inventory> inventoriesFromMongo = mongoTemplate.find(query, Inventory.class);

        Map<String, Long> itemCountMap = inventoriesFromMongo.stream()
                .collect(Collectors.groupingBy(Inventory::getReceiveItemId, Collectors.counting()));

        return inventoriesFromMongo.stream()
                .filter(inventory -> {
                    return productService.findById(inventory.getProductId())
                            .map(product -> {
                                if (StringUtils.isBlank(product.getInventoryAlert())) {
                                    return false;
                                }
                                BigDecimal inventoryAlert = new BigDecimal(product.getInventoryAlert());
                                BigDecimal totalAmount = new BigDecimal(itemCountMap.get(inventory.getReceiveItemId()));
                                BigDecimal totalInventoryAlert = PackageForm.COMPLETE.equals(product.getPackageForm())
                                        ? inventoryAlert.multiply(new BigDecimal(product.getPackageQuantity()))
                                        : inventoryAlert;
                                // 只保留 totalAmount 小於等於 inventoryAlert 的Inventory
                                return totalInventoryAlert.compareTo(totalAmount) >= 0;
                            })
                            .orElse(false);  // 如果找不到產品，回傳 false
                })
                .collect(Collectors.groupingBy(
                        Inventory::getReceiveItemId,
                        Collectors.groupingBy(
                                inventory -> itemCountMap.get(inventory.getReceiveItemId()),
                                Collectors.toList()
                        )
                ));
    }

    @Override
    public List<Inventory> findByReceiveItemIdAndUsedDateIsNull(String receiveItemId) {
        return inventoryRepository.findByReceiveItemIdAndUsedDateIsNull(receiveItemId);
    }
}
