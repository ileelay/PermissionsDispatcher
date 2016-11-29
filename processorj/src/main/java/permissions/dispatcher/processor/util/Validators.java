package permissions.dispatcher.processor.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import permissions.dispatcher.processor.PermissionsProcessor;
import permissions.dispatcher.processor.ProcessorUnit;
import permissions.dispatcher.processor.RuntimePermissionsElement;
import permissions.dispatcher.processor.exeception.DuplicatedValueException;
import permissions.dispatcher.processor.exeception.MixPermissionTypeException;
import permissions.dispatcher.processor.exeception.NoAnnotatedMethodsException;
import permissions.dispatcher.processor.exeception.NoParametersAllowedException;
import permissions.dispatcher.processor.exeception.NoThrowsAllowedException;
import permissions.dispatcher.processor.exeception.PrivateMethodException;
import permissions.dispatcher.processor.exeception.WrongClassException;
import permissions.dispatcher.processor.exeception.WrongParametersException;
import permissions.dispatcher.processor.exeception.WrongReturnTypeException;

import static permissions.dispatcher.processor.PermissionsProcessor.TYPE_UTILS;

/**
 * Created by Lilei on 2016.
 */

public class Validators {

    private static String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    private static String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";


//    fun findAndValidateProcessorUnit(units: List<ProcessorUnit>, e: Element): ProcessorUnit {
//        val type = e.asType()
//        try {
//            return units.first { type.isSubtypeOf(it.getTargetType()) }
//        } catch (ex: NoSuchElementException) {
//            throw WrongClassException(type)
//        }
//    }

    public static ProcessorUnit findAndValidateProcessorUnit(List<ProcessorUnit> units, Element e) {
        TypeMirror type = e.asType();
        ProcessorUnit unit = units.get(0);
        if (Extensions.TypeMirrorIsSubtypeOf(type, unit.getTargetType())) {
            return unit;
        } else {
            throw WrongClassException.getInstance(type);
        }
    }

//    fun <A : Annotation> checkDuplicatedValue(items: List<ExecutableElement>, annotationClass: Class<A>) {
//        val allItems: ArrayList<List<String>> = arrayListOf()
//        items.forEach {
//            val permissionValue = it.getAnnotation(annotationClass).permissionValue()
//            Collections.sort(permissionValue)
//            allItems.forEach { oldItem ->
//                if (oldItem.equals(permissionValue)) {
//                    throw DuplicatedValueException(permissionValue, it, annotationClass)
//                }
//            }
//            allItems.add(permissionValue)
//        }
//    }

    public static <A extends Annotation> void checkDuplicatedValue(List<ExecutableElement> items, Class<A> annotationClass) {
        ArrayList<List<String>> allItems = new ArrayList<List<String>>();
        for (int i = 0; i < items.size(); i++) {
            ExecutableElement it = items.get(i);
            List<String> permissionValue = Extensions.AnnotationPermissionValue(it.getAnnotation(annotationClass));
            Collections.sort(permissionValue);
            for (List<String> oldItem : allItems) {
                if (oldItem.equals(permissionValue)) {
                    throw DuplicatedValueException.getInstance(permissionValue, it, annotationClass);
                }
            }
            allItems.add(permissionValue);
        }
    }

//    fun <A : Annotation> checkNotEmpty(items: List<ExecutableElement>, rpe: RuntimePermissionsElement, annotationClass: Class<A>) {
//        if (items.isEmpty()) {
//            throw NoAnnotatedMethodsException(rpe, annotationClass)
//        }
//    }

    public static <A extends Annotation> void checkNotEmpty(List<ExecutableElement> items, RuntimePermissionsElement rpe, Class<A> annotationClass) {
        if (items.isEmpty()) {
            throw NoAnnotatedMethodsException.getInstance(rpe, annotationClass);
        }
    }

//    fun <A : Annotation> checkPrivateMethods(items: List<ExecutableElement>, annotationClass: Class<A>) {
//        items.forEach {
//            if (it.modifiers.contains(Modifier.PRIVATE)) {
//                throw PrivateMethodException(it, annotationClass)
//            }
//        }
//    }

