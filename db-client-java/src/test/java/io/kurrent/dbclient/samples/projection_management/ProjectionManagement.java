package io.kurrent.dbclient.samples.projection_management;

import io.kurrent.dbclient.*;
import org.junit.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProjectionManagement {

    @Test
    public void testProjectionManagementSamples() throws Throwable {

        Database database = DatabaseFactory.spawn();
        try {
            KurrentDBProjectionManagementClient client = KurrentDBProjectionManagementClient.from(database.defaultClient());
            disable(client);
            disableNotFound(client);
            enable(client);
            enableNotFound(client);
            delete(client);
            deleteNotFound(client);
            abort(client);
            abortNotFound(client);
            reset(client);
            resetNotFound(client);
            create(client);
            createConflict(client);
            update(client);
            updateNotFound(client);
            restartSubSystem(client);
        } finally {
            database.dispose();
        }
    }

    private static KurrentDBProjectionManagementClient createClient(String connection) {
        // region createClient
        KurrentDBClientSettings settings = KurrentDBConnectionString.parseOrThrow(connection);
        KurrentDBProjectionManagementClient client = KurrentDBProjectionManagementClient.create(settings);
        // endregion createClient

        Assert.assertNotNull(client);

        return client;
    }

    private static void disable(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region Disable
        client.disable("$by_category").get();
        // endregion Disable
    }

    private static void disableNotFound(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region DisableNotFound
        try {
            client.disable("projection that does not exists").get();
        } catch (ExecutionException ex) {
            if (ex.getMessage().contains("NotFound")) {
                System.out.println(ex.getMessage());
            }
        }
        // endregion DisableNotFound
    }

    private static void enable(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region Enable
        client.enable("$by_category").get();
        // endregion Enable
    }

    private static void enableNotFound(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region EnableNotFound
        try {
            client.disable("projection that does not exists").get();
        } catch (ExecutionException ex) {
            if (ex.getMessage().contains("NotFound")) {
                System.out.println(ex.getMessage());
            }
        }
        // endregion EnableNotFound
    }

    private static void delete(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        String name = "to-be-deleted-projection";
        client.create(name, "fromAll().when()").get();

        // region Delete
        // A projection must be disabled to allow it to be deleted.
        client.disable(name).get();
        // The projection can now be deleted
        client.delete(name).get();
        // endregion Delete
    }

    private static void deleteNotFound(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region DeleteNotFound
        try {
            client.delete("projection that does not exists").get();
        } catch (ExecutionException ex) {
            if (ex.getMessage().contains("NotFound")) {
                System.out.println(ex.getMessage());
            }
        }
        // endregion DeleteNotFound
    }

    private static void abort(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region Abort
        client.abort("$by_category").get();
        // endregion Abort
    }

    private static void abortNotFound(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region Abort_NotFound
        try {
            client.abort("projection that does not exists").get();
        } catch (ExecutionException ex) {
            if (ex.getMessage().contains("NotFound")) {
                System.out.println(ex.getMessage());
            }
        }
        // endregion Abort_NotFound
    }

    private static void reset(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region Reset
        client.reset("$by_category").get();
        // endregion Reset
    }

    private static void resetNotFound(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region Reset_NotFound
        try {
            client.reset("projection that does not exists").get();
        } catch (ExecutionException ex) {
            if (ex.getMessage().contains("NotFound")) {
                System.out.println(ex.getMessage());
            }
        }
        // endregion Reset_NotFound
    }

    private static void create(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region CreateContinuous
        String js =
                "fromAll()" +
                ".when({" +
                "    $init: function() {" +
                "        return {" +
                "            count: 0" +
                "        };" +
                "    }," +
                "    $any: function(s, e) {" +
                "        s.count += 1;" +
                "    }" +
                "})" +
                ".outputState();";

        String name = "countEvents_Create_" + java.util.UUID.randomUUID();

        client.create(name, js).get();
        // endregion CreateContinuous
    }

    private static void createConflict(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        String js = "{}";
        String name = "projection" + java.util.UUID.randomUUID();

        // region CreateContinuous_Conflict
        try {
            client.create(name, js).get();
        } catch (ExecutionException ex) {
            if (ex.getMessage().contains("Conflict")) {
                System.out.println(name + " already exists");
            }
        }
        // endregion CreateContinuous_Conflict
    }

    private static void update(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region Update
        String name = "countEvents_Update_" + java.util.UUID.randomUUID();
        String js =
                "fromAll()" +
                        ".when({" +
                        "    $init: function() {" +
                        "        return {" +
                        "            count: 0" +
                        "        };" +
                        "    }," +
                        "    $any: function(s, e) {" +
                        "        s.count += 1;" +
                        "    }" +
                        "})" +
                        ".outputState();";

        client.create(name, "fromAll().when()").get();
        client.update(name, js).get();
        // endregion Update
    }

    private static void updateNotFound(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region Update_NotFound
        try {
            client.update("Update Not existing projection", "fromAll().when()").get();
        } catch (ExecutionException ex) {
            if (ex.getMessage().contains("NotFound")) {
                System.out.println("'Update Not existing projection' does not exists and can not be updated");
            }
        }
        // endregion Update_NotFound
    }

    private static void listAll(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region ListAll
        List<ProjectionDetails> details = client.list().get();

        for (ProjectionDetails detail: details) {
            System.out.println(
                detail.getName() + ", " +
                detail.getStatus() + ", " +
                detail.getCheckpointStatus() + ", " +
                detail.getMode() + ", " +
                detail.getProgress());
        }
        // endregion ListAll

        Assert.assertTrue(details.size() >= 5);
    }

    private static void list(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region ListContinuous
        List<ProjectionDetails> details = client.list().get();

        for (ProjectionDetails detail: details) {
            System.out.println(
                detail.getName() + ", " +
                detail.getStatus() + ", " +
                detail.getCheckpointStatus() + ", " +
                detail.getMode() + ", " +
                detail.getProgress());
        }
        // endregion ListContinuous

        Assert.assertTrue(details.size() >= 5);
    }

    private static void getStatus(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region GetStatus
        ProjectionDetails status = client.getStatus("$by_category").get();
        System.out.println(
            status.getName() + ", " +
            status.getStatus() + ", " +
            status.getCheckpointStatus() + ", " +
            status.getMode() + ", " +
            status.getProgress());
        // endregion GetStatus

        Assert.assertEquals("$by_category", status.getName());
    }

    private static void getState(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region GetState
        // This example requires the following class to be defined:
        //
        //    public static class CountResult {
        //        private int count;
        //        public int getCount() {
        //            return count;
        //        }
        //        public void setCount(final int count){
        //            this.count = count;
        //        }
        //    }
        String name = "get_state_example";
        String js =
            "fromAll()" +
            ".when({" +
            "    $init() {" +
            "        return {" +
            "            count: 0," +
            "        };" +
            "    }," +
            "    $any(s, e) {" +
            "        s.count += 1;" +
            "    }" +
            "})" +
            ".outputState();";

        client.create(name, js).get();

        Thread.sleep(500); //give it some time to process and have a state.

        CountResult result = client
            .getState(name, CountResult.class)
            .get();

        System.out.println(result);
        // endregion GetState

        Assert.assertTrue(result.getCount() > 0);
    }

    private static void getResult(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region GetResult
        String name = "get_result_example";
        String js =
            "fromAll()" +
            ".when({" +
            "    $init() {" +
            "        return {" +
            "            count: 0," +
            "        };" +
            "    }," +
            "    $any(s, e) {" +
            "        s.count += 1;" +
            "    }" +
            "})" +
            ".transformBy((state) => state.count)" +
            ".outputState();";

        client.create(name, js).get();

        Thread.sleep(500); //give it some time to process and have a state.

        int result = client
                .getResult(name, int.class)
                .get();

        System.out.println(result);
        // endregion GetResult

        Assert.assertTrue(result > 0);
    }

    private static void restartSubSystem(KurrentDBProjectionManagementClient client)
            throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
        // region RestartSubSystem
        client.restartSubsystem().get();
        // endregion RestartSubSystem
    }

    public static class CountResult {
        private int count;
        public int getCount() {
            return count;
        }
        public void setCount(final int count){
            this.count = count;
        }
    }
}
