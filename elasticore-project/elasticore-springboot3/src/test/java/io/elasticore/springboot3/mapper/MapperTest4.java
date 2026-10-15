package io.elasticore.springboot3.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * gRPC style object mapping tests (completely protobuf dependency free)
 * Detection and processing using only getListCount(), getList(index) patterns
 */
class MapperTest4 {

    @Test
    @DisplayName("gRPC style -> DTO: Basic List mapping")
    void testGrpcStyleToDto() {
        // Create gRPC style object
        GrpcStyleBrandListOutput grpcSource = new GrpcStyleBrandListOutput();
        grpcSource.addBrand(new GrpcStyleBrandOutput("1", "Toyota"));
        grpcSource.addBrand(new GrpcStyleBrandOutput("2", "BMW"));
        grpcSource.addBrand(new GrpcStyleBrandOutput("3", "Hyundai"));

        BrandListDTO target = new BrandListDTO();

        Mapper.of(grpcSource, target).execute();

        assertNotNull(target.getList());
        assertEquals(3, target.getList().size());

        assertEquals("1", target.getList().get(0).getId());
        assertEquals("Toyota", target.getList().get(0).getName());

        assertEquals("2", target.getList().get(1).getId());
        assertEquals("BMW", target.getList().get(1).getName());

        assertEquals("3", target.getList().get(2).getId());
        assertEquals("Hyundai", target.getList().get(2).getName());
    }

    @Test
    @DisplayName("gRPC style -> DTO: Class type mapping")
    void testGrpcStyleToDtoClass() {
        GrpcStyleBrandListOutput grpcSource = new GrpcStyleBrandListOutput();
        grpcSource.addBrand(new GrpcStyleBrandOutput("4", "Tesla"));
        grpcSource.addBrand(new GrpcStyleBrandOutput("5", "Ferrari"));

        BrandListDTO target = Mapper.of(grpcSource, BrandListDTO.class).execute();

        assertNotNull(target);
        assertNotNull(target.getList());
        assertEquals(2, target.getList().size());

        assertEquals("4", target.getList().get(0).getId());
        assertEquals("Tesla", target.getList().get(0).getName());

        assertEquals("5", target.getList().get(1).getId());
        assertEquals("Ferrari", target.getList().get(1).getName());
    }

    @Test
    @DisplayName("gRPC style -> DTO: Empty List mapping")
    void testGrpcStyleEmptyListMapping() {
        GrpcStyleBrandListOutput grpcSource = new GrpcStyleBrandListOutput();
        // Empty list

        BrandListDTO target = Mapper.of(grpcSource, BrandListDTO.class).execute();

        assertNotNull(target);
        assertNotNull(target.getList());
        assertEquals(0, target.getList().size());
    }

    @Test
    @DisplayName("gRPC style -> DTO: Field mapping with map()")
    void testGrpcStyleFieldMapping() {
        GrpcStyleBrandOutput grpcBrand = new GrpcStyleBrandOutput("6", "Volkswagen");

        CustomBrandDTO target = Mapper.of(grpcBrand, CustomBrandDTO.class)
                .map("id", "brandId")
                .map("name", "brandName")
                .execute();

        assertNotNull(target);
        assertEquals("6", target.getBrandId());
        assertEquals("Volkswagen", target.getBrandName());
    }

    @Test
    @DisplayName("gRPC style -> DTO: skipNull test")
    void testGrpcStyleSkipNull() {
        GrpcStyleBrandOutput grpcBrand = new GrpcStyleBrandOutput(null, "BMW");

        CustomBrandDTO target = new CustomBrandDTO();
        target.setBrandId("existing-id");
        target.setBrandName("Existing Brand");

        Mapper.of(grpcBrand, target)
                .map("id", "brandId")
                .map("name", "brandName")
                .skipNull(true)
                .execute();

        assertEquals("existing-id", target.getBrandId()); // null value skipped, existing value retained
        assertEquals("BMW", target.getBrandName()); // updated with new value
    }

