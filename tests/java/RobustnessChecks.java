import edu.ufl.ads.jobscheduler.datastructure.AvlTree;
import edu.ufl.ads.jobscheduler.datastructure.WaitingMaxHeap;
import edu.ufl.ads.jobscheduler.io.JobSchedulerApplication;
import edu.ufl.ads.jobscheduler.model.Job;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

/** Contract and regression checks without external testing libraries. */
public final class RobustnessChecks {
    public static void main(String[] args) throws Exception {
        WaitingMaxHeap heap = new WaitingMaxHeap();
        rejects(NullPointerException.class, () -> heap.add(null));
        check(heap.size() == 0 && heap.statistics().insertions() == 0,
                "Rejected null insertion must not mutate the heap");
        check(heap.extractMax() == null, "Empty extraction remains nullable");
        rejects(NullPointerException.class, () -> heap.priorityChanged(null));
        AvlTree tree = new AvlTree();
        rejects(NullPointerException.class, () -> tree.insert(null));
        check(tree.size() == 0 && tree.statistics().insertions() == 0,
                "Rejected null insertion must not mutate the tree");
        Job job = new Job(7, 0, 3, 10, 0);
        heap.add(job);
        heap.add(new Job(8, 0, 2, 10, 0));
        check(heap.extractMax() == job && heap.extractMax().getId() == 8,
                "Heap remains usable after rejected null insertion");
        check(job.getStartTime() == null && job.getCompletionTime() == null,
                "Waiting job timestamps must remain null");
        Method outputPath = JobSchedulerApplication.class.getDeclaredMethod("outputPath", Path.class);
        outputPath.setAccessible(true);
        try {
            outputPath.invoke(null, Path.of("/"));
            throw new AssertionError("Root path should be rejected");
        } catch (InvocationTargetException error) {
            check(error.getCause() instanceof IllegalArgumentException,
                    "Root path should have a descriptive argument error");
        }
        Path input = Files.createTempFile("scheduler-invalid-", ".txt");
        try {
            for (String command : new String[] {"0", "0 SUBMIT 1", "0 FIND", "0 PROCESS_NEXT extra"}) {
                Files.writeString(input, "CAPACITY 1\n" + command + "\n");
                rejects(IllegalArgumentException.class,
                        () -> JobSchedulerApplication.main(new String[] {input.toString()}));
            }
        } finally {
            Files.deleteIfExists(input);
        }
        System.out.println("PASS: null contracts, heap integrity, output paths, malformed command validation");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void rejects(Class<? extends Throwable> type, Action action) throws Exception {
        try {
            action.run();
        } catch (Throwable error) {
            if (type.isInstance(error)) return;
            throw new AssertionError("Expected " + type.getName() + " but got " + error, error);
        }
        throw new AssertionError("Expected " + type.getName());
    }

    @FunctionalInterface
    private interface Action { void run() throws Exception; }
}
