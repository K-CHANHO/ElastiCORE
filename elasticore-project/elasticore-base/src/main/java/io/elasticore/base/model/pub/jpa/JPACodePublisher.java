package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ECoreModelContext;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.SourceFileAccessFactory;
import io.elasticore.base.model.ConstantParam;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.ModelComponent;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.dto.DataTransfer;
import io.elasticore.base.model.dto.DataTransferModels;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.model.entity.EntityModels;
import io.elasticore.base.model.enums.EnumModel;
import io.elasticore.base.model.enums.EnumModels;
import io.elasticore.base.model.repo.Repository;
import io.elasticore.base.model.repo.RepositoryModels;
import io.elasticore.base.util.ConsoleLog;
import io.elasticore.base.util.FileOrder;
import io.elasticore.base.util.HashUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;


public class JPACodePublisher implements CodePublisher {

    protected ECoreModelContext ctx;

    private SourceFileAccessFactory sourceFileAccessFactory;

    protected JPACodePublisher() {

    }

    @Override
    public void setSrcCodeWriterFactory(SourceFileAccessFactory sourceFileAccessFactory) {
        this.sourceFileAccessFactory = sourceFileAccessFactory;
    }

    @Override
    public SourceFileAccessFactory getSrcFileAccessFactory() {
        return this.sourceFileAccessFactory;
    }


    @Override
    public void errorOnPublish(ModelComponent modelComponent, Throwable e) {

        // TODO
        e.printStackTrace();
    }

    public static JPACodePublisher newInstance() {
        return new JPACodePublisher();
    }


    @Override
    public boolean deleteSource(ModelDomain domain, String rootDir) {
        String entityNs = domain.getModel().getNamespace(ConstantParam.KEYNAME_ENTITY);

        String dtoNs = domain.getModel().getNamespace(ConstantParam.KEYNAME_DTO);
        String enumNs = domain.getModel().getNamespace(ConstantParam.KEYNAME_ENUMERATION);
        String repoNs = domain.getModel().getNamespace(ConstantParam.KEYNAME_REPOSITORY);
        String svcNs = domain.getModel().getNamespace(ConstantParam.KEYNAME_SERVICE);
        String controlNs = domain.getModel().getNamespace(ConstantParam.KEYNAME_CONTROL);


        deleteFilesWithEcdStart( rootDir + convertToFilePath(entityNs) );
        deleteFilesWithEcdStart( rootDir + convertToFilePath(dtoNs) );
        deleteFilesWithEcdStart( rootDir + convertToFilePath(enumNs) );
        deleteFilesWithEcdStart( rootDir + convertToFilePath(repoNs) );
        deleteFilesWithEcdStart( rootDir + convertToFilePath(svcNs) );
        deleteFilesWithEcdStart( rootDir + convertToFilePath(controlNs) );

        if(ConsoleLog.countStoredLogkey("DELETE_OK")>0) {
            ConsoleLog.print("");
            ConsoleLog.print("[INFO] delete files:");
            ConsoleLog.print("--------------------------------------");
            ConsoleLog.printStoredLog("DELETE_OK" , "  ");
        }



        if(ConsoleLog.countStoredLogkey("DELETE_FAIL")>0) {
            ConsoleLog.print("");
            ConsoleLog.print("[WARN] undelete files:");
            ConsoleLog.print("--------------------------------------");
            ConsoleLog.printStoredWarnLog("DELETE_FAIL" , "  ");
        }

        if(ConsoleLog.countStoredLogkey("DELETE_FAIL2")>0) {
            ConsoleLog.print("");
            ConsoleLog.print("[WARN] modified files:");
            ConsoleLog.print("--------------------------------------");
            ConsoleLog.printStoredWarnLog("DELETE_FAIL2" , "  ");
        }



        return true;
    }


    private static String convertToFilePath(String qualifiedClassName) {
        return "/"+qualifiedClassName.replace('.', '/');
    }