    @Test
    @DisplayName("gRPC style List -> DTO List: toList() test")
    void testGrpcStyleListToList() {
        List<GrpcStyleBrandOutput> grpcList = Arrays.asList(
                new GrpcStyleBrandOutput("7", "Audi"),
                new GrpcStyleBrandOutput("8", "Lexus"),
                new GrpcStyleBrandOutput("9", "Volvo")
        );

        List<BrandDTO> targetList = Mapper.of(grpcList, BrandDTO.class)
                .toList();

        assertNotNull(targetList);
        assertEquals(3, targetList.size());

        assertEquals("7", targetList.get(0).getId());
        assertEquals("Audi", targetList.get(0).getName());

        assertEquals("8", targetList.get(1).getId());
        assertEquals("Lexus", targetList.get(1).getName());

        assertEquals("9", targetList.get(2).getId());
        assertEquals("Volvo", targetList.get(2).getName());
    }

    @Test
    @DisplayName("Regular DTO -> gRPC style DTO mapping")
    void testDtoToGrpcStyle() {
        BrandListDTO source = new BrandListDTO();
        source.setList(Arrays.asList(
                new BrandDTO("1", "Mercedes", "Germany"),
                new BrandDTO("2", "Peugeot", "France")
        ));

        GrpcStyleBrandListOutput target = new GrpcStyleBrandListOutput();

        Mapper.of(source, target).execute();

        assertEquals(2, target.getListCount());

        // 첫 번째 브랜드 검증
        assertEquals("1", target.getList(0).getId());
        assertEquals("Mercedes", target.getList(0).getName());
        assertEquals("Germany", target.getList(0).getCountry());

        // 두 번째 브랜드 검증
        assertEquals("2", target.getList(1).getId());
        assertEquals("Peugeot", target.getList(1).getName());
        assertEquals("France", target.getList(1).getCountry());
    }

    @Test
    @DisplayName("DTO -> gRPC Builder pattern mapping (addAllList)")
    void testDtoToGrpcBuilderPattern() {
        BrandListDTO source = new BrandListDTO();
        source.setList(Arrays.asList(
                new BrandDTO("10", "Porsche", "Germany"),
                new BrandDTO("11", "Lamborghini", "Italy"),
                new BrandDTO("12", "McLaren", "UK")
        ));

        GrpcStyleBrandListBuilder target = new GrpcStyleBrandListBuilder();

        Mapper.of(source, target).execute();

        assertEquals(3, target.getListCount());

        assertEquals("10", target.getList(0).getId());
        assertEquals("Porsche", target.getList(0).getName());

        assertEquals("11", target.getList(1).getId());
        assertEquals("Lamborghini", target.getList(1).getName());

        assertEquals("12", target.getList(2).getId());
        assertEquals("McLaren", target.getList(2).getName());
    }

    @Test
    @DisplayName("DTO -> gRPC Builder pattern mapping (addList single)")
    void testDtoToGrpcBuilderSingleAdd() {
        BrandListDTO source = new BrandListDTO();
        source.setList(Arrays.asList(
                new BrandDTO("13", "Bentley", "UK"),
                new BrandDTO("14", "Rolls-Royce", "UK")
        ));

        GrpcStyleBrandListBuilderSingle target = new GrpcStyleBrandListBuilderSingle();

        Mapper.of(source, target).execute();

        assertEquals(2, target.getListCount());

        // 올바른 필드 검증
        assertEquals("13", target.getList(0).getId());
        assertEquals("Bentley", target.getList(0).getName());

        assertEquals("14", target.getList(1).getId());
        assertEquals("Rolls-Royce", target.getList(1).getName());
    }

    @Test
    @DisplayName("Complex gRPC Builder pattern mapping")
    void testComplexGrpcBuilderPattern() {
        ComplexDTO source = new ComplexDTO();
        source.setName("Complex Test");
        source.setBrands(Arrays.asList(
                new BrandDTO("15", "Ferrari", "Italy"),
                new BrandDTO("16", "Maserati", "Italy")
        ));
        source.setTags(Arrays.asList("luxury", "sports", "italian"));

        GrpcStyleComplexBuilder target = new GrpcStyleComplexBuilder();

        Mapper.of(source, target).execute();

        assertEquals("Complex Test", target.getName());

        // Brand validation
        assertEquals(2, target.getBrandsCount());
        assertEquals("15", target.getBrands(0).getId());
        assertEquals("Ferrari", target.getBrands(0).getName());
        assertEquals("Italy", target.getBrands(0).getCountry());
        assertEquals("16", target.getBrands(1).getId());
        assertEquals("Maserati", target.getBrands(1).getName());
        assertEquals("Italy", target.getBrands(1).getCountry());

        // Tags validation
        assertEquals(3, target.getTagsCount());
        assertEquals("luxury", target.getTags(0));
        assertEquals("sports", target.getTags(1));
        assertEquals("italian", target.getTags(2));
    }

