package com.mrwind.tdcant.repository;



import com.mrwind.tdcant.entity.ShipmentDispatchAndETAEntity;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface ShipmentDispatchAndETAEntityRepository extends MongoRepository<ShipmentDispatchAndETAEntity, String> {
    @Query("{ '_id' : ?0 }")
    public List<ShipmentDispatchAndETAEntity> findByExpressnoList(List<String> expressnoList);

}
