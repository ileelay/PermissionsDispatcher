package permissions.dispatcher.processor.exeception;

import javax.lang.model.element.ExecutableElement;

import permissions.dispatcher.processor.RuntimePermissionsElement;

/**
 * Created by Lilei on 2016.
 */

public class NoParametersAllowedException extends RuntimeException {

    public static RuntimeException getInstance(ExecutableElement e) {

        return new NoParametersAllowedException("Method "+e.getSimpleName().toString()+" must not have any parameters");
    }

    private NoParametersAllowedException(String s) {
        super(s);
    }
}
