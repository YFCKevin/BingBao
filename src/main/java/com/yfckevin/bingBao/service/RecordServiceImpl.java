package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.dto.*;
import com.yfckevin.bingBao.entity.*;
import com.yfckevin.bingBao.entity.Record;
import com.yfckevin.bingBao.enums.Action;
import com.yfckevin.bingBao.enums.StorePlace;
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
public class RecordServiceImpl implements RecordService{
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
        record.setOperator(tempMaster.getCreator());
        record.setCurrentTime(tempMaster.getCreationDate());
        record.setAction(Action.IMPORT_IMAGE);
        recordRepository.save(record);
    }

    @Override
    public void createProduct(List<Product> productList) {
        Record record = new Record();
        record.setOperator(productList.get(0).getCreator());
        record.setCurrentTime(productList.get(0).getCreationDate());
        record.setItem(productList.stream().map(Product::getName).collect(Collectors.joining(",")));
        record.setAction(Action.CREATE_PRODUCT);
        recordRepository.save(record);
    }

    @Override
    public void editProduct(Product product) {
        Record record = new Record();
        record.setOperator(product.getModifier());
        record.setCurrentTime(product.getModificationDate());
        record.setItem(product.getName());
        record.setAction(Action.EDIT_PRODUCT);
        recordRepository.save(record);
    }

    @Override
    public void deleteProduct(String name, MemberDTO member) {
        Record record = new Record();
        record.setOperator(member.getName());
        record.setCurrentTime(sdf.format(new Date()));
        record.setItem(name);
        record.setAction(Action.DELETE_PRODUCT);
        recordRepository.save(record);
    }

    @Override
    public void receive(List<Inventory> inventoryList, ReceiveRequestDTO dto) {
        Record record = new Record();
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
        record.setStoreRecordList(storedRecordList);
        record.setAction(Action.RECEIVE);
        recordRepository.save(record);
    }

    @Override
    public void editInventoryAmount(List<Inventory> inventoryList, int usedAmount) {
        Record record = new Record();
        record.setOperator(inventoryList.get(0).getModifier());
        record.setCurrentTime(inventoryList.get(0).getModificationDate());
        final Product product = productService.findById(inventoryList.get(0).getProductId()).get();
        record.setItem(product.getName());
        record.setAction(Action.EDIT_INVENTORY_AMOUNT);
        record.setActionDetail("使用數量：" + usedAmount + "個");
        recordRepository.save(record);
    }

    @Override
    public void editInventoryExpiryDate(List<Inventory> inventoryList, String expiryDate) {
        Record record = new Record();
        record.setOperator(inventoryList.get(0).getModifier());
        record.setCurrentTime(inventoryList.get(0).getModificationDate());
        final Product product = productService.findById(inventoryList.get(0).getProductId()).get();
        record.setItem(product.getName());
        record.setAction(Action.EDIT_INVENTORY_EXPIRYDATE);
        record.setActionDetail("[" + product.getName() + "] 修改有效期限為：" + expiryDate);
        recordRepository.save(record);
    }

    @Override
    public void cloneInventory(List<Inventory> inventoryList, int cloneAmount) {
        Record record = new Record();
        record.setOperator(inventoryList.get(0).getCreator());
        record.setCurrentTime(inventoryList.get(0).getCreationDate());
        final Product product = productService.findById(inventoryList.get(0).getProductId()).get();
        record.setItem(product.getName());
        record.setAction(Action.CLONE_INVENTORY);
        record.setActionDetail("[" + product.getName() + "] 增加庫存數量：" + cloneAmount + "個");
        recordRepository.save(record);
    }

    @Override
    public void editInventoryStorePlace(List<Inventory> inventoryList, String newStorePlace) {
        Record record = new Record();
        record.setOperator(inventoryList.get(0).getModifier());
        record.setCurrentTime(inventoryList.get(0).getModificationDate());
        final Product product = productService.findById(inventoryList.get(0).getProductId()).get();
        record.setItem(product.getName());
        record.setAction(Action.EDIT_INVENTORY_STOREPLACE);
        record.setActionDetail("[" + product.getName() + "] 變更新存放位置：" + StorePlace.valueOf(newStorePlace).getLabel());
        recordRepository.save(record);
    }

    @Override
    public void deleteInventory(List<Inventory> inventoryList, MemberDTO member) {
        Record record = new Record();
        record.setOperator(member.getName());
        record.setCurrentTime(inventoryList.get(0).getDeletionDate());
        final Product product = productService.findById(inventoryList.get(0).getProductId()).get();
        record.setItem(product.getName());
        record.setAction(Action.DELETE_INVENTORY);
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

        if(!orCriterias.isEmpty()) {
            criteria = criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }
        if(!andCriterias.isEmpty()) {
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
