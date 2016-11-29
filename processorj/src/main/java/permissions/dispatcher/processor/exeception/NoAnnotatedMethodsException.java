package permissions.dispatcher.processor.exeception;

import permissions.dispatcher.processor.RuntimePermissionsElement;

/**
 * Created by Lilei on 2016.
 */

public class NoAnnotatedMethodsException extends RuntimeException {

    public static RuntimeException getInstance(RuntimePermissionsElement rpe, Class type) {

        return new NoAnnotatedMethodsException("Annotated class "+rpe.inputClassName+" doesn't have any method annotated with "+type.getSimpleName()+"");
    }

    private NoAnnotatedMethodsException(String s) {
        super(s);
    }
}
