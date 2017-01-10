package common;

/**
 * 24/01/2011
 * @author dicri02
 *
 * A class to help benchmark code
 * It simulates a real stop watch
 */
import java.util.concurrent.TimeUnit;

public class StopWatch {

    private long startTime = -1;
    private long stopTime = -1;
    private boolean running = false;

    public StopWatch start() {
       startTime = System.currentTimeMillis();
       running = true;
       return this;
    }
    public StopWatch stop() {
       stopTime = System.currentTimeMillis();
       running = false;
       return this;
    }
    /** returns elapsed time in seconds
      * if the watch hasn't been started then return zero
      */
    public long getElapsedTime() {
       if (startTime == -1) {
            return 0;
       }
       if (running){
           long millis = System.currentTimeMillis() - startTime;
           return TimeUnit.MILLISECONDS.toSeconds(millis);
       } else {
           long millis = stopTime-startTime;
           return TimeUnit.MILLISECONDS.toSeconds(millis);
       }
    }

    public StopWatch reset() {
       startTime = -1;
       stopTime = -1;
       running = false;
       return this;
    }
}
