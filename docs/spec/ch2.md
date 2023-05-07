# Chapter 2 - The mechanism

This chapter will explain some internal mechanism of this library.

## 2.1  Auto mount/unmount

For example, `FieldAccessor#getMounted` method will auto convert the origin object into the MP instance.

Another example, we'll auto convert the possible MP objects/classes into the origin classes when we
 looking up the constructors.

That's auto mount/unmount.

## 2.2  Extending MP

A MP interface can extend other MP interface like this:

```java
public class Thing {
    // ...
}

// --- NEW FILE ---

public class EnhancedThing extends Thing {
    // ...
}

// --- NEW FILE ---

@MountPoint("xxx.Thing")
public interface ThingMP {
    // ...
}

@MountPoint("xxx.EnhancedThing")
public interface EnhancedThingMP extends ThingMP {
    // methods from EnhancedThing ...
}
```

This mechanism helps you to write MP interfaces faster, because you won't need to write the same methods
 from superclasses anymore.

MP interfaces can also extend a normal interface, but only default methods from the normal interfaces are accepted.

Which means, if there is an unimplemented method in a normal interface, and a MP extended that normal interface,
 the MP is invalid at this time. Because we don't know what is the implementation of that method.
