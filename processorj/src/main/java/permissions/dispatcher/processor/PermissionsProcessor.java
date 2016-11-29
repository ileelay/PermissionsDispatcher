package permissions.dispatcher.processor;

import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import permissions.dispatcher.RuntimePermissions;
import permissions.dispatcher.processor.impl.ActivityProcessorUnit;
import permissions.dispatcher.processor.impl.NativeFragmentProcessorUnit;
import permissions.dispatcher.processor.impl.SupportFragmentProcessorUnit;

import static permissions.dispatcher.processor.util.Validators.findAndValidateProcessorUnit;

/**
 * Created by Lilei on 2016.
 */
public class PermissionsProcessor extends AbstractProcessor {
    public static Elements ELEMENT_UTILS;

    public static Types TYPE_UTILS;
    private Filer filer;
    public static Messager messager;

    private List<ProcessorUnit> processorUnits;

    @Override
    public synchronized void init(ProcessingEnvironment environment) {
        super.init(environment);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        ELEMENT_UTILS = environment.getElementUtils();
        TYPE_UTILS = environment.getTypeUtils();
        processorUnits = new ArrayList<ProcessorUnit>();
        processorUnits.add(new ActivityProcessorUnit());
        processorUnits.add(new SupportFragmentProcessorUnit());
        processorUnits.add(new NativeFragmentProcessorUnit());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment environment) {
        // Create a RequestCodeProvider which guarantees unique request codes for each permission request
        RequestCodeProvider requestCodeProvider = new RequestCodeProvider();
        Set<? extends Element> elementsAnnotatedWith = environment.getElementsAnnotatedWith(RuntimePermissions.class);
        for (Element it : elementsAnnotatedWith) {
            // Find a suitable ProcessorUnit for this element
            ProcessorUnit processorUnit = findAndValidateProcessorUnit(processorUnits, it);
            // Create a RuntimePermissionsElement for this value
            messager.printMessage(Diagnostic.Kind.NOTE, "per rpe");
            RuntimePermissionsElement rpe = new RuntimePermissionsElement((TypeElement) it);
            messager.printMessage(Diagnostic.Kind.NOTE, "aft rpe");
            // Create a JavaFile for this element and write it out
            JavaFile javaFile = processorUnit.createJavaFile(rpe, requestCodeProvider);
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> strings = new HashSet<String>();
        strings.add(RuntimePermissions.class.getCanonicalName());
        return strings;
    }
}
