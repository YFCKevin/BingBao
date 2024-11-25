package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.dto.MemberDTO;
import com.yfckevin.bingBao.dto.ReceiveRequestDTO;
import com.yfckevin.bingBao.dto.SearchDTO;
import com.yfckevin.bingBao.entity.*;
import com.yfckevin.bingBao.entity.Record;

import java.util.List;

public interface RecordService {
    void importImage(TempMaster tempMaster);

    void createProduct(List<Product> productList);

    void editProduct(Product product);

    void deleteProduct(String name, MemberDTO member);

    void receive(List<Inventory> inventoryList, ReceiveRequestDTO dto);

    void editInventoryAmount(List<Inventory> inventoryList, int usedAmount);

    void editInventoryExpiryDate(List<Inventory> inventoryList, String expiryDate);

    void cloneInventory(List<Inventory> inventoryList, int cloneAmount);

    void editInventoryStorePlace(List<Inventory> saveInventoryList, String newStorePlace);

    void deleteInventory(List<Inventory> saveInventoryList, MemberDTO member);

    void saveSupplier(Supplier savedSupplier);

    void editSupplier(Supplier savedSupplier);

    void deleteSupplier(Supplier savedSupplier, MemberDTO member);

    List<Record> searchRecord(SearchDTO searchDTO);

    Record getRecordInfo(String recordId);
}