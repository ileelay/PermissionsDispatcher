package permissions.dispatcher.processor.exeception;

import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by Lilei on 2016.
 */

public class WrongParametersException extends RuntimeException {

    public static RuntimeException getInstance(ExecutableElement e, List<TypeMirror> requiredTypes) {
        return new WrongParametersException("Method  must declare parameters of type");
    }

    private WrongParametersException(String s) {
        super(s);
    }
}
