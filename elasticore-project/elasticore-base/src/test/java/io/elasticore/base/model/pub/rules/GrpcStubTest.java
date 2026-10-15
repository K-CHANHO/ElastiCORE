package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.pub.rules.base.GrpcStubRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The gRPC stubs of a proto service, in both languages.
 *
 * These name classes protoc generates from the .proto, so neither template
 * project compiles them - elasticore-template excludes its proto output and
 * elasticore-template-kt excludes the same package. What they should say is
 * therefore stated here rather than left to a compiler that never sees them.
 */
public class GrpcStubTest {

    private ProtoServiceSpec sync() {
        return new ProtoServiceSpec("SyncGrpcService", "kr.co.acme.grpc")
                .rpc("getSyncStatus", "SyncStatusRequest", "SyncStatusReply", "reads the status")
                .server(true)
                .client(true, null);
    }

    private String render(ProtoServiceSpec p, String artifact, SourceCode.TemplateSet set) {
        return new RuleEngine().register(new GrpcStubRules())
                .templateSet(set)
                .run(p, artifact)
                .render();
    }

    // ---- the implementation stub ---------------------------------------

    @Test
    public void javaProvider_extendsTheGeneratedBaseAndTakesAnObserver() {
        String out = render(sync(), "grpc_svc_provider", SourceCode.TemplateSet.COMPATIBLE);

        assertTrue(out.contains("extends SyncGrpcServiceGrpc.SyncGrpcServiceImplBase"), out);
        assertTrue(out.contains(
                "public void getSyncStatus(SyncStatusRequest request,"
                        + " StreamObserver<SyncStatusReply> responseObserver) {"), out);
        assertTrue(out.contains("responseObserver.onCompleted();"), out);
    }

    /** Kotlin requires the modifier Java leaves to an optional annotation. */
    @Test
    public void kotlinProvider_marksItsMethodsOverride() {
        String out = render(sync(), "grpc_svc_provider", SourceCode.TemplateSet.KOTLIN);

        assertTrue(out.contains(
                "override fun getSyncStatus(request: SyncStatusRequest,"
                        + " responseObserver: StreamObserver<SyncStatusReply>) {"), out);
        assertTrue(out.contains("responseObserver.onCompleted()"), out);
        assertFalse(out.contains("responseObserver.onCompleted();"), out);
    }

    // ---- the calling stub ----------------------------------------------

    @Test
    public void javaClient_callsTheBlockingStub() {
        String out = render(sync(), "grpc_svc_client", SourceCode.TemplateSet.COMPATIBLE);

        assertTrue(out.contains("public SyncStatusReply getSyncStatus(SyncStatusRequest request) {"), out);
        assertTrue(out.contains("SyncStatusReply response = blockingStub.getSyncStatus(request);"), out);
    }

    @Test
    public void kotlinClient_callsTheBlockingStub() {
        String out = render(sync(), "grpc_svc_client", SourceCode.TemplateSet.KOTLIN);

        assertTrue(out.contains("fun getSyncStatus(request: SyncStatusRequest): SyncStatusReply {"), out);
        assertTrue(out.contains("val response = blockingStub.getSyncStatus(request)"), out);
    }

    /**
     * The address is a Spring placeholder in the generated source, not a slot
     * of this generator - the client reads it at startup.
     */
    @Test
    public void client_readsItsAddressFromConfiguration() {
        String out = render(sync(), "grpc_svc_client", SourceCode.TemplateSet.COMPATIBLE);

        assertTrue(out.contains("${grpc.syncGrpcService.ip:localhost}"), out);
        assertTrue(out.contains("${grpc.syncGrpcService.port:9090}"), out);
    }

    /** A name the DSL gave @client is the prefix its address is read under. */
    @Test
    public void client_usesTheNameTheDslGaveIt() {
        String out = render(sync().client(true, "syncEdge"), "grpc_svc_client",
                SourceCode.TemplateSet.COMPATIBLE);

        assertTrue(out.contains("${grpc.syncEdge.ip:localhost}"), out);
    }

    // ---- what the DSL did not ask for ----------------------------------

    /** Without @server there is nothing to implement. */
    @Test
    public void withoutServer_noProviderIsWritten() {
        ProtoServiceSpec p = sync().server(false);
        SourceCode code = new RuleEngine().register(new GrpcStubRules())
                .run(p, "grpc_svc_provider");

        assertFalse(code.isMake());
    }

    /** Without @client there is nothing to call it with. */
    @Test
    public void withoutClient_noClientIsWritten() {
        ProtoServiceSpec p = sync().client(false, null);
        SourceCode code = new RuleEngine().register(new GrpcStubRules())
                .run(p, "grpc_svc_client");

        assertFalse(code.isMake());
    }
}
