package io.elasticore.springboot3.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest3 {

    @Test
    @DisplayName("Object to Object 매핑: 필드명이 일치할 때 정상 복사")
    void testObjectToObjectMapping() {
        SourceDAO source = new SourceDAO("John", "Developer", 30);
        TargetDAO target = new TargetDAO();

        Mapper.of(source, target).execute();

        assertEquals(null, target.getTitle());
        assertEquals(source.getDesc(), target.getDesc());
        assertEquals(source.getAge(), target.getAge());
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

    @Test
    @DisplayName("기본 매핑")
    void testBasic() {
        HelloRequest request = new HelloRequest();
        TestInputDTO input = new TestInputDTO();

        Mapper.of(request, input).execute();

        assertEquals("Test", input.getName());
    }

    // ============================================
    // List 매핑 테스트 케이스들 추가
    // ============================================

    @Test
    @DisplayName("DTO -> DTO: List<String> 필드 매핑")
    void testStringListMapping() {
        SourceWithStringList source = new SourceWithStringList();
        source.setName("John");
        source.setTags(Arrays.asList("java", "spring", "mysql"));

        TargetWithStringList target = new TargetWithStringList();

        Mapper.of(source, target).execute();

        assertEquals("John", target.getName());
        assertEquals(3, target.getTags().size());
        assertEquals("java", target.getTags().get(0));
        assertEquals("spring", target.getTags().get(1));
        assertEquals("mysql", target.getTags().get(2));
    }

    @Test
    @DisplayName("DTO -> DTO: List<Integer> 필드 매핑")
    void testIntegerListMapping() {
        SourceWithIntegerList source = new SourceWithIntegerList();
        source.setScores(Arrays.asList(95, 87, 92, 78));

        TargetWithIntegerList target = new TargetWithIntegerList();

        Mapper.of(source, target).execute();

        assertEquals(4, target.getScores().size());
        assertEquals(Integer.valueOf(95), target.getScores().get(0));
        assertEquals(Integer.valueOf(87), target.getScores().get(1));
        assertEquals(Integer.valueOf(92), target.getScores().get(2));
        assertEquals(Integer.valueOf(78), target.getScores().get(3));
    }

    @Test
    @DisplayName("DTO -> DTO: List<Object> 복합 객체 매핑")
    void testComplexObjectListMapping() {
        PersonDTO person1 = new PersonDTO("Alice", 25);
        PersonDTO person2 = new PersonDTO("Bob", 30);

        SourceWithObjectList source = new SourceWithObjectList();
        source.setTeamName("Development Team");
        source.setMembers(Arrays.asList(person1, person2));

        TargetWithObjectList target = new TargetWithObjectList();

        Mapper.of(source, target).execute();

        assertEquals("Development Team", target.getTeamName());
        assertEquals(2, target.getMembers().size());
        assertEquals("Alice", target.getMembers().get(0).getName());
        assertEquals(25, target.getMembers().get(0).getAge());
        assertEquals("Bob", target.getMembers().get(1).getName());
        assertEquals(30, target.getMembers().get(1).getAge());
    }

    @Test
    @DisplayName("DTO -> DTO: 빈 List 매핑")
    void testEmptyListMapping() {
        SourceWithStringList source = new SourceWithStringList();
        source.setName("EmptyTest");
        source.setTags(new ArrayList<>());

        TargetWithStringList target = new TargetWithStringList();

        Mapper.of(source, target).execute();

        assertEquals("EmptyTest", target.getName());
        assertNotNull(target.getTags());
        assertTrue(target.getTags().isEmpty());
    }

    @Test
    @DisplayName("DTO -> DTO: null List 매핑")
    void testNullListMapping() {
        SourceWithStringList source = new SourceWithStringList();
        source.setName("NullTest");
        source.setTags(null);

        TargetWithStringList target = new TargetWithStringList();

        Mapper.of(source, target).execute();

        assertEquals("NullTest", target.getName());
        assertNull(target.getTags());
    }

    @Test
    @DisplayName("DTO -> DTO: null List skipNull=true 테스트")
    void testNullListSkipMapping() {
        SourceWithStringList source = new SourceWithStringList();
        source.setName("SkipNullTest");
        source.setTags(null);

        TargetWithStringList target = new TargetWithStringList();
        target.setTags(Arrays.asList("existing", "tags"));

        Mapper.of(source, target)
                .skipNull(true)
                .execute();

        assertEquals("SkipNullTest", target.getName());
        assertNotNull(target.getTags());
        assertEquals(2, target.getTags().size());
        assertEquals("existing", target.getTags().get(0));
        assertEquals("tags", target.getTags().get(1));
    }

    @Test
    @DisplayName("DTO -> DTO: 다중 List 필드 매핑")
    void testMultipleListMapping() {
        MultiListSourceDTO source = new MultiListSourceDTO();
        source.setStringList(Arrays.asList("a", "b", "c"));
        source.setIntegerList(Arrays.asList(1, 2, 3));
        source.setBooleanList(Arrays.asList(true, false, true));

        MultiListTargetDTO target = new MultiListTargetDTO();

        Mapper.of(source, target).execute();

        assertEquals(3, target.getStringList().size());
        assertEquals("a", target.getStringList().get(0));

        assertEquals(3, target.getIntegerList().size());
        assertEquals(Integer.valueOf(1), target.getIntegerList().get(0));

        assertEquals(3, target.getBooleanList().size());
        assertTrue(target.getBooleanList().get(0));
        assertFalse(target.getBooleanList().get(1));
    }

    @Test
    @DisplayName("List<DTO> -> List<DTO>: 객체 리스트 간 매핑 with map() 사용")
    void testDtoListToDtoListMapping() {
        List<SourcePersonDTO> sourceList = Arrays.asList(
                new SourcePersonDTO("John", "Engineer", 30),
                new SourcePersonDTO("Jane", "Designer", 28),
                new SourcePersonDTO("Mike", "Manager", 35)
        );

        List<TargetPersonDTO> targetList = Mapper.of(sourceList, TargetPersonDTO.class)
                .map("position", "title")  // position -> title 매핑
                .toList();

        assertEquals(3, targetList.size());

        assertEquals("John", targetList.get(0).getName());
        assertEquals("Engineer", targetList.get(0).getTitle());
        assertEquals(30, targetList.get(0).getAge());

        assertEquals("Jane", targetList.get(1).getName());
        assertEquals("Designer", targetList.get(1).getTitle());
        assertEquals(28, targetList.get(1).getAge());
    }

    @Test
    @DisplayName("DTO -> Map: List 필드가 Map으로 매핑")
    void testDtoWithListToMapMapping() {
        SourceWithStringList source = new SourceWithStringList();
        source.setName("MapTest");
        source.setTags(Arrays.asList("tag1", "tag2", "tag3"));

        Map<String, Object> targetMap = new HashMap<>();

        Mapper.of(source, targetMap).execute();

        assertEquals("MapTest", targetMap.get("name"));

        @SuppressWarnings("unchecked")
        List<String> mappedTags = (List<String>) targetMap.get("tags");
        assertNotNull(mappedTags);
        assertEquals(3, mappedTags.size());
        assertEquals("tag1", mappedTags.get(0));
    }

    @Test
    @DisplayName("Map -> DTO: Map의 List 값이 DTO List 필드로 매핑")
    void testMapWithListToDtoMapping() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("name", "FromMapTest");
        sourceMap.put("tags", Arrays.asList("map-tag1", "map-tag2"));

        TargetWithStringList target = new TargetWithStringList();

        Mapper.of(sourceMap, target).execute();

        assertEquals("FromMapTest", target.getName());
        assertNotNull(target.getTags());
        assertEquals(2, target.getTags().size());
        assertEquals("map-tag1", target.getTags().get(0));
        assertEquals("map-tag2", target.getTags().get(1));
    }

    @Test
    @DisplayName("중첩된 List 매핑: List<List<String>>")
    void testNestedListMapping() {
        NestedListSourceDTO source = new NestedListSourceDTO();
        List<List<String>> nestedList = Arrays.asList(
                Arrays.asList("group1-item1", "group1-item2"),
                Arrays.asList("group2-item1", "group2-item2", "group2-item3")
        );
        source.setNestedStringList(nestedList);

        NestedListTargetDTO target = new NestedListTargetDTO();

        Mapper.of(source, target).execute();

        assertNotNull(target.getNestedStringList());
        assertEquals(2, target.getNestedStringList().size());
        assertEquals(2, target.getNestedStringList().get(0).size());
        assertEquals(3, target.getNestedStringList().get(1).size());
        assertEquals("group1-item1", target.getNestedStringList().get(0).get(0));
        assertEquals("group2-item3", target.getNestedStringList().get(1).get(2));
    }

    // ==== 기존 DTO 클래스들 ====

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

    // ============================================
    // List 매핑 관련 DTO 클래스들
    // ============================================

    public static class SourceWithStringList {
        private String name;
        private List<String> tags;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    public static class TargetWithStringList {
        private String name;
        private List<String> tags;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    public static class SourceWithIntegerList {
        private List<Integer> scores;

        public List<Integer> getScores() { return scores; }
        public void setScores(List<Integer> scores) { this.scores = scores; }
    }

    public static class TargetWithIntegerList {
        private List<Integer> scores;

        public List<Integer> getScores() { return scores; }
        public void setScores(List<Integer> scores) { this.scores = scores; }
    }

    public static class PersonDTO {
        private String name;
        private int age;

        public PersonDTO() {}
        public PersonDTO(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class SourceWithObjectList {
        private String teamName;
        private List<PersonDTO> members;

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        public List<PersonDTO> getMembers() { return members; }
        public void setMembers(List<PersonDTO> members) { this.members = members; }
    }

    public static class TargetWithObjectList {
        private String teamName;
        private List<PersonDTO> members;

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        public List<PersonDTO> getMembers() { return members; }
        public void setMembers(List<PersonDTO> members) { this.members = members; }
    }

    public static class MultiListSourceDTO {
        private List<String> stringList;
        private List<Integer> integerList;
        private List<Boolean> booleanList;

        public List<String> getStringList() { return stringList; }
        public void setStringList(List<String> stringList) { this.stringList = stringList; }
        public List<Integer> getIntegerList() { return integerList; }
        public void setIntegerList(List<Integer> integerList) { this.integerList = integerList; }
        public List<Boolean> getBooleanList() { return booleanList; }
        public void setBooleanList(List<Boolean> booleanList) { this.booleanList = booleanList; }
    }

    public static class MultiListTargetDTO {
        private List<String> stringList;
        private List<Integer> integerList;
        private List<Boolean> booleanList;

        public List<String> getStringList() { return stringList; }
        public void setStringList(List<String> stringList) { this.stringList = stringList; }
        public List<Integer> getIntegerList() { return integerList; }
        public void setIntegerList(List<Integer> integerList) { this.integerList = integerList; }
        public List<Boolean> getBooleanList() { return booleanList; }
        public void setBooleanList(List<Boolean> booleanList) { this.booleanList = booleanList; }
    }

    public static class SourcePersonDTO {
        private String name;
        private String position;
        private int age;

        public SourcePersonDTO() {}
        public SourcePersonDTO(String name, String position, int age) {
            this.name = name;
            this.position = position;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class TargetPersonDTO {
        private String name;
        private String title;
        private int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class NestedListSourceDTO {
        private List<List<String>> nestedStringList;

        public List<List<String>> getNestedStringList() { return nestedStringList; }
        public void setNestedStringList(List<List<String>> nestedStringList) {
            this.nestedStringList = nestedStringList;
        }
    }

    public static class NestedListTargetDTO {
        private List<List<String>> nestedStringList;

        public List<List<String>> getNestedStringList() { return nestedStringList; }
        public void setNestedStringList(List<List<String>> nestedStringList) {
            this.nestedStringList = nestedStringList;
        }
    }
}