package com.yfckevin.bingBao.service;


import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.entity.Product;

import java.util.List;

public interface DataProcessService {
    void inventoryDataProcess (List<Inventory> inventoriesToUpdate);
    void productDataProcess (Product productsToUpdate);
    void productDataProcessBatch (List<Product> productsToUpdate);
    void productDataProcessToDelete(Product productsToDelete);
}