    public static void deleteFilesWithEcdStart(String directoryPath) {

        File directory = new File(directoryPath);

        if (!directory.isDirectory()) {
            return;
        }

        File[] files = FileOrder.list(directory);
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isFile()) {

                HashUtils.Response response = null;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {

                    response = HashUtils.checkContentModified(reader);

                } catch (IOException e) {

                }

                if(response !=null) {
                    if(response.getStatus() == HashUtils.NO_MODIFIED) {
                        if (file.delete()) {
                            ConsoleLog.storeLog("DELETE_OK",directoryPath +" "+file.getName() );
                        }else{
                            ConsoleLog.storeLog("DELETE_FAIL",directoryPath +" "+file.getName() );
                        }
                    }else if(response.getStatus() == HashUtils.MODIFIED) {
                        ConsoleLog.storeLog("DELETE_FAIL2",directoryPath +" "+file.getName() );
                    }
                }

            }
        }
    }


    @Override
    public ECoreModelContext getECoreModelContext() {
        return this.ctx;
    }

    protected boolean printRuleCheckInfo(ECoreModelContext ctx, ModelDomain domain) {
        if(ConsoleLog.countStoredLogkey("FIELD_NM_ERR")>0) {
            ConsoleLog.print("");
            ConsoleLog.print("[WARN] field naming convention error:");
            ConsoleLog.print("--------------------------------------");
            ConsoleLog.printStoredWarnLog("FIELD_NM_ERR", "  ");
            ConsoleLog.print("");

            ConsoleLog.printWarn("ElatiCORE processing interrupted");

            return false;
        }

        return true;
    }

    public void publish(ECoreModelContext ctx, ModelDomain domain) {

        if(!printRuleCheckInfo(ctx, domain))
            return;

        //private void publish(ModelDomain domain) {
        this.ctx = ctx;

        ECoreModel model = domain.getModel();

        EntityModels entityModels = model.getEntityModels();

        ModelComponentItems<Entity> items = entityModels.getItems();

        EntitySrcFilePublisher entityCodePublisher = new EntitySrcFilePublisher(this);
        for (int i = 0; i < items.size(); i++) {
            Entity entity = items.get(i);

            // Runtime-only DSL entities (@runtime) are never code-generated.
            if (entity.getMetaInfo() != null && entity.getMetaInfo().hasMetaAnnotation("runtime")) {
                continue;
            }

            String name = entity.getIdentity().getName();

            entityCodePublisher.publish(domain, entity);
        }

        entityCodePublisher.publish(domain, entityModels);


        EnumFilePublisher enumCodePublisher = new EnumFilePublisher(this);
        EnumModels enumModels = model.getEnumModels();

        while (enumModels.getItems().hasNext()) {
            EnumModel enumModel = enumModels.getItems().next();

            enumCodePublisher.publish(domain, enumModel);
        }

        // Generate Views.java for JsonView support (before DTO generation)
        ViewsSrcFilePublisher viewsPublisher = new ViewsSrcFilePublisher(this);
        viewsPublisher.publish(domain);

        DtoSrcFilePublisher dtoSrcFilePublisher = new DtoSrcFilePublisher(this);
        DataTransferModels dataTransferModels = model.getDataTransferModels();
        ModelComponentItems<DataTransfer> dtoItems = dataTransferModels.getItems();
        for (int i = 0; i < dtoItems.size(); i++) {
            DataTransfer dto = dtoItems.get(i);
            dtoSrcFilePublisher.publish(domain, dto);
        }

        SearchDtoSrcFilePublisher searchDtoSrcFilePublisher = new SearchDtoSrcFilePublisher(this);
        for (int i = 0; i < dtoItems.size(); i++) {
            DataTransfer dto = dtoItems.get(i);
            searchDtoSrcFilePublisher.publish(domain, dto);
        }


        // Repository
        RepositoryFilePublisher repositoryCodePublisher = new RepositoryFilePublisher(this);
        RepositoryModels repositoryModels = model.getRepositoryModels();

        ModelComponentItems<Repository> items1 = repositoryModels.getItems();
        while (items1.hasNext()) {
            Repository repoModel = items1.next();
            repositoryCodePublisher.publish(domain, repoModel);
        }

        RepositoryHelperFilePublisher repositoryHelperFilePublisher = new RepositoryHelperFilePublisher(this);
        repositoryHelperFilePublisher.publish(domain, repositoryModels);


        // mapper
        MapperSrcPublisher mapperSrcPublisher = new MapperSrcPublisher(this);
        mapperSrcPublisher.publish(domain);


        // etc
        EtcSrcFilePublisher etcSrcFilePublisher = new EtcSrcFilePublisher(this);
        etcSrcFilePublisher.publish(domain);


        // for Service
        ServiceSrcPublisher svcPublisher = new ServiceSrcPublisher(this);
        svcPublisher.publish(domain);

        // Test Code Generation - check if testCode is enabled (move here to avoid NullPointerException later)
        String testCodeConfig = model.getConfig("testCode", "false");
        if ("true".equals(testCodeConfig)) {
            try {
                ServiceTestPublisher testPublisher = new ServiceTestPublisher(this);
                testPublisher.publish(domain);
            } catch (Exception e) {
                ConsoleLog.printWarn("Failed to generate test code: " + e.getMessage());
            }
        }

        ControlSrcPublisher controlSrcPublisher = new ControlSrcPublisher(this);
        controlSrcPublisher.publish(domain);

        // Port service
        PortSrcPublisher portSrcPublisher = new PortSrcPublisher(this);
        portSrcPublisher.publish(domain);

        ProtoSrcPublisher protoSrcPublisher = new ProtoSrcPublisher(this);
        protoSrcPublisher.publish(domain);

        printInfo(ctx, domain);
    }


    protected void printInfo(ECoreModelContext ctx, ModelDomain domain) {
        String domainLabel = "  Domain: " + domain.getName() + "  ";
        int boxWidth = Math.max(domainLabel.length(), 40);
        String padded = String.format("%-" + boxWidth + "s", domainLabel);
        // Drawn in ASCII on purpose. Console output crosses three encodings on
        // the way to a terminal - the forked JVM's, the build tool's capture of
        // it, and the terminal's code page - and on a Korean Windows those do
        // not agree, which turned the box-drawing characters this used into
        // mojibake. Nothing else printed here leaves ASCII.
        String line = new String(new char[boxWidth]).replace('\0', '-');
        ConsoleLog.print("");
        ConsoleLog.print("+" + line + "+");
        ConsoleLog.print("|" + padded + "|");
        ConsoleLog.print("+" + line + "+");
        ConsoleLog.print("");

        // DSL validation warnings - show only warnings belonging to this domain
        String domainPrefix = "[" + domain.getName() + "]";
        int dslWarnCount = ConsoleLog.countByPrefix("DSL_WARN", domainPrefix);
        if (dslWarnCount > 0) {
            ConsoleLog.print("[WARN] DSL Validation Warnings:");
            ConsoleLog.print("--------------------------------------");
            ConsoleLog.printAndRemoveByPrefix("DSL_WARN", domainPrefix, "  ", true);
            ConsoleLog.print("");
        }

        // Validation mode: show validation report
        if (ConsoleLog.isValidationMode()) {
            int validCount = ConsoleLog.countStoredLogkey("VALID_OK");
            int diffCount = ConsoleLog.countStoredLogkey("VALIDATION_DIFF");
            int newCount = ConsoleLog.countStoredLogkey("VALIDATION_NEW");
            int userModCount = ConsoleLog.countStoredLogkey("VALID_USER_MODIFIED");

            if (diffCount > 0) {
                ConsoleLog.print("[ERROR] Validation Failed - Stale Generated Files:");
                ConsoleLog.print("======================================");
                ConsoleLog.printStoredWarnLog("VALIDATION_DIFF", "  ");
                ConsoleLog.print("");
            }

            if (newCount > 0) {
                ConsoleLog.print("[WARN] New Files Pending Generation:");
                ConsoleLog.print("--------------------------------------");
                ConsoleLog.printStoredWarnLog("VALIDATION_NEW", "  ");
                ConsoleLog.print("");
            }

            if (userModCount > 0) {
                ConsoleLog.print("[WARN] User-Modified Files:");
                ConsoleLog.print("--------------------------------------");
                ConsoleLog.printStoredWarnLog("VALID_USER_MODIFIED", "  ");
                ConsoleLog.print("");
            }

            if (validCount > 0) {
                ConsoleLog.print("[OK] Validated Files: " + validCount);
                ConsoleLog.print("--------------------------------------");
                ConsoleLog.printStoredLog("VALID_OK", "  ", 5);
                ConsoleLog.print("");
            }

            if (diffCount == 0 && newCount == 0 && userModCount == 0) {
                ConsoleLog.printInfo("[OK] All generated files are up-to-date.");
            }

            // Clear per-domain logs after printing, accumulate counts for final summary.
            // DSL_WARN is intentionally NOT cleared here: warnings for other domains
            // remain in the list and are printed when their own domain is processed.
            // Any orphan DSL_WARN entries are flushed in flushPendingDslWarnings() after all domains complete.
            ConsoleLog.clearKey("VALIDATION_DIFF");
            ConsoleLog.clearKey("VALIDATION_NEW");
            ConsoleLog.clearKey("VALID_USER_MODIFIED");
            ConsoleLog.clearKey("VALID_OK");
            return;
        }

        if (ConsoleLog.countStoredLogkey("TEMPLATE") > 0) {
            ConsoleLog.print("[INFO] Custom Code Templates:");
            ConsoleLog.print("--------------------------------------");
            ConsoleLog.printStoredInfoLog("TEMPLATE", "  ");
            ConsoleLog.print("");
        }

        if (ConsoleLog.countStoredLogkey("NO_MODIFIED") > 0) {
            ConsoleLog.print("[INFO] No Changes Detected:");
            ConsoleLog.print("--------------------------------------");
            ConsoleLog.printStoredLog("NO_MODIFIED", "  ", 3);
            ConsoleLog.print("");
        }

        if (ConsoleLog.countStoredLogkey("USER_MODIFIED") > 0) {
            ConsoleLog.print("[WARN] Externally Modified Files Detected:");
            ConsoleLog.print("--------------------------------------");
            ConsoleLog.printStoredWarnLog("USER_MODIFIED", "  ");
            ConsoleLog.print("");
        }

        if (ConsoleLog.countStoredLogkey("PUBLISH") > 0) {
            ConsoleLog.print("[INFO] New Changes:");
            ConsoleLog.print("--------------------------------------");
            ConsoleLog.printStoredInfoLog("PUBLISH", "  ");
            ConsoleLog.print("");
        }

        if (ConsoleLog.countStoredLogkey("TEST_PUBLISH") > 0) {
            ConsoleLog.print("[INFO] Test Classes Generated:");
            ConsoleLog.print("--------------------------------------");
            ConsoleLog.printStoredInfoLog("TEST_PUBLISH", "  ");
            ConsoleLog.print("");
        }

        // Per-domain cleanup: clear keys consumed by this method, but preserve DSL_WARN
        // so warnings for other domains remain available when their printInfo runs.
        ConsoleLog.clearKey("NO_MODIFIED");
        ConsoleLog.clearKey("USER_MODIFIED");
        ConsoleLog.clearKey("PUBLISH");
        ConsoleLog.clearKey("TEST_PUBLISH");
        ConsoleLog.clearKey("TEMPLATE");
    }
}
