package uk.ac.manchester.tornado.examples.cgra;

import uk.ac.manchester.tornado.api.ImmutableTaskGraph;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;
import uk.ac.manchester.tornado.api.exceptions.TornadoExecutionPlanException;
import uk.ac.manchester.tornado.api.types.arrays.IntArray;

import java.util.Arrays;

public class Mem {

    public static void noop(IntArray inArray, IntArray outArray) {
        for (@Parallel int i = 0; i < inArray.getSize(); i++) {
            outArray.set(i, inArray.get(i));
        }
    }


    public static void main(String[] args) throws TornadoExecutionPlanException {

        final int numElements = 8;
        IntArray iBuf = new IntArray(numElements);
        IntArray oBuf = new IntArray(numElements);

        iBuf.init(0xDEADBEEF);
        oBuf.init(0x00000000);

        TaskGraph taskGraph = new TaskGraph("s0") //
                .transferToDevice(DataTransferMode.FIRST_EXECUTION, iBuf) //
                .task("t0", Mem::noop, iBuf, oBuf) //
                .transferToHost(DataTransferMode.EVERY_EXECUTION, oBuf);

        ImmutableTaskGraph immutableTaskGraph = taskGraph.snapshot();
        try (TornadoExecutionPlan executor = new TornadoExecutionPlan(immutableTaskGraph)) {
            executor.execute();
        }

        System.out.println("a: " + Arrays.toString(iBuf.toHeapArray()));
        System.out.println("b: " + Arrays.toString(oBuf.toHeapArray()));
    }

}
