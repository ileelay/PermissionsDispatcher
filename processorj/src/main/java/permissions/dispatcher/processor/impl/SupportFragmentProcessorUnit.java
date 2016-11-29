package permissions.dispatcher.processor.impl;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.type.TypeMirror;

import permissions.dispatcher.processor.RuntimePermissionsElement;

import static permissions.dispatcher.processor.util.Helpers.typeMirrorOf;

/**
 * Created by Lilei on 2016.
 */

public class SupportFragmentProcessorUnit extends BaseProcessorUnit {
    @Override
    void checkPrerequisites(RuntimePermissionsElement rpe) {

    }

    @Override
    void addRequestPermissionsStatement(MethodSpec.Builder builder, String targetParam, String permissionField, String requestCodeField) {
        builder.addStatement("$N.requestPermissions($N, $N)", targetParam, permissionField, requestCodeField);
    }

    @Override
    void addShouldShowRequestPermissionRationaleCondition(MethodSpec.Builder builder, String targetParam, String permissionField, boolean isPositiveCondition) {
        builder.beginControlFlow("if ($N$T.shouldShowRequestPermissionRationale($N.getActivity(), $N))", isPositiveCondition ? "" : "!", PERMISSION_UTILS, targetParam, permissionField);
    }

    @Override
    String getActivityName(String targetParam) {
        return targetParam + ".getActivity()";
    }

    @Override
    public TypeMirror getTargetType() {
        return typeMirrorOf("android.support.v4.app.Fragment");
    }
}
