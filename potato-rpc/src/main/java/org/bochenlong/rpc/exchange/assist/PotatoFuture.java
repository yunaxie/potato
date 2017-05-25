package org.bochenlong.rpc.exchange.assist;

import org.bochenlong.rpc.exchange.Response;
import org.bochenlong.rpc.exchange.ResponseCode;

import java.util.concurrent.*;

/**
 * Created by bochenlong on 16-11-14.
 */
public class PotatoFuture implements Future {
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    
    private volatile int status;
    
    public static final int RUNNING = 1;
    private static final int CANCELLED = 2;
    private static final int COMPLETED = 10;
    private static final int ERROR = 50;
    private static final int EXCEPTION = 51;
    
    private volatile Response result;
    
    public PotatoFuture() {
        status = RUNNING;
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return true;
    }
    
    @Override
    public boolean isCancelled() {
        return status == CANCELLED;
    }
    
    @Override
    public boolean isDone() {
        return status == COMPLETED;
    }
    
    @Override
    public Response get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return result();
    }
    
    @Override
    public Response get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (countDownLatch.await(timeout, unit)) {
            return result();
        }
        throw new TimeoutException("future get the result timeout");
    }
    
    private Response result() throws ExecutionException, InterruptedException {
        switch (this.status) {
            case COMPLETED:
                return this.result;
            case ERROR:
                throw new ExecutionException(new Throwable((String) this.result.getData()));
            case EXCEPTION:
                throw new ExecutionException(new Throwable((String) this.result.getData()));
            case CANCELLED:
                throw new InterruptedException("future have be interrupted");
            default:
                throw new ExecutionException(new Throwable("UnKnow exception when future get"));
        }
    }
    
    public void set(Response v) {
        this.result = v;
        changeStatus(v);
    }
    
    public void cancel() {
        changeStatus(CANCELLED);
    }
    
    public int getStatus() {
        return this.status;
    }
    
    private void changeStatus(Response v) {
        if (v.getCode() == ResponseCode.ERROR.getValue()) {
            changeStatus(ERROR);
        } else if (v.getCode() == ResponseCode.EXCEPTION.getValue()) {
            changeStatus(EXCEPTION);
        } else
            changeStatus(COMPLETED);
    }
    
    private synchronized void changeStatus(int status) {
        if (this.status != RUNNING) {
            return;
        }
        this.status = status;
        this.countDownLatch.countDown();
    }
}
