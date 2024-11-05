package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.entity.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataProcessServiceImpl implements DataProcessService{
    private final RedisTemplate<String, String> redisTemplate;
    private HashOperations<String, String, String> hashOperations;
    public static final String BING_BAO_INVENTORY_KEY_PREFIX = "bingBao_inventory:";
    public static final String BING_BAO_PRODUCT_KEY_PREFIX = "bingBao_product:";
    private final SimpleDateFormat sdf;

    public DataProcessServiceImpl(RedisTemplate<String, String> redisTemplate, @Qualifier("sdf") SimpleDateFormat sdf) {
        this.redisTemplate = redisTemplate;
        this.sdf = sdf;
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public void inventoryDataProcess(List<Inventory> inventoriesToUpdate) {
        for (Inventory inventory : inventoriesToUpdate) {
            String redisKey = BING_BAO_INVENTORY_KEY_PREFIX + inventory.getId();

            Map<String, String> inventoryMap = new HashMap<>();
            inventoryMap.put("storePlace", inventory.getStorePlace().getLabel());
            inventoryMap.put("usedDate", inventory.getUsedDate() != null ? inventory.getUsedDate() : null);
            inventoryMap.put("deletionDate", inventory.getDeletionDate() != null ? inventory.getDeletionDate() : null);
            inventoryMap.put("expiryDate", inventory.getExpiryDate() != null ? inventory.getExpiryDate() : null);
            inventoryMap.put("productId", inventory.getProductId());
            inventoryMap.put("creator", inventory.getCreator());
            inventoryMap.put("creationDate", inventory.getCreationDate() != null ? inventory.getCreationDate() : null);
            inventoryMap.put("supplierId", inventory.getSupplierId());
            inventoryMap.put("id", inventory.getId());

            hashOperations.putAll(redisKey, inventoryMap);

            redisTemplate.opsForSet().add(BING_BAO_INVENTORY_KEY_PREFIX, redisKey);
        }
    }

    @Override
    public void productDataProcess(Product product) {
        constructProduct(product, product.getDeletionDate());
    }

    @Override
    public void productDataProcessBatch(List<Product> productsToUpdate) {
        for (Product product : productsToUpdate) {
            constructProduct(product, product.getDeletionDate());
        }
    }

    @Override
    public void productDataProcessToDelete(Product product) {
        constructProduct(product, sdf.format(new Date()));
    }


    private void constructProduct(Product product, String deletionDate) {
        String redisKey = BING_BAO_PRODUCT_KEY_PREFIX + product.getId();

        Map<String, String> productMap = new HashMap<>();
        productMap.put("creator", product.getCreator());
        productMap.put("description", product.getDescription());
        productMap.put("id", product.getId());
        productMap.put("coverName", product.getCoverName());
        productMap.put("addShoppingList", String.valueOf(product.isAddShoppingList()));
        productMap.put("creationDate", product.getCreationDate());
        productMap.put("deletionDate", deletionDate);
        productMap.put("inventoryAlert", product.getInventoryAlert());
        productMap.put("mainCategory", product.getMainCategory() != null ? product.getMainCategory().getLabel() : null);
        productMap.put("modificationDate", product.getModificationDate());
        productMap.put("modifier", product.getModifier());
        productMap.put("name", product.getName());
        productMap.put("overdueNotice", product.getOverdueNotice());
        productMap.put("packageForm", product.getPackageForm() != null ? product.getPackageForm().getLabel() :null);
        productMap.put("packageNumber", product.getPackageNumber());
        productMap.put("packageQuantity", product.getPackageQuantity());
        productMap.put("packageUnit", product.getPackageUnit() != null ? product.getPackageUnit().getLabel() : null);
        productMap.put("price", String.valueOf(product.getPrice()));
        productMap.put("serialNumber", product.getSerialNumber());
        productMap.put("subCategory", product.getSubCategory() != null ? product.getSubCategory().getLabel() : null);
        hashOperations.putAll(redisKey, productMap);

        redisTemplate.opsForSet().add(BING_BAO_PRODUCT_KEY_PREFIX, redisKey);
    }
}
