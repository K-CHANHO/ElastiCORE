package io.elasticore.springboot3.mapper;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MappingUtils2FullTest {

    // DTO 정의
    public static class SourceDTO {
        private String name;
        private int age;
        private List<SourceChildDTO> children;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public List<SourceChildDTO> getChildren() { return children; }
        public void setChildren(List<SourceChildDTO> children) { this.children = children; }
    }

    public static class SourceChildDTO {
        private String childName;

        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }
    }

    public static class TargetDTO {
        private String name;
        private int age;
        private List<TargetChildDTO> children;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public List<TargetChildDTO> getChildren() { return children; }
        public void setChildren(List<TargetChildDTO> children) { this.children = children; }
    }

    public static class TargetChildDTO {
        private String childName;

        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }
    }

    public static class SourceDAO {
        private String name;
        private String desc;
        private int age;
        private String[] tags;
        private int[] scores;

        public SourceDAO() {}

        public SourceDAO(String name, String desc, int age) {
            this.name = name;
            this.desc = desc;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public String[] getTags() { return tags; }
        public void setTags(String[] tags) { this.tags = tags; }

        public int[] getScores() { return scores; }
        public void setScores(int[] scores) { this.scores = scores; }
    }

    public static class TargetDAO {
        private String title;
        private String desc;
        private int age;
        private String[] tags;
        private int[] scores;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public String[] getTags() { return tags; }
        public void setTags(String[] tags) { this.tags = tags; }

        public int[] getScores() { return scores; }
        public void setScores(int[] scores) { this.scores = scores; }
    }

    public static class A {
        private List<TestDAO> list;

        public List<TestDAO> getList() { return list; }
        public void setList(List<TestDAO> list) { this.list = list; }
    }

    public static class B {
        private List<Test2DAO> list;

        public List<Test2DAO> getList() { return list; }
        public void setList(List<Test2DAO> list) { this.list = list; }
    }

    public static class TestDAO {
        private String name;
        private int value;

        public TestDAO() {}
        public TestDAO(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    public static class Test2DAO {
        private String name;
        private int value;

        public Test2DAO() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    @Test
    void testSimpleCopy() {
        SourceDTO source = new SourceDTO();
        source.setName("홍길동");
        source.setAge(35);

        TargetDTO target = new TargetDTO();
        MappingUtils2.copy(source, target);

        assertEquals("홍길동", target.getName());
        assertEquals(35, target.getAge());
    }

    @Test
    void testNestedListCopy() {
        SourceChildDTO child1 = new SourceChildDTO();
        child1.setChildName("철수");

        SourceChildDTO child2 = new SourceChildDTO();
        child2.setChildName("영희");

        SourceDTO source = new SourceDTO();
        source.setName("부모");
        source.setAge(45);
        source.setChildren(Arrays.asList(child1, child2));

        TargetDTO target = new TargetDTO();
        MappingUtils2.copy(source, target);

        assertEquals("부모", target.getName());
        assertEquals(45, target.getAge());
        assertNotNull(target.getChildren());
        assertEquals(2, target.getChildren().size());
        assertEquals("철수", target.getChildren().get(0).getChildName());
        assertEquals("영희", target.getChildren().get(1).getChildName());
    }

    @Test
    void testListConversion() {
        SourceDTO s1 = new SourceDTO();
        s1.setName("유저1");
        s1.setAge(21);

        SourceDTO s2 = new SourceDTO();
        s2.setName("유저2");
        s2.setAge(32);

        List<SourceDTO> sourceList = Arrays.asList(s1, s2);
        List<TargetDTO> resultList = MappingUtils2.toList(sourceList, TargetDTO.class);

        assertEquals(2, resultList.size());
        assertEquals("유저1", resultList.get(0).getName());
        assertEquals("유저2", resultList.get(1).getName());
        assertEquals(21, resultList.get(0).getAge());
        assertEquals(32, resultList.get(1).getAge());
    }

    @Test
    void testCustomFieldMappingWithArray() {
        SourceDAO source = new SourceDAO("이름", "설명", 28);
        source.setTags(new String[] {"a", "b"});
        source.setScores(new int[] {100, 90});

        TargetDAO target = new TargetDAO();
        MappingUtils2.copy(source, target, Arrays.asList("name", "desc", "age", "tags", "scores"), Arrays.asList("title", "desc", "age", "tags", "scores"));

        assertEquals("이름", target.getTitle());
        assertEquals("설명", target.getDesc());
        assertEquals(28, target.getAge());
        assertArrayEquals(new String[] {"a", "b"}, target.getTags());
        assertArrayEquals(new int[] {100, 90}, target.getScores());
    }

    @Test
    void testNestedListToDifferentClass() {
        TestDAO t1 = new TestDAO("x", 1);
        TestDAO t2 = new TestDAO("y", 2);

        A source = new A();
        source.setList(Arrays.asList(t1, t2));

        B target = new B();
        MappingUtils2.copy(source, target);

        assertNotNull(target.getList());
        assertEquals(2, target.getList().size());
        assertEquals("x", target.getList().get(0).getName());
        assertEquals(1, target.getList().get(0).getValue());
    }
}
