//ecd:-501658250H20260926235721_V3.0
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
 * JPA repository interface for managing GiftCard entities.
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */


public interface GiftCardRepository extends JpaRepository<GiftCard,String>, JpaSpecificationExecutor<GiftCard> {



}
