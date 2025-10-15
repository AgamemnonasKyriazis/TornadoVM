package uk.ac.manchester.tornado.examples.cgra;

import uk.ac.manchester.tornado.api.ImmutableTaskGraph;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;
import uk.ac.manchester.tornado.api.exceptions.TornadoExecutionPlanException;
import uk.ac.manchester.tornado.api.types.arrays.IntArray;
import uk.ac.manchester.tornado.api.GridScheduler;
import uk.ac.manchester.tornado.api.KernelContext;
import uk.ac.manchester.tornado.api.WorkerGrid;
import uk.ac.manchester.tornado.api.WorkerGrid1D;

import java.util.Arrays;

public class Compute1D {
    
    private static void nKernel(KernelContext context, IntArray A, IntArray B, IntArray C) {
        int idx = context.globalIdx;
        C.set(idx, A.get(idx) * B.get(idx));
    }

    public static void main(String[] args) throws TornadoExecutionPlanException {
        final int numElements = 32;
        IntArray A = new IntArray(numElements);
        IntArray B = new IntArray(numElements);
        IntArray C = new IntArray(numElements);
        A.init(0x1);
        B.init(0x2);
        C.init(0x0);

        WorkerGrid workerGrid = new WorkerGrid1D(numElements);    // Create a 2D Worker
        GridScheduler gridScheduler = new GridScheduler("s0.t0", workerGrid);  // Attach the worker to the Grid
        KernelContext context = new KernelContext();             // Create a context

        TaskGraph taskGraph = new TaskGraph("s0") //
                .transferToDevice(DataTransferMode.FIRST_EXECUTION, A, B) //
                .task("t0", Compute1D::nKernel, context, A, B, C) //
                .transferToHost(DataTransferMode.EVERY_EXECUTION, C);

        ImmutableTaskGraph immutableTaskGraph = taskGraph.snapshot();
        try (TornadoExecutionPlan executor = new TornadoExecutionPlan(immutableTaskGraph)) {
            executor.withGridScheduler(gridScheduler);
            executor.execute();
        }

        System.out.println("C: " + Arrays.toString(C.toHeapArray()));
    }
}