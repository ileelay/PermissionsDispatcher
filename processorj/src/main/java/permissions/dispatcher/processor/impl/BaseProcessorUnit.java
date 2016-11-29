package permissions.dispatcher.processor.impl;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.processor.ProcessorUnit;
import permissions.dispatcher.processor.RequestCodeProvider;
import permissions.dispatcher.processor.RuntimePermissionsElement;
import permissions.dispatcher.processor.impl.helper.SensitivePermissionInterface;
import permissions.dispatcher.processor.impl.helper.SystemAlertWindowHelper;
import permissions.dispatcher.processor.impl.helper.WriteSettingsHelper;
import permissions.dispatcher.processor.util.Extensions;

import static permissions.dispatcher.processor.util.Constants.FILE_COMMENT;
import static permissions.dispatcher.processor.util.Helpers.pendingRequestFieldName;
import static permissions.dispatcher.processor.util.Helpers.permissionFieldName;
import static permissions.dispatcher.processor.util.Helpers.permissionRequestTypeName;
import static permissions.dispatcher.processor.util.Helpers.requestCodeFieldName;
import static permissions.dispatcher.processor.util.Helpers.typeNameOf;
import static permissions.dispatcher.processor.util.Helpers.varargsParametersCodeBlock;
import static permissions.dispatcher.processor.util.Helpers.withCheckMethodName;

/**
 * Created by Lilei on 2016.
 */

public abstract class BaseProcessorUnit implements ProcessorUnit {

    protected ClassName PERMISSION_UTILS = ClassName.get("permissions.dispatcher", "PermissionUtils");
    private ClassName BUILD = ClassName.get("android.os", "Build");
    private String MANIFEST_WRITE_SETTING = "android.permission.WRITE_SETTINGS";
    private String MANIFEST_SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
    private Map<String, SensitivePermissionInterface> ADD_WITH_CHECK_BODY_MAP = new HashMap<String, SensitivePermissionInterface>();

    {
        ADD_WITH_CHECK_BODY_MAP.put(MANIFEST_SYSTEM_ALERT_WINDOW, new SystemAlertWindowHelper());
        ADD_WITH_CHECK_BODY_MAP.put(MANIFEST_WRITE_SETTING, new WriteSettingsHelper());
    }

    @Override
    public JavaFile createJavaFile(RuntimePermissionsElement rpe, RequestCodeProvider requestCodeProvider) {

        // Check the prerequisites for creating a Java file for this element
        checkPrerequisites(rpe);

        return JavaFile.builder(rpe.packageName, createTypeSpec(rpe, requestCodeProvider))
                .addFileComment(FILE_COMMENT)
                .build();

    }

    abstract void checkPrerequisites(RuntimePermissionsElement rpe);

    abstract void addRequestPermissionsStatement(MethodSpec.Builder builder, String targetParam, String permissionField, String requestCodeField);

    abstract void addShouldShowRequestPermissionRationaleCondition(MethodSpec.Builder builder, String targetParam, String permissionField, boolean isPositiveCondition);

    abstract String getActivityName(String targetParam);

    private TypeSpec createTypeSpec(RuntimePermissionsElement rpe, RequestCodeProvider requestCodeProvider) {
        return TypeSpec.classBuilder(rpe.generatedClassName)
                .addModifiers(Modifier.FINAL)
                .addFields(createFields(rpe.needsElements, requestCodeProvider))
                .addMethod(createConstructor())
                .addMethods(createWithCheckMethods(rpe))
                .addMethods(createPermissionHandlingMethods(rpe))
                .addTypes(createPermissionRequestClasses(rpe))
                .build();
    }

    private List<FieldSpec> createFields(List<ExecutableElement> needsElements, RequestCodeProvider requestCodeProvider) {
        List<FieldSpec> fields = new ArrayList<FieldSpec>();
        for (ExecutableElement it : needsElements) {
            // For each method annotated with @NeedsPermission, add REQUEST integer and PERMISSION String[] fields
            fields.add(createRequestCodeField(it, requestCodeProvider.nextRequestCode()));
            fields.add(createPermissionField(it));

            if (!it.getTypeParameters().isEmpty()) {
                fields.add(createPendingRequestField(it));
            }
        }
        return fields;
    }

    private FieldSpec createRequestCodeField(ExecutableElement e, int index) {
        return FieldSpec.builder(int.class, requestCodeFieldName(e))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", index)
                .build();
    }

