package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.dto.*;
import com.yfckevin.bingBao.entity.*;
import com.yfckevin.bingBao.entity.Record;
import com.yfckevin.bingBao.enums.Action;
import com.yfckevin.bingBao.enums.StorePlace;
import com.yfckevin.bingBao.enums.TraceState;
import com.yfckevin.bingBao.repository.RecordRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecordServiceImpl implements RecordService {
    private final RecordRepository recordRepository;
    private final ProductService productService;
    private final SimpleDateFormat sdf;
    private final MongoTemplate mongoTemplate;

    public RecordServiceImpl(RecordRepository recordRepository, ProductService productService, @Qualifier("sdf") SimpleDateFormat sdf, MongoTemplate mongoTemplate) {
        this.recordRepository = recordRepository;
        this.productService = productService;
        this.sdf = sdf;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void importImage(TempMaster tempMaster) {
        Record record = new Record();
        record.setTraceId(UUID.randomUUID().toString());
        record.setTempMasterId(tempMaster.getId());
        record.setOperator(tempMaster.getCreator());
        record.setCurrentTime(tempMaster.getCreationDate());
        record.setAction(Action.IMPORT_IMAGE);
        record.setTraceState(TraceState.PRODUCT_STATE);
        recordRepository.save(record);
    }

    @Override
    public void createProduct(List<Product> productList, String tempMasterId) {
        Record record = new Record();
        if (StringUtils.isNotBlank(tempMasterId)) {
            // 多新增食材
            Optional<Record> opt = recordRepository.findByTempMasterId(tempMasterId);
            opt.ifPresent(r -> record.setTraceId(r.getTraceId()));
        } else {
            // 單一新增食材
            record.setTraceId(UUID.randomUUID().toString());
        }
        record.setProductId(productList.stream().map(Product::getId).toList());
        record.setOperator(productList.get(0).getCreator());
        record.setCurrentTime(productList.get(0).getCreationDate());
        record.setItem(productList.stream().map(Product::getName).collect(Collectors.joining(",")));
        record.setAction(Action.CREATE_PRODUCT);
        record.setTraceState(TraceState.PRODUCT_STATE);
        recordRepository.save(record);
    }

    @Override
    public void editProduct(Product product) {
        Record record = new Record();
        recordRepository.findByProductIdAndTraceState(product.getId(), TraceState.PRODUCT_STATE).stream()
                .findFirst()
                .ifPresentOrElse(
                        r -> record.setTraceId(r.getTraceId()),
                        () -> record.setTraceId(UUID.randomUUID().toString())
                );
        record.setProductId(List.of(product.getId()));
        record.setOperator(product.getModifier());
        record.setCurrentTime(product.getModificationDate());
        record.setItem(product.getName());
        record.setAction(Action.EDIT_PRODUCT);
        record.setTraceState(TraceState.PRODUCT_STATE);
        recordRepository.save(record);
    }

    @Override
    public void deleteProduct(String name, String productId, MemberDTO member) {
        Record record = new Record();
        recordRepository.findByProductIdAndTraceState(productId, TraceState.PRODUCT_STATE).stream()
                .findFirst()
                .ifPresentOrElse(
                        r -> record.setTraceId(r.getTraceId()),
                        () -> record.setTraceId(UUID.randomUUID().toString())
                );
        record.setProductId(List.of(productId));
        record.setOperator(member.getName());
        record.setCurrentTime(sdf.format(new Date()));
        record.setItem(name);
        record.setAction(Action.DELETE_PRODUCT);
        record.setTraceState(TraceState.PRODUCT_STATE);
        recordRepository.save(record);
    }

    @Override
    public void receive(List<Inventory> inventoryList, ReceiveRequestDTO dto) {
        Record record = new Record();
        record.setTraceId(UUID.randomUUID().toString());
        record.setReceiveItemId(inventoryList.stream().map(Inventory::getReceiveItemId).distinct().toList());
        record.setOperator(inventoryList.get(0).getCreator());
        record.setCurrentTime(inventoryList.get(0).getCreationDate());
        final List<String> productIds = dto.getSelectedProducts().stream().map(ReceiveProductDTO::getProductId).toList();
        final List<Product> productList = productService.findByIdIn(productIds);
        final Map<String, String> productMap = productList.stream().collect(Collectors.toMap(Product::getId, Product::getName));
        record.setItem(productList.stream().map(Product::getName).collect(Collectors.joining(",")));
        final List<StoredRecord> storedRecordList = dto.getSelectedProducts().stream().map(p -> {
            final String productName = productMap.get(p.getProductId());
            StoredRecord storedRecord = new StoredRecord();
            storedRecord.setName(productName);
            storedRecord.setStorePlace(p.getStorePlace());
            storedRecord.setAmount(String.valueOf(p.getQuantity()));
            storedRecord.setTotalAmount(String.valueOf(p.getTotalQuantity()));
            storedRecord.setExpiryDate(LocalDate.now().plusDays(Long.parseLong(p.getExpiryDate())).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            return storedRecord;
        }).toList();
        record.setProductId(productIds);
        record.setStoreRecordList(storedRecordList);
        record.setAction(Action.RECEIVE);
        record.setTraceState(TraceState.INVENTORY_STATE);
        recordRepository.save(record);
    }

    @Override
    public void editInventoryAmount(List<Inventory> inventoryList, int usedAmount) {
        Record record = new Record();
        final String receiveItemId = inventoryList.get(0).getReceiveItemId();
        final String productId = inventoryList.get(0).getProductId();
        recordRepository.findByReceiveItemIdAndTraceState(receiveItemId, TraceState.INVENTORY_STATE).stream()
                .findFirst()
                .ifPresentOrElse(
                        r -> record.setTraceId(r.getTraceId()),
                        () -> record.setTraceId(UUID.randomUUID().toString())
                );
        record.setReceiveItemId(List.of(receiveItemId));
        record.setProductId(List.of(productId));
        record.setOperator(inventoryList.get(0).getModifier());
        record.setCurrentTime(inventoryList.get(0).getModificationDate());
        final Product product = productService.findById(inventoryList.get(0).getProductId()).get();
        record.setItem(product.getName());
        record.setAction(Action.EDIT_INVENTORY_AMOUNT);
        record.setTraceState(TraceState.INVENTORY_STATE);
        record.setActionDetail("使用數量：" + usedAmount + "個");
        recordRepository.save(record);
    }

    @Override
    public void editInventoryExpiryDate(List<Inventory> inventoryList, String expiryDate) {
        Record record = new Record();
        final String receiveItemId = inventoryList.get(0).getReceiveItemId();
        final String productId = inventoryList.get(0).getProductId();
        recordRepository.findByReceiveItemIdAndTraceState(receiveItemId, TraceState.INVENTORY_STATE).stream()
                .findFirst()
                .ifPresentOrElse(
                        r -> record.setTraceId(r.getTraceId()),
                        () -> record.setTraceId(UUID.randomUUID().toString())
                );
        record.setReceiveItemId(List.of(receiveItemId));
        record.setProductId(List.of(productId));
        record.setOperator(inventoryList.get(0).getModifier());
        record.setCurrentTime(inventoryList.get(0).getModificationDate());
        final Product product = productService.findById(inventoryList.get(0).getProductId()).get();
        record.setItem(product.getName());
        record.setAction(Action.EDIT_INVENTORY_EXPIRYDATE);
        record.setTraceState(TraceState.INVENTORY_STATE);
        record.setActionDetail("[" + product.getName() + "] 修改有效期限為：" + expiryDate);
        recordRepository.save(record);
    }

    @Override
    public void cloneInventory(List<Inventory> inventoryList, int cloneAmount) {
        Record record = new Record();
        final String receiveItemId = inventoryList.get(0).getReceiveItemId();
        final String productId = inventoryList.get(0).getProductId();
        recordRepository.findByReceiveItemIdAndTraceState(receiveItemId, TraceState.INVENTORY_STATE).stream()
                .findFirst()
                .ifPresentOrElse(
                        r -> record.setTraceId(r.getTraceId()),
                        () -> record.setTraceId(UUID.randomUUID().toString())
                );
        record.setReceiveItemId(List.of(receiveItemId));
        record.setProductId(List.of(productId));
        record.setOperator(inventoryList.get(0).getCreator());
        record.setCurrentTime(inventoryList.get(0).getCreationDate());
        final Product product = productService.findById(inventoryList.get(0).getProductId()).get();
        record.setItem(product.getName());
        record.setAction(Action.CLONE_INVENTORY);
        record.setTraceState(TraceState.INVENTORY_STATE);
        record.setActionDetail("[" + product.getName() + "] 增加庫存數量：" + cloneAmount + "個");
        recordRepository.save(record);
    }

    @Override
    public void editInventoryStorePlace(List<Inventory> inventoryList, String newStorePlace) {
        Record record = new Record();
        final String receiveItemId = inventoryList.get(0).getReceiveItemId();
        final String productId = inventoryList.get(0).getProductId();
        recordRepository.findByReceiveItemIdAndTraceState(receiveItemId, TraceState.INVENTORY_STATE).stream()
                .findFirst()
                .ifPresentOrElse(
                        r -> record.setTraceId(r.getTraceId()),
                        () -> record.setTraceId(UUID.randomUUID().toString())
                );
        record.setReceiveItemId(List.of(receiveItemId));
        record.setProductId(List.of(productId));
        record.setOperator(inventoryList.get(0).getModifier());
        record.setCurrentTime(inventoryList.get(0).getModificationDate());
        final Product product = productService.findById(inventoryList.get(0).getProductId()).get();
        record.setItem(product.getName());
        record.setAction(Action.EDIT_INVENTORY_STOREPLACE);
        record.setTraceState(TraceState.INVENTORY_STATE);
        record.setActionDetail("[" + product.getName() + "] 變更新存放位置：" + StorePlace.valueOf(newStorePlace).getLabel());
        recordRepository.save(record);
    }

    @Override
    public void deleteInventory(List<Inventory> inventoryList, MemberDTO member) {
        Record record = new Record();
        final String receiveItemId = inventoryList.get(0).getReceiveItemId();
        final String productId = inventoryList.get(0).getProductId();
        recordRepository.findByReceiveItemIdAndTraceState(receiveItemId, TraceState.INVENTORY_STATE).stream()
                .findFirst()
                .ifPresentOrElse(
                        r -> record.setTraceId(r.getTraceId()),
                        () -> record.setTraceId(UUID.randomUUID().toString())
                );
        record.setReceiveItemId(List.of(receiveItemId));
        record.setProductId(List.of(productId));
        record.setOperator(member.getName());
        record.setCurrentTime(inventoryList.get(0).getDeletionDate());
        final Product product = productService.findById(inventoryList.get(0).getProductId()).get();
        record.setItem(product.getName());
        record.setAction(Action.DELETE_INVENTORY);
        record.setTraceState(TraceState.INVENTORY_STATE);
        recordRepository.save(record);
    }

    @Override
    public void addToShoppingList(List<Inventory> inventoryList, List<ShoppingItem> shoppingList) {
        Record record = new Record();
        final List<String> receiveItemIds = inventoryList.stream().map(Inventory::getReceiveItemId).distinct().toList();
        final List<String> productIds = inventoryList.stream().map(Inventory::getProductId).distinct().toList();
        record.setReceiveItemId(receiveItemIds);
        record.setProductId(productIds);
        record.setShoppingItemId(shoppingList.stream().map(ShoppingItem::getId).distinct().toList());
        record.setTraceId(UUID.randomUUID().toString());    //創建新的traceId
        record.setTraceState(TraceState.SHOPPING_STATE);
        record.setOperator("系統自動加入");
        record.setCurrentTime(sdf.format(new Date()));
        final List<Product> productList = productService.findByIdIn(productIds);
        final String productNameStr = productList.stream().map(Product::getName).collect(Collectors.joining(","));
        record.setItem(productNameStr);
        record.setAction(Action.ADD_SHOPPING_LIST);
        record.setActionDetail(productNameStr + "[ 加入待購清單 ]");
        recordRepository.save(record);
    }

    @Override
    public void deleteShoppingItem(ShoppingItem shoppingItem) {
        Record record = new Record();
        recordRepository.findByShoppingItemIdAndTraceState(shoppingItem.getId(), TraceState.SHOPPING_STATE).stream()
                .findFirst()
                .ifPresentOrElse(
                        r -> record.setTraceId(r.getTraceId()),
                        () -> record.setTraceId(UUID.randomUUID().toString())
                );
        record.setShoppingItemId(List.of(shoppingItem.getId()));
        if (StringUtils.isNotBlank(shoppingItem.getProductId())) {
            record.setProductId(List.of(shoppingItem.getProductId()));
        }
        record.setTraceState(TraceState.SHOPPING_STATE);
        record.setOperator(shoppingItem.getModifier());
        record.setCurrentTime(shoppingItem.getDeletionDate());
        record.setItem(shoppingItem.getName());
        record.setAction(Action.DELETE_SHOPPING_ITEM);
        recordRepository.save(record);
    }

    @Override
    public void changePriority(ShoppingItem shoppingItem) {
        Record record = new Record();
        recordRepository.findByShoppingItemIdAndTraceState(shoppingItem.getId(), TraceState.SHOPPING_STATE).stream()
                .findFirst()
                .ifPresentOrElse(
                        r -> record.setTraceId(r.getTraceId()),
                        () -> record.setTraceId(UUID.randomUUID().toString())
                );
        record.setShoppingItemId(List.of(shoppingItem.getId()));
        if (StringUtils.isNotBlank(shoppingItem.getProductId())) {
            record.setProductId(List.of(shoppingItem.getProductId()));
        }
        record.setTraceState(TraceState.SHOPPING_STATE);
        record.setOperator(shoppingItem.getModifier());
        record.setCurrentTime(shoppingItem.getModificationDate());
        record.setItem(shoppingItem.getName());
        record.setAction(Action.CHANGE_PRIORITY);
        record.setActionDetail("[" + shoppingItem.getName() + "] 變更優先級別為：" + shoppingItem.getPriorityType().getLabel());
        recordRepository.save(record);
    }

    @Override
    public void editPurchaseQuantity(ShoppingItem shoppingItem) {
        Record record = new Record();
        recordRepository.findByShoppingItemIdAndTraceState(shoppingItem.getId(), TraceState.SHOPPING_STATE).stream()
                .findFirst()
                .ifPresentOrElse(
                        r -> record.setTraceId(r.getTraceId()),
                        () -> record.setTraceId(UUID.randomUUID().toString())
                );
        record.setShoppingItemId(List.of(shoppingItem.getId()));
        if (StringUtils.isNotBlank(shoppingItem.getProductId())) {
            record.setProductId(List.of(shoppingItem.getProductId()));
        }
        record.setTraceState(TraceState.SHOPPING_STATE);
        record.setOperator(shoppingItem.getModifier());
        record.setCurrentTime(shoppingItem.getModificationDate());
        record.setItem(shoppingItem.getName());
        record.setAction(Action.EDIT_PURCHASE_QUANTITY);
        record.setActionDetail("[" + shoppingItem.getName() + "] 變更回購品項數量為：" + shoppingItem.getPurchaseQuantity() + "個");
        recordRepository.save(record);
    }

    @Override
    public void changePurchase(ShoppingItem shoppingItem) {
        Record record = new Record();
        recordRepository.findByShoppingItemIdAndTraceState(shoppingItem.getId(), TraceState.SHOPPING_STATE).stream()
                .findFirst()
                .ifPresentOrElse(
                        r -> record.setTraceId(r.getTraceId()),
                        () -> record.setTraceId(UUID.randomUUID().toString())
                );
        record.setShoppingItemId(List.of(shoppingItem.getId()));
        if (StringUtils.isNotBlank(shoppingItem.getProductId())) {
            record.setProductId(List.of(shoppingItem.getProductId()));
        }
        record.setTraceState(TraceState.SHOPPING_STATE);
        record.setOperator(shoppingItem.getModifier());
        record.setCurrentTime(shoppingItem.getModificationDate());
        record.setItem(shoppingItem.getName());
        record.setAction(Action.CHANGE_PURCHASE);
        record.setActionDetail("[" + shoppingItem.getName() + "] 已購買完成");
        recordRepository.save(record);
    }

    @Override
    public void saveSupplier(Supplier savedSupplier) {
        Record record = new Record();
        record.setOperator(savedSupplier.getCreator());
        record.setCurrentTime(savedSupplier.getCreationDate());
        record.setItem(savedSupplier.getName());
        record.setAction(Action.CREATE_SUPPLIER);
        recordRepository.save(record);
    }

    @Override
    public void editSupplier(Supplier savedSupplier) {
        Record record = new Record();
        record.setOperator(savedSupplier.getModifier());
        record.setCurrentTime(savedSupplier.getModificationDate());
        record.setItem(savedSupplier.getName());
        record.setAction(Action.EDIT_SUPPLIER);
        recordRepository.save(record);
    }

    @Override
    public void deleteSupplier(Supplier savedSupplier, MemberDTO member) {
        Record record = new Record();
        record.setOperator(member.getName());
        record.setCurrentTime(savedSupplier.getDeletionDate());
        record.setItem(savedSupplier.getName());
        record.setAction(Action.DELETE_SUPPLIER);
        recordRepository.save(record);
    }

    @Override
    public List<Record> searchRecord(SearchDTO searchDTO) {
        final String keyword = searchDTO.getKeyword().trim();
        final String startDate = searchDTO.getStartDate();
        final String endDate = searchDTO.getEndDate();
        final String actionType = searchDTO.getActionType();
        final String memberName = searchDTO.getMemberName();

        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        Criteria criteria = Criteria.where("deletionDate").exists(false);

        if (StringUtils.isNotBlank(keyword)) {
            Criteria criteria_name = Criteria.where("item").regex(keyword, "i");
            orCriterias.add(criteria_name);
        }

        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            Criteria criteria_start = Criteria.where("currentTime").gte(startDate);
            Criteria criteria_end = Criteria.where("currentTime").lte(endDate);
            andCriterias.add(criteria_start);
            andCriterias.add(criteria_end);
        }

        if (StringUtils.isNotBlank(actionType)) {
            Criteria criteria_action = Criteria.where("action").is(actionType);
            andCriterias.add(criteria_action);
        }

        if (StringUtils.isNotBlank(memberName)) {
            Criteria criteria_memberName = Criteria.where("operator").is(memberName);
            andCriterias.add(criteria_memberName);
        }

        if (!orCriterias.isEmpty()) {
            criteria = criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }
        if (!andCriterias.isEmpty()) {
            criteria = criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Order.desc("currentTime")));

        return mongoTemplate.find(query, Record.class);
    }

    @Override
    public Record getRecordInfo(String recordId) {
        return recordRepository.findById(recordId).get();
    }
}
