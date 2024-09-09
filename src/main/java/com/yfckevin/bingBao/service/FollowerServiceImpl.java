package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Follower;
import com.yfckevin.bingBao.repository.FollowerRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FollowerServiceImpl implements FollowerService{
    private final FollowerRepository followerRepository;
    private final MongoTemplate mongoTemplate;

    public FollowerServiceImpl(FollowerRepository followerRepository, MongoTemplate mongoTemplate) {
        this.followerRepository = followerRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void save(Follower follower) {
        followerRepository.save(follower);
    }

    @Override
    public Optional<Follower> findByUserId(String userId) {
        return followerRepository.findByUserId(userId);
    }

    @Override
    public List<Follower> findByIdIn(List<String> userIds) {
        return followerRepository.findByIdIn(userIds);
    }

    @Override
    public List<Follower> findAll() {
        return followerRepository.findAll();
    }

    @Override
    public List<Follower> searchFollower(String keyword) {
        List<Criteria> orCriterias = new ArrayList<>();

        Criteria criteria = Criteria.where("deletionDate").exists(false);

        if (StringUtils.isNotBlank(keyword)) {
            Criteria criteria_name = Criteria.where("displayName").regex(keyword, "i");
            orCriterias.add(criteria_name);
        }

        if(!orCriterias.isEmpty()) {
            criteria = criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Follower.class);
    }
}
