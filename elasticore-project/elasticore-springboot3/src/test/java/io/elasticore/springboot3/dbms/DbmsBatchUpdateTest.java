package io.elasticore.springboot3.dbms;

import io.elasticore.runtime.port.DbmsService;
import io.elasticore.springboot3.bean.ApplicationContextProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for SQL port batch update support: when a port method takes a List (or array)
 * of DTO elements and returns int with an insert/update/delete method name, the proxy
 * must route to executeBatchUpdate so each element is executed individually.
 */
@ExtendWith(MockitoExtension.class)
class DbmsBatchUpdateTest {

    @Mock
    private BeanFactory beanFactory;

    @Mock
    private Environment environment;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private DbmsSqlExecutor executor;

    // Test DTO used as a batch element
    public static class LogInput {
        private String endpoint;

        public LogInput(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getEndpoint() {
            return endpoint;
        }
    }

    @DbmsService(id = "test.BatchService", sqlSource = "blueprint/test/port/dbms_port.yml")
    interface BatchService {
        int insertLogs(List<LogInput> items);

        int insertLogArray(LogInput[] items);

        int insertLog(LogInput item);

        int deleteByIds(List<Long> ids);
    }

    private BatchService createProxy() {
        DbmsService meta = BatchService.class.getAnnotation(DbmsService.class);
        return DbmsServiceProxyFactory.createService(BatchService.class, meta, beanFactory, environment);
    }

    @Nested
    @DisplayName("resolveBatchInput - batch eligibility detection")
    class ResolveBatchInputTest {

        @Test
        @DisplayName("List of DTO elements is eligible for batch execution")
        void listOfDto() {
            List<LogInput> input = Arrays.asList(new LogInput("/a"), new LogInput("/b"));
            assertThat(DbmsServiceProxyFactory.DbmsInvocationHandler.resolveBatchInput(input))
                    .isSameAs(input);
        }

        @Test
        @DisplayName("List of Map elements is eligible for batch execution")
        void listOfMap() {
            Map<String, Object> row = new HashMap<>();
            row.put("endpoint", "/a");
            List<Map<String, Object>> input = Collections.singletonList(row);
            assertThat(DbmsServiceProxyFactory.DbmsInvocationHandler.resolveBatchInput(input))
                    .isSameAs(input);
        }

