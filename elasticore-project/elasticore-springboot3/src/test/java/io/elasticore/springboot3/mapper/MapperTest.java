package io.elasticore.springboot3.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    @Test
    @DisplayName("Object to Object 매핑: 필드명이 일치할 때 정상 복사")
    void testObjectToObjectMapping() {
        SourceDAO source = new SourceDAO("John", "Developer", 30);
        TargetDAO target = new TargetDAO();

        Mapper.of(source, target).execute();

        assertEquals(null, target.getTitle());
        assertEquals(source.getDesc(), target.getDesc());
        assertEquals(source.getAge(), target.getAge());

        MappingContext.withGuard(2, c -> {
            return !"title".equals(c.getFieldName());
        });

    }

    @Test
    @DisplayName("Object to Map 매핑: 필드명이 Map 키로 복사")
    void testObjectToMapMapping() {
        SourceDAO source = new SourceDAO("Alice", "Engineer", 25);
        Map<String, Object> map = new HashMap<>();

        Mapper.of(source, map).execute();

        assertEquals("Alice", map.get("name"));
        assertEquals("Engineer", map.get("desc"));
        assertEquals(25, map.get("age"));
    }

    @Test
    @DisplayName("Map to Object 매핑: Map 키 -> 필드명 매핑")
    void testMapToObjectMapping() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("name", "Bob");
        sourceMap.put("desc", "Tester");
        sourceMap.put("age", 40);

        TargetDAO target = new TargetDAO();
        Mapper.of(sourceMap, target)
                .map("name", "title")  // name -> title로 매핑
                .execute();

        assertEquals("Bob", target.getTitle());
        assertEquals("Tester", target.getDesc());
        assertEquals(40, target.getAge());
    }

    @Test
    @DisplayName("List<Object> 매핑: 객체 리스트 변환")
    void testListMapping() {
        List<SourceDAO> sources = Arrays.asList(
                new SourceDAO("Tom", "Manager", 45),
                new SourceDAO("Jerry", "Designer", 29)
        );

        List<TargetDAO> targets = Mapper.of(sources, TargetDAO.class)
                .map("name","title")
                .toList();

        assertEquals(2, targets.size());
        assertEquals("Tom", targets.get(0).getTitle());
        assertEquals("Jerry", targets.get(1).getTitle());
    }


    @Test
    @DisplayName("null 필드 skip 테스트 (skipNull=true)")
    void testSkipNullFields() {
        SourceDAO source = new SourceDAO(null, "With Desc", 0);
        TargetDAO target = new TargetDAO();
        target.setTitle("Existing Title");
        target.setDesc("Existing Desc");
        target.setAge(99);

        Mapper.of(source, target)
                .skipNull(true)
                .execute();

        // name이 null이면 기존 값 유지
        assertEquals("Existing Title", target.getTitle());
        assertEquals("With Desc", target.getDesc());
        assertEquals(0, target.getAge());
    }

    @Test
    @DisplayName("Source 또는 Target이 null인 경우 예외 발생")
    void testNullSourceOrTarget() {
        assertThrows(IllegalArgumentException.class,
                () -> Mapper.of(null, new TargetDAO()).execute());

        assertThrows(IllegalArgumentException.class,
                () -> Mapper.of(new SourceDAO(), (Object) null).execute());
    }

    @Test
    @DisplayName("같은 객체를 복사하려고 할 때 예외 발생")
    void testSameSourceAndTarget() {
        SourceDAO source = new SourceDAO();
        assertThrows(IllegalArgumentException.class,
                () -> Mapper.of(source, source).execute());
    }

    @Test
    @DisplayName("toClass로 인스턴스 생성 실패 시 예외 발생")
    void testTargetClassInstantiationFailure() {
        class NoDefaultConstructor {
            private NoDefaultConstructor(String value) {}
        }
        assertThrows(RuntimeException.class,
                () -> Mapper.of(new SourceDAO(), NoDefaultConstructor.class).execute());
    }

    @Test
    @DisplayName("빈 소스 객체 매핑 테스트")
    void testEmptySourceObject() {
        SourceDAO source = new SourceDAO();
        TargetDAO target = new TargetDAO();

        Mapper.of(source, target).execute();

        assertNull(target.getTitle());
        assertNull(target.getDesc());
        assertEquals(0, target.getAge());
    }


    @Test
    @DisplayName("배열(String[], int[]) 필드 매핑 테스트")
    void testArrayFieldMapping() {
        SourceDAO source = new SourceDAO();
        source.setName("Test Name");
        source.setDesc("Test Desc");
        source.setAge(99);
        source.setTags(new String[]{"tag1", "tag2"});
        source.setScores(new int[]{100, 90, 80});

        TargetDAO target = new TargetDAO();

        Mapper.of(source, target)
                .map("name", "title")
                .execute();

        assertEquals("Test Name", target.getTitle());
        assertEquals("Test Desc", target.getDesc());
        assertEquals(99, target.getAge());

        assertArrayEquals(new String[]{"tag1", "tag2"}, target.getTags());
        assertArrayEquals(new int[]{100, 90, 80}, target.getScores());
    }


    // ==== 서브 클래스 ====

    public static class SourceDAO {
        private String name;
        private String desc;
        private int age;
        private String[] tags;  // 추가
        private int[] scores;   // 추가

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
        private String[] tags;   // 추가
        private int[] scores;    // 추가

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


    @Test
    @DisplayName("기본 매핑")
    void testBasic() {

        HelloRequest request = new HelloRequest();

        TestInputDTO input = new TestInputDTO();

        Mapper.of(request, input).execute();


        assertEquals("Test", input.getName());


    }


    public static class HelloRequest {
        public String getName() {
            return "Test";
        }
    }

    public static class TestInputDTO {
        private String name = null;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;

        }
    }
}