    @Test
    @DisplayName("gRPC Builder skipNull test")
    void testGrpcBuilderSkipNull() {
        ComplexDTO source = new ComplexDTO();
        source.setName(null); // null value
        source.setBrands(Arrays.asList(new BrandDTO("17", "Bugatti", "France")));
        source.setTags(null); // null list

        GrpcStyleComplexBuilder target = new GrpcStyleComplexBuilder();
        target.setName("Existing Name");

        Mapper.of(source, target)
                .skipNull(true)
                .execute();

        assertEquals("Existing Name", target.getName()); // null skipped, existing value retained
        assertEquals(1, target.getBrandsCount()); // brand is set
        assertEquals("17", target.getBrands(0).getId());
        assertEquals("Bugatti", target.getBrands(0).getName());
        assertEquals("France", target.getBrands(0).getCountry());
        assertEquals(0, target.getTagsCount()); // null list is skipped
    }

    // ==================== gRPC Style Mock Classes ====================

    /**
     * Simulates gRPC Count/Get pattern class with real gRPC naming convention
     */
    static class GrpcStyleBrandListOutput extends MockGeneratedMessage {
        private List<GrpcStyleBrandOutput> list = new ArrayList<>();

        // gRPC pattern: getListCount()
        public int getListCount() {
            return list.size();
        }

        // gRPC pattern: getList(int index)
        public GrpcStyleBrandOutput getList(int index) {
            return list.get(index);
        }

        // Test helper method
        public void addBrand(GrpcStyleBrandOutput brand) {
            list.add(brand);
        }

        // Real gRPC naming pattern: getXxxList() - getter for list
        public List<GrpcStyleBrandOutput> getListList() {
            return new ArrayList<>(list);
        }

        // For direct internal list access (not typical gRPC pattern, but for testing)
        public void setList(List<GrpcStyleBrandOutput> list) {
            this.list = new ArrayList<>(list);
        }
    }

    static class GrpcStyleBrandOutput extends MockGeneratedMessage {
        private String id;
        private String name;
        private String country;

        public GrpcStyleBrandOutput() {
            // Default constructor for object creation
        }

