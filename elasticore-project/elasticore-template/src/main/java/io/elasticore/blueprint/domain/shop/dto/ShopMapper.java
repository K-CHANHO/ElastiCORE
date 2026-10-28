//ecd:392570855H20261028193835_V3.0
package io.elasticore.blueprint.domain.shop.dto;

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
import io.elasticore.blueprint.domain.shop.entity.*;
import io.elasticore.blueprint.domain.shop.dto.*;
import io.elasticore.blueprint.domain.shop.enums.*;

import io.elasticore.blueprint.domain.shop.enums.*;
import io.elasticore.blueprint.domain.shop.dto.*;
import io.elasticore.blueprint.domain.shop.entity.*;
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


public class ShopMapper {

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

    
    public static void mapping(Ledger from, LedgerDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(Ledger from, LedgerDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("owner").checkEnable())
        setVal(from.getOwner(), to::setOwner, isSkipNull, e->toDTO(e,c));
        if(c==null || c.fd("partner").checkEnable())
        setVal(from.getPartner(), to::setPartner, isSkipNull, e->toDTO(e,c));
        if(hasValue(from.getOwner()))
            to.setOwnerCustomerId(from.getOwner().getCustomerId());
        if(hasValue(from.getPartner()))
            to.setPartnerCustomerId(from.getPartner().getCustomerId());
        if(c==null || c.fd("ledgerId").checkEnable())
        setVal(from.getLedgerId(), to::setLedgerId, isSkipNull);
        if(c==null || c.fd("code").checkEnable())
        setVal(from.getCode(), to::setCode, isSkipNull);
        if(c==null || c.fd("memo").checkEnable())
        setVal(from.getMemo(), to::setMemo, isSkipNull);
        if(c==null || c.fd("amount").checkEnable())
        setVal(from.getAmount(), to::setAmount, isSkipNull);
        if(c==null || c.fd("postedAt").checkEnable())
        setVal(from.getPostedAt(), to::setPostedAt, isSkipNull);
        if(c==null || c.fd("deleted").checkEnable())
        setVal(from.getDeleted(), to::setDeleted, isSkipNull);
    }
    
    
    public static void mapping(Ledger from, LedgerDTO to){
        mapping(from,to,false);
    }
    public static void mapping(Ledger from, LedgerDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static LedgerDTO toDTO(Ledger from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static LedgerDTO toDTO(Ledger from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        LedgerDTO to = new LedgerDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<LedgerDTO> toLedgerDTOList(List<Ledger> fromList){
        return toLedgerDTOList(fromList,(MappingContext) null);
    }
    public static List<LedgerDTO> toLedgerDTOList(List<Ledger> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<LedgerDTO> toLedgerDTOList(List<Ledger> fromList, BiFunction<Ledger, LedgerDTO, LedgerDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                LedgerDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(Category from, CategoryDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(Category from, CategoryDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("subCategories").checkEnable())
        setVal(from.getSubCategories(), to::setSubCategories, isSkipNull, e->toCategoryDTOList(e,c));
        if(c==null || c.fd("catId").checkEnable())
        setVal(from.getCatId(), to::setCatId, isSkipNull);
        if(c==null || c.fd("name").checkEnable())
        setVal(from.getName(), to::setName, isSkipNull);
        if(c==null || c.fd("description").checkEnable())
        setVal(from.getDescription(), to::setDescription, isSkipNull);
        if(c==null || c.fd("sortOrder").checkEnable())
        setVal(from.getSortOrder(), to::setSortOrder, isSkipNull);
        if(c==null || c.fd("active").checkEnable())
        setVal(from.getActive(), to::setActive, isSkipNull);
        if(isSkip("Category","AuditEntity")) return;
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
    }
    
    
    public static void mapping(Category from, CategoryDTO to){
        mapping(from,to,false);
    }
    public static void mapping(Category from, CategoryDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static CategoryDTO toDTO(Category from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static CategoryDTO toDTO(Category from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        CategoryDTO to = new CategoryDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<CategoryDTO> toCategoryDTOList(List<Category> fromList){
        return toCategoryDTOList(fromList,(MappingContext) null);
    }
    public static List<CategoryDTO> toCategoryDTOList(List<Category> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<CategoryDTO> toCategoryDTOList(List<Category> fromList, BiFunction<Category, CategoryDTO, CategoryDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                CategoryDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(Product from, ProductDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(Product from, ProductDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(hasValue(from.getCategory()))
            to.setCategoryName(from.getCategory().getName());
        if(hasValue(from.getCategory()))
            to.setCategoryCatId(from.getCategory().getCatId());
        if(c==null || c.fd("productId").checkEnable())
        setVal(from.getProductId(), to::setProductId, isSkipNull);
        if(c==null || c.fd("name").checkEnable())
        setVal(from.getName(), to::setName, isSkipNull);
        if(c==null || c.fd("sku").checkEnable())
        setVal(from.getSku(), to::setSku, isSkipNull);
        if(c==null || c.fd("description").checkEnable())
        setVal(from.getDescription(), to::setDescription, isSkipNull);
        if(c==null || c.fd("price").checkEnable())
        setVal(from.getPrice(), to::setPrice, isSkipNull);
        if(c==null || c.fd("costPrice").checkEnable())
        setVal(from.getCostPrice(), to::setCostPrice, isSkipNull);
        if(c==null || c.fd("stockQuantity").checkEnable())
        setVal(from.getStockQuantity(), to::setStockQuantity, isSkipNull);
        if(c==null || c.fd("weight").checkEnable())
        setVal(from.getWeight(), to::setWeight, isSkipNull);
        if(c==null || c.fd("rating").checkEnable())
        setVal(from.getRating(), to::setRating, isSkipNull);
        if(c==null || c.fd("active").checkEnable())
        setVal(from.getActive(), to::setActive, isSkipNull);
        if(c==null || c.fd("launchDate").checkEnable())
        setVal(from.getLaunchDate(), to::setLaunchDate, isSkipNull);
        if(c==null || c.fd("lastRestocked").checkEnable())
        setVal(from.getLastRestocked(), to::setLastRestocked, isSkipNull);
        if(c==null || c.fd("uuid").checkEnable())
        setVal(from.getUuid(), to::setUuid, isSkipNull);
        if(c==null || c.fd("metadata").checkEnable())
        setVal(from.getMetadata(), to::setMetadata, isSkipNull);
        if(c==null || c.fd("metadataRaw").checkEnable())
        setVal(from.getMetadataRaw(), to::setMetadataRaw, isSkipNull);
        if(c==null || c.fd("oldPayload").checkEnable())
        setVal(from.getOldPayload(), to::setOldPayload, isSkipNull);
        if(c==null || c.fd("customXml").checkEnable())
        setVal(from.getCustomXml(), to::setCustomXml, isSkipNull);
        if(c==null || c.fd("forceLong").checkEnable())
        setVal(from.getForceLong(), to::setForceLong, isSkipNull);
        if(c==null || c.fd("tags").checkEnable())
        setVal(from.getTags(), to::setTags, isSkipNull);
        if(c==null || c.fd("category").checkEnable())
        setVal(from.getCategory(), to::setCategory, isSkipNull, e->toDTO(e,c));
        if(isSkip("Product","AuditEntity")) return;
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
    }
    
    
    public static void mapping(Product from, ProductDTO to){
        mapping(from,to,false);
    }
    public static void mapping(Product from, ProductDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static ProductDTO toDTO(Product from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static ProductDTO toDTO(Product from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        ProductDTO to = new ProductDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<ProductDTO> toProductDTOList(List<Product> fromList){
        return toProductDTOList(fromList,(MappingContext) null);
    }
    public static List<ProductDTO> toProductDTOList(List<Product> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<ProductDTO> toProductDTOList(List<Product> fromList, BiFunction<Product, ProductDTO, ProductDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                ProductDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(Product from, ProductInputDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(Product from, ProductInputDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("productId").checkEnable())
        setVal(from.getProductId(), to::setProductId, isSkipNull);
        if(c==null || c.fd("name").checkEnable())
        setVal(from.getName(), to::setName, isSkipNull);
        if(c==null || c.fd("sku").checkEnable())
        setVal(from.getSku(), to::setSku, isSkipNull);
        if(c==null || c.fd("description").checkEnable())
        setVal(from.getDescription(), to::setDescription, isSkipNull);
        if(c==null || c.fd("price").checkEnable())
        setVal(from.getPrice(), to::setPrice, isSkipNull);
        if(c==null || c.fd("costPrice").checkEnable())
        setVal(from.getCostPrice(), to::setCostPrice, isSkipNull);
        if(c==null || c.fd("stockQuantity").checkEnable())
        setVal(from.getStockQuantity(), to::setStockQuantity, isSkipNull);
        if(c==null || c.fd("weight").checkEnable())
        setVal(from.getWeight(), to::setWeight, isSkipNull);
        if(c==null || c.fd("rating").checkEnable())
        setVal(from.getRating(), to::setRating, isSkipNull);
        if(c==null || c.fd("active").checkEnable())
        setVal(from.getActive(), to::setActive, isSkipNull);
        if(c==null || c.fd("launchDate").checkEnable())
        setVal(from.getLaunchDate(), to::setLaunchDate, isSkipNull);
        if(c==null || c.fd("lastRestocked").checkEnable())
        setVal(from.getLastRestocked(), to::setLastRestocked, isSkipNull);
        if(c==null || c.fd("uuid").checkEnable())
        setVal(from.getUuid(), to::setUuid, isSkipNull);
        if(c==null || c.fd("metadata").checkEnable())
        setVal(from.getMetadata(), to::setMetadata, isSkipNull);
        if(c==null || c.fd("metadataRaw").checkEnable())
        setVal(from.getMetadataRaw(), to::setMetadataRaw, isSkipNull);
        if(c==null || c.fd("oldPayload").checkEnable())
        setVal(from.getOldPayload(), to::setOldPayload, isSkipNull);
        if(c==null || c.fd("customXml").checkEnable())
        setVal(from.getCustomXml(), to::setCustomXml, isSkipNull);
        if(c==null || c.fd("forceLong").checkEnable())
        setVal(from.getForceLong(), to::setForceLong, isSkipNull);
        if(c==null || c.fd("tags").checkEnable())
        setVal(from.getTags(), to::setTags, isSkipNull);
        if(c==null || c.fd("category").checkEnable())
        setVal(from.getCategory(), to::setCategory, isSkipNull, e->toDTO(e,c));
        if(isSkip("Product","AuditEntity")) return;
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
    }
    
    
    public static void mapping(Product from, ProductInputDTO to){
        mapping(from,to,false);
    }
    public static void mapping(Product from, ProductInputDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static ProductInputDTO toProductInputDTO(Product from){
        return toProductInputDTO(from,MappingContext.withGuard(2,null));
    }
    public static ProductInputDTO toProductInputDTO(Product from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        ProductInputDTO to = new ProductInputDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<ProductInputDTO> toProductInputDTOList(List<Product> fromList){
        return toProductInputDTOList(fromList,(MappingContext) null);
    }
    public static List<ProductInputDTO> toProductInputDTOList(List<Product> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toProductInputDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<ProductInputDTO> toProductInputDTOList(List<Product> fromList, BiFunction<Product, ProductInputDTO, ProductInputDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                ProductInputDTO to = toProductInputDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(Customer from, CustomerDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(Customer from, CustomerDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("customerId").checkEnable())
        setVal(from.getCustomerId(), to::setCustomerId, isSkipNull);
        if(c==null || c.fd("firstName").checkEnable())
        setVal(from.getFirstName(), to::setFirstName, isSkipNull);
        if(c==null || c.fd("lastName").checkEnable())
        setVal(from.getLastName(), to::setLastName, isSkipNull);
        if(c==null || c.fd("email").checkEnable())
        setVal(from.getEmail(), to::setEmail, isSkipNull);
        if(c==null || c.fd("phone").checkEnable())
        setVal(from.getPhone(), to::setPhone, isSkipNull);
        if(c==null || c.fd("dateOfBirth").checkEnable())
        setVal(from.getDateOfBirth(), to::setDateOfBirth, isSkipNull);
        if(c==null || c.fd("loyaltyPoints").checkEnable())
        setVal(from.getLoyaltyPoints(), to::setLoyaltyPoints, isSkipNull);
        if(c==null || c.fd("customerType").checkEnable())
        setVal(from.getCustomerType(), to::setCustomerType, isSkipNull);
        if(isSkip("Customer","AuditEntity")) return;
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
    }
    
    
    public static void mapping(Customer from, CustomerDTO to){
        mapping(from,to,false);
    }
    public static void mapping(Customer from, CustomerDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static CustomerDTO toDTO(Customer from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static CustomerDTO toDTO(Customer from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        CustomerDTO to = new CustomerDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<CustomerDTO> toCustomerDTOList(List<Customer> fromList){
        return toCustomerDTOList(fromList,(MappingContext) null);
    }
    public static List<CustomerDTO> toCustomerDTOList(List<Customer> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<CustomerDTO> toCustomerDTOList(List<Customer> fromList, BiFunction<Customer, CustomerDTO, CustomerDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                CustomerDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(VipProfile from, VipProfileDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(VipProfile from, VipProfileDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("customer").checkEnable())
        setVal(from.getCustomer(), to::setCustomer, isSkipNull, e->toDTO(e,c));
        if(hasValue(from.getCustomer()))
            to.setCustomerCustomerId(from.getCustomer().getCustomerId());
        if(c==null || c.fd("profileId").checkEnable())
        setVal(from.getProfileId(), to::setProfileId, isSkipNull);
        if(c==null || c.fd("tier").checkEnable())
        setVal(from.getTier(), to::setTier, isSkipNull);
        if(c==null || c.fd("discountRate").checkEnable())
        setVal(from.getDiscountRate(), to::setDiscountRate, isSkipNull);
        if(c==null || c.fd("enrollDate").checkEnable())
        setVal(from.getEnrollDate(), to::setEnrollDate, isSkipNull);
    }
    
    
    public static void mapping(VipProfile from, VipProfileDTO to){
        mapping(from,to,false);
    }
    public static void mapping(VipProfile from, VipProfileDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static VipProfileDTO toDTO(VipProfile from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static VipProfileDTO toDTO(VipProfile from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        VipProfileDTO to = new VipProfileDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<VipProfileDTO> toVipProfileDTOList(List<VipProfile> fromList){
        return toVipProfileDTOList(fromList,(MappingContext) null);
    }
    public static List<VipProfileDTO> toVipProfileDTOList(List<VipProfile> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<VipProfileDTO> toVipProfileDTOList(List<VipProfile> fromList, BiFunction<VipProfile, VipProfileDTO, VipProfileDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                VipProfileDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(Order from, OrderDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(Order from, OrderDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(hasValue(from.getCustomer()))
            to.setCustomerFirstName(from.getCustomer().getFirstName());
        if(hasValue(from.getCustomer()))
            to.setCustomerEmail(from.getCustomer().getEmail());
        if(c==null || c.fd("items").checkEnable())
        setVal(from.getItems(), to::setItems, isSkipNull, e->toOrderItemDTOList(e,c));
        if(c==null || c.fd("orderId").checkEnable())
        setVal(from.getOrderId(), to::setOrderId, isSkipNull);
        if(c==null || c.fd("orderDate").checkEnable())
        setVal(from.getOrderDate(), to::setOrderDate, isSkipNull);
        if(c==null || c.fd("totalAmount").checkEnable())
        setVal(from.getTotalAmount(), to::setTotalAmount, isSkipNull);
        if(c==null || c.fd("status").checkEnable())
        setVal(from.getStatus(), to::setStatus, isSkipNull);
        if(c==null || c.fd("paymentMethod").checkEnable())
        setVal(from.getPaymentMethod(), to::setPaymentMethod, isSkipNull);
        if(c==null || c.fd("notes").checkEnable())
        setVal(from.getNotes(), to::setNotes, isSkipNull);
        if(c==null || c.fd("trackingNumber").checkEnable())
        setVal(from.getTrackingNumber(), to::setTrackingNumber, isSkipNull);
        if(c==null || c.fd("version").checkEnable())
        setVal(from.getVersion(), to::setVersion, isSkipNull);
        if(isSkip("Order","AuditEntity")) return;
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
    }
    
    
    public static void mapping(Order from, OrderDTO to){
        mapping(from,to,false);
    }
    public static void mapping(Order from, OrderDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static OrderDTO toDTO(Order from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static OrderDTO toDTO(Order from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        OrderDTO to = new OrderDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<OrderDTO> toOrderDTOList(List<Order> fromList){
        return toOrderDTOList(fromList,(MappingContext) null);
    }
    public static List<OrderDTO> toOrderDTOList(List<Order> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<OrderDTO> toOrderDTOList(List<Order> fromList, BiFunction<Order, OrderDTO, OrderDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                OrderDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(OrderItem from, OrderItemDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(OrderItem from, OrderItemDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(hasValue(from.getProduct()))
            to.setProductName(from.getProduct().getName());
        if(hasValue(from.getProduct()))
            to.setProductSku(from.getProduct().getSku());
        if(c==null || c.fd("orderItemId").checkEnable())
        setVal(from.getOrderItemId(), to::setOrderItemId, isSkipNull);
        if(c==null || c.fd("itemSeq").checkEnable())
        setVal(from.getItemSeq(), to::setItemSeq, isSkipNull);
        if(c==null || c.fd("quantity").checkEnable())
        setVal(from.getQuantity(), to::setQuantity, isSkipNull);
        if(c==null || c.fd("unitPrice").checkEnable())
        setVal(from.getUnitPrice(), to::setUnitPrice, isSkipNull);
        if(c==null || c.fd("discount").checkEnable())
        setVal(from.getDiscount(), to::setDiscount, isSkipNull);
    }
    
    
    public static void mapping(OrderItem from, OrderItemDTO to){
        mapping(from,to,false);
    }
    public static void mapping(OrderItem from, OrderItemDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static OrderItemDTO toDTO(OrderItem from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static OrderItemDTO toDTO(OrderItem from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        OrderItemDTO to = new OrderItemDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<OrderItemDTO> toOrderItemDTOList(List<OrderItem> fromList){
        return toOrderItemDTOList(fromList,(MappingContext) null);
    }
    public static List<OrderItemDTO> toOrderItemDTOList(List<OrderItem> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<OrderItemDTO> toOrderItemDTOList(List<OrderItem> fromList, BiFunction<OrderItem, OrderItemDTO, OrderItemDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                OrderItemDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(Coupon from, CouponView to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(Coupon from, CouponView to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("couponCode").checkEnable())
        setVal(from.getCouponCode(), to::setCouponCode, isSkipNull);
        if(c==null || c.fd("title").checkEnable())
        setVal(from.getTitle(), to::setTitle, isSkipNull);
        if(c==null || c.fd("discountRate").checkEnable())
        setVal(from.getDiscountRate(), to::setDiscountRate, isSkipNull);
    }
    
    
    public static void mapping(Coupon from, CouponView to){
        mapping(from,to,false);
    }
    public static void mapping(Coupon from, CouponView to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static CouponView toDTO(Coupon from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static CouponView toDTO(Coupon from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        CouponView to = new CouponView();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<CouponView> toCouponViewList(List<Coupon> fromList){
        return toCouponViewList(fromList,(MappingContext) null);
    }
    public static List<CouponView> toCouponViewList(List<Coupon> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<CouponView> toCouponViewList(List<Coupon> fromList, BiFunction<Coupon, CouponView, CouponView> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                CouponView to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(GiftCard from, GiftCardDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(GiftCard from, GiftCardDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("cardNo").checkEnable())
        setVal(from.getCardNo(), to::setCardNo, isSkipNull);
        if(c==null || c.fd("balance").checkEnable())
        setVal(from.getBalance(), to::setBalance, isSkipNull);
    }
    
    
    public static void mapping(GiftCard from, GiftCardDTO to){
        mapping(from,to,false);
    }
    public static void mapping(GiftCard from, GiftCardDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static GiftCardDTO toDTO(GiftCard from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static GiftCardDTO toDTO(GiftCard from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        GiftCardDTO to = new GiftCardDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<GiftCardDTO> toGiftCardDTOList(List<GiftCard> fromList){
        return toGiftCardDTOList(fromList,(MappingContext) null);
    }
    public static List<GiftCardDTO> toGiftCardDTOList(List<GiftCard> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<GiftCardDTO> toGiftCardDTOList(List<GiftCard> fromList, BiFunction<GiftCard, GiftCardDTO, GiftCardDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                GiftCardDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(LedgerDTO from, Ledger to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(LedgerDTO from, Ledger to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("ledgerId").checkEnable())
        setVal(from.getLedgerId(), to::setLedgerId, isSkipNull);
        if(c==null || c.fd("code").checkEnable())
        setVal(from.getCode(), to::setCode, isSkipNull);
        if(c==null || c.fd("memo").checkEnable())
        setVal(from.getMemo(), to::setMemo, isSkipNull);
        if(c==null || c.fd("amount").checkEnable())
        setVal(from.getAmount(), to::setAmount, isSkipNull);
        if(c==null || c.fd("postedAt").checkEnable())
        setVal(from.getPostedAt(), to::setPostedAt, isSkipNull);
        if(c==null || c.fd("deleted").checkEnable())
        setVal(from.getDeleted(), to::setDeleted, isSkipNull);
        
        
        if(hasValue(from.getOwnerCustomerId())){
            Customer t = new Customer();
            t.setCustomerId(from.getOwnerCustomerId());
            to.setOwner(t);
        }
        
        
        if(hasValue(from.getPartnerCustomerId())){
            Customer t = new Customer();
            t.setCustomerId(from.getPartnerCustomerId());
            to.setPartner(t);
        }
    }
    
    
    public static void mapping(LedgerDTO from, Ledger to){
        mapping(from,to,false);
    }
    public static void mapping(LedgerDTO from, Ledger to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(LedgerDTO from, Ledger to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(LedgerDTO from, Ledger to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(LedgerDTO from, Ledger to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(LedgerDTO from, Ledger to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("ledgerId").checkEnable())
        setVal(from.getLedgerId(), to::setLedgerId, isSkipNull);
        if(c==null || c.fd("code").checkEnable())
        setVal(from.getCode(), to::setCode, isSkipNull);
        if(c==null || c.fd("memo").checkEnable())
        setVal(from.getMemo(), to::setMemo, isSkipNull);
        if(c==null || c.fd("amount").checkEnable())
        setVal(from.getAmount(), to::setAmount, isSkipNull);
        if(c==null || c.fd("postedAt").checkEnable())
        setVal(from.getPostedAt(), to::setPostedAt, isSkipNull);
        if(c==null || c.fd("deleted").checkEnable())
        setVal(from.getDeleted(), to::setDeleted, isSkipNull);
        
        
        if(hasValue(from.getOwnerCustomerId())){
            if(em != null){
                to.setOwner(em.getReference(Customer.class, from.getOwnerCustomerId()));
            }
            else{
                Customer t = new Customer();
                t.setCustomerId(from.getOwnerCustomerId());
                to.setOwner(t);
            }
        }
        
        
        if(hasValue(from.getPartnerCustomerId())){
            if(em != null){
                to.setPartner(em.getReference(Customer.class, from.getPartnerCustomerId()));
            }
            else{
                Customer t = new Customer();
                t.setCustomerId(from.getPartnerCustomerId());
                to.setPartner(t);
            }
        }
    }
    
    
    public static Ledger toEntity(LedgerDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static Ledger toEntity(LedgerDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Ledger to = new Ledger();
        mapping(from,to,c);
        return to;
    }
    
    
    public static Ledger toEntity(LedgerDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static Ledger toEntity(LedgerDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Ledger to = new Ledger();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<Ledger> toLedgerList(List<LedgerDTO> fromList){
        return toLedgerList(fromList,(MappingContext) null);
    }
    public static List<Ledger> toLedgerList(List<LedgerDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<Ledger> toLedgerList(List<LedgerDTO> fromList, EntityManager em){
        return toLedgerList(fromList,(MappingContext) null,em);
    }
    public static List<Ledger> toLedgerList(List<LedgerDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<Ledger> toLedgerList(List<LedgerDTO> fromList, BiFunction<LedgerDTO, Ledger, Ledger> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                Ledger to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(ProductDTO from, Product to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(ProductDTO from, Product to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("productId").checkEnable())
        setVal(from.getProductId(), to::setProductId, isSkipNull);
        if(c==null || c.fd("name").checkEnable())
        setVal(from.getName(), to::setName, isSkipNull);
        if(c==null || c.fd("sku").checkEnable())
        setVal(from.getSku(), to::setSku, isSkipNull);
        if(c==null || c.fd("description").checkEnable())
        setVal(from.getDescription(), to::setDescription, isSkipNull);
        if(c==null || c.fd("price").checkEnable())
        setVal(from.getPrice(), to::setPrice, isSkipNull);
        if(c==null || c.fd("costPrice").checkEnable())
        setVal(from.getCostPrice(), to::setCostPrice, isSkipNull);
        if(c==null || c.fd("stockQuantity").checkEnable())
        setVal(from.getStockQuantity(), to::setStockQuantity, isSkipNull);
        if(c==null || c.fd("weight").checkEnable())
        setVal(from.getWeight(), to::setWeight, isSkipNull);
        if(c==null || c.fd("rating").checkEnable())
        setVal(from.getRating(), to::setRating, isSkipNull);
        if(c==null || c.fd("active").checkEnable())
        setVal(from.getActive(), to::setActive, isSkipNull);
        if(c==null || c.fd("launchDate").checkEnable())
        setVal(from.getLaunchDate(), to::setLaunchDate, isSkipNull);
        if(c==null || c.fd("lastRestocked").checkEnable())
        setVal(from.getLastRestocked(), to::setLastRestocked, isSkipNull);
        if(c==null || c.fd("uuid").checkEnable())
        setVal(from.getUuid(), to::setUuid, isSkipNull);
        if(c==null || c.fd("metadata").checkEnable())
        setVal(from.getMetadata(), to::setMetadata, isSkipNull);
        if(c==null || c.fd("metadataRaw").checkEnable())
        setVal(from.getMetadataRaw(), to::setMetadataRaw, isSkipNull);
        if(c==null || c.fd("oldPayload").checkEnable())
        setVal(from.getOldPayload(), to::setOldPayload, isSkipNull);
        if(c==null || c.fd("customXml").checkEnable())
        setVal(from.getCustomXml(), to::setCustomXml, isSkipNull);
        if(c==null || c.fd("forceLong").checkEnable())
        setVal(from.getForceLong(), to::setForceLong, isSkipNull);
        if(c==null || c.fd("tags").checkEnable())
        setVal(from.getTags(), to::setTags, isSkipNull);
        if(c==null || c.fd("category").checkEnable())
        setVal(from.getCategory(), to::setCategory, isSkipNull, e->toEntity(e,c));
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(hasValue(from.getCategoryName()) && hasValue(to.getCategory()))
            to.getCategory().setName(from.getCategoryName());
        
        
        if(hasValue(from.getCategoryCatId())){
            Category t = new Category();
            t.setCatId(from.getCategoryCatId());
            to.setCategory(t);
        }
    }
    
    
    public static void mapping(ProductDTO from, Product to){
        mapping(from,to,false);
    }
    public static void mapping(ProductDTO from, Product to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(ProductDTO from, Product to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(ProductDTO from, Product to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(ProductDTO from, Product to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(ProductDTO from, Product to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("productId").checkEnable())
        setVal(from.getProductId(), to::setProductId, isSkipNull);
        if(c==null || c.fd("name").checkEnable())
        setVal(from.getName(), to::setName, isSkipNull);
        if(c==null || c.fd("sku").checkEnable())
        setVal(from.getSku(), to::setSku, isSkipNull);
        if(c==null || c.fd("description").checkEnable())
        setVal(from.getDescription(), to::setDescription, isSkipNull);
        if(c==null || c.fd("price").checkEnable())
        setVal(from.getPrice(), to::setPrice, isSkipNull);
        if(c==null || c.fd("costPrice").checkEnable())
        setVal(from.getCostPrice(), to::setCostPrice, isSkipNull);
        if(c==null || c.fd("stockQuantity").checkEnable())
        setVal(from.getStockQuantity(), to::setStockQuantity, isSkipNull);
        if(c==null || c.fd("weight").checkEnable())
        setVal(from.getWeight(), to::setWeight, isSkipNull);
        if(c==null || c.fd("rating").checkEnable())
        setVal(from.getRating(), to::setRating, isSkipNull);
        if(c==null || c.fd("active").checkEnable())
        setVal(from.getActive(), to::setActive, isSkipNull);
        if(c==null || c.fd("launchDate").checkEnable())
        setVal(from.getLaunchDate(), to::setLaunchDate, isSkipNull);
        if(c==null || c.fd("lastRestocked").checkEnable())
        setVal(from.getLastRestocked(), to::setLastRestocked, isSkipNull);
        if(c==null || c.fd("uuid").checkEnable())
        setVal(from.getUuid(), to::setUuid, isSkipNull);
        if(c==null || c.fd("metadata").checkEnable())
        setVal(from.getMetadata(), to::setMetadata, isSkipNull);
        if(c==null || c.fd("metadataRaw").checkEnable())
        setVal(from.getMetadataRaw(), to::setMetadataRaw, isSkipNull);
        if(c==null || c.fd("oldPayload").checkEnable())
        setVal(from.getOldPayload(), to::setOldPayload, isSkipNull);
        if(c==null || c.fd("customXml").checkEnable())
        setVal(from.getCustomXml(), to::setCustomXml, isSkipNull);
        if(c==null || c.fd("forceLong").checkEnable())
        setVal(from.getForceLong(), to::setForceLong, isSkipNull);
        if(c==null || c.fd("tags").checkEnable())
        setVal(from.getTags(), to::setTags, isSkipNull);
        if(c==null || c.fd("category").checkEnable())
        setVal(from.getCategory(), to::setCategory, isSkipNull, e->toEntity(e,c,em));
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(hasValue(from.getCategoryName()) && hasValue(to.getCategory()))
            to.getCategory().setName(from.getCategoryName());
        
        
        if(hasValue(from.getCategoryCatId())){
            if(em != null){
                to.setCategory(em.getReference(Category.class, from.getCategoryCatId()));
            }
            else{
                Category t = new Category();
                t.setCatId(from.getCategoryCatId());
                to.setCategory(t);
            }
        }
    }
    
    
    public static Product toEntity(ProductDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static Product toEntity(ProductDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Product to = new Product();
        mapping(from,to,c);
        return to;
    }
    
    
    public static Product toEntity(ProductDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static Product toEntity(ProductDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Product to = new Product();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<Product> toProductList(List<ProductDTO> fromList){
        return toProductList(fromList,(MappingContext) null);
    }
    public static List<Product> toProductList(List<ProductDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<Product> toProductList(List<ProductDTO> fromList, EntityManager em){
        return toProductList(fromList,(MappingContext) null,em);
    }
    public static List<Product> toProductList(List<ProductDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<Product> toProductList(List<ProductDTO> fromList, BiFunction<ProductDTO, Product, Product> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                Product to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(ProductInputDTO from, Product to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(ProductInputDTO from, Product to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("productId").checkEnable())
        setVal(from.getProductId(), to::setProductId, isSkipNull);
        if(c==null || c.fd("name").checkEnable())
        setVal(from.getName(), to::setName, isSkipNull);
        if(c==null || c.fd("sku").checkEnable())
        setVal(from.getSku(), to::setSku, isSkipNull);
        if(c==null || c.fd("description").checkEnable())
        setVal(from.getDescription(), to::setDescription, isSkipNull);
        if(c==null || c.fd("price").checkEnable())
        setVal(from.getPrice(), to::setPrice, isSkipNull);
        if(c==null || c.fd("costPrice").checkEnable())
        setVal(from.getCostPrice(), to::setCostPrice, isSkipNull);
        if(c==null || c.fd("stockQuantity").checkEnable())
        setVal(from.getStockQuantity(), to::setStockQuantity, isSkipNull);
        if(c==null || c.fd("weight").checkEnable())
        setVal(from.getWeight(), to::setWeight, isSkipNull);
        if(c==null || c.fd("rating").checkEnable())
        setVal(from.getRating(), to::setRating, isSkipNull);
        if(c==null || c.fd("active").checkEnable())
        setVal(from.getActive(), to::setActive, isSkipNull);
        if(c==null || c.fd("launchDate").checkEnable())
        setVal(from.getLaunchDate(), to::setLaunchDate, isSkipNull);
        if(c==null || c.fd("lastRestocked").checkEnable())
        setVal(from.getLastRestocked(), to::setLastRestocked, isSkipNull);
        if(c==null || c.fd("uuid").checkEnable())
        setVal(from.getUuid(), to::setUuid, isSkipNull);
        if(c==null || c.fd("metadata").checkEnable())
        setVal(from.getMetadata(), to::setMetadata, isSkipNull);
        if(c==null || c.fd("metadataRaw").checkEnable())
        setVal(from.getMetadataRaw(), to::setMetadataRaw, isSkipNull);
        if(c==null || c.fd("oldPayload").checkEnable())
        setVal(from.getOldPayload(), to::setOldPayload, isSkipNull);
        if(c==null || c.fd("customXml").checkEnable())
        setVal(from.getCustomXml(), to::setCustomXml, isSkipNull);
        if(c==null || c.fd("forceLong").checkEnable())
        setVal(from.getForceLong(), to::setForceLong, isSkipNull);
        if(c==null || c.fd("tags").checkEnable())
        setVal(from.getTags(), to::setTags, isSkipNull);
        if(c==null || c.fd("category").checkEnable())
        setVal(from.getCategory(), to::setCategory, isSkipNull, e->toEntity(e,c));
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
    }
    
    
    public static void mapping(ProductInputDTO from, Product to){
        mapping(from,to,false);
    }
    public static void mapping(ProductInputDTO from, Product to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(ProductInputDTO from, Product to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(ProductInputDTO from, Product to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(ProductInputDTO from, Product to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(ProductInputDTO from, Product to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("productId").checkEnable())
        setVal(from.getProductId(), to::setProductId, isSkipNull);
        if(c==null || c.fd("name").checkEnable())
        setVal(from.getName(), to::setName, isSkipNull);
        if(c==null || c.fd("sku").checkEnable())
        setVal(from.getSku(), to::setSku, isSkipNull);
        if(c==null || c.fd("description").checkEnable())
        setVal(from.getDescription(), to::setDescription, isSkipNull);
        if(c==null || c.fd("price").checkEnable())
        setVal(from.getPrice(), to::setPrice, isSkipNull);
        if(c==null || c.fd("costPrice").checkEnable())
        setVal(from.getCostPrice(), to::setCostPrice, isSkipNull);
        if(c==null || c.fd("stockQuantity").checkEnable())
        setVal(from.getStockQuantity(), to::setStockQuantity, isSkipNull);
        if(c==null || c.fd("weight").checkEnable())
        setVal(from.getWeight(), to::setWeight, isSkipNull);
        if(c==null || c.fd("rating").checkEnable())
        setVal(from.getRating(), to::setRating, isSkipNull);
        if(c==null || c.fd("active").checkEnable())
        setVal(from.getActive(), to::setActive, isSkipNull);
        if(c==null || c.fd("launchDate").checkEnable())
        setVal(from.getLaunchDate(), to::setLaunchDate, isSkipNull);
        if(c==null || c.fd("lastRestocked").checkEnable())
        setVal(from.getLastRestocked(), to::setLastRestocked, isSkipNull);
        if(c==null || c.fd("uuid").checkEnable())
        setVal(from.getUuid(), to::setUuid, isSkipNull);
        if(c==null || c.fd("metadata").checkEnable())
        setVal(from.getMetadata(), to::setMetadata, isSkipNull);
        if(c==null || c.fd("metadataRaw").checkEnable())
        setVal(from.getMetadataRaw(), to::setMetadataRaw, isSkipNull);
        if(c==null || c.fd("oldPayload").checkEnable())
        setVal(from.getOldPayload(), to::setOldPayload, isSkipNull);
        if(c==null || c.fd("customXml").checkEnable())
        setVal(from.getCustomXml(), to::setCustomXml, isSkipNull);
        if(c==null || c.fd("forceLong").checkEnable())
        setVal(from.getForceLong(), to::setForceLong, isSkipNull);
        if(c==null || c.fd("tags").checkEnable())
        setVal(from.getTags(), to::setTags, isSkipNull);
        if(c==null || c.fd("category").checkEnable())
        setVal(from.getCategory(), to::setCategory, isSkipNull, e->toEntity(e,c,em));
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
    }
    
    
    public static Product toEntity(ProductInputDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static Product toEntity(ProductInputDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Product to = new Product();
        mapping(from,to,c);
        return to;
    }
    
    
    public static Product toEntity(ProductInputDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static Product toEntity(ProductInputDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Product to = new Product();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<Product> toProduct2List(List<ProductInputDTO> fromList){
        return toProduct2List(fromList,(MappingContext) null);
    }
    public static List<Product> toProduct2List(List<ProductInputDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<Product> toProduct2List(List<ProductInputDTO> fromList, EntityManager em){
        return toProduct2List(fromList,(MappingContext) null,em);
    }
    public static List<Product> toProduct2List(List<ProductInputDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<Product> toProduct2List(List<ProductInputDTO> fromList, BiFunction<ProductInputDTO, Product, Product> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                Product to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(CategoryDTO from, Category to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(CategoryDTO from, Category to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("catId").checkEnable())
        setVal(from.getCatId(), to::setCatId, isSkipNull);
        if(c==null || c.fd("name").checkEnable())
        setVal(from.getName(), to::setName, isSkipNull);
        if(c==null || c.fd("description").checkEnable())
        setVal(from.getDescription(), to::setDescription, isSkipNull);
        if(c==null || c.fd("sortOrder").checkEnable())
        setVal(from.getSortOrder(), to::setSortOrder, isSkipNull);
        if(c==null || c.fd("active").checkEnable())
        setVal(from.getActive(), to::setActive, isSkipNull);
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
    }
    
    
    public static void mapping(CategoryDTO from, Category to){
        mapping(from,to,false);
    }
    public static void mapping(CategoryDTO from, Category to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(CategoryDTO from, Category to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(CategoryDTO from, Category to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(CategoryDTO from, Category to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(CategoryDTO from, Category to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("catId").checkEnable())
        setVal(from.getCatId(), to::setCatId, isSkipNull);
        if(c==null || c.fd("name").checkEnable())
        setVal(from.getName(), to::setName, isSkipNull);
        if(c==null || c.fd("description").checkEnable())
        setVal(from.getDescription(), to::setDescription, isSkipNull);
        if(c==null || c.fd("sortOrder").checkEnable())
        setVal(from.getSortOrder(), to::setSortOrder, isSkipNull);
        if(c==null || c.fd("active").checkEnable())
        setVal(from.getActive(), to::setActive, isSkipNull);
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
    }
    
    
    public static Category toEntity(CategoryDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static Category toEntity(CategoryDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Category to = new Category();
        mapping(from,to,c);
        return to;
    }
    
    
    public static Category toEntity(CategoryDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static Category toEntity(CategoryDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Category to = new Category();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<Category> toCategoryList(List<CategoryDTO> fromList){
        return toCategoryList(fromList,(MappingContext) null);
    }
    public static List<Category> toCategoryList(List<CategoryDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<Category> toCategoryList(List<CategoryDTO> fromList, EntityManager em){
        return toCategoryList(fromList,(MappingContext) null,em);
    }
    public static List<Category> toCategoryList(List<CategoryDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<Category> toCategoryList(List<CategoryDTO> fromList, BiFunction<CategoryDTO, Category, Category> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                Category to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(OrderDTO from, Order to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(OrderDTO from, Order to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("orderId").checkEnable())
        setVal(from.getOrderId(), to::setOrderId, isSkipNull);
        if(c==null || c.fd("orderDate").checkEnable())
        setVal(from.getOrderDate(), to::setOrderDate, isSkipNull);
        if(c==null || c.fd("totalAmount").checkEnable())
        setVal(from.getTotalAmount(), to::setTotalAmount, isSkipNull);
        if(c==null || c.fd("status").checkEnable())
        setVal(from.getStatus(), to::setStatus, isSkipNull);
        if(c==null || c.fd("paymentMethod").checkEnable())
        setVal(from.getPaymentMethod(), to::setPaymentMethod, isSkipNull);
        if(c==null || c.fd("notes").checkEnable())
        setVal(from.getNotes(), to::setNotes, isSkipNull);
        if(c==null || c.fd("trackingNumber").checkEnable())
        setVal(from.getTrackingNumber(), to::setTrackingNumber, isSkipNull);
        if(c==null || c.fd("version").checkEnable())
        setVal(from.getVersion(), to::setVersion, isSkipNull);
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
        if(hasValue(from.getCustomerFirstName()) && hasValue(to.getCustomer()))
            to.getCustomer().setFirstName(from.getCustomerFirstName());
        if(hasValue(from.getCustomerEmail()) && hasValue(to.getCustomer()))
            to.getCustomer().setEmail(from.getCustomerEmail());
    }
    
    
    public static void mapping(OrderDTO from, Order to){
        mapping(from,to,false);
    }
    public static void mapping(OrderDTO from, Order to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(OrderDTO from, Order to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(OrderDTO from, Order to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(OrderDTO from, Order to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(OrderDTO from, Order to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("orderId").checkEnable())
        setVal(from.getOrderId(), to::setOrderId, isSkipNull);
        if(c==null || c.fd("orderDate").checkEnable())
        setVal(from.getOrderDate(), to::setOrderDate, isSkipNull);
        if(c==null || c.fd("totalAmount").checkEnable())
        setVal(from.getTotalAmount(), to::setTotalAmount, isSkipNull);
        if(c==null || c.fd("status").checkEnable())
        setVal(from.getStatus(), to::setStatus, isSkipNull);
        if(c==null || c.fd("paymentMethod").checkEnable())
        setVal(from.getPaymentMethod(), to::setPaymentMethod, isSkipNull);
        if(c==null || c.fd("notes").checkEnable())
        setVal(from.getNotes(), to::setNotes, isSkipNull);
        if(c==null || c.fd("trackingNumber").checkEnable())
        setVal(from.getTrackingNumber(), to::setTrackingNumber, isSkipNull);
        if(c==null || c.fd("version").checkEnable())
        setVal(from.getVersion(), to::setVersion, isSkipNull);
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
        if(hasValue(from.getCustomerFirstName()) && hasValue(to.getCustomer()))
            to.getCustomer().setFirstName(from.getCustomerFirstName());
        if(hasValue(from.getCustomerEmail()) && hasValue(to.getCustomer()))
            to.getCustomer().setEmail(from.getCustomerEmail());
    }
    
    
    public static Order toEntity(OrderDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static Order toEntity(OrderDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Order to = new Order();
        mapping(from,to,c);
        return to;
    }
    
    
    public static Order toEntity(OrderDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static Order toEntity(OrderDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Order to = new Order();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<Order> toOrderList(List<OrderDTO> fromList){
        return toOrderList(fromList,(MappingContext) null);
    }
    public static List<Order> toOrderList(List<OrderDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<Order> toOrderList(List<OrderDTO> fromList, EntityManager em){
        return toOrderList(fromList,(MappingContext) null,em);
    }
    public static List<Order> toOrderList(List<OrderDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<Order> toOrderList(List<OrderDTO> fromList, BiFunction<OrderDTO, Order, Order> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                Order to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(OrderItemDTO from, OrderItem to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(OrderItemDTO from, OrderItem to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("orderItemId").checkEnable())
        setVal(from.getOrderItemId(), to::setOrderItemId, isSkipNull);
        if(c==null || c.fd("itemSeq").checkEnable())
        setVal(from.getItemSeq(), to::setItemSeq, isSkipNull);
        if(c==null || c.fd("quantity").checkEnable())
        setVal(from.getQuantity(), to::setQuantity, isSkipNull);
        if(c==null || c.fd("unitPrice").checkEnable())
        setVal(from.getUnitPrice(), to::setUnitPrice, isSkipNull);
        if(c==null || c.fd("discount").checkEnable())
        setVal(from.getDiscount(), to::setDiscount, isSkipNull);
        if(hasValue(from.getProductName()) && hasValue(to.getProduct()))
            to.getProduct().setName(from.getProductName());
        if(hasValue(from.getProductSku()) && hasValue(to.getProduct()))
            to.getProduct().setSku(from.getProductSku());
    }
    
    
    public static void mapping(OrderItemDTO from, OrderItem to){
        mapping(from,to,false);
    }
    public static void mapping(OrderItemDTO from, OrderItem to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(OrderItemDTO from, OrderItem to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(OrderItemDTO from, OrderItem to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(OrderItemDTO from, OrderItem to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(OrderItemDTO from, OrderItem to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("orderItemId").checkEnable())
        setVal(from.getOrderItemId(), to::setOrderItemId, isSkipNull);
        if(c==null || c.fd("itemSeq").checkEnable())
        setVal(from.getItemSeq(), to::setItemSeq, isSkipNull);
        if(c==null || c.fd("quantity").checkEnable())
        setVal(from.getQuantity(), to::setQuantity, isSkipNull);
        if(c==null || c.fd("unitPrice").checkEnable())
        setVal(from.getUnitPrice(), to::setUnitPrice, isSkipNull);
        if(c==null || c.fd("discount").checkEnable())
        setVal(from.getDiscount(), to::setDiscount, isSkipNull);
        if(hasValue(from.getProductName()) && hasValue(to.getProduct()))
            to.getProduct().setName(from.getProductName());
        if(hasValue(from.getProductSku()) && hasValue(to.getProduct()))
            to.getProduct().setSku(from.getProductSku());
    }
    
    
    public static OrderItem toEntity(OrderItemDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static OrderItem toEntity(OrderItemDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        OrderItem to = new OrderItem();
        mapping(from,to,c);
        return to;
    }
    
    
    public static OrderItem toEntity(OrderItemDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static OrderItem toEntity(OrderItemDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        OrderItem to = new OrderItem();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<OrderItem> toOrderItemList(List<OrderItemDTO> fromList){
        return toOrderItemList(fromList,(MappingContext) null);
    }
    public static List<OrderItem> toOrderItemList(List<OrderItemDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<OrderItem> toOrderItemList(List<OrderItemDTO> fromList, EntityManager em){
        return toOrderItemList(fromList,(MappingContext) null,em);
    }
    public static List<OrderItem> toOrderItemList(List<OrderItemDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<OrderItem> toOrderItemList(List<OrderItemDTO> fromList, BiFunction<OrderItemDTO, OrderItem, OrderItem> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                OrderItem to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(CustomerDTO from, Customer to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(CustomerDTO from, Customer to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("customerId").checkEnable())
        setVal(from.getCustomerId(), to::setCustomerId, isSkipNull);
        if(c==null || c.fd("firstName").checkEnable())
        setVal(from.getFirstName(), to::setFirstName, isSkipNull);
        if(c==null || c.fd("lastName").checkEnable())
        setVal(from.getLastName(), to::setLastName, isSkipNull);
        if(c==null || c.fd("email").checkEnable())
        setVal(from.getEmail(), to::setEmail, isSkipNull);
        if(c==null || c.fd("phone").checkEnable())
        setVal(from.getPhone(), to::setPhone, isSkipNull);
        if(c==null || c.fd("dateOfBirth").checkEnable())
        setVal(from.getDateOfBirth(), to::setDateOfBirth, isSkipNull);
        if(c==null || c.fd("loyaltyPoints").checkEnable())
        setVal(from.getLoyaltyPoints(), to::setLoyaltyPoints, isSkipNull);
        if(c==null || c.fd("customerType").checkEnable())
        setVal(from.getCustomerType(), to::setCustomerType, isSkipNull);
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
    }
    
    
    public static void mapping(CustomerDTO from, Customer to){
        mapping(from,to,false);
    }
    public static void mapping(CustomerDTO from, Customer to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(CustomerDTO from, Customer to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(CustomerDTO from, Customer to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(CustomerDTO from, Customer to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(CustomerDTO from, Customer to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("customerId").checkEnable())
        setVal(from.getCustomerId(), to::setCustomerId, isSkipNull);
        if(c==null || c.fd("firstName").checkEnable())
        setVal(from.getFirstName(), to::setFirstName, isSkipNull);
        if(c==null || c.fd("lastName").checkEnable())
        setVal(from.getLastName(), to::setLastName, isSkipNull);
        if(c==null || c.fd("email").checkEnable())
        setVal(from.getEmail(), to::setEmail, isSkipNull);
        if(c==null || c.fd("phone").checkEnable())
        setVal(from.getPhone(), to::setPhone, isSkipNull);
        if(c==null || c.fd("dateOfBirth").checkEnable())
        setVal(from.getDateOfBirth(), to::setDateOfBirth, isSkipNull);
        if(c==null || c.fd("loyaltyPoints").checkEnable())
        setVal(from.getLoyaltyPoints(), to::setLoyaltyPoints, isSkipNull);
        if(c==null || c.fd("customerType").checkEnable())
        setVal(from.getCustomerType(), to::setCustomerType, isSkipNull);
        if(c==null || c.fd("createdBy").checkEnable())
        setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);
        if(c==null || c.fd("createDate").checkEnable())
        setVal(from.getCreateDate(), to::setCreateDate, isSkipNull);
        if(c==null || c.fd("lastModifiedBy").checkEnable())
        setVal(from.getLastModifiedBy(), to::setLastModifiedBy, isSkipNull);
        if(c==null || c.fd("lastModifiedDate").checkEnable())
        setVal(from.getLastModifiedDate(), to::setLastModifiedDate, isSkipNull);
    }
    
    
    public static Customer toEntity(CustomerDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static Customer toEntity(CustomerDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Customer to = new Customer();
        mapping(from,to,c);
        return to;
    }
    
    
    public static Customer toEntity(CustomerDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static Customer toEntity(CustomerDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Customer to = new Customer();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<Customer> toCustomerList(List<CustomerDTO> fromList){
        return toCustomerList(fromList,(MappingContext) null);
    }
    public static List<Customer> toCustomerList(List<CustomerDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<Customer> toCustomerList(List<CustomerDTO> fromList, EntityManager em){
        return toCustomerList(fromList,(MappingContext) null,em);
    }
    public static List<Customer> toCustomerList(List<CustomerDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<Customer> toCustomerList(List<CustomerDTO> fromList, BiFunction<CustomerDTO, Customer, Customer> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                Customer to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(CouponView from, Coupon to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(CouponView from, Coupon to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("couponCode").checkEnable())
        setVal(from.getCouponCode(), to::setCouponCode, isSkipNull);
        if(c==null || c.fd("title").checkEnable())
        setVal(from.getTitle(), to::setTitle, isSkipNull);
        if(c==null || c.fd("discountRate").checkEnable())
        setVal(from.getDiscountRate(), to::setDiscountRate, isSkipNull);
    }
    
    
    public static void mapping(CouponView from, Coupon to){
        mapping(from,to,false);
    }
    public static void mapping(CouponView from, Coupon to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(CouponView from, Coupon to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(CouponView from, Coupon to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(CouponView from, Coupon to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(CouponView from, Coupon to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("couponCode").checkEnable())
        setVal(from.getCouponCode(), to::setCouponCode, isSkipNull);
        if(c==null || c.fd("title").checkEnable())
        setVal(from.getTitle(), to::setTitle, isSkipNull);
        if(c==null || c.fd("discountRate").checkEnable())
        setVal(from.getDiscountRate(), to::setDiscountRate, isSkipNull);
    }
    
    
    public static Coupon toEntity(CouponView from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static Coupon toEntity(CouponView from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Coupon to = new Coupon();
        mapping(from,to,c);
        return to;
    }
    
    
    public static Coupon toEntity(CouponView from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static Coupon toEntity(CouponView from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Coupon to = new Coupon();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<Coupon> toCouponList(List<CouponView> fromList){
        return toCouponList(fromList,(MappingContext) null);
    }
    public static List<Coupon> toCouponList(List<CouponView> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<Coupon> toCouponList(List<CouponView> fromList, EntityManager em){
        return toCouponList(fromList,(MappingContext) null,em);
    }
    public static List<Coupon> toCouponList(List<CouponView> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<Coupon> toCouponList(List<CouponView> fromList, BiFunction<CouponView, Coupon, Coupon> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                Coupon to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(GiftCardDTO from, GiftCard to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(GiftCardDTO from, GiftCard to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("cardNo").checkEnable())
        setVal(from.getCardNo(), to::setCardNo, isSkipNull);
        if(c==null || c.fd("balance").checkEnable())
        setVal(from.getBalance(), to::setBalance, isSkipNull);
    }
    
    
    public static void mapping(GiftCardDTO from, GiftCard to){
        mapping(from,to,false);
    }
    public static void mapping(GiftCardDTO from, GiftCard to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(GiftCardDTO from, GiftCard to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(GiftCardDTO from, GiftCard to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(GiftCardDTO from, GiftCard to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(GiftCardDTO from, GiftCard to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("cardNo").checkEnable())
        setVal(from.getCardNo(), to::setCardNo, isSkipNull);
        if(c==null || c.fd("balance").checkEnable())
        setVal(from.getBalance(), to::setBalance, isSkipNull);
    }
    
    
    public static GiftCard toEntity(GiftCardDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static GiftCard toEntity(GiftCardDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        GiftCard to = new GiftCard();
        mapping(from,to,c);
        return to;
    }
    
    
    public static GiftCard toEntity(GiftCardDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static GiftCard toEntity(GiftCardDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        GiftCard to = new GiftCard();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<GiftCard> toGiftCardList(List<GiftCardDTO> fromList){
        return toGiftCardList(fromList,(MappingContext) null);
    }
    public static List<GiftCard> toGiftCardList(List<GiftCardDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<GiftCard> toGiftCardList(List<GiftCardDTO> fromList, EntityManager em){
        return toGiftCardList(fromList,(MappingContext) null,em);
    }
    public static List<GiftCard> toGiftCardList(List<GiftCardDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<GiftCard> toGiftCardList(List<GiftCardDTO> fromList, BiFunction<GiftCardDTO, GiftCard, GiftCard> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                GiftCard to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(VipProfileDTO from, VipProfile to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(VipProfileDTO from, VipProfile to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("profileId").checkEnable())
        setVal(from.getProfileId(), to::setProfileId, isSkipNull);
        if(c==null || c.fd("tier").checkEnable())
        setVal(from.getTier(), to::setTier, isSkipNull);
        if(c==null || c.fd("discountRate").checkEnable())
        setVal(from.getDiscountRate(), to::setDiscountRate, isSkipNull);
        if(c==null || c.fd("enrollDate").checkEnable())
        setVal(from.getEnrollDate(), to::setEnrollDate, isSkipNull);
        
        
        if(hasValue(from.getCustomerCustomerId())){
            Customer t = new Customer();
            t.setCustomerId(from.getCustomerCustomerId());
            to.setCustomer(t);
        }
    }
    
    
    public static void mapping(VipProfileDTO from, VipProfile to){
        mapping(from,to,false);
    }
    public static void mapping(VipProfileDTO from, VipProfile to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(VipProfileDTO from, VipProfile to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(VipProfileDTO from, VipProfile to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(VipProfileDTO from, VipProfile to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(VipProfileDTO from, VipProfile to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("profileId").checkEnable())
        setVal(from.getProfileId(), to::setProfileId, isSkipNull);
        if(c==null || c.fd("tier").checkEnable())
        setVal(from.getTier(), to::setTier, isSkipNull);
        if(c==null || c.fd("discountRate").checkEnable())
        setVal(from.getDiscountRate(), to::setDiscountRate, isSkipNull);
        if(c==null || c.fd("enrollDate").checkEnable())
        setVal(from.getEnrollDate(), to::setEnrollDate, isSkipNull);
        
        
        if(hasValue(from.getCustomerCustomerId())){
            if(em != null){
                to.setCustomer(em.getReference(Customer.class, from.getCustomerCustomerId()));
            }
            else{
                Customer t = new Customer();
                t.setCustomerId(from.getCustomerCustomerId());
                to.setCustomer(t);
            }
        }
    }
    
    
    public static VipProfile toEntity(VipProfileDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static VipProfile toEntity(VipProfileDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        VipProfile to = new VipProfile();
        mapping(from,to,c);
        return to;
    }
    
    
    public static VipProfile toEntity(VipProfileDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static VipProfile toEntity(VipProfileDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        VipProfile to = new VipProfile();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<VipProfile> toVipProfileList(List<VipProfileDTO> fromList){
        return toVipProfileList(fromList,(MappingContext) null);
    }
    public static List<VipProfile> toVipProfileList(List<VipProfileDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<VipProfile> toVipProfileList(List<VipProfileDTO> fromList, EntityManager em){
        return toVipProfileList(fromList,(MappingContext) null,em);
    }
    public static List<VipProfile> toVipProfileList(List<VipProfileDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->ShopMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<VipProfile> toVipProfileList(List<VipProfileDTO> fromList, BiFunction<VipProfileDTO, VipProfile, VipProfile> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                VipProfile to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static Specification<Ledger> toSpec(LedgerQuery searchDTO){
        Specification<Ledger> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<Ledger> toSpec(LedgerQuery searchDTO, Specification<Ledger> sp){
        String memo = searchDTO.getMemo();
        if(hasValue(memo)){
            sp = sp.and((r,q,c) -> c.like(r.get("memo"),"%" +trim(memo)+ "%"));
        }
        String code = searchDTO.getCode();
        if(hasValue(code)){
            sp = sp.and((r,q,c) -> c.equal(r.get("code"),code));
        }
        Customer owner = searchDTO.getOwner();
        if(hasValue(owner)){
            sp = sp.and((r,q,c) -> c.equal(r.get("owner"),owner));
        }
        Customer partner = searchDTO.getPartner();
        if(hasValue(partner)){
            sp = sp.and((r,q,c) -> c.equal(r.get("partner"),partner));
        }
        return sp;
    }
    
    
    public static Specification<Ledger> toSpec(LedgerSrchDTO searchDTO){
        Specification<Ledger> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<Ledger> toSpec(LedgerSrchDTO searchDTO, Specification<Ledger> sp){
        Long ownerCustomerId = searchDTO.getOwnerCustomerId();
        if(hasValue(ownerCustomerId)){
            sp = sp.and((r,q,c) -> c.equal(r.get("owner").get("customerId"),ownerCustomerId));
        }
        Long partnerCustomerId = searchDTO.getPartnerCustomerId();
        if(hasValue(partnerCustomerId)){
            sp = sp.and((r,q,c) -> c.equal(r.get("partner").get("customerId"),partnerCustomerId));
        }
        Long ledgerId = searchDTO.getLedgerId();
        if(hasValue(ledgerId)){
            sp = sp.and((r,q,c) -> c.equal(r.get("ledgerId"),ledgerId));
        }
        String code = searchDTO.getCode();
        if(hasValue(code)){
            sp = sp.and((r,q,c) -> c.equal(r.get("code"),code));
        }
        String memo = searchDTO.getMemo();
        if(hasValue(memo)){
            sp = sp.and((r,q,c) -> c.like(r.get("memo"),"%" +trim(memo)+ "%"));
        }
        Long amount = searchDTO.getAmount();
        if(hasValue(amount)){
            sp = sp.and((r,q,c) -> c.equal(r.get("amount"),amount));
        }
        java.time.LocalDateTime postedAtFrom = searchDTO.getPostedAtFrom();
        java.time.LocalDateTime postedAtTo = searchDTO.getPostedAtTo();
        if(hasValue(postedAtFrom) && hasValue(postedAtTo)){
            sp = sp.and((r,q,c) -> c.between(r.get("postedAt"),postedAtFrom,postedAtTo));
        }
        else if(hasValue(postedAtFrom)){
            sp = sp.and((r,q,c) -> c.greaterThanOrEqualTo(r.get("postedAt"),postedAtFrom));
        }
        else if(hasValue(postedAtTo)){
            sp = sp.and((r,q,c) -> c.lessThanOrEqualTo(r.get("postedAt"),postedAtTo));
        }
        Boolean deleted = searchDTO.getDeleted();
        if(hasValue(deleted)){
            sp = sp.and((r,q,c) -> c.equal(r.get("deleted"),deleted));
        }
        return sp;
    }
    
    
    public static Specification<Product> toSpec(ProductSrchDTO searchDTO){
        Specification<Product> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<Product> toSpec(ProductSrchDTO searchDTO, Specification<Product> sp){
        String categoryName = searchDTO.getCategoryName();
        if(hasValue(categoryName)){
            sp = sp.and((r,q,c) -> c.like(r.get("category").get("name"),"%" +trim(categoryName)+ "%"));
        }
        String name = searchDTO.getName();
        if(hasValue(name)){
            sp = sp.and((r,q,c) -> c.like(r.get("name"),"%" +trim(name)+ "%"));
        }
        String sku = searchDTO.getSku();
        if(hasValue(sku)){
            sp = sp.and((r,q,c) -> c.equal(r.get("sku"),sku));
        }
        java.math.BigDecimal priceFrom = searchDTO.getPriceFrom();
        java.math.BigDecimal priceTo = searchDTO.getPriceTo();
        if(hasValue(priceFrom) && hasValue(priceTo)){
            sp = sp.and((r,q,c) -> c.between(r.get("price"),priceFrom,priceTo));
        }
        else if(hasValue(priceFrom)){
            sp = sp.and((r,q,c) -> c.greaterThanOrEqualTo(r.get("price"),priceFrom));
        }
        else if(hasValue(priceTo)){
            sp = sp.and((r,q,c) -> c.lessThanOrEqualTo(r.get("price"),priceTo));
        }
        Integer stockQuantity = searchDTO.getStockQuantity();
        if(hasValue(stockQuantity)){
            sp = sp.and((r,q,c) -> c.greaterThanOrEqualTo(r.get("stockQuantity"),stockQuantity));
        }
        Boolean active = searchDTO.getActive();
        if(hasValue(active)){
            sp = sp.and((r,q,c) -> c.equal(r.get("active"),active));
        }
        java.time.LocalDate launchDateFrom = searchDTO.getLaunchDateFrom();
        java.time.LocalDate launchDateTo = searchDTO.getLaunchDateTo();
        if(hasValue(launchDateFrom) && hasValue(launchDateTo)){
            java.time.LocalDate launchDateTo_2 = launchDateTo.plusDays(1);
            sp = sp.and((r,q,c) -> c.between(r.get("launchDate"),launchDateFrom,launchDateTo_2));
        }
        else if(hasValue(launchDateFrom)){
            sp = sp.and((r,q,c) -> c.greaterThanOrEqualTo(r.get("launchDate"),launchDateFrom));
        }
        else if(hasValue(launchDateTo)){
            java.time.LocalDate launchDateTo_2 = launchDateTo.plusDays(1);
            sp = sp.and((r,q,c) -> c.lessThan(r.get("launchDate"),launchDateTo_2));
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
    
    
    public static Specification<Order> toSpec(OrderSrchDTO searchDTO){
        Specification<Order> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<Order> toSpec(OrderSrchDTO searchDTO, Specification<Order> sp){
        String customerName = searchDTO.getCustomerName();
        if(hasValue(customerName)){
            sp = sp.and((r,q,c) -> c.like(r.get("customer").get("firstName"),"%" +trim(customerName)+ "%"));
        }
        java.time.LocalDateTime orderDateFrom = searchDTO.getOrderDateFrom();
        java.time.LocalDateTime orderDateTo = searchDTO.getOrderDateTo();
        if(hasValue(orderDateFrom) && hasValue(orderDateTo)){
            sp = sp.and((r,q,c) -> c.between(r.get("orderDate"),orderDateFrom,orderDateTo));
        }
        else if(hasValue(orderDateFrom)){
            sp = sp.and((r,q,c) -> c.greaterThanOrEqualTo(r.get("orderDate"),orderDateFrom));
        }
        else if(hasValue(orderDateTo)){
            sp = sp.and((r,q,c) -> c.lessThanOrEqualTo(r.get("orderDate"),orderDateTo));
        }
        OrderStatus status = searchDTO.getStatus();
        if(hasValue(status)){
            sp = sp.and((r, q, c) -> r.get("status").in(status));
        }
        PaymentMethod paymentMethod = searchDTO.getPaymentMethod();
        if(hasValue(paymentMethod)){
            sp = sp.and((r,q,c) -> c.equal(r.get("paymentMethod"),paymentMethod));
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
    
    
    public static Specification<Coupon> toSpec(CouponQuery searchDTO){
        Specification<Coupon> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<Coupon> toSpec(CouponQuery searchDTO, Specification<Coupon> sp){
        String title = searchDTO.getTitle();
        if(hasValue(title)){
            sp = sp.and((r,q,c) -> c.like(r.get("title"),"%" +trim(title)+ "%"));
        }
        return sp;
    }
    
    
    public static Specification<GiftCard> toSpec(GiftCardSrchDTO searchDTO){
        Specification<GiftCard> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<GiftCard> toSpec(GiftCardSrchDTO searchDTO, Specification<GiftCard> sp){
        String cardNo = searchDTO.getCardNo();
        if(hasValue(cardNo)){
            sp = sp.and((r,q,c) -> c.equal(r.get("cardNo"),cardNo));
        }
        return sp;
    }
    
    
    public static Specification<Category> toSpec(CategorySrchDTO searchDTO){
        Specification<Category> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<Category> toSpec(CategorySrchDTO searchDTO, Specification<Category> sp){
        Long parentCategoryCatId = searchDTO.getParentCategoryCatId();
        if(hasValue(parentCategoryCatId)){
            sp = sp.and((r,q,c) -> c.equal(r.get("parentCategory").get("catId"),parentCategoryCatId));
        }
        Long catId = searchDTO.getCatId();
        if(hasValue(catId)){
            sp = sp.and((r,q,c) -> c.equal(r.get("catId"),catId));
        }
        String name = searchDTO.getName();
        if(hasValue(name)){
            sp = sp.and((r,q,c) -> c.like(r.get("name"),"%" +trim(name)+ "%"));
        }
        sp=setSpec(sp, "description", searchDTO.getDescription());
        Integer sortOrder = searchDTO.getSortOrder();
        if(hasValue(sortOrder)){
            sp = sp.and((r,q,c) -> c.equal(r.get("sortOrder"),sortOrder));
        }
        Boolean active = searchDTO.getActive();
        if(hasValue(active)){
            sp = sp.and((r,q,c) -> c.equal(r.get("active"),active));
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
    
    
    public static Specification<Customer> toSpec(CustomerSrchDTO searchDTO){
        Specification<Customer> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<Customer> toSpec(CustomerSrchDTO searchDTO, Specification<Customer> sp){
        CustomerType customerType = searchDTO.getCustomerType();
        if(hasValue(customerType)){
            sp = sp.and((r,q,c) -> c.equal(r.get("customerType"),customerType));
        }
        Long vipProfileProfileId = searchDTO.getVipProfileProfileId();
        if(hasValue(vipProfileProfileId)){
            sp = sp.and((r,q,c) -> c.equal(r.get("vipProfile").get("profileId"),vipProfileProfileId));
        }
        Long customerId = searchDTO.getCustomerId();
        if(hasValue(customerId)){
            sp = sp.and((r,q,c) -> c.equal(r.get("customerId"),customerId));
        }
        String firstName = searchDTO.getFirstName();
        if(hasValue(firstName)){
            sp = sp.and((r,q,c) -> c.like(r.get("firstName"),"%" +trim(firstName)+ "%"));
        }
        String lastName = searchDTO.getLastName();
        if(hasValue(lastName)){
            sp = sp.and((r,q,c) -> c.like(r.get("lastName"),"%" +trim(lastName)+ "%"));
        }
        String email = searchDTO.getEmail();
        if(hasValue(email)){
            sp = sp.and((r,q,c) -> c.equal(r.get("email"),email));
        }
        sp=setSpec(sp, "phone", searchDTO.getPhone());
        java.time.LocalDate dateOfBirthFrom = searchDTO.getDateOfBirthFrom();
        java.time.LocalDate dateOfBirthTo = searchDTO.getDateOfBirthTo();
        if(hasValue(dateOfBirthFrom) && hasValue(dateOfBirthTo)){
            java.time.LocalDate dateOfBirthTo_2 = dateOfBirthTo.plusDays(1);
            sp = sp.and((r,q,c) -> c.between(r.get("dateOfBirth"),dateOfBirthFrom,dateOfBirthTo_2));
        }
        else if(hasValue(dateOfBirthFrom)){
            sp = sp.and((r,q,c) -> c.greaterThanOrEqualTo(r.get("dateOfBirth"),dateOfBirthFrom));
        }
        else if(hasValue(dateOfBirthTo)){
            java.time.LocalDate dateOfBirthTo_2 = dateOfBirthTo.plusDays(1);
            sp = sp.and((r,q,c) -> c.lessThan(r.get("dateOfBirth"),dateOfBirthTo_2));
        }
        Long loyaltyPoints = searchDTO.getLoyaltyPoints();
        if(hasValue(loyaltyPoints)){
            sp = sp.and((r,q,c) -> c.equal(r.get("loyaltyPoints"),loyaltyPoints));
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
    
    
    public static Specification<VipProfile> toSpec(VipProfileSrchDTO searchDTO){
        Specification<VipProfile> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<VipProfile> toSpec(VipProfileSrchDTO searchDTO, Specification<VipProfile> sp){
        Long customerCustomerId = searchDTO.getCustomerCustomerId();
        if(hasValue(customerCustomerId)){
            sp = sp.and((r,q,c) -> c.equal(r.get("customer").get("customerId"),customerCustomerId));
        }
        return sp;
    }
    
    
    public static Specification<OrderItem> toSpec(OrderItemSrchDTO searchDTO){
        Specification<OrderItem> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<OrderItem> toSpec(OrderItemSrchDTO searchDTO, Specification<OrderItem> sp){
        String orderOrderId = searchDTO.getOrderOrderId();
        if(hasValue(orderOrderId)){
            sp = sp.and((r,q,c) -> c.equal(r.get("order").get("orderId"),orderOrderId));
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
