package permissions.dispatcher.processor.exeception;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by Lilei on 2016.
 */

public class WrongReturnTypeException extends RuntimeException {
    public static RuntimeException getInstance(ExecutableElement e) {
        return new WrongReturnTypeException("Method "+e.getSimpleName().toString()+" must specify return type 'void', not "+e.getReturnType());
    }

    private WrongReturnTypeException(String s) {
        super(s);
    }
}
