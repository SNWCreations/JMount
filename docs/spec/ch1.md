# Chapter 1 - The API

## 1.1  Origin & Mount Point

For example:

```java
package unstable.vX_XX; // X_XX is the version, represents the unstable part of the package name
public class Something {
    public void ability() {
        // code goes here
    }
    
    private void privateAbility() {
        // code goes here
    }
}

// -- NEW FILE --

@MountPoint("unstable.v{VERSION}.Something")
public interface SomethingMP {
    void ability();
    
    void privateAbility();
}
```

The `Something` class is in the situation we've explained in [Chapter 0](ch0.md), and it is the "origin" class.

The "origin" means things under an unstable package.

The `SomethingMP` class (exactly it is interface) is just like a mirror of the things declared in the `Something` class,
 and it is annotated with `MountPoint` annotation, it is a "Mount Point" ("MP" is its short name).

## 1.2  Mount

A `Mount` object represents a configured, ready-to-use utility tool
 for creating instances of Mount Points and origin things.

It has some attributes like...
* Name transformer
* Pattern Replacer
* ClassLoader

### 1.2.1  Name transformer

The name transformer is a object which is used to convert name strings with pattern
 (e.g. class name in `@MountPoint` annotations) into the exact names.

### 1.2.2  Pattern Replacer

It just like a part of name transformer, but ONLY transform class names in the value of `@MountPoint` annotations.

### 1.2.3  ClassLoader

The class loader in Mount objects will be used to look up classes using the exact names from name transformers
 (or pattern replacer).

In some implementations it maybe used to be the parent of the MP implementation classes' class loader.

## 1.3  Mount Point

The Mount Point is an interface which is declared with `@MountPoint` annotation.

There is no restriction of Mount Points' name. But we recommend you name them by using format like `SomethingMP`.

All methods declared in MPs **MUST EXACTLY** match the methods in the underlying class declared in the value of
 `@MountPoint` annotation.

Any situation which does NOT match the standard and NOT EXCLUDED will cause the mounting failure.

However, the following situations are **EXCLUDED**:

  a. Any the argument types in the MP method is MP, and there is a method's argument type match the origin classes 
      of the MP argument types. 
  
  For example:
```java
public class Packet { /*...*/ }

// -- NEW FILE --

public class Connection {
    public void send(Packet packet) {
        // code goes here
    }
}
```

  The following MP example hits this situation:

```java
@MountPoint("xxx.Packet")
public interface PacketMP { /*...*/ }

@MountPoint("xxx.Connection")
public interface ConnectionMP {
    void send(PacketMP packet); // matches Connection#send(Packet)
}
```

  b. The argument type (or return type) is annotated with `@RuntimeType` annotation.

  The `@RuntimeType` annotation suppress the type check.

  c. The method is annotated with `@Redirect` annotation.

  The `@Redirect` annotation allows you to change the name of the target name in the MP.

  d. The method is a kind of field accessor.

  For example:

```java
public class Thing {
    private boolean var0;
    private final boolean var1 = false;
}

@MountPoint("xxx.Thing")
public interface ThingMP {
    @AccessField("var0")
    boolean getVar0();
    
    @AccessField("var0")
    void setVar0(boolean var0);
    
    @AccessField
    boolean var0();
    
//  The following method can NOT pass check, because var1 is final.
//    @AccessField("var1")
//    void setVar1(boolean var1);
}
```

  In this example, we won't try to look up a method named "getVar0", "setVar0" or "var0".

  If there is a method name conflicted with them, we'll ignore it.
  If you want to call the real underlying implementation of the conflict methods, use `@Redirect` instead.

### 1.3.1  Default methods

For default methods in the MP interfaces, we'll **try** to use its method signature to match the method in the
 underlying class.

If found, use the underlying method as the implementation in the resulting instance.
 If not found, use the default implementation.

## 1.4  Annotation

Thanks to the Java annotation type, it is possible to declare metadata for things in a graceful form.

In this section, I'll explain the annotations provided by the library.

### 1.4.1  MountPoint

The `@MountPoint` annotation is used to mark the interfaces which will be regarded as a MP.

