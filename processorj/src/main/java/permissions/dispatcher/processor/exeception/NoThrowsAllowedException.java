package permissions.dispatcher.processor.exeception;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by Lilei on 2016.
 */

public class NoThrowsAllowedException extends RuntimeException {

    public static RuntimeException getInstance(ExecutableElement e) {
        return new NoThrowsAllowedException("Method "+e.getSimpleName().toString()+"must not have any 'throws' declaration in its signature");
    }

    private NoThrowsAllowedException(String s) {
        super(s);
    }
}