    public static <A extends Annotation> void checkPrivateMethods(List<ExecutableElement> items, Class<A> annotationClass) {
        for (ExecutableElement it : items) {
            if (it.getModifiers().contains(Modifier.PRIVATE))
                throw PrivateMethodException.getInstance(it, annotationClass);
        }
    }

//    fun checkMethodSignature(items: List<ExecutableElement>) {
//        items.forEach {
//            // Allow 'void' return type only
//            if (it.returnType.kind != TypeKind.VOID) {
//                throw WrongReturnTypeException(it)
//            }
//            // Allow methods without 'throws' declaration only
//            if (it.thrownTypes.isNotEmpty()) {
//                throw NoThrowsAllowedException(it)
//            }
//        }
//    }

    public static void checkMethodSignature(List<ExecutableElement> items) {
        for (ExecutableElement it : items) {
//             Allow 'void' return type only
            if (it.getReturnType().getKind() != TypeKind.VOID) {
                throw WrongReturnTypeException.getInstance(it);
            }
//             Allow methods without 'throws' declaration only
            if (!it.getThrownTypes().isEmpty()) {
                throw NoThrowsAllowedException.getInstance(it);
            }
        }
    }

//    fun checkMethodParameters(items: List<ExecutableElement>, numParams: Int, vararg requiredTypes: TypeMirror) {
//        items.forEach {
//            // Check each element's parameters against the requirements
//            val params = it.parameters
//            if (numParams == 0 && params.isNotEmpty()) {
//                throw NoParametersAllowedException(it)
//            }
//
//            if (numParams != params.size) {
//                throw WrongParametersException(it, requiredTypes)
//            }
//
//            params.forEachIndexed { i, param ->
//                    val requiredType = requiredTypes[i]
//                if (!TYPE_UTILS.isSameType(param.asType(), requiredType)) {
//                    throw WrongParametersException(it, requiredTypes)
//                }
//            }
//        }
//    }

    public static void checkMethodParameters(List<ExecutableElement> items, int numParams, TypeMirror... requiredTypes) {
        for (int i = 0; i < items.size(); i++) {
            ExecutableElement it = items.get(i);
            List<? extends VariableElement> params = it.getParameters();
            if (numParams == 0 && !params.isEmpty()) {
                throw NoParametersAllowedException.getInstance(it);
            }
            if (numParams != params.size()) {
                throw WrongParametersException.getInstance(it, requiredTypes);
            }
            for (int j = 0; j < params.size(); j++) {
                TypeMirror requiredType = requiredTypes[j];
                VariableElement param = params.get(j);
                if (!TYPE_UTILS.isSameType(param.asType(), requiredType)) {
                    throw WrongParametersException.getInstance(it, requiredTypes);
                }
            }
        }
    }

//    fun <A : Annotation> checkMixPermissionType(items: List<ExecutableElement>, annotationClass: Class<A>) {
//        items.forEach {
//            val permissionValue = it.getAnnotation(annotationClass).permissionValue()
//            if (permissionValue.size > 1) {
//                if (permissionValue.contains(WRITE_SETTINGS)) {
//                    throw MixPermissionTypeException(it, WRITE_SETTINGS)
//                } else if (permissionValue.contains(SYSTEM_ALERT_WINDOW)) {
//                    throw MixPermissionTypeException(it, SYSTEM_ALERT_WINDOW)
//                }
//            }
//        }
//    }

    public static <A extends Annotation> void checkMixPermissionType(List<ExecutableElement> items, Class<A> annotationClass) {
        for (ExecutableElement it : items) {
            List<String> permissionValue = Extensions.AnnotationPermissionValue(it.getAnnotation(annotationClass));
            if (permissionValue.size() > 1) {
                if (permissionValue.contains(WRITE_SETTINGS)) {
                    throw MixPermissionTypeException.getInstance(it, WRITE_SETTINGS);
                } else if (permissionValue.contains(SYSTEM_ALERT_WINDOW)) {
                    throw MixPermissionTypeException.getInstance(it, SYSTEM_ALERT_WINDOW);
                }
            }
        }

    }

}
