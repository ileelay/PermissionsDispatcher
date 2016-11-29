package permissions.dispatcher.processor.impl;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.type.TypeMirror;

import permissions.dispatcher.processor.RuntimePermissionsElement;

import static permissions.dispatcher.processor.util.Helpers.typeMirrorOf;

/**
 * Created by Lilei on 2016.
 */

public class ActivityProcessorUnit extends BaseProcessorUnit {
    private ClassName ACTIVITY_COMPAT = ClassName.get("android.support.v4.app", "ActivityCompat");

    @Override
    void checkPrerequisites(RuntimePermissionsElement rpe) {

    }

    @Override
    void addRequestPermissionsStatement(MethodSpec.Builder builder, String targetParam, String permissionField, String requestCodeField) {
        builder.addStatement("$T.requestPermissions($N, $N, $N)", ACTIVITY_COMPAT, targetParam, permissionField, requestCodeField);
    }

    @Override
    void addShouldShowRequestPermissionRationaleCondition(MethodSpec.Builder builder, String targetParam, String permissionField, boolean isPositiveCondition) {
        builder.beginControlFlow("if ($N$T.shouldShowRequestPermissionRationale($N, $N))", isPositiveCondition ? "" : "!", PERMISSION_UTILS, targetParam, permissionField);
    }

    @Override
    String getActivityName(String targetParam) {
        return targetParam;
    }

    @Override
    public TypeMirror getTargetType() {
        return typeMirrorOf("android.app.Activity");
    }
}
