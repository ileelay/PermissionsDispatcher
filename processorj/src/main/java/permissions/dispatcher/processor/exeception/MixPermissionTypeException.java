package permissions.dispatcher.processor.exeception;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by Lilei on 2016.
 */

public class MixPermissionTypeException extends RuntimeException{
    public static RuntimeException getInstance(  ExecutableElement e, String permissionName) {

        return new MixPermissionTypeException("Method "+e.getSimpleName().toString()+" defines "+permissionName+" with other permissions at the same time.");
    }

    private MixPermissionTypeException(String s) {
        super(s);
    }
}
