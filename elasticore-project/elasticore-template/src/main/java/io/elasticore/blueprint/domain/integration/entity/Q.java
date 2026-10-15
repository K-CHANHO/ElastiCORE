//ecd:284370095H20260414094512_V2.1
package io.elasticore.blueprint.domain.integration.entity;


import io.elasticore.springboot3.entity.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;
import io.elasticore.blueprint.domain.integration.enums.SyncStatus;
import io.elasticore.blueprint.domain.shop.enums.PaymentMethod;
import io.elasticore.blueprint.domain.shop.enums.OrderStatus;


/**
 * This class serves as a domain-specific unified metadata holder,
 * containing field names and type information for entities.
 *
 * Generated and managed by ElastiCORE.
 */

public class Q {

	public static $ApiLog<ApiLog> ApiLog=new $ApiLog<>();
	public static class $ApiLog<T> {
	
	    private FieldInfo p=null;
	    public $ApiLog() {}
	    public $ApiLog(FieldInfo p) {this.p=p;}
	    public static JoinSpec<ApiLog> joinSpec() {
	    	return new JoinSpec<>(ApiLog.class);
	    }
	    public static UpdateSpec<ApiLog> updateSpec() {
	    	return new UpdateSpec<>(ApiLog.class);
	    }
	    public static DeleteSpec<ApiLog> deleteSpec() {
	    	return new DeleteSpec<>(ApiLog.class);
	    }
	
