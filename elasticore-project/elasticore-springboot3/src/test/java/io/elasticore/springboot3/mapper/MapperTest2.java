package io.elasticore.springboot3.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest2 {

    @Test
    @DisplayName("List 필드 내부 타입이 다른 경우 매핑: List<TestDAO> -> List<Test2DAO>")
    void testListFieldWithDifferentElementTypes() {
        // 준비
        A source = new A();

        TestDAO item1 = new TestDAO("Item1", 10);
        TestDAO item2 = new TestDAO("Item2", 20);
        source.setList(Arrays.asList(item1, item2));

        B target = new B();

        // 매핑 실행
        Mapper.of(source, target)
                
                .execute();

        // 검증
        assertNotNull(target.getList());
        assertEquals(2, target.getList().size());

        Test2DAO targetItem1 = target.getList().get(0);
        Test2DAO targetItem2 = target.getList().get(1);

        assertEquals("Item1", targetItem1.getName());
        assertEquals(10, targetItem1.getValue());

        assertEquals("Item2", targetItem2.getName());
        assertEquals(20, targetItem2.getValue());
    }

    /**
     * Source Class
     */
    public static class A {
        private List<TestDAO> list;

        public List<TestDAO> getList() { return list; }
        public void setList(List<TestDAO> list) { this.list = list; }
    }

    /**
     * Target Class
     */
    public static class B {
        private List<Test2DAO> list;

        public List<Test2DAO> getList() { return list; }
        public void setList(List<Test2DAO> list) { this.list = list; }
    }

    /**
     * List Source Element
     */
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

    /**
     * List Target Element
     */
    public static class Test2DAO {
        private String name;
        private int value;

        public Test2DAO() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
}
