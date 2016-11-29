package permissions.dispatcher.processor.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;

import static permissions.dispatcher.processor.PermissionsProcessor.TYPE_UTILS;

/**
 * Created by Lilei on 2016.
 */

public class Extensions {

//    fun TypeElement.packageName(): String {
//        val qn = this.qualifiedName.toString()
//        return qn.substring(0, qn.lastIndexOf('.'))
//    }

    public static String TypeElementPackageName(TypeElement element) {
        String qn = element.getQualifiedName().toString();
        return qn.substring(0, qn.lastIndexOf('.'));
    }

//    fun Element.simpleString(): String = this.simpleName.toString()

    public static String ElementSimpleString(Element element) {
        return element.getSimpleName().toString();
    }

    //    fun TypeMirror.simpleString(): String {
//        val toString: String = this.toString()
//        val indexOfDot: Int = toString.lastIndexOf('.')
//        return if (indexOfDot == -1) toString else toString.substring(indexOfDot + 1)
//    }
    public static String TypeMirrorSimpleString(TypeMirror typeMirror) {
        String toString = typeMirror.toString();
        int indexOfDot = toString.lastIndexOf('.');
        if (indexOfDot == -1)
            return toString;
        else
            return toString.substring(indexOfDot + 1);
    }

//    fun <A : Annotation> Element.hasAnnotation(annotationType: Class<A>): Boolean =
//            this.getAnnotation(annotationType) != null

    public static <A extends Annotation> boolean ElementHasAnnotation(Element element, Class<A> annotationType) {
        return element.getAnnotation(annotationType) != null;
    }

//    fun Annotation.permissionValue(): List<String> {
//        when (this) {
//            is NeedsPermission -> return this.value.asList()
//            is OnShowRationale -> return this.value.asList()
//            is OnPermissionDenied -> return this.value.asList()
//            is OnNeverAskAgain -> return this.value.asList()
//        }
//        return emptyList()
//    }

    public static List<String> AnnotationPermissionValue(Annotation annotation) {
        if (annotation instanceof NeedsPermission) {
            return Arrays.asList(((NeedsPermission) annotation).value());
        }
        if (annotation instanceof OnShowRationale) {
            return Arrays.asList(((OnShowRationale) annotation).value());
        }
        if (annotation instanceof OnPermissionDenied) {
            return Arrays.asList(((OnPermissionDenied) annotation).value());
        }
        if (annotation instanceof OnNeverAskAgain) {
            return Arrays.asList(((OnNeverAskAgain) annotation).value());
        }
        return new ArrayList<String>();
    }

//    fun <A : Annotation> Element.childElementsAnnotatedWith(annotationClass: Class<A>): List<ExecutableElement> =
//            this.enclosedElements
//            .filter { it.hasAnnotation(annotationClass) }
//    .map { it as ExecutableElement }

    public static <A extends Annotation> List<ExecutableElement> ElementChildElementsAnnotatedWith(Element e, Class<A> annotationClass) {
        List<? extends Element> elements = e.getEnclosedElements();
        List<ExecutableElement> filter = new ArrayList<ExecutableElement>();
        for (Element el : elements) {
            if (ElementHasAnnotation(el, annotationClass) && el instanceof ExecutableElement) {
                filter.add((ExecutableElement) el);
            }
        }
        return filter;
    }


//    fun TypeMirror.isSubtypeOf(ofType: TypeMirror): Boolean = TYPE_UTILS.isSubtype(this, ofType)


    public static boolean TypeMirrorIsSubtypeOf(TypeMirror typeMirror, TypeMirror ofType) {
        return TYPE_UTILS.isSubtype(typeMirror, ofType);
    }

}
