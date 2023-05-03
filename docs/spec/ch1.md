# Chapter 1 - The API

## 1.1  Definition

### 1.1.1  Origin & Mount Point

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

## 1.4 Annotation

TODO
