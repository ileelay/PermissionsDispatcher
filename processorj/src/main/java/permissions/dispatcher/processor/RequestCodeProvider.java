package permissions.dispatcher.processor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Lilei on 2016.
 */

public class RequestCodeProvider {


    AtomicInteger currentCode = new AtomicInteger(0);


    public int nextRequestCode() {
        return currentCode.getAndIncrement();
    }

}
