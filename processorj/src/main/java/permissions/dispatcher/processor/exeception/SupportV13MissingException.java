package permissions.dispatcher.processor.exeception;

import javax.lang.model.element.ExecutableElement;

import permissions.dispatcher.processor.RuntimePermissionsElement;

/**
 * Created by Lilei on 2016.
 */

public class SupportV13MissingException extends RuntimeException {
    public static RuntimeException getInstance( RuntimePermissionsElement e) {
        return new SupportV13MissingException("PermissionsDispatcher for annotated class "+e.inputClassName+" can\'t be generated, because the support-v13 dependency is missing on your project");
    }

    private SupportV13MissingException(String s) {
        super(s);
    }
}
