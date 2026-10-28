//ecd:1070804630H20261028193834_V3.0
package io.elasticore.blueprint.domain.shop.entity;


import io.elasticore.springboot3.entity.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;
import io.elasticore.blueprint.domain.shop.enums.CustomerType;
import io.elasticore.blueprint.domain.shop.enums.PaymentMethod;
import io.elasticore.blueprint.domain.shop.enums.OrderStatus;


/**
 * This class serves as a domain-specific unified metadata holder,
 * containing field names and type information for entities.
 *
 * Generated and managed by ElastiCORE.
 */

public class Q {

	public static $Ledger<Ledger> Ledger=new $Ledger<>();
	public static class $Ledger<T> {
	
	    private FieldInfo p=null;
	    public $Ledger() {}
	    public $Ledger(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Ledger> joinSpec() {
	    	return new JoinSpec<>(Ledger.class);
	    }
	    public static UpdateSpec<Ledger> updateSpec() {
	    	return new UpdateSpec<>(Ledger.class);
	    }
	    public static DeleteSpec<Ledger> deleteSpec() {
	    	return new DeleteSpec<>(Ledger.class);
	    }
	
	    public final FieldInfo<T> getLedgerId() {return new FieldInfo(Ledger.class,"ledgerId",Long.class, null,p);}
	    public final String ledgerId="ledgerId";
	    public Specification<T> ledgerId(Op op,Object value) {return getLedgerId().where(op,value);}
	    public Specification<T> ledgerId(Op op,Object value,boolean chkEmpty) {return getLedgerId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCode() {return new FieldInfo(Ledger.class,"code",String.class, null,p);}
	    public final String code="code";
	    public Specification<T> code(Op op,Object value) {return getCode().where(op,value);}
	    public Specification<T> code(Op op,Object value,boolean chkEmpty) {return getCode().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getMemo() {return new FieldInfo(Ledger.class,"memo",String.class, null,p);}
	    public final String memo="memo";
	    public Specification<T> memo(Op op,Object value) {return getMemo().where(op,value);}
	    public Specification<T> memo(Op op,Object value,boolean chkEmpty) {return getMemo().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getAmount() {return new FieldInfo(Ledger.class,"amount",Long.class, null,p);}
	    public final String amount="amount";
	    public Specification<T> amount(Op op,Object value) {return getAmount().where(op,value);}
	    public Specification<T> amount(Op op,Object value,boolean chkEmpty) {return getAmount().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPostedAt() {return new FieldInfo(Ledger.class,"postedAt",java.time.LocalDateTime.class, null,p);}
	    public final String postedAt="postedAt";
	    public Specification<T> postedAt(Op op,Object value) {return getPostedAt().where(op,value);}
	    public Specification<T> postedAt(Op op,Object value,boolean chkEmpty) {return getPostedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOwner() {return new FieldInfo(Ledger.class,"owner",Customer.class, null,p);}
	    public final String owner="owner";
	    public final $Customer<T> owner() {return new $Customer(getOwner());}
	
	    public final FieldInfo<T> getPartner() {return new FieldInfo(Ledger.class,"partner",Customer.class, null,p);}
	    public final String partner="partner";
	    public final $Customer<T> partner() {return new $Customer(getPartner());}
	
	    public final FieldInfo<T> getDeleted() {return new FieldInfo(Ledger.class,"deleted",Boolean.class, null,p);}
	    public final String deleted="deleted";
	    public Specification<T> deleted(Op op,Object value) {return getDeleted().where(op,value);}
	    public Specification<T> deleted(Op op,Object value,boolean chkEmpty) {return getDeleted().where(op,value,chkEmpty);}
	}
	
	public static $Address<Address> Address=new $Address<>();
	public static class $Address<T> {
	
	    private FieldInfo p=null;
	    public $Address() {}
	    public $Address(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Address> joinSpec() {
	    	return new JoinSpec<>(Address.class);
	    }
	    public static UpdateSpec<Address> updateSpec() {
	    	return new UpdateSpec<>(Address.class);
	    }
	    public static DeleteSpec<Address> deleteSpec() {
	    	return new DeleteSpec<>(Address.class);
	    }
	
	    public final FieldInfo<T> getStreet() {return new FieldInfo(Address.class,"street",String.class, null,p);}
	    public final String street="street";
	    public Specification<T> street(Op op,Object value) {return getStreet().where(op,value);}
	    public Specification<T> street(Op op,Object value,boolean chkEmpty) {return getStreet().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCity() {return new FieldInfo(Address.class,"city",String.class, null,p);}
	    public final String city="city";
	    public Specification<T> city(Op op,Object value) {return getCity().where(op,value);}
	    public Specification<T> city(Op op,Object value,boolean chkEmpty) {return getCity().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getState() {return new FieldInfo(Address.class,"state",String.class, null,p);}
	    public final String state="state";
	    public Specification<T> state(Op op,Object value) {return getState().where(op,value);}
	    public Specification<T> state(Op op,Object value,boolean chkEmpty) {return getState().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getZipCode() {return new FieldInfo(Address.class,"zipCode",String.class, null,p);}
	    public final String zipCode="zipCode";
	    public Specification<T> zipCode(Op op,Object value) {return getZipCode().where(op,value);}
	    public Specification<T> zipCode(Op op,Object value,boolean chkEmpty) {return getZipCode().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCountry() {return new FieldInfo(Address.class,"country",String.class, null,p);}
	    public final String country="country";
	    public Specification<T> country(Op op,Object value) {return getCountry().where(op,value);}
	    public Specification<T> country(Op op,Object value,boolean chkEmpty) {return getCountry().where(op,value,chkEmpty);}
	}
	
	public static $Category<Category> Category=new $Category<>();
	public static class $Category<T> {
	
	    private FieldInfo p=null;
	    public $Category() {}
	    public $Category(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Category> joinSpec() {
	    	return new JoinSpec<>(Category.class);
	    }
	    public static UpdateSpec<Category> updateSpec() {
	    	return new UpdateSpec<>(Category.class);
	    }
	    public static DeleteSpec<Category> deleteSpec() {
	    	return new DeleteSpec<>(Category.class);
	    }
	
	    public final FieldInfo<T> getCatId() {return new FieldInfo(Category.class,"catId",Long.class, null,p);}
	    public final String catId="catId";
	    public Specification<T> catId(Op op,Object value) {return getCatId().where(op,value);}
	    public Specification<T> catId(Op op,Object value,boolean chkEmpty) {return getCatId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getName() {return new FieldInfo(Category.class,"name",String.class, null,p);}
	    public final String name="name";
	    public Specification<T> name(Op op,Object value) {return getName().where(op,value);}
	    public Specification<T> name(Op op,Object value,boolean chkEmpty) {return getName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getDescription() {return new FieldInfo(Category.class,"description",String.class, null,p);}
	    public final String description="description";
	    public Specification<T> description(Op op,Object value) {return getDescription().where(op,value);}
	    public Specification<T> description(Op op,Object value,boolean chkEmpty) {return getDescription().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSortOrder() {return new FieldInfo(Category.class,"sortOrder",Integer.class, null,p);}
	    public final String sortOrder="sortOrder";
	    public Specification<T> sortOrder(Op op,Object value) {return getSortOrder().where(op,value);}
	    public Specification<T> sortOrder(Op op,Object value,boolean chkEmpty) {return getSortOrder().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getActive() {return new FieldInfo(Category.class,"active",Boolean.class, null,p);}
	    public final String active="active";
	    public Specification<T> active(Op op,Object value) {return getActive().where(op,value);}
	    public Specification<T> active(Op op,Object value,boolean chkEmpty) {return getActive().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getParentCategory() {return new FieldInfo(Category.class,"parentCategory",Category.class, null,p);}
	    public final String parentCategory="parentCategory";
	    public final $Category<T> parentCategory() {return new $Category(getParentCategory());}
	
	    public final FieldInfo<T> getSubCategories() {return new FieldInfo(Category.class,"subCategories",Category.class, List.class,p);}
	    public final String subCategories="subCategories";
	
	    public final FieldInfo<T> getCreatedBy() {return new FieldInfo(Category.class,"createdBy",String.class, null,p);}
	    public final String createdBy="createdBy";
	    public Specification<T> createdBy(Op op,Object value) {return getCreatedBy().where(op,value);}
	    public Specification<T> createdBy(Op op,Object value,boolean chkEmpty) {return getCreatedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreateDate() {return new FieldInfo(Category.class,"createDate",java.time.LocalDateTime.class, null,p);}
	    public final String createDate="createDate";
	    public Specification<T> createDate(Op op,Object value) {return getCreateDate().where(op,value);}
	    public Specification<T> createDate(Op op,Object value,boolean chkEmpty) {return getCreateDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedBy() {return new FieldInfo(Category.class,"lastModifiedBy",String.class, null,p);}
	    public final String lastModifiedBy="lastModifiedBy";
	    public Specification<T> lastModifiedBy(Op op,Object value) {return getLastModifiedBy().where(op,value);}
	    public Specification<T> lastModifiedBy(Op op,Object value,boolean chkEmpty) {return getLastModifiedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedDate() {return new FieldInfo(Category.class,"lastModifiedDate",java.time.LocalDateTime.class, null,p);}
	    public final String lastModifiedDate="lastModifiedDate";
	    public Specification<T> lastModifiedDate(Op op,Object value) {return getLastModifiedDate().where(op,value);}
	    public Specification<T> lastModifiedDate(Op op,Object value,boolean chkEmpty) {return getLastModifiedDate().where(op,value,chkEmpty);}
	}
	
	public static $Product<Product> Product=new $Product<>();
	public static class $Product<T> {
	
	    private FieldInfo p=null;
	    public $Product() {}
	    public $Product(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Product> joinSpec() {
	    	return new JoinSpec<>(Product.class);
	    }
	    public static UpdateSpec<Product> updateSpec() {
	    	return new UpdateSpec<>(Product.class);
	    }
	    public static DeleteSpec<Product> deleteSpec() {
	    	return new DeleteSpec<>(Product.class);
	    }
	
	    public final FieldInfo<T> getProductId() {return new FieldInfo(Product.class,"productId",String.class, null,p);}
	    public final String productId="productId";
	    public Specification<T> productId(Op op,Object value) {return getProductId().where(op,value);}
	    public Specification<T> productId(Op op,Object value,boolean chkEmpty) {return getProductId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getName() {return new FieldInfo(Product.class,"name",String.class, null,p);}
	    public final String name="name";
	    public Specification<T> name(Op op,Object value) {return getName().where(op,value);}
	    public Specification<T> name(Op op,Object value,boolean chkEmpty) {return getName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSku() {return new FieldInfo(Product.class,"sku",String.class, null,p);}
	    public final String sku="sku";
	    public Specification<T> sku(Op op,Object value) {return getSku().where(op,value);}
	    public Specification<T> sku(Op op,Object value,boolean chkEmpty) {return getSku().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getDescription() {return new FieldInfo(Product.class,"description",String.class, null,p);}
	    public final String description="description";
	    public Specification<T> description(Op op,Object value) {return getDescription().where(op,value);}
	    public Specification<T> description(Op op,Object value,boolean chkEmpty) {return getDescription().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPrice() {return new FieldInfo(Product.class,"price",java.math.BigDecimal.class, null,p);}
	    public final String price="price";
	    public Specification<T> price(Op op,Object value) {return getPrice().where(op,value);}
	    public Specification<T> price(Op op,Object value,boolean chkEmpty) {return getPrice().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCostPrice() {return new FieldInfo(Product.class,"costPrice",java.math.BigDecimal.class, null,p);}
	    public final String costPrice="costPrice";
	    public Specification<T> costPrice(Op op,Object value) {return getCostPrice().where(op,value);}
	    public Specification<T> costPrice(Op op,Object value,boolean chkEmpty) {return getCostPrice().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getStockQuantity() {return new FieldInfo(Product.class,"stockQuantity",Integer.class, null,p);}
	    public final String stockQuantity="stockQuantity";
	    public Specification<T> stockQuantity(Op op,Object value) {return getStockQuantity().where(op,value);}
	    public Specification<T> stockQuantity(Op op,Object value,boolean chkEmpty) {return getStockQuantity().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getWeight() {return new FieldInfo(Product.class,"weight",Float.class, null,p);}
	    public final String weight="weight";
	    public Specification<T> weight(Op op,Object value) {return getWeight().where(op,value);}
	    public Specification<T> weight(Op op,Object value,boolean chkEmpty) {return getWeight().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getRating() {return new FieldInfo(Product.class,"rating",Double.class, null,p);}
	    public final String rating="rating";
	    public Specification<T> rating(Op op,Object value) {return getRating().where(op,value);}
	    public Specification<T> rating(Op op,Object value,boolean chkEmpty) {return getRating().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getActive() {return new FieldInfo(Product.class,"active",Boolean.class, null,p);}
	    public final String active="active";
	    public Specification<T> active(Op op,Object value) {return getActive().where(op,value);}
	    public Specification<T> active(Op op,Object value,boolean chkEmpty) {return getActive().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLaunchDate() {return new FieldInfo(Product.class,"launchDate",java.time.LocalDate.class, null,p);}
	    public final String launchDate="launchDate";
	    public Specification<T> launchDate(Op op,Object value) {return getLaunchDate().where(op,value);}
	    public Specification<T> launchDate(Op op,Object value,boolean chkEmpty) {return getLaunchDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastRestocked() {return new FieldInfo(Product.class,"lastRestocked",java.time.LocalDateTime.class, null,p);}
	    public final String lastRestocked="lastRestocked";
	    public Specification<T> lastRestocked(Op op,Object value) {return getLastRestocked().where(op,value);}
	    public Specification<T> lastRestocked(Op op,Object value,boolean chkEmpty) {return getLastRestocked().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getUuid() {return new FieldInfo(Product.class,"uuid",String.class, null,p);}
	    public final String uuid="uuid";
	    public Specification<T> uuid(Op op,Object value) {return getUuid().where(op,value);}
	    public Specification<T> uuid(Op op,Object value,boolean chkEmpty) {return getUuid().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getMetadata() {return new FieldInfo(Product.class,"metadata",String.class, null,p);}
	    public final String metadata="metadata";
	    public Specification<T> metadata(Op op,Object value) {return getMetadata().where(op,value);}
	    public Specification<T> metadata(Op op,Object value,boolean chkEmpty) {return getMetadata().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getThumbnail() {return new FieldInfo(Product.class,"thumbnail",String.class, null,p);}
	    public final String thumbnail="thumbnail";
	    public Specification<T> thumbnail(Op op,Object value) {return getThumbnail().where(op,value);}
	    public Specification<T> thumbnail(Op op,Object value,boolean chkEmpty) {return getThumbnail().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getManualContent() {return new FieldInfo(Product.class,"manualContent",String.class, null,p);}
	    public final String manualContent="manualContent";
	    public Specification<T> manualContent(Op op,Object value) {return getManualContent().where(op,value);}
	    public Specification<T> manualContent(Op op,Object value,boolean chkEmpty) {return getManualContent().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getMetadataRaw() {return new FieldInfo(Product.class,"metadataRaw",String.class, null,p);}
	    public final String metadataRaw="metadataRaw";
	    public Specification<T> metadataRaw(Op op,Object value) {return getMetadataRaw().where(op,value);}
	    public Specification<T> metadataRaw(Op op,Object value,boolean chkEmpty) {return getMetadataRaw().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOldPayload() {return new FieldInfo(Product.class,"oldPayload",String.class, null,p);}
	    public final String oldPayload="oldPayload";
	    public Specification<T> oldPayload(Op op,Object value) {return getOldPayload().where(op,value);}
	    public Specification<T> oldPayload(Op op,Object value,boolean chkEmpty) {return getOldPayload().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCustomXml() {return new FieldInfo(Product.class,"customXml",String.class, null,p);}
	    public final String customXml="customXml";
	    public Specification<T> customXml(Op op,Object value) {return getCustomXml().where(op,value);}
	    public Specification<T> customXml(Op op,Object value,boolean chkEmpty) {return getCustomXml().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getForceLong() {return new FieldInfo(Product.class,"forceLong",String.class, null,p);}
	    public final String forceLong="forceLong";
	    public Specification<T> forceLong(Op op,Object value) {return getForceLong().where(op,value);}
	    public Specification<T> forceLong(Op op,Object value,boolean chkEmpty) {return getForceLong().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTags() {return new FieldInfo(Product.class,"tags",String[].class, null,p);}
	    public final String tags="tags";
	    public Specification<T> tags(Op op,Object value) {return getTags().where(op,value);}
	    public Specification<T> tags(Op op,Object value,boolean chkEmpty) {return getTags().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCategory() {return new FieldInfo(Product.class,"category",Category.class, null,p);}
	    public final String category="category";
	    public final $Category<T> category() {return new $Category(getCategory());}
	
	    public final FieldInfo<T> getOrderItems() {return new FieldInfo(Product.class,"orderItems",OrderItem.class, List.class,p);}
	    public final String orderItems="orderItems";
	
	    public final FieldInfo<T> getCreatedBy() {return new FieldInfo(Product.class,"createdBy",String.class, null,p);}
	    public final String createdBy="createdBy";
	    public Specification<T> createdBy(Op op,Object value) {return getCreatedBy().where(op,value);}
	    public Specification<T> createdBy(Op op,Object value,boolean chkEmpty) {return getCreatedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreateDate() {return new FieldInfo(Product.class,"createDate",java.time.LocalDateTime.class, null,p);}
	    public final String createDate="createDate";
	    public Specification<T> createDate(Op op,Object value) {return getCreateDate().where(op,value);}
	    public Specification<T> createDate(Op op,Object value,boolean chkEmpty) {return getCreateDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedBy() {return new FieldInfo(Product.class,"lastModifiedBy",String.class, null,p);}
	    public final String lastModifiedBy="lastModifiedBy";
	    public Specification<T> lastModifiedBy(Op op,Object value) {return getLastModifiedBy().where(op,value);}
	    public Specification<T> lastModifiedBy(Op op,Object value,boolean chkEmpty) {return getLastModifiedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedDate() {return new FieldInfo(Product.class,"lastModifiedDate",java.time.LocalDateTime.class, null,p);}
	    public final String lastModifiedDate="lastModifiedDate";
	    public Specification<T> lastModifiedDate(Op op,Object value) {return getLastModifiedDate().where(op,value);}
	    public Specification<T> lastModifiedDate(Op op,Object value,boolean chkEmpty) {return getLastModifiedDate().where(op,value,chkEmpty);}
	}
	
	public static $Customer<Customer> Customer=new $Customer<>();
	public static class $Customer<T> {
	
	    private FieldInfo p=null;
	    public $Customer() {}
	    public $Customer(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Customer> joinSpec() {
	    	return new JoinSpec<>(Customer.class);
	    }
	    public static UpdateSpec<Customer> updateSpec() {
	    	return new UpdateSpec<>(Customer.class);
	    }
	    public static DeleteSpec<Customer> deleteSpec() {
	    	return new DeleteSpec<>(Customer.class);
	    }
	
	    public final FieldInfo<T> getCustomerId() {return new FieldInfo(Customer.class,"customerId",Long.class, null,p);}
	    public final String customerId="customerId";
	    public Specification<T> customerId(Op op,Object value) {return getCustomerId().where(op,value);}
	    public Specification<T> customerId(Op op,Object value,boolean chkEmpty) {return getCustomerId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getFirstName() {return new FieldInfo(Customer.class,"firstName",String.class, null,p);}
	    public final String firstName="firstName";
	    public Specification<T> firstName(Op op,Object value) {return getFirstName().where(op,value);}
	    public Specification<T> firstName(Op op,Object value,boolean chkEmpty) {return getFirstName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastName() {return new FieldInfo(Customer.class,"lastName",String.class, null,p);}
	    public final String lastName="lastName";
	    public Specification<T> lastName(Op op,Object value) {return getLastName().where(op,value);}
	    public Specification<T> lastName(Op op,Object value,boolean chkEmpty) {return getLastName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEmail() {return new FieldInfo(Customer.class,"email",String.class, null,p);}
	    public final String email="email";
	    public Specification<T> email(Op op,Object value) {return getEmail().where(op,value);}
	    public Specification<T> email(Op op,Object value,boolean chkEmpty) {return getEmail().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPhone() {return new FieldInfo(Customer.class,"phone",String.class, null,p);}
	    public final String phone="phone";
	    public Specification<T> phone(Op op,Object value) {return getPhone().where(op,value);}
	    public Specification<T> phone(Op op,Object value,boolean chkEmpty) {return getPhone().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getDateOfBirth() {return new FieldInfo(Customer.class,"dateOfBirth",java.time.LocalDate.class, null,p);}
	    public final String dateOfBirth="dateOfBirth";
	    public Specification<T> dateOfBirth(Op op,Object value) {return getDateOfBirth().where(op,value);}
	    public Specification<T> dateOfBirth(Op op,Object value,boolean chkEmpty) {return getDateOfBirth().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLoyaltyPoints() {return new FieldInfo(Customer.class,"loyaltyPoints",Long.class, null,p);}
	    public final String loyaltyPoints="loyaltyPoints";
	    public Specification<T> loyaltyPoints(Op op,Object value) {return getLoyaltyPoints().where(op,value);}
	    public Specification<T> loyaltyPoints(Op op,Object value,boolean chkEmpty) {return getLoyaltyPoints().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCustomerType() {return new FieldInfo(Customer.class,"customerType",CustomerType.class, null,p);}
	    public final String customerType="customerType";
	    public Specification<T> customerType(Op op,Object value) {return getCustomerType().where(op,value);}
	    public Specification<T> customerType(Op op,Object value,boolean chkEmpty) {return getCustomerType().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getBillingAddress() {return new FieldInfo(Customer.class,"billingAddress",Address.class, null,p);}
	    public final String billingAddress="billingAddress";
	    public final $Address<T> billingAddress() {return new $Address(getBillingAddress());}
	
	    public final FieldInfo<T> getVipProfile() {return new FieldInfo(Customer.class,"vipProfile",VipProfile.class, null,p);}
	    public final String vipProfile="vipProfile";
	    public final $VipProfile<T> vipProfile() {return new $VipProfile(getVipProfile());}
	
	    public final FieldInfo<T> getOrders() {return new FieldInfo(Customer.class,"orders",Order.class, List.class,p);}
	    public final String orders="orders";
	
	    public final FieldInfo<T> getCreatedBy() {return new FieldInfo(Customer.class,"createdBy",String.class, null,p);}
	    public final String createdBy="createdBy";
	    public Specification<T> createdBy(Op op,Object value) {return getCreatedBy().where(op,value);}
	    public Specification<T> createdBy(Op op,Object value,boolean chkEmpty) {return getCreatedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreateDate() {return new FieldInfo(Customer.class,"createDate",java.time.LocalDateTime.class, null,p);}
	    public final String createDate="createDate";
	    public Specification<T> createDate(Op op,Object value) {return getCreateDate().where(op,value);}
	    public Specification<T> createDate(Op op,Object value,boolean chkEmpty) {return getCreateDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedBy() {return new FieldInfo(Customer.class,"lastModifiedBy",String.class, null,p);}
	    public final String lastModifiedBy="lastModifiedBy";
	    public Specification<T> lastModifiedBy(Op op,Object value) {return getLastModifiedBy().where(op,value);}
	    public Specification<T> lastModifiedBy(Op op,Object value,boolean chkEmpty) {return getLastModifiedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedDate() {return new FieldInfo(Customer.class,"lastModifiedDate",java.time.LocalDateTime.class, null,p);}
	    public final String lastModifiedDate="lastModifiedDate";
	    public Specification<T> lastModifiedDate(Op op,Object value) {return getLastModifiedDate().where(op,value);}
	    public Specification<T> lastModifiedDate(Op op,Object value,boolean chkEmpty) {return getLastModifiedDate().where(op,value,chkEmpty);}
	}
	
	public static $VipProfile<VipProfile> VipProfile=new $VipProfile<>();
	public static class $VipProfile<T> {
	
	    private FieldInfo p=null;
	    public $VipProfile() {}
	    public $VipProfile(FieldInfo p) {this.p=p;}
	    public static JoinSpec<VipProfile> joinSpec() {
	    	return new JoinSpec<>(VipProfile.class);
	    }
	    public static UpdateSpec<VipProfile> updateSpec() {
	    	return new UpdateSpec<>(VipProfile.class);
	    }
	    public static DeleteSpec<VipProfile> deleteSpec() {
	    	return new DeleteSpec<>(VipProfile.class);
	    }
	
	    public final FieldInfo<T> getProfileId() {return new FieldInfo(VipProfile.class,"profileId",Long.class, null,p);}
	    public final String profileId="profileId";
	    public Specification<T> profileId(Op op,Object value) {return getProfileId().where(op,value);}
	    public Specification<T> profileId(Op op,Object value,boolean chkEmpty) {return getProfileId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTier() {return new FieldInfo(VipProfile.class,"tier",String.class, null,p);}
	    public final String tier="tier";
	    public Specification<T> tier(Op op,Object value) {return getTier().where(op,value);}
	    public Specification<T> tier(Op op,Object value,boolean chkEmpty) {return getTier().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getDiscountRate() {return new FieldInfo(VipProfile.class,"discountRate",Double.class, null,p);}
	    public final String discountRate="discountRate";
	    public Specification<T> discountRate(Op op,Object value) {return getDiscountRate().where(op,value);}
	    public Specification<T> discountRate(Op op,Object value,boolean chkEmpty) {return getDiscountRate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEnrollDate() {return new FieldInfo(VipProfile.class,"enrollDate",java.time.LocalDate.class, null,p);}
	    public final String enrollDate="enrollDate";
	    public Specification<T> enrollDate(Op op,Object value) {return getEnrollDate().where(op,value);}
	    public Specification<T> enrollDate(Op op,Object value,boolean chkEmpty) {return getEnrollDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCustomer() {return new FieldInfo(VipProfile.class,"customer",Customer.class, null,p);}
	    public final String customer="customer";
	    public final $Customer<T> customer() {return new $Customer(getCustomer());}
	}
	
	public static $Order<Order> Order=new $Order<>();
	public static class $Order<T> {
	
	    private FieldInfo p=null;
	    public $Order() {}
	    public $Order(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Order> joinSpec() {
	    	return new JoinSpec<>(Order.class);
	    }
	    public static UpdateSpec<Order> updateSpec() {
	    	return new UpdateSpec<>(Order.class);
	    }
	    public static DeleteSpec<Order> deleteSpec() {
	    	return new DeleteSpec<>(Order.class);
	    }
	
	    public final FieldInfo<T> getOrderId() {return new FieldInfo(Order.class,"orderId",String.class, null,p);}
	    public final String orderId="orderId";
	    public Specification<T> orderId(Op op,Object value) {return getOrderId().where(op,value);}
	    public Specification<T> orderId(Op op,Object value,boolean chkEmpty) {return getOrderId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOrderDate() {return new FieldInfo(Order.class,"orderDate",java.time.LocalDateTime.class, null,p);}
	    public final String orderDate="orderDate";
	    public Specification<T> orderDate(Op op,Object value) {return getOrderDate().where(op,value);}
	    public Specification<T> orderDate(Op op,Object value,boolean chkEmpty) {return getOrderDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTotalAmount() {return new FieldInfo(Order.class,"totalAmount",java.math.BigDecimal.class, null,p);}
	    public final String totalAmount="totalAmount";
	    public Specification<T> totalAmount(Op op,Object value) {return getTotalAmount().where(op,value);}
	    public Specification<T> totalAmount(Op op,Object value,boolean chkEmpty) {return getTotalAmount().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getStatus() {return new FieldInfo(Order.class,"status",OrderStatus.class, null,p);}
	    public final String status="status";
	    public Specification<T> status(Op op,Object value) {return getStatus().where(op,value);}
	    public Specification<T> status(Op op,Object value,boolean chkEmpty) {return getStatus().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPaymentMethod() {return new FieldInfo(Order.class,"paymentMethod",PaymentMethod.class, null,p);}
	    public final String paymentMethod="paymentMethod";
	    public Specification<T> paymentMethod(Op op,Object value) {return getPaymentMethod().where(op,value);}
	    public Specification<T> paymentMethod(Op op,Object value,boolean chkEmpty) {return getPaymentMethod().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCustomer() {return new FieldInfo(Order.class,"customer",Customer.class, null,p);}
	    public final String customer="customer";
	    public final $Customer<T> customer() {return new $Customer(getCustomer());}
	
	    public final FieldInfo<T> getItems() {return new FieldInfo(Order.class,"items",OrderItem.class, List.class,p);}
	    public final String items="items";
	
	    public final FieldInfo<T> getShippingAddress() {return new FieldInfo(Order.class,"shippingAddress",Address.class, null,p);}
	    public final String shippingAddress="shippingAddress";
	    public final $Address<T> shippingAddress() {return new $Address(getShippingAddress());}
	
	    public final FieldInfo<T> getNotes() {return new FieldInfo(Order.class,"notes",String.class, null,p);}
	    public final String notes="notes";
	    public Specification<T> notes(Op op,Object value) {return getNotes().where(op,value);}
	    public Specification<T> notes(Op op,Object value,boolean chkEmpty) {return getNotes().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTrackingNumber() {return new FieldInfo(Order.class,"trackingNumber",String.class, null,p);}
	    public final String trackingNumber="trackingNumber";
	    public Specification<T> trackingNumber(Op op,Object value) {return getTrackingNumber().where(op,value);}
	    public Specification<T> trackingNumber(Op op,Object value,boolean chkEmpty) {return getTrackingNumber().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getVersion() {return new FieldInfo(Order.class,"version",Long.class, null,p);}
	    public final String version="version";
	    public Specification<T> version(Op op,Object value) {return getVersion().where(op,value);}
	    public Specification<T> version(Op op,Object value,boolean chkEmpty) {return getVersion().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreatedBy() {return new FieldInfo(Order.class,"createdBy",String.class, null,p);}
	    public final String createdBy="createdBy";
	    public Specification<T> createdBy(Op op,Object value) {return getCreatedBy().where(op,value);}
	    public Specification<T> createdBy(Op op,Object value,boolean chkEmpty) {return getCreatedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreateDate() {return new FieldInfo(Order.class,"createDate",java.time.LocalDateTime.class, null,p);}
	    public final String createDate="createDate";
	    public Specification<T> createDate(Op op,Object value) {return getCreateDate().where(op,value);}
	    public Specification<T> createDate(Op op,Object value,boolean chkEmpty) {return getCreateDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedBy() {return new FieldInfo(Order.class,"lastModifiedBy",String.class, null,p);}
	    public final String lastModifiedBy="lastModifiedBy";
	    public Specification<T> lastModifiedBy(Op op,Object value) {return getLastModifiedBy().where(op,value);}
	    public Specification<T> lastModifiedBy(Op op,Object value,boolean chkEmpty) {return getLastModifiedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedDate() {return new FieldInfo(Order.class,"lastModifiedDate",java.time.LocalDateTime.class, null,p);}
	    public final String lastModifiedDate="lastModifiedDate";
	    public Specification<T> lastModifiedDate(Op op,Object value) {return getLastModifiedDate().where(op,value);}
	    public Specification<T> lastModifiedDate(Op op,Object value,boolean chkEmpty) {return getLastModifiedDate().where(op,value,chkEmpty);}
	}
	
	public static $OrderItem<OrderItem> OrderItem=new $OrderItem<>();
	public static class $OrderItem<T> {
	
	    private FieldInfo p=null;
	    public $OrderItem() {}
	    public $OrderItem(FieldInfo p) {this.p=p;}
	    public static JoinSpec<OrderItem> joinSpec() {
	    	return new JoinSpec<>(OrderItem.class);
	    }
	    public static UpdateSpec<OrderItem> updateSpec() {
	    	return new UpdateSpec<>(OrderItem.class);
	    }
	    public static DeleteSpec<OrderItem> deleteSpec() {
	    	return new DeleteSpec<>(OrderItem.class);
	    }
	
	    public final FieldInfo<T> getOrderItemId() {return new FieldInfo(OrderItem.class,"orderItemId",String.class, null,p);}
	    public final String orderItemId="orderItemId";
	    public Specification<T> orderItemId(Op op,Object value) {return getOrderItemId().where(op,value);}
	    public Specification<T> orderItemId(Op op,Object value,boolean chkEmpty) {return getOrderItemId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOrder() {return new FieldInfo(OrderItem.class,"order",Order.class, null,p);}
	    public final String order="order";
	    public final $Order<T> order() {return new $Order(getOrder());}
	
	    public final FieldInfo<T> getItemSeq() {return new FieldInfo(OrderItem.class,"itemSeq",Integer.class, null,p);}
	    public final String itemSeq="itemSeq";
	    public Specification<T> itemSeq(Op op,Object value) {return getItemSeq().where(op,value);}
	    public Specification<T> itemSeq(Op op,Object value,boolean chkEmpty) {return getItemSeq().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getProduct() {return new FieldInfo(OrderItem.class,"product",Product.class, null,p);}
	    public final String product="product";
	    public final $Product<T> product() {return new $Product(getProduct());}
	
	    public final FieldInfo<T> getQuantity() {return new FieldInfo(OrderItem.class,"quantity",Integer.class, null,p);}
	    public final String quantity="quantity";
	    public Specification<T> quantity(Op op,Object value) {return getQuantity().where(op,value);}
	    public Specification<T> quantity(Op op,Object value,boolean chkEmpty) {return getQuantity().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getUnitPrice() {return new FieldInfo(OrderItem.class,"unitPrice",java.math.BigDecimal.class, null,p);}
	    public final String unitPrice="unitPrice";
	    public Specification<T> unitPrice(Op op,Object value) {return getUnitPrice().where(op,value);}
	    public Specification<T> unitPrice(Op op,Object value,boolean chkEmpty) {return getUnitPrice().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getDiscount() {return new FieldInfo(OrderItem.class,"discount",java.math.BigDecimal.class, null,p);}
	    public final String discount="discount";
	    public Specification<T> discount(Op op,Object value) {return getDiscount().where(op,value);}
	    public Specification<T> discount(Op op,Object value,boolean chkEmpty) {return getDiscount().where(op,value,chkEmpty);}
	}
	
	public static $Coupon<Coupon> Coupon=new $Coupon<>();
	public static class $Coupon<T> {
	
	    private FieldInfo p=null;
	    public $Coupon() {}
	    public $Coupon(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Coupon> joinSpec() {
	    	return new JoinSpec<>(Coupon.class);
	    }
	    public static UpdateSpec<Coupon> updateSpec() {
	    	return new UpdateSpec<>(Coupon.class);
	    }
	    public static DeleteSpec<Coupon> deleteSpec() {
	    	return new DeleteSpec<>(Coupon.class);
	    }
	
	    public final FieldInfo<T> getCouponCode() {return new FieldInfo(Coupon.class,"couponCode",String.class, null,p);}
	    public final String couponCode="couponCode";
	    public Specification<T> couponCode(Op op,Object value) {return getCouponCode().where(op,value);}
	    public Specification<T> couponCode(Op op,Object value,boolean chkEmpty) {return getCouponCode().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTitle() {return new FieldInfo(Coupon.class,"title",String.class, null,p);}
	    public final String title="title";
	    public Specification<T> title(Op op,Object value) {return getTitle().where(op,value);}
	    public Specification<T> title(Op op,Object value,boolean chkEmpty) {return getTitle().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getDiscountRate() {return new FieldInfo(Coupon.class,"discountRate",Integer.class, null,p);}
	    public final String discountRate="discountRate";
	    public Specification<T> discountRate(Op op,Object value) {return getDiscountRate().where(op,value);}
	    public Specification<T> discountRate(Op op,Object value,boolean chkEmpty) {return getDiscountRate().where(op,value,chkEmpty);}
	}
	
	public static $GiftCard<GiftCard> GiftCard=new $GiftCard<>();
	public static class $GiftCard<T> {
	
	    private FieldInfo p=null;
	    public $GiftCard() {}
	    public $GiftCard(FieldInfo p) {this.p=p;}
	    public static JoinSpec<GiftCard> joinSpec() {
	    	return new JoinSpec<>(GiftCard.class);
	    }
	    public static UpdateSpec<GiftCard> updateSpec() {
	    	return new UpdateSpec<>(GiftCard.class);
	    }
	    public static DeleteSpec<GiftCard> deleteSpec() {
	    	return new DeleteSpec<>(GiftCard.class);
	    }
	
	    public final FieldInfo<T> getCardNo() {return new FieldInfo(GiftCard.class,"cardNo",String.class, null,p);}
	    public final String cardNo="cardNo";
	    public Specification<T> cardNo(Op op,Object value) {return getCardNo().where(op,value);}
	    public Specification<T> cardNo(Op op,Object value,boolean chkEmpty) {return getCardNo().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getBalance() {return new FieldInfo(GiftCard.class,"balance",java.math.BigDecimal.class, null,p);}
	    public final String balance="balance";
	    public Specification<T> balance(Op op,Object value) {return getBalance().where(op,value);}
	    public Specification<T> balance(Op op,Object value,boolean chkEmpty) {return getBalance().where(op,value,chkEmpty);}
	}
	
}
