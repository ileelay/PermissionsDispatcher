package permissions.dispatcher.processor.exeception;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by Lilei on 2016.
 */

public class DuplicatedValueException extends RuntimeException{

    public static RuntimeException getInstance(List<String> value, ExecutableElement e, Class annotation) {

        return new DuplicatedValueException(value+" is duplicated in "+e.getSimpleName().toString()+" annotated with "+annotation.getSimpleName());
    }

    private DuplicatedValueException(String s) {
        super(s);
    }
}
