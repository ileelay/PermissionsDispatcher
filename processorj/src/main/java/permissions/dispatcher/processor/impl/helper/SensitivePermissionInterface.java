package permissions.dispatcher.processor.impl.helper;

import com.squareup.javapoet.MethodSpec;

/**
 * Created by Lilei on 2016.
 */

public interface SensitivePermissionInterface {

    void addHasSelfPermissionsCondition(MethodSpec.Builder builder, String activityVar, String permissionField);

    void addRequestPermissionsStatement(MethodSpec.Builder builder, String activityVar, String requestCodeField);
}
