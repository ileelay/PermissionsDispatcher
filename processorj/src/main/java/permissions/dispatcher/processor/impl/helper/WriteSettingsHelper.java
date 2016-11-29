package permissions.dispatcher.processor.impl.helper;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

/**
 * Created by Lilei on 2016.
 */

public class WriteSettingsHelper implements SensitivePermissionInterface {

    private ClassName PERMISSION_UTILS = ClassName.get("permissions.dispatcher", "PermissionUtils");
    private ClassName SETTINGS = ClassName.get("android.provider", "Settings");
    private ClassName INTENT = ClassName.get("android.content", "Intent");
    private ClassName URI = ClassName.get("android.net", "Uri");

    @Override
    public void addHasSelfPermissionsCondition(MethodSpec.Builder builder, String activityVar, String permissionField) {
        builder.beginControlFlow("if ($T.hasSelfPermissions($N, $N) || $T.System.canWrite($N))", PERMISSION_UTILS, activityVar, permissionField, SETTINGS, activityVar);
    }

    @Override
    public void addRequestPermissionsStatement(MethodSpec.Builder builder, String activityVar, String requestCodeField) {
        builder.addStatement("$T intent = new $T($T.ACTION_MANAGE_WRITE_SETTINGS, $T.parse(\"package:\" + $N.getPackageName()))", INTENT, INTENT, SETTINGS, URI, activityVar);
        builder.addStatement("$N.startActivityForResult(intent, $N)", activityVar, requestCodeField);

    }
}
