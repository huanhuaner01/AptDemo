package com.example;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * TODO What does this class to do?
 *
 * @author Muyangmin
 * @since 2.0.0
 */
@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(BindView.class.getCanonicalName());
    }
    private Elements elementUtil;
    private Types typesUtil;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtil = processingEnvironment.getElementUtils();
        typesUtil = processingEnvironment.getTypeUtils();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
     /*   Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            BindView view = element.getAnnotation(BindView.class);
            int id = view.value();

            MethodSpec main = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String[].class, "args")
                    .addStatement("$T.out.println($S)", System.class, "BindView, " + var)
                    .build();
            TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(main)
                    .build();
            JavaFile javaFile = JavaFile.builder("com.soubu.helloworld", helloWorld)
                    .build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;*/
        Map<TypeElement, List<FieldViewBinding>> targetMap = new HashMap<>();
        setElemtBindView(roundEnvironment, targetMap, BindView.class);
        addClassAndMethod(targetMap);
        return false;
    }


    private void setElemtBindView(RoundEnvironment roundEnvironment, Map<TypeElement, List<FieldViewBinding>> targetMap, Class<BindView> annotation) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(annotation)) {
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            List<FieldViewBinding> list = targetMap.get(enclosingElement);
            if (null == list) {
                list = new ArrayList<>();
                targetMap.put(enclosingElement, list);
            }
            int id = element.getAnnotation(annotation).value();
            String fieldName = element.getSimpleName().toString();
            TypeMirror typeMirror = element.asType();
            FieldViewBinding fieldViewBinding = new FieldViewBinding(fieldName, typeMirror, id);
            list.add(fieldViewBinding);
        }
    }

    private void addClassAndMethod(Map<TypeElement, List<FieldViewBinding>> targetMap) {
        //遍历Map
        for (Map.Entry<TypeElement, List<FieldViewBinding>> item : targetMap.entrySet()) {
            List<FieldViewBinding> list = item.getValue();
            if (null == list || list.size() == 0) {
                continue;
            }
            TypeElement typeElement = item.getKey();
            String packageName = getPackageName(typeElement);
            String className = getClassName(typeElement, packageName);
            ClassName name = ClassName.bestGuess(className);
            ClassName viewBinder = ClassName.get("com.example.injectlibrary", "ViewBinder");
            TypeSpec.Builder result = TypeSpec.classBuilder(className + "$$ViewBinder")
                    .addModifiers(Modifier.PUBLIC)
                    .addTypeVariable(TypeVariableName.get("T", name))
                    .addSuperinterface(ParameterizedTypeName.get(viewBinder, name));
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC, Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    .addAnnotation(Override.class)
                    .addParameter(name, "target", Modifier.FINAL);
            for (int i = 0; i < list.size(); i++) {
                FieldViewBinding fieldViewBinding = list.get(i);
                String packageNameString = fieldViewBinding.getTypeMirror().toString();
                ClassName viewclassName = ClassName.bestGuess(packageNameString);
                methodBuilder.addStatement("target.$L=($T)target.findViewById($L)", fieldViewBinding.getName(), viewclassName, fieldViewBinding.getResId());
            }
            result.addMethod(methodBuilder.build());
            try {
                JavaFile.builder(packageName, result.build())
                        .addFileComment("auto create make")
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getClassName(TypeElement typeElement, String packageName) {
        int packageNameLength = packageName.length() + 1;
        return typeElement.getQualifiedName().toString().substring(packageNameLength).replace(".", "$");
    }

    private String getPackageName(TypeElement enclosingElement) {
        return elementUtil.getPackageOf(enclosingElement).getQualifiedName().toString();
    }
}
