package permissions.dispatcher.processor.exeception;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by Lilei on 2016.
 */

public class PrivateMethodException extends RuntimeException {
    public static RuntimeException getInstance(ExecutableElement e,Class annotationType) {
        return new PrivateMethodException("Method "+e.getSimpleName().toString()+" annotated with "+annotationType.getSimpleName()+" must not be private");
    }

    private PrivateMethodException(String s) {
        super(s);
    }
}
