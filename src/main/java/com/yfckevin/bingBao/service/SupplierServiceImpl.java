package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Supplier;
import com.yfckevin.bingBao.repository.SupplierRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SupplierServiceImpl implements SupplierService{
    private final SupplierRepository supplierRepository;
    private final MongoTemplate mongoTemplate;

    public SupplierServiceImpl(SupplierRepository supplierRepository, MongoTemplate mongoTemplate) {
        this.supplierRepository = supplierRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<Supplier> findById(String supplierId) {
        return supplierRepository.findById(supplierId);
    }

    @Override
    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @Override
    public List<Supplier> findAllByOrderByCreationDateDesc() {
        return supplierRepository.findAllByOrderByCreationDateDesc();
    }

    @Override
    public List<Supplier> findSupplierByCondition(String keyword) {
        List<Criteria> orCriterias = new ArrayList<>();

        Criteria criteria = Criteria.where("deletionDate").exists(false);

        if (StringUtils.isNotBlank(keyword)) {
            Criteria criteria_name = Criteria.where("name").regex(keyword, "i");
            Criteria criteria_address = Criteria.where("address").regex(keyword, "i");
            Criteria criteria_phone = Criteria.where("phone").regex(keyword, "i");
            Criteria criteria_website = Criteria.where("website").regex(keyword, "i");
            orCriterias.add(criteria_name);
            orCriterias.add(criteria_address);
            orCriterias.add(criteria_phone);
            orCriterias.add(criteria_website);
        }

        if(!orCriterias.isEmpty()) {
            criteria = criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Order.desc("creationDate")));

        return mongoTemplate.find(query, Supplier.class);
    }
}
