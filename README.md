https://www.baeldung.com/java-annotation-processing-builder
https://www.baeldung.com/java-poet

```java
            if (field.asType().getKind().isPrimitive()) {
              qualifiedType=field.asType().toString();
            }
            else {
              switch ((field.asType().getKind())) {
                case ARRAY:
                  qualifiedType = field.asType().toString();
                  break;
                default:
                  qualifiedType = field.asType().toString();
                  break;
              }
            }
```