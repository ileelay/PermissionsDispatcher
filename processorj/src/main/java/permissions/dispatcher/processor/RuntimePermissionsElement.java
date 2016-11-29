package permissions.dispatcher.processor;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.tools.Diagnostic;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.processor.util.Extensions;
import permissions.dispatcher.processor.util.Helpers;

import static permissions.dispatcher.processor.util.Constants.GEN_CLASS_SUFFIX;
import static permissions.dispatcher.processor.util.Validators.checkDuplicatedValue;
import static permissions.dispatcher.processor.util.Validators.checkMethodParameters;
import static permissions.dispatcher.processor.util.Validators.checkMethodSignature;
import static permissions.dispatcher.processor.util.Validators.checkMixPermissionType;
import static permissions.dispatcher.processor.util.Validators.checkNotEmpty;
import static permissions.dispatcher.processor.util.Validators.checkPrivateMethods;

/**
 * Created by Lilei on 2016.
 */

public class RuntimePermissionsElement {
    public String inputClassName;
    public TypeName typeName;
    public List<TypeVariableName> typeVariables;
    public String packageName;
    public String generatedClassName;
    public List<ExecutableElement> needsElements;
    public List<ExecutableElement> onRationaleElements;
    public List<ExecutableElement> onDeniedElements;
    public List<ExecutableElement> onNeverAskElements;

    public RuntimePermissionsElement(TypeElement e) {
        typeName = TypeName.get(e.asType());
        typeVariables = mapParameters(e);
        packageName = Extensions.TypeElementPackageName(e);
        inputClassName = Extensions.ElementSimpleString(e);
        generatedClassName = inputClassName + GEN_CLASS_SUFFIX;
        needsElements = Extensions.ElementChildElementsAnnotatedWith(e, NeedsPermission.class);
        onRationaleElements = Extensions.ElementChildElementsAnnotatedWith(e, OnShowRationale.class);
        onDeniedElements = Extensions.ElementChildElementsAnnotatedWith(e, OnPermissionDenied.class);
        onNeverAskElements = Extensions.ElementChildElementsAnnotatedWith(e, OnNeverAskAgain.class);
        PermissionsProcessor.messager.printMessage(Diagnostic.Kind.NOTE, "per validateNeedsMethods");
        validateNeedsMethods();
        PermissionsProcessor.messager.printMessage(Diagnostic.Kind.NOTE, "per validateRationaleMethods");
        validateRationaleMethods();
        PermissionsProcessor.messager.printMessage(Diagnostic.Kind.NOTE, "per validateDeniedMethods");
        validateDeniedMethods();
        PermissionsProcessor.messager.printMessage(Diagnostic.Kind.NOTE, "per validateNeverAskMethods");
        validateNeverAskMethods();
    }


    public List<TypeVariableName> mapParameters(TypeElement e) {
        List<? extends TypeParameterElement> parameters = e.getTypeParameters();
        List<TypeVariableName> list = new ArrayList<TypeVariableName>();
        for (TypeParameterElement it : parameters) {
            list.add(TypeVariableName.get(it));
        }
        return list;
    }

    private void validateNeedsMethods() {
        checkNotEmpty(needsElements, this, NeedsPermission.class);
        checkPrivateMethods(needsElements, NeedsPermission.class);
        checkMethodSignature(needsElements);
        checkMixPermissionType(needsElements, NeedsPermission.class);
    }

    private void validateRationaleMethods() {
        PermissionsProcessor.messager.printMessage(Diagnostic.Kind.NOTE, "per checkDuplicatedValue");
        checkDuplicatedValue(onRationaleElements, OnShowRationale.class);
        PermissionsProcessor.messager.printMessage(Diagnostic.Kind.NOTE, "per checkPrivateMethods");
        checkPrivateMethods(onRationaleElements, OnShowRationale.class);
        PermissionsProcessor.messager.printMessage(Diagnostic.Kind.NOTE, "per checkMethodSignature");
        checkMethodSignature(onRationaleElements);
        PermissionsProcessor.messager.printMessage(Diagnostic.Kind.NOTE, "per checkMethodParameters");
        checkMethodParameters(onRationaleElements, 1, Helpers.typeMirrorOf("permissions.dispatcher.PermissionRequest"));
    }

    private void validateDeniedMethods() {
        checkDuplicatedValue(onDeniedElements, OnPermissionDenied.class);
        checkPrivateMethods(onDeniedElements, OnPermissionDenied.class);
        checkMethodSignature(onDeniedElements);
        checkMethodParameters(onDeniedElements, 0);
    }

    private void validateNeverAskMethods() {
        checkDuplicatedValue(onNeverAskElements, OnNeverAskAgain.class);
        checkPrivateMethods(onNeverAskElements, OnNeverAskAgain.class);
        checkMethodSignature(onNeverAskElements);
        checkMethodParameters(onNeverAskElements, 0);
    }

    public ExecutableElement findOnRationaleForNeeds(ExecutableElement needsElement) {
        return Helpers.findMatchingMethodForNeeds(needsElement, onRationaleElements, OnShowRationale.class);
    }

    public ExecutableElement findOnDeniedForNeeds(ExecutableElement needsElement) {
        return Helpers.findMatchingMethodForNeeds(needsElement, onDeniedElements, OnPermissionDenied.class);
    }

    public ExecutableElement findOnNeverAskForNeeds(ExecutableElement needsElement) {
        return Helpers.findMatchingMethodForNeeds(needsElement, onNeverAskElements, OnNeverAskAgain.class);
    }

}
