package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.dto.SearchDTO;
import com.yfckevin.bingBao.entity.ReceiveForm;
import com.yfckevin.bingBao.repository.ReceiveFormRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReceiveFormServiceImpl implements ReceiveFormService{
    private final ReceiveFormRepository receiveFormRepository;
    private final MongoTemplate mongoTemplate;

    public ReceiveFormServiceImpl(ReceiveFormRepository receiveFormRepository, MongoTemplate mongoTemplate) {
        this.receiveFormRepository = receiveFormRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ReceiveForm save(ReceiveForm receiveForm) {
        return receiveFormRepository.save(receiveForm);
    }

    @Override
    public Optional<ReceiveForm> findById(String id) {
        return receiveFormRepository.findById(id);
    }

    @Override
    public List<ReceiveForm> findAllByDeletionDateIsNullOrderByCreationDateDesc() {
        return receiveFormRepository.findAllByDeletionDateIsNullOrderByCreationDateDesc();
    }

    @Override
    public List<ReceiveForm> findReceiveFormByCondition(SearchDTO searchDTO) {
        List<Criteria> andCriterias = new ArrayList<>();
        List<Criteria> orCriterias = new ArrayList<>();

        Criteria criteria = Criteria.where("deletionDate").exists(false);

        List<AggregationOperation> aggregationOperations = new ArrayList<>();
        AggregationOperation lookupOperation = Aggregation.lookup(
                "product",
                "receiveItems.productId",
                "_id",
                "productInfo"
        );
        aggregationOperations.add(lookupOperation);

        if (StringUtils.isNotBlank(searchDTO.getSupplierName())) {
            AggregationOperation matchProductInfoOperation = Aggregation.match(
                    Criteria.where("productInfo.name").regex(searchDTO.getKeyword(), "i")
            );
            aggregationOperations.add(matchProductInfoOperation);
        }

        if (StringUtils.isNotBlank(searchDTO.getKeyword())) {
            Criteria criteria_itemName = Criteria.where("receiveItems")
                    .elemMatch(Criteria.where("name").regex(searchDTO.getKeyword(), "i"));
            orCriterias.add(criteria_itemName);
        }

        if (StringUtils.isNotBlank(searchDTO.getStartDate()) && StringUtils.isNotBlank(searchDTO.getEndDate())) {
            Criteria criteria_start = Criteria.where("receiveDate").gte(searchDTO.getStartDate());
            Criteria criteria_end = Criteria.where("receiveDate").lte(searchDTO.getEndDate());
            andCriterias.add(criteria_start);
            andCriterias.add(criteria_end);
        }

        if(!orCriterias.isEmpty()) {
            criteria = criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }
        if(!andCriterias.isEmpty()) {
            criteria = criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }

        AggregationOperation matchBaseCriteriaOperation = Aggregation.match(criteria);
        aggregationOperations.add(matchBaseCriteriaOperation);

        AggregationOperation sortOperation = Aggregation.sort(Sort.by(Sort.Order.desc("creationDate")));
        aggregationOperations.add(sortOperation);

        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);

        AggregationResults<ReceiveForm> results = mongoTemplate.aggregate(aggregation, "receive_form", ReceiveForm.class);
        return results.getMappedResults();
    }
}
