//ecd:-1288266199H20260411113046_V2.1
package io.elasticore.blueprint.domain.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.*;
import io.elasticore.blueprint.domain.integration.entity.*;





/**
 * JPA repository interface for managing ApiLog entities.
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */


public interface ApiLogRepository extends JpaRepository<ApiLog,Long>, JpaSpecificationExecutor<ApiLog> {



}