The requirements of a valid MP declaration was written in [section 1.3](#13--mount-point)

### 1.4.2  Redirect

The `@Redirect` annotation is used to mark the MP methods which will be regarded as another **method** in the
 underlying class which has the **compatible** return type and argument type.

For example:

```java
public class Thing {
    public void doSth() {
        // code goes here
    }
}

@MountPoint("xx.Thing")
public interface ThingMP {
    @Redirect("doSth")
    void sth();
}
```

We won't check if there is a method named `Thing#sth`, we'll regard the `ThingMP#doSth`
 as `Thing#doSth`.

### 1.4.3  AccessField

The `@AccessField` annotation marks the MP method as an accessor of a field declared in the underlying class.

The value of it is optional, if no value provided, the method name will be used to look up the field in the
 underlying class.

The method marked with `@AccessField` annotation has several forms:

  a. Return type is `FieldAccessor`
  
  For example:

```java
public class Something {
    // ...
}

public class Thing {
    Something var0;
}
```

```java
@MountPoint("xx.Thing")
public interface ThingMP {
    @AccessField
    FieldAccessor<SomethingMP> var0(); // will look up var0 field in Thing
 
    @AccessField("var0")
    FieldAccessor<SomethingMP> var0Access();
}
```

  The more information about `FieldAccessor` at [section 1.5](#15--field-accessor) is available.

  b. Return type is NOT `FieldAccessor`
  
  At this time, the methods in this form should follow a part of the JavaBean specification:
    For getters, return type is the type of the field, and it takes no argument. For setters, takes one argument,
    and its type is the type of the field.
  
  In this specification, you needn't to let the methods in this form use the actual underlying class type. Just use MP,
    we'll handle it.
  
  For example:

```java
@MountPoint("xx.Thing")
public interface ThingMP {
    @AccessField
    SomethingMP var0(); // will look up var0 field in Thing
 
    @AccessField("var0")
    void var0(SomethingMP somethingToSet);
}
```

### 1.4.4  RuntimeType

We'll try our best to prevent the wrong type for your code.

So we will check your interface on mounting to ensure it fits the underlying
 class.

But we know you need to suppress the type check in some special situations,
 so the `@RuntimeType` annotation can help you to solve this problem.

**WARNING**: Do NOT use this annotation if possible. Or you should pay
 attention to the arguments you'll pass to the method.

It can be used at methods declaration and argument type declaration.

The most important thing is that this annotation does **NOT** remove the type check,
 we just delayed it so that it will work on call.

For example:

```java
public class Thing {
    
    public Thing operation(Thing anotherThing) {
        // code...
    }
}

// -- NEW FILE --

@MountPoint("xxx.Thing")
public interface ThingMP {
    
    @RuntimeType // don't care the type, just return Object
    Object operation(@RuntimeType Object anotherThing);
    
    // this still work
    ThingMP operation(ThingMP anotherThing);
}
```

## 1.5  Field Accessor

In the [section 1.4.3](#143--accessfield), we've already introduced the `@AccessField` annotation
 to you, this section is used to fully explain the details about `FieldAccessor`.

The code of FieldAccessor just like:

```java
import java.lang.reflect.Field;

// The "T" type means the MP type of the underlying field
public interface FieldAccessor<T> {

    Object get();

    void set(Object newValue);

    // Fail if the T type is unknown for this instance
    T getMounted() throws IllegalStateException;

    boolean isFinal();

    Field getUnderlyingField();
}
```

When we detected a method with `@AccessField` annotation, if it wants to return `FieldAccessor`,
 we'll get the data type of the underlying field, and compare it with the
 parameter type "T" which is declared with the method, if the underlying type equals to the underlying
 type provided by "T" type, or the underlying type of "T" type is superclass of the underlying type of
 the underlying field, the check passed.

But there is some exception:

  a. The "T" type is the wrapper type of Java standard data type, or String.
  
  It's OK to use declarations like `FieldAccessor<Integer>` and `FieldAccessor<String>`.
  
  b. The "T" type is `?`
  
  It means you don't have an available MP type for the underlying field, at this time, the `getMounted`
   method of the `FieldAccessor` produced by the MP method won't work (will throw `IllegalStateException`).
  
  Tips: `? extends XXX` or `? super XXX` won't work. A range is useless for us.
  
If you have a field with `Object` type, feel free to use `FieldAccessor<?>`, and  you don't
 need to use `FieldAccessor<Object>`, because it is useless.

## 1.6  Constructor

We know you maybe need to construct some origin things to use. (e.g. Creating a Minecraft packet)

So we also provide ways for you to create origin instances. You also can wrap the result into MP with a
 existing MP interface at once by using special forms of constructors we provided.

The constructor-related methods will be provided by `Mount` objects.

Their method signatures are:

    Mount#findConstructor(String originClassName, Class<?>... argTypes) -> java.lang.reflect.Constructor<?>
    Mount#findWrappedConstructor(String originClassName, Class<?>[] argTypes[, Function<java.lang.reflect.Constructor<?>, WrappedConstructor>]) -> WrappedConstructor
    Mount#findConstructorMP(Class<T> mp, Class<?> argTypes[, Function<java.lang.reflect.Constructor<?>, WrappedConstructor>]) -> ConstructorMP<T>

The `Function<java.lang.reflect.Constructor<?>, WrappedConstructor>` is optional if you've specified it
 by using the `wrappedConstructorProvider` method in the builder of `Mount`.

`argTypes` can contain the `MP` types, they will be converted into original classes if needed.

`originClassName` is the **remapped** name of the origin class, which means the patterns in the name
 should be already replaced by using the correct thing.

The constructor family in this specification is:

`ConstructorMP` wraps `WrappedConstructor`, `WrappedConstructor` wraps `java.lang.reflect.Constructor<?>`.

`ConstructorMP` can wrap the result from the real constructor into MP instances.

`WrappedConstructor` is just an interface, but it can have different implementations (e.g. MethodHandle based)
 to avoid the performance problem caused by the Java reflection API.

If you need to access the origin constructor from the `WrappedConstructor`, feel free to use its
 `getUnderlyingConstructor` method.

`WrappedConstructor` can be provided by the `getUnderlyingWrappedConstructor` method of `ConstructorMP`.

The constructor instances will **NOT** be cached, we'll look up them on every call. So manage it by yourself.

