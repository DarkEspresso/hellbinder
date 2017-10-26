package tech.darkespresso.hellbinder.compiler;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/** Utilities for the unit tests. */
public final class TestUtils {
  public static <T, E extends Annotation> T fromSource(
      @Nonnull final String source,
      @Nonnull Class<E> annotationType,
      @Nonnull ProcessorCallback<T> callback) {
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final JavaFileObject fileObject =
        new SimpleJavaFileObject(URI.create("string:///foobar.java"), Kind.SOURCE) {
          @Override
          public CharSequence getCharContent(boolean b) throws IOException {
            return source;
          }
        };
    CompilationTask task =
        compiler.getTask(null, null, null, null, null, Collections.singletonList(fileObject));
    Processor<T, E> processor = new Processor<>(annotationType, callback);
    task.setProcessors(Collections.singletonList(processor));
    if (!task.call()) {
      throw new IllegalArgumentException();
    }
    return processor.value;
  }

  public interface ProcessorCallback<T> {
    T invoke(Element element, ProcessingEnvironment processingEnv) throws Exception;
  }

  private static class Processor<T, E extends Annotation> extends AbstractProcessor {
    private final Class<E> annotationType;
    private final ProcessorCallback<T> callback;

    private T value;

    private Processor(Class<E> annotationType, ProcessorCallback<T> callback) {
      this.annotationType = annotationType;
      this.callback = callback;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
      return Collections.singleton(annotationType.getName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
      Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotationType);
      if (elements.size() > 0) {
        if (value != null || elements.size() > 1) {
          throw new IllegalArgumentException();
        }
        try {
          value = callback.invoke(elements.iterator().next(), processingEnv);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      return true;
    }
  }
}
