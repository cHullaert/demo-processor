package com.darwinit.annotation.builder;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes(
        "com.darwinit.annotation.builder.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations,
                         RoundEnvironment roundEnv) {
    for (TypeElement annotation : annotations) {
      Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

      annotatedElements.forEach(element -> {

        if(element.getKind() == ElementKind.CLASS) {
          TypeElement clazz=((TypeElement) element);
          String classname=clazz.getQualifiedName().toString();
          processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "builder processor -> class: "+classname);
          List<VariableElement> fields=ElementFilter.fieldsIn(clazz.getEnclosedElements());

          fields.forEach(field -> {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "builder processor -> field: "+field.getSimpleName());
            String qualifiedType=field.asType().toString();

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "builder processor -> type: "+qualifiedType);
          });
          
          JavaFile javaFile=createBuilderClass(clazz, fields);
          try {
            javaFile.writeTo(processingEnv.getFiler());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
    }

    return true;
  }

  private JavaFile createBuilderClass(TypeElement clazz, List<VariableElement> fields) {
    List<FieldSpec> fieldSpecs=createFields(fields);
    List<MethodSpec> methodSpecs=createSetters(fields);
    MethodSpec buildMethod=createBuildMethod(clazz, fields);
    
    TypeSpec builder = TypeSpec
            .classBuilder(clazz.getSimpleName().toString()+"Builder")
            .addModifiers(Modifier.PUBLIC)
            .addFields(fieldSpecs)
            .addMethods(methodSpecs)
            .addMethod(buildMethod)
            .build();
    return JavaFile
            .builder(getPackageName(clazz), builder)
            .indent("    ")
            .build();

  }

  private MethodSpec createBuildMethod(TypeElement clazz, List<VariableElement> fields) {
    String params=fields.stream()
            .map(field -> "this."+field.getSimpleName().toString())
            .collect(Collectors.joining(","));

    CodeBlock codeBlock=CodeBlock
            .builder()
            .addStatement("return new "+clazz.getSimpleName().toString()+"("+params+")")
            .build();

    return MethodSpec
            .methodBuilder("build")
            .returns(TypeName.get(clazz.asType()))
            .addModifiers(Modifier.PUBLIC)
            .addCode(codeBlock)
            .build();
  }

  private String getPackageName(TypeElement clazz) {
    return clazz.getQualifiedName().toString().substring(0, clazz.getQualifiedName().toString().lastIndexOf("."));
  }

  private List<MethodSpec> createSetters(List<VariableElement> fields) {
    return fields.stream().map(
            field -> MethodSpec
                    .methodBuilder("set"+ StringUtils.capitalize(field.getSimpleName().toString()))
                    .addParameter(TypeName.get(field.asType()), field.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("this."+field.getSimpleName().toString()+"="+field.getSimpleName().toString())
                    .build()
    ).collect(Collectors.toList());
  }

  private List<FieldSpec> createFields(List<VariableElement> fields) {
    return fields.stream().map(
      field -> FieldSpec
                .builder(TypeName.get(field.asType()), field.getSimpleName().toString(), Modifier.PRIVATE)
                .build()
    ).collect(Collectors.toList());
  }
}