    private FieldSpec createPermissionField(ExecutableElement e) {
        List<String> permissionValue = Extensions.AnnotationPermissionValue(e.getAnnotation(NeedsPermission.class));

        //        val formattedValue: String = permissionValue.joinToString(
//                separator = ",",
//                prefix = "{",
//                postfix = "}",
//                transform = { "\"$it\"" }
//        )

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < permissionValue.size(); i++) {
            String it = permissionValue.get(i);
            sb.append("\"").append(it).append("\"");
            if (i != permissionValue.size() - 1)
                sb.append(",");
        }
        sb.append("}");
        String formattedValue = sb.toString();
        return FieldSpec.builder(ArrayTypeName.of(String.class), permissionFieldName(e))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$N", "new String[] " + formattedValue)
                .build();
    }

    private FieldSpec createPendingRequestField(ExecutableElement e) {
        return FieldSpec.builder(ClassName.get("permissions.dispatcher", "GrantableRequest"), pendingRequestFieldName(e))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .build();
    }

    private MethodSpec createConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private List<MethodSpec> createWithCheckMethods(RuntimePermissionsElement rpe) {
        ArrayList<MethodSpec> methods = new ArrayList<MethodSpec>();
        for (ExecutableElement it : rpe.needsElements) {
            methods.add(createWithCheckMethod(rpe, it));
        }

        return methods;
    }

    private MethodSpec createWithCheckMethod(RuntimePermissionsElement rpe, ExecutableElement method) {
        String targetParam = "target";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(withCheckMethodName(method))
                .addTypeVariables(rpe.typeVariables)
                .addModifiers(Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(rpe.typeName, targetParam);

        // If the method has parameters, add those as well
        for (VariableElement it : method.getParameters()) {
            builder.addParameter(typeNameOf(it), Extensions.ElementSimpleString(it));
        }


        // Delegate method body generation to implementing classes
        addWithCheckBody(builder, method, rpe, targetParam);
        return builder.build();
    }

    private void addWithCheckBody(MethodSpec.Builder builder, ExecutableElement needsMethod, RuntimePermissionsElement rpe, String targetParam) {
        // Create field names for the constants to use
        String requestCodeField = requestCodeFieldName(needsMethod);
        String permissionField = permissionFieldName(needsMethod);

        // if maxSdkVersion is lower than os level does nothing
        int needsPermissionMaxSdkVersion = needsMethod.getAnnotation(NeedsPermission.class).maxSdkVersion();
        if (needsPermissionMaxSdkVersion > 0) {
            builder.beginControlFlow("if ($T.VERSION.SDK_INT > $L)", BUILD, needsPermissionMaxSdkVersion)
                    .addCode(CodeBlock.builder()
                            .add("$N.$N(", targetParam, Extensions.ElementSimpleString(needsMethod))
                            .add(varargsParametersCodeBlock(needsMethod))
                            .addStatement(")")
                            .addStatement("return")
                            .build())
                    .endControlFlow();
        }

        // Add the conditional for when permission has already been granted
        String needsPermissionParameter = (needsMethod.getAnnotation(NeedsPermission.class)).value()[0];
        String activityVar = getActivityName(targetParam);
        SensitivePermissionInterface anInterface = ADD_WITH_CHECK_BODY_MAP.get(needsPermissionParameter);
        if (anInterface != null) {
            anInterface.addHasSelfPermissionsCondition(builder, activityVar, permissionField);
        } else {
            builder.beginControlFlow("if ($T.hasSelfPermissions($N, $N))", PERMISSION_UTILS, activityVar, permissionField);
        }
        builder.addCode(CodeBlock.builder()
                .add("$N.$N(", targetParam, Extensions.ElementSimpleString(needsMethod))
                .add(varargsParametersCodeBlock(needsMethod))
                .addStatement(")")
                .build()
        );
        builder.nextControlFlow("else");

        // Add the conditional for "OnShowRationale", if present
        ExecutableElement onRationale = rpe.findOnRationaleForNeeds(needsMethod);
        boolean hasParameters = !needsMethod.getParameters().isEmpty();
        if (hasParameters) {
            // If the method has parameters, precede the potential OnRationale call with
            // an instantiation of the temporary Request object
            CodeBlock.Builder varargsCall = CodeBlock.builder()
                    .add("$N = new $N($N, ",
                            pendingRequestFieldName(needsMethod),
                            permissionRequestTypeName(needsMethod),
                            targetParam
                    )
                    .add(varargsParametersCodeBlock(needsMethod))
                    .addStatement(")");
            builder.addCode(varargsCall.build());
        }
        if (onRationale != null) {
            addShouldShowRequestPermissionRationaleCondition(builder, targetParam, permissionField, true);
            if (hasParameters) {
                // For methods with parameters, use the PermissionRequest instantiated above
                builder.addStatement("$N.$N($N)", targetParam, Extensions.ElementSimpleString(onRationale), pendingRequestFieldName(needsMethod));
            } else {
                // Otherwise, create a new PermissionRequest on-the-fly
                builder.addStatement("$N.$N(new $N($N))", targetParam, Extensions.ElementSimpleString(onRationale), permissionRequestTypeName(needsMethod), targetParam);
            }
            builder.nextControlFlow("else");
        }

        // Add the branch for "request permission"
        SensitivePermissionInterface anInterface1 = ADD_WITH_CHECK_BODY_MAP.get(needsPermissionParameter);
        if (anInterface1 != null)
            anInterface1.addRequestPermissionsStatement(builder, activityVar, requestCodeField);
        else
            addRequestPermissionsStatement(builder, targetParam, permissionField, requestCodeField);
        if (onRationale != null) {
            builder.endControlFlow();
        }
        builder.endControlFlow();
    }

    private List<MethodSpec> createPermissionHandlingMethods(RuntimePermissionsElement rpe) {
        ArrayList<MethodSpec> methods = new ArrayList<MethodSpec>();

        if (hasNormalPermission(rpe)) {
            methods.add(createPermissionResultMethod(rpe));
        }

        if (hasSystemAlertWindowPermission(rpe) || hasWriteSettingPermission(rpe)) {
            methods.add(createOnActivityResultMethod(rpe));
        }

        return methods;
    }

    private MethodSpec createOnActivityResultMethod(RuntimePermissionsElement rpe) {
        String targetParam = "target";
        String requestCodeParam = "requestCode";
        String grantResultsParam = "grantResults";
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onActivityResult")
                .addTypeVariables(rpe.typeVariables)
                .addModifiers(Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(rpe.typeName, targetParam)
                .addParameter(TypeName.INT, requestCodeParam);

        builder.beginControlFlow("switch ($N)", requestCodeParam);
        for (ExecutableElement needsMethod : rpe.needsElements) {
            String needsPermissionParameter = needsMethod.getAnnotation(NeedsPermission.class).
                    value()[0];
            if (!ADD_WITH_CHECK_BODY_MAP.containsKey(needsPermissionParameter)) {
                continue;
            }

            builder.addCode("case $N:\n", requestCodeFieldName(needsMethod));

            addResultCaseBody(builder, needsMethod, rpe, targetParam, grantResultsParam);
        }

        builder.addCode("default:\n").addStatement("break").endControlFlow();

        return builder.build();
    }

    private MethodSpec createPermissionResultMethod(RuntimePermissionsElement rpe) {
        String targetParam = "target";
        String requestCodeParam = "requestCode";
        String grantResultsParam = "grantResults";
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onRequestPermissionsResult")
                .addTypeVariables(rpe.typeVariables)
                .addModifiers(Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(rpe.typeName, targetParam)
                .addParameter(TypeName.INT, requestCodeParam)
                .addParameter(ArrayTypeName.of(TypeName.INT), grantResultsParam);

        // For each @NeedsPermission method, add a switch case
        builder.beginControlFlow("switch ($N)", requestCodeParam);
        for (ExecutableElement needsMethod : rpe.needsElements) {
            String needsPermissionParameter = needsMethod.getAnnotation(NeedsPermission.class).value()[0];
            if (ADD_WITH_CHECK_BODY_MAP.containsKey(needsPermissionParameter)) {
                continue;
            }

            builder.addCode("case $N:\n", requestCodeFieldName(needsMethod));

            // Delegate switch-case generation to implementing classes
            addResultCaseBody(builder, needsMethod, rpe, targetParam, grantResultsParam);
        }

        // Add the default case
        builder.addCode("default:\n")
                .addStatement("break")
                .endControlFlow();

        return builder.build();
    }

    private void addResultCaseBody(MethodSpec.Builder builder, ExecutableElement needsMethod, RuntimePermissionsElement rpe, String targetParam, String grantResultsParam) {

        ExecutableElement onDenied = rpe.findOnDeniedForNeeds(needsMethod);
        boolean hasDenied = onDenied != null;
        String needsPermissionParameter = needsMethod.getAnnotation(NeedsPermission.class).value()[0];
        String permissionField = permissionFieldName(needsMethod);
        if (!ADD_WITH_CHECK_BODY_MAP.containsKey(needsPermissionParameter)) {
            builder.beginControlFlow("if ($T.getTargetSdkVersion($N) < 23 && !$T.hasSelfPermissions($N, $N))",
                    PERMISSION_UTILS, getActivityName(targetParam), PERMISSION_UTILS, getActivityName(targetParam), permissionField);
            if (hasDenied) {
//                 builder.addStatement("$N.$N()", targetParam, onDenied !!.simpleString());
                builder.addStatement("$N.$N()", targetParam, Extensions.ElementSimpleString(onDenied));
            }
            builder.addStatement("return");
            builder.endControlFlow();
        }

        // Add the conditional for "permission verified"
        SensitivePermissionInterface anInterface = ADD_WITH_CHECK_BODY_MAP.get(needsPermissionParameter);
        if (anInterface != null) {
            anInterface.addHasSelfPermissionsCondition(builder, getActivityName(targetParam), permissionField);
        } else {
            builder.beginControlFlow("if ($T.verifyPermissions($N))", PERMISSION_UTILS, grantResultsParam);
        }
        // Based on whether or not the method has parameters, delegate to the "pending request" object or invoke the method directly
        boolean hasParameters = !needsMethod.getParameters().isEmpty();
        if (hasParameters) {
            String pendingField = pendingRequestFieldName(needsMethod);
            builder.beginControlFlow("if ($N != null)", pendingField);
            builder.addStatement("$N.grant()", pendingField);
            builder.endControlFlow();
        } else {
            builder.addStatement("target.$N()", Extensions.ElementSimpleString(needsMethod));
        }

        // Add the conditional for "permission denied" and/or "never ask again", if present
        ExecutableElement onNeverAsk = rpe.findOnNeverAskForNeeds(needsMethod);
        boolean hasNeverAsk = onNeverAsk != null;

        if (hasDenied || hasNeverAsk) {
            builder.nextControlFlow("else");
        }
        if (hasNeverAsk) {
            // Split up the "else" case with another if condition checking for "never ask again" first
            addShouldShowRequestPermissionRationaleCondition(builder, targetParam, permissionFieldName(needsMethod), false);
            builder.addStatement("target.$N()", Extensions.ElementSimpleString(onNeverAsk));

            // If a "permission denied" is present as well, go into an else case, otherwise close this temporary branch
            if (hasDenied) {
                builder.nextControlFlow("else");
            } else {
                builder.endControlFlow();
            }
        }
        if (hasDenied) {
            // Add the "permissionDenied" statement
            builder.addStatement("$N.$N()", targetParam, Extensions.ElementSimpleString(onDenied));

            // Close the additional control flow potentially opened by a "never ask again" method
            if (hasNeverAsk) {
                builder.endControlFlow();
            }
        }
        // Close the "switch" control flow
        builder.endControlFlow();

        // Remove the temporary pending request field, in case it was used for a method with parameters
        if (hasParameters) {
            builder.addStatement("$N = null", pendingRequestFieldName(needsMethod));
        }
        builder.addStatement("break");
    }

    private boolean hasNormalPermission(RuntimePermissionsElement rpe) {
        for (ExecutableElement it : rpe.needsElements) {
            List<String> permissionValue = Extensions.AnnotationPermissionValue(it.getAnnotation(NeedsPermission.class));
            if (!permissionValue.contains(MANIFEST_SYSTEM_ALERT_WINDOW) && !permissionValue.contains(MANIFEST_WRITE_SETTING)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSystemAlertWindowPermission(RuntimePermissionsElement rpe) {
        return isDefinePermission(rpe, MANIFEST_SYSTEM_ALERT_WINDOW);
    }

    private boolean hasWriteSettingPermission(RuntimePermissionsElement rpe) {
        return isDefinePermission(rpe, MANIFEST_WRITE_SETTING);
    }

    private boolean isDefinePermission(RuntimePermissionsElement rpe, String permissionName) {
        for (ExecutableElement it : rpe.needsElements) {
            List<String> permissionValue = Extensions.AnnotationPermissionValue(it.getAnnotation(NeedsPermission.class));
            if (permissionValue.contains(permissionName)) {
                return true;
            }
        }
        return false;
    }

    private List<TypeSpec> createPermissionRequestClasses(RuntimePermissionsElement rpe) {
        ArrayList<TypeSpec> classes = new ArrayList<TypeSpec>();
        for (ExecutableElement it : rpe.needsElements) {
            ExecutableElement onRationale = rpe.findOnRationaleForNeeds(it);
            if (onRationale != null || !it.getParameters().isEmpty()) {
                classes.add(createPermissionRequestClass(rpe, it));
            }
        }

        return classes;
    }

    private TypeSpec createPermissionRequestClass(RuntimePermissionsElement rpe, ExecutableElement needsMethod) {
        // Select the superinterface of the generated class
        // based on whether or not the annotated method has parameters
        boolean hasParameters = !needsMethod.getParameters().isEmpty();
        String superInterfaceName = hasParameters ? "GrantableRequest" : "PermissionRequest";

        TypeName targetType = rpe.typeName;
        TypeSpec.Builder builder = TypeSpec.classBuilder(permissionRequestTypeName(needsMethod))
                .addTypeVariables(rpe.typeVariables)
                .addSuperinterface(ClassName.get("permissions.dispatcher", superInterfaceName))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

        // Add required fields to the target
        String weakFieldName = "weakTarget";
        ParameterizedTypeName weakFieldType = ParameterizedTypeName.get(ClassName.get("java.lang.ref", "WeakReference"), targetType);
        builder.addField(weakFieldType, weakFieldName, Modifier.PRIVATE, Modifier.FINAL);
        for (VariableElement it : needsMethod.getParameters()) {
            builder.addField(typeNameOf(it), Extensions.ElementSimpleString(it), Modifier.PRIVATE, Modifier.FINAL);
        }
        // Add constructor
        String targetParam = "target";
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(targetType, targetParam)
                .addStatement("this.$L = new WeakReference<>($N)", weakFieldName, targetParam);
        for (VariableElement it : needsMethod.getParameters()) {
            String fieldName = Extensions.ElementSimpleString(it);
            constructorBuilder
                    .addParameter(typeNameOf(it), fieldName)
                    .addStatement("this.$L = $N", fieldName, fieldName);
        }

        builder.addMethod(constructorBuilder.build());

        // Add proceed() override
        MethodSpec.Builder proceedMethod = MethodSpec.methodBuilder("proceed")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addStatement("$T target = $N.get()", targetType, weakFieldName)
                .addStatement("if (target == null) return");
        String requestCodeField = requestCodeFieldName(needsMethod);
        SensitivePermissionInterface anInterface = ADD_WITH_CHECK_BODY_MAP.get(needsMethod.getAnnotation(NeedsPermission.class).value()[0]);
        if (anInterface != null) {
            anInterface.addRequestPermissionsStatement(proceedMethod, getActivityName(targetParam), requestCodeField);
        } else {
            addRequestPermissionsStatement(proceedMethod, targetParam, permissionFieldName(needsMethod), requestCodeField);
        }
        builder.addMethod(proceedMethod.build());

        // Add cancel() override method
        MethodSpec.Builder cancelMethod = MethodSpec.methodBuilder("cancel")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);
        ExecutableElement onDenied = rpe.findOnDeniedForNeeds(needsMethod);
        if (onDenied != null) {
            cancelMethod
                    .addStatement("$T target = $N.get()", targetType, weakFieldName)
                    .addStatement("if (target == null) return")
                    .addStatement("target.$N()", Extensions.ElementSimpleString(onDenied));
        }
        builder.addMethod(cancelMethod.build());

        // For classes with additional parameters, add a "grant()" method
        if (hasParameters) {
            MethodSpec.Builder grantMethod = MethodSpec.methodBuilder("grant")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID);
            grantMethod
                    .addStatement("$T target = $N.get()", targetType, weakFieldName)
                    .addStatement("if (target == null) return");

            // Compose the call to the permission-protected method;
            // since the number of parameters is variable, utilize the low-level CodeBlock here
            // to compose the method call and its parameters
            grantMethod.addCode(
                    CodeBlock.builder()
                            .add("target.$N(", Extensions.ElementSimpleString(needsMethod))
                            .add(varargsParametersCodeBlock(needsMethod))
                            .addStatement(")")
                            .build()
            );
            builder.addMethod(grantMethod.build());
        }

        return builder.build();
    }

}
