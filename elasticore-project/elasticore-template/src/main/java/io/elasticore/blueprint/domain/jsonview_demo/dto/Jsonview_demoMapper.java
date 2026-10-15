//ecd:-682675414H20260328122051_V2.0
package io.elasticore.blueprint.domain.jsonview_demo.dto;

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
import io.elasticore.blueprint.domain.jsonview_demo.entity.*;
import io.elasticore.blueprint.domain.jsonview_demo.dto.*;



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


public class Jsonview_demoMapper {

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

    
    public static void mapping(BlogPost from, BlogPostDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(BlogPost from, BlogPostDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("id").checkEnable())
        setVal(from.getId(), to::setId, isSkipNull);
        if(c==null || c.fd("title").checkEnable())
        setVal(from.getTitle(), to::setTitle, isSkipNull);
        if(c==null || c.fd("content").checkEnable())
        setVal(from.getContent(), to::setContent, isSkipNull);
        if(c==null || c.fd("summary").checkEnable())
        setVal(from.getSummary(), to::setSummary, isSkipNull);
        if(c==null || c.fd("status").checkEnable())
        setVal(from.getStatus(), to::setStatus, isSkipNull);
        if(c==null || c.fd("viewCount").checkEnable())
        setVal(from.getViewCount(), to::setViewCount, isSkipNull);
        if(c==null || c.fd("author").checkEnable())
        setVal(from.getAuthor(), to::setAuthor, isSkipNull);
        if(c==null || c.fd("publishedAt").checkEnable())
        setVal(from.getPublishedAt(), to::setPublishedAt, isSkipNull);
        if(c==null || c.fd("modifiedAt").checkEnable())
        setVal(from.getModifiedAt(), to::setModifiedAt, isSkipNull);
    }
    
    
    public static void mapping(BlogPost from, BlogPostDTO to){
        mapping(from,to,false);
    }
    public static void mapping(BlogPost from, BlogPostDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static BlogPostDTO toDTO(BlogPost from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static BlogPostDTO toDTO(BlogPost from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        BlogPostDTO to = new BlogPostDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<BlogPostDTO> toBlogPostDTOList(List<BlogPost> fromList){
        return toBlogPostDTOList(fromList,(MappingContext) null);
    }
    public static List<BlogPostDTO> toBlogPostDTOList(List<BlogPost> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->Jsonview_demoMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<BlogPostDTO> toBlogPostDTOList(List<BlogPost> fromList, BiFunction<BlogPost, BlogPostDTO, BlogPostDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                BlogPostDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(BlogPost from, PostSummaryDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(BlogPost from, PostSummaryDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("id").checkEnable())
        setVal(from.getId(), to::setId, isSkipNull);
        if(c==null || c.fd("title").checkEnable())
        setVal(from.getTitle(), to::setTitle, isSkipNull);
        if(c==null || c.fd("summary").checkEnable())
        setVal(from.getSummary(), to::setSummary, isSkipNull);
        if(c==null || c.fd("status").checkEnable())
        setVal(from.getStatus(), to::setStatus, isSkipNull);
        if(c==null || c.fd("viewCount").checkEnable())
        setVal(from.getViewCount(), to::setViewCount, isSkipNull);
        if(c==null || c.fd("author").checkEnable())
        setVal(from.getAuthor(), to::setAuthor, isSkipNull);
        if(c==null || c.fd("publishedAt").checkEnable())
        setVal(from.getPublishedAt(), to::setPublishedAt, isSkipNull);
        if(c==null || c.fd("modifiedAt").checkEnable())
        setVal(from.getModifiedAt(), to::setModifiedAt, isSkipNull);
    }
    
    
    public static void mapping(BlogPost from, PostSummaryDTO to){
        mapping(from,to,false);
    }
    public static void mapping(BlogPost from, PostSummaryDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static PostSummaryDTO toPostSummaryDTO(BlogPost from){
        return toPostSummaryDTO(from,MappingContext.withGuard(2,null));
    }
    public static PostSummaryDTO toPostSummaryDTO(BlogPost from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        PostSummaryDTO to = new PostSummaryDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<PostSummaryDTO> toPostSummaryDTOList(List<BlogPost> fromList){
        return toPostSummaryDTOList(fromList,(MappingContext) null);
    }
    public static List<PostSummaryDTO> toPostSummaryDTOList(List<BlogPost> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->Jsonview_demoMapper.toPostSummaryDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<PostSummaryDTO> toPostSummaryDTOList(List<BlogPost> fromList, BiFunction<BlogPost, PostSummaryDTO, PostSummaryDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                PostSummaryDTO to = toPostSummaryDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(Member from, MemberDTO to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(Member from, MemberDTO to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("id").checkEnable())
        setVal(from.getId(), to::setId, isSkipNull);
        if(c==null || c.fd("username").checkEnable())
        setVal(from.getUsername(), to::setUsername, isSkipNull);
        if(c==null || c.fd("email").checkEnable())
        setVal(from.getEmail(), to::setEmail, isSkipNull);
        if(c==null || c.fd("nickname").checkEnable())
        setVal(from.getNickname(), to::setNickname, isSkipNull);
        if(c==null || c.fd("role").checkEnable())
        setVal(from.getRole(), to::setRole, isSkipNull);
        if(c==null || c.fd("loginCount").checkEnable())
        setVal(from.getLoginCount(), to::setLoginCount, isSkipNull);
        if(c==null || c.fd("lastLoginAt").checkEnable())
        setVal(from.getLastLoginAt(), to::setLastLoginAt, isSkipNull);
        if(c==null || c.fd("bio").checkEnable())
        setVal(from.getBio(), to::setBio, isSkipNull);
        if(c==null || c.fd("createdAt").checkEnable())
        setVal(from.getCreatedAt(), to::setCreatedAt, isSkipNull);
    }
    
    
    public static void mapping(Member from, MemberDTO to){
        mapping(from,to,false);
    }
    public static void mapping(Member from, MemberDTO to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static MemberDTO toDTO(Member from){
        return toDTO(from,MappingContext.withGuard(2,null));
    }
    public static MemberDTO toDTO(Member from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        MemberDTO to = new MemberDTO();
        mapping(from,to,c);
        return to;
    }
    
    
    public static List<MemberDTO> toMemberDTOList(List<Member> fromList){
        return toMemberDTOList(fromList,(MappingContext) null);
    }
    public static List<MemberDTO> toMemberDTOList(List<Member> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->Jsonview_demoMapper.toDTO(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<MemberDTO> toMemberDTOList(List<Member> fromList, BiFunction<Member, MemberDTO, MemberDTO> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                MemberDTO to = toDTO(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(BlogPostDTO from, BlogPost to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(BlogPostDTO from, BlogPost to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("id").checkEnable())
        setVal(from.getId(), to::setId, isSkipNull);
        if(c==null || c.fd("title").checkEnable())
        setVal(from.getTitle(), to::setTitle, isSkipNull);
        if(c==null || c.fd("content").checkEnable())
        setVal(from.getContent(), to::setContent, isSkipNull);
        if(c==null || c.fd("summary").checkEnable())
        setVal(from.getSummary(), to::setSummary, isSkipNull);
        if(c==null || c.fd("status").checkEnable())
        setVal(from.getStatus(), to::setStatus, isSkipNull);
        if(c==null || c.fd("viewCount").checkEnable())
        setVal(from.getViewCount(), to::setViewCount, isSkipNull);
        if(c==null || c.fd("author").checkEnable())
        setVal(from.getAuthor(), to::setAuthor, isSkipNull);
        if(c==null || c.fd("publishedAt").checkEnable())
        setVal(from.getPublishedAt(), to::setPublishedAt, isSkipNull);
        if(c==null || c.fd("modifiedAt").checkEnable())
        setVal(from.getModifiedAt(), to::setModifiedAt, isSkipNull);
    }
    
    
    public static void mapping(BlogPostDTO from, BlogPost to){
        mapping(from,to,false);
    }
    public static void mapping(BlogPostDTO from, BlogPost to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(BlogPostDTO from, BlogPost to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(BlogPostDTO from, BlogPost to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(BlogPostDTO from, BlogPost to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(BlogPostDTO from, BlogPost to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("id").checkEnable())
        setVal(from.getId(), to::setId, isSkipNull);
        if(c==null || c.fd("title").checkEnable())
        setVal(from.getTitle(), to::setTitle, isSkipNull);
        if(c==null || c.fd("content").checkEnable())
        setVal(from.getContent(), to::setContent, isSkipNull);
        if(c==null || c.fd("summary").checkEnable())
        setVal(from.getSummary(), to::setSummary, isSkipNull);
        if(c==null || c.fd("status").checkEnable())
        setVal(from.getStatus(), to::setStatus, isSkipNull);
        if(c==null || c.fd("viewCount").checkEnable())
        setVal(from.getViewCount(), to::setViewCount, isSkipNull);
        if(c==null || c.fd("author").checkEnable())
        setVal(from.getAuthor(), to::setAuthor, isSkipNull);
        if(c==null || c.fd("publishedAt").checkEnable())
        setVal(from.getPublishedAt(), to::setPublishedAt, isSkipNull);
        if(c==null || c.fd("modifiedAt").checkEnable())
        setVal(from.getModifiedAt(), to::setModifiedAt, isSkipNull);
    }
    
    
    public static BlogPost toEntity(BlogPostDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static BlogPost toEntity(BlogPostDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        BlogPost to = new BlogPost();
        mapping(from,to,c);
        return to;
    }
    
    
    public static BlogPost toEntity(BlogPostDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static BlogPost toEntity(BlogPostDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        BlogPost to = new BlogPost();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<BlogPost> toBlogPostList(List<BlogPostDTO> fromList){
        return toBlogPostList(fromList,(MappingContext) null);
    }
    public static List<BlogPost> toBlogPostList(List<BlogPostDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->Jsonview_demoMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<BlogPost> toBlogPostList(List<BlogPostDTO> fromList, EntityManager em){
        return toBlogPostList(fromList,(MappingContext) null,em);
    }
    public static List<BlogPost> toBlogPostList(List<BlogPostDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->Jsonview_demoMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<BlogPost> toBlogPostList(List<BlogPostDTO> fromList, BiFunction<BlogPostDTO, BlogPost, BlogPost> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                BlogPost to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(MemberDTO from, Member to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(MemberDTO from, Member to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("id").checkEnable())
        setVal(from.getId(), to::setId, isSkipNull);
        if(c==null || c.fd("username").checkEnable())
        setVal(from.getUsername(), to::setUsername, isSkipNull);
        if(c==null || c.fd("email").checkEnable())
        setVal(from.getEmail(), to::setEmail, isSkipNull);
        if(c==null || c.fd("nickname").checkEnable())
        setVal(from.getNickname(), to::setNickname, isSkipNull);
        if(c==null || c.fd("role").checkEnable())
        setVal(from.getRole(), to::setRole, isSkipNull);
        if(c==null || c.fd("loginCount").checkEnable())
        setVal(from.getLoginCount(), to::setLoginCount, isSkipNull);
        if(c==null || c.fd("lastLoginAt").checkEnable())
        setVal(from.getLastLoginAt(), to::setLastLoginAt, isSkipNull);
        if(c==null || c.fd("bio").checkEnable())
        setVal(from.getBio(), to::setBio, isSkipNull);
        if(c==null || c.fd("createdAt").checkEnable())
        setVal(from.getCreatedAt(), to::setCreatedAt, isSkipNull);
    }
    
    
    public static void mapping(MemberDTO from, Member to){
        mapping(from,to,false);
    }
    public static void mapping(MemberDTO from, Member to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(MemberDTO from, Member to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(MemberDTO from, Member to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(MemberDTO from, Member to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(MemberDTO from, Member to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("id").checkEnable())
        setVal(from.getId(), to::setId, isSkipNull);
        if(c==null || c.fd("username").checkEnable())
        setVal(from.getUsername(), to::setUsername, isSkipNull);
        if(c==null || c.fd("email").checkEnable())
        setVal(from.getEmail(), to::setEmail, isSkipNull);
        if(c==null || c.fd("nickname").checkEnable())
        setVal(from.getNickname(), to::setNickname, isSkipNull);
        if(c==null || c.fd("role").checkEnable())
        setVal(from.getRole(), to::setRole, isSkipNull);
        if(c==null || c.fd("loginCount").checkEnable())
        setVal(from.getLoginCount(), to::setLoginCount, isSkipNull);
        if(c==null || c.fd("lastLoginAt").checkEnable())
        setVal(from.getLastLoginAt(), to::setLastLoginAt, isSkipNull);
        if(c==null || c.fd("bio").checkEnable())
        setVal(from.getBio(), to::setBio, isSkipNull);
        if(c==null || c.fd("createdAt").checkEnable())
        setVal(from.getCreatedAt(), to::setCreatedAt, isSkipNull);
    }
    
    
    public static Member toEntity(MemberDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static Member toEntity(MemberDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Member to = new Member();
        mapping(from,to,c);
        return to;
    }
    
    
    public static Member toEntity(MemberDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static Member toEntity(MemberDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        Member to = new Member();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<Member> toMemberList(List<MemberDTO> fromList){
        return toMemberList(fromList,(MappingContext) null);
    }
    public static List<Member> toMemberList(List<MemberDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->Jsonview_demoMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<Member> toMemberList(List<MemberDTO> fromList, EntityManager em){
        return toMemberList(fromList,(MappingContext) null,em);
    }
    public static List<Member> toMemberList(List<MemberDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->Jsonview_demoMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<Member> toMemberList(List<MemberDTO> fromList, BiFunction<MemberDTO, Member, Member> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                Member to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static void mapping(PostSummaryDTO from, BlogPost to, boolean isSkipNull){
        mapping(from,to,isSkipNull,(MappingContext) null);
    }
    public static void mapping(PostSummaryDTO from, BlogPost to, boolean isSkipNull, MappingContext c){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("id").checkEnable())
        setVal(from.getId(), to::setId, isSkipNull);
        if(c==null || c.fd("title").checkEnable())
        setVal(from.getTitle(), to::setTitle, isSkipNull);
        if(c==null || c.fd("summary").checkEnable())
        setVal(from.getSummary(), to::setSummary, isSkipNull);
        if(c==null || c.fd("status").checkEnable())
        setVal(from.getStatus(), to::setStatus, isSkipNull);
        if(c==null || c.fd("viewCount").checkEnable())
        setVal(from.getViewCount(), to::setViewCount, isSkipNull);
        if(c==null || c.fd("author").checkEnable())
        setVal(from.getAuthor(), to::setAuthor, isSkipNull);
        if(c==null || c.fd("publishedAt").checkEnable())
        setVal(from.getPublishedAt(), to::setPublishedAt, isSkipNull);
        if(c==null || c.fd("modifiedAt").checkEnable())
        setVal(from.getModifiedAt(), to::setModifiedAt, isSkipNull);
    }
    
    
    public static void mapping(PostSummaryDTO from, BlogPost to){
        mapping(from,to,false);
    }
    public static void mapping(PostSummaryDTO from, BlogPost to, MappingContext c){
        mapping(from,to,false,c);
    }
    
    
    public static void mapping(PostSummaryDTO from, BlogPost to, EntityManager em){
        mapping(from,to,false,null,em);
    }
    public static void mapping(PostSummaryDTO from, BlogPost to, boolean isSkipNull, EntityManager em){
        mapping(from,to,isSkipNull,null,em);
    }
    public static void mapping(PostSummaryDTO from, BlogPost to, MappingContext c, EntityManager em){
        mapping(from,to,false,c,em);
    }
    public static void mapping(PostSummaryDTO from, BlogPost to, boolean isSkipNull, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable()) return;
        checkPermission(from, to);
        if(from ==null || to ==null) return;
        if(c==null || c.fd("id").checkEnable())
        setVal(from.getId(), to::setId, isSkipNull);
        if(c==null || c.fd("title").checkEnable())
        setVal(from.getTitle(), to::setTitle, isSkipNull);
        if(c==null || c.fd("summary").checkEnable())
        setVal(from.getSummary(), to::setSummary, isSkipNull);
        if(c==null || c.fd("status").checkEnable())
        setVal(from.getStatus(), to::setStatus, isSkipNull);
        if(c==null || c.fd("viewCount").checkEnable())
        setVal(from.getViewCount(), to::setViewCount, isSkipNull);
        if(c==null || c.fd("author").checkEnable())
        setVal(from.getAuthor(), to::setAuthor, isSkipNull);
        if(c==null || c.fd("publishedAt").checkEnable())
        setVal(from.getPublishedAt(), to::setPublishedAt, isSkipNull);
        if(c==null || c.fd("modifiedAt").checkEnable())
        setVal(from.getModifiedAt(), to::setModifiedAt, isSkipNull);
    }
    
    
    public static BlogPost toEntity(PostSummaryDTO from){
        return toEntity(from,MappingContext.withGuard(2,null));
    }
    public static BlogPost toEntity(PostSummaryDTO from,MappingContext c1){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        BlogPost to = new BlogPost();
        mapping(from,to,c);
        return to;
    }
    
    
    public static BlogPost toEntity(PostSummaryDTO from, EntityManager em){
        return toEntity(from,MappingContext.withGuard(2,null),em);
    }
    public static BlogPost toEntity(PostSummaryDTO from, MappingContext c1, EntityManager em){
        MappingContext c=c1!=null?c1.getChild():null;
        if(c!=null && !c.checkEnable()) return null;
        if(from==null) return null;
        BlogPost to = new BlogPost();
        mapping(from,to,c,em);
        return to;
    }
    
    
    public static List<BlogPost> toBlogPost2List(List<PostSummaryDTO> fromList){
        return toBlogPost2List(fromList,(MappingContext) null);
    }
    public static List<BlogPost> toBlogPost2List(List<PostSummaryDTO> fromList, MappingContext c){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->Jsonview_demoMapper.toEntity(e,c)).collect(Collectors.toList());
    }
    
    
    public static List<BlogPost> toBlogPost2List(List<PostSummaryDTO> fromList, EntityManager em){
        return toBlogPost2List(fromList,(MappingContext) null,em);
    }
    public static List<BlogPost> toBlogPost2List(List<PostSummaryDTO> fromList, MappingContext c, EntityManager em){
        if(c!=null && !c.checkEnable(1)) return null;
        if(fromList==null) return null;
        return fromList.stream().map(e->Jsonview_demoMapper.toEntity(e,c,em)).collect(Collectors.toList());
    }
    
    
    public static List<BlogPost> toBlogPost2List(List<PostSummaryDTO> fromList, BiFunction<PostSummaryDTO, BlogPost, BlogPost> modifier){
        if(fromList==null) return null;
        return fromList.stream()
            .map(from -> {
                BlogPost to = toEntity(from);
                return modifier.apply(from, to);
            }
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
    }
    
    
    public static Specification<BlogPost> toSpec(BlogPostSrchDTO searchDTO){
        Specification<BlogPost> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<BlogPost> toSpec(BlogPostSrchDTO searchDTO, Specification<BlogPost> sp){
        String title = searchDTO.getTitle();
        if(hasValue(title)){
            sp = sp.and((r,q,c) -> c.like(r.get("title"),"%" +trim(title)+ "%"));
        }
        String author = searchDTO.getAuthor();
        if(hasValue(author)){
            sp = sp.and((r,q,c) -> c.like(r.get("author"),"%" +trim(author)+ "%"));
        }
        String status = searchDTO.getStatus();
        if(hasValue(status)){
            sp = sp.and((r,q,c) -> c.equal(r.get("status"),status));
        }
        return sp;
    }
    
    
    public static Specification<Member> toSpec(MemberSrchDTO searchDTO){
        Specification<Member> b = (r, q, c) -> c.conjunction();
        return toSpec(searchDTO, b);
    }
    
    
    public static Specification<Member> toSpec(MemberSrchDTO searchDTO, Specification<Member> sp){
        String username = searchDTO.getUsername();
        if(hasValue(username)){
            sp = sp.and((r,q,c) -> c.like(r.get("username"),"%" +trim(username)+ "%"));
        }
        String email = searchDTO.getEmail();
        if(hasValue(email)){
            sp = sp.and((r,q,c) -> c.like(r.get("email"),"%" +trim(email)+ "%"));
        }
        String role = searchDTO.getRole();
        if(hasValue(role)){
            sp = sp.and((r,q,c) -> c.equal(r.get("role"),role));
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
