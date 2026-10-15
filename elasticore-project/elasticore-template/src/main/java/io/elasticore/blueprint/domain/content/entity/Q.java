//ecd:1058117549H20261012161930_V3.0
package io.elasticore.blueprint.domain.content.entity;


import io.elasticore.springboot3.entity.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;
import io.elasticore.blueprint.domain.content.enums.ArticleStatus;


/**
 * This class serves as a domain-specific unified metadata holder,
 * containing field names and type information for entities.
 *
 * Generated and managed by ElastiCORE.
 */

public class Q {

	public static $ContentCategory<ContentCategory> ContentCategory=new $ContentCategory<>();
	public static class $ContentCategory<T> {
	
	    private FieldInfo p=null;
	    public $ContentCategory() {}
	    public $ContentCategory(FieldInfo p) {this.p=p;}
	    public static JoinSpec<ContentCategory> joinSpec() {
	    	return new JoinSpec<>(ContentCategory.class);
	    }
	    public static UpdateSpec<ContentCategory> updateSpec() {
	    	return new UpdateSpec<>(ContentCategory.class);
	    }
	    public static DeleteSpec<ContentCategory> deleteSpec() {
	    	return new DeleteSpec<>(ContentCategory.class);
	    }
	
	    public final FieldInfo<T> getCategoryId() {return new FieldInfo(ContentCategory.class,"categoryId",Long.class, null,p);}
	    public final String categoryId="categoryId";
	    public Specification<T> categoryId(Op op,Object value) {return getCategoryId().where(op,value);}
	    public Specification<T> categoryId(Op op,Object value,boolean chkEmpty) {return getCategoryId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getName() {return new FieldInfo(ContentCategory.class,"name",String.class, null,p);}
	    public final String name="name";
	    public Specification<T> name(Op op,Object value) {return getName().where(op,value);}
	    public Specification<T> name(Op op,Object value,boolean chkEmpty) {return getName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSlug() {return new FieldInfo(ContentCategory.class,"slug",String.class, null,p);}
	    public final String slug="slug";
	    public Specification<T> slug(Op op,Object value) {return getSlug().where(op,value);}
	    public Specification<T> slug(Op op,Object value,boolean chkEmpty) {return getSlug().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getDescription() {return new FieldInfo(ContentCategory.class,"description",String.class, null,p);}
	    public final String description="description";
	    public Specification<T> description(Op op,Object value) {return getDescription().where(op,value);}
	    public Specification<T> description(Op op,Object value,boolean chkEmpty) {return getDescription().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSortOrder() {return new FieldInfo(ContentCategory.class,"sortOrder",Integer.class, null,p);}
	    public final String sortOrder="sortOrder";
	    public Specification<T> sortOrder(Op op,Object value) {return getSortOrder().where(op,value);}
	    public Specification<T> sortOrder(Op op,Object value,boolean chkEmpty) {return getSortOrder().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getParent() {return new FieldInfo(ContentCategory.class,"parent",ContentCategory.class, null,p);}
	    public final String parent="parent";
	    public final $ContentCategory<T> parent() {return new $ContentCategory(getParent());}
	
	    public final FieldInfo<T> getChildren() {return new FieldInfo(ContentCategory.class,"children",ContentCategory.class, List.class,p);}
	    public final String children="children";
	
	    public final FieldInfo<T> getChildCount() {return new FieldInfo(ContentCategory.class,"childCount",Long.class, null,p);}
	    public final String childCount="childCount";
	    public Specification<T> childCount(Op op,Object value) {return getChildCount().where(op,value);}
	    public Specification<T> childCount(Op op,Object value,boolean chkEmpty) {return getChildCount().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getParentName() {return new FieldInfo(ContentCategory.class,"parentName",String.class, null,p);}
	    public final String parentName="parentName";
	    public Specification<T> parentName(Op op,Object value) {return getParentName().where(op,value);}
	    public Specification<T> parentName(Op op,Object value,boolean chkEmpty) {return getParentName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreatedBy() {return new FieldInfo(ContentCategory.class,"createdBy",String.class, null,p);}
	    public final String createdBy="createdBy";
	    public Specification<T> createdBy(Op op,Object value) {return getCreatedBy().where(op,value);}
	    public Specification<T> createdBy(Op op,Object value,boolean chkEmpty) {return getCreatedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreateDate() {return new FieldInfo(ContentCategory.class,"createDate",java.time.LocalDateTime.class, null,p);}
	    public final String createDate="createDate";
	    public Specification<T> createDate(Op op,Object value) {return getCreateDate().where(op,value);}
	    public Specification<T> createDate(Op op,Object value,boolean chkEmpty) {return getCreateDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedBy() {return new FieldInfo(ContentCategory.class,"lastModifiedBy",String.class, null,p);}
	    public final String lastModifiedBy="lastModifiedBy";
	    public Specification<T> lastModifiedBy(Op op,Object value) {return getLastModifiedBy().where(op,value);}
	    public Specification<T> lastModifiedBy(Op op,Object value,boolean chkEmpty) {return getLastModifiedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedDate() {return new FieldInfo(ContentCategory.class,"lastModifiedDate",java.time.LocalDateTime.class, null,p);}
	    public final String lastModifiedDate="lastModifiedDate";
	    public Specification<T> lastModifiedDate(Op op,Object value) {return getLastModifiedDate().where(op,value);}
	    public Specification<T> lastModifiedDate(Op op,Object value,boolean chkEmpty) {return getLastModifiedDate().where(op,value,chkEmpty);}
	}
	
	public static $Tag<Tag> Tag=new $Tag<>();
	public static class $Tag<T> {
	
	    private FieldInfo p=null;
	    public $Tag() {}
	    public $Tag(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Tag> joinSpec() {
	    	return new JoinSpec<>(Tag.class);
	    }
	    public static UpdateSpec<Tag> updateSpec() {
	    	return new UpdateSpec<>(Tag.class);
	    }
	    public static DeleteSpec<Tag> deleteSpec() {
	    	return new DeleteSpec<>(Tag.class);
	    }
	
	    public final FieldInfo<T> getTagId() {return new FieldInfo(Tag.class,"tagId",Long.class, null,p);}
	    public final String tagId="tagId";
	    public Specification<T> tagId(Op op,Object value) {return getTagId().where(op,value);}
	    public Specification<T> tagId(Op op,Object value,boolean chkEmpty) {return getTagId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getName() {return new FieldInfo(Tag.class,"name",String.class, null,p);}
	    public final String name="name";
	    public Specification<T> name(Op op,Object value) {return getName().where(op,value);}
	    public Specification<T> name(Op op,Object value,boolean chkEmpty) {return getName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getColor() {return new FieldInfo(Tag.class,"color",String.class, null,p);}
	    public final String color="color";
	    public Specification<T> color(Op op,Object value) {return getColor().where(op,value);}
	    public Specification<T> color(Op op,Object value,boolean chkEmpty) {return getColor().where(op,value,chkEmpty);}
	}
	
	public static $Article<Article> Article=new $Article<>();
	public static class $Article<T> {
	
	    private FieldInfo p=null;
	    public $Article() {}
	    public $Article(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Article> joinSpec() {
	    	return new JoinSpec<>(Article.class);
	    }
	    public static UpdateSpec<Article> updateSpec() {
	    	return new UpdateSpec<>(Article.class);
	    }
	    public static DeleteSpec<Article> deleteSpec() {
	    	return new DeleteSpec<>(Article.class);
	    }
	
	    public final FieldInfo<T> getArticleId() {return new FieldInfo(Article.class,"articleId",String.class, null,p);}
	    public final String articleId="articleId";
	    public Specification<T> articleId(Op op,Object value) {return getArticleId().where(op,value);}
	    public Specification<T> articleId(Op op,Object value,boolean chkEmpty) {return getArticleId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTitle() {return new FieldInfo(Article.class,"title",String.class, null,p);}
	    public final String title="title";
	    public Specification<T> title(Op op,Object value) {return getTitle().where(op,value);}
	    public Specification<T> title(Op op,Object value,boolean chkEmpty) {return getTitle().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSlug() {return new FieldInfo(Article.class,"slug",String.class, null,p);}
	    public final String slug="slug";
	    public Specification<T> slug(Op op,Object value) {return getSlug().where(op,value);}
	    public Specification<T> slug(Op op,Object value,boolean chkEmpty) {return getSlug().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSummary() {return new FieldInfo(Article.class,"summary",String.class, null,p);}
	    public final String summary="summary";
	    public Specification<T> summary(Op op,Object value) {return getSummary().where(op,value);}
	    public Specification<T> summary(Op op,Object value,boolean chkEmpty) {return getSummary().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getBody() {return new FieldInfo(Article.class,"body",String.class, null,p);}
	    public final String body="body";
	    public Specification<T> body(Op op,Object value) {return getBody().where(op,value);}
	    public Specification<T> body(Op op,Object value,boolean chkEmpty) {return getBody().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getBodyText() {return new FieldInfo(Article.class,"bodyText",String.class, null,p);}
	    public final String bodyText="bodyText";
	    public Specification<T> bodyText(Op op,Object value) {return getBodyText().where(op,value);}
	    public Specification<T> bodyText(Op op,Object value,boolean chkEmpty) {return getBodyText().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPublishDate() {return new FieldInfo(Article.class,"publishDate",java.time.LocalDateTime.class, null,p);}
	    public final String publishDate="publishDate";
	    public Specification<T> publishDate(Op op,Object value) {return getPublishDate().where(op,value);}
	    public Specification<T> publishDate(Op op,Object value,boolean chkEmpty) {return getPublishDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getViewCount() {return new FieldInfo(Article.class,"viewCount",Long.class, null,p);}
	    public final String viewCount="viewCount";
	    public Specification<T> viewCount(Op op,Object value) {return getViewCount().where(op,value);}
	    public Specification<T> viewCount(Op op,Object value,boolean chkEmpty) {return getViewCount().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getFeatured() {return new FieldInfo(Article.class,"featured",Boolean.class, null,p);}
	    public final String featured="featured";
	    public Specification<T> featured(Op op,Object value) {return getFeatured().where(op,value);}
	    public Specification<T> featured(Op op,Object value,boolean chkEmpty) {return getFeatured().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getArticleStatus() {return new FieldInfo(Article.class,"articleStatus",ArticleStatus.class, null,p);}
	    public final String articleStatus="articleStatus";
	    public Specification<T> articleStatus(Op op,Object value) {return getArticleStatus().where(op,value);}
	    public Specification<T> articleStatus(Op op,Object value,boolean chkEmpty) {return getArticleStatus().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCategory() {return new FieldInfo(Article.class,"category",ContentCategory.class, null,p);}
	    public final String category="category";
	    public final $ContentCategory<T> category() {return new $ContentCategory(getCategory());}
	
	    public final FieldInfo<T> getTags() {return new FieldInfo(Article.class,"tags",Tag.class, List.class,p);}
	    public final String tags="tags";
	
	    public final FieldInfo<T> getAttachments() {return new FieldInfo(Article.class,"attachments",Attachment.class, List.class,p);}
	    public final String attachments="attachments";
	
	    public final FieldInfo<T> getRatings() {return new FieldInfo(Article.class,"ratings",Integer[].class, null,p);}
	    public final String ratings="ratings";
	    public Specification<T> ratings(Op op,Object value) {return getRatings().where(op,value);}
	    public Specification<T> ratings(Op op,Object value,boolean chkEmpty) {return getRatings().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLabels() {return new FieldInfo(Article.class,"labels",String[].class, null,p);}
	    public final String labels="labels";
	    public Specification<T> labels(Op op,Object value) {return getLabels().where(op,value);}
	    public Specification<T> labels(Op op,Object value,boolean chkEmpty) {return getLabels().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreatedBy() {return new FieldInfo(Article.class,"createdBy",String.class, null,p);}
	    public final String createdBy="createdBy";
	    public Specification<T> createdBy(Op op,Object value) {return getCreatedBy().where(op,value);}
	    public Specification<T> createdBy(Op op,Object value,boolean chkEmpty) {return getCreatedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreateDate() {return new FieldInfo(Article.class,"createDate",java.time.LocalDateTime.class, null,p);}
	    public final String createDate="createDate";
	    public Specification<T> createDate(Op op,Object value) {return getCreateDate().where(op,value);}
	    public Specification<T> createDate(Op op,Object value,boolean chkEmpty) {return getCreateDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedBy() {return new FieldInfo(Article.class,"lastModifiedBy",String.class, null,p);}
	    public final String lastModifiedBy="lastModifiedBy";
	    public Specification<T> lastModifiedBy(Op op,Object value) {return getLastModifiedBy().where(op,value);}
	    public Specification<T> lastModifiedBy(Op op,Object value,boolean chkEmpty) {return getLastModifiedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedDate() {return new FieldInfo(Article.class,"lastModifiedDate",java.time.LocalDateTime.class, null,p);}
	    public final String lastModifiedDate="lastModifiedDate";
	    public Specification<T> lastModifiedDate(Op op,Object value) {return getLastModifiedDate().where(op,value);}
	    public Specification<T> lastModifiedDate(Op op,Object value,boolean chkEmpty) {return getLastModifiedDate().where(op,value,chkEmpty);}
	}
	
	public static $Comment<Comment> Comment=new $Comment<>();
	public static class $Comment<T> {
	
	    private FieldInfo p=null;
	    public $Comment() {}
	    public $Comment(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Comment> joinSpec() {
	    	return new JoinSpec<>(Comment.class);
	    }
	    public static UpdateSpec<Comment> updateSpec() {
	    	return new UpdateSpec<>(Comment.class);
	    }
	    public static DeleteSpec<Comment> deleteSpec() {
	    	return new DeleteSpec<>(Comment.class);
	    }
	
	    public final FieldInfo<T> getCommentId() {return new FieldInfo(Comment.class,"commentId",Long.class, null,p);}
	    public final String commentId="commentId";
	    public Specification<T> commentId(Op op,Object value) {return getCommentId().where(op,value);}
	    public Specification<T> commentId(Op op,Object value,boolean chkEmpty) {return getCommentId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getContent() {return new FieldInfo(Comment.class,"content",String.class, null,p);}
	    public final String content="content";
	    public Specification<T> content(Op op,Object value) {return getContent().where(op,value);}
	    public Specification<T> content(Op op,Object value,boolean chkEmpty) {return getContent().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getAuthorName() {return new FieldInfo(Comment.class,"authorName",String.class, null,p);}
	    public final String authorName="authorName";
	    public Specification<T> authorName(Op op,Object value) {return getAuthorName().where(op,value);}
	    public Specification<T> authorName(Op op,Object value,boolean chkEmpty) {return getAuthorName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getAuthorEmail() {return new FieldInfo(Comment.class,"authorEmail",String.class, null,p);}
	    public final String authorEmail="authorEmail";
	    public Specification<T> authorEmail(Op op,Object value) {return getAuthorEmail().where(op,value);}
	    public Specification<T> authorEmail(Op op,Object value,boolean chkEmpty) {return getAuthorEmail().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getApproved() {return new FieldInfo(Comment.class,"approved",Boolean.class, null,p);}
	    public final String approved="approved";
	    public Specification<T> approved(Op op,Object value) {return getApproved().where(op,value);}
	    public Specification<T> approved(Op op,Object value,boolean chkEmpty) {return getApproved().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getArticle() {return new FieldInfo(Comment.class,"article",Article.class, null,p);}
	    public final String article="article";
	    public final $Article<T> article() {return new $Article(getArticle());}
	
	    public final FieldInfo<T> getParentComment() {return new FieldInfo(Comment.class,"parentComment",Comment.class, null,p);}
	    public final String parentComment="parentComment";
	    public final $Comment<T> parentComment() {return new $Comment(getParentComment());}
	
	    public final FieldInfo<T> getReplies() {return new FieldInfo(Comment.class,"replies",Comment.class, List.class,p);}
	    public final String replies="replies";
	
	    public final FieldInfo<T> getCreatedBy() {return new FieldInfo(Comment.class,"createdBy",String.class, null,p);}
	    public final String createdBy="createdBy";
	    public Specification<T> createdBy(Op op,Object value) {return getCreatedBy().where(op,value);}
	    public Specification<T> createdBy(Op op,Object value,boolean chkEmpty) {return getCreatedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCreateDate() {return new FieldInfo(Comment.class,"createDate",java.time.LocalDateTime.class, null,p);}
	    public final String createDate="createDate";
	    public Specification<T> createDate(Op op,Object value) {return getCreateDate().where(op,value);}
	    public Specification<T> createDate(Op op,Object value,boolean chkEmpty) {return getCreateDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedBy() {return new FieldInfo(Comment.class,"lastModifiedBy",String.class, null,p);}
	    public final String lastModifiedBy="lastModifiedBy";
	    public Specification<T> lastModifiedBy(Op op,Object value) {return getLastModifiedBy().where(op,value);}
	    public Specification<T> lastModifiedBy(Op op,Object value,boolean chkEmpty) {return getLastModifiedBy().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLastModifiedDate() {return new FieldInfo(Comment.class,"lastModifiedDate",java.time.LocalDateTime.class, null,p);}
	    public final String lastModifiedDate="lastModifiedDate";
	    public Specification<T> lastModifiedDate(Op op,Object value) {return getLastModifiedDate().where(op,value);}
	    public Specification<T> lastModifiedDate(Op op,Object value,boolean chkEmpty) {return getLastModifiedDate().where(op,value,chkEmpty);}
	}
	
	public static $Attachment<Attachment> Attachment=new $Attachment<>();
	public static class $Attachment<T> {
	
	    private FieldInfo p=null;
	    public $Attachment() {}
	    public $Attachment(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Attachment> joinSpec() {
	    	return new JoinSpec<>(Attachment.class);
	    }
	    public static UpdateSpec<Attachment> updateSpec() {
	    	return new UpdateSpec<>(Attachment.class);
	    }
	    public static DeleteSpec<Attachment> deleteSpec() {
	    	return new DeleteSpec<>(Attachment.class);
	    }
	
	    public final FieldInfo<T> getAttachId() {return new FieldInfo(Attachment.class,"attachId",Long.class, null,p);}
	    public final String attachId="attachId";
	    public Specification<T> attachId(Op op,Object value) {return getAttachId().where(op,value);}
	    public Specification<T> attachId(Op op,Object value,boolean chkEmpty) {return getAttachId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getFileName() {return new FieldInfo(Attachment.class,"fileName",String.class, null,p);}
	    public final String fileName="fileName";
	    public Specification<T> fileName(Op op,Object value) {return getFileName().where(op,value);}
	    public Specification<T> fileName(Op op,Object value,boolean chkEmpty) {return getFileName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getFileSize() {return new FieldInfo(Attachment.class,"fileSize",Long.class, null,p);}
	    public final String fileSize="fileSize";
	    public Specification<T> fileSize(Op op,Object value) {return getFileSize().where(op,value);}
	    public Specification<T> fileSize(Op op,Object value,boolean chkEmpty) {return getFileSize().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getContentType() {return new FieldInfo(Attachment.class,"contentType",String.class, null,p);}
	    public final String contentType="contentType";
	    public Specification<T> contentType(Op op,Object value) {return getContentType().where(op,value);}
	    public Specification<T> contentType(Op op,Object value,boolean chkEmpty) {return getContentType().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getStorageKey() {return new FieldInfo(Attachment.class,"storageKey",String.class, null,p);}
	    public final String storageKey="storageKey";
	    public Specification<T> storageKey(Op op,Object value) {return getStorageKey().where(op,value);}
	    public Specification<T> storageKey(Op op,Object value,boolean chkEmpty) {return getStorageKey().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getArticle() {return new FieldInfo(Attachment.class,"article",Article.class, null,p);}
	    public final String article="article";
	    public final $Article<T> article() {return new $Article(getArticle());}
	}
	
	public static $Bookmark<Bookmark> Bookmark=new $Bookmark<>();
	public static class $Bookmark<T> {
	
	    private FieldInfo p=null;
	    public $Bookmark() {}
	    public $Bookmark(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Bookmark> joinSpec() {
	    	return new JoinSpec<>(Bookmark.class);
	    }
	    public static UpdateSpec<Bookmark> updateSpec() {
	    	return new UpdateSpec<>(Bookmark.class);
	    }
	    public static DeleteSpec<Bookmark> deleteSpec() {
	    	return new DeleteSpec<>(Bookmark.class);
	    }
	
	    public final FieldInfo<T> getBookmarkId() {return new FieldInfo(Bookmark.class,"bookmarkId",Long.class, null,p);}
	    public final String bookmarkId="bookmarkId";
	    public Specification<T> bookmarkId(Op op,Object value) {return getBookmarkId().where(op,value);}
	    public Specification<T> bookmarkId(Op op,Object value,boolean chkEmpty) {return getBookmarkId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getMemo() {return new FieldInfo(Bookmark.class,"memo",String.class, null,p);}
	    public final String memo="memo";
	    public Specification<T> memo(Op op,Object value) {return getMemo().where(op,value);}
	    public Specification<T> memo(Op op,Object value,boolean chkEmpty) {return getMemo().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getArticle() {return new FieldInfo(Bookmark.class,"article",Article.class, null,p);}
	    public final String article="article";
	    public final $Article<T> article() {return new $Article(getArticle());}
	}
	
}
