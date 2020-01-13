package com.test.tps.repository;



import com.test.tps.entity.ShipmentDispatchAndETAEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface ShipmentDispatchAndETAEntityRepository extends MongoRepository<ShipmentDispatchAndETAEntity, String> {
    @Query("{ '_id' : ?0 }")
    public List<ShipmentDispatchAndETAEntity> findByExpressnoList(List<String> expressnoList);

}
