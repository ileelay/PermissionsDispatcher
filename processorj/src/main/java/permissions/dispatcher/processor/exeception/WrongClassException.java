package permissions.dispatcher.processor.exeception;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

import permissions.dispatcher.processor.RuntimePermissionsElement;

/**
 * Created by Lilei on 2016.
 */

public class WrongClassException extends RuntimeException {
    public static RuntimeException getInstance(TypeMirror type) {
        return new WrongClassException("Class "+ TypeName.get(type).toString()+" can't be annotated with '@RuntimePermissions'");
    }

    private WrongClassException(String s) {
        super(s);
    }
}
