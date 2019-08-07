package ru.runa.gpd.util;

import com.google.common.base.Throwables;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Vitaly Alekseev
 *
 * @since Aug 6, 2019
 */
public final class LambdaUtils {

    public static void call(WithExceptionFunction c) {
        try {
            c.call();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Throwables.propagate(e);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static <T> void forEach(List<T> list, IterateWithIndexFunction<T> callback) {
        IntStream.range(0, list.size()).forEach(i -> call(() -> callback.call(i, list.get(i))));
    }

    public static <T> T get(SupplierWithException<T> supplier) {
        try {
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Throwables.propagate(e);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private LambdaUtils() {
    }

    public static interface WithExceptionFunction {

        public void call() throws Exception;
    }

    public static interface IterateWithIndexFunction<T> {

        public void call(int index, T entry) throws Exception;
    }

    public static interface SupplierWithException<T> {

        T get() throws Exception;
    }
}