        public GrpcStyleBrandOutput(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public GrpcStyleBrandOutput(String id, String name, String country) {
            this.id = id;
            this.name = name;
            this.country = country;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }

    /**
     * gRPC Builder pattern simulation class with real gRPC naming (addAllXxx)
     */
    static class GrpcStyleBrandListBuilder extends MockGeneratedMessage {
        private List<GrpcStyleBrandOutput> list = new ArrayList<>();

        // gRPC pattern: getListCount()
        public int getListCount() {
            return list.size();
        }

        // gRPC pattern: getList(int index)
        public GrpcStyleBrandOutput getList(int index) {
            return list.get(index);
        }

        // Real gRPC Builder pattern: addAllXxx(Iterable) - note: addAllList not addAllListList
        public GrpcStyleBrandListBuilder addAllList(Iterable<? extends BrandDTO> values) {
            for (BrandDTO value : values) {
                // Convert DTO to gRPC style object
                GrpcStyleBrandOutput grpcBrand = new GrpcStyleBrandOutput(
                        value.getId(),
                        value.getName(),
                        value.getCountry()
                );
                list.add(grpcBrand);
            }
            return this;
        }

        // Real gRPC naming pattern: getXxxList() - getter for list
        public List<GrpcStyleBrandOutput> getListList() {
            return new ArrayList<>(list);
        }
    }

    /**
     * gRPC Builder pattern simulation class (using addXxx single)
     */
    static class GrpcStyleBrandListBuilderSingle extends MockGeneratedMessage {
        private List<GrpcStyleBrandOutput> list = new ArrayList<>();

        // gRPC pattern: getListCount()
        public int getListCount() {
            return list.size();
        }

        // gRPC pattern: getList(int index)
        public GrpcStyleBrandOutput getList(int index) {
            return list.get(index);
        }

        // gRPC Builder pattern: addXxx(single item) - note: addList not addListList
        public GrpcStyleBrandListBuilderSingle addList(BrandDTO value) {
            // Convert DTO to gRPC style object
            GrpcStyleBrandOutput grpcBrand = new GrpcStyleBrandOutput(
                    value.getId(),
                    value.getName(),
                    value.getCountry()
            );
            list.add(grpcBrand);
            return this;
        }

        // Real gRPC naming pattern: getXxxList() - getter for list
        public List<GrpcStyleBrandOutput> getListList() {
            return new ArrayList<>(list);
        }
    }

    /**
     * Complex gRPC Builder pattern test class with real gRPC naming
     */
    static class GrpcStyleComplexBuilder extends MockGeneratedMessage {
        private String name;
        private List<GrpcStyleBrandOutput> brands = new ArrayList<>();
        private List<String> tags = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        // Brands related gRPC pattern
        public int getBrandsCount() {
            return brands.size();
        }

        public GrpcStyleBrandOutput getBrands(int index) {
            return brands.get(index);
        }

        // Real gRPC naming: getBrandsList() - getter for brands list
        public List<GrpcStyleBrandOutput> getBrandsList() {
            return new ArrayList<>(brands);
        }

        // Real gRPC Builder pattern: addAllBrands(Iterable)
        public GrpcStyleComplexBuilder addAllBrands(Iterable<? extends BrandDTO> values) {
            for (BrandDTO value : values) {
                // Convert DTO to gRPC style object
                GrpcStyleBrandOutput grpcBrand = new GrpcStyleBrandOutput(
                        value.getId(),
                        value.getName(),
                        value.getCountry()
                );
                brands.add(grpcBrand);
            }
            return this;
        }

        // Tags related gRPC pattern
        public int getTagsCount() {
            return tags.size();
        }

        public String getTags(int index) {
            return tags.get(index);
        }

        // Real gRPC naming: getTagsList() - getter for tags list
        public List<String> getTagsList() {
            return new ArrayList<>(tags);
        }

        // Real gRPC Builder pattern: addTags(String)
        public GrpcStyleComplexBuilder addTags(String value) {
            tags.add(value);
            return this;
        }

        // Real gRPC Builder pattern: addAllTags(Iterable)
        public GrpcStyleComplexBuilder addAllTags(Iterable<String> values) {
            for (String value : values) {
                tags.add(value);
            }
            return this;
        }
    }

    /**
     * Base class simulating GeneratedMessage
     */
    static abstract class MockGeneratedMessage {
        // Mock class that serves the role of gRPC's GeneratedMessage
        // In reality it would inherit from com.google.protobuf.GeneratedMessage
        // but here we're just simulating
    }

    // ==================== DTO Classes ====================

    static class BrandListDTO {
        private List<BrandDTO> list;

        public List<BrandDTO> getList() {
            return list;
        }

        public void setList(List<BrandDTO> list) {
            this.list = list;
        }
    }

    static class BrandDTO {
        private String id;
        private String name;
        private String country;

        public BrandDTO() {}

        public BrandDTO(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public BrandDTO(String id, String name, String country) {
            this.id = id;
            this.name = name;
            this.country = country;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }

    static class CustomBrandDTO {
        private String brandId;
        private String brandName;

        public String getBrandId() {
            return brandId;
        }

        public void setBrandId(String brandId) {
            this.brandId = brandId;
        }

        public String getBrandName() {
            return brandName;
        }

        public void setBrandName(String brandName) {
            this.brandName = brandName;
        }
    }

    static class ComplexDTO {
        private String name;
        private List<BrandDTO> brands;
        private List<String> tags;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<BrandDTO> getBrands() {
            return brands;
        }

        public void setBrands(List<BrandDTO> brands) {
            this.brands = brands;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }
}