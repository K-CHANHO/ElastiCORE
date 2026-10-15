//ecd:-1517061205H20260328122051_V2.0
package io.elasticore.blueprint.domain.jsonview_demo.entity;


import io.elasticore.springboot3.entity.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;



/**
 * This class serves as a domain-specific unified metadata holder,
 * containing field names and type information for entities.
 *
 * Generated and managed by ElastiCORE.
 */

public class Q {

	public static $BlogPost<BlogPost> BlogPost=new $BlogPost<>();
	public static class $BlogPost<T> {
	
	    private FieldInfo p=null;
	    public $BlogPost() {}
	    public $BlogPost(FieldInfo p) {this.p=p;}
	    public static JoinSpec<BlogPost> joinSpec() {
	    	return new JoinSpec<>(BlogPost.class);
	    }
	    public static UpdateSpec<BlogPost> updateSpec() {
	    	return new UpdateSpec<>(BlogPost.class);
	    }
	    public static DeleteSpec<BlogPost> deleteSpec() {
	    	return new DeleteSpec<>(BlogPost.class);
	    }
	
	    public final FieldInfo<T> getId() {return new FieldInfo(BlogPost.class,"id",Long.class, null,p);}
	    public final String id="id";
	    public Specification<T> id(Op op,Object value) {return getId().where(op,value);}
	    public Specification<T> id(Op op,Object value,boolean chkEmpty) {return getId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTitle() {return new FieldInfo(BlogPost.class,"title",String.class, null,p);}
	    public final String title="title";
	    public Specification<T> title(Op op,Object value) {return getTitle().where(op,value);}
	    public Specification<T> title(Op op,Object value,boolean chkEmpty) {return getTitle().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getContent() {return new FieldInfo(BlogPost.class,"content",String.class, null,p);}
	    public final String content="content";
	    public Specification<T> content(Op op,Object value) {return getContent().where(op,value);}
	    public Specification<T> content(Op op,Object value,boolean chkEmpty) {return getContent().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSummary() {return new FieldInfo(BlogPost.class,"summary",String.class, null,p);}
	    public final String summary="summary";
	    public Specification<T> summary(Op op,Object value) {return getSummary().where(op,value);}
	    public Specification<T> summary(Op op,Object value,boolean chkEmpty) {return getSummary().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getStatus() {return new FieldInfo(BlogPost.class,"status",String.class, null,p);}
	    public final String status="status";
	    public Specification<T> status(Op op,Object value) {return getStatus().where(op,value);}
	    public Specification<T> status(Op op,Object value,boolean chkEmpty) {return getStatus().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getViewCount() {return new FieldInfo(BlogPost.class,"viewCount",Integer.class, null,p);}
	    public final String viewCount="viewCount";
	    public Specification<T> viewCount(Op op,Object value) {return getViewCount().where(op,value);}
	    public Specification<T> viewCount(Op op,Object value,boolean chkEmpty) {return getViewCount().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getAuthor() {return new FieldInfo(BlogPost.class,"author",String.class, null,p);}
	    public final String author="author";
	    public Specification<T> author(Op op,Object value) {return getAuthor().where(op,value);}
	    public Specification<T> author(Op op,Object value,boolean chkEmpty) {return getAuthor().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPublishedAt() {return new FieldInfo(BlogPost.class,"publishedAt",java.time.LocalDateTime.class, null,p);}
	    public final String publishedAt="publishedAt";
	    public Specification<T> publishedAt(Op op,Object value) {return getPublishedAt().where(op,value);}
	    public Specification<T> publishedAt(Op op,Object value,boolean chkEmpty) {return getPublishedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getModifiedAt() {return new FieldInfo(BlogPost.class,"modifiedAt",java.time.LocalDateTime.class, null,p);}
	    public final String modifiedAt="modifiedAt";
	    public Specification<T> modifiedAt(Op op,Object value) {return getModifiedAt().where(op,value);}
	    public Specification<T> modifiedAt(Op op,Object value,boolean chkEmpty) {return getModifiedAt().where(op,value,chkEmpty);}
	}
	
	public static $Member<Member> Member=new $Member<>();
	public static class $Member<T> {
	
	    private FieldInfo p=null;
	    public $Member() {}
	    public $Member(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Member> joinSpec() {
	    	return new JoinSpec<>(Member.class);
	    }
	    public static UpdateSpec<Member> updateSpec() {
	    	return new UpdateSpec<>(Member.class);
	    }
	    public static DeleteSpec<Member> deleteSpec() {
	    	return new DeleteSpec<>(Member.class);
	    }
	
	    public final FieldInfo<T> getId() {return new FieldInfo(Member.class,"id",Long.class, null,p);}
	    public final String id="id";
	    public Specification<T> id(Op op,Object value) {return getId().where(op,value);}
	    public Specification<T> id(Op op,Object value,boolean chkEmpty) {return getId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getUsername() {return new FieldInfo(Member.class,"username",String.class, null,p);}
	    public final String username="username";
	    public Specification<T> username(Op op,Object value) {return getUsername().where(op,value);}
	    public Specification<T> username(Op op,Object value,boolean chkEmpty) {return getUsername().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPassword() {return new FieldInfo(Member.class,"password",String.class, null,p);}
	    public final String password="password";
	    public Specification<T> password(Op op,Object value) {return getPassword().where(op,value);}
	    public Specification<T> password(Op op,Object value,boolean chkEmpty) {return getPassword().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEmail() {return new FieldInfo(Member.class,"email",String.class, null,p);}
	    public final String email="email";
	    public Specification<T> email(Op op,Object value) {return getEmail().where(op,value);}
	    public Specification<T> email(Op op,Object value,boolean chkEmpty) {return getEmail().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getNickname() {return new FieldInfo(Member.class,"nickname",String.class, null,p);}
	    public final String nickname="nickname";
	    public Specification<T> nickname(Op op,Object value) {return getNickname().where(op,value);}
	    public Specification<T> nickname(Op op,Object value,boolean chkEmpty) {return getNickname().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getRole() {return new FieldInfo(Member.class,"role",String.class, null,p);}
	    public final String role="role";
	    public Specification<T> role(Op op,Object value) {return getRole().where(op,value);}
	    public Specification<T> role(Op op,Object value,boolean chkEmpty) {return getRole().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLoginCount() {return new FieldInfo(Member.class,"loginCount",Integer.class, null,p);}
	    public final String loginCount="loginCount";
	    public Specification<T> loginCount(Op op,Object value) {return getLoginCount().where(op,value);}
	    public Specification<T> loginCount(Op op,Object value,boolean chkEmpty) {return getLoginCount().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastLoginAt() {return new FieldInfo(Member.class,"lastLoginAt",java.time.LocalDateTime.class, null,p);}
	    public final String lastLoginAt="lastLoginAt";
	    public Specification<T> lastLoginAt(Op op,Object value) {return getLastLoginAt().where(op,value);}
	    public Specification<T> lastLoginAt(Op op,Object value,boolean chkEmpty) {return getLastLoginAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getBio() {return new FieldInfo(Member.class,"bio",String.class, null,p);}
	    public final String bio="bio";
	    public Specification<T> bio(Op op,Object value) {return getBio().where(op,value);}
	    public Specification<T> bio(Op op,Object value,boolean chkEmpty) {return getBio().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreatedAt() {return new FieldInfo(Member.class,"createdAt",java.time.LocalDateTime.class, null,p);}
	    public final String createdAt="createdAt";
	    public Specification<T> createdAt(Op op,Object value) {return getCreatedAt().where(op,value);}
	    public Specification<T> createdAt(Op op,Object value,boolean chkEmpty) {return getCreatedAt().where(op,value,chkEmpty);}
	}
	
}
