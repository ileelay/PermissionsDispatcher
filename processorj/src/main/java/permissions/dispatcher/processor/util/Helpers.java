package permissions.dispatcher.processor.util;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import permissions.dispatcher.NeedsPermission;

import static permissions.dispatcher.processor.PermissionsProcessor.ELEMENT_UTILS;

/**
 * Created by Lilei on 2016.
 */


public class Helpers {
//    fun typeMirrorOf(className: String): TypeMirror = ELEMENT_UTILS.getTypeElement(className).asType()

    public static TypeMirror typeMirrorOf( String className) {
        return ELEMENT_UTILS.getTypeElement(className).asType();
    }

//    fun typeNameOf(it: Element): TypeName = TypeName.get(it.asType())

    public static TypeName typeNameOf(Element it) {
        return TypeName.get(it.asType());
    }

//    fun requestCodeFieldName(e:ExecutableElement):String="$GEN_REQUESTCODE_PREFIX${e.simpleString().toUpperCase()}"

    public static String requestCodeFieldName(ExecutableElement e) {
        return Constants.GEN_REQUESTCODE_PREFIX + Extensions.ElementSimpleString(e).toUpperCase();
    }

//    fun permissionFieldName(e:ExecutableElement):String="$GEN_PERMISSION_PREFIX${e.simpleString().toUpperCase()}"

    public static String permissionFieldName(ExecutableElement e) {
        return Constants.GEN_PERMISSION_PREFIX + Extensions.ElementSimpleString(e).toUpperCase();
    }

//    fun pendingRequestFieldName(e:ExecutableElement):String="$GEN_PENDING_PREFIX${e.simpleString().toUpperCase()}"


    public static String pendingRequestFieldName(ExecutableElement e) {
        return Constants.GEN_PENDING_PREFIX + Extensions.ElementSimpleString(e).toUpperCase();
    }

//    fun withCheckMethodName(e:ExecutableElement):String="${e.simpleString()}$GEN_WITHCHECK_SUFFIX"


    public static String withCheckMethodName(ExecutableElement e) {
        return Extensions.ElementSimpleString(e) + Constants.GEN_WITHCHECK_SUFFIX;
    }

//    fun permissionRequestTypeName(e:ExecutableElement) :String="${e.simpleString().capitalize()}$GEN_PERMISSIONREQUEST_SUFFIX"

    public static String permissionRequestTypeName(ExecutableElement e) {
        return capitalize(Extensions.ElementSimpleString(e)) + Constants.GEN_PERMISSIONREQUEST_SUFFIX;
    }
//    fun <A : Annotation> findMatchingMethodForNeeds(needsElement: ExecutableElement, otherElements: List<ExecutableElement>, annotationType: Class<A>): ExecutableElement? {
//        val value: List<String> = needsElement.getAnnotation(NeedsPermission::class.java).permissionValue()
//        return otherElements.firstOrNull {
//            it.getAnnotation(annotationType).permissionValue().equals(value)
//        }
//    }

    public static <A extends Annotation> ExecutableElement findMatchingMethodForNeeds(ExecutableElement needsElement, List<ExecutableElement> otherElements, Class<A> annotationType) {
        List<String> value = Extensions.AnnotationPermissionValue(needsElement.getAnnotation(NeedsPermission.class));
        for (ExecutableElement element : otherElements) {
            if (Extensions.AnnotationPermissionValue(element.getAnnotation(annotationType)).equals(value)) {
                return element;
            }
        }
        return null;
    }


//    fun varargsParametersCodeBlock(needsElement: ExecutableElement): CodeBlock {
//        val varargsCall = CodeBlock.builder()
//        needsElement.parameters.forEachIndexed { i, it ->
//                varargsCall.add("\$L", it.simpleString())
//            if (i < needsElement.parameters.size - 1) {
//                varargsCall.add(", ")
//            }
//        }
//        return varargsCall.build()
//    }


    public static CodeBlock varargsParametersCodeBlock(ExecutableElement needsElement) {
        CodeBlock.Builder varargsCall = CodeBlock.builder();
        List<? extends VariableElement> parameters = needsElement.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            VariableElement it = parameters.get(i);
            varargsCall.add("$L", Extensions.ElementSimpleString(it));
            if (i < parameters.size() - 1) {
                varargsCall.add(", ");
            }
        }
        return varargsCall.build();
    }

    private static String capitalize(String s) {
        if (s != null && !"".equals(s) && Character.isLowerCase(s.charAt(0)))
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        else
            return s;
    }
}
