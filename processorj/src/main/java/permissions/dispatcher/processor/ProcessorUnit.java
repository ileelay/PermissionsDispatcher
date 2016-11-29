package permissions.dispatcher.processor;

import com.squareup.javapoet.JavaFile;

import javax.lang.model.type.TypeMirror;

/**
 * Created by Lilei on 2016.
 */

public interface ProcessorUnit {



   TypeMirror getTargetType();

    JavaFile createJavaFile( RuntimePermissionsElement rpe, RequestCodeProvider requestCodeProvider);
}