	    public final FieldInfo<T> getLogId() {return new FieldInfo(ApiLog.class,"logId",Long.class, null,p);}
	    public final String logId="logId";
	    public Specification<T> logId(Op op,Object value) {return getLogId().where(op,value);}
	    public Specification<T> logId(Op op,Object value,boolean chkEmpty) {return getLogId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEndpoint() {return new FieldInfo(ApiLog.class,"endpoint",String.class, null,p);}
	    public final String endpoint="endpoint";
	    public Specification<T> endpoint(Op op,Object value) {return getEndpoint().where(op,value);}
	    public Specification<T> endpoint(Op op,Object value,boolean chkEmpty) {return getEndpoint().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getMethod() {return new FieldInfo(ApiLog.class,"method",String.class, null,p);}
	    public final String method="method";
	    public Specification<T> method(Op op,Object value) {return getMethod().where(op,value);}
	    public Specification<T> method(Op op,Object value,boolean chkEmpty) {return getMethod().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getRequestBody() {return new FieldInfo(ApiLog.class,"requestBody",String.class, null,p);}
	    public final String requestBody="requestBody";
	    public Specification<T> requestBody(Op op,Object value) {return getRequestBody().where(op,value);}
	    public Specification<T> requestBody(Op op,Object value,boolean chkEmpty) {return getRequestBody().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getResponseBody() {return new FieldInfo(ApiLog.class,"responseBody",String.class, null,p);}
	    public final String responseBody="responseBody";
	    public Specification<T> responseBody(Op op,Object value) {return getResponseBody().where(op,value);}
	    public Specification<T> responseBody(Op op,Object value,boolean chkEmpty) {return getResponseBody().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getStatusCode() {return new FieldInfo(ApiLog.class,"statusCode",Integer.class, null,p);}
	    public final String statusCode="statusCode";
	    public Specification<T> statusCode(Op op,Object value) {return getStatusCode().where(op,value);}
	    public Specification<T> statusCode(Op op,Object value,boolean chkEmpty) {return getStatusCode().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getDuration() {return new FieldInfo(ApiLog.class,"duration",Long.class, null,p);}
	    public final String duration="duration";
	    public Specification<T> duration(Op op,Object value) {return getDuration().where(op,value);}
	    public Specification<T> duration(Op op,Object value,boolean chkEmpty) {return getDuration().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreatedAt() {return new FieldInfo(ApiLog.class,"createdAt",java.time.LocalDateTime.class, null,p);}
	    public final String createdAt="createdAt";
	    public Specification<T> createdAt(Op op,Object value) {return getCreatedAt().where(op,value);}
	    public Specification<T> createdAt(Op op,Object value,boolean chkEmpty) {return getCreatedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getRelatedOrderStatus() {return new FieldInfo(ApiLog.class,"relatedOrderStatus",OrderStatus.class, null,p);}
	    public final String relatedOrderStatus="relatedOrderStatus";
	    public Specification<T> relatedOrderStatus(Op op,Object value) {return getRelatedOrderStatus().where(op,value);}
	    public Specification<T> relatedOrderStatus(Op op,Object value,boolean chkEmpty) {return getRelatedOrderStatus().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getInheritedPaymentMethod() {return new FieldInfo(ApiLog.class,"inheritedPaymentMethod",PaymentMethod.class, null,p);}
	    public final String inheritedPaymentMethod="inheritedPaymentMethod";
	    public Specification<T> inheritedPaymentMethod(Op op,Object value) {return getInheritedPaymentMethod().where(op,value);}
	    public Specification<T> inheritedPaymentMethod(Op op,Object value,boolean chkEmpty) {return getInheritedPaymentMethod().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreatedBy() {return new FieldInfo(ApiLog.class,"createdBy",String.class, null,p);}
	    public final String createdBy="createdBy";
	    public Specification<T> createdBy(Op op,Object value) {return getCreatedBy().where(op,value);}
	    public Specification<T> createdBy(Op op,Object value,boolean chkEmpty) {return getCreatedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreateDate() {return new FieldInfo(ApiLog.class,"createDate",java.time.LocalDateTime.class, null,p);}
	    public final String createDate="createDate";
	    public Specification<T> createDate(Op op,Object value) {return getCreateDate().where(op,value);}
	    public Specification<T> createDate(Op op,Object value,boolean chkEmpty) {return getCreateDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedBy() {return new FieldInfo(ApiLog.class,"lastModifiedBy",String.class, null,p);}
	    public final String lastModifiedBy="lastModifiedBy";
	    public Specification<T> lastModifiedBy(Op op,Object value) {return getLastModifiedBy().where(op,value);}
	    public Specification<T> lastModifiedBy(Op op,Object value,boolean chkEmpty) {return getLastModifiedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedDate() {return new FieldInfo(ApiLog.class,"lastModifiedDate",java.time.LocalDateTime.class, null,p);}
	    public final String lastModifiedDate="lastModifiedDate";
	    public Specification<T> lastModifiedDate(Op op,Object value) {return getLastModifiedDate().where(op,value);}
	    public Specification<T> lastModifiedDate(Op op,Object value,boolean chkEmpty) {return getLastModifiedDate().where(op,value,chkEmpty);}
	}
	
	public static $SyncRecord<SyncRecord> SyncRecord=new $SyncRecord<>();
	public static class $SyncRecord<T> {
	
	    private FieldInfo p=null;
	    public $SyncRecord() {}
	    public $SyncRecord(FieldInfo p) {this.p=p;}
	    public static JoinSpec<SyncRecord> joinSpec() {
	    	return new JoinSpec<>(SyncRecord.class);
	    }
	    public static UpdateSpec<SyncRecord> updateSpec() {
	    	return new UpdateSpec<>(SyncRecord.class);
	    }
	    public static DeleteSpec<SyncRecord> deleteSpec() {
	    	return new DeleteSpec<>(SyncRecord.class);
	    }
	
	    public final FieldInfo<T> getRecordId() {return new FieldInfo(SyncRecord.class,"recordId",String.class, null,p);}
	    public final String recordId="recordId";
	    public Specification<T> recordId(Op op,Object value) {return getRecordId().where(op,value);}
	    public Specification<T> recordId(Op op,Object value,boolean chkEmpty) {return getRecordId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSourceSystem() {return new FieldInfo(SyncRecord.class,"sourceSystem",String.class, null,p);}
	    public final String sourceSystem="sourceSystem";
	    public Specification<T> sourceSystem(Op op,Object value) {return getSourceSystem().where(op,value);}
	    public Specification<T> sourceSystem(Op op,Object value,boolean chkEmpty) {return getSourceSystem().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTargetSystem() {return new FieldInfo(SyncRecord.class,"targetSystem",String.class, null,p);}
	    public final String targetSystem="targetSystem";
	    public Specification<T> targetSystem(Op op,Object value) {return getTargetSystem().where(op,value);}
	    public Specification<T> targetSystem(Op op,Object value,boolean chkEmpty) {return getTargetSystem().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEntityType() {return new FieldInfo(SyncRecord.class,"entityType",String.class, null,p);}
	    public final String entityType="entityType";
	    public Specification<T> entityType(Op op,Object value) {return getEntityType().where(op,value);}
	    public Specification<T> entityType(Op op,Object value,boolean chkEmpty) {return getEntityType().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEntityId() {return new FieldInfo(SyncRecord.class,"entityId",String.class, null,p);}
	    public final String entityId="entityId";
	    public Specification<T> entityId(Op op,Object value) {return getEntityId().where(op,value);}
	    public Specification<T> entityId(Op op,Object value,boolean chkEmpty) {return getEntityId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSyncStatus() {return new FieldInfo(SyncRecord.class,"syncStatus",SyncStatus.class, null,p);}
	    public final String syncStatus="syncStatus";
	    public Specification<T> syncStatus(Op op,Object value) {return getSyncStatus().where(op,value);}
	    public Specification<T> syncStatus(Op op,Object value,boolean chkEmpty) {return getSyncStatus().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSyncDate() {return new FieldInfo(SyncRecord.class,"syncDate",java.time.LocalDateTime.class, null,p);}
	    public final String syncDate="syncDate";
	    public Specification<T> syncDate(Op op,Object value) {return getSyncDate().where(op,value);}
	    public Specification<T> syncDate(Op op,Object value,boolean chkEmpty) {return getSyncDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getErrorMessage() {return new FieldInfo(SyncRecord.class,"errorMessage",String.class, null,p);}
	    public final String errorMessage="errorMessage";
	    public Specification<T> errorMessage(Op op,Object value) {return getErrorMessage().where(op,value);}
	    public Specification<T> errorMessage(Op op,Object value,boolean chkEmpty) {return getErrorMessage().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getRetryCount() {return new FieldInfo(SyncRecord.class,"retryCount",Integer.class, null,p);}
	    public final String retryCount="retryCount";
	    public Specification<T> retryCount(Op op,Object value) {return getRetryCount().where(op,value);}
	    public Specification<T> retryCount(Op op,Object value,boolean chkEmpty) {return getRetryCount().where(op,value,chkEmpty);}
	}
	
}
