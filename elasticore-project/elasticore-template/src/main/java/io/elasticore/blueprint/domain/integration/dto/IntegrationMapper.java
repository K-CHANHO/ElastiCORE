//ecd:-232443533H20260414094512_V2.1
package io.elasticore.blueprint.domain.integration.dto;

import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.EntityManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import io.elasticore.blueprint.domain.integration.entity.*;
import io.elasticore.blueprint.domain.integration.dto.*;
import io.elasticore.blueprint.domain.integration.enums.*;

import io.elasticore.blueprint.domain.integration.enums.*;
import io.elasticore.blueprint.domain.shop.enums.*;
import io.elasticore.springboot3.mapper.MappingContext;
import io.elasticore.runtime.security.TransformPermissionChecker;

/**
 * Provides transformation utilities between DTOs and entities,
 * including conditional mapping, permission validation, and dynamic query support.
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */


public class IntegrationMapper {

    private static TransformPermissionChecker permissionChecker;

    public static void setTransformPermissionChecker(TransformPermissionChecker checker) {
        permissionChecker = checker;
    }


    protected static void checkPermission(Object from, Object to) {
        if(permissionChecker !=null) {
            if( !permissionChecker.hasPermission(from, to)) {
                throw new PermissionDeniedDataAccessException(from.getClass().getName()+ " access error" ,new RuntimeException());
            }
        }
    }

    
    public static void mapping(ApiLog from, ApiLogDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(ApiLog from, ApiLogDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("relatedOrderStatus").checkEnable())
        setVal(from.getRelatedOrderStatus(), to::setRelatedOrderStatus, isSkipNull);
        if(c==null || c.fd("logId").checkEnable())
        setVal(from.getLogId(), to::setLogId, isSkipNull);
        if(c==null || c.fd("endpoint").checkEnable())
        setVal(from.getEndpoint(), to::setEndpoint, isSkipNull);
        if(c==null || c.fd("method").checkEnable())
        setVal(from.getMethod(), to::setMethod, isSkipNull);
        if(c==null || c.fd("requestBody").checkEnable())
        setVal(from.getRequestBody(), to::setRequestBody, isSkipNull);
        if(c==null || c.fd("responseBody").checkEnable())
        setVal(from.getResponseBody(), to::setResponseBody, isSkipNull);
        if(c==null || c.fd("statusCode").checkEnable())
        setVal(from.getStatusCode(), to::setStatusCode, isSkipNull);
        if(c==null || c.fd("duration").checkEnable())
        setVal(from.getDuration(), to::setDuration, isSkipNull);
        if(c==null || c.fd("createdAt").checkEnable())
        setVal(from.getCreatedAt(), to::setCreatedAt, isSkipNull);
        if(isSkip("ApiLog","AuditableWithEnum")) return;
        if(c==null || c.fd("inheritedPaymentMethod").checkEnable())
        setVal(from.getInheritedPaymentMethod(), to::setInheritedPaymentMethod, isSkipNull);
        if(isSkip("ApiLog","AuditEntity")) return;
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
    }
    
    
    public static void mapping(ApiLog from, ApiLogDTO to){
        mapping(from,to,false);
    }
    public static void mapping(ApiLog from, ApiLogDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static ApiLogDTO toDTO(ApiLog from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static ApiLogDTO toDTO(ApiLog from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        ApiLogDTO to = new ApiLogDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<ApiLogDTO> toApiLogDTOList(List<ApiLog> fromList){
        return toApiLogDTOList(fromList,(MappingContext) null);
    }
    public static List<ApiLogDTO> toApiLogDTOList(List<ApiLog> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->IntegrationMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<ApiLogDTO> toApiLogDTOList(List<ApiLog> fromList, BiFunction<ApiLog, ApiLogDTO, ApiLogDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                ApiLogDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(SyncRecord from, SyncRecordDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(SyncRecord from, SyncRecordDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("syncStatus").checkEnable())
        setVal(from.getSyncStatus(), to::setSyncStatus, isSkipNull);
        if(c==null || c.fd("recordId").checkEnable())
        setVal(from.getRecordId(), to::setRecordId, isSkipNull);
        if(c==null || c.fd("sourceSystem").checkEnable())
        setVal(from.getSourceSystem(), to::setSourceSystem, isSkipNull);
        if(c==null || c.fd("targetSystem").checkEnable())
        setVal(from.getTargetSystem(), to::setTargetSystem, isSkipNull);
        if(c==null || c.fd("entityType").checkEnable())
        setVal(from.getEntityType(), to::setEntityType, isSkipNull);
        if(c==null || c.fd("entityId").checkEnable())
        setVal(from.getEntityId(), to::setEntityId, isSkipNull);
        if(c==null || c.fd("syncDate").checkEnable())
        setVal(from.getSyncDate(), to::setSyncDate, isSkipNull);
        if(c==null || c.fd("errorMessage").checkEnable())
        setVal(from.getErrorMessage(), to::setErrorMessage, isSkipNull);
        if(c==null || c.fd("retryCount").checkEnable())
        setVal(from.getRetryCount(), to::setRetryCount, isSkipNull);
    }
    
    
    public static void mapping(SyncRecord from, SyncRecordDTO to){
        mapping(from,to,false);
    }
    public static void mapping(SyncRecord from, SyncRecordDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static SyncRecordDTO toDTO(SyncRecord from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static SyncRecordDTO toDTO(SyncRecord from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        SyncRecordDTO to = new SyncRecordDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<SyncRecordDTO> toSyncRecordDTOList(List<SyncRecord> fromList){
        return toSyncRecordDTOList(fromList,(MappingContext) null);
    }
    public static List<SyncRecordDTO> toSyncRecordDTOList(List<SyncRecord> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->IntegrationMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<SyncRecordDTO> toSyncRecordDTOList(List<SyncRecord> fromList, BiFunction<SyncRecord, SyncRecordDTO, SyncRecordDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                SyncRecordDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(ApiLogDTO from, ApiLog to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(ApiLogDTO from, ApiLog to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("logId").checkEnable())
        setVal(from.getLogId(), to::setLogId, isSkipNull);
        if(c==null || c.fd("endpoint").checkEnable())
        setVal(from.getEndpoint(), to::setEndpoint, isSkipNull);
        if(c==null || c.fd("method").checkEnable())
        setVal(from.getMethod(), to::setMethod, isSkipNull);
        if(c==null || c.fd("requestBody").checkEnable())
        setVal(from.getRequestBody(), to::setRequestBody, isSkipNull);
        if(c==null || c.fd("responseBody").checkEnable())
        setVal(from.getResponseBody(), to::setResponseBody, isSkipNull);
        if(c==null || c.fd("statusCode").checkEnable())
        setVal(from.getStatusCode(), to::setStatusCode, isSkipNull);
        if(c==null || c.fd("duration").checkEnable())
        setVal(from.getDuration(), to::setDuration, isSkipNull);
        if(c==null || c.fd("createdAt").checkEnable())
        setVal(from.getCreatedAt(), to::setCreatedAt, isSkipNull);
        if(c==null || c.fd("relatedOrderStatus").checkEnable())
        setVal(from.getRelatedOrderStatus(), to::setRelatedOrderStatus, isSkipNull);
        if(c==null || c.fd("inheritedPaymentMethod").checkEnable())
        setVal(from.getInheritedPaymentMethod(), to::setInheritedPaymentMethod, isSkipNull);
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
    }
    
    
    public static void mapping(ApiLogDTO from, ApiLog to){
        mapping(from,to,false);
    }
    public static void mapping(ApiLogDTO from, ApiLog to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(ApiLogDTO from, ApiLog to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(ApiLogDTO from, ApiLog to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(ApiLogDTO from, ApiLog to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(ApiLogDTO from, ApiLog to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("logId").checkEnable())
        setVal(from.getLogId(), to::setLogId, isSkipNull);
        if(c==null || c.fd("endpoint").checkEnable())
        setVal(from.getEndpoint(), to::setEndpoint, isSkipNull);
        if(c==null || c.fd("method").checkEnable())
        setVal(from.getMethod(), to::setMethod, isSkipNull);
        if(c==null || c.fd("requestBody").checkEnable())
        setVal(from.getRequestBody(), to::setRequestBody, isSkipNull);
        if(c==null || c.fd("responseBody").checkEnable())
        setVal(from.getResponseBody(), to::setResponseBody, isSkipNull);
        if(c==null || c.fd("statusCode").checkEnable())
        setVal(from.getStatusCode(), to::setStatusCode, isSkipNull);
        if(c==null || c.fd("duration").checkEnable())
        setVal(from.getDuration(), to::setDuration, isSkipNull);
        if(c==null || c.fd("createdAt").checkEnable())
        setVal(from.getCreatedAt(), to::setCreatedAt, isSkipNull);
        if(c==null || c.fd("relatedOrderStatus").checkEnable())
        setVal(from.getRelatedOrderStatus(), to::setRelatedOrderStatus, isSkipNull);
        if(c==null || c.fd("inheritedPaymentMethod").checkEnable())
        setVal(from.getInheritedPaymentMethod(), to::setInheritedPaymentMethod, isSkipNull);
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
    }
    
    
    public static ApiLog toEntity(ApiLogDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static ApiLog toEntity(ApiLogDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        ApiLog to = new ApiLog();
        mapping(from,to,c);
        return to;
    }
    
    
    public static ApiLog toEntity(ApiLogDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static ApiLog toEntity(ApiLogDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        ApiLog to = new ApiLog();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<ApiLog> toApiLogList(List<ApiLogDTO> fromList){
        return toApiLogList(fromList,(MappingContext) null);
    }
    public static List<ApiLog> toApiLogList(List<ApiLogDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->IntegrationMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<ApiLog> toApiLogList(List<ApiLogDTO> fromList, EntityManager em){
        return toApiLogList(fromList,(MappingContext) null,em);
    }
    public static List<ApiLog> toApiLogList(List<ApiLogDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->IntegrationMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<ApiLog> toApiLogList(List<ApiLogDTO> fromList, BiFunction<ApiLogDTO, ApiLog, ApiLog> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                ApiLog to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(SyncRecordDTO from, SyncRecord to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(SyncRecordDTO from, SyncRecord to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("recordId").checkEnable())
        setVal(from.getRecordId(), to::setRecordId, isSkipNull);
        if(c==null || c.fd("sourceSystem").checkEnable())
        setVal(from.getSourceSystem(), to::setSourceSystem, isSkipNull);
        if(c==null || c.fd("targetSystem").checkEnable())
        setVal(from.getTargetSystem(), to::setTargetSystem, isSkipNull);
        if(c==null || c.fd("entityType").checkEnable())
        setVal(from.getEntityType(), to::setEntityType, isSkipNull);
        if(c==null || c.fd("entityId").checkEnable())
        setVal(from.getEntityId(), to::setEntityId, isSkipNull);
        if(c==null || c.fd("syncStatus").checkEnable())
        setVal(from.getSyncStatus(), to::setSyncStatus, isSkipNull);
        if(c==null || c.fd("syncDate").checkEnable())
        setVal(from.getSyncDate(), to::setSyncDate, isSkipNull);
        if(c==null || c.fd("errorMessage").checkEnable())
        setVal(from.getErrorMessage(), to::setErrorMessage, isSkipNull);
        if(c==null || c.fd("retryCount").checkEnable())
        setVal(from.getRetryCount(), to::setRetryCount, isSkipNull);
    }
    
    
    public static void mapping(SyncRecordDTO from, SyncRecord to){
        mapping(from,to,false);
    }
    public static void mapping(SyncRecordDTO from, SyncRecord to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(SyncRecordDTO from, SyncRecord to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(SyncRecordDTO from, SyncRecord to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(SyncRecordDTO from, SyncRecord to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(SyncRecordDTO from, SyncRecord to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("recordId").checkEnable())
        setVal(from.getRecordId(), to::setRecordId, isSkipNull);
        if(c==null || c.fd("sourceSystem").checkEnable())
        setVal(from.getSourceSystem(), to::setSourceSystem, isSkipNull);
        if(c==null || c.fd("targetSystem").checkEnable())
        setVal(from.getTargetSystem(), to::setTargetSystem, isSkipNull);
        if(c==null || c.fd("entityType").checkEnable())
        setVal(from.getEntityType(), to::setEntityType, isSkipNull);
        if(c==null || c.fd("entityId").checkEnable())
        setVal(from.getEntityId(), to::setEntityId, isSkipNull);
        if(c==null || c.fd("syncStatus").checkEnable())
        setVal(from.getSyncStatus(), to::setSyncStatus, isSkipNull);
        if(c==null || c.fd("syncDate").checkEnable())
        setVal(from.getSyncDate(), to::setSyncDate, isSkipNull);
        if(c==null || c.fd("errorMessage").checkEnable())
        setVal(from.getErrorMessage(), to::setErrorMessage, isSkipNull);
        if(c==null || c.fd("retryCount").checkEnable())
        setVal(from.getRetryCount(), to::setRetryCount, isSkipNull);
    }
    
    
    public static SyncRecord toEntity(SyncRecordDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static SyncRecord toEntity(SyncRecordDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        SyncRecord to = new SyncRecord();
        mapping(from,to,c);
        return to;
    }
    
    
    public static SyncRecord toEntity(SyncRecordDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static SyncRecord toEntity(SyncRecordDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        SyncRecord to = new SyncRecord();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<SyncRecord> toSyncRecordList(List<SyncRecordDTO> fromList){
        return toSyncRecordList(fromList,(MappingContext) null);
    }
    public static List<SyncRecord> toSyncRecordList(List<SyncRecordDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->IntegrationMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<SyncRecord> toSyncRecordList(List<SyncRecordDTO> fromList, EntityManager em){
        return toSyncRecordList(fromList,(MappingContext) null,em);
    }
    public static List<SyncRecord> toSyncRecordList(List<SyncRecordDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->IntegrationMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<SyncRecord> toSyncRecordList(List<SyncRecordDTO> fromList, BiFunction<SyncRecordDTO, SyncRecord, SyncRecord> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                SyncRecord to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static Specification<ApiLog> toSpec(ApiLogSrchDTO searchDTO){
        Specification<ApiLog> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<ApiLog> toSpec(ApiLogSrchDTO searchDTO, Specification<ApiLog> sp){
        OrderStatus relatedOrderStatus = searchDTO.getRelatedOrderStatus();
        if(hasValue(relatedOrderStatus)){
            sp = sp.and((r,q,c) -> c.equal(r.get("relatedOrderStatus"),relatedOrderStatus));
        }
        Long logId = searchDTO.getLogId();
        if(hasValue(logId)){
            sp = sp.and((r,q,c) -> c.equal(r.get("logId"),logId));
        }
        String endpoint = searchDTO.getEndpoint();
        if(hasValue(endpoint)){
            sp = sp.and((r,q,c) -> c.like(r.get("endpoint"),"%" +trim(endpoint)+ "%"));
        }
        sp=setSpec(sp, "method", searchDTO.getMethod());
        sp=setSpec(sp, "requestBody", searchDTO.getRequestBody());
        sp=setSpec(sp, "responseBody", searchDTO.getResponseBody());
        Integer statusCode = searchDTO.getStatusCode();
        if(hasValue(statusCode)){
            sp = sp.and((r,q,c) -> c.equal(r.get("statusCode"),statusCode));
        }
        Long duration = searchDTO.getDuration();
        if(hasValue(duration)){
            sp = sp.and((r,q,c) -> c.equal(r.get("duration"),duration));
        }
        java.time.LocalDateTime createdAtFrom = searchDTO.getCreatedAtFrom();
        java.time.LocalDateTime createdAtTo = searchDTO.getCreatedAtTo();
        if(hasValue(createdAtFrom) && hasValue(createdAtTo)){
            sp = sp.and((r,q,c) -> c.between(r.get("createdAt"),createdAtFrom,createdAtTo));
        }
        else if(hasValue(createdAtFrom)){
            sp = sp.and((r,q,c) -> c.greaterThanOrEqualTo(r.get("createdAt"),createdAtFrom));
        }
        else if(hasValue(createdAtTo)){
            sp = sp.and((r,q,c) -> c.lessThanOrEqualTo(r.get("createdAt"),createdAtTo));
        }
        PaymentMethod inheritedPaymentMethod = searchDTO.getInheritedPaymentMethod();
        if(hasValue(inheritedPaymentMethod)){
            sp = sp.and((r,q,c) -> c.equal(r.get("inheritedPaymentMethod"),inheritedPaymentMethod));
        }
        sp=setSpec(sp, "createdBy", searchDTO.getCreatedBy());
        java.time.LocalDateTime createDateFrom = searchDTO.getCreateDateFrom();
        java.time.LocalDateTime createDateTo = searchDTO.getCreateDateTo();
        if(hasValue(createDateFrom) && hasValue(createDateTo)){
            sp = sp.and((r,q,c) -> c.between(r.get("createDate"),createDateFrom,createDateTo));
        }
        else if(hasValue(createDateFrom)){
            sp = sp.and((r,q,c) -> c.greaterThanOrEqualTo(r.get("createDate"),createDateFrom));
        }
        else if(hasValue(createDateTo)){
            sp = sp.and((r,q,c) -> c.lessThanOrEqualTo(r.get("createDate"),createDateTo));
        }
        sp=setSpec(sp, "lastModifiedBy", searchDTO.getLastModifiedBy());
        java.time.LocalDateTime lastModifiedDateFrom = searchDTO.getLastModifiedDateFrom();
        java.time.LocalDateTime lastModifiedDateTo = searchDTO.getLastModifiedDateTo();
        if(hasValue(lastModifiedDateFrom) && hasValue(lastModifiedDateTo)){
            sp = sp.and((r,q,c) -> c.between(r.get("lastModifiedDate"),lastModifiedDateFrom,lastModifiedDateTo));
        }
        else if(hasValue(lastModifiedDateFrom)){
            sp = sp.and((r,q,c) -> c.greaterThanOrEqualTo(r.get("lastModifiedDate"),lastModifiedDateFrom));
        }
        else if(hasValue(lastModifiedDateTo)){
            sp = sp.and((r,q,c) -> c.lessThanOrEqualTo(r.get("lastModifiedDate"),lastModifiedDateTo));
        }
        return sp;
    }
    
    
    public static Specification<SyncRecord> toSpec(SyncRecordSrchDTO searchDTO){
        Specification<SyncRecord> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<SyncRecord> toSpec(SyncRecordSrchDTO searchDTO, Specification<SyncRecord> sp){
        SyncStatus syncStatus = searchDTO.getSyncStatus();
        if(hasValue(syncStatus)){
            sp = sp.and((r, q, c) -> r.get("syncStatus").in(syncStatus));
        }
        sp=setSpec(sp, "recordId", searchDTO.getRecordId());
        String sourceSystem = searchDTO.getSourceSystem();
        if(hasValue(sourceSystem)){
            sp = sp.and((r,q,c) -> c.equal(r.get("sourceSystem"),sourceSystem));
        }
        String targetSystem = searchDTO.getTargetSystem();
        if(hasValue(targetSystem)){
            sp = sp.and((r,q,c) -> c.equal(r.get("targetSystem"),targetSystem));
        }
        String entityType = searchDTO.getEntityType();
        if(hasValue(entityType)){
            sp = sp.and((r,q,c) -> c.like(r.get("entityType"),"%" +trim(entityType)+ "%"));
        }
        sp=setSpec(sp, "entityId", searchDTO.getEntityId());
        java.time.LocalDateTime syncDateFrom = searchDTO.getSyncDateFrom();
        java.time.LocalDateTime syncDateTo = searchDTO.getSyncDateTo();
        if(hasValue(syncDateFrom) && hasValue(syncDateTo)){
            sp = sp.and((r,q,c) -> c.between(r.get("syncDate"),syncDateFrom,syncDateTo));
        }
        else if(hasValue(syncDateFrom)){
            sp = sp.and((r,q,c) -> c.greaterThanOrEqualTo(r.get("syncDate"),syncDateFrom));
        }
        else if(hasValue(syncDateTo)){
            sp = sp.and((r,q,c) -> c.lessThanOrEqualTo(r.get("syncDate"),syncDateTo));
        }
        sp=setSpec(sp, "errorMessage", searchDTO.getErrorMessage());
        Integer retryCount = searchDTO.getRetryCount();
        if(hasValue(retryCount)){
            sp = sp.and((r,q,c) -> c.equal(r.get("retryCount"),retryCount));
        }
        return sp;
    }
    

    protected static Specification setSpec(Specification sp, String fieldName, String value) {
        if(hasValue(value)){
            if(value.startsWith("%") || value.endsWith("%"))
                return sp.and((r,q,c) -> c.like(r.get(fieldName),value));
            else
                return sp.and((r,q,c) -> c.equal(r.get(fieldName),value));
        }
        return sp;
    }

    private static boolean hasValue(String val) {
        if(val != null && !val.isBlank())
            return true;

        return false;
    }

    private static boolean hasValue(java.time.LocalDateTime dateTime) {
        if(dateTime !=null)
            return true;

        return false;
    }

    private static boolean hasValue(List val) {
            if(val != null && val.size()>0)
                return true;

            return false;
    }

    private static boolean hasValue(Object val) {
        if(val != null )
            return true;

        return false;
    }

    private static String trim(String val) {
        if(val != null )
            return val.trim();

        return val;
    }

    private static Object trim(Object val) {
        return val;
    }

    private static boolean isSkip(String fromClassName, String refClassName) {
        return false;
    }

    public static void setVal(String value, Consumer<String> setter, boolean isSkipNull) {
        if ((value != null && !value.isEmpty())  || !isSkipNull) {
            Optional.ofNullable(value).ifPresent(setter);
        }
    }

    public static <T> void setVal(T value, Consumer<T> setter, boolean isSkipNull) {
        if (value != null || !isSkipNull) {
            Optional.ofNullable(value).ifPresent(setter);
        }
    }


    public static <T, R> void setVal(T value, Consumer<R> setter, boolean isSkipNull, Function<T, R> mapper) {


        if (value != null || !isSkipNull) {
            Optional.ofNullable(value).map(mapper).ifPresent(setter);
        }
    }
}
