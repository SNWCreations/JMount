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
@MountPoint("xx.Thing")
public interface ThingMP {
    @AccessField
    FieldAccessor var0(); // will look up var0 field in Thing
 
    @AccessField("var0")
    FieldAccessor var0Access();
}
```

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