        @Test
        @DisplayName("Array of DTO elements is converted to a batch list")
        void arrayOfDto() {
            LogInput[] input = {new LogInput("/a"), new LogInput("/b")};
            List<?> result = DbmsServiceProxyFactory.DbmsInvocationHandler.resolveBatchInput(input);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("List of simple values keeps legacy IN-clause binding (not batch)")
        void listOfSimpleValues() {
            assertThat(DbmsServiceProxyFactory.DbmsInvocationHandler.resolveBatchInput(Arrays.asList(1L, 2L))).isNull();
            assertThat(DbmsServiceProxyFactory.DbmsInvocationHandler.resolveBatchInput(Arrays.asList("a", "b"))).isNull();
        }

        @Test
        @DisplayName("Non-collection inputs are not batch eligible")
        void nonCollectionInput() {
            assertThat(DbmsServiceProxyFactory.DbmsInvocationHandler.resolveBatchInput(new LogInput("/a"))).isNull();
            assertThat(DbmsServiceProxyFactory.DbmsInvocationHandler.resolveBatchInput(new HashMap<>())).isNull();
            assertThat(DbmsServiceProxyFactory.DbmsInvocationHandler.resolveBatchInput(null)).isNull();
            assertThat(DbmsServiceProxyFactory.DbmsInvocationHandler.resolveBatchInput(new int[]{1, 2})).isNull();
        }

        @Test
        @DisplayName("Empty list is treated as a no-op batch")
        void emptyList() {
            List<?> result = DbmsServiceProxyFactory.DbmsInvocationHandler.resolveBatchInput(Collections.emptyList());
            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("Proxy routing for int-returning insert/update/delete methods")
    class ProxyRoutingTest {

        @Test
        @DisplayName("List of DTO input routes to executeBatchUpdate")
        void listInputRoutesToBatch() throws Exception {
            try (MockedStatic<ApplicationContextProvider> mocked = mockStatic(ApplicationContextProvider.class)) {
                mocked.when(ApplicationContextProvider::getApplicationContext).thenReturn(applicationContext);
                when(applicationContext.getBean(DbmsSqlExecutor.class)).thenReturn(executor);
                when(executor.executeBatchUpdate(any(DbmsService.class), eq("insertLogs"), anyList())).thenReturn(3);

                List<LogInput> items = Arrays.asList(new LogInput("/a"), new LogInput("/b"), new LogInput("/c"));
                int affected = createProxy().insertLogs(items);

                assertThat(affected).isEqualTo(3);
                verify(executor).executeBatchUpdate(any(DbmsService.class), eq("insertLogs"), eq(items));
                verify(executor, never()).executeUpdate(any(DbmsService.class), anyString(), any());
            }
        }

        @Test
        @DisplayName("Array of DTO input routes to executeBatchUpdate")
        void arrayInputRoutesToBatch() throws Exception {
            try (MockedStatic<ApplicationContextProvider> mocked = mockStatic(ApplicationContextProvider.class)) {
                mocked.when(ApplicationContextProvider::getApplicationContext).thenReturn(applicationContext);
                when(applicationContext.getBean(DbmsSqlExecutor.class)).thenReturn(executor);
                when(executor.executeBatchUpdate(any(DbmsService.class), eq("insertLogArray"), anyList())).thenReturn(2);

                int affected = createProxy().insertLogArray(new LogInput[]{new LogInput("/a"), new LogInput("/b")});

                assertThat(affected).isEqualTo(2);
                verify(executor).executeBatchUpdate(any(DbmsService.class), eq("insertLogArray"), anyList());
            }
        }

        @Test
        @DisplayName("Empty list input returns 0 without executing any SQL")
        void emptyListReturnsZero() throws Exception {
            try (MockedStatic<ApplicationContextProvider> mocked = mockStatic(ApplicationContextProvider.class)) {
                mocked.when(ApplicationContextProvider::getApplicationContext).thenReturn(applicationContext);
                when(applicationContext.getBean(DbmsSqlExecutor.class)).thenReturn(executor);

                int affected = createProxy().insertLogs(Collections.emptyList());

                assertThat(affected).isZero();
                verify(executor, never()).executeBatchUpdate(any(DbmsService.class), anyString(), anyList());
                verify(executor, never()).executeUpdate(any(DbmsService.class), anyString(), any());
            }
        }

        @Test
        @DisplayName("Single DTO input keeps legacy executeUpdate path")
        void singleDtoKeepsLegacyPath() throws Exception {
            try (MockedStatic<ApplicationContextProvider> mocked = mockStatic(ApplicationContextProvider.class)) {
                mocked.when(ApplicationContextProvider::getApplicationContext).thenReturn(applicationContext);
                when(applicationContext.getBean(DbmsSqlExecutor.class)).thenReturn(executor);
                when(executor.executeUpdate(any(DbmsService.class), eq("insertLog"), any())).thenReturn(1);

                int affected = createProxy().insertLog(new LogInput("/a"));

                assertThat(affected).isEqualTo(1);
                verify(executor).executeUpdate(any(DbmsService.class), eq("insertLog"), any(LogInput.class));
                verify(executor, never()).executeBatchUpdate(any(DbmsService.class), anyString(), anyList());
            }
        }

        @Test
        @DisplayName("List of simple values keeps legacy executeUpdate path (IN-clause)")
        void simpleValueListKeepsLegacyPath() throws Exception {
            try (MockedStatic<ApplicationContextProvider> mocked = mockStatic(ApplicationContextProvider.class)) {
                mocked.when(ApplicationContextProvider::getApplicationContext).thenReturn(applicationContext);
                when(applicationContext.getBean(DbmsSqlExecutor.class)).thenReturn(executor);
                when(executor.executeUpdate(any(DbmsService.class), eq("deleteByIds"), any())).thenReturn(2);

                int affected = createProxy().deleteByIds(Arrays.asList(1L, 2L));

                assertThat(affected).isEqualTo(2);
                verify(executor).executeUpdate(any(DbmsService.class), eq("deleteByIds"), any());
                verify(executor, never()).executeBatchUpdate(any(DbmsService.class), anyString(), anyList());
            }
        }
    }

    @Nested
    @DisplayName("DbmsSqlExecutor default executeBatchUpdate")
    class DefaultBatchUpdateTest {

        @Test
        @DisplayName("Delegates each element to executeUpdate and sums affected rows")
        void sumsPerElementResults() throws Exception {
            DbmsService meta = BatchService.class.getAnnotation(DbmsService.class);
            when(executor.executeBatchUpdate(any(DbmsService.class), anyString(), anyList())).thenCallRealMethod();
            when(executor.executeUpdate(any(DbmsService.class), anyString(), any())).thenReturn(1);

            List<LogInput> items = Arrays.asList(new LogInput("/a"), null, new LogInput("/b"));
            int total = executor.executeBatchUpdate(meta, "insertLogs", items);

            assertThat(total).isEqualTo(2);
            verify(executor, times(2)).executeUpdate(any(DbmsService.class), eq("insertLogs"), any(LogInput.class));
        }

        @Test
        @DisplayName("Returns 0 for null or empty list")
        void zeroForNullOrEmpty() throws Exception {
            DbmsService meta = BatchService.class.getAnnotation(DbmsService.class);
            when(executor.executeBatchUpdate(any(DbmsService.class), anyString(), any())).thenCallRealMethod();

            assertThat(executor.executeBatchUpdate(meta, "insertLogs", null)).isZero();
            assertThat(executor.executeBatchUpdate(meta, "insertLogs", Collections.emptyList())).isZero();
            verify(executor, never()).executeUpdate(any(DbmsService.class), anyString(), any());
        }
    }
}
