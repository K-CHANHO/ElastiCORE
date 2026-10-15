//ecd:-2143339423H20260411113047_V2.1
package io.elasticore.blueprint.domain.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.*;
import io.elasticore.blueprint.domain.shop.entity.*;





/**
 * JPA repository interface for managing VipProfile entities.
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */


public interface VipProfileRepository extends JpaRepository<VipProfile,Long>, JpaSpecificationExecutor<VipProfile> {



}
