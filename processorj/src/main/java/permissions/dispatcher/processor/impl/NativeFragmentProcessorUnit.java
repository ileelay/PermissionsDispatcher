package permissions.dispatcher.processor.impl;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.type.TypeMirror;

import permissions.dispatcher.processor.RuntimePermissionsElement;
import permissions.dispatcher.processor.exeception.SupportV13MissingException;

import static permissions.dispatcher.processor.util.Helpers.typeMirrorOf;

/**
 * Created by Lilei on 2016.
 */

public class NativeFragmentProcessorUnit extends BaseProcessorUnit {
    private ClassName PERMISSION_UTILS_V13 = ClassName.get("permissions.dispatcher.v13", "PermissionUtilsV13");

    @Override
    void checkPrerequisites(RuntimePermissionsElement rpe) {
        try {
            // Check if FragmentCompat can be accessed; if not, throw an exception
            Class.forName("android.support.v13.app.FragmentCompat");

        } catch (ClassNotFoundException e) {
            // Thrown if support-v13 is missing on the classpath
            throw SupportV13MissingException.getInstance(rpe);

        } catch (NoClassDefFoundError e) {
            // Expected in success cases, because the Android environment is still missing
            // when this is called from within the Annotation processor. 'FragmentCompat' itself
            // can be resolved, but accessing it requires an Android environment, which doesn't exist
            // since this is an annotation processor
        }
    }

    @Override
    void addRequestPermissionsStatement(MethodSpec.Builder builder, String targetParam, String permissionField, String requestCodeField) {
        builder.addStatement("$T.getInstance().requestPermissions($N, $N, $N)", PERMISSION_UTILS_V13, targetParam, permissionField, requestCodeField);
    }

    @Override
    void addShouldShowRequestPermissionRationaleCondition(MethodSpec.Builder builder, String targetParam, String permissionField, boolean isPositiveCondition) {
        builder.beginControlFlow("if ($N$T.getInstance().shouldShowRequestPermissionRationale($N, $N))", isPositiveCondition ? "" : "!", PERMISSION_UTILS_V13, targetParam, permissionField);
    }

    @Override
    String getActivityName(String targetParam) {
        return targetParam + ".getActivity()";
    }

    @Override
    public TypeMirror getTargetType() {
        return typeMirrorOf("android.app.Fragment");
    }
}
